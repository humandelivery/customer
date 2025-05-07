package goorm.humandelivery.dto;

public class MatchingSuccessResponse {
    private TaxiDriverStatus taxiDriverStatus;
    private String taxiDriverLoginId;

    public TaxiDriverStatus getTaxiDriverStatus() {
        return taxiDriverStatus;
    }

    public void setTaxiDriverStatus(TaxiDriverStatus taxiDriverStatus) {
        this.taxiDriverStatus = taxiDriverStatus;
    }

    public String getTaxiDriverLoginId() {
        return taxiDriverLoginId;
    }

    public void setTaxiDriverLoginId(String taxiDriverLoginId) {
        this.taxiDriverLoginId = taxiDriverLoginId;
    }
}
