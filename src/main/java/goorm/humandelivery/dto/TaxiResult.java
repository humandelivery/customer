package goorm.humandelivery.dto;

import java.time.LocalDateTime;

public class TaxiResult {
    Long taxiId;
    Location departPosition;
    Location arrivingPosition;
    LocalDateTime departDate;
    LocalDateTime arrivingTime;

    public TaxiResult() {

    }

    public TaxiResult(Long taxiId, LocalDateTime departDate, Location departPosition,
                      LocalDateTime arrivingTime, Location arrivingPosition) {
        this.taxiId = taxiId;
        this.departDate = departDate;
        this.departPosition = departPosition;
        this.arrivingTime = arrivingTime;
        this.arrivingPosition = arrivingPosition;
    }


    public Long getTaxiId() {
        return taxiId;
    }
    public void setTaxiId(Long taxiId) {
        this.taxiId = taxiId;
    }
    public Location getDepartPosition() {
        return departPosition;
    }
    public void setDepartPosition(Location departPosition) {
        this.departPosition = departPosition;
    }
    public Location getArrivingPosition() {
        return arrivingPosition;
    }
    public void setArrivingPosition(Location arrivingPosition) {
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
