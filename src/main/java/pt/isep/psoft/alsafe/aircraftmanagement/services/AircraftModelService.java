package pt.isep.psoft.alsafe.aircraftmanagement.services;

import org.springframework.stereotype.Service;
import pt.isep.psoft.alsafe.aircraftmanagement.api.CreateAircraftModelDTO;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftModel;
import pt.isep.psoft.alsafe.aircraftmanagement.repositories.AircraftModelRepository;
import pt.isep.psoft.alsafe.aircraftmanagement.api.dto.UpdateAircraftModelDTO;
import pt.isep.psoft.alsafe.shared.exceptions.ResourceNotFoundException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
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

    public AircraftModel updateAircraftModel(Long id, UpdateAircraftModelDTO dto) {
        AircraftModel model = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aircraft Model not found"));

        if (!model.getVersion().equals(dto.getVersion())) {
            throw new ObjectOptimisticLockingFailureException(AircraftModel.class, id);
        }

        model.updateSpecifications(
                dto.getSeatingCapacity(),
                dto.getFuelCapacity(),
                dto.getMaxRange(),
                dto.getCruisingSpeed()
        );

        return repository.save(model);
    }
}