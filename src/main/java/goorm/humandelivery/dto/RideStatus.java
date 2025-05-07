package goorm.humandelivery.dto;

public class RideStatus {
    private String status;

    public RideStatus() {}

    public RideStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}
