package pt.isep.psoft.alsafe.aircraftmanagement.api;

import org.junit.jupiter.api.Test;
import pt.isep.psoft.alsafe.aircraftmanagement.api.dto.TopAircraftModelDTO;
import pt.isep.psoft.alsafe.aircraftmanagement.api.dto.UpdateAircraftModelDTO;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.Aircraft;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftModel;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.Manufacturer;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class AircraftManagementDTOTest {

    @Test
    void testCreateAircraftDTO() {
        CreateAircraftDTO dto = new CreateAircraftDTO();
        dto.setRegistrationNumber("CS-TPA");
        dto.setModelName("A320");
        LocalDate date = LocalDate.now();
        dto.setManufacturingDate(date);
        dto.setActiveConfigurationName("Economy");
        dto.setActiveCapacity(150);

        assertEquals("CS-TPA", dto.getRegistrationNumber());
        assertEquals("A320", dto.getModelName());
        assertEquals(date, dto.getManufacturingDate());
        assertEquals("Economy", dto.getActiveConfigurationName());
        assertEquals(150, dto.getActiveCapacity());

        assertNotNull(dto.toString());
        assertEquals(dto, dto);
        assertNotNull(dto.hashCode());
    }

    @Test
    void testCreateAircraftModelDTO() {
        CreateAircraftModelDTO dto = new CreateAircraftModelDTO();
        dto.setManufacturer(Manufacturer.AIRBUS);
        dto.setModelName("A320");
        dto.setSeatingCapacity(160);
        dto.setFuelCapacity(24000.0);
        dto.setMaxRange(6300.0);
        dto.setCruisingSpeed(828.0);
        dto.setImage("img.png");

        assertEquals(Manufacturer.AIRBUS, dto.getManufacturer());
        assertEquals("A320", dto.getModelName());
        assertEquals(160, dto.getSeatingCapacity());
        assertEquals(24000.0, dto.getFuelCapacity());
        assertEquals(6300.0, dto.getMaxRange());
        assertEquals(828.0, dto.getCruisingSpeed());
        assertEquals("img.png", dto.getImage());

        assertNotNull(dto.toString());
        assertEquals(dto, dto);
    }

    @Test
    void testUpdateAircraftStatusDTO() {
        UpdateAircraftStatusDTO dto = new UpdateAircraftStatusDTO();
        dto.setStatus("AVAILABLE");
        dto.setVersion(1L);

        assertEquals("AVAILABLE", dto.getStatus());
        assertEquals(1L, dto.getVersion());

        assertNotNull(dto.toString());
        assertEquals(dto, dto);
    }

    @Test
    void testAircraftResponseDTO() {
        AircraftModel model = new AircraftModel(Manufacturer.AIRBUS, "A320neo", 160, 24000.0, 6300.0, 828.0);
        Aircraft aircraft = new Aircraft("CS-TPA", model, LocalDate.now(), "Economy");
        
        AircraftResponseDTO dto = new AircraftResponseDTO(aircraft);
        
        assertEquals("CS-TPA", dto.getRegistrationNumber());
        assertEquals("A320neo", dto.getModelName());
        assertEquals("Economy", dto.getActiveConfigurationName());
        
        dto.setRegistrationNumber("CS-TPB");
        assertEquals("CS-TPB", dto.getRegistrationNumber());
        
        assertNotNull(dto.toString());
    }

    @Test
    void testAircraftStatusOverviewDTO() {
        AircraftStatusOverviewDTO dto = new AircraftStatusOverviewDTO();
        assertEquals(0, dto.getTotalAvailable());
        assertTrue(dto.getAircraftsByStatus().isEmpty());

        AircraftModel model = new AircraftModel(Manufacturer.AIRBUS, "A320neo", 160, 24000.0, 6300.0, 828.0);
        Aircraft aircraft = new Aircraft("CS-TPA", model, LocalDate.now(), "Economy");
        AircraftResponseDTO responseDTO = new AircraftResponseDTO(aircraft);

        dto.addAircraftToStatus("AVAILABLE", responseDTO);

        assertEquals(1, dto.getTotalAvailable());
        assertEquals(1, dto.getAircraftsByStatus().get("AVAILABLE").size());

        dto.setTotalAvailable(2);
        assertEquals(2, dto.getTotalAvailable());
        
        assertNotNull(dto.toString());
    }

    @Test
    void testAircraftOperationalHoursDTO() {
        AircraftModel model = new AircraftModel(Manufacturer.AIRBUS, "A320neo", 160, 24000.0, 6300.0, 828.0);
        Aircraft aircraft = new Aircraft("CS-TPA", model, LocalDate.now(), "Economy");
        aircraft.addFlightHours(100.5);

        AircraftOperationalHoursDTO dto = new AircraftOperationalHoursDTO(aircraft);

        assertEquals("CS-TPA", dto.getRegistrationNumber());
        assertEquals(100.5, dto.getTotalOperationalHours());

        dto.setRegistrationNumber("CS-TPB");
        dto.setTotalOperationalHours(200.0);
        assertEquals("CS-TPB", dto.getRegistrationNumber());
        assertEquals(200.0, dto.getTotalOperationalHours());

        assertNotNull(dto.toString());
    }

    @Test
    void testUpdateAircraftModelDTO() {
        UpdateAircraftModelDTO dto = new UpdateAircraftModelDTO();
        dto.setSeatingCapacity(200);
        dto.setFuelCapacity(25000.0);
        dto.setMaxRange(6500.0);
        dto.setCruisingSpeed(850.0);
        dto.setVersion(1L);

        assertEquals(200, dto.getSeatingCapacity());
        assertEquals(25000.0, dto.getFuelCapacity());
        assertEquals(6500.0, dto.getMaxRange());
        assertEquals(850.0, dto.getCruisingSpeed());
        assertEquals(1L, dto.getVersion());

        UpdateAircraftModelDTO dto2 = new UpdateAircraftModelDTO(200, 25000.0, 6500.0, 850.0, 1L);
        assertEquals(dto, dto2);
        assertEquals(dto.hashCode(), dto2.hashCode());
        assertNotNull(dto.toString());
    }

    @Test
    void testTopAircraftModelDTO() {
        AircraftModel model = new AircraftModel(Manufacturer.AIRBUS, "A320neo", 160, 24000.0, 6300.0, 828.0);
        TopAircraftModelDTO dto = new TopAircraftModelDTO(model, 1000.0);

        assertEquals("A320neo", dto.getModelName());
        assertEquals("AIRBUS", dto.getManufacturer());
        assertEquals(160, dto.getSeatingCapacity());
        assertEquals(24000.0, dto.getFuelCapacity());
        assertEquals(6300.0, dto.getMaxRange());
        assertEquals(828.0, dto.getCruisingSpeed());
        assertEquals(1000.0, dto.getUtilizationValue());
    }
}
