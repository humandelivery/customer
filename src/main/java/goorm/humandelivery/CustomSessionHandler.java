package goorm.humandelivery;

import goorm.humandelivery.dto.*;
import org.springframework.messaging.simp.stomp.*;

import java.lang.reflect.Type;
import java.util.Map;

class CustomSessionHandler extends StompSessionHandlerAdapter {

    private final ClientStatusContext statusContext = new ClientStatusContext();
    private final MessageStorage messageStorage = new MessageStorage();
    private final CallRetryHandler callRetryHandler = new CallRetryHandler();
    private StompSession.Subscription locationSubscription; // 구독을 해제하기 위해서 필요함

    // 사용자 입력을 저장할 필드 추가
    private final String originAddress;
    private final String destinationAddress;
    private final String taxiType;

    // 생성자 추가
    public CustomSessionHandler(String originAddress, String destinationAddress, String taxiType) {
        this.originAddress = originAddress;
        this.destinationAddress = destinationAddress;
        this.taxiType = taxiType;
    }

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        System.out.println("WebSocket 연결 완료");

        subscribeErrorMessages(session);
        subscribeDispatchError(session);

        // 명령줄 인자로 받은 주소 사용
        System.out.println("출발지: " + originAddress);
        System.out.println("목적지: " + destinationAddress);
        System.out.println("택시 타입: " + taxiType);

        Location originLocation = safeConvertAddress(originAddress);
        Location destinationLocation = safeConvertAddress(destinationAddress);
        System.out.println("좌표 변환 완료");

