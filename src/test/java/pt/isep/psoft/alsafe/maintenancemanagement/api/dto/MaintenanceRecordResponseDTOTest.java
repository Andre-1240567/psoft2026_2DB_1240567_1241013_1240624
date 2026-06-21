package pt.isep.psoft.alsafe.maintenancemanagement.api.dto;

import org.junit.jupiter.api.Test;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.Aircraft;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftModel;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.Manufacturer;
import pt.isep.psoft.alsafe.maintenancemanagement.domain.MaintenanceComponent;
import pt.isep.psoft.alsafe.maintenancemanagement.domain.MaintenanceRecord;
import pt.isep.psoft.alsafe.maintenancemanagement.domain.MaintenanceTemplate;
import pt.isep.psoft.alsafe.maintenancemanagement.domain.TemplateType;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MaintenanceRecordResponseDTOTest {

    private AircraftModel createModel() {
        return new AircraftModel(Manufacturer.AIRBUS, "A320neo", 160, 24000.0, 6300.0, 828.0);
    }

    private Aircraft createAircraft(AircraftModel model) {
        return new Aircraft("CS-TPA", model, LocalDate.of(2024, 1, 15), "Economy");
    }

    private MaintenanceTemplate createTemplate(AircraftModel model) {
        return new MaintenanceTemplate(
                "A-Check Routine Inspection",
                TemplateType.INSPECTION,
                8.0,
                List.of(model),
                List.of("Visual inspection", "Check tyre pressure")
        );
    }

    @Test
    void ensureDtoMapsAllFieldsFromPlannedRecord() {
        AircraftModel model = createModel();
        Aircraft aircraft = createAircraft(model);
        MaintenanceTemplate template = createTemplate(model);

        MaintenanceRecord record = new MaintenanceRecord(
                aircraft, template, "Routine check", LocalDate.of(2026, 7, 1),
                null, MaintenanceComponent.AIRFRAME, 1200.0);

        MaintenanceRecordResponseDTO dto = new MaintenanceRecordResponseDTO(record);

        assertEquals(record.getId(), dto.getId());
        assertEquals("CS-TPA", dto.getRegistrationNumber());
        assertEquals("A320neo", dto.getModelName());
        assertEquals(template.getId(), dto.getTemplateId());
        assertEquals("A-Check Routine Inspection", dto.getTemplateName());
        assertEquals("Routine check", dto.getDescription());
        assertEquals(LocalDate.of(2026, 7, 1), dto.getStartDate());
        assertEquals(8.0, dto.getExpectedDurationHours());
        assertEquals("PLANNED", dto.getStatus());
        assertEquals("AIRFRAME", dto.getComponent());
        assertNotNull(dto.getCreatedAt());
        assertNull(dto.getCompletedAt());
        assertNull(dto.getCompletionNotes());
        assertNull(dto.getActualDurationHours());
        assertEquals(1200.0, dto.getEstimatedCost());
        assertNull(dto.getActualCost());
        assertNull(dto.getNextMaintenanceDueDate());
        assertNull(dto.getNextMaintenanceDueHours());
        assertEquals(2, dto.getChecklist().size());
        assertEquals(record.getVersion(), dto.getVersion());
    }

    @Test
    void ensureDtoReflectsCompletedRecordDetails() {
        AircraftModel model = createModel();
        Aircraft aircraft = createAircraft(model);
        MaintenanceTemplate template = createTemplate(model);

        MaintenanceRecord record = new MaintenanceRecord(
                aircraft, template, "Routine check", LocalDate.of(2026, 7, 1),
                null, MaintenanceComponent.AIRFRAME, 1200.0);

        record.startWork();
        record.markAsCompleted("All good", 7.5, 1150.0);

        MaintenanceRecordResponseDTO dto = new MaintenanceRecordResponseDTO(record);

        assertEquals("COMPLETED", dto.getStatus());
        assertNotNull(dto.getCompletedAt());
        assertEquals("All good", dto.getCompletionNotes());
        assertEquals(7.5, dto.getActualDurationHours());
        assertEquals(1150.0, dto.getActualCost());
    }

    @Test
    void ensureDtoReflectsNextMaintenanceSchedulingFields() {
        AircraftModel model = createModel();
        Aircraft aircraft = createAircraft(model);
        MaintenanceTemplate template = createTemplate(model);

        MaintenanceRecord record = new MaintenanceRecord(
                aircraft, template, "Routine check", LocalDate.of(2026, 7, 1),
                null, MaintenanceComponent.AIRFRAME, 1200.0);

        record.scheduleNextMaintenance(LocalDate.of(2027, 1, 1), 9000.0);

        MaintenanceRecordResponseDTO dto = new MaintenanceRecordResponseDTO(record);

        assertEquals(LocalDate.of(2027, 1, 1), dto.getNextMaintenanceDueDate());
        assertEquals(9000.0, dto.getNextMaintenanceDueHours());
    }
}