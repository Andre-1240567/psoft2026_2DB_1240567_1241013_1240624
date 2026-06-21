package pt.isep.psoft.alsafe.aircraftmanagement.api.dto;

import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftModel;

@Getter
public class AircraftModelResponseDTO extends RepresentationModel<AircraftModelResponseDTO> {

    private final Long id;
    private final String manufacturer;
    private final String modelName;
    private final Integer seatingCapacity;
    private final Double fuelCapacity;
    private final Double maxRange;
    private final Double cruisingSpeed;
    private final String image;
    private final Long version;

    public AircraftModelResponseDTO(AircraftModel model) {
        this.id = model.getId();
        this.manufacturer = model.getManufacturer().name();
        this.modelName = model.getModelName();
        this.seatingCapacity = model.getSeatingCapacity();
        this.fuelCapacity = model.getFuelCapacity();
        this.maxRange = model.getMaxRange();
        this.cruisingSpeed = model.getCruisingSpeed();
        this.image = model.getImage();
        this.version = model.getVersion();
    }
}
