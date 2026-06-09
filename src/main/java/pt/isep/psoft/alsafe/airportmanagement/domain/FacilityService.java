package pt.isep.psoft.alsafe.airportmanagement.domain;

import jakarta.persistence.Embeddable;
import lombok.Getter;

@Getter
@Embeddable
public class FacilityService {

    private String serviceType;
    private String description;

    protected FacilityService() {}

    public FacilityService(String serviceType, String description) {
        if (serviceType == null || serviceType.trim().isEmpty()) {
            throw new IllegalArgumentException("Service type cannot be empty.");
        }
        this.serviceType = serviceType;
        this.description = description;
    }
}
