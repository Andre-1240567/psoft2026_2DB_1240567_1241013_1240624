package pt.isep.psoft.alsafe.maintenancemanagement.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChecklistItem {

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private boolean completed;

    public ChecklistItem(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Checklist item description cannot be blank.");
        }
        this.description = description.trim();
        this.completed = false;
    }

    public ChecklistItem copy() {
        return new ChecklistItem(this.description);
    }

    public void markDone() {
        if (this.completed) {
            throw new IllegalStateException(
                    "Checklist item '" + description + "' is already marked as completed.");
        }
        this.completed = true;
    }
}