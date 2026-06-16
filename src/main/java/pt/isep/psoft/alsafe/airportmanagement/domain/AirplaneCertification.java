package pt.isep.psoft.alsafe.airportmanagement.domain;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDate;

@Embeddable
@Getter
@EqualsAndHashCode
public class AirplaneCertification {

    @Column(nullable = false)
    private String modelName;

    @Column(nullable = false)
    private LocalDate certificationDate;

    protected AirplaneCertification() {}

    public AirplaneCertification(String modelName) {
        this(modelName, LocalDate.now());
    }

    public AirplaneCertification(String modelName, LocalDate certificationDate) {
        if (modelName == null || modelName.trim().isEmpty()) {
            throw new IllegalArgumentException("modelName cannot be null or empty.");
        }
        this.modelName = modelName;
        this.certificationDate = certificationDate != null ? certificationDate : LocalDate.now();
    }
}
