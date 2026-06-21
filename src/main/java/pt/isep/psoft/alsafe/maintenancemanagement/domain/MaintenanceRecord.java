package pt.isep.psoft.alsafe.maintenancemanagement.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.Aircraft;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MaintenanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @ManyToOne(optional = false)
    @JoinColumn(name = "aircraft_registration", nullable = false)
    private Aircraft aircraft;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private MaintenanceTemplate template;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private Double expectedDurationHours;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaintenanceStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime completedAt;

    @Column(length = 2000)
    private String completionNotes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaintenanceComponent component;

    @Column
    private Double actualDurationHours;

    @Column
    private Double estimatedCost;

    @Column
    private Double actualCost;

    @Column
    private LocalDate nextMaintenanceDueDate;

    @Column
    private Double nextMaintenanceDueHours;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "maintenance_record_checklist",
            joinColumns = @JoinColumn(name = "record_id")
    )
    @OrderColumn(name = "item_order")
    private List<ChecklistItem> checklist = new ArrayList<>();

    public MaintenanceRecord(Aircraft aircraft,
                             MaintenanceTemplate template,
                             String description,
                             LocalDate startDate,
                             Double expectedDurationHours,
                             MaintenanceComponent component,
                             Double estimatedCost) {

        if (aircraft == null) {
            throw new IllegalArgumentException("Aircraft cannot be null.");
        }
        if (template == null) {
            throw new IllegalArgumentException("Maintenance template cannot be null.");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be blank.");
        }
        if (startDate == null) {
            throw new IllegalArgumentException("Start date cannot be null.");
        }
        if (component == null) {
            throw new IllegalArgumentException("Maintenance component cannot be null.");
        }

        if (!template.isApplicableTo(aircraft.getModel())) {
            throw new IllegalArgumentException(
                    "Template '" + template.getTemplateName() + "' is not applicable to aircraft model '"
                    + aircraft.getModel().getModelName() + "'.");
        }

        double resolvedDuration = (expectedDurationHours != null && expectedDurationHours > 0)
                ? expectedDurationHours
                : template.getDefaultDurationHours();

        if (estimatedCost != null && estimatedCost < 0) {
            throw new IllegalArgumentException("Estimated cost cannot be negative.");
        }

        this.aircraft = aircraft;
        this.template = template;
        this.description = description.trim();
        this.startDate = startDate;
        this.expectedDurationHours = resolvedDuration;
        this.component = component;
        this.estimatedCost = estimatedCost;
        this.status = MaintenanceStatus.PLANNED;
        this.createdAt = LocalDateTime.now();

        this.checklist = template.cloneChecklist();
    }

    public void startWork() {
        if (this.status != MaintenanceStatus.PLANNED) {
            throw new IllegalStateException(
                    "Cannot start work: record is already in status '" + this.status + "'.");
        }
        this.status = MaintenanceStatus.IN_PROGRESS;
    }

    public void markAsCompleted(String completionNotes,
                                Double actualDurationHours,
                                Double actualCost) {

        if (this.status != MaintenanceStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                    "Cannot complete a record that is not IN_PROGRESS. Current status: " + this.status + ".");
        }
        if (completionNotes == null || completionNotes.trim().isEmpty()) {
            throw new IllegalArgumentException("Completion notes cannot be blank.");
        }
        if (actualDurationHours == null || actualDurationHours <= 0) {
            throw new IllegalArgumentException("Actual duration must be strictly positive.");
        }
        if (actualCost != null && actualCost < 0) {
            throw new IllegalArgumentException("Actual cost cannot be negative.");
        }

        this.completionNotes = completionNotes.trim();
        this.actualDurationHours = actualDurationHours;
        this.actualCost = actualCost;
        this.status = MaintenanceStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void markAsCompleted(String completionNotes, Double actualDurationHours) {
        markAsCompleted(completionNotes, actualDurationHours, null);
    }

    public void cancel(String reason) {
        if (this.status == MaintenanceStatus.COMPLETED || this.status == MaintenanceStatus.CANCELED) {
            throw new IllegalStateException(
                    "Cannot cancel a record in status '" + this.status + "'.");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("A cancellation reason must be provided.");
        }
        this.completionNotes = "CANCELED — " + reason.trim();
        this.status = MaintenanceStatus.CANCELED;
        this.completedAt = LocalDateTime.now();
    }

    public void scheduleNextMaintenance(LocalDate dueDate, Double dueHours) {
        if (dueDate == null && dueHours == null) {
            throw new IllegalArgumentException(
                    "At least one of dueDate or dueHours must be provided for next maintenance scheduling.");
        }
        if (dueHours != null && dueHours <= 0) {
            throw new IllegalArgumentException("Due hours must be strictly positive.");
        }
        this.nextMaintenanceDueDate = dueDate;
        this.nextMaintenanceDueHours = dueHours;
    }

    public void adjustExpectedDuration(Double newDurationHours) {
        if (this.status != MaintenanceStatus.PLANNED) {
            throw new IllegalStateException(
                    "Expected duration can only be adjusted while the record is PLANNED.");
        }
        if (newDurationHours == null || newDurationHours <= 0) {
            throw new IllegalArgumentException("Expected duration must be strictly positive.");
        }
        this.expectedDurationHours = newDurationHours;
    }

    public boolean isOngoing() {
        return this.status == MaintenanceStatus.PLANNED
                || this.status == MaintenanceStatus.IN_PROGRESS;
    }

    public List<ChecklistItem> getChecklist() {
        return Collections.unmodifiableList(checklist);
    }

    public void completeChecklistItem(int index) {
        if (this.status == MaintenanceStatus.COMPLETED || this.status == MaintenanceStatus.CANCELED) {
            throw new IllegalStateException(
                    "Cannot update checklist on a record in status '" + this.status + "'.");
        }
        checklist.get(index).markDone();
    }
}