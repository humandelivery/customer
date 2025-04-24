package goorm.humandelivery.dto;

public record CallRequest(
        String departPosition,
        String arrivalPosition,
        String taxiType

) {
}
