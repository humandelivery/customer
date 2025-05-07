package goorm.humandelivery.dto;

import java.time.LocalDateTime;

public class CallCancelRequest {
    String reason;
    LocalDateTime date;
    Long callId;

    public CallCancelRequest() {}
    public CallCancelRequest(String reason, LocalDateTime date,Long callId) {
        this.reason = reason;
        this.date = date;
        this.callId = callId;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }


    public LocalDateTime getDate() {
        return date;
    }

    public String getReason() {
        return reason;
    }
    public Long getCallId() {
        return callId;
    }
    public void setCallId(Long callId) {
        this.callId = callId;
    }
}
