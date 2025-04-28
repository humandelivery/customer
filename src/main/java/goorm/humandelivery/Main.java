package goorm.humandelivery;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;
import java.util.concurrent.Future;

public class Main {

    public static void main(String[] args) throws Exception {

        Scanner scanner = new Scanner(System.in);
        // REST 로그인 & jwt 토큰 획득
        String loginId = scanner.nextLine();
        String password = scanner.nextLine();
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
        String responseBody = response.body();

        // 로그인 성공 여부 판단
        if (response.statusCode() == 200) { // 200 OK
            String jwtToken = objectMapper.readTree(responseBody)
                    .get("token")
                    .asText();

            System.out.println("JWT 토큰 발급 성공: " + jwtToken);

            // WebSocket & STOMP 클라이언트 구성
            StandardWebSocketClient standardWebSocketClient = new StandardWebSocketClient();
            WebSocketStompClient stompClient = new WebSocketStompClient(standardWebSocketClient);
            stompClient.setMessageConverter(new MappingJackson2MessageConverter());

            String wsUrl = "ws://localhost:8080/ws";

            CustomSessionHandler sessionHandler = new CustomSessionHandler(jwtToken);
            Future<StompSession> future = stompClient.connectAsync(wsUrl, sessionHandler);

            Thread.currentThread().join();

        } else {
            String errorMessage = objectMapper.readTree(responseBody)
                    .get("error")
                    .asText();
            System.out.println("로그인 실패: " + errorMessage);
            System.exit(1); // 프로그램 종료
        }
    }
}