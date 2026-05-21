package pt.isep.psoft.alsafe.aircraftmanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.Aircraft;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftStatus;

import java.util.List;

@Repository
public interface AircraftRepository extends JpaRepository<Aircraft, String> {

    List<Aircraft> findByModel_ModelName(String modelName);
    List<Aircraft> findByStatus(AircraftStatus status);
    List<Aircraft> findByModel_ModelNameAndStatus(String modelName, AircraftStatus status);
}