package pt.isep.psoft.alsafe.maintenancemanagement.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.Aircraft;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftModel;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftStatus;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.Manufacturer;
import pt.isep.psoft.alsafe.aircraftmanagement.repositories.AircraftRepository;
import pt.isep.psoft.alsafe.maintenancemanagement.domain.MaintenanceComponent;
import pt.isep.psoft.alsafe.maintenancemanagement.domain.MaintenanceRecord;
import pt.isep.psoft.alsafe.maintenancemanagement.domain.MaintenanceTemplate;
import pt.isep.psoft.alsafe.maintenancemanagement.domain.TemplateType;
import pt.isep.psoft.alsafe.maintenancemanagement.repositories.MaintenanceRecordRepository;
import pt.isep.psoft.alsafe.maintenancemanagement.repositories.MaintenanceTemplateRepository;
import pt.isep.psoft.alsafe.shared.exceptions.ResourceNotFoundException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaintenanceRecordServiceTest {

    @Mock
    private MaintenanceRecordRepository recordRepository;

    @Mock
    private MaintenanceTemplateRepository templateRepository;

    @Mock
    private AircraftRepository aircraftRepository;

    private MaintenanceRecordService service;

    private AircraftModel a320;
    private Aircraft aircraft;
    private MaintenanceTemplate template;

    @BeforeEach
    void setUp() {
        service = new MaintenanceRecordService(recordRepository, templateRepository, aircraftRepository);

        a320 = new AircraftModel(Manufacturer.AIRBUS, "A320", 180, 24000.0, 6100.0, 828.0);
        aircraft = new Aircraft("CS-TTA", a320, LocalDate.of(2018, 5, 10), "Standard");
        template = new MaintenanceTemplate(
                "A-Check", TemplateType.INSPECTION, 8.0,
                List.of(a320), List.of("Check oil level"));
    }

    
    
    

    @Nested
    @DisplayName("createRecord()")
    class CreateRecordTests {

        @Test
        @DisplayName("creates the record and marks the aircraft as UNDER_MAINTENANCE")
        void createsRecordAndUpdatesAircraftStatus() {
            when(aircraftRepository.findById("CS-TTA")).thenReturn(Optional.of(aircraft));
            when(templateRepository.findById(1L)).thenReturn(Optional.of(template));
            when(recordRepository.save(any(MaintenanceRecord.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            MaintenanceRecord result = service.createRecord(
                    "cs-tta", 1L, "Routine check", LocalDate.now(), null,
                    MaintenanceComponent.AIRFRAME, 500.0);

            assertThat(result.getAircraft()).isEqualTo(aircraft);
            assertThat(aircraft.getStatus()).isEqualTo(AircraftStatus.UNDER_MAINTENANCE);
            verify(aircraftRepository).save(aircraft);
            verify(recordRepository).save(any(MaintenanceRecord.class));
        }

        @Test
        @DisplayName("looks up the aircraft case-insensitively (uppercased registration)")
        void looksUpAircraftCaseInsensitively() {
            when(aircraftRepository.findById("CS-TTA")).thenReturn(Optional.of(aircraft));
            when(templateRepository.findById(1L)).thenReturn(Optional.of(template));
            when(recordRepository.save(any(MaintenanceRecord.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            service.createRecord("cs-tta", 1L, "Routine check", LocalDate.now(), null,
                    MaintenanceComponent.AIRFRAME, null);

            verify(aircraftRepository).findById("CS-TTA");
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when the aircraft does not exist")
        void throwsWhenAircraftNotFound() {
            when(aircraftRepository.findById("ZZ-999")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.createRecord(
                    "ZZ-999", 1L, "Routine check", LocalDate.now(), null,
                    MaintenanceComponent.AIRFRAME, null))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("ZZ-999");

            verify(recordRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws IllegalStateException when the aircraft is already under maintenance")
        void throwsWhenAircraftAlreadyUnderMaintenance() {
            aircraft.updateStatus(AircraftStatus.UNDER_MAINTENANCE);
            when(aircraftRepository.findById("CS-TTA")).thenReturn(Optional.of(aircraft));

            assertThatThrownBy(() -> service.createRecord(
                    "CS-TTA", 1L, "Routine check", LocalDate.now(), null,
                    MaintenanceComponent.AIRFRAME, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already under maintenance");

            verify(templateRepository, never()).findById(any());
            verify(recordRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when the template does not exist")
        void throwsWhenTemplateNotFound() {
            when(aircraftRepository.findById("CS-TTA")).thenReturn(Optional.of(aircraft));
            when(templateRepository.findById(404L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.createRecord(
                    "CS-TTA", 404L, "Routine check", LocalDate.now(), null,
                    MaintenanceComponent.AIRFRAME, null))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("404");

            verify(recordRepository, never()).save(any());
        }

        @Test
        @DisplayName("propagates domain validation when the template is not applicable to the aircraft model")
        void propagatesTemplateNotApplicable() {
            AircraftModel b737 = new AircraftModel(Manufacturer.BOEING, "737-800", 189, 26000.0, 5400.0, 842.0);
            MaintenanceTemplate templateForB737 = new MaintenanceTemplate(
                    "B-Check", TemplateType.SCHEDULED_MAINTENANCE, 16.0,
                    List.of(b737), List.of("Engine check"));

            when(aircraftRepository.findById("CS-TTA")).thenReturn(Optional.of(aircraft));
            when(templateRepository.findById(2L)).thenReturn(Optional.of(templateForB737));

            assertThatThrownBy(() -> service.createRecord(
                    "CS-TTA", 2L, "Routine check", LocalDate.now(), null,
                    MaintenanceComponent.ENGINE, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("not applicable");

            verify(recordRepository, never()).save(any());
            
            assertThat(aircraft.getStatus()).isEqualTo(AircraftStatus.AVAILABLE);
        }
    }

    
    
    

    @Nested
    @DisplayName("getRecordsForAircraft()")
    class GetRecordsForAircraftTests {

        @Test
        @DisplayName("returns the records when the aircraft exists")
        void returnsRecords() {
            MaintenanceRecord record = buildPlannedRecord();
            when(aircraftRepository.existsById("CS-TTA")).thenReturn(true);
            when(recordRepository.findByAircraft_RegistrationNumber("CS-TTA"))
                    .thenReturn(List.of(record));

            List<MaintenanceRecord> result = service.getRecordsForAircraft("cs-tta");

            assertThat(result).containsExactly(record);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when the aircraft does not exist")
        void throwsWhenAircraftNotFound() {
            when(aircraftRepository.existsById("ZZ-999")).thenReturn(false);

            assertThatThrownBy(() -> service.getRecordsForAircraft("ZZ-999"))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(recordRepository, never()).findByAircraft_RegistrationNumber(any());
        }
    }

    
    
    

    @Nested
    @DisplayName("getTotalMaintenanceHoursPerAircraft()")
    class TotalHoursTests {

        @Test
        @DisplayName("maps repository rows into MaintenanceHoursSummary projections")
        void mapsRowsToProjections() {
            Object[] row1 = new Object[]{"CS-TTA", 42.5};
            Object[] row2 = new Object[]{"CS-TTB", null};
            when(recordRepository.findTotalMaintenanceHoursPerAircraft())
                    .thenReturn(List.<Object[]>of(row1, row2));

            List<MaintenanceRecordService.MaintenanceHoursSummary> result =
                    service.getTotalMaintenanceHoursPerAircraft();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).registrationNumber()).isEqualTo("CS-TTA");
            assertThat(result.get(0).totalHours()).isEqualTo(42.5);
            assertThat(result.get(1).totalHours()).isEqualTo(0.0); 
        }
    }

    
    
    

    @Nested
    @DisplayName("completeRecord()")
    class CompleteRecordTests {

        @Test
        @DisplayName("completes the record and restores aircraft to AVAILABLE")
        void completesRecordAndRestoresAircraft() {
            MaintenanceRecord record = buildInProgressRecord();
            aircraft.updateStatus(AircraftStatus.UNDER_MAINTENANCE);
            setVersion(record, 0L);

            when(recordRepository.findById(1L)).thenReturn(Optional.of(record));
            when(recordRepository.save(any(MaintenanceRecord.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            MaintenanceRecord result = service.completeRecord(
                    1L, 0L, "All good", 7.5, 480.0);

            assertThat(result.getStatus().name()).isEqualTo("COMPLETED");
            assertThat(aircraft.getStatus()).isEqualTo(AircraftStatus.AVAILABLE);
            verify(aircraftRepository).save(aircraft);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when the record does not exist")
        void throwsWhenRecordNotFound() {
            when(recordRepository.findById(404L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.completeRecord(404L, 0L, "notes", 5.0, null))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("throws ObjectOptimisticLockingFailureException on version mismatch")
        void throwsOnVersionMismatch() {
            MaintenanceRecord record = buildInProgressRecord();
            setVersion(record, 3L);
            when(recordRepository.findById(1L)).thenReturn(Optional.of(record));

            assertThatThrownBy(() -> service.completeRecord(1L, 0L, "notes", 5.0, null))
                    .isInstanceOf(ObjectOptimisticLockingFailureException.class);

            verify(recordRepository, never()).save(any());
            verify(aircraftRepository, never()).save(any());
        }

        @Test
        @DisplayName("propagates domain state-machine errors (e.g. not IN_PROGRESS)")
        void propagatesStateMachineError() {
            MaintenanceRecord record = buildPlannedRecord(); 
            setVersion(record, 0L);
            when(recordRepository.findById(1L)).thenReturn(Optional.of(record));

            assertThatThrownBy(() -> service.completeRecord(1L, 0L, "notes", 5.0, null))
                    .isInstanceOf(IllegalStateException.class);

            verify(recordRepository, never()).save(any());
        }
    }

    
    
    

    @Nested
    @DisplayName("startRecord()")
    class StartRecordTests {

        @Test
        @DisplayName("transitions the record to IN_PROGRESS")
        void startsRecord() {
            MaintenanceRecord record = buildPlannedRecord();
            setVersion(record, 0L);
            when(recordRepository.findById(1L)).thenReturn(Optional.of(record));
            when(recordRepository.save(any(MaintenanceRecord.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            MaintenanceRecord result = service.startRecord(1L, 0L);

            assertThat(result.getStatus().name()).isEqualTo("IN_PROGRESS");
        }

        @Test
        @DisplayName("throws ObjectOptimisticLockingFailureException on version mismatch")
        void throwsOnVersionMismatch() {
            MaintenanceRecord record = buildPlannedRecord();
            setVersion(record, 2L);
            when(recordRepository.findById(1L)).thenReturn(Optional.of(record));

            assertThatThrownBy(() -> service.startRecord(1L, 0L))
                    .isInstanceOf(ObjectOptimisticLockingFailureException.class);
        }
    }

    
    
    

    @Nested
    @DisplayName("cancelRecord()")
    class CancelRecordTests {

        @Test
        @DisplayName("cancels the record and restores aircraft to AVAILABLE")
        void cancelsRecordAndRestoresAircraft() {
            MaintenanceRecord record = buildPlannedRecord();
            aircraft.updateStatus(AircraftStatus.UNDER_MAINTENANCE);
            setVersion(record, 0L);
            when(recordRepository.findById(1L)).thenReturn(Optional.of(record));
            when(recordRepository.save(any(MaintenanceRecord.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            MaintenanceRecord result = service.cancelRecord(1L, 0L, "Aircraft reassigned");

            assertThat(result.getStatus().name()).isEqualTo("CANCELED");
            assertThat(aircraft.getStatus()).isEqualTo(AircraftStatus.AVAILABLE);
            verify(aircraftRepository).save(aircraft);
        }

        @Test
        @DisplayName("throws ObjectOptimisticLockingFailureException on version mismatch")
        void throwsOnVersionMismatch() {
            MaintenanceRecord record = buildPlannedRecord();
            setVersion(record, 9L);
            when(recordRepository.findById(1L)).thenReturn(Optional.of(record));

            assertThatThrownBy(() -> service.cancelRecord(1L, 0L, "Reason"))
                    .isInstanceOf(ObjectOptimisticLockingFailureException.class);

            verify(aircraftRepository, never()).save(any());
        }
    }

    
    
    

    @Nested
    @DisplayName("searchRecords()")
    class SearchRecordsTests {

        @Test
        @DisplayName("delegates to the repository with uppercased registration and parsed component")
        void delegatesWithParsedFilters() {
            MaintenanceRecord record = buildPlannedRecord();
            LocalDate from = LocalDate.of(2026, 1, 1);
            LocalDate to = LocalDate.of(2026, 12, 31);
            when(recordRepository.search("CS-TTA", MaintenanceComponent.ENGINE, from, to))
                    .thenReturn(List.of(record));

            List<MaintenanceRecord> result = service.searchRecords("cs-tta", "engine", from, to);

            assertThat(result).containsExactly(record);
        }

        @Test
        @DisplayName("treats null/blank filters as 'ignore'")
        void treatsBlankFiltersAsIgnore() {
            when(recordRepository.search(null, null, null, null)).thenReturn(List.of());

            service.searchRecords(null, "  ", null, null);

            verify(recordRepository).search(null, null, null, null);
        }

        @Test
        @DisplayName("throws IllegalArgumentException for an invalid component name")
        void throwsForInvalidComponent() {
            assertThatThrownBy(() -> service.searchRecords(null, "WINGS", null, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid maintenance component");
        }

        @Test
        @DisplayName("throws IllegalArgumentException when 'from' is after 'to'")
        void throwsWhenFromAfterTo() {
            LocalDate from = LocalDate.of(2026, 6, 1);
            LocalDate to = LocalDate.of(2026, 1, 1);

            assertThatThrownBy(() -> service.searchRecords(null, null, from, to))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot be after");
        }

        @Test
        @DisplayName("allows searching with only 'from' provided (no upper bound)")
        void allowsOnlyFromProvided() {
            LocalDate from = LocalDate.of(2026, 1, 1);
            when(recordRepository.search(null, null, from, null)).thenReturn(List.of());

            List<MaintenanceRecord> result = service.searchRecords(null, null, from, null);

            assertThat(result).isEmpty();
            verify(recordRepository).search(null, null, from, null);
        }

        @Test
        @DisplayName("allows searching with only 'to' provided (no lower bound)")
        void allowsOnlyToProvided() {
            LocalDate to = LocalDate.of(2026, 12, 31);
            when(recordRepository.search(null, null, null, to)).thenReturn(List.of());

            List<MaintenanceRecord> result = service.searchRecords(null, null, null, to);

            assertThat(result).isEmpty();
            verify(recordRepository).search(null, null, null, to);
        }
    }

    
    
    

    @Test
    @DisplayName("getOngoingActivities() delegates to repository")
    void getOngoingActivitiesDelegates() {
        MaintenanceRecord record = buildPlannedRecord();
        when(recordRepository.findAllOngoing()).thenReturn(List.of(record));

        List<MaintenanceRecord> result = service.getOngoingActivities();

        assertThat(result).containsExactly(record);
    }

    
    
    

    @Nested
    @DisplayName("Cost report methods")
    class CostReportTests {

        @Test
        @DisplayName("getCostReportPerAircraft() maps rows, handling null aggregates")
        void mapsCostReportPerAircraft() {
            Object[] row = new Object[]{"CS-TTA", 1000.0, null};
            when(recordRepository.findCostReportPerAircraft()).thenReturn(List.<Object[]>of(row));

            List<MaintenanceRecordService.MaintenanceCostSummary> result =
                    service.getCostReportPerAircraft();

            assertThat(result.get(0).key()).isEqualTo("CS-TTA");
            assertThat(result.get(0).estimatedCost()).isEqualTo(1000.0);
            assertThat(result.get(0).actualCost()).isNull();
        }

        @Test
        @DisplayName("getCostReportPerAircraft() maps rows with both costs present")
        void mapsCostReportPerAircraftBothPresent() {
            Object[] row = new Object[]{"CS-TTB", 2000.0, 2150.0};
            when(recordRepository.findCostReportPerAircraft()).thenReturn(List.<Object[]>of(row));

            List<MaintenanceRecordService.MaintenanceCostSummary> result =
                    service.getCostReportPerAircraft();

            assertThat(result.get(0).estimatedCost()).isEqualTo(2000.0);
            assertThat(result.get(0).actualCost()).isEqualTo(2150.0);
        }

        @Test
        @DisplayName("getCostReportPerAircraft() maps rows with both costs null")
        void mapsCostReportPerAircraftBothNull() {
            Object[] row = new Object[]{"CS-TTC", null, null};
            when(recordRepository.findCostReportPerAircraft()).thenReturn(List.<Object[]>of(row));

            List<MaintenanceRecordService.MaintenanceCostSummary> result =
                    service.getCostReportPerAircraft();

            assertThat(result.get(0).estimatedCost()).isNull();
            assertThat(result.get(0).actualCost()).isNull();
        }

        @Test
        @DisplayName("getCostReportPerAircraftModel() maps rows, handling null aggregates")
        void mapsCostReportPerAircraftModel() {
            Object[] row = new Object[]{"A320", null, 950.0};
            when(recordRepository.findCostReportPerAircraftModel()).thenReturn(List.<Object[]>of(row));

            List<MaintenanceRecordService.MaintenanceCostSummary> result =
                    service.getCostReportPerAircraftModel();

            assertThat(result.get(0).key()).isEqualTo("A320");
            assertThat(result.get(0).estimatedCost()).isNull();
            assertThat(result.get(0).actualCost()).isEqualTo(950.0);
        }

        @Test
        @DisplayName("getCostReportPerAircraftModel() maps rows with both costs present")
        void mapsCostReportPerAircraftModelBothPresent() {
            Object[] row = new Object[]{"737-800", 3000.0, 3200.0};
            when(recordRepository.findCostReportPerAircraftModel()).thenReturn(List.<Object[]>of(row));

            List<MaintenanceRecordService.MaintenanceCostSummary> result =
                    service.getCostReportPerAircraftModel();

            assertThat(result.get(0).estimatedCost()).isEqualTo(3000.0);
            assertThat(result.get(0).actualCost()).isEqualTo(3200.0);
        }

        @Test
        @DisplayName("getCostReportPerAircraftModel() maps rows with both costs null")
        void mapsCostReportPerAircraftModelBothNull() {
            Object[] row = new Object[]{"A350", null, null};
            when(recordRepository.findCostReportPerAircraftModel()).thenReturn(List.<Object[]>of(row));

            List<MaintenanceRecordService.MaintenanceCostSummary> result =
                    service.getCostReportPerAircraftModel();

            assertThat(result.get(0).estimatedCost()).isNull();
            assertThat(result.get(0).actualCost()).isNull();
        }
    }

    
    
    

    @Test
    @DisplayName("getAverageTurnaroundPerAircraftModel() maps rows, defaulting null to 0.0")
    void mapsAverageTurnaround() {
        Object[] row = new Object[]{"A320", 9.3};
        when(recordRepository.findAverageTurnaroundPerAircraftModel()).thenReturn(List.<Object[]>of(row));

        List<MaintenanceRecordService.MaintenanceTurnaroundSummary> result =
                service.getAverageTurnaroundPerAircraftModel();

        assertThat(result.get(0).modelName()).isEqualTo("A320");
        assertThat(result.get(0).avgDurationHours()).isEqualTo(9.3);
    }

    @Test
    @DisplayName("getAverageTurnaroundPerAircraftModel() defaults a null average to 0.0")
    void mapsAverageTurnaroundWithNullValue() {
        Object[] row = new Object[]{"ATR 72-600", null};
        when(recordRepository.findAverageTurnaroundPerAircraftModel()).thenReturn(List.<Object[]>of(row));

        List<MaintenanceRecordService.MaintenanceTurnaroundSummary> result =
                service.getAverageTurnaroundPerAircraftModel();

        assertThat(result.get(0).modelName()).isEqualTo("ATR 72-600");
        assertThat(result.get(0).avgDurationHours()).isEqualTo(0.0);
    }

    
    
    

    @Nested
    @DisplayName("Alert queries")
    class AlertTests {

        @Test
        @DisplayName("getAlertsDueByDate() delegates with today's date")
        void getAlertsDueByDate() {
            MaintenanceRecord record = buildPlannedRecord();
            ArgumentCaptor<LocalDate> dateCaptor = ArgumentCaptor.forClass(LocalDate.class);
            when(recordRepository.findDueForMaintenanceByDate(dateCaptor.capture()))
                    .thenReturn(List.of(record));

            List<MaintenanceRecord> result = service.getAlertsDueByDate();

            assertThat(result).containsExactly(record);
            assertThat(dateCaptor.getValue()).isEqualTo(LocalDate.now());
        }

        @Test
        @DisplayName("getAlertsDueByFlightHours() delegates to repository")
        void getAlertsDueByFlightHours() {
            MaintenanceRecord record = buildPlannedRecord();
            when(recordRepository.findDueForMaintenanceByFlightHours()).thenReturn(List.of(record));

            List<MaintenanceRecord> result = service.getAlertsDueByFlightHours();

            assertThat(result).containsExactly(record);
        }
    }

    
    
    

    @Nested
    @DisplayName("getRecordById()")
    class GetRecordByIdTests {

        @Test
        @DisplayName("returns the record when found")
        void returnsRecordWhenFound() {
            MaintenanceRecord record = buildPlannedRecord();
            when(recordRepository.findById(1L)).thenReturn(Optional.of(record));

            assertThat(service.getRecordById(1L)).isEqualTo(record);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when not found")
        void throwsWhenNotFound() {
            when(recordRepository.findById(404L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getRecordById(404L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    private MaintenanceRecord buildPlannedRecord() {
        return new MaintenanceRecord(
                aircraft, template, "Routine check", LocalDate.now(), null,
                MaintenanceComponent.AIRFRAME, null);
    }

    private MaintenanceRecord buildInProgressRecord() {
        MaintenanceRecord record = buildPlannedRecord();
        record.startWork();
        return record;
    }

    private void setVersion(MaintenanceRecord record, Long version) {
        try {
            var field = MaintenanceRecord.class.getDeclaredField("version");
            field.setAccessible(true);
            field.set(record, version);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}