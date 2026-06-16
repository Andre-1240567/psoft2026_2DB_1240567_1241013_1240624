package pt.isep.psoft.alsafe.aircraftmanagement.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import pt.isep.psoft.alsafe.aircraftmanagement.api.CreateAircraftDTO;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.*;
import pt.isep.psoft.alsafe.aircraftmanagement.repositories.AircraftModelRepository;
import pt.isep.psoft.alsafe.aircraftmanagement.repositories.AircraftRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AircraftServiceTest {

    @Mock
    private AircraftRepository aircraftRepository;

    @Mock
    private AircraftModelRepository aircraftModelRepository;

    @InjectMocks
    private AircraftService aircraftService;

    private AircraftModel mockModel;
    private Aircraft mockAircraft;

    @BeforeEach
    void setUp() {
        mockModel = new AircraftModel(Manufacturer.AIRBUS, "A320neo", 160, 24000.0, 6300.0, 828.0);
        mockAircraft = spy(new Aircraft("CS-TPA", mockModel, LocalDate.now(), "Economy"));

        lenient().when(mockAircraft.getVersion()).thenReturn(0L);
    }

    @Test
    void ensureAircraftCreationFailsWhenRegistrationAlreadyExists() {
        CreateAircraftDTO dto = new CreateAircraftDTO();
        dto.setRegistrationNumber("CS-TPA");

        when(aircraftRepository.existsById("CS-TPA")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            aircraftService.createAircraft(dto);
        });
        assertTrue(exception.getMessage().contains("already exists"));
    }

    @Test
    void ensureAircraftCreationFailsWhenModelDoesNotExist() {
        CreateAircraftDTO dto = new CreateAircraftDTO();
        dto.setRegistrationNumber("CS-TPA");
        dto.setModelName("GHOST-MODEL");

        when(aircraftRepository.existsById("CS-TPA")).thenReturn(false);

        when(aircraftModelRepository.findByModelName("GHOST-MODEL")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            aircraftService.createAircraft(dto);
        });
    }

    @Test
    void ensureOptimisticLockingFailsOnStatusUpdate() {
        when(aircraftRepository.findById("CS-TPA")).thenReturn(Optional.of(mockAircraft));

        assertThrows(ObjectOptimisticLockingFailureException.class, () -> {
            aircraftService.updateAircraftStatus("CS-TPA", "UNDER_MAINTENANCE", 99L);
        });
    }

    @Test
    void ensureGetAircraftStatusOverviewWorks() {
        when(aircraftRepository.findAll()).thenReturn(java.util.List.of(mockAircraft));
        
        pt.isep.psoft.alsafe.aircraftmanagement.api.AircraftStatusOverviewDTO overview = aircraftService.getAircraftStatusOverview();
        
        assertNotNull(overview);
        assertEquals(1, overview.getTotalAvailable());
        assertEquals(1, overview.getAircraftsByStatus().get("AVAILABLE").size());
        assertEquals("CS-TPA", overview.getAircraftsByStatus().get("AVAILABLE").get(0).getRegistrationNumber());
    }

    @Test
    void ensureGetAircraftsOperationalHoursWorksAndIsSorted() {
        Aircraft a1 = new Aircraft("CS-TPA", mockModel, LocalDate.now(), "Economy");
        a1.addFlightHours(100.0);
        Aircraft a2 = new Aircraft("CS-TPB", mockModel, LocalDate.now(), "Economy");
        a2.addFlightHours(200.0);
        
        when(aircraftRepository.findAll()).thenReturn(java.util.Arrays.asList(a1, a2));
        
        java.util.List<pt.isep.psoft.alsafe.aircraftmanagement.api.AircraftOperationalHoursDTO> result = aircraftService.getAircraftsOperationalHours();
        
        assertEquals(2, result.size());
        assertEquals("CS-TPB", result.get(0).getRegistrationNumber()); // 200.0 is greater
        assertEquals("CS-TPA", result.get(1).getRegistrationNumber());
    }
}