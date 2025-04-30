package goorm.humandelivery;

import goorm.humandelivery.dto.*;
import org.springframework.messaging.simp.stomp.*;
import java.lang.reflect.Type;
import java.util.*;

class CustomSessionHandler extends StompSessionHandlerAdapter {

    private final ClientStatusContext statusContext = new ClientStatusContext();
    private final MessageStorage messageStorage = new MessageStorage();

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        System.out.println("WebSocket 연결 완료");

        Scanner scanner = new Scanner(System.in);
        String expectedOriginAddress = scanner.nextLine();
        String expectedDestinationAddress = scanner.nextLine();
        String taxiType = scanner.nextLine();

        Location originLocation = safeConvertAddress(expectedOriginAddress);
        Location destinationLocation = safeConvertAddress(expectedDestinationAddress);
        System.out.println("좌표 변환 완료");

        if (originLocation != null && destinationLocation != null) {
            CallRequest callRequest = new CallRequest(originLocation, destinationLocation, taxiType);
            StompHeaders headers = new StompHeaders();
            headers.setDestination("/app/call/request");
            session.send(headers, callRequest);
            System.out.println("콜 요청 전송 완료");

            subscribeCallResponse(session);
            subscribeTaxiInfo(session);
            subscribeTaxiLocation(session);
            subscribeTaxiResult(session);
        } else {
            System.out.println("주소 변환 실패, 콜 요청을 진행할 수 없습니다.");
        }
    }

    private void subscribeCallResponse(StompSession session) {
        StompHeaders headers = new StompHeaders();
        headers.setDestination("/user/queue/call/response");
        session.subscribe(headers, new StompFrameHandler() {
            public Type getPayloadType(StompHeaders headers) { return CallRequestMessageResponse.class; }
            public void handleFrame(StompHeaders headers, Object payload) {
                CallRequestMessageResponse response = (CallRequestMessageResponse) payload;
                System.out.println("콜 요청 수신 완료: " + response);
                statusContext.setState(ClientState.READY);
                messageStorage.processPendingMessages(statusContext);
            }
        });
    }

    private void subscribeTaxiInfo(StompSession session) {
        StompHeaders headers = new StompHeaders();
        headers.setDestination("/user/queue/accept-call-result");
        session.subscribe(headers, new StompFrameHandler() {
            public Type getPayloadType(StompHeaders headers) { return TaxiInfo.class; }
            public void handleFrame(StompHeaders headers, Object payload) {
                TaxiInfo taxiInfo = (TaxiInfo) payload;
                if (statusContext.getState() != ClientState.READY) {
                    messageStorage.storeTaxiInfo(taxiInfo);
                    return;
                }

                System.out.println("택시 정보 수신(배차 완료): " + taxiInfo);
                statusContext.setState(ClientState.MATCHED);
                messageStorage.processPendingMessages(statusContext);
            }
        });
    }

    private void subscribeTaxiLocation(StompSession session) {
        StompHeaders headers = new StompHeaders();
        headers.setDestination("/user/queue/driving-location");
        session.subscribe(headers, new StompFrameHandler() {
            public Type getPayloadType(StompHeaders headers) { return TaxiLocation.class; }
            public void handleFrame(StompHeaders headers, Object payload) {
                TaxiLocation taxiLocation = (TaxiLocation) payload;
                if (statusContext.getState() != ClientState.MATCHED) {
                    messageStorage.storeTaxiLocation(taxiLocation);
                    return;
                }
                Location location = taxiLocation.getLocation(); // 좌표 -> 주소 로직을 추가해서 나타낼때 좌표대신 주소로 해야하나?
                System.out.println("택시 위치 수신: " + location);
                statusContext.setState(ClientState.MOVING);
                messageStorage.processPendingMessages(statusContext);
            }
        });
    }

    private void subscribeTaxiResult(StompSession session) {
        StompHeaders headers = new StompHeaders();
        headers.setDestination("/queue/taxi/result");
        session.subscribe(headers, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) { return TaxiResult.class; }
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                TaxiResult result = (TaxiResult) payload;
                if (statusContext.getState() != ClientState.COMPLETED) {
                    messageStorage.storeTaxiResult(result);
                    return;
                }
                System.out.println("운행 결과 수신: " + result);
                session.disconnect();
                System.exit(0);
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

    private String safeConvertCoordinates(double latitude, double longitude) {
        try {
            return KakaoMap.convertCoordinatesToAddress(latitude, longitude);
        } catch (Exception e) {
            System.err.println("좌표 변환 중 오류 발생");
            e.printStackTrace();
            return null;
        }
    }
}