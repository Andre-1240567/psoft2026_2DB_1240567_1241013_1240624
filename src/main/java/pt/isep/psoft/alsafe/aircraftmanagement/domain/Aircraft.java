package pt.isep.psoft.alsafe.aircraftmanagement.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
public class Aircraft {

    @Id
    private String registrationNumber;

    @ManyToOne(optional = false)
    @JoinColumn(name = "model_id", nullable = false)
    private AircraftModel model;

    @Column(nullable = false)
    private LocalDate manufacturingDate;

    @Column(nullable = false)
    private String activeConfigurationName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AircraftStatus status;

    @Version
    private Long version;

    public Aircraft(String registrationNumber, AircraftModel model, LocalDate manufacturingDate, String activeConfigurationName) {
        if (registrationNumber == null || registrationNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Registration number cannot be empty");
        }
        if (model == null) {
            throw new IllegalArgumentException("Aircraft must belong to a model");
        }
        if (manufacturingDate == null || manufacturingDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Manufacturing date cannot be in the future");
        }

        this.registrationNumber = registrationNumber.toUpperCase();
        this.model = model;
        this.manufacturingDate = manufacturingDate;
        this.activeConfigurationName = activeConfigurationName;
        this.status = AircraftStatus.AVAILABLE;
    }
}
