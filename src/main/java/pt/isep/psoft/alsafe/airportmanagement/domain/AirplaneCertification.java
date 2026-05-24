package pt.isep.psoft.alsafe.airportmanagement.domain;


import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Embeddable
@Getter
public class AirplaneCertification {

    private String modelName;
    private LocalDate certificationDate;

    public AirplaneCertification() {}

    public AirplaneCertification(String modelName, LocalDate certificationDate) {
        if(modelName == null || modelName.trim().isEmpty()) {
            throw new IllegalArgumentException("modelName cannot be null or empty.");
        }
        this.modelName = modelName;
        this.certificationDate = certificationDate != null ? certificationDate : LocalDate.now();
    }
}
