package goorm.humandelivery.dto;

public class CallRequest {
    private Location expectedOrigin;
    private Location expectedDestination;
    private String taxiType;
    private Integer retryCount;

    public CallRequest() {}

    public CallRequest(Location expectedOrigin, Location expectedDestination, String taxiType, Integer retryCount) {
        this.expectedOrigin = expectedOrigin;
        this.expectedDestination = expectedDestination;
        this.taxiType = taxiType;
        this.retryCount = retryCount;
    }

    public Location getExpectedOrigin() {
        return expectedOrigin;
    }

    public void setExpectedOrigin(Location expectedOrigin) {
        this.expectedOrigin = expectedOrigin;
    }

    public Location getExpectedDestination() {
        return expectedDestination;
    }

    public void setExpectedDestination(Location expectedDestination) {
        this.expectedDestination = expectedDestination;
    }

    public String getTaxiType() {
        return taxiType;
    }

    public void setTaxiType(String taxiType) {
        this.taxiType = taxiType;
    }

    public Integer getRetryCount() {
        return retryCount;
    }
    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

}
