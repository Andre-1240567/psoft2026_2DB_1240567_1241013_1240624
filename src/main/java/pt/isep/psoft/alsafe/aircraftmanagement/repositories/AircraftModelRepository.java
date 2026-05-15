package pt.isep.psoft.alsafe.aircraftmanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftModel;

public interface AircraftModelRepository extends JpaRepository<AircraftModel, Long> {
    Iterable<AircraftModel> findByManufacturer(String manufacturer);
}