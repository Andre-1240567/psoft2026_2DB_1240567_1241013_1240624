package pt.isep.psoft.alsafe.flightroutes.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pt.isep.psoft.alsafe.aircraftmanagement.domain.Aircraft;
import pt.isep.psoft.alsafe.flightroutes.domain.ScheduledFlight;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface ScheduledFlightRepository extends JpaRepository<ScheduledFlight, String> {
    List<ScheduledFlight> findByAircraft_RegistrationNumber(String registrationNumber);

    @Query("SELECT CASE WHEN COUNT(sf) > 0 THEN true ELSE false END FROM ScheduledFlight sf " +
           "WHERE sf.aircraft = :aircraft " +
           "AND sf.scheduledDeparture < :newArrival " +
           "AND sf.scheduledArrival > :newDeparture")
    boolean existsByAircraftAndTimeRange(
            @Param("aircraft") Aircraft aircraft,
            @Param("newDeparture") LocalDateTime newDeparture,
            @Param("newArrival") LocalDateTime newArrival
    );
}