package pt.isep.psoft.alsafe.flightroutes.repositories;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pt.isep.psoft.alsafe.aircraftmanagement.domain.Aircraft;
import pt.isep.psoft.alsafe.flightroutes.domain.ScheduledFlight;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduledFlightRepository extends JpaRepository<ScheduledFlight, String> {

    List<ScheduledFlight> findByAircraft_RegistrationNumber(String registrationNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT sf FROM ScheduledFlight sf WHERE sf.aircraft = :aircraft " +
           "AND sf.status = 'SCHEDULED' " +
           "AND (sf.scheduledDeparture <= :bufferArrival AND sf.scheduledArrival >= :bufferDeparture)")
    List<ScheduledFlight> findOverlappingFlightsWithLock(
            @Param("aircraft") Aircraft aircraft, 
            @Param("bufferDeparture") LocalDateTime bufferDeparture, 
            @Param("bufferArrival") LocalDateTime bufferArrival);

    @Query("SELECT sf FROM ScheduledFlight sf " +
           "WHERE sf.route.origin.iataCode.code = :originIata " +
           "AND sf.status != 'CANCELED' " + 
           "AND sf.scheduledDeparture >= :now " +
           "AND sf.scheduledDeparture <= :endWindow " +
           "ORDER BY sf.scheduledDeparture ASC")
    List<ScheduledFlight> findUpcomingDepartures(
            @Param("originIata") String originIata,
            @Param("now") LocalDateTime now,
            @Param("endWindow") LocalDateTime endWindow
    );
}