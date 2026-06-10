package pt.isep.psoft.alsafe.airportmanagement.domain;

import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.time.LocalTime;

@Getter
@Embeddable
public class OperationalHours {
    private LocalTime openingTime;
    private LocalTime closingTime;

    protected OperationalHours() {}

    public OperationalHours(LocalTime openingTime, LocalTime closingTime) {
        if (openingTime == null || closingTime == null) {
            throw new IllegalArgumentException("Opening and closing times are mandatory.");
        }
        if (openingTime.isAfter(closingTime)) {
            throw new IllegalArgumentException("Opening time must be before closing time.");
        }
        this.openingTime = openingTime;
        this.closingTime = closingTime;
    }
}
