package pt.isep.psoft.alsafe.shared.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import pt.isep.psoft.alsafe.shared.exceptions.ResourceNotFoundException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Nested
    @DisplayName("handleValidationExceptions() — @Valid / Bean Validation failures")
    class ValidationExceptionTests {

        @Test
        @DisplayName("maps a single field error to a 400 with that field as the key")
        void mapsSingleFieldError() {
            MethodArgumentNotValidException ex = buildValidationException(
                    new FieldError("dto", "description", "Description is mandatory."));

            ResponseEntity<Map<String, String>> response = handler.handleValidationExceptions(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody())
                    .containsEntry("description", "Description is mandatory.");
        }

        @Test
        @DisplayName("maps multiple distinct field errors, one entry per field")
        void mapsMultipleDistinctFieldErrors() {
            MethodArgumentNotValidException ex = buildValidationException(
                    new FieldError("dto", "description", "Description is mandatory."),
                    new FieldError("dto", "startDate", "Start date is mandatory."));

            ResponseEntity<Map<String, String>> response = handler.handleValidationExceptions(ex);

            assertThat(response.getBody())
                    .hasSize(2)
                    .containsEntry("description", "Description is mandatory.")
                    .containsEntry("startDate", "Start date is mandatory.");
        }

        @Test
        @DisplayName("DOCUMENTS a real limitation: two errors on the SAME field overwrite each other")
        void multipleErrorsOnSameFieldOverwriteEachOther() {
            
            
            MethodArgumentNotValidException ex = buildValidationException(
                    new FieldError("dto", "actualDurationHours", "Actual duration is mandatory."),
                    new FieldError("dto", "actualDurationHours", "Actual duration must be strictly positive."));

            ResponseEntity<Map<String, String>> response = handler.handleValidationExceptions(ex);

            
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().get("actualDurationHours"))
                    .as("the Map-based implementation silently drops one of the two messages "
                            + "for the same field — this test documents that behaviour")
                    .isIn("Actual duration is mandatory.", "Actual duration must be strictly positive.");
        }

        private MethodArgumentNotValidException buildValidationException(FieldError... fieldErrors) {
            MethodArgumentNotValidException ex = Mockito.mock(MethodArgumentNotValidException.class);
            BindingResult bindingResult = Mockito.mock(BindingResult.class);
            Mockito.when(ex.getBindingResult()).thenReturn(bindingResult);
            Mockito.when(bindingResult.getAllErrors()).thenReturn(List.of(fieldErrors));
            return ex;
        }
    }

    @Nested
    @DisplayName("handleIllegalArgumentException() — 400")
    class IllegalArgumentExceptionTests {

        @Test
        @DisplayName("returns 400 with the exception message under 'error'")
        void returns400WithMessage() {
            IllegalArgumentException ex = new IllegalArgumentException("Estimated cost cannot be negative.");

            ResponseEntity<Map<String, String>> response = handler.handleIllegalArgumentException(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).containsEntry("error", "Estimated cost cannot be negative.");
        }
    }

    @Nested
    @DisplayName("handleResourceNotFoundException() — 404")
    class ResourceNotFoundExceptionTests {

        @Test
        @DisplayName("returns 404 with the exception message under 'error'")
        void returns404WithMessage() {
            ResourceNotFoundException ex = new ResourceNotFoundException(
                    "Maintenance record with id '999' not found.");

            ResponseEntity<Map<String, String>> response = handler.handleResourceNotFoundException(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody())
                    .containsEntry("error", "Maintenance record with id '999' not found.");
        }
    }

    @Nested
    @DisplayName("handleIllegalStateException() — 409")
    class IllegalStateExceptionTests {

        @Test
        @DisplayName("returns 409 with the exception message under 'error'")
        void returns409WithMessage() {
            IllegalStateException ex = new IllegalStateException(
                    "Cannot cancel a record in status 'COMPLETED'.");

            ResponseEntity<Map<String, String>> response = handler.handleIllegalStateException(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody())
                    .containsEntry("error", "Cannot cancel a record in status 'COMPLETED'.");
        }
    }

    @Nested
    @DisplayName("handleOptimisticLockingFailure() — 409")
    class OptimisticLockingFailureTests {

        @Test
        @DisplayName("returns 409 with a generic concurrency message, NOT the raw exception message")
        void returns409WithGenericMessage() {
            ObjectOptimisticLockingFailureException ex =
                    new ObjectOptimisticLockingFailureException(
                            pt.isep.psoft.alsafe.maintenancemanagement.domain.MaintenanceRecord.class, 1L);

            ResponseEntity<Map<String, String>> response = handler.handleOptimisticLockingFailure(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody().get("error"))
                    .as("the client-facing message should be a stable, friendly explanation, "
                            + "not Hibernate's internal exception text")
                    .contains("Concurrency conflict")
                    .contains("refresh your data");
        }
    }

    @Nested
    @DisplayName("handleMessageNotReadable() — 400")
    class MessageNotReadableTests {

        @Test
        @DisplayName("returns 400 with a generic malformed-request message")
        void returns400WithGenericMessage() {
            HttpMessageNotReadableException ex = Mockito.mock(HttpMessageNotReadableException.class);

            ResponseEntity<Map<String, String>> response = handler.handleMessageNotReadable(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().get("error"))
                    .contains("Malformed JSON");
        }
    }

    @Nested
    @DisplayName("handleBadCredentialsException() — 401")
    class BadCredentialsExceptionTests {

        @Test
        @DisplayName("returns 401 with a generic 'Invalid credentials' message, NOT leaking exception details")
        void returns401WithGenericMessage() {
            BadCredentialsException ex = new BadCredentialsException("Bad credentials for user 'admin'");

            ResponseEntity<Map<String, String>> response = handler.handleBadCredentialsException(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(response.getBody())
                    .as("must not leak the internal exception message (e.g. usernames) to the client")
                    .containsEntry("error", "Invalid credentials.");
        }
    }
}