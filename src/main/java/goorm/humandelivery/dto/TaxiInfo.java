package goorm.humandelivery.dto;

public class TaxiInfo {
    Long taxiId;
    String phoneNumber;
    String location;

    public TaxiInfo() {

    }

    public TaxiInfo(Long taxiId, String phoneNumber, String Location) {
        this.taxiId = taxiId;
        this.phoneNumber = phoneNumber;
        this.location = Location;
    }
    public Long getTaxiId() {
        return taxiId;
    }
    public void setTaxiId(Long taxiId) {
        this.taxiId = taxiId;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }
}
