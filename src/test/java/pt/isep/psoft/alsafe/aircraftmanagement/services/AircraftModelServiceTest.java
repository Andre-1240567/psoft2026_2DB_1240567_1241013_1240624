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

    @Mock
    private pt.isep.psoft.alsafe.aircraftmanagement.repositories.AircraftRepository aircraftRepository;

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

    @Test
    void ensureCreateAircraftModelWorks() {
        CreateAircraftModelDTO dto = new CreateAircraftModelDTO();
        dto.setManufacturer(Manufacturer.BOEING);
        dto.setModelName("737 MAX");
        dto.setSeatingCapacity(180);
        dto.setFuelCapacity(26000.0);
        dto.setMaxRange(6500.0);
        dto.setCruisingSpeed(840.0);
        dto.setImage("path/to/image.png");

        when(repository.save(any(AircraftModel.class))).thenAnswer(i -> i.getArgument(0));

        AircraftModel created = service.createAircraftModel(dto);
        assertNotNull(created);
        assertEquals("737 MAX", created.getModelName());
        assertEquals("path/to/image.png", created.getImage());
    }

    @Test
    void ensureGetTop5MostUtilizedModelsWorks() {
        Object[] result1 = new Object[]{mockModel, 1000.0};
        when(aircraftRepository.findTopMostUtilizedAircraftModelsByFlightHours(any(org.springframework.data.domain.Pageable.class)))
            .thenReturn(java.util.Collections.singletonList(result1));

        java.util.List<pt.isep.psoft.alsafe.aircraftmanagement.api.dto.TopAircraftModelDTO> topModels = service.getTop5MostUtilizedModels("hours");

        assertEquals(1, topModels.size());
        assertEquals("737 MAX", topModels.get(0).getModelName());
        assertEquals(1000.0, topModels.get(0).getUtilizationValue());
    }

    @Test
    void ensureGetTop5MostUtilizedModelsWorksWithAssignments() {
        Object[] result1 = new Object[]{mockModel, 50.0};
        when(aircraftRepository.findTopMostUtilizedAircraftModelsByAssignments(any(org.springframework.data.domain.Pageable.class)))
            .thenReturn(java.util.Collections.singletonList(result1));

        java.util.List<pt.isep.psoft.alsafe.aircraftmanagement.api.dto.TopAircraftModelDTO> topModels = service.getTop5MostUtilizedModels("assignments");

        assertEquals(1, topModels.size());
        assertEquals("737 MAX", topModels.get(0).getModelName());
        assertEquals(50.0, topModels.get(0).getUtilizationValue());
    }

    @Test
    void ensureGetTop5MostUtilizedModelsThrowsOnInvalidCriteria() {
        assertThrows(IllegalArgumentException.class, () -> {
            service.getTop5MostUtilizedModels("invalid_criteria");
        });
    }

    @Test
    void ensureGetTop5MostUtilizedModelsHandlesNullValue() {
        Object[] result1 = new Object[]{mockModel, null};
        when(aircraftRepository.findTopMostUtilizedAircraftModelsByFlightHours(any(org.springframework.data.domain.Pageable.class)))
            .thenReturn(java.util.Collections.singletonList(result1));

        java.util.List<pt.isep.psoft.alsafe.aircraftmanagement.api.dto.TopAircraftModelDTO> topModels = service.getTop5MostUtilizedModels("hours");

        assertEquals(1, topModels.size());
        assertEquals(0.0, topModels.get(0).getUtilizationValue());
    }

    @Test
    void ensureGetAllAircraftModelsWorks() {
        when(repository.findAll()).thenReturn(java.util.Collections.singletonList(mockModel));
        
        java.util.List<AircraftModel> models = service.getAllAircraftModels();
        
        assertEquals(1, models.size());
        assertEquals("737 MAX", models.get(0).getModelName());
    }
}
