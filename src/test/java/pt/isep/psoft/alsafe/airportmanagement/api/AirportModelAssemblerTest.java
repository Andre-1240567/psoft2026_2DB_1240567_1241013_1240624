package pt.isep.psoft.alsafe.airportmanagement.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.psoft.alsafe.airportmanagement.api.dto.AirportViewDTO;
import pt.isep.psoft.alsafe.airportmanagement.domain.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AirportModelAssemblerTest {

    private AirportModelAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new AirportModelAssembler();
    }

    @Test
    void testToModelAddsHateoasLinks() {
        IATACode iata = new IATACode("OPO");
        GPSCoordinates gps = new GPSCoordinates(41.2356, -8.6780);
        Location location = new Location("Europe", "Portugal", "Porto", gps);
        Timezone tz = new Timezone("UTC+01:00");
        
        Airport airport = new Airport(iata, "Francisco Sa Carneiro", location, tz);

        AirportViewDTO dto = assembler.toModel(airport);

        assertNotNull(dto);
        assertTrue(dto.hasLink("self"), "Deve conter o link 'self'");
        assertTrue(dto.hasLink("routes"), "Deve conter o link 'routes'");
        
        assertTrue(dto.getLink("self").get().getHref().contains("/api/airports/OPO"));
        assertTrue(dto.getLink("routes").get().getHref().contains("/api/airports/OPO/routes"));
    }
}