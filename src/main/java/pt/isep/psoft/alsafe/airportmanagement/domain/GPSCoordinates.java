package pt.isep.psoft.alsafe.airportmanagement.domain;
import jakarta.persistence.Embeddable;
import lombok.Getter;

@Getter
@Embeddable
public class GPSCoordinates {
    private double latitude;
    private double longitude;

    protected GPSCoordinates() {}

    public GPSCoordinates(Double latitude, Double longitude) {
        if (latitude == null || latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("The latitude must be between -90 and 90.");
        }
        if (longitude == null || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("The longitude must be between -180 and 180.");
        }
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
