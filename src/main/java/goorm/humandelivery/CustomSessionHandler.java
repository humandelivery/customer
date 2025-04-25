package goorm.humandelivery;

import goorm.humandelivery.dto.CallRequest;
import goorm.humandelivery.dto.LoginRequest;
import goorm.humandelivery.dto.LoginResponse;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

import java.lang.reflect.Type;
import java.util.Scanner;

class CustomSessionHandler extends StompSessionHandlerAdapter {

    private StompSession stompSession;

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        this.stompSession = session;

        System.out.println("연결 완료");

        Scanner scanner = new Scanner(System.in);
        System.out.println("아이디, 비밀번호를 입력해주세요 ");
        String id = scanner.nextLine();
        String password = scanner.nextLine();

        // 로그인 요청 객체 생성
        LoginRequest loginRequest = new LoginRequest(id, password);

        // 로그인 요청을 전송
        session.send("/app/api/v1/customer/auth-tokens", loginRequest);
        System.out.println("로그인 정보 전송");

        // 로그인 결과를 받기 위해 구독
        session.subscribe("/topic/auth-tokens/result", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return LoginResponse.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                LoginResponse response = (LoginResponse) payload;

                if ("login success".equals(response.getMessage())) {
                    // 로그인 성공 시 JWT 토큰을 사용하여 헤더 추가
                    StompHeaders stompHeader = new StompHeaders();
                    stompHeader.setDestination("/app/call");  // URL은 추후 변경 예정
                    stompHeader.set("Authorization", "Bearer " + response.getJwtToken());

                    // 사용자에게 출발지, 도착지, 택시 종류를 입력받음
                    Scanner scanner = new Scanner(System.in);
                    System.out.println("출발지, 도착지, 택시종류(일반, 모범, 우버 등등)을 입력해주세요 ");
                    String departPosition = scanner.nextLine();
                    String arrivalPosition = scanner.nextLine();
                    String taxiType = scanner.nextLine();

                    // 콜 요청 객체 생성
                    CallRequest callRequest = new CallRequest(departPosition, arrivalPosition, taxiType);

                    // 콜 요청 객체를 전송
                    stompSession.send(stompHeader, callRequest);
                    System.out.println("콜 요청 전송");
                } else {
                    System.out.println("로그인 실패");
                }
            }
        });
    }
}
