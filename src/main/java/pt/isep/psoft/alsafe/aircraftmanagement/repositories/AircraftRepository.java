package pt.isep.psoft.alsafe.aircraftmanagement.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.Aircraft;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftStatus;

import java.util.List;

@Repository
public interface AircraftRepository extends JpaRepository<Aircraft, String> {

    List<Aircraft> findByModel_ModelName(String modelName);
    List<Aircraft> findByStatus(AircraftStatus status);
    List<Aircraft> findByModel_ModelNameAndStatus(String modelName, AircraftStatus status);

    @Query("SELECT a.model, SUM(a.totalFlightHours) as tfh FROM Aircraft a GROUP BY a.model ORDER BY tfh DESC")
    List<Object[]> findTopMostUtilizedAircraftModelsByFlightHours(Pageable pageable);

    @Query("SELECT a.model, SUM(a.numberOfAssignments) as tna FROM Aircraft a GROUP BY a.model ORDER BY tna DESC")
    List<Object[]> findTopMostUtilizedAircraftModelsByAssignments(Pageable pageable);
}