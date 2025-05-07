package goorm.humandelivery;

import goorm.humandelivery.dto.*;
import org.springframework.messaging.simp.stomp.*;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

import static goorm.humandelivery.dto.DrivingStatus.COMPLETE;

class CustomSessionHandler extends StompSessionHandlerAdapter {

    private final ClientStatusContext statusContext = new ClientStatusContext();
    private final MessageStorage messageStorage = new MessageStorage();
    private final CallRetryHandler callRetryHandler = new CallRetryHandler();
    private StompSession.Subscription locationSubscription; // 구독을 해제하기 위해서 필요함
    private StompSession session; // 취소 요청을 위해 세션 저장
    private final AtomicBoolean canCancelCall = new AtomicBoolean(true); // 콜 취소 가능 상태 플래그
    private final AtomicBoolean canCancelMatch = new AtomicBoolean(false); // 매칭 취소 가능 상태 플래그
    private final AtomicBoolean cancelResponseReceived = new AtomicBoolean(false); // 응답 수신 후 스레드 종료를 위한 플래그


    private Long callId;

    // 사용자 입력을 저장할 필드 추가
    private final String originAddress;
    private final String destinationAddress;
    private final String taxiType;

    // 생성자 추가
    public CustomSessionHandler(String originAddress, String destinationAddress, String taxiType) {
        this.originAddress = originAddress;
        this.destinationAddress = destinationAddress;
        this.taxiType = taxiType;

        // 취소 입력을 기다리는 스레드 시작
        startCancelListenerThread();
    }

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        System.out.println("WebSocket 연결 완료");
        this.session = session; // 세션 저장

        subscribeErrorMessages(session);
        subscribeDispatchError(session);
        subscribeCancelResponse(session);

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
            System.out.println("콜 취소: 'cancel' 입력 (배차 전까지만 가능)");
            System.out.println("매칭 취소: 'matchCancel' 입력 (배차 후 이동 시작 전까지 가능)");

