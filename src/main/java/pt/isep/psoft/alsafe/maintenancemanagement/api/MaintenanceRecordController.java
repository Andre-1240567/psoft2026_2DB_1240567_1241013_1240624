package pt.isep.psoft.alsafe.maintenancemanagement.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pt.isep.psoft.alsafe.maintenancemanagement.api.dto.*;
import pt.isep.psoft.alsafe.maintenancemanagement.domain.MaintenanceRecord;
import pt.isep.psoft.alsafe.maintenancemanagement.services.MaintenanceRecordService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/maintenance-records")
@Tag(name = "Maintenance Records", description = "Endpoints for maintenance record management (WP#4A/B)")
public class MaintenanceRecordController {

    private final MaintenanceRecordService recordService;
    private final MaintenanceRecordModelAssembler assembler;

    public MaintenanceRecordController(MaintenanceRecordService recordService,
                                        MaintenanceRecordModelAssembler assembler) {
        this.recordService = recordService;
        this.assembler = assembler;
    }

    @Operation(summary = "Create a maintenance record for an aircraft (US115B)",
               description = "Opens a new maintenance record using an existing template. The template's " +
                             "checklist is deep-copied into the record at creation time. The aircraft " +
                             "status is automatically set to UNDER_MAINTENANCE. " +
                             "Requires MAINTENANCE_TECHNICIAN role.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Record created — aircraft marked as UNDER_MAINTENANCE"),
        @ApiResponse(responseCode = "400", description = "Invalid request body — missing fields, invalid component, " +
                                                         "or template not applicable to the aircraft's model"),
        @ApiResponse(responseCode = "401", description = "Authentication required — no valid JWT token provided"),
        @ApiResponse(responseCode = "403", description = "Access denied — requires MAINTENANCE_TECHNICIAN role"),
        @ApiResponse(responseCode = "404", description = "Aircraft or maintenance template not found"),
        @ApiResponse(responseCode = "409", description = "Aircraft is already UNDER_MAINTENANCE")
    })
    @PreAuthorize("hasRole('MAINTENANCE_TECHNICIAN')")
    @PostMapping
    public ResponseEntity<MaintenanceRecordResponseDTO> createRecord(
            @Valid @RequestBody CreateMaintenanceRecordDTO dto) {

        MaintenanceRecord created = recordService.createRecord(
                dto.getRegistrationNumber(),
                dto.getTemplateId(),
                dto.getDescription(),
                dto.getStartDate(),
                dto.getExpectedDurationHours(),
                parseComponent(dto.getComponent()),
                dto.getEstimatedCost()
        );

        return new ResponseEntity<>(assembler.toModel(created), HttpStatus.CREATED);
    }

    @Operation(summary = "View a maintenance record by id",
               description = "Returns the full details of a maintenance record, including its working " +
                             "checklist and HATEOAS links for valid state transitions. " +
                             "Accessible by MAINTENANCE_TECHNICIAN, MAINTENANCE_SUPERVISOR, and ATCC roles.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Record found and returned"),
        @ApiResponse(responseCode = "401", description = "Authentication required — no valid JWT token provided"),
        @ApiResponse(responseCode = "403", description = "Access denied — requires MAINTENANCE_TECHNICIAN, " +
                                                         "MAINTENANCE_SUPERVISOR, or ATCC role"),
        @ApiResponse(responseCode = "404", description = "No record found with the given id")
    })
    @PreAuthorize("hasAnyRole('MAINTENANCE_TECHNICIAN', 'MAINTENANCE_SUPERVISOR', 'ATCC')")
    @GetMapping("/{id}")
    public ResponseEntity<MaintenanceRecordResponseDTO> getRecordById(@PathVariable Long id) {
        MaintenanceRecord record = recordService.getRecordById(id);
        return ResponseEntity.ok(assembler.toModel(record));
    }

    @Operation(summary = "View all maintenance records for a specific aircraft (US116)",
               description = "Returns the full maintenance history for a given aircraft registration number. " +
                             "The list includes records in all statuses (PLANNED, IN_PROGRESS, COMPLETED, CANCELED).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Records returned — may be empty if no records exist yet"),
        @ApiResponse(responseCode = "401", description = "Authentication required — no valid JWT token provided"),
        @ApiResponse(responseCode = "403", description = "Access denied — requires MAINTENANCE_TECHNICIAN, " +
                                                         "MAINTENANCE_SUPERVISOR, or ATCC role"),
        @ApiResponse(responseCode = "404", description = "No aircraft found with the given registration number")
    })
    @PreAuthorize("hasAnyRole('MAINTENANCE_TECHNICIAN', 'MAINTENANCE_SUPERVISOR', 'ATCC')")
    @GetMapping("/aircraft/{registrationNumber}")
    public ResponseEntity<List<MaintenanceRecordResponseDTO>> getRecordsForAircraft(
            @PathVariable String registrationNumber) {

        List<MaintenanceRecord> records = recordService.getRecordsForAircraft(registrationNumber);
        return ResponseEntity.ok(records.stream().map(assembler::toModel).toList());
    }

    @Operation(summary = "View total maintenance hours for aircraft in the fleet (US117)",
               description = "Returns the sum of expected maintenance hours per aircraft, sorted descending. " +
                             "Useful for identifying high-maintenance aircraft in the fleet. " +
                             "Requires ATCC role.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Summary list returned — may be empty if no records exist"),
        @ApiResponse(responseCode = "401", description = "Authentication required — no valid JWT token provided"),
        @ApiResponse(responseCode = "403", description = "Access denied — requires ATCC role")
    })
    @PreAuthorize("hasRole('ATCC')")
    @GetMapping("/reports/total-hours")
    public ResponseEntity<List<MaintenanceHoursSummaryDTO>> getTotalMaintenanceHours() {
        List<MaintenanceHoursSummaryDTO> result = recordService.getTotalMaintenanceHoursPerAircraft()
                .stream()
                .map(MaintenanceHoursSummaryDTO::new)
                .toList();
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Start work on a planned maintenance record (PLANNED → IN_PROGRESS)",
               description = "Transitions the record from PLANNED to IN_PROGRESS. " +
                             "The version field is mandatory for optimistic locking — supply the version " +
                             "returned by the last GET. Requires MAINTENANCE_TECHNICIAN role.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Record transitioned to IN_PROGRESS"),
        @ApiResponse(responseCode = "400", description = "Missing version field in request body"),
        @ApiResponse(responseCode = "401", description = "Authentication required — no valid JWT token provided"),
        @ApiResponse(responseCode = "403", description = "Access denied — requires MAINTENANCE_TECHNICIAN role"),
        @ApiResponse(responseCode = "404", description = "No record found with the given id"),
        @ApiResponse(responseCode = "409", description = "Record is not PLANNED (already started, completed, or canceled), " +
                                                         "or optimistic locking conflict — re-fetch and retry")
    })
    @PreAuthorize("hasRole('MAINTENANCE_TECHNICIAN')")
    @PatchMapping("/{id}/start")
    public ResponseEntity<MaintenanceRecordResponseDTO> startRecord(
            @PathVariable Long id,
            @Valid @RequestBody VersionedActionDTO dto) {

        MaintenanceRecord updated = recordService.startRecord(id, dto.getVersion());
        return ResponseEntity.ok(assembler.toModel(updated));
    }

    @Operation(summary = "Mark a maintenance record as completed (US119 — IN_PROGRESS → COMPLETED)",
               description = "Closes an in-progress maintenance record. Completion notes and actual duration " +
                             "are mandatory. The aircraft status is restored to AVAILABLE. " +
                             "The version field is mandatory for optimistic locking. " +
                             "Requires MAINTENANCE_TECHNICIAN role.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Record marked COMPLETED — aircraft restored to AVAILABLE"),
        @ApiResponse(responseCode = "400", description = "Missing or invalid fields — completionNotes and " +
                                                         "actualDurationHours are mandatory and must be valid"),
        @ApiResponse(responseCode = "401", description = "Authentication required — no valid JWT token provided"),
        @ApiResponse(responseCode = "403", description = "Access denied — requires MAINTENANCE_TECHNICIAN role"),
        @ApiResponse(responseCode = "404", description = "No record found with the given id"),
        @ApiResponse(responseCode = "409", description = "Record is not IN_PROGRESS (cannot complete a PLANNED or " +
                                                         "already-closed record), or optimistic locking conflict")
    })
    @PreAuthorize("hasRole('MAINTENANCE_TECHNICIAN')")
    @PatchMapping("/{id}/complete")
    public ResponseEntity<MaintenanceRecordResponseDTO> completeRecord(
            @PathVariable Long id,
            @Valid @RequestBody CompleteMaintenanceRecordDTO dto) {

        MaintenanceRecord updated = recordService.completeRecord(
                id,
                dto.getVersion(),
                dto.getCompletionNotes(),
                dto.getActualDurationHours(),
                dto.getActualCost()
        );

        return ResponseEntity.ok(assembler.toModel(updated));
    }

    @Operation(summary = "Cancel a planned or in-progress maintenance record",
               description = "Voids a maintenance record that has not yet been completed. " +
                             "Can be called on PLANNED or IN_PROGRESS records. " +
                             "The aircraft status is restored to AVAILABLE. " +
                             "A cancellation reason is mandatory. " +
                             "Requires MAINTENANCE_TECHNICIAN role.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Record canceled — aircraft restored to AVAILABLE"),
        @ApiResponse(responseCode = "400", description = "Missing reason or version in request body"),
        @ApiResponse(responseCode = "401", description = "Authentication required — no valid JWT token provided"),
        @ApiResponse(responseCode = "403", description = "Access denied — requires MAINTENANCE_TECHNICIAN role"),
        @ApiResponse(responseCode = "404", description = "No record found with the given id"),
        @ApiResponse(responseCode = "409", description = "Record is already COMPLETED or CANCELED, " +
                                                         "or optimistic locking conflict — re-fetch and retry")
    })
    @PreAuthorize("hasRole('MAINTENANCE_TECHNICIAN')")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<MaintenanceRecordResponseDTO> cancelRecord(
            @PathVariable Long id,
            @Valid @RequestBody CancelMaintenanceRecordDTO dto) {

        MaintenanceRecord updated = recordService.cancelRecord(id, dto.getVersion(), dto.getReason());
        return ResponseEntity.ok(assembler.toModel(updated));
    }

    @Operation(summary = "Search maintenance records (US218)",
               description = "Flexible search across all maintenance records. All parameters are optional — " +
                             "omitting a filter means 'match all'. Supports filtering by aircraft registration, " +
                             "maintenance component (ENGINE, AIRFRAME, AVIONICS, INTERIOR, EXTERIOR), " +
                             "and/or date range (ISO-8601 dates: yyyy-MM-dd). " +
                             "Accessible by MAINTENANCE_TECHNICIAN, MAINTENANCE_SUPERVISOR, and ATCC roles.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Matching records returned — may be empty"),
        @ApiResponse(responseCode = "400", description = "Invalid component value or 'from' date is after 'to' date"),
        @ApiResponse(responseCode = "401", description = "Authentication required — no valid JWT token provided"),
        @ApiResponse(responseCode = "403", description = "Access denied — requires MAINTENANCE_TECHNICIAN, " +
                                                         "MAINTENANCE_SUPERVISOR, or ATCC role")
    })
    @PreAuthorize("hasAnyRole('MAINTENANCE_TECHNICIAN', 'MAINTENANCE_SUPERVISOR', 'ATCC')")
    @GetMapping("/search")
    public ResponseEntity<List<MaintenanceRecordResponseDTO>> searchRecords(
            @RequestParam(required = false) String registrationNumber,
            @RequestParam(required = false) String component,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        List<MaintenanceRecord> records = recordService.searchRecords(registrationNumber, component, from, to);
        return ResponseEntity.ok(records.stream().map(assembler::toModel).toList());
    }

    @Operation(summary = "View all ongoing maintenance activities across the fleet (US219)",
               description = "Returns all records currently in PLANNED or IN_PROGRESS status, " +
                             "sorted by start date ascending. Intended for fleet-wide oversight " +
                             "by the Maintenance Supervisor. Requires MAINTENANCE_SUPERVISOR role.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ongoing records returned — may be empty"),
        @ApiResponse(responseCode = "401", description = "Authentication required — no valid JWT token provided"),
        @ApiResponse(responseCode = "403", description = "Access denied — requires MAINTENANCE_SUPERVISOR role")
    })
    @PreAuthorize("hasRole('MAINTENANCE_SUPERVISOR')")
    @GetMapping("/ongoing")
    public ResponseEntity<List<MaintenanceRecordResponseDTO>> getOngoingActivities() {
        List<MaintenanceRecord> records = recordService.getOngoingActivities();
        return ResponseEntity.ok(records.stream().map(assembler::toModel).toList());
    }

    @Operation(summary = "Generate maintenance cost report per aircraft (US220)",
               description = "Returns estimated and actual maintenance costs aggregated by aircraft " +
                             "registration number, sorted by actual cost descending. " +
                             "Requires ATCC role.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cost summary list returned — may be empty"),
        @ApiResponse(responseCode = "401", description = "Authentication required — no valid JWT token provided"),
        @ApiResponse(responseCode = "403", description = "Access denied — requires ATCC role")
    })
    @PreAuthorize("hasRole('ATCC')")
    @GetMapping("/reports/cost-per-aircraft")
    public ResponseEntity<List<MaintenanceCostSummaryDTO>> getCostReportPerAircraft() {
        List<MaintenanceCostSummaryDTO> result = recordService.getCostReportPerAircraft()
                .stream()
                .map(MaintenanceCostSummaryDTO::new)
                .toList();
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Generate maintenance cost report per aircraft model (US220)",
               description = "Returns estimated and actual maintenance costs aggregated by aircraft model name, " +
                             "sorted by actual cost descending. Useful for comparing costs across fleet types. " +
                             "Requires ATCC role.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cost summary list returned — may be empty"),
        @ApiResponse(responseCode = "401", description = "Authentication required — no valid JWT token provided"),
        @ApiResponse(responseCode = "403", description = "Access denied — requires ATCC role")
    })
    @PreAuthorize("hasRole('ATCC')")
    @GetMapping("/reports/cost-per-model")
    public ResponseEntity<List<MaintenanceCostSummaryDTO>> getCostReportPerAircraftModel() {
        List<MaintenanceCostSummaryDTO> result = recordService.getCostReportPerAircraftModel()
                .stream()
                .map(MaintenanceCostSummaryDTO::new)
                .toList();
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "View average maintenance turnaround time per aircraft type (US221)",
               description = "Returns the average actual maintenance duration (in hours) per aircraft model, " +
                             "computed only from COMPLETED records. Sorted by average duration descending. " +
                             "Requires MAINTENANCE_SUPERVISOR role.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Turnaround summary list returned — may be empty " +
                                                         "if no records have been completed yet"),
        @ApiResponse(responseCode = "401", description = "Authentication required — no valid JWT token provided"),
        @ApiResponse(responseCode = "403", description = "Access denied — requires MAINTENANCE_SUPERVISOR role")
    })
    @PreAuthorize("hasRole('MAINTENANCE_SUPERVISOR')")
    @GetMapping("/reports/avg-turnaround")
    public ResponseEntity<List<MaintenanceTurnaroundSummaryDTO>> getAverageTurnaround() {
        List<MaintenanceTurnaroundSummaryDTO> result = recordService.getAverageTurnaroundPerAircraftModel()
                .stream()
                .map(MaintenanceTurnaroundSummaryDTO::new)
                .toList();
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "List aircraft due for scheduled maintenance by calendar date (US222)",
               description = "Returns records where the nextMaintenanceDueDate is today or in the past, " +
                             "indicating overdue or due-today maintenance. Sorted by due date ascending. " +
                             "Requires ATCC role.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Due records returned — empty list means no aircraft are overdue"),
        @ApiResponse(responseCode = "401", description = "Authentication required — no valid JWT token provided"),
        @ApiResponse(responseCode = "403", description = "Access denied — requires ATCC role")
    })
    @PreAuthorize("hasRole('ATCC')")
    @GetMapping("/alerts/due-by-date")
    public ResponseEntity<List<MaintenanceRecordResponseDTO>> getAlertsDueByDate() {
        List<MaintenanceRecord> records = recordService.getAlertsDueByDate();
        return ResponseEntity.ok(records.stream().map(assembler::toModel).toList());
    }

    @Operation(summary = "List aircraft due for scheduled maintenance by flight hours (US222)",
               description = "Returns records where the aircraft's accumulated flight hours have reached or " +
                             "exceeded the nextMaintenanceDueHours threshold. " +
                             "Sorted by flight hours descending (most overdue first). " +
                             "Requires ATCC role.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Due records returned — empty list means no aircraft have hit their hour threshold"),
        @ApiResponse(responseCode = "401", description = "Authentication required — no valid JWT token provided"),
        @ApiResponse(responseCode = "403", description = "Access denied — requires ATCC role")
    })
    @PreAuthorize("hasRole('ATCC')")
    @GetMapping("/alerts/due-by-flight-hours")
    public ResponseEntity<List<MaintenanceRecordResponseDTO>> getAlertsDueByFlightHours() {
        List<MaintenanceRecord> records = recordService.getAlertsDueByFlightHours();
        return ResponseEntity.ok(records.stream().map(assembler::toModel).toList());
    }

    private pt.isep.psoft.alsafe.maintenancemanagement.domain.MaintenanceComponent parseComponent(String value) {
        try {
            return pt.isep.psoft.alsafe.maintenancemanagement.domain.MaintenanceComponent.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid maintenance component: '" + value
                    + "'. Valid values are: ENGINE, AIRFRAME, AVIONICS, INTERIOR, EXTERIOR.");
        }
    }
}