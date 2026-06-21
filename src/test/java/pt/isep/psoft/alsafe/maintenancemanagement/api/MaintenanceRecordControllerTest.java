package pt.isep.psoft.alsafe.maintenancemanagement.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import pt.isep.psoft.alsafe.aircraftmanagement.domain.Aircraft;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftModel;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.Manufacturer;
import pt.isep.psoft.alsafe.maintenancemanagement.api.dto.*;
import pt.isep.psoft.alsafe.maintenancemanagement.domain.MaintenanceComponent;
import pt.isep.psoft.alsafe.maintenancemanagement.domain.MaintenanceRecord;
import pt.isep.psoft.alsafe.maintenancemanagement.domain.MaintenanceTemplate;
import pt.isep.psoft.alsafe.maintenancemanagement.domain.TemplateType;
import pt.isep.psoft.alsafe.maintenancemanagement.services.MaintenanceRecordService;
import pt.isep.psoft.alsafe.maintenancemanagement.services.MaintenanceRecordService.MaintenanceCostSummary;
import pt.isep.psoft.alsafe.maintenancemanagement.services.MaintenanceRecordService.MaintenanceHoursSummary;
import pt.isep.psoft.alsafe.maintenancemanagement.services.MaintenanceRecordService.MaintenanceTurnaroundSummary;
import pt.isep.psoft.alsafe.security.jwt.AuthTokenFilter;
import pt.isep.psoft.alsafe.shared.exceptions.ResourceNotFoundException;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MaintenanceRecordController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(MaintenanceRecordModelAssembler.class)
class MaintenanceRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MaintenanceRecordService recordService;

    @MockBean
    private AuthTokenFilter authTokenFilter;

    private MaintenanceRecord plannedRecord;

    @BeforeEach
    void setUp() {
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

        plannedRecord = new MaintenanceRecord(
                aircraft, template, "Routine check", LocalDate.of(2026, 7, 1),
                null, MaintenanceComponent.AIRFRAME, 1200.0);
        ReflectionTestUtils.setField(plannedRecord, "id", 10L);
    }

    
    
    

    @Test
    void ensureCreateRecordReturns201Created() throws Exception {
        CreateMaintenanceRecordDTO dto = new CreateMaintenanceRecordDTO();
        dto.setRegistrationNumber("CS-TPA");
        dto.setTemplateId(1L);
        dto.setDescription("Routine check");
        dto.setStartDate(LocalDate.of(2026, 7, 1));
        dto.setComponent("AIRFRAME");
        dto.setEstimatedCost(1200.0);

        when(recordService.createRecord(eq("CS-TPA"), eq(1L), eq("Routine check"),
                eq(LocalDate.of(2026, 7, 1)), isNull(), eq(MaintenanceComponent.AIRFRAME), eq(1200.0)))
                .thenReturn(plannedRecord);

        mockMvc.perform(post("/api/maintenance-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.registrationNumber").value("CS-TPA"))
                .andExpect(jsonPath("$.status").value("PLANNED"))
                .andExpect(jsonPath("$._links.self").exists())
                .andExpect(jsonPath("$._links.start").exists());
    }

    @Test
    void ensureCreateRecordWithInvalidComponentReturns400() throws Exception {
        CreateMaintenanceRecordDTO dto = new CreateMaintenanceRecordDTO();
        dto.setRegistrationNumber("CS-TPA");
        dto.setTemplateId(1L);
        dto.setDescription("Routine check");
        dto.setStartDate(LocalDate.of(2026, 7, 1));
        dto.setComponent("NOT_A_REAL_COMPONENT");

        mockMvc.perform(post("/api/maintenance-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ensureCreateRecordWithMissingDescriptionReturns400() throws Exception {
        CreateMaintenanceRecordDTO dto = new CreateMaintenanceRecordDTO();
        dto.setRegistrationNumber("CS-TPA");
        dto.setTemplateId(1L);
        dto.setStartDate(LocalDate.of(2026, 7, 1));
        dto.setComponent("AIRFRAME");

        mockMvc.perform(post("/api/maintenance-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ensureCreateRecordForUnknownAircraftReturns404() throws Exception {
        CreateMaintenanceRecordDTO dto = new CreateMaintenanceRecordDTO();
        dto.setRegistrationNumber("XX-XXX");
        dto.setTemplateId(1L);
        dto.setDescription("Routine check");
        dto.setStartDate(LocalDate.of(2026, 7, 1));
        dto.setComponent("AIRFRAME");

        when(recordService.createRecord(any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new ResourceNotFoundException("Aircraft 'XX-XXX' not found."));

        mockMvc.perform(post("/api/maintenance-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void ensureCreateRecordForAircraftAlreadyUnderMaintenanceReturns409() throws Exception {
        CreateMaintenanceRecordDTO dto = new CreateMaintenanceRecordDTO();
        dto.setRegistrationNumber("CS-TPA");
        dto.setTemplateId(1L);
        dto.setDescription("Routine check");
        dto.setStartDate(LocalDate.of(2026, 7, 1));
        dto.setComponent("AIRFRAME");

        when(recordService.createRecord(any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new IllegalStateException("Aircraft 'CS-TPA' is already under maintenance."));

        mockMvc.perform(post("/api/maintenance-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    void ensureCreateRecordWithTemplateNotApplicableReturns400() throws Exception {
        CreateMaintenanceRecordDTO dto = new CreateMaintenanceRecordDTO();
        dto.setRegistrationNumber("CS-TPA");
        dto.setTemplateId(1L);
        dto.setDescription("Routine check");
        dto.setStartDate(LocalDate.of(2026, 7, 1));
        dto.setComponent("AIRFRAME");

        when(recordService.createRecord(any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException(
                        "Template 'A-Check Routine Inspection' is not applicable to aircraft model 'A350'."));

        mockMvc.perform(post("/api/maintenance-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    
    
    

    @Test
    void ensureGetRecordByIdReturns200() throws Exception {
        when(recordService.getRecordById(10L)).thenReturn(plannedRecord);

        mockMvc.perform(get("/api/maintenance-records/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$._links.self").exists());
    }

    @Test
    void ensureGetRecordByIdReturns404WhenNotFound() throws Exception {
        when(recordService.getRecordById(999L))
                .thenThrow(new ResourceNotFoundException("Maintenance record with id '999' not found."));

        mockMvc.perform(get("/api/maintenance-records/999"))
                .andExpect(status().isNotFound());
    }

    
    
    

    @Test
    void ensureGetRecordsForAircraftReturns200() throws Exception {
        when(recordService.getRecordsForAircraft("CS-TPA")).thenReturn(List.of(plannedRecord));

        mockMvc.perform(get("/api/maintenance-records/aircraft/CS-TPA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].registrationNumber").value("CS-TPA"));
    }

    @Test
    void ensureGetRecordsForUnknownAircraftReturns404() throws Exception {
        when(recordService.getRecordsForAircraft("XX-XXX"))
                .thenThrow(new ResourceNotFoundException("Aircraft 'XX-XXX' not found."));

        mockMvc.perform(get("/api/maintenance-records/aircraft/XX-XXX"))
                .andExpect(status().isNotFound());
    }

    
    
    

    @Test
    void ensureGetTotalMaintenanceHoursReturns200() throws Exception {
        when(recordService.getTotalMaintenanceHoursPerAircraft())
                .thenReturn(List.of(new MaintenanceHoursSummary("CS-TPA", 125.5)));

        mockMvc.perform(get("/api/maintenance-records/reports/total-hours"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].registrationNumber").value("CS-TPA"))
                .andExpect(jsonPath("$[0].totalHours").value(125.5));
    }

    
    
    

    @Test
    void ensureStartRecordReturns200() throws Exception {
        VersionedActionDTO dto = new VersionedActionDTO();
        dto.setVersion(0L);

        plannedRecord.startWork();
        when(recordService.startRecord(10L, 0L)).thenReturn(plannedRecord);

        mockMvc.perform(patch("/api/maintenance-records/10/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void ensureStartRecordWithOutdatedVersionReturns409() throws Exception {
        VersionedActionDTO dto = new VersionedActionDTO();
        dto.setVersion(99L);

        when(recordService.startRecord(10L, 99L))
                .thenThrow(new org.springframework.orm.ObjectOptimisticLockingFailureException(
                        MaintenanceRecord.class, 10L));

        mockMvc.perform(patch("/api/maintenance-records/10/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    void ensureStartRecordWithoutVersionReturns400() throws Exception {
        mockMvc.perform(patch("/api/maintenance-records/10/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ensureStartAlreadyStartedRecordReturns409() throws Exception {
        VersionedActionDTO dto = new VersionedActionDTO();
        dto.setVersion(0L);

        when(recordService.startRecord(10L, 0L))
                .thenThrow(new IllegalStateException("Cannot start work: record is already in status 'IN_PROGRESS'."));

        mockMvc.perform(patch("/api/maintenance-records/10/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    
    
    

    @Test
    void ensureCompleteRecordReturns200() throws Exception {
        CompleteMaintenanceRecordDTO dto = new CompleteMaintenanceRecordDTO();
        dto.setCompletionNotes("All good");
        dto.setActualDurationHours(7.5);
        dto.setActualCost(1150.0);
        dto.setVersion(0L);

        plannedRecord.startWork();
        plannedRecord.markAsCompleted("All good", 7.5, 1150.0);
        when(recordService.completeRecord(10L, 0L, "All good", 7.5, 1150.0)).thenReturn(plannedRecord);

        mockMvc.perform(patch("/api/maintenance-records/10/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.completionNotes").value("All good"));
    }

    @Test
    void ensureCompleteRecordWithoutNotesReturns400() throws Exception {
        CompleteMaintenanceRecordDTO dto = new CompleteMaintenanceRecordDTO();
        dto.setActualDurationHours(7.5);
        dto.setVersion(0L);

        mockMvc.perform(patch("/api/maintenance-records/10/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ensureCompletePlannedRecordReturns409() throws Exception {
        CompleteMaintenanceRecordDTO dto = new CompleteMaintenanceRecordDTO();
        dto.setCompletionNotes("All good");
        dto.setActualDurationHours(7.5);
        dto.setVersion(0L);

        when(recordService.completeRecord(any(), any(), any(), any(), any()))
                .thenThrow(new IllegalStateException(
                        "Cannot complete a record that is not IN_PROGRESS. Current status: PLANNED."));

        mockMvc.perform(patch("/api/maintenance-records/10/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    void ensureCompleteRecordReturns404WhenNotFound() throws Exception {
        CompleteMaintenanceRecordDTO dto = new CompleteMaintenanceRecordDTO();
        dto.setCompletionNotes("All good");
        dto.setActualDurationHours(7.5);
        dto.setVersion(0L);

        when(recordService.completeRecord(any(), any(), any(), any(), any()))
                .thenThrow(new ResourceNotFoundException("Maintenance record with id '999' not found."));

        mockMvc.perform(patch("/api/maintenance-records/999/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    
    
    

    @Test
    void ensureCancelRecordReturns200() throws Exception {
        CancelMaintenanceRecordDTO dto = new CancelMaintenanceRecordDTO();
        dto.setReason("No longer needed");
        dto.setVersion(0L);

        plannedRecord.cancel("No longer needed");
        when(recordService.cancelRecord(10L, 0L, "No longer needed")).thenReturn(plannedRecord);

        mockMvc.perform(patch("/api/maintenance-records/10/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));
    }

    @Test
    void ensureCancelRecordWithoutReasonReturns400() throws Exception {
        CancelMaintenanceRecordDTO dto = new CancelMaintenanceRecordDTO();
        dto.setVersion(0L);

        mockMvc.perform(patch("/api/maintenance-records/10/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ensureCancelAlreadyCompletedRecordReturns409() throws Exception {
        CancelMaintenanceRecordDTO dto = new CancelMaintenanceRecordDTO();
        dto.setReason("Too late");
        dto.setVersion(0L);

        when(recordService.cancelRecord(any(), any(), any()))
                .thenThrow(new IllegalStateException("Cannot cancel a record in status 'COMPLETED'."));

        mockMvc.perform(patch("/api/maintenance-records/10/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    
    
    

    @Test
    void ensureSearchRecordsReturns200() throws Exception {
        when(recordService.searchRecords("CS-TPA", "AIRFRAME",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)))
                .thenReturn(List.of(plannedRecord));

        mockMvc.perform(get("/api/maintenance-records/search")
                        .param("registrationNumber", "CS-TPA")
                        .param("component", "AIRFRAME")
                        .param("from", "2026-01-01")
                        .param("to", "2026-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].registrationNumber").value("CS-TPA"));
    }

    @Test
    void ensureSearchRecordsWithoutFiltersReturns200() throws Exception {
        when(recordService.searchRecords(isNull(), isNull(), isNull(), isNull()))
                .thenReturn(List.of(plannedRecord));

        mockMvc.perform(get("/api/maintenance-records/search"))
                .andExpect(status().isOk());
    }

    @Test
    void ensureSearchRecordsWithInvalidComponentReturns400() throws Exception {
        when(recordService.searchRecords(any(), eq("NOT_REAL"), any(), any()))
                .thenThrow(new IllegalArgumentException("Invalid maintenance component: 'NOT_REAL'."));

        mockMvc.perform(get("/api/maintenance-records/search").param("component", "NOT_REAL"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ensureSearchRecordsWithFromAfterToReturns400() throws Exception {
        when(recordService.searchRecords(any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Start date 'from' cannot be after end date 'to'."));

        mockMvc.perform(get("/api/maintenance-records/search")
                        .param("from", "2026-12-31")
                        .param("to", "2026-01-01"))
                .andExpect(status().isBadRequest());
    }

    
    
    

    @Test
    void ensureGetOngoingActivitiesReturns200() throws Exception {
        when(recordService.getOngoingActivities()).thenReturn(List.of(plannedRecord));

        mockMvc.perform(get("/api/maintenance-records/ongoing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PLANNED"));
    }

    @Test
    void ensureGetOngoingActivitiesReturnsEmptyList() throws Exception {
        when(recordService.getOngoingActivities()).thenReturn(List.of());

        mockMvc.perform(get("/api/maintenance-records/ongoing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    
    
    

    @Test
    void ensureGetCostReportPerAircraftReturns200() throws Exception {
        when(recordService.getCostReportPerAircraft())
                .thenReturn(List.of(new MaintenanceCostSummary("CS-TPA", 1200.0, 1150.0)));

        mockMvc.perform(get("/api/maintenance-records/reports/cost-per-aircraft"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].key").value("CS-TPA"))
                .andExpect(jsonPath("$[0].estimatedCost").value(1200.0))
                .andExpect(jsonPath("$[0].actualCost").value(1150.0));
    }

    @Test
    void ensureGetCostReportPerAircraftModelReturns200() throws Exception {
        when(recordService.getCostReportPerAircraftModel())
                .thenReturn(List.of(new MaintenanceCostSummary("A320neo", 1200.0, 1150.0)));

        mockMvc.perform(get("/api/maintenance-records/reports/cost-per-model"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].key").value("A320neo"));
    }

    
    
    

    @Test
    void ensureGetAverageTurnaroundReturns200() throws Exception {
        when(recordService.getAverageTurnaroundPerAircraftModel())
                .thenReturn(List.of(new MaintenanceTurnaroundSummary("A320neo", 9.5)));

        mockMvc.perform(get("/api/maintenance-records/reports/avg-turnaround"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].modelName").value("A320neo"))
                .andExpect(jsonPath("$[0].avgDurationHours").value(9.5));
    }

    
    
    

    @Test
    void ensureGetAlertsDueByDateReturns200() throws Exception {
        when(recordService.getAlertsDueByDate()).thenReturn(List.of(plannedRecord));

        mockMvc.perform(get("/api/maintenance-records/alerts/due-by-date"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].registrationNumber").value("CS-TPA"));
    }

    @Test
    void ensureGetAlertsDueByFlightHoursReturns200() throws Exception {
        when(recordService.getAlertsDueByFlightHours()).thenReturn(List.of(plannedRecord));

        mockMvc.perform(get("/api/maintenance-records/alerts/due-by-flight-hours"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].registrationNumber").value("CS-TPA"));
    }

    @Test
    void ensureMalformedJsonReturns400() throws Exception {
        mockMvc.perform(post("/api/maintenance-records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ not valid json }"))
                .andExpect(status().isBadRequest());
    }
}