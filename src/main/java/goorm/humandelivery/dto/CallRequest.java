package goorm.humandelivery.dto;

public class CallRequest {
    private String expectedOrigin;
    private String expectedDestination;
    private String taxiType;

    public CallRequest() {
    }

    public CallRequest(String expectedOrigin, String expectedDestination, String taxiType) {
        this.expectedOrigin = expectedOrigin;
        this.expectedDestination = expectedDestination;
        this.taxiType = taxiType;
    }

    public String getExpectedOrigin() {
        return expectedOrigin;
    }

    public void setExpectedOrigin(String expectedOrigin) {
        this.expectedOrigin = expectedOrigin;
    }

    public String getExpectedDestination() {
        return expectedDestination;
    }

    public void setExpectedDestination(String expectedDestination) {
        this.expectedDestination = expectedDestination;
    }

    public String getTaxiType() {
        return taxiType;
    }

    public void setTaxiType(String taxiType) {
        this.taxiType = taxiType;
    }
}
