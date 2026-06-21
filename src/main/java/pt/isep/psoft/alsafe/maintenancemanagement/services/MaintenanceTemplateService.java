package pt.isep.psoft.alsafe.maintenancemanagement.services;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftModel;
import pt.isep.psoft.alsafe.aircraftmanagement.repositories.AircraftModelRepository;
import pt.isep.psoft.alsafe.maintenancemanagement.domain.MaintenanceTemplate;
import pt.isep.psoft.alsafe.maintenancemanagement.domain.TemplateType;
import pt.isep.psoft.alsafe.maintenancemanagement.repositories.MaintenanceTemplateRepository;
import pt.isep.psoft.alsafe.shared.exceptions.ResourceNotFoundException;

import java.util.List;

/**
 * Application service for {@link MaintenanceTemplate} management (US115A).
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Orchestrate repository calls and domain object creation.</li>
 *   <li>Enforce uniqueness constraints that span multiple aggregates
 *       (e.g. template name uniqueness — the domain object cannot check this alone).</li>
 *   <li>Translate service-layer concerns into domain calls.</li>
 * </ul>
 *
 * <p>This service does <strong>not</strong> contain business logic — that lives in the domain.
 */
@Service
public class MaintenanceTemplateService {

    private final MaintenanceTemplateRepository templateRepository;
    private final AircraftModelRepository aircraftModelRepository;

    public MaintenanceTemplateService(MaintenanceTemplateRepository templateRepository,
                                      AircraftModelRepository aircraftModelRepository) {
        this.templateRepository = templateRepository;
        this.aircraftModelRepository = aircraftModelRepository;
    }

    // -------------------------------------------------------------------------
    // US115A — Create a maintenance template
    // -------------------------------------------------------------------------

    /**
     * Creates and persists a new {@link MaintenanceTemplate}.
     *
     * @param templateName        unique name; service enforces uniqueness.
     * @param templateType        category of work.
     * @param defaultDurationHours expected hours.
     * @param modelIds            IDs of applicable {@link AircraftModel}s; at least one required.
     * @param checklistDescriptions task descriptions; at least one required.
     * @return the persisted template.
     * @throws IllegalArgumentException if the name is already taken or any model ID is not found.
     */
    @Transactional
    public MaintenanceTemplate createTemplate(String templateName,
                                              TemplateType templateType,
                                              Double defaultDurationHours,
                                              List<Long> modelIds,
                                              List<String> checklistDescriptions) {

        if (templateRepository.existsByTemplateName(templateName)) {
            throw new IllegalStateException(
                    "A maintenance template with name '" + templateName + "' already exists.");
        }

        List<AircraftModel> models = resolveModels(modelIds);

        MaintenanceTemplate template = new MaintenanceTemplate(
                templateName,
                templateType,
                defaultDurationHours,
                models,
                checklistDescriptions
        );

        return templateRepository.save(template);
    }

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<MaintenanceTemplate> getAllTemplates() {
        return templateRepository.findAll();
    }

    @Transactional(readOnly = true)
    public MaintenanceTemplate getTemplateById(Long id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Maintenance template with id '" + id + "' not found."));
    }

    @Transactional(readOnly = true)
    public List<MaintenanceTemplate> getTemplatesByType(TemplateType type) {
        return templateRepository.findByTemplateType(type);
    }

    /**
     * Returns all templates applicable to a given aircraft model.
     * Useful in the UI when the technician picks a template for a specific aircraft (US115B).
     */
    @Transactional(readOnly = true)
    public List<MaintenanceTemplate> getTemplatesForModel(Long modelId) {
        // Ensure the model exists before querying — gives a clear 404 instead of an empty list.
        aircraftModelRepository.findById(modelId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Aircraft model with id '" + modelId + "' not found."));

        return templateRepository.findByApplicableModel(modelId);
    }

    // -------------------------------------------------------------------------
    // Update — mirrors AircraftModelService.updateAircraftModel pattern
    // -------------------------------------------------------------------------

    /**
     * Updates a template's core details and/or its applicable models and checklist.
     * Uses optimistic locking: the client must supply the current {@code version}.
     *
     * @param id                  template primary key.
     * @param clientVersion       version field from the client's last read.
     * @param templateName        new name; null means "keep existing".
     * @param templateType        new type; null means "keep existing".
     * @param defaultDurationHours new duration; null means "keep existing".
     * @param modelIds            new list of model IDs; null means "keep existing".
     * @param checklistDescriptions new checklist; null means "keep existing".
     */
    @Transactional
    public MaintenanceTemplate updateTemplate(Long id,
                                              Long clientVersion,
                                              String templateName,
                                              TemplateType templateType,
                                              Double defaultDurationHours,
                                              List<Long> modelIds,
                                              List<String> checklistDescriptions) {

        MaintenanceTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Maintenance template with id '" + id + "' not found."));

        // Optimistic locking check — same pattern as AircraftModelService
        if (!template.getVersion().equals(clientVersion)) {
            throw new ObjectOptimisticLockingFailureException(MaintenanceTemplate.class, id);
        }

        // Partial update: only apply fields that were provided
        String resolvedName     = templateName        != null ? templateName        : template.getTemplateName();
        TemplateType resolvedType = templateType      != null ? templateType        : template.getTemplateType();
        Double resolvedDuration = defaultDurationHours != null ? defaultDurationHours : template.getDefaultDurationHours();

        // Name uniqueness check (only if the name is actually changing)
        if (!resolvedName.equals(template.getTemplateName())
                && templateRepository.existsByTemplateName(resolvedName)) {
            throw new IllegalStateException(
                    "A maintenance template with name '" + resolvedName + "' already exists.");
        }

        template.updateDetails(resolvedName, resolvedType, resolvedDuration);

        if (modelIds != null) {
            template.updateApplicableModels(resolveModels(modelIds));
        }

        if (checklistDescriptions != null) {
            template.replaceChecklist(checklistDescriptions);
        }

        return templateRepository.save(template);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Resolves a list of aircraft model IDs into domain objects.
     * Throws {@link ResourceNotFoundException} for the first ID that is not found.
     */
    private List<AircraftModel> resolveModels(List<Long> modelIds) {
        return modelIds.stream()
                .map(modelId -> aircraftModelRepository.findById(modelId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Aircraft model with id '" + modelId + "' not found.")))
                .toList();
    }
}