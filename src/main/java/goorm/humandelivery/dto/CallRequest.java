package goorm.humandelivery.dto;

public class CallRequest {
    private String departPosition;
    private String arrivalPosition;
    private String taxiType;

    public CallRequest() {
    }

    public CallRequest(String departPosition, String arrivalPosition, String taxiType) {
        this.departPosition = departPosition;
        this.arrivalPosition = arrivalPosition;
        this.taxiType = taxiType;
    }

    public String getDepartPosition() {
        return departPosition;
    }

    public void setDepartPosition(String departPosition) {
        this.departPosition = departPosition;
    }

    public String getArrivalPosition() {
        return arrivalPosition;
    }

    public void setArrivalPosition(String arrivalPosition) {
        this.arrivalPosition = arrivalPosition;
    }

    public String getTaxiType() {
        return taxiType;
    }

    public void setTaxiType(String taxiType) {
        this.taxiType = taxiType;
    }
}
