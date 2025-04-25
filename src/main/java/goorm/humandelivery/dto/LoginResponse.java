package goorm.humandelivery.dto;

public class LoginResponse {
    private String message;
    private String jwtToken;

    public LoginResponse() {
    }

    public LoginResponse(String message, String jwtToken) {
        this.message = message;
        this.jwtToken = jwtToken;
    }

    public String getMessage() {
        return message;
    }

    public String getJwtToken() {
        return jwtToken;
    }

}
