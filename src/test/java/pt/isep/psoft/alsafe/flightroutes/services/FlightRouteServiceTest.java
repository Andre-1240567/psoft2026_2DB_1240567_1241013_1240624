package pt.isep.psoft.alsafe.flightroutes.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.isep.psoft.alsafe.airports.domain.Airport;
import pt.isep.psoft.alsafe.airports.repositories.AirportRepository;
import pt.isep.psoft.alsafe.flightroutes.api.CreateFlightRouteDTO;
import pt.isep.psoft.alsafe.flightroutes.domain.FlightRoute;
import pt.isep.psoft.alsafe.flightroutes.repositories.FlightRouteRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Esta anotação liga os superpoderes do Mockito nesta classe
@ExtendWith(MockitoExtension.class)
class FlightRouteServiceTest {

    // 1. Criamos os "duplos de cinema" para os repositórios (não tocam na BD real)
    @Mock
    private FlightRouteRepository routeRepository;

    @Mock
    private AirportRepository airportRepository;

    // 2. Injetamos esses duplos diretamente dentro do nosso Service verdadeiro
    @InjectMocks
    private FlightRouteService flightRouteService;

    @Test
    void ensureRouteIsCreatedSuccessfully() {
        // Arrange (Preparar os dados de entrada)
        CreateFlightRouteDTO dto = new CreateFlightRouteDTO();
        dto.setOriginIata("OPO");
        dto.setDestinationIata("MAD");
        dto.setDistance(500.0);
        dto.setEstimatedFlightTime(60);
        dto.setMinRangeRequired(600.0);
        dto.setMinCapacityRequired(150);

        Airport origin = new Airport("OPO");
        Airport destination = new Airport("MAD");

        // TREINAR OS MOCKS:
        // "Quando o Service te perguntar pelo aeroporto OPO, devolve o objeto origin"
        when(airportRepository.findById("OPO")).thenReturn(Optional.of(origin));
        when(airportRepository.findById("MAD")).thenReturn(Optional.of(destination));
        
        // "Quando o Service te mandar gravar uma rota, devolve a própria rota que ele te deu"
        when(routeRepository.save(any(FlightRoute.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act (Agir)
        FlightRoute createdRoute = flightRouteService.createFlightRoute(dto);

        // Assert (Verificar se tudo correu bem)
        assertNotNull(createdRoute);
        assertEquals("OPO", createdRoute.getOrigin().getIataCode());
        assertEquals("MAD", createdRoute.getDestination().getIataCode());
        
        // O verify() garante que o Service efetivamente chamou a gravação da BD!
        verify(airportRepository, times(1)).findById("OPO");
        verify(routeRepository, times(1)).save(any(FlightRoute.class));
    }

    @Test
    void ensureExceptionIsThrownWhenOriginIsInvalid() {
        // Arrange
        CreateFlightRouteDTO dto = new CreateFlightRouteDTO();
        dto.setOriginIata("XXX"); // Um IATA que não existe!
        dto.setDestinationIata("MAD");

        // TREINAR O MOCK: 
        // "Quando procurarem por XXX, devolve VAZIO"
        when(airportRepository.findById("XXX")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            flightRouteService.createFlightRoute(dto);
        });

        // Verifica se a mensagem devolvida foi a correta
        assertEquals("Origin airport not found: XXX", exception.getMessage());
        
        // Garante que o Service parou a meio e NUNCA tentou gravar nada na BD corrompendo o sistema
        verify(routeRepository, never()).save(any());
    }
}