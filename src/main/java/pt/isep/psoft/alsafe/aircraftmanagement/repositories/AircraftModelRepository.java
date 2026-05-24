package pt.isep.psoft.alsafe.aircraftmanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftModel;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.Manufacturer;

import java.util.Optional;

@Repository
public interface AircraftModelRepository extends JpaRepository<AircraftModel, Long> {

    Iterable<AircraftModel> findByManufacturer(Manufacturer manufacturer);

    Optional<AircraftModel> findByModelName(String modelName);
}