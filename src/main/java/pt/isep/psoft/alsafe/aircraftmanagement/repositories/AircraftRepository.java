package pt.isep.psoft.alsafe.aircraftmanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.Aircraft;

@Repository
public interface AircraftRepository extends JpaRepository<Aircraft, String> {
}