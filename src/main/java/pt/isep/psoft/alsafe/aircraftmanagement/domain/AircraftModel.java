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

    @Version
    private Long version;

    @Column(nullable = false, unique = true)
    private String modelName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Manufacturer manufacturer;

    @Column(nullable = false)
    private Integer seatingCapacity;

    @Column(nullable = false)
    private Double fuelCapacity;

    @Column(nullable = false)
    private Double maxRange;

    @Column(nullable = false)
    private Double cruisingSpeed;

    public AircraftModel(Manufacturer manufacturer, String modelName, Integer seatingCapacity, Double fuelCapacity, Double maxRange, Double cruisingSpeed) {
        if (manufacturer == null) {
            throw new IllegalArgumentException("Manufacturer cannot be null.");
        }
        if (modelName == null || modelName.trim().isEmpty()) {
            throw new IllegalArgumentException("Model name cannot be empty.");
        }

        if (seatingCapacity == null || seatingCapacity <= 0) {
            throw new IllegalArgumentException("Seating capacity must be strictly positive.");
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
        this.seatingCapacity = seatingCapacity;
        this.fuelCapacity = fuelCapacity;
        this.maxRange = maxRange;
        this.cruisingSpeed = cruisingSpeed;
    }

    public void updateSpecifications(Integer seatingCapacity, Double fuelCapacity, Double maxRange, Double cruisingSpeed) {
        if (seatingCapacity == null || seatingCapacity <= 0) {
            throw new IllegalArgumentException("Seating capacity must be strictly positive.");
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

        this.seatingCapacity = seatingCapacity;
        this.fuelCapacity = fuelCapacity;
        this.maxRange = maxRange;
        this.cruisingSpeed = cruisingSpeed;
    }
}