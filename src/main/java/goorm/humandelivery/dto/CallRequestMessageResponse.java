package goorm.humandelivery.dto;

public class CallRequestMessageResponse {
    private String message;

    public CallRequestMessageResponse() {}

    public CallRequestMessageResponse(String message) {
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