package goorm.humandelivery.dto;

public class TaxiInfo {
    int taxiId;
    String phoneNumber;
    String Location;

    public TaxiInfo() {

    }

    public TaxiInfo(int taxiId, String phoneNumber, String Location) {
        this.taxiId = taxiId;
        this.phoneNumber = phoneNumber;
        this.Location = Location;
    }
    public int getTaxiId() {
        return taxiId;
    }
    public void setTaxiId(int taxiId) {
        this.taxiId = taxiId;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public String getLocation() {
        return Location;
    }
}
