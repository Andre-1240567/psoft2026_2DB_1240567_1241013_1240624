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

    @Column(nullable = false)
    private String manufacturer;

    @Column(nullable = false)
    private Double fuelCapacity;

    @Column(nullable = false)
    private Double maxRange;

    @Column(nullable = false)
    private Double cruisingSpeed;

    public AircraftModel(String manufacturer, String modelName, Double fuelCapacity, Double maxRange, Double cruisingSpeed) {
        if (fuelCapacity <= 0 || maxRange <= 0 || cruisingSpeed <= 0) {
            throw new IllegalArgumentException("Capacity, range, and speed must be strictly positive.");
        }
        this.manufacturer = manufacturer;
        this.modelName = modelName;
        this.fuelCapacity = fuelCapacity;
        this.maxRange = maxRange;
        this.cruisingSpeed = cruisingSpeed;
    }
}