        if (originLocation != null && destinationLocation != null) {
            CallRequest callRequest = new CallRequest(originLocation, destinationLocation, taxiType, 1);
            callRetryHandler.setLastCallRequest(callRequest);
            StompHeaders headers = new StompHeaders();
            headers.setDestination("/app/call/request");
            session.send(headers, callRequest);
            System.out.println("콜 요청 전송 완료");

            subscribeCallResponse(session);
            subscribeTaxiInfo(session);
            subscribeRideStatus(session);
        } else {
            System.out.println("주소 변환 실패, 콜 요청을 진행할 수 없습니다.");
        }
    }

    private void subscribeCallResponse(StompSession session) {
        StompHeaders headers = new StompHeaders();
        headers.setDestination("/user/queue/call/response");
        session.subscribe(headers, new StompFrameHandler() {
            public Type getPayloadType(StompHeaders headers) {
                return CallRequestMessageResponse.class;
            }

            public void handleFrame(StompHeaders headers, Object payload) {
                CallRequestMessageResponse response = (CallRequestMessageResponse) payload;
                System.out.println("콜 응답 수신");

                if ("콜이 성공적으로 요청되었습니다.".equals(response.getMessage())) {
                    System.out.println("배차 가능, 대기 중...");
                    statusContext.setState(ClientState.READY);
                    messageStorage.processPendingMessages(statusContext);
                } else {
                    System.out.println("배차 실패: " + response.getMessage());
                    callRetryHandler.retry(session);
                }
            }
        });
    }

    private void subscribeTaxiInfo(StompSession session) {
        StompHeaders headers = new StompHeaders();
        headers.setDestination("/user/queue/accept-call-result");
        session.subscribe(headers, new StompFrameHandler() {
            public Type getPayloadType(StompHeaders headers) {
                return TaxiInfo.class;
            }

            public void handleFrame(StompHeaders headers, Object payload) {
                TaxiInfo taxiInfo = (TaxiInfo) payload;
                if (statusContext.getState() != ClientState.MATCHED) {
                    messageStorage.storeTaxiInfo(taxiInfo);
                    return;
                }

                System.out.println("택시 정보 수신(배차 완료): " + taxiInfo);
                System.out.println("택시 기사가 출발지로 이동 중입니다...");
                statusContext.setState(ClientState.MATCHED);
            }
        });
    }

    private void subscribeLocation(StompSession session) {
        StompHeaders headers = new StompHeaders();
        headers.setDestination("user/queue/update-taxidriver-location");
        locationSubscription = session.subscribe(headers, new StompFrameHandler() {
            public Type getPayloadType(StompHeaders headers) {
                return Location.class;
            }

            public void handleFrame(StompHeaders headers, Object payload) {
                Location location = (Location) payload;
                System.out.println(location);
            }
        });
    }


    private void subscribeRideStatus(StompSession session) {
        StompHeaders headers = new StompHeaders();
        headers.setDestination("/user/queue/ride-status");
        session.subscribe(headers, new StompFrameHandler() {
            public Type getPayloadType(StompHeaders headers) {
                return Map.class; // Map으로 통일
            }

            public void handleFrame(StompHeaders headers, Object payload) {
                try {
                    Map<String, Object> map = (Map<String, Object>) payload;

                    // 1. RESERVED 상태 처리
                    if (map.containsKey("status")) {
                        String status = (String) map.get("status");
                        if ("RESERVED".equals(status)) {
                            System.out.println("고객 탑승 위치로 이동중입니다.");
                            statusContext.setState(ClientState.MATCHED);
                            subscribeLocation(session);
                        }
                    }
                    // 2. 승차/하차 상태 처리
                    else if (map.containsKey("isDrivingStarted") || map.containsKey("isDrivingFinished")) {
                        boolean isDrivingStarted = false;
                        if (map.containsKey("isDrivingStarted")) {
                            isDrivingStarted = (Boolean) map.get("isDrivingStarted");
                        }

                        boolean isDrivingFinished = false;
                        if (map.containsKey("isDrivingFinished")) {
                            isDrivingFinished = (Boolean) map.get("isDrivingFinished");
                        }

                        // 승차 완료
                        if (isDrivingStarted) {
                            System.out.println("승차 완료. 목적지로 이동을 시작합니다.");
                            statusContext.setState(ClientState.MOVING);

                            if (locationSubscription != null) {
                                locationSubscription.unsubscribe();
                                System.out.println("택시 위치 구독 해제 완료 (승차 완료)");
                            }
                        }

                        // 하차 완료
                        if (isDrivingFinished) {
                            System.out.println("하차 완료. 운행이 종료되었습니다.");
                            statusContext.setState(ClientState.COMPLETED);
                        }
                    }
                    // 3. 운행 완료 요약
                    else if (map.containsKey("drivingStatus")) {
                        String drivingStatus = map.get("drivingStatus").toString();
                        if (drivingStatus.contains("COMPLETE")) {
                            System.out.println("운행이 완료되었습니다.");


                            Long callId = (Long) map.get("callId");
                            System.out.println("운행 요약 정보 수신 완료 . 콜 ID: " + callId);


                            callRetryHandler.shutdown();
                            session.disconnect();
                            System.exit(0);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("메시지 처리 중 오류 발생: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    private void subscribeDispatchError(StompSession session) {
        StompHeaders headers = new StompHeaders();
        headers.setDestination("/user/queue/dispatch-error");
        session.subscribe(headers, new StompFrameHandler() {
            public Type getPayloadType(StompHeaders headers) {
                return CancelRequest.class;
            }

            public void handleFrame(StompHeaders headers, Object payload) {
                CancelRequest cancel = (CancelRequest) payload;
                System.out.println("서버 취소 알림: " + cancel.getMessage());
                statusContext.setState(ClientState.CANCELED);
                CallRequest lastRequest = callRetryHandler.getLastCallRequest();
                if (lastRequest != null) {
                    CallRequest newRequest = new CallRequest(
                            lastRequest.getExpectedOrigin(),
                            lastRequest.getExpectedDestination(),
                            lastRequest.getTaxiType(),
                            1
                    );
                    callRetryHandler.setLastCallRequest(newRequest);
                    session.send("/app/call/request", newRequest);
                    System.out.println("취소 후 새로운 콜 요청 전송");
                } else {
                    System.out.println("이전 콜 요청 정보가 없어 재요청할 수 없습니다.");
                }
            }
        });
    }

    private void subscribeErrorMessages(StompSession session) {
        StompHeaders headers = new StompHeaders();
        headers.setDestination("/user/queue/errors");
        session.subscribe(headers, new StompFrameHandler() {
            public Type getPayloadType(StompHeaders headers) {
                return ErrorResponse.class;
            }

            public void handleFrame(StompHeaders headers, Object payload) {
                ErrorResponse errorResponse = (ErrorResponse) payload;
                System.out.println("에러 메시지 수신: " + errorResponse);

                if (errorResponse.getErrorCode() != null &&
                        "NoAvailableTaxiException".equals(errorResponse.getErrorCode())) {
                    callRetryHandler.retry(session);
                }
            }
        });
    }

    private Location safeConvertAddress(String address) {
        try {
            return KakaoMap.convertAddressToLocation(address);
        } catch (Exception e) {
            System.err.println("주소 변환 중 오류 발생");
            e.printStackTrace();
            return null;
        }
    }
}