package goorm.humandelivery;

import goorm.humandelivery.dto.Location;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class KakaoMap {

    private static String GEOCODE_URL = "https://dapi.kakao.com/v2/local/search/address.json?query=";
    private static String REVERSE_GEOCODE_URL = "https://dapi.kakao.com/v2/local/geo/coord2address.json?x=";
    private static String GEOCODE_USER_INFO = "KakaoAK 34e02894a927a49fe302cffac7e9c50f";  // 발급받은 REST API 키
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 주소를 위도, 경도로 변환하는 메서드 (Location 객체 반환)
    public static Location convertAddressToLocation(String address) throws Exception {
        // 주소 인코딩
        String encodedAddress = java.net.URLEncoder.encode(address, "UTF-8");
        URI uri = new URI(GEOCODE_URL + encodedAddress);

        // HttpClient를 사용한 요청
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Authorization", GEOCODE_USER_INFO)
                .header("Content-Type", "application/json")
                .build();

        // 요청 보내기
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // 응답 코드 확인
        if (response.statusCode() == 200) {
            String responseBody = response.body();

            // JSON 파싱을 위한 ObjectMapper 사용
            JsonNode rootNode = objectMapper.readTree(responseBody);

            // documents 배열이 비어있지 않은지 확인
            if (rootNode.has("documents") && rootNode.get("documents").size() > 0) {
                JsonNode firstDocument = rootNode.get("documents").get(0);

                // x와 y 값 추출
                String lat = firstDocument.get("y").asText(); // 위도
                String lng = firstDocument.get("x").asText(); // 경도

                // Location 객체 반환
                return new Location(Double.parseDouble(lat), Double.parseDouble(lng));
            } else {
                System.out.println("검색 결과가 없습니다");
            }
        } else {
            System.out.println("HTTP 요청 실패: " + response.statusCode());
        }

        return null; // 실패 시 null 반환
    }

    // 위도, 경도를 주소로 변환하는 메서드
    public static String convertCoordinatesToAddress(double latitude, double longitude) throws Exception {
        // 좌표를 URL 인코딩하여 요청
        String uri2 = REVERSE_GEOCODE_URL + longitude + "&y=" + latitude;
        URI uri = new URI(uri2);


        // HttpClient를 사용한 요청
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Authorization", GEOCODE_USER_INFO)
                .header("Content-Type", "application/json")
                .build();

        // 요청 보내기
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // 응답 코드 확인
        if (response.statusCode() == 200) {
            String responseBody = response.body();

            // JSON 파싱을 위한 ObjectMapper 사용
            JsonNode rootNode = objectMapper.readTree(responseBody);

            // documents 배열이 비어있지 않은지 확인
            if (rootNode.has("documents") && rootNode.get("documents").size() > 0) {
                JsonNode firstDocument = rootNode.get("documents").get(0);

                // 주소 값 추출
                String address = firstDocument.get("address_name").asText();
                return address;  // 주소 반환
            } else {
                System.out.println("주소를 찾을 수 없습니다.");
            }
        } else {
            System.out.println("HTTP 요청 실패: " + response.statusCode());
        }

        return null;  // 실패 시 null 반환
    }
}
