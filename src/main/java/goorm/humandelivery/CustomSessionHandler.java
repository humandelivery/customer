package goorm.humandelivery;

import goorm.humandelivery.dto.*;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

import java.lang.reflect.Type;
import java.util.Scanner;



class CustomSessionHandler extends StompSessionHandlerAdapter {

    private final String jwtToken;

    public CustomSessionHandler(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        System.out.println("WebSocket 연결 완료"); // 추후 제거 예정 , 확인용

        // 사용자에게 콜 정보 입력 받기
        Scanner scanner = new Scanner(System.in);
        System.out.println("출발지, 도착지, 택시종류(일반, 모범, 우버 등등)을 입력해주세요 ");
        String expectedOrigin = scanner.nextLine();
        String expectedDestination = scanner.nextLine();
        String taxiType = scanner.nextLine();

        // 콜 요청 객체 생성
        CallRequest callRequest = new CallRequest(expectedOrigin, expectedDestination, taxiType);

        // JWT 포함한 헤더 세팅
        StompHeaders stompHeader = new StompHeaders();
        stompHeader.setDestination("/app/call"); // 목적지 주소는 추후 변경 예정
        stompHeader.set("Authorization", "Bearer " + jwtToken);

        // 콜 요청 전송
        session.send(stompHeader, callRequest);
        System.out.println("콜 요청 전송 완료");

        session.subscribe("/topic/call/result", new StompFrameHandler() { // 목적지 주소는 추후 변경 예정
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return CallResponse.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                CallResponse callResponse = (CallResponse) payload;
                System.out.println("배차 완료 " + callResponse);
            }
        });

        session.subscribe("/topic/taxi/info", new StompFrameHandler() { // 목적지 주소는 추후 변경 예정
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

        //  택시 위치 주기적 수신
        session.subscribe("/topic/taxi/location", new StompFrameHandler() { // 목적지 주소는 추후 변경 예정
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return TaxiLocation.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                TaxiLocation taxiLocation = (TaxiLocation) payload;
                System.out.println("택시 현재 위치: " + taxiLocation);
            }
        });
        //  운행 결과 수신
        session.subscribe("/topic/taxi/result", new StompFrameHandler() { // 목적지 주소는 추후 변경 예정
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return TaxiResult.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                TaxiResult rideResult = (TaxiResult) payload;
                System.out.println("운행 결과 수신: " + rideResult);

                System.out.println("계속 연결하시겠습니까? (Y/N):");
                String input = scanner.nextLine();
                if (input.equalsIgnoreCase("N")) {
                    System.out.println("연결 종료");
                    session.disconnect();
                    System.exit(0); // 자바 프로그램 종료
                } else {
                    System.out.println("연결 유지");
                    // 연결 유지? 초기화면으로 돌아감?
                }
            }
        });
    }
}
