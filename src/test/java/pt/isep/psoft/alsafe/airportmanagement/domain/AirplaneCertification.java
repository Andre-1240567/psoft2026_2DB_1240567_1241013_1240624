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
    private String aircraftModelName;

    @Column(nullable = false)
    private LocalDate certificationDate;

    protected AirplaneCertification() {
    }

    public AirplaneCertification(String aircraftModelName) {
        if (aircraftModelName == null || aircraftModelName.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do modelo de avião é obrigatório.");
        }
        this.aircraftModelName = aircraftModelName;
        this.certificationDate = LocalDate.now();
    }
}