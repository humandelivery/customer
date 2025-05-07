package goorm.humandelivery.dto;

import java.time.LocalDateTime;

public class DrivingSummaryResponse {

    private Long callId;
    private String customerLoginId;
    private String taxiDriverLoginId;
    private Location origin;
    private LocalDateTime pickupTime;
    private Location destination;
    private LocalDateTime arrivingTime;
    private DrivingStatus drivingStatus;
    private boolean reported;

    public void setCallId(Long callId) {
        this.callId = callId;
    }

    public void setCustomerLoginId(String customerLoginId) {
        this.customerLoginId = customerLoginId;
    }

    public void setTaxiDriverLoginId(String taxiDriverLoginId) {
        this.taxiDriverLoginId = taxiDriverLoginId;
    }

    public void setOrigin(Location origin) {
        this.origin = origin;
    }

    public void setPickupTime(LocalDateTime pickupTime) {
        this.pickupTime = pickupTime;
    }

    public void setDestination(Location destination) {
        this.destination = destination;
    }

    public void setArrivingTime(LocalDateTime arrivingTime) {
        this.arrivingTime = arrivingTime;
    }

    public void setDrivingStatus(DrivingStatus drivingStatus) {
        this.drivingStatus = drivingStatus;
    }

    public void setReported(boolean reported) {
        this.reported = reported;
    }

    public Long getCallId() {
        return callId;
    }

    public String getCustomerLoginId() {
        return customerLoginId;
    }

    public String getTaxiDriverLoginId() {
        return taxiDriverLoginId;
    }

    public Location getOrigin() {
        return origin;
    }

    public LocalDateTime getPickupTime() {
        return pickupTime;
    }

    public Location getDestination() {
        return destination;
    }

    public LocalDateTime getArrivingTime() {
        return arrivingTime;
    }

    public DrivingStatus getDrivingStatus() {
        return drivingStatus;
    }

    public boolean isReported() {
        return reported;
    }
}