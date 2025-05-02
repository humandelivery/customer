package goorm.humandelivery.dto;

public class CancelRequest {
    private String message;

    public CancelRequest() {}
    public CancelRequest(String message) {
        this.message = message;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    @Override
    public String toString() {
        return  message;
    }
}
