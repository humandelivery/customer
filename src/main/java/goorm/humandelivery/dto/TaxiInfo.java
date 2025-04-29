package goorm.humandelivery.dto;

public class TaxiInfo {
    Long taxiId;
    String phoneNumber;
    Location location;

    public TaxiInfo() {

    }

    public TaxiInfo(Long taxiId, String phoneNumber, Location location) {
        this.taxiId = taxiId;
        this.phoneNumber = phoneNumber;
        this.location = location;
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
    public Location getLocation() {
        return location;
    }
    public void setLocation(Location location) {
        this.location = location;
    }
}
