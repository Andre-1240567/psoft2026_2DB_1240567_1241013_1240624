package pt.isep.psoft.alsafe.maintenancemanagement.api;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.Aircraft;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftModel;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.Manufacturer;
import pt.isep.psoft.alsafe.maintenancemanagement.api.dto.MaintenanceRecordResponseDTO;
import pt.isep.psoft.alsafe.maintenancemanagement.domain.MaintenanceComponent;
import pt.isep.psoft.alsafe.maintenancemanagement.domain.MaintenanceRecord;
import pt.isep.psoft.alsafe.maintenancemanagement.domain.MaintenanceTemplate;
import pt.isep.psoft.alsafe.maintenancemanagement.domain.TemplateType;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MaintenanceRecordModelAssemblerTest {

    private final MaintenanceRecordModelAssembler assembler = new MaintenanceRecordModelAssembler();

    @BeforeEach
    void setUpRequestContext() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void tearDownRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    private MaintenanceRecord createPlannedRecord() {
        AircraftModel model = new AircraftModel(Manufacturer.AIRBUS, "A320neo", 160, 24000.0, 6300.0, 828.0);
        Aircraft aircraft = new Aircraft("CS-TPA", model, LocalDate.of(2024, 1, 15), "Economy");

        MaintenanceTemplate template = new MaintenanceTemplate(
                "A-Check Routine Inspection",
                TemplateType.INSPECTION,
                8.0,
                List.of(model),
                List.of("Visual inspection")
        );
        ReflectionTestUtils.setField(template, "id", 1L);

        MaintenanceRecord record = new MaintenanceRecord(
                aircraft, template, "Routine check", LocalDate.of(2026, 7, 1),
                null, MaintenanceComponent.AIRFRAME, 1200.0);

        ReflectionTestUtils.setField(record, "id", 10L);
        return record;
    }

    @Test
    void ensureToModelAddsSelfAndAircraftRecordsLinks() {
        MaintenanceRecord record = createPlannedRecord();

        MaintenanceRecordResponseDTO dto = assembler.toModel(record);

        assertTrue(dto.getLink("self").isPresent());
        assertTrue(dto.getLink("self").get().getHref().contains("/api/maintenance-records/10"));
        assertTrue(dto.getLink("aircraft-records").isPresent());
        assertTrue(dto.getLink("aircraft-records").get().getHref().contains("CS-TPA"));
    }

    @Test
    void ensurePlannedRecordExposesStartAndCancelLinks() {
        MaintenanceRecord record = createPlannedRecord();

        MaintenanceRecordResponseDTO dto = assembler.toModel(record);

        assertTrue(dto.getLink("start").isPresent());
        assertTrue(dto.getLink("cancel").isPresent());
        assertTrue(dto.getLink("complete").isEmpty());
    }

    @Test
    void ensureInProgressRecordExposesCompleteAndCancelLinks() {
        MaintenanceRecord record = createPlannedRecord();
        record.startWork();

        MaintenanceRecordResponseDTO dto = assembler.toModel(record);

        assertTrue(dto.getLink("complete").isPresent());
        assertTrue(dto.getLink("cancel").isPresent());
        assertTrue(dto.getLink("start").isEmpty());
    }

    @Test
    void ensureCompletedRecordExposesNoActionLinks() {
        MaintenanceRecord record = createPlannedRecord();
        record.startWork();
        record.markAsCompleted("All good", 7.5, 1150.0);

        MaintenanceRecordResponseDTO dto = assembler.toModel(record);

        assertTrue(dto.getLink("start").isEmpty());
        assertTrue(dto.getLink("complete").isEmpty());
        assertTrue(dto.getLink("cancel").isEmpty());
    }

    @Test
    void ensureCanceledRecordExposesNoActionLinks() {
        MaintenanceRecord record = createPlannedRecord();
        record.cancel("No longer needed");

        MaintenanceRecordResponseDTO dto = assembler.toModel(record);

        assertTrue(dto.getLink("start").isEmpty());
        assertTrue(dto.getLink("complete").isEmpty());
        assertTrue(dto.getLink("cancel").isEmpty());
    }
}