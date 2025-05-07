package goorm.humandelivery.dto;

public class LocationResponse {
    private Location location;

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "LocationResponse{" +
                "location=" + location +
                '}';
    }
}
