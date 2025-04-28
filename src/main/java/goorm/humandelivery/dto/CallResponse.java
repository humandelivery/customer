package goorm.humandelivery.dto;

public class CallResponse {
    private String message;

    public CallResponse() {}

    public CallResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return  message ;
    }
}