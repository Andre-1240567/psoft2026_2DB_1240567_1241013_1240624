package pt.isep.psoft.alsafe.maintenancemanagement.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MaintenanceTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(nullable = false, unique = true)
    private String templateName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TemplateType templateType;

    @Column(nullable = false)
    private Double defaultDurationHours;

    @ManyToMany
    @JoinTable(
            name = "template_applicable_models",
            joinColumns = @JoinColumn(name = "template_id"),
            inverseJoinColumns = @JoinColumn(name = "model_id")
    )
    private List<AircraftModel> applicableModels = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "template_checklist_items",
            joinColumns = @JoinColumn(name = "template_id")
    )
    @OrderColumn(name = "item_order")
    private List<ChecklistItem> checklist = new ArrayList<>();

    public MaintenanceTemplate(String templateName,
                               TemplateType templateType,
                               Double defaultDurationHours,
                               List<AircraftModel> applicableModels,
                               List<String> checklistItems) {

        if (templateName == null || templateName.trim().isEmpty()) {
            throw new IllegalArgumentException("Template name cannot be blank.");
        }
        if (templateType == null) {
            throw new IllegalArgumentException("Template type cannot be null.");
        }
        if (defaultDurationHours == null || defaultDurationHours <= 0) {
            throw new IllegalArgumentException("Default duration must be strictly positive.");
        }
        if (applicableModels == null || applicableModels.isEmpty()) {
            throw new IllegalArgumentException("At least one applicable aircraft model must be specified.");
        }
        if (checklistItems == null || checklistItems.isEmpty()) {
            throw new IllegalArgumentException("A template must have at least one checklist item.");
        }

        this.templateName = templateName.trim();
        this.templateType = templateType;
        this.defaultDurationHours = defaultDurationHours;
        this.applicableModels = new ArrayList<>(applicableModels);

        for (String desc : checklistItems) {
            this.checklist.add(new ChecklistItem(desc));
        }
    }

    public void updateDetails(String templateName,
                              TemplateType templateType,
                              Double defaultDurationHours) {

        if (templateName == null || templateName.trim().isEmpty()) {
            throw new IllegalArgumentException("Template name cannot be blank.");
        }
        if (templateType == null) {
            throw new IllegalArgumentException("Template type cannot be null.");
        }
        if (defaultDurationHours == null || defaultDurationHours <= 0) {
            throw new IllegalArgumentException("Default duration must be strictly positive.");
        }

        this.templateName = templateName.trim();
        this.templateType = templateType;
        this.defaultDurationHours = defaultDurationHours;
    }

    public void updateApplicableModels(List<AircraftModel> models) {
        if (models == null || models.isEmpty()) {
            throw new IllegalArgumentException("At least one applicable aircraft model must be specified.");
        }
        this.applicableModels = new ArrayList<>(models);
    }

    public void replaceChecklist(List<String> descriptions) {
        if (descriptions == null || descriptions.isEmpty()) {
            throw new IllegalArgumentException("A template must have at least one checklist item.");
        }
        this.checklist.clear();
        for (String desc : descriptions) {
            this.checklist.add(new ChecklistItem(desc));
        }
    }

    public List<ChecklistItem> getChecklist() {
        return Collections.unmodifiableList(checklist);
    }

    public List<ChecklistItem> cloneChecklist() {
        List<ChecklistItem> copy = new ArrayList<>();
        for (ChecklistItem item : this.checklist) {
            copy.add(item.copy());
        }
        return copy;
    }

    public boolean isApplicableTo(AircraftModel model) {
        if (model == null) return false;
        return applicableModels.stream()
                .anyMatch(m -> m.getModelName().equals(model.getModelName()));
    }
}