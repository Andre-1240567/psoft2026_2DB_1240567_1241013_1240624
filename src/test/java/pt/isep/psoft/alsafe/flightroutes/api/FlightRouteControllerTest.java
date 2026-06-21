package pt.isep.psoft.alsafe.flightroutes.api;

import org.springframework.hateoas.Link;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import pt.isep.psoft.alsafe.airportmanagement.domain.*;
import pt.isep.psoft.alsafe.flightroutes.api.dto.AlternativeRouteResponseDTO;
import pt.isep.psoft.alsafe.flightroutes.api.dto.CreateFlightRouteDTO;
import pt.isep.psoft.alsafe.flightroutes.api.dto.FlightRouteResponseDTO;
import pt.isep.psoft.alsafe.flightroutes.api.dto.RouteUtilizationDTO;
import pt.isep.psoft.alsafe.flightroutes.api.dto.UpdateFlightRouteDTO;
import pt.isep.psoft.alsafe.flightroutes.domain.FlightRoute;
import pt.isep.psoft.alsafe.flightroutes.domain.RouteHistory;
import pt.isep.psoft.alsafe.flightroutes.domain.RouteRequirement;
import pt.isep.psoft.alsafe.flightroutes.domain.RouteStatus;
import pt.isep.psoft.alsafe.flightroutes.services.FlightRouteService;
import pt.isep.psoft.alsafe.shared.exceptions.ResourceNotFoundException;
import pt.isep.psoft.alsafe.security.jwt.AuthTokenFilter;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FlightRouteController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(FlightRouteModelAssembler.class)
class FlightRouteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FlightRouteService flightRouteService;

    @MockBean
    private AuthTokenFilter authTokenFilter;

    private FlightRoute validRoute;

    @BeforeEach
    void setUp() {
        Airport origin = new Airport(
                new IATACode("OPO"), "Sá Carneiro",
                new Location("Norte", "Portugal", "Porto", new GPSCoordinates(41.2, -8.6)),
                new Timezone("UTC+01:00"));

        Airport destination = new Airport(
                new IATACode("MAD"), "Barajas",
                new Location("Madrid", "Espanha", "Madrid", new GPSCoordinates(40.4, -3.7)),
                new Timezone("UTC+02:00"));

                RouteRequirement req = new RouteRequirement(500.0, 150);
        validRoute = new FlightRoute("teste-id-123", origin, destination, 300.0, 60, req, "Tester");
    }

    @Test
    void ensureCreateRouteReturns201Created() throws Exception {
        CreateFlightRouteDTO dto = new CreateFlightRouteDTO();
        dto.setOriginIata("OPO");
        dto.setDestinationIata("MAD");
        dto.setDistance(300.0);
        dto.setEstimatedFlightTime(60);
        dto.setMinRangeRequired(500.0);
        dto.setMinCapacityRequired(150);

        FlightRouteResponseDTO mockedResponse = new FlightRouteResponseDTO(validRoute);
        mockedResponse.add(Link.of("/api/flight-routes/teste-id-123").withSelfRel());

        when(flightRouteService.createFlightRoute(any(CreateFlightRouteDTO.class)))
                .thenReturn(mockedResponse);

        mockMvc.perform(post("/api/flight-routes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.routeId").value("teste-id-123"))
                .andExpect(jsonPath("$._links").exists())
                .andExpect(jsonPath("$._links.self").exists());
    }

    @Test
    void ensureCreateRouteWithSameOriginAndDestinationReturns400() throws Exception {
        CreateFlightRouteDTO dto = new CreateFlightRouteDTO();
        dto.setOriginIata("OPO");
        dto.setDestinationIata("OPO");
        dto.setDistance(300.0);
        dto.setEstimatedFlightTime(60);
        dto.setMinRangeRequired(500.0);
        dto.setMinCapacityRequired(150);

        when(flightRouteService.createFlightRoute(any(CreateFlightRouteDTO.class)))
                .thenThrow(new IllegalArgumentException("The origin and destination cannot be the same airport."));

        mockMvc.perform(post("/api/flight-routes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ensureCreateRouteWithNegativeDistanceReturns400() throws Exception {
        CreateFlightRouteDTO invalidDto = new CreateFlightRouteDTO();
        invalidDto.setOriginIata("OPO");
        invalidDto.setDestinationIata("MAD");
        invalidDto.setDistance(-100.0);
        invalidDto.setEstimatedFlightTime(60);
        invalidDto.setMinRangeRequired(500.0);
        invalidDto.setMinCapacityRequired(150);

        mockMvc.perform(post("/api/flight-routes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ensureCreateRouteWithMissingOriginIataReturns400() throws Exception {
        CreateFlightRouteDTO invalidDto = new CreateFlightRouteDTO();
        invalidDto.setDestinationIata("MAD");
        invalidDto.setDistance(300.0);
        invalidDto.setEstimatedFlightTime(60);
        invalidDto.setMinRangeRequired(500.0);
        invalidDto.setMinCapacityRequired(150);

        mockMvc.perform(post("/api/flight-routes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ensureCreateRouteWithInvalidIataFormat_tooLong_returns400() throws Exception {
        CreateFlightRouteDTO invalidDto = new CreateFlightRouteDTO();
        invalidDto.setOriginIata("OPO1");
        invalidDto.setDestinationIata("MAD");
        invalidDto.setDistance(300.0);
        invalidDto.setEstimatedFlightTime(60);
        invalidDto.setMinRangeRequired(500.0);
        invalidDto.setMinCapacityRequired(150);

        mockMvc.perform(post("/api/flight-routes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ensureCreateRouteWithLowercaseIataReturns400() throws Exception {
        CreateFlightRouteDTO invalidDto = new CreateFlightRouteDTO();
        invalidDto.setOriginIata("opo");
        invalidDto.setDestinationIata("MAD");
        invalidDto.setDistance(300.0);
        invalidDto.setEstimatedFlightTime(60);
        invalidDto.setMinRangeRequired(500.0);
        invalidDto.setMinCapacityRequired(150);

        mockMvc.perform(post("/api/flight-routes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ensureCreateRouteWithUnknownAirportReturns404() throws Exception {
        CreateFlightRouteDTO dto = new CreateFlightRouteDTO();
        dto.setOriginIata("XXX");
        dto.setDestinationIata("MAD");
        dto.setDistance(300.0);
        dto.setEstimatedFlightTime(60);
        dto.setMinRangeRequired(500.0);
        dto.setMinCapacityRequired(150);

        when(flightRouteService.createFlightRoute(any(CreateFlightRouteDTO.class)))
                .thenThrow(new ResourceNotFoundException("Airport with code XXX not found."));

        mockMvc.perform(post("/api/flight-routes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void ensureGetRouteByIdReturns200OK() throws Exception {
        FlightRouteResponseDTO mockedResponse = new FlightRouteResponseDTO(validRoute);
        mockedResponse.add(Link.of("/api/flight-routes/teste-id-123").withSelfRel());

        when(flightRouteService.getRouteById("teste-id-123"))
                .thenReturn(mockedResponse);

        mockMvc.perform(get("/api/flight-routes/teste-id-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.routeId").value("teste-id-123"))
                .andExpect(jsonPath("$._links").exists());
    }

    @Test
    void ensureGetRouteByIdReturns404WhenNotFound() throws Exception {
        when(flightRouteService.getRouteById("nonexistent"))
                .thenThrow(new ResourceNotFoundException("Route not found."));

        mockMvc.perform(get("/api/flight-routes/nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void ensureGetAllRoutesReturns200WithPaginatedContent() throws Exception {
        Page<FlightRouteResponseDTO> page = new PageImpl<>(List.of(new FlightRouteResponseDTO(validRoute)));

        when(flightRouteService.searchRoutes(isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/flight-routes")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk());
    }

    @Test
    void ensureSearchByOriginIataReturns200() throws Exception {
        Page<FlightRouteResponseDTO> page = new PageImpl<>(List.of(new FlightRouteResponseDTO(validRoute)));

        when(flightRouteService.searchRoutes(eq("OPO"), isNull(), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/flight-routes")
                        .param("originIata", "OPO")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk());
    }

    @Test
    void ensureSearchByBothOriginAndDestinationReturns200() throws Exception {
        Page<FlightRouteResponseDTO> page = new PageImpl<>(List.of(new FlightRouteResponseDTO(validRoute)));

        when(flightRouteService.searchRoutes(eq("OPO"), eq("MAD"), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/flight-routes")
                        .param("originIata", "OPO")
                        .param("destinationIata", "MAD")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk());
    }


    @Test
    void ensureUpdateRouteReturns200OK() throws Exception {
        UpdateFlightRouteDTO dto = new UpdateFlightRouteDTO();
        dto.setDistance(350.0);
        dto.setEstimatedFlightTime(55);
        dto.setMinRangeRequired(600.0);
        dto.setMinCapacityRequired(180);
        dto.setVersion(0L);

        FlightRouteResponseDTO mockedResponse = new FlightRouteResponseDTO(validRoute);
        mockedResponse.add(Link.of("/api/flight-routes/teste-id-123").withSelfRel());

        when(flightRouteService.updateRoute(eq("teste-id-123"), any(UpdateFlightRouteDTO.class)))
                .thenReturn(mockedResponse);

        mockMvc.perform(put("/api/flight-routes/teste-id-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.routeId").value("teste-id-123"))
                .andExpect(jsonPath("$._links").exists());
    }

    @Test
    void ensureUpdateRouteReturns404WhenNotFound() throws Exception {
        UpdateFlightRouteDTO dto = new UpdateFlightRouteDTO();
        dto.setDistance(350.0);
        dto.setEstimatedFlightTime(55);
        dto.setMinRangeRequired(600.0);
        dto.setMinCapacityRequired(180);
        dto.setVersion(0L);

        when(flightRouteService.updateRoute(eq("nonexistent"), any(UpdateFlightRouteDTO.class)))
                .thenThrow(new ResourceNotFoundException("Route not found."));

        mockMvc.perform(put("/api/flight-routes/nonexistent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void ensureUpdateDeactivatedRouteReturns409Conflict() throws Exception {
        UpdateFlightRouteDTO dto = new UpdateFlightRouteDTO();
        dto.setDistance(350.0);
        dto.setEstimatedFlightTime(55);
        dto.setMinRangeRequired(600.0);
        dto.setMinCapacityRequired(180);
        dto.setVersion(0L);

        when(flightRouteService.updateRoute(eq("teste-id-123"), any(UpdateFlightRouteDTO.class)))
                .thenThrow(new IllegalStateException("Cannot update a deactivated route."));

        mockMvc.perform(put("/api/flight-routes/teste-id-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    void ensureUpdateRouteWithOutdatedVersionReturns409Conflict() throws Exception {
        UpdateFlightRouteDTO dto = new UpdateFlightRouteDTO();
        dto.setDistance(350.0);
        dto.setEstimatedFlightTime(55);
        dto.setMinRangeRequired(600.0);
        dto.setMinCapacityRequired(180);
        dto.setVersion(0L);

        when(flightRouteService.updateRoute(eq("teste-id-123"), any(UpdateFlightRouteDTO.class)))
                .thenThrow(new org.springframework.orm.ObjectOptimisticLockingFailureException(
                        FlightRoute.class, "teste-id-123"));

        mockMvc.perform(put("/api/flight-routes/teste-id-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    void ensureDeactivateRouteReturns200OK() throws Exception {
        FlightRouteResponseDTO mockedResponse = new FlightRouteResponseDTO(validRoute);
        mockedResponse.add(Link.of("/api/flight-routes/teste-id-123").withSelfRel());

        when(flightRouteService.deactivateRoute(eq("teste-id-123")))
                .thenReturn(mockedResponse);

        mockMvc.perform(patch("/api/flight-routes/teste-id-123/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.routeId").value("teste-id-123"))
                .andExpect(jsonPath("$._links").exists());
    }

    @Test
    void ensureDeactivateRouteReturns404WhenNotFound() throws Exception {
        when(flightRouteService.deactivateRoute(eq("nonexistent")))
                .thenThrow(new ResourceNotFoundException("Route not found."));

        mockMvc.perform(patch("/api/flight-routes/nonexistent/deactivate"))
                .andExpect(status().isNotFound());
    }

    @Test
    void ensureDeactivateAlreadyDeactivatedRouteReturns409Conflict() throws Exception {
        when(flightRouteService.deactivateRoute(eq("teste-id-123")))
                .thenThrow(new IllegalStateException("Route is already deactivated."));

        mockMvc.perform(patch("/api/flight-routes/teste-id-123/deactivate"))
                .andExpect(status().isConflict());
    }

    @Test
    void ensureGetActiveRoutesSortedByPopularityReturns200() throws Exception {
        Page<FlightRouteResponseDTO> page = new PageImpl<>(List.of(new FlightRouteResponseDTO(validRoute)));

        when(flightRouteService.getActiveRoutesSorted(any(), eq("popularity"), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/flight-routes")
                        .param("sortBy", "popularity")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk());
    }

    @Test
    void ensureGetTotalNetworkDistanceReturns200AndValidJson() throws Exception {
        when(flightRouteService.getTotalNetworkDistance()).thenReturn(15000.5);

        mockMvc.perform(get("/api/flight-routes/network/total-distance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDistance").value(15000.5));
    }

    @Test
    void ensureGetAlternativeRoutesReturns200() throws Exception {
        AlternativeRouteResponseDTO altDTO = new AlternativeRouteResponseDTO(List.of(), 500.0, 60);
        
        when(flightRouteService.findAlternativeRoutes("OPO", "MAD", "fewest-stops"))
                .thenReturn(List.of(altDTO));

        mockMvc.perform(get("/api/flight-routes/alternatives")
                        .param("originIata", "OPO")
                        .param("destinationIata", "MAD")
                        .param("algorithm", "fewest-stops"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].totalDistance").value(500.0));
    }

    @Test
    void ensureGetAlternativeRoutesWithSameOriginAndDestinationReturns400() throws Exception {
        mockMvc.perform(get("/api/flight-routes/alternatives")
                        .param("originIata", "OPO")
                        .param("destinationIata", "OPO") 
                        .param("algorithm", "fewest-stops"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ensureGetRouteHistoryReturns200OK() throws Exception {
        FlightRouteResponseDTO.RouteHistoryDTO historyDTO = new FlightRouteResponseDTO.RouteHistoryDTO(
                new RouteHistory("Route created", "admin")
        );

        when(flightRouteService.getRouteHistory("teste-id-123")).thenReturn(List.of(historyDTO));

        mockMvc.perform(get("/api/flight-routes/teste-id-123/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("Route created"));
    }

    @Test
    void ensureSearchRoutesWithoutSortReturns200() throws Exception {
        Page<FlightRouteResponseDTO> page = new PageImpl<>(List.of(new FlightRouteResponseDTO(validRoute)));

        when(flightRouteService.searchRoutes(eq("OPO"), isNull(), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/flight-routes")
                        .param("originIata", "OPO")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk());
    }

    @Test
    void ensureGetActiveRoutesSortedWithExplicitStatusReturns200() throws Exception {
        Page<FlightRouteResponseDTO> page = new PageImpl<>(List.of(new FlightRouteResponseDTO(validRoute)));

        when(flightRouteService.getActiveRoutesSorted(eq(RouteStatus.ACTIVE), eq("distance"), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/flight-routes")
                        .param("sortBy", "distance")
                        .param("status", "ACTIVE")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk());
     }

        @Test
        void ensureGetRouteUtilizationReportReturns200() throws Exception {
        RouteUtilizationDTO dto = new RouteUtilizationDTO("route-1", "OPO", "LIS", 5L);

        when(flightRouteService.getRouteUtilizationReport()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/flight-routes/reports/utilization"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].routeId").value("route-1"))
                .andExpect(jsonPath("$[0].originIata").value("OPO"))
                .andExpect(jsonPath("$[0].destinationIata").value("LIS"))
                .andExpect(jsonPath("$[0].totalFlights").value(5));
        }

        @Test
        void ensureGetRouteUtilizationReportReturnsEmptyList() throws Exception {
        when(flightRouteService.getRouteUtilizationReport()).thenReturn(List.of());

        mockMvc.perform(get("/api/flight-routes/reports/utilization"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
        }


        @Test
        void ensureExportGeoJsonReturns200WithCorrectContentType() throws Exception {
        when(flightRouteService.exportGeoJson())
                .thenReturn("{\"type\":\"FeatureCollection\",\"features\":[]}");

        mockMvc.perform(get("/api/flight-routes/export")
                        .param("format", "geojson"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/geo+json"))
                .andExpect(content().string("{\"type\":\"FeatureCollection\",\"features\":[]}"));
        }

        @Test
        void ensureExportKmlReturns200WithCorrectContentType() throws Exception {
        when(flightRouteService.exportKml())
                .thenReturn("<?xml version=\"1.0\" encoding=\"UTF-8\"?><kml></kml>");

        mockMvc.perform(get("/api/flight-routes/export")
                        .param("format", "kml"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/vnd.google-earth.kml+xml"))
                .andExpect(content().string("<?xml version=\"1.0\" encoding=\"UTF-8\"?><kml></kml>"));
        }

        @Test
        void ensureExportWithInvalidFormatReturns400() throws Exception {
        mockMvc.perform(get("/api/flight-routes/export")
                        .param("format", "csv"))
                .andExpect(status().isBadRequest());
        }

        @Test
        void ensureMalformedJsonReturns400() throws Exception {
        mockMvc.perform(post("/api/flight-routes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ this is not valid json }"))
                .andExpect(status().isBadRequest());
        }


}