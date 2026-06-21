package pt.isep.psoft.alsafe.flightroutes.api;

import org.junit.jupiter.api.Test;
import pt.isep.psoft.alsafe.airportmanagement.domain.*;
import pt.isep.psoft.alsafe.flightroutes.api.dto.AlternativeRouteResponseDTO;
import pt.isep.psoft.alsafe.flightroutes.api.dto.CreateFlightRouteDTO;
import pt.isep.psoft.alsafe.flightroutes.api.dto.CreateScheduledFlightDTO;
import pt.isep.psoft.alsafe.flightroutes.api.dto.DeparturesBoardResponseDTO;
import pt.isep.psoft.alsafe.flightroutes.api.dto.FlightRouteResponseDTO;
import pt.isep.psoft.alsafe.flightroutes.api.dto.ScheduledFlightResponseDTO;
import pt.isep.psoft.alsafe.flightroutes.api.dto.UpdateFlightRouteDTO;
import pt.isep.psoft.alsafe.flightroutes.domain.FlightRoute;
import pt.isep.psoft.alsafe.flightroutes.domain.RouteRequirement;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FlightRoutesDtoTest {

    @Test
    void ensureCreateFlightRouteDtoWorks() {
        CreateFlightRouteDTO dto = new CreateFlightRouteDTO();
        dto.setOriginIata("OPO");
        dto.setDestinationIata("MAD");
        dto.setDistance(500.0);
        dto.setEstimatedFlightTime(60);
        dto.setMinRangeRequired(600.0);
        dto.setMinCapacityRequired(150);

        assertEquals("OPO", dto.getOriginIata());
        assertEquals("MAD", dto.getDestinationIata());
        assertEquals(500.0, dto.getDistance());
        assertEquals(60, dto.getEstimatedFlightTime());
        assertEquals(600.0, dto.getMinRangeRequired());
        assertEquals(150, dto.getMinCapacityRequired());
        assertNotNull(dto.toString());
        assertDoesNotThrow(dto::hashCode);
    }

    @Test
    void ensureCreateScheduledFlightDtoWorks() {
        CreateScheduledFlightDTO dto = new CreateScheduledFlightDTO();
        LocalDateTime now = LocalDateTime.now();
        
        dto.setRouteId("R123");
        dto.setAircraftRegistration("CS-TPA");
        dto.setDepartureTime(now);
        dto.setArrivalTime(now.plusHours(2));

        assertEquals("R123", dto.getRouteId());
        assertEquals("CS-TPA", dto.getAircraftRegistration());
        assertEquals(now, dto.getDepartureTime());
        assertEquals(now.plusHours(2), dto.getArrivalTime());
        assertNotNull(dto.toString());
        assertDoesNotThrow(dto::hashCode);
    }

    @Test
    void ensureDeparturesBoardResponseDtoWorks() {
        LocalDateTime now = LocalDateTime.now();
        DeparturesBoardResponseDTO dto = new DeparturesBoardResponseDTO("FL123", now, "MAD", "A320", "SCHEDULED");
        
        assertEquals("FL123", dto.getFlightNumber());
        assertEquals(now, dto.getScheduledDeparture());
        assertEquals("MAD", dto.getDestinationIata());
        assertEquals("A320", dto.getAircraftModel());
        assertEquals("SCHEDULED", dto.getStatus());

        DeparturesBoardResponseDTO emptyDto = new DeparturesBoardResponseDTO();
        emptyDto.setFlightNumber("FL999");
        assertEquals("FL999", emptyDto.getFlightNumber());
        assertNotNull(dto.toString());
        assertDoesNotThrow(dto::hashCode);
    }

    @Test
    void ensureUpdateFlightRouteDtoWorks() {
        UpdateFlightRouteDTO dto = new UpdateFlightRouteDTO();
        dto.setDistance(300.0);
        dto.setEstimatedFlightTime(45);
        dto.setMinRangeRequired(400.0);
        dto.setMinCapacityRequired(100);
        dto.setVersion(1L);

        assertEquals(300.0, dto.getDistance());
        assertEquals(45, dto.getEstimatedFlightTime());
        assertEquals(400.0, dto.getMinRangeRequired());
        assertEquals(100, dto.getMinCapacityRequired());
        assertEquals(1L, dto.getVersion());
        assertNotNull(dto.toString());
        assertDoesNotThrow(dto::hashCode);
    }

    @Test
    void ensureAlternativeRouteResponseDtoWorks() {
        AlternativeRouteResponseDTO dto = new AlternativeRouteResponseDTO(List.of(), 500.0, 60);
        
        assertTrue(dto.getRouteLegs().isEmpty());
        assertEquals(500.0, dto.getTotalDistance());
        assertEquals(60, dto.getTotalEstimatedFlightTime());
        assertEquals(0, dto.getNumberOfStops());
        assertNotNull(dto.toString());
        assertDoesNotThrow(dto::hashCode);
    }

    @Test
    void ensureFlightRouteResponseDtoWorks() {
        Airport origin = new Airport(new IATACode("OPO"), "OPO", new Location("R", "C", "C", new GPSCoordinates(0.0, 0.0)), new Timezone("UTC+00:00"));
        Airport dest = new Airport(new IATACode("MAD"), "MAD", new Location("R", "C", "C", new GPSCoordinates(0.0, 0.0)), new Timezone("UTC+00:00"));
        FlightRoute route = new FlightRoute("R123", origin, dest, 500.0, 60, new RouteRequirement(1000.0, 100), "admin");

        FlightRouteResponseDTO dto = new FlightRouteResponseDTO(route);
        
        assertEquals("R123", dto.getRouteId());
        assertEquals("OPO", dto.getOriginIataCode());
        assertEquals("MAD", dto.getDestinationIataCode());
        assertEquals(500.0, dto.getDistance());
        assertEquals(60, dto.getEstimatedFlightTime());
        assertEquals(1000.0, dto.getMinRangeRequired());
        assertEquals(100, dto.getMinCapacityRequired());
        assertNotNull(dto.getRouteStatus());
        assertEquals(1, dto.getHistory().size());
        
        assertNotNull(dto.getHistory().get(0).getChangeDate());
        assertEquals("Flight route created.", dto.getHistory().get(0).getDescription());
        assertEquals("admin", dto.getHistory().get(0).getAuthor());
    }

    @Test
    void ensureScheduledFlightResponseDtoWorks() {
        LocalDateTime now = LocalDateTime.now();
        ScheduledFlightResponseDTO dto = new ScheduledFlightResponseDTO("FL123", "R123", "CS-TPA", now, now.plusHours(2), "SCHEDULED");

        assertEquals("FL123", dto.getFlightNumber());
        assertEquals("R123", dto.getRouteId());
        assertEquals("CS-TPA", dto.getAircraftRegistration());
        assertEquals(now, dto.getScheduledDeparture());
        assertEquals(now.plusHours(2), dto.getScheduledArrival());
        assertEquals("SCHEDULED", dto.getStatus());
        assertNotNull(dto.toString());
        assertDoesNotThrow(dto::hashCode);
    }
}