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
    private final pt.isep.psoft.alsafe.aircraftmanagement.repositories.AircraftRepository aircraftRepository;

    public AircraftModelService(AircraftModelRepository repository, pt.isep.psoft.alsafe.aircraftmanagement.repositories.AircraftRepository aircraftRepository) {
        this.repository = repository;
        this.aircraftRepository = aircraftRepository;
    }

    public AircraftModel createAircraftModel(CreateAircraftModelDTO dto) {
        AircraftModel newModel = new AircraftModel(
                dto.getManufacturer(),
                dto.getModelName(),
                dto.getSeatingCapacity(),
                dto.getFuelCapacity(),
                dto.getMaxRange(),
                dto.getCruisingSpeed(),
                dto.getImage()
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

    public java.util.List<pt.isep.psoft.alsafe.aircraftmanagement.api.dto.TopAircraftModelDTO> getTop5MostUtilizedModels(String criteria) {
        org.springframework.data.domain.Pageable topFive = org.springframework.data.domain.PageRequest.of(0, 5);
        java.util.List<Object[]> results;
        
        if ("hours".equalsIgnoreCase(criteria)) {
            results = aircraftRepository.findTopMostUtilizedAircraftModelsByFlightHours(topFive);
        } else if ("assignments".equalsIgnoreCase(criteria)) {
            results = aircraftRepository.findTopMostUtilizedAircraftModelsByAssignments(topFive);
        } else {
            throw new IllegalArgumentException("Invalid criteria. Must be 'hours' or 'assignments'.");
        }

        return results.stream().map(result -> {
            AircraftModel model = (AircraftModel) result[0];
            Double value = result[1] != null ? ((Number) result[1]).doubleValue() : 0.0;
            return new pt.isep.psoft.alsafe.aircraftmanagement.api.dto.TopAircraftModelDTO(model, value);
        }).collect(java.util.stream.Collectors.toList());
    }
}