package pt.isep.psoft.alsafe.maintenancemanagement.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChecklistItemTest {

    @Nested
    @DisplayName("Constructor validation")
    class ConstructorTests {

        @Test
        @DisplayName("creates a valid item, not completed, with trimmed description")
        void createsValidItem() {
            ChecklistItem item = new ChecklistItem("  Check oil level  ");

            assertThat(item.getDescription()).isEqualTo("Check oil level");
            assertThat(item.isCompleted()).isFalse();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "   ", "\t"})
        @DisplayName("rejects null, empty or blank descriptions")
        void rejectsBlankDescription(String description) {
            assertThatThrownBy(() -> new ChecklistItem(description))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("blank");
        }
    }

    @Nested
    @DisplayName("copy()")
    class CopyTests {

        @Test
        @DisplayName("produces an independent, not-completed clone")
        void copyProducesIndependentClone() {
            ChecklistItem original = new ChecklistItem("Inspect landing gear");
            original.markDone();

            ChecklistItem copy = original.copy();

            assertThat(copy.getDescription()).isEqualTo(original.getDescription());
            assertThat(copy.isCompleted())
                    .as("a fresh copy must always start as not completed, even if the source was done")
                    .isFalse();
            assertThat(copy).isNotSameAs(original);
        }
    }

    @Nested
    @DisplayName("markDone()")
    class MarkDoneTests {

        @Test
        @DisplayName("marks an incomplete item as completed")
        void marksIncompleteItemAsDone() {
            ChecklistItem item = new ChecklistItem("Torque check bolts");

            item.markDone();

            assertThat(item.isCompleted()).isTrue();
        }

        @Test
        @DisplayName("throws IllegalStateException when already completed (idempotency guard)")
        void throwsWhenAlreadyCompleted() {
            ChecklistItem item = new ChecklistItem("Replace filter");
            item.markDone();

            assertThatThrownBy(item::markDone)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already marked as completed");
        }
    }
}