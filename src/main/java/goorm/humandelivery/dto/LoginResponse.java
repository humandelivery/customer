package goorm.humandelivery.dto;

public record LoginResponse(
        String message,
        String jwtToken
) {
}
