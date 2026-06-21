package pt.isep.psoft.alsafe.maintenancemanagement.repositories;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.Aircraft;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftModel;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.Manufacturer;
import pt.isep.psoft.alsafe.maintenancemanagement.domain.MaintenanceComponent;
import pt.isep.psoft.alsafe.maintenancemanagement.domain.MaintenanceRecord;
import pt.isep.psoft.alsafe.maintenancemanagement.domain.MaintenanceTemplate;
import pt.isep.psoft.alsafe.maintenancemanagement.domain.TemplateType;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class MaintenanceRecordRepositoryTest {

    @Autowired
    private MaintenanceRecordRepository recordRepository;

    @Autowired
    private EntityManager entityManager;

    private AircraftModel a320;
    private AircraftModel b737;
    private Aircraft aircraftA;
    private Aircraft aircraftB;
    private MaintenanceTemplate templateForA320;
    private MaintenanceTemplate templateForB737;

    @BeforeEach
    void setUp() {
        a320 = new AircraftModel(Manufacturer.AIRBUS, "A320", 180, 24000.0, 6100.0, 828.0);
        b737 = new AircraftModel(Manufacturer.BOEING, "737-800", 189, 26000.0, 5400.0, 842.0);
        entityManager.persist(a320);
        entityManager.persist(b737);

        aircraftA = new Aircraft("CS-TTA", a320, LocalDate.of(2018, 5, 10), "Standard");
        aircraftB = new Aircraft("CS-TTB", b737, LocalDate.of(2019, 3, 1), "Standard");
        entityManager.persist(aircraftA);
        entityManager.persist(aircraftB);

        templateForA320 = new MaintenanceTemplate(
                "A-Check", TemplateType.INSPECTION, 8.0, List.of(a320), List.of("Check oil"));
        templateForB737 = new MaintenanceTemplate(
                "B-Check", TemplateType.SCHEDULED_MAINTENANCE, 16.0, List.of(b737), List.of("Engine check"));
        entityManager.persist(templateForA320);
        entityManager.persist(templateForB737);

        entityManager.flush();
    }

    @Nested
    @DisplayName("findByAircraft_RegistrationNumber() — US116")
    class FindByAircraftTests {

        @Test
        @DisplayName("returns only records for the given aircraft")
        void returnsOnlyMatchingRecords() {
            persistRecord(aircraftA, templateForA320, LocalDate.now(), MaintenanceComponent.AIRFRAME, null, null);
            persistRecord(aircraftB, templateForB737, LocalDate.now(), MaintenanceComponent.ENGINE, null, null);

            List<MaintenanceRecord> result = recordRepository.findByAircraft_RegistrationNumber("CS-TTA");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getAircraft().getRegistrationNumber()).isEqualTo("CS-TTA");
        }

        @Test
        @DisplayName("returns an empty list when the aircraft has no records")
        void returnsEmptyWhenNoRecords() {
            assertThat(recordRepository.findByAircraft_RegistrationNumber("CS-TTA")).isEmpty();
        }
    }

    @Nested
    @DisplayName("findTotalMaintenanceHoursPerAircraft() — US117")
    class TotalHoursTests {

        @Test
        @DisplayName("sums expectedDurationHours per aircraft, sorted descending")
        void sumsHoursPerAircraft() {
            persistRecordWithDuration(aircraftA, templateForA320, 8.0);
            persistRecordWithDuration(aircraftA, templateForA320, 4.0);
            persistRecordWithDuration(aircraftB, templateForB737, 20.0);

            List<Object[]> result = recordRepository.findTotalMaintenanceHoursPerAircraft();

            assertThat(result).hasSize(2);
            
            assertThat(result.get(0)[0]).isEqualTo("CS-TTB");
            assertThat(((Number) result.get(0)[1]).doubleValue()).isEqualTo(20.0);
            assertThat(result.get(1)[0]).isEqualTo("CS-TTA");
            assertThat(((Number) result.get(1)[1]).doubleValue()).isEqualTo(12.0);
        }
    }

    @Nested
    @DisplayName("search() — US218")
    class SearchTests {

        @Test
        @DisplayName("filters by registration number only")
        void filtersByRegistrationOnly() {
            persistRecord(aircraftA, templateForA320, LocalDate.now(), MaintenanceComponent.AIRFRAME, null, null);
            persistRecord(aircraftB, templateForB737, LocalDate.now(), MaintenanceComponent.ENGINE, null, null);

            List<MaintenanceRecord> result = recordRepository.search("CS-TTA", null, null, null);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getAircraft().getRegistrationNumber()).isEqualTo("CS-TTA");
        }

        @Test
        @DisplayName("filters by component only")
        void filtersByComponentOnly() {
            persistRecord(aircraftA, templateForA320, LocalDate.now(), MaintenanceComponent.AIRFRAME, null, null);
            persistRecord(aircraftA, templateForA320, LocalDate.now(), MaintenanceComponent.AVIONICS, null, null);

            List<MaintenanceRecord> result = recordRepository.search(null, MaintenanceComponent.AVIONICS, null, null);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getComponent()).isEqualTo(MaintenanceComponent.AVIONICS);
        }

        @Test
        @DisplayName("filters by date range only")
        void filtersByDateRangeOnly() {
            persistRecord(aircraftA, templateForA320, LocalDate.of(2026, 1, 10), MaintenanceComponent.AIRFRAME, null, null);
            persistRecord(aircraftA, templateForA320, LocalDate.of(2026, 6, 10), MaintenanceComponent.AIRFRAME, null, null);

            List<MaintenanceRecord> result = recordRepository.search(
                    null, null, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 1));

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStartDate()).isEqualTo(LocalDate.of(2026, 1, 10));
        }

        @Test
        @DisplayName("combines all filters together (AND semantics)")
        void combinesAllFilters() {
            persistRecord(aircraftA, templateForA320, LocalDate.of(2026, 1, 10), MaintenanceComponent.AIRFRAME, null, null);
            persistRecord(aircraftA, templateForA320, LocalDate.of(2026, 1, 15), MaintenanceComponent.AVIONICS, null, null);
            persistRecord(aircraftB, templateForB737, LocalDate.of(2026, 1, 10), MaintenanceComponent.AIRFRAME, null, null);

            List<MaintenanceRecord> result = recordRepository.search(
                    "CS-TTA", MaintenanceComponent.AIRFRAME,
                    LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getAircraft().getRegistrationNumber()).isEqualTo("CS-TTA");
            assertThat(result.get(0).getComponent()).isEqualTo(MaintenanceComponent.AIRFRAME);
        }

        @Test
        @DisplayName("returns all records when every filter is null")
        void returnsAllWhenNoFilters() {
            persistRecord(aircraftA, templateForA320, LocalDate.now(), MaintenanceComponent.AIRFRAME, null, null);
            persistRecord(aircraftB, templateForB737, LocalDate.now(), MaintenanceComponent.ENGINE, null, null);

            List<MaintenanceRecord> result = recordRepository.search(null, null, null, null);

            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("findAllOngoing() — US219")
    class FindAllOngoingTests {

        @Test
        @DisplayName("returns only PLANNED and IN_PROGRESS records, ordered by startDate ascending")
        void returnsOnlyOngoing() {
            MaintenanceRecord planned = persistRecord(
                    aircraftA, templateForA320, LocalDate.of(2026, 3, 1), MaintenanceComponent.AIRFRAME, null, null);

            MaintenanceRecord inProgress = persistRecord(
                    aircraftB, templateForB737, LocalDate.of(2026, 1, 1), MaintenanceComponent.ENGINE, null, null);
            inProgress.startWork();
            entityManager.persist(inProgress);

            MaintenanceRecord completed = persistRecord(
                    aircraftA, templateForA320, LocalDate.of(2025, 12, 1), MaintenanceComponent.AIRFRAME, null, null);
            completed.startWork();
            completed.markAsCompleted("Done", 5.0);
            entityManager.persist(completed);

            entityManager.flush();

            List<MaintenanceRecord> result = recordRepository.findAllOngoing();

            assertThat(result).hasSize(2);
            
            assertThat(result.get(0).getId()).isEqualTo(inProgress.getId());
            assertThat(result.get(1).getId()).isEqualTo(planned.getId());
        }
    }

    @Nested
    @DisplayName("findCostReportPerAircraft() / findCostReportPerAircraftModel() — US220")
    class CostReportTests {

        @Test
        @DisplayName("sums estimated and actual cost per aircraft")
        void sumsCostPerAircraft() {
            MaintenanceRecord r1 = persistRecord(
                    aircraftA, templateForA320, LocalDate.now(), MaintenanceComponent.AIRFRAME, 500.0, null);
            r1.startWork();
            r1.markAsCompleted("Done", 8.0, 480.0);
            entityManager.persist(r1);

            MaintenanceRecord r2 = persistRecord(
                    aircraftA, templateForA320, LocalDate.now(), MaintenanceComponent.ENGINE, 300.0, null);
            entityManager.flush();

            List<Object[]> result = recordRepository.findCostReportPerAircraft();

            assertThat(result).hasSize(1);
            assertThat(result.get(0)[0]).isEqualTo("CS-TTA");
            assertThat(((Number) result.get(0)[1]).doubleValue()).isEqualTo(800.0); 
            assertThat(((Number) result.get(0)[2]).doubleValue()).isEqualTo(480.0); 
        }

        @Test
        @DisplayName("sums cost per aircraft model, aggregating across aircraft of the same model")
        void sumsCostPerModel() {
            persistRecord(aircraftA, templateForA320, LocalDate.now(), MaintenanceComponent.AIRFRAME, 500.0, null);

            Aircraft aircraftA2 = new Aircraft("CS-TTC", a320, LocalDate.of(2020, 1, 1), "Standard");
            entityManager.persist(aircraftA2);
            persistRecord(aircraftA2, templateForA320, LocalDate.now(), MaintenanceComponent.AIRFRAME, 300.0, null);
            entityManager.flush();

            List<Object[]> result = recordRepository.findCostReportPerAircraftModel();

            assertThat(result).hasSize(1);
            assertThat(result.get(0)[0]).isEqualTo("A320");
            assertThat(((Number) result.get(0)[1]).doubleValue()).isEqualTo(800.0);
        }
    }

    @Nested
    @DisplayName("findAverageTurnaroundPerAircraftModel() — US221")
    class AverageTurnaroundTests {

        @Test
        @DisplayName("averages actualDurationHours only for COMPLETED records")
        void averagesOnlyCompletedRecords() {
            MaintenanceRecord completed1 = persistRecord(
                    aircraftA, templateForA320, LocalDate.now(), MaintenanceComponent.AIRFRAME, null, null);
            completed1.startWork();
            completed1.markAsCompleted("Done", 10.0);
            entityManager.persist(completed1);

            MaintenanceRecord completed2 = persistRecord(
                    aircraftA, templateForA320, LocalDate.now(), MaintenanceComponent.ENGINE, null, null);
            completed2.startWork();
            completed2.markAsCompleted("Done", 6.0);
            entityManager.persist(completed2);

            
            persistRecord(aircraftA, templateForA320, LocalDate.now(), MaintenanceComponent.AVIONICS, null, null);

            entityManager.flush();

            List<Object[]> result = recordRepository.findAverageTurnaroundPerAircraftModel();

            assertThat(result).hasSize(1);
            assertThat(result.get(0)[0]).isEqualTo("A320");
            assertThat(((Number) result.get(0)[1]).doubleValue()).isEqualTo(8.0); 
        }

        @Test
        @DisplayName("returns an empty list when no records are COMPLETED")
        void returnsEmptyWhenNoneCompleted() {
            persistRecord(aircraftA, templateForA320, LocalDate.now(), MaintenanceComponent.AIRFRAME, null, null);

            assertThat(recordRepository.findAverageTurnaroundPerAircraftModel()).isEmpty();
        }
    }

    @Nested
    @DisplayName("findDueForMaintenanceByDate() / findDueForMaintenanceByFlightHours() — US222")
    class AlertQueryTests {

        @Test
        @DisplayName("findDueForMaintenanceByDate() returns records due today or earlier")
        void returnsRecordsDueByDate() {
            MaintenanceRecord overdue = persistRecord(
                    aircraftA, templateForA320, LocalDate.now(), MaintenanceComponent.AIRFRAME, null, null);
            overdue.scheduleNextMaintenance(LocalDate.now().minusDays(1), null);
            entityManager.persist(overdue);

            MaintenanceRecord future = persistRecord(
                    aircraftB, templateForB737, LocalDate.now(), MaintenanceComponent.ENGINE, null, null);
            future.scheduleNextMaintenance(LocalDate.now().plusMonths(3), null);
            entityManager.persist(future);

            entityManager.flush();

            List<MaintenanceRecord> result = recordRepository.findDueForMaintenanceByDate(LocalDate.now());

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(overdue.getId());
        }

        @Test
        @DisplayName("findDueForMaintenanceByFlightHours() returns records where aircraft hours >= threshold")
        void returnsRecordsDueByFlightHours() {
            aircraftA.addFlightHours(1600.0);

            MaintenanceRecord due = persistRecord(
                    aircraftA, templateForA320, LocalDate.now(), MaintenanceComponent.AIRFRAME, null, null);
            due.scheduleNextMaintenance(null, 1500.0);
            entityManager.persist(due);

            MaintenanceRecord notDue = persistRecord(
                    aircraftB, templateForB737, LocalDate.now(), MaintenanceComponent.ENGINE, null, null);
            notDue.scheduleNextMaintenance(null, 5000.0);
            entityManager.persist(notDue);

            entityManager.persist(aircraftA);
            entityManager.flush();

            List<MaintenanceRecord> result = recordRepository.findDueForMaintenanceByFlightHours();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(due.getId());
        }
    }

    
    
    

    private MaintenanceRecord persistRecord(Aircraft aircraft,
                                             MaintenanceTemplate template,
                                             LocalDate startDate,
                                             MaintenanceComponent component,
                                             Double estimatedCost,
                                             Double expectedDuration) {
        MaintenanceRecord record = new MaintenanceRecord(
                aircraft, template, "Maintenance work", startDate,
                expectedDuration, component, estimatedCost);
        entityManager.persist(record);
        entityManager.flush();
        return record;
    }

    private void persistRecordWithDuration(Aircraft aircraft, MaintenanceTemplate template, double durationHours) {
        MaintenanceRecord record = new MaintenanceRecord(
                aircraft, template, "Maintenance work", LocalDate.now(),
                durationHours, MaintenanceComponent.AIRFRAME, null);
        entityManager.persist(record);
        entityManager.flush();
    }
}