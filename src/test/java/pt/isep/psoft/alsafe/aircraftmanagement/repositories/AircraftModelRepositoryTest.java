package pt.isep.psoft.alsafe.aircraftmanagement.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftModel;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.Manufacturer;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class AircraftModelRepositoryTest {

    @Autowired
    private AircraftModelRepository aircraftModelRepository;

    private AircraftModel boeingModel;
    private AircraftModel airbusModel;

    @BeforeEach
    void setUp() {
        boeingModel = new AircraftModel(Manufacturer.BOEING, "737 MAX", 180, 26000.0, 6500.0, 840.0);
        airbusModel = new AircraftModel(Manufacturer.AIRBUS, "A320neo", 160, 24000.0, 6300.0, 828.0);
        aircraftModelRepository.save(boeingModel);
        aircraftModelRepository.save(airbusModel);
    }

    @Test
    void testSaveAndRetrieve() {
        AircraftModel savedModel = aircraftModelRepository.save(new AircraftModel(Manufacturer.EMBRAER, "E190", 90, 10000.0, 3000.0, 800.0));
        assertNotNull(savedModel.getId());

        Optional<AircraftModel> retrieved = aircraftModelRepository.findById(savedModel.getId());
        assertTrue(retrieved.isPresent());
        assertEquals("E190", retrieved.get().getModelName());
    }

    @Test
    void testFindByManufacturer() {
        Iterable<AircraftModel> boeings = aircraftModelRepository.findByManufacturer(Manufacturer.BOEING);
        assertTrue(boeings.iterator().hasNext());
        assertEquals(Manufacturer.BOEING, boeings.iterator().next().getManufacturer());
    }

    @Test
    void testFindByModelName() {
        Optional<AircraftModel> model = aircraftModelRepository.findByModelName("A320neo");
        assertTrue(model.isPresent());
        assertEquals(Manufacturer.AIRBUS, model.get().getManufacturer());
    }

    @Test
    void testFindByModelNameNotFound() {
        Optional<AircraftModel> model = aircraftModelRepository.findByModelName("UNKNOWN_MODEL");
        assertTrue(model.isEmpty());
    }

    @Test
    void testDeleteModel() {
        aircraftModelRepository.delete(boeingModel);
        Optional<AircraftModel> retrieved = aircraftModelRepository.findById(boeingModel.getId());
        assertTrue(retrieved.isEmpty());
    }

    @Test
    void testCountModels() {
        assertEquals(2, aircraftModelRepository.count());
    }

    @Test
    void testUpdateModel() {
        boeingModel.updateSpecifications(200, 30000.0, 7000.0, 850.0);
        AircraftModel updated = aircraftModelRepository.save(boeingModel);
        assertEquals(200, updated.getSeatingCapacity());
        assertEquals(30000.0, updated.getFuelCapacity());
    }
}
