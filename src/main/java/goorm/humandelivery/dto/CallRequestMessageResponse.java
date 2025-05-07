package goorm.humandelivery.dto;

public class CallRequestMessageResponse {
    private String message;
    private String status;
    private Long callId;

    public CallRequestMessageResponse() {}

    public CallRequestMessageResponse(String message, String status, Long callId) {
        this.message = message;
        this.status = status;
        this.callId = callId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public Long getCallId() {
        return callId;
    }
    public void setCallId(Long callId) {
        this.callId = callId;
    }

    @Override
    public String toString() {
        return  message ;
    }
}