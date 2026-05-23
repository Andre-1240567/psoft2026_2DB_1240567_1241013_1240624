package pt.isep.psoft.alsafe.aircraftmanagement.services;

import org.springframework.stereotype.Service;
import pt.isep.psoft.alsafe.aircraftmanagement.api.CreateAircraftModelDTO;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftModel;
import pt.isep.psoft.alsafe.aircraftmanagement.repositories.AircraftModelRepository;

@Service
public class AircraftModelService {

    private final AircraftModelRepository repository;

    public AircraftModelService(AircraftModelRepository repository) {
        this.repository = repository;
    }

    public AircraftModel createAircraftModel(CreateAircraftModelDTO dto) {
        AircraftModel newModel = new AircraftModel(
                dto.getManufacturer(),
                dto.getModelName(),
                dto.getSeatingCapacity(),
                dto.getFuelCapacity(),
                dto.getMaxRange(),
                dto.getCruisingSpeed()
        );

        return repository.save(newModel);
    }
}