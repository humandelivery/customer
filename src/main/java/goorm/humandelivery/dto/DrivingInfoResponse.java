package goorm.humandelivery.dto;

public class DrivingInfoResponse {
    private boolean isDrivingStarted;
    private boolean isDrivingFinished;

    public boolean isDrivingStarted() {
        return isDrivingStarted;
    }
    public void setDrivingStarted(boolean isDrivingStarted) {
        this.isDrivingStarted = isDrivingStarted;
    }
    public boolean isDrivingFinished() {
        return isDrivingFinished;
    }
    public void setDrivingFinished(boolean isDrivingFinished) {
        this.isDrivingFinished = isDrivingFinished;
    }
}
