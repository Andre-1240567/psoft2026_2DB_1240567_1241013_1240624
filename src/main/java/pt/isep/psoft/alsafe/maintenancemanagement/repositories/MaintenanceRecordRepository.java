package pt.isep.psoft.alsafe.maintenancemanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.isep.psoft.alsafe.maintenancemanagement.domain.MaintenanceComponent;
import pt.isep.psoft.alsafe.maintenancemanagement.domain.MaintenanceRecord;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecord, Long> {


    List<MaintenanceRecord> findByAircraft_RegistrationNumber(String registrationNumber);

    @Query("""
            SELECT r.aircraft.registrationNumber,
                   SUM(r.expectedDurationHours)
            FROM MaintenanceRecord r
            GROUP BY r.aircraft.registrationNumber
            ORDER BY SUM(r.expectedDurationHours) DESC
            """)
    List<Object[]> findTotalMaintenanceHoursPerAircraft();

    @Query("""
            SELECT r FROM MaintenanceRecord r
            WHERE (:registrationNumber IS NULL OR r.aircraft.registrationNumber = :registrationNumber)
              AND (:component        IS NULL OR r.component = :component)
              AND (:from             IS NULL OR r.startDate >= :from)
              AND (:to               IS NULL OR r.startDate <= :to)
            """)
    List<MaintenanceRecord> search(
            @Param("registrationNumber") String registrationNumber,
            @Param("component")          MaintenanceComponent component,
            @Param("from")               LocalDate from,
            @Param("to")                 LocalDate to
    );

    @Query("""
            SELECT r FROM MaintenanceRecord r
            WHERE r.status = pt.isep.psoft.alsafe.maintenancemanagement.domain.MaintenanceStatus.PLANNED
               OR r.status = pt.isep.psoft.alsafe.maintenancemanagement.domain.MaintenanceStatus.IN_PROGRESS
            ORDER BY r.startDate ASC
            """)
    List<MaintenanceRecord> findAllOngoing();

    @Query("""
            SELECT r.aircraft.registrationNumber,
                   SUM(r.estimatedCost),
                   SUM(r.actualCost)
            FROM MaintenanceRecord r
            GROUP BY r.aircraft.registrationNumber
            ORDER BY SUM(r.actualCost) DESC NULLS LAST
            """)
    List<Object[]> findCostReportPerAircraft();

    @Query("""
            SELECT r.aircraft.model.modelName,
                   SUM(r.estimatedCost),
                   SUM(r.actualCost)
            FROM MaintenanceRecord r
            GROUP BY r.aircraft.model.modelName
            ORDER BY SUM(r.actualCost) DESC NULLS LAST
            """)
    List<Object[]> findCostReportPerAircraftModel();

    @Query("""
            SELECT r.aircraft.model.modelName,
                   AVG(r.actualDurationHours)
            FROM MaintenanceRecord r
            WHERE r.status = pt.isep.psoft.alsafe.maintenancemanagement.domain.MaintenanceStatus.COMPLETED
              AND r.actualDurationHours IS NOT NULL
            GROUP BY r.aircraft.model.modelName
            ORDER BY AVG(r.actualDurationHours) DESC
            """)
    List<Object[]> findAverageTurnaroundPerAircraftModel();

    @Query("""
            SELECT r FROM MaintenanceRecord r
            WHERE r.nextMaintenanceDueDate IS NOT NULL
              AND r.nextMaintenanceDueDate <= :today
            ORDER BY r.nextMaintenanceDueDate ASC
            """)
    List<MaintenanceRecord> findDueForMaintenanceByDate(@Param("today") LocalDate today);

    @Query("""
            SELECT r FROM MaintenanceRecord r
            WHERE r.nextMaintenanceDueHours IS NOT NULL
              AND r.aircraft.totalFlightHours >= r.nextMaintenanceDueHours
            ORDER BY r.aircraft.totalFlightHours DESC
            """)
    List<MaintenanceRecord> findDueForMaintenanceByFlightHours();
}