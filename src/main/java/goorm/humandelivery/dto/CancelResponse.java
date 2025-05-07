package goorm.humandelivery.dto;

public class CancelResponse {
    String message;
    Long callId;
    public CancelResponse(){}
    public CancelResponse(String message, Long callId) {
        this.message = message;
        this.callId = callId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getCallId() {
        return callId;
    }

    public void setCallId(Long callId) {
        this.callId = callId;
    }
}
