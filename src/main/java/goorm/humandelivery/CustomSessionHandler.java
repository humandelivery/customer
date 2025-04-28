package goorm.humandelivery;

import goorm.humandelivery.dto.*;
import org.springframework.messaging.simp.stomp.*;
import java.lang.reflect.Type;
import java.util.Scanner;
class CustomSessionHandler extends StompSessionHandlerAdapter {

    private final String jwtToken;

    public CustomSessionHandler(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders){
        System.out.println("WebSocket 연결 완료");

        // 사용자에게 콜 정보 입력 받기
        Scanner scanner = new Scanner(System.in);

        String expectedOriginAddress = scanner.nextLine();
        String expectedDestinationAddress = scanner.nextLine();
        String taxiType = scanner.nextLine();

        // 주소를 위도, 경도로 변환
        Location originLocation = safeConvertAddress(expectedOriginAddress);
        Location destinationLocation = safeConvertAddress(expectedDestinationAddress);

        if (originLocation != null && destinationLocation != null) {
            // 콜 요청 객체 생성
            CallRequest callRequest = new CallRequest(originLocation, destinationLocation, taxiType);

            // JWT 포함한 헤더 세팅
            StompHeaders stompHeaders = new StompHeaders();
            stompHeaders.setDestination("/app/call");
            stompHeaders.add("Authorization", "Bearer " + jwtToken);

            // 콜 요청 전송
            session.send(stompHeaders, callRequest);
            System.out.println("콜 요청 전송 완료");

            // 콜 요청 후에 구독을 등록
            subscribeTaxiInfo(session);
            subscribeTaxiLocation(session);
            subscribeTaxiResult(session);

        } else {
            System.out.println("주소 변환 실패, 콜 요청을 진행할 수 없습니다.");
        }
    }

    private void subscribeTaxiInfo(StompSession session) {
        StompHeaders headers = new StompHeaders();
        headers.setDestination("/topic/taxi/info");
        headers.add("Authorization", "Bearer " + jwtToken);

        session.subscribe(headers, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return TaxiInfo.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                TaxiInfo taxiInfo = (TaxiInfo) payload;
                System.out.println("택시 정보 수신: " + taxiInfo);
            }
        });
    }

    private void subscribeTaxiLocation(StompSession session) {
        StompHeaders headers = new StompHeaders();
        headers.setDestination("/user/queue/update-taxidriver-location");
        headers.add("Authorization", "Bearer " + jwtToken);

        session.subscribe(headers, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return TaxiLocation.class;  // TaxiLocation 클래스를 사용
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                // TaxiLocation에서 Location을 가져옴
                TaxiLocation taxiLocation = (TaxiLocation) payload;
                Location location = taxiLocation.getLocation();  // Location 객체

                // 좌표 출력
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                // 좌표를 주소로 변환하는 부분
                String address = safeConvertCoordinates(latitude, longitude);  // 안전한 변환 호출
                if (address != null) {
                    System.out.println("변환된 주소: " + address);
                } else {
                    System.out.println("주소를 변환할 수 없습니다.");
                }
            }
        });
    }

    private void subscribeTaxiResult(StompSession session) {
        StompHeaders headers = new StompHeaders();
        headers.setDestination("/topic/taxi/result");
        headers.add("Authorization", "Bearer " + jwtToken);

        session.subscribe(headers, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return TaxiResult.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                TaxiResult taxiResult = (TaxiResult) payload;
                System.out.println("운행 결과 수신: " + taxiResult);
                session.disconnect();
                System.exit(0);
            }
        });
    }

    @Override
    public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
        System.err.println("WebSocket 예외 발생: " + exception.getMessage());
        exception.printStackTrace();
    }

    @Override
    public void handleTransportError(StompSession session, Throwable exception) {
        System.err.println("WebSocket 전송 오류: " + exception.getMessage());
        exception.printStackTrace();
    }

    // 주소를 위도, 경도로 변환하는 메서드
    private Location safeConvertAddress(String address) {
        try {
            return KakaoMap.convertAddressToLocation(address);
        } catch (Exception e) {
            System.err.println("주소 변환 중 오류 발생: " + address);
            e.printStackTrace();
            return null;
        }
    }

    // 위도, 경도를 주소로 변환하는 메서드
    private String safeConvertCoordinates(double latitude, double longitude) {
        try {
            // 좌표를 주소로 변환
            return KakaoMap.convertCoordinatesToAddress(latitude, longitude);
        } catch (Exception e) {
            System.err.println("좌표 변환 중 오류 발생: 위도 = " + latitude + ", 경도 = " + longitude);
            e.printStackTrace();
            return null;  // 변환 실패 시 null 반환
        }
    }
}
