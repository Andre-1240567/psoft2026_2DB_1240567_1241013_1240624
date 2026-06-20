package pt.isep.psoft.alsafe.aircraftmanagement.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.Aircraft;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftModel;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftStatus;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.Manufacturer;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class AircraftRepositoryTest {

    @Autowired
    private AircraftRepository aircraftRepository;

    @Autowired
    private AircraftModelRepository aircraftModelRepository;

    private AircraftModel boeingModel;
    private AircraftModel airbusModel;
    private Aircraft aircraft1;
    private Aircraft aircraft2;

    @BeforeEach
    void setUp() {
        boeingModel = new AircraftModel(Manufacturer.BOEING, "737 MAX", 180, 26000.0, 6500.0, 840.0);
        airbusModel = new AircraftModel(Manufacturer.AIRBUS, "A320neo", 160, 24000.0, 6300.0, 828.0);
        aircraftModelRepository.save(boeingModel);
        aircraftModelRepository.save(airbusModel);

        aircraft1 = new Aircraft("CS-TPA", boeingModel, LocalDate.now(), "Economy");
        aircraft1.addFlightHours(100.0);
        aircraft1.addAssignment();
        aircraft1.addAssignment();

        aircraft2 = new Aircraft("CS-TPB", airbusModel, LocalDate.now(), "Economy");
        aircraft2.updateStatus(AircraftStatus.UNDER_MAINTENANCE);
        aircraft2.addFlightHours(50.0);
        aircraft2.addAssignment();

        aircraftRepository.save(aircraft1);
        aircraftRepository.save(aircraft2);
    }

    @Test
    void testSaveAndRetrieve() {
        Aircraft newAircraft = new Aircraft("CS-TPC", boeingModel, LocalDate.now(), "Business");
        aircraftRepository.save(newAircraft);

        Optional<Aircraft> retrieved = aircraftRepository.findById("CS-TPC");
        assertTrue(retrieved.isPresent());
        assertEquals("Business", retrieved.get().getActiveConfigurationName());
    }

    @Test
    void testFindByModel_ModelName() {
        List<Aircraft> result = aircraftRepository.findByModel_ModelName("737 MAX");
        assertEquals(1, result.size());
        assertEquals("CS-TPA", result.get(0).getRegistrationNumber());
    }

    @Test
    void testFindByStatus() {
        List<Aircraft> result = aircraftRepository.findByStatus(AircraftStatus.UNDER_MAINTENANCE);
        assertEquals(1, result.size());
        assertEquals("CS-TPB", result.get(0).getRegistrationNumber());
    }

    @Test
    void testFindByModel_ModelNameAndStatus() {
        List<Aircraft> result = aircraftRepository.findByModel_ModelNameAndStatus("A320neo", AircraftStatus.UNDER_MAINTENANCE);
        assertEquals(1, result.size());
        assertEquals("CS-TPB", result.get(0).getRegistrationNumber());
        
        List<Aircraft> emptyResult = aircraftRepository.findByModel_ModelNameAndStatus("A320neo", AircraftStatus.AVAILABLE);
        assertTrue(emptyResult.isEmpty());
    }

    @Test
    void testFindTopMostUtilizedAircraftModelsByFlightHours() {
        List<Object[]> result = aircraftRepository.findTopMostUtilizedAircraftModelsByFlightHours(PageRequest.of(0, 5));
        assertEquals(2, result.size());
        
        AircraftModel topModel = (AircraftModel) result.get(0)[0];
        Double topHours = (Double) result.get(0)[1];
        
        assertEquals("737 MAX", topModel.getModelName());
        assertEquals(100.0, topHours);
    }

    @Test
    void testFindTopMostUtilizedAircraftModelsByAssignments() {
        List<Object[]> result = aircraftRepository.findTopMostUtilizedAircraftModelsByAssignments(PageRequest.of(0, 5));
        assertEquals(2, result.size());
        
        AircraftModel topModel = (AircraftModel) result.get(0)[0];
        Long topAssignments = (Long) result.get(0)[1];
        
        assertEquals("737 MAX", topModel.getModelName());
        assertEquals(2L, topAssignments);
    }

    @Test
    void testDeleteAircraft() {
        aircraftRepository.delete(aircraft1);
        Optional<Aircraft> retrieved = aircraftRepository.findById("CS-TPA");
        assertTrue(retrieved.isEmpty());
    }

    @Test
    void testCountAircrafts() {
        assertEquals(2, aircraftRepository.count());
    }
}
