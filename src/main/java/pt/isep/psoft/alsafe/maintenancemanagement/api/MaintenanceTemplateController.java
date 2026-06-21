package pt.isep.psoft.alsafe.maintenancemanagement.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pt.isep.psoft.alsafe.maintenancemanagement.api.dto.CreateMaintenanceTemplateDTO;
import pt.isep.psoft.alsafe.maintenancemanagement.api.dto.MaintenanceTemplateResponseDTO;
import pt.isep.psoft.alsafe.maintenancemanagement.api.dto.UpdateMaintenanceTemplateDTO;
import pt.isep.psoft.alsafe.maintenancemanagement.domain.MaintenanceTemplate;
import pt.isep.psoft.alsafe.maintenancemanagement.domain.TemplateType;
import pt.isep.psoft.alsafe.maintenancemanagement.services.MaintenanceTemplateService;

import java.util.List;

@RestController
@RequestMapping("/api/maintenance-templates")
@Tag(name = "Maintenance Templates", description = "Endpoints for maintenance template management (WP#4A)")
public class MaintenanceTemplateController {

    private final MaintenanceTemplateService templateService;
    private final MaintenanceTemplateModelAssembler assembler;

    public MaintenanceTemplateController(MaintenanceTemplateService templateService,
                                          MaintenanceTemplateModelAssembler assembler) {
        this.templateService = templateService;
        this.assembler = assembler;
    }

    @Operation(summary = "Create a maintenance template (US115A)",
               description = "Creates a reusable maintenance template with a checklist and applicable aircraft models. " +
                             "Requires MAINTENANCE_TECHNICIAN role.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Template created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request body — missing or invalid fields, " +
                                                         "blank checklist items, or empty model list"),
        @ApiResponse(responseCode = "401", description = "Authentication required — no valid JWT token provided"),
        @ApiResponse(responseCode = "403", description = "Access denied — requires MAINTENANCE_TECHNICIAN role"),
        @ApiResponse(responseCode = "404", description = "One or more aircraft model IDs not found"),
        @ApiResponse(responseCode = "409", description = "A template with the same name already exists")
    })
    @PreAuthorize("hasRole('MAINTENANCE_TECHNICIAN')")
    @PostMapping
    public ResponseEntity<MaintenanceTemplateResponseDTO> createTemplate(
            @Valid @RequestBody CreateMaintenanceTemplateDTO dto) {

        TemplateType type = parseTemplateType(dto.getTemplateType());

        MaintenanceTemplate created = templateService.createTemplate(
                dto.getTemplateName(),
                type,
                dto.getDefaultDurationHours(),
                dto.getApplicableModelIds(),
                dto.getChecklistItems()
        );

        return new ResponseEntity<>(assembler.toModel(created), HttpStatus.CREATED);
    }

    @Operation(summary = "View a maintenance template by id",
               description = "Returns the full details of a maintenance template, including its checklist " +
                             "and applicable aircraft models. Accessible by MAINTENANCE_TECHNICIAN, " +
                             "MAINTENANCE_SUPERVISOR, and ATCC roles.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Template found and returned"),
        @ApiResponse(responseCode = "401", description = "Authentication required — no valid JWT token provided"),
        @ApiResponse(responseCode = "403", description = "Access denied — requires MAINTENANCE_TECHNICIAN, " +
                                                         "MAINTENANCE_SUPERVISOR, or ATCC role"),
        @ApiResponse(responseCode = "404", description = "No template found with the given id")
    })
    @PreAuthorize("hasAnyRole('MAINTENANCE_TECHNICIAN', 'MAINTENANCE_SUPERVISOR', 'ATCC')")
    @GetMapping("/{id}")
    public ResponseEntity<MaintenanceTemplateResponseDTO> getTemplateById(@PathVariable Long id) {
        MaintenanceTemplate template = templateService.getTemplateById(id);
        return ResponseEntity.ok(assembler.toModel(template));
    }

    @Operation(summary = "List all maintenance templates",
               description = "Returns all maintenance templates. Optionally filters by template type " +
                             "(INSPECTION, SCHEDULED_MAINTENANCE, OVERHAUL, MODIFICATION) or by applicable " +
                             "aircraft model ID. If both filters are provided, modelId takes priority.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List returned — may be empty if no templates match"),
        @ApiResponse(responseCode = "400", description = "Invalid templateType value — must be one of: " +
                                                         "INSPECTION, SCHEDULED_MAINTENANCE, OVERHAUL, MODIFICATION"),
        @ApiResponse(responseCode = "401", description = "Authentication required — no valid JWT token provided"),
        @ApiResponse(responseCode = "403", description = "Access denied — requires MAINTENANCE_TECHNICIAN, " +
                                                         "MAINTENANCE_SUPERVISOR, or ATCC role"),
        @ApiResponse(responseCode = "404", description = "modelId provided but no aircraft model found with that id")
    })
    @PreAuthorize("hasAnyRole('MAINTENANCE_TECHNICIAN', 'MAINTENANCE_SUPERVISOR', 'ATCC')")
    @GetMapping
    public ResponseEntity<List<MaintenanceTemplateResponseDTO>> getTemplates(
            @RequestParam(required = false) String templateType,
            @RequestParam(required = false) Long modelId) {

        List<MaintenanceTemplate> templates;
        if (modelId != null) {
            templates = templateService.getTemplatesForModel(modelId);
        } else if (templateType != null && !templateType.isBlank()) {
            templates = templateService.getTemplatesByType(parseTemplateType(templateType));
        } else {
            templates = templateService.getAllTemplates();
        }

        return ResponseEntity.ok(templates.stream().map(assembler::toModel).toList());
    }

    @Operation(summary = "Update a maintenance template (partial update)",
               description = "Updates one or more fields of an existing template. Any field left null " +
                             "keeps its current value. The version field is mandatory for optimistic " +
                             "locking — supply the version returned by the last GET. " +
                             "Requires MAINTENANCE_TECHNICIAN role.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Template updated and returned"),
        @ApiResponse(responseCode = "400", description = "Invalid request body — invalid templateType, " +
                                                         "non-positive duration, or blank checklist items"),
        @ApiResponse(responseCode = "401", description = "Authentication required — no valid JWT token provided"),
        @ApiResponse(responseCode = "403", description = "Access denied — requires MAINTENANCE_TECHNICIAN role"),
        @ApiResponse(responseCode = "404", description = "Template not found, or one or more aircraft model IDs not found"),
        @ApiResponse(responseCode = "409", description = "Optimistic locking conflict — template was modified " +
                                                         "by another request; re-fetch and retry. " +
                                                         "Also returned if the new name is already taken.")
    })
    @PreAuthorize("hasRole('MAINTENANCE_TECHNICIAN')")
    @PatchMapping("/{id}")
    public ResponseEntity<MaintenanceTemplateResponseDTO> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMaintenanceTemplateDTO dto) {

        TemplateType type = dto.getTemplateType() != null ? parseTemplateType(dto.getTemplateType()) : null;

        MaintenanceTemplate updated = templateService.updateTemplate(
                id,
                dto.getVersion(),
                dto.getTemplateName(),
                type,
                dto.getDefaultDurationHours(),
                dto.getApplicableModelIds(),
                dto.getChecklistItems()
        );

        return ResponseEntity.ok(assembler.toModel(updated));
    }

    private TemplateType parseTemplateType(String value) {
        try {
            return TemplateType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid template type: '" + value
                    + "'. Valid values are: INSPECTION, SCHEDULED_MAINTENANCE, OVERHAUL, MODIFICATION.");
        }
    }
}