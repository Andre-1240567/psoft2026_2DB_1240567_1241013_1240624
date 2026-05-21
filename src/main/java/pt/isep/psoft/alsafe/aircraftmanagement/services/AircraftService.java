package pt.isep.psoft.alsafe.aircraftmanagement.services;

import org.springframework.stereotype.Service;
import pt.isep.psoft.alsafe.aircraftmanagement.api.CreateAircraftDTO;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.Aircraft;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftModel;
import pt.isep.psoft.alsafe.aircraftmanagement.repositories.AircraftModelRepository;
import pt.isep.psoft.alsafe.aircraftmanagement.repositories.AircraftRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AircraftService {

    private final AircraftRepository aircraftRepository;
    private final AircraftModelRepository aircraftModelRepository;

    public AircraftService(AircraftRepository aircraftRepository, AircraftModelRepository aircraftModelRepository) {
        this.aircraftRepository = aircraftRepository;
        this.aircraftModelRepository = aircraftModelRepository;
    }

    @Transactional
    public Aircraft createAircraft(CreateAircraftDTO dto) {

        if (aircraftRepository.existsById(dto.getRegistrationNumber())) {
            throw new IllegalArgumentException("Aircraft with registration number " + dto.getRegistrationNumber() + " already exists.");
        }

        Optional<AircraftModel> modelOpt = aircraftModelRepository.findByModelName(dto.getModelName());
        if (modelOpt.isEmpty()) {
            throw new IllegalArgumentException("Aircraft Model '" + dto.getModelName() + "' not found in the system.");
        }

        Aircraft aircraft = new Aircraft(
                dto.getRegistrationNumber(),
                modelOpt.get(),
                dto.getManufacturingDate(),
                dto.getActiveConfigurationName()
        );

        return aircraftRepository.save(aircraft);
    }
}
