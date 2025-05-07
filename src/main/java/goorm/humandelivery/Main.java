package goorm.humandelivery;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

public class Main {

    public static void main(String[] args) throws Exception {

        Scanner scanner = new Scanner(System.in);

        // 명령줄 인자 파싱
        String loginId = scanner.nextLine();
        String password = scanner.nextLine();
        String originAddress = scanner.nextLine();
        String destinationAddress = scanner.nextLine();
        String taxiType = scanner.nextLine();

        // REST 로그인 & jwt 토큰 획득
        String restLoginUrl = "http://localhost:8080/api/v1/customer/auth-tokens";

        String jsonBody = String.format("{\"loginId\": \"%s\", \"password\": \"%s\"}", loginId, password);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(restLoginUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // JSON 파싱
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String responseBody = response.body();

        System.out.println("로그인 응답 내용: " + responseBody);

        // 로그인 성공 여부 판단
        if (response.statusCode() == 200) { // 200 OK
            String jwtToken = objectMapper.readTree(responseBody)
                    .get("accessToken")
                    .asText();

            System.out.println("JWT 토큰 발급 성공: " + jwtToken);

            // WebSocket & STOMP 클라이언트 구성
            StandardWebSocketClient standardWebSocketClient = new StandardWebSocketClient();
            WebSocketStompClient stompClient = new WebSocketStompClient(standardWebSocketClient);

            // MappingJackson2MessageConverter에 JavaTimeModule 추가
            MappingJackson2MessageConverter messageConverter = new MappingJackson2MessageConverter();
            ObjectMapper converterObjectMapper = new ObjectMapper();
            converterObjectMapper.registerModule(new JavaTimeModule());
            messageConverter.setObjectMapper(converterObjectMapper);

            stompClient.setMessageConverter(messageConverter);

            String wsUrl = "ws://localhost:8080/ws";

            WebSocketHttpHeaders httpHeaders = new WebSocketHttpHeaders();
            StompHeaders stompHeaders = new StompHeaders();
            stompHeaders.add("Authorization", jwtToken);

            // 커스텀 세션 핸들러에 사용자 입력을 전달
            CustomSessionHandler sessionHandler = new CustomSessionHandler(originAddress, destinationAddress, taxiType);
            Future<StompSession> future = stompClient.connectAsync(wsUrl, httpHeaders, stompHeaders, sessionHandler);

            // 메인 스레드를 살아있게 유지하기 위해 CountDownLatch 사용
            CountDownLatch latch = new CountDownLatch(1);
            latch.await(); // latch가 열릴 때까지 대기

        } else {
            String errorMessage = objectMapper.readTree(responseBody)
                    .get("error")
                    .asText();
            System.out.println("로그인 실패: " + errorMessage);
            System.exit(1); // 프로그램 종료
        }
    }
}