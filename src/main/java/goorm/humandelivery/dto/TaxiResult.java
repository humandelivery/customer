package goorm.humandelivery.dto;

import java.time.LocalDateTime;

public class TaxiResult {
    int taxiId;
    String departPosition;
    String arrivingPosition;
    LocalDateTime departDate;
    LocalDateTime arrivingTime;

    public TaxiResult() {

    }

    public TaxiResult(int taxiId, LocalDateTime departDate, String departPosition,
                      LocalDateTime arrivingTime, String arrivingPosition) {
        this.taxiId = taxiId;
        this.departDate = departDate;
        this.departPosition = departPosition;
        this.arrivingTime = arrivingTime;
        this.arrivingPosition = arrivingPosition;
    }


    public int getTaxiId() {
        return taxiId;
    }
    public void setTaxiId(int taxiId) {
        this.taxiId = taxiId;
    }
    public String getDepartPosition() {
        return departPosition;
    }
    public void setDepartPosition(String departPosition) {
        this.departPosition = departPosition;
    }
    public String getArrivingPosition() {
        return arrivingPosition;
    }
    public void setArrivingPosition(String arrivingPosition) {
        this.arrivingPosition = arrivingPosition;
    }
    public LocalDateTime getDepartDate() {
        return departDate;
    }
    public void setDepartDate(LocalDateTime departDate) {
        this.departDate = departDate;
    }

    public LocalDateTime getArrivingTime() {
        return arrivingTime;
    }
    public void setArrivingTime(LocalDateTime arrivingTime) {
        this.arrivingTime = arrivingTime;
    }
}