            subscribeCallResponse(session);
            subscribeDispatchStatus(session);
            subscribeDrivingStart(session);
            subscribeDrivingFinish(session);

        } else {
            System.out.println("주소 변환 실패, 콜 요청을 진행할 수 없습니다.");
        }
    }

    private void startCancelListenerThread() {
        Thread cancelThread = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {

                if (cancelResponseReceived.get()) {
                    System.out.println("취소 응답을 받았으므로 종료합니다.");
                    break;
                }

                String input = scanner.nextLine().trim();

                // 일반 콜 취소 처리
                if ("cancel".equalsIgnoreCase(input)) {
                    if (canCancelCall.get() && statusContext.getState() == ClientState.READY) {
                        sendCancelRequest();
                    } else {
                        System.out.println("현재 상태에서는 콜 취소할 수 없습니다. 매칭 취소를 원하시면 'matchCancel'를 입력하세요.");
                    }
                }

                // 매칭 취소 처리
                else if ("matchCancel".equalsIgnoreCase(input)) {
                    if (canCancelMatch.get() && statusContext.getState() == ClientState.MATCHED) {
                        sendMatchCancelRequest();
                    } else {
                        System.out.println("현재 상태에서는 매칭 취소할 수 없습니다. (이동 중이거나 매칭 전 상태)");
                    }
                }

                // 프로그램이 종료된 경우 스레드도 종료
                if (statusContext.getState() == ClientState.COMPLETED ||
                        statusContext.getState() == ClientState.CANCELED) {
                    break;
                }

                try {
                    Thread.sleep(100); // CPU 사용량 줄이기
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        cancelThread.setDaemon(true); // 메인 스레드가 종료되면 같이 종료
        cancelThread.start();
    }

    private void sendCancelRequest() {
        if (session != null) {
            StompHeaders headers = new StompHeaders();
            headers.setDestination("/call/cancel/request");
            // 일반 콜 취소 요청
            CallCancelRequest cancelRequest = new CallCancelRequest("call cancel", LocalDateTime.now(),callId);
            session.send(headers, cancelRequest);
            System.out.println("콜 취소 요청을 전송했습니다.");
            canCancelCall.set(false);
            statusContext.setState(ClientState.CANCELED);
        } else {
            System.out.println("취소 요청을 보낼 수 없습니다. 세션이 없습니다.");
        }
    }

    private void sendMatchCancelRequest() {
        if (session != null) {
            StompHeaders headers = new StompHeaders();
            headers.setDestination("/match/cancel/request");
            // 매칭 취소 요청
            MatchCancelRequest MatchCancelRequest = new MatchCancelRequest("match cancel",LocalDateTime.now(),callId);
            session.send(headers, MatchCancelRequest);
            System.out.println("매칭 취소 요청을 전송했습니다.");
            canCancelMatch.set(false);
            statusContext.setState(ClientState.CANCELED);
        } else {
            System.out.println("취소 요청을 보낼 수 없습니다. 세션이 없습니다.");
        }
    }

    private void subscribeCancelResponse(StompSession session) {
        StompHeaders headers = new StompHeaders();
        headers.setDestination("/queue/cancel/response");
        session.subscribe(headers, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return CancelResponse.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                CancelResponse cancelResponse = (CancelResponse) payload;
                System.out.println("서버로부터 취소 응답 수신");
                System.out.println("메시지 : " + cancelResponse.getMessage());
                cancelResponseReceived.set(true);

                if (session != null && session.isConnected()) {
                    try {
                        session.disconnect();
                        System.out.println("세션이 정상적으로 종료되었습니다.");
                        System.exit(0);
                    } catch (Exception e) {
                        System.err.println("세션 종료 중 오류 발생: " + e.getMessage());
                    }
                }
            }
        });
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
                    callId = response.getCallId();
                    canCancelCall.set(true);
                    canCancelMatch.set(false);
                    messageStorage.processPendingMessages(statusContext);
                } else {
                    System.out.println("배차 실패: " + response.getMessage());
                    callRetryHandler.retry(session);
                }
            }
        });
    }

    private void subscribeLocation(StompSession session) {
        StompHeaders headers = new StompHeaders();
        headers.setDestination("/user/queue/update-taxidriver-location");
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

    @SuppressWarnings("unchecked")
    // 배차 완료 메세지
    private void subscribeDispatchStatus(StompSession session) {
        System.out.println("subscribeDispatchStatus 호출");
        StompHeaders headers = new StompHeaders();
        headers.setDestination("/user/queue/dispatch-status");
        session.subscribe(headers, new StompFrameHandler() {
            public Type getPayloadType(StompHeaders headers) {
                return MatchingSuccessResponse.class;
            }

            public void handleFrame(StompHeaders headers, Object payload) {
                System.out.println(" subscribeRideStatus handleFrame 호출");

                try {
                    MatchingSuccessResponse response = (MatchingSuccessResponse)payload;

                    if (response.getTaxiDriverStatus() == TaxiDriverStatus.RESERVED) {
                        System.out.println("고객 탑승 위치로 이동중입니다.");
                        statusContext.setState(ClientState.MATCHED);
                        subscribeLocation(session);
                        canCancelMatch.set(true);
                        canCancelCall.set(false);
                    }
                } catch (Exception e) {
                    System.err.println("메시지 처리 중 오류 발생: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    // 승차 완료 메세지
    private void subscribeDrivingStart(StompSession session) {
        System.out.println("subscribeDispatchStatus 호출");
        StompHeaders headers = new StompHeaders();
        headers.setDestination("/user/queue/driving-start");
        session.subscribe(headers, new StompFrameHandler() {
            public Type getPayloadType(StompHeaders headers) {
                return DrivingInfoResponse.class;
            }

            public void handleFrame(StompHeaders headers, Object payload) {
                System.out.println("subscribeRideStatus handleFrame 호출");
                try {
                    DrivingInfoResponse response = (DrivingInfoResponse) payload;

                    // 승차 완료
                    if (response.isDrivingStarted()) {
                        System.out.println("승차 완료. 목적지로 이동을 시작합니다.");
                        statusContext.setState(ClientState.MOVING);
                        cancelResponseReceived.set(true);

                        if (locationSubscription != null) {
                            locationSubscription.unsubscribe();
                            System.out.println("택시 위치 구독 해제 완료 (승차 완료)");
                        }
                    }
                } catch (Exception e) {
                    System.err.println("메시지 처리 중 오류 발생: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }


    @SuppressWarnings("unchecked")
    // 하차 완료 메세지
    private void subscribeDrivingFinish(StompSession session) {
        System.out.println("subscribeDispatchStatus 호출");
        StompHeaders headers = new StompHeaders();
        headers.setDestination("/user/queue/driving-finish");
        session.subscribe(headers, new StompFrameHandler() {
            public Type getPayloadType(StompHeaders headers) {
                return DrivingSummaryResponse.class;
            }

            public void handleFrame(StompHeaders headers, Object payload) {
                System.out.println("subscribeRideStatus handleFrame 호출");
                try {
                    DrivingSummaryResponse response = (DrivingSummaryResponse) payload;

                    DrivingStatus drivingStatus = response.getDrivingStatus();

                    // 하차 완료
                    if (drivingStatus == COMPLETE) {
                        System.out.println("하차 완료. 운행이 종료되었습니다.");
                        statusContext.setState(ClientState.COMPLETED);
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