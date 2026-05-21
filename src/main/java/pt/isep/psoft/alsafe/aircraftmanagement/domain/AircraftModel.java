package pt.isep.psoft.alsafe.aircraftmanagement.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class AircraftModel {

    @Id // Primary Key
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String modelName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Manufacturer manufacturer;

    @Column(nullable = false)
    private Double fuelCapacity;

    @Column(nullable = false)
    private Double maxRange;

    @Column(nullable = false)
    private Double cruisingSpeed;

    public AircraftModel(Manufacturer manufacturer, String modelName, Double fuelCapacity, Double maxRange, Double cruisingSpeed) {
        if (manufacturer == null) {
            throw new IllegalArgumentException("Manufacturer cannot be null.");
        }
        if (modelName == null || modelName.trim().isEmpty()) {
            throw new IllegalArgumentException("Model name cannot be empty.");
        }
        if (fuelCapacity == null || fuelCapacity <= 0) {
            throw new IllegalArgumentException("Fuel capacity must be strictly positive.");
        }
        if (maxRange == null || maxRange <= 0) {
            throw new IllegalArgumentException("Max range must be strictly positive.");
        }
        if (cruisingSpeed == null || cruisingSpeed <= 0) {
            throw new IllegalArgumentException("Cruising speed must be strictly positive.");
        }

        this.manufacturer = manufacturer;
        this.modelName = modelName;
        this.fuelCapacity = fuelCapacity;
        this.maxRange = maxRange;
        this.cruisingSpeed = cruisingSpeed;
    }
}