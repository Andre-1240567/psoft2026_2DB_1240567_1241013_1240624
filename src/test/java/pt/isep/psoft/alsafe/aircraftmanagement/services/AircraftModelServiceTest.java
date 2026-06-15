package pt.isep.psoft.alsafe.aircraftmanagement.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import pt.isep.psoft.alsafe.aircraftmanagement.api.CreateAircraftModelDTO;
import pt.isep.psoft.alsafe.aircraftmanagement.api.dto.UpdateAircraftModelDTO;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftModel;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.Manufacturer;
import pt.isep.psoft.alsafe.aircraftmanagement.repositories.AircraftModelRepository;
import pt.isep.psoft.alsafe.shared.exceptions.ResourceNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AircraftModelServiceTest {

    @Mock
    private AircraftModelRepository repository;

    @InjectMocks
    private AircraftModelService service;

    private AircraftModel mockModel;

    @BeforeEach
    void setUp() throws Exception {
        mockModel = new AircraftModel(Manufacturer.BOEING, "737 MAX", 180, 26000.0, 6500.0, 840.0);
        // Using reflection to set the version because it's managed by JPA
        java.lang.reflect.Field versionField = AircraftModel.class.getDeclaredField("version");
        versionField.setAccessible(true);
        versionField.set(mockModel, 1L);
    }

    @Test
    void ensureUpdateAircraftModelChangesValues() {
        when(repository.findById(1L)).thenReturn(Optional.of(mockModel));
        when(repository.save(any(AircraftModel.class))).thenAnswer(i -> i.getArgument(0));

        UpdateAircraftModelDTO dto = new UpdateAircraftModelDTO(200, 27000.0, 6600.0, 850.0, 1L);
        AircraftModel updated = service.updateAircraftModel(1L, dto);

        assertEquals(200, updated.getSeatingCapacity());
        assertEquals(27000.0, updated.getFuelCapacity());
        assertEquals(6600.0, updated.getMaxRange());
        assertEquals(850.0, updated.getCruisingSpeed());
        
        verify(repository, times(1)).save(any(AircraftModel.class));
    }

    @Test
    void ensureUpdateAircraftModelThrowsExceptionIfNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        UpdateAircraftModelDTO dto = new UpdateAircraftModelDTO(200, 27000.0, 6600.0, 850.0, 1L);

        assertThrows(ResourceNotFoundException.class, () -> {
            service.updateAircraftModel(1L, dto);
        });
        
        verify(repository, never()).save(any(AircraftModel.class));
    }

    @Test
    void ensureUpdateAircraftModelThrowsExceptionIfVersionMismatch() {
        when(repository.findById(1L)).thenReturn(Optional.of(mockModel));

        UpdateAircraftModelDTO dto = new UpdateAircraftModelDTO(200, 27000.0, 6600.0, 850.0, 2L); // Different version

        assertThrows(ObjectOptimisticLockingFailureException.class, () -> {
            service.updateAircraftModel(1L, dto);
        });
        
        verify(repository, never()).save(any(AircraftModel.class));
    }
}
