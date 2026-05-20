package pt.isep.psoft.alsafe.flightroutes.services;

import org.springframework.stereotype.Service;
import pt.isep.psoft.alsafe.airports.domain.Airport;
import pt.isep.psoft.alsafe.airports.repositories.AirportRepository;
import pt.isep.psoft.alsafe.flightroutes.api.CreateFlightRouteDTO;
import pt.isep.psoft.alsafe.flightroutes.api.UpdateFlightRouteDTO;
import pt.isep.psoft.alsafe.flightroutes.domain.FlightRoute;
import pt.isep.psoft.alsafe.flightroutes.domain.RouteRequirement;
import pt.isep.psoft.alsafe.flightroutes.repositories.FlightRouteRepository;

import java.util.UUID;

// O @Service diz ao Spring Boot que esta classe contém a lógica de negócio
@Service
public class FlightRouteService {

    private final FlightRouteRepository routeRepository;
    private final AirportRepository airportRepository;

    // Injetamos os repositórios para podermos falar com a base de dados
    public FlightRouteService(FlightRouteRepository routeRepository, AirportRepository airportRepository) {
        this.routeRepository = routeRepository;
        this.airportRepository = airportRepository;
    }

    public FlightRoute createFlightRoute(CreateFlightRouteDTO dto) {
        
        // 1. Verificar se o aeroporto de ORIGEM existe
        // O método findById vai à procura do código IATA (ex: "OPO"). Se não achar, rebenta com erro.
        Airport origin = airportRepository.findById(dto.getOriginIata())
                .orElseThrow(() -> new IllegalArgumentException("Origin airport not found: " + dto.getOriginIata()));

        // 2. Verificar se o aeroporto de DESTINO existe
        Airport destination = airportRepository.findById(dto.getDestinationIata())
                .orElseThrow(() -> new IllegalArgumentException("Destination airport not found: " + dto.getDestinationIata()));

        // 3. Criar o Value Object com os Requisitos
        RouteRequirement requirements = new RouteRequirement(dto.getMinRangeRequired(), dto.getMinCapacityRequired());

        // 4. Gerar o tal ID numérico/UUID automático que o stor confirmou no fórum
        String routeId = UUID.randomUUID().toString(); 

        // 5. Instanciar a nova Rota
        FlightRoute newRoute = new FlightRoute(routeId, origin, destination, 
                dto.getDistance(), dto.getEstimatedFlightTime(), requirements);

        // 6. Guardar na Base de Dados e devolver o resultado
        return routeRepository.save(newRoute);
        
    }

    public FlightRoute deactivateRoute(String routeId) {
        // 1. Procurar a rota pelo ID
        FlightRoute route = routeRepository.findById(routeId)
                .orElseThrow(() -> new IllegalArgumentException("Flight Route not found: " + routeId));

        // 2. Mandar a entidade mudar o seu estado e registar o histórico
        route.deactivate();

        // 3. Gravar as alterações (o Spring deteta o que mudou e faz o UPDATE na base de dados)
        return routeRepository.save(route);
    }

    public FlightRoute updateRoute(String routeId, UpdateFlightRouteDTO dto) {
        // 1. Procurar a rota
        FlightRoute route = routeRepository.findById(routeId)
                .orElseThrow(() -> new IllegalArgumentException("Flight Route not found: " + routeId));

        // 2. Criar os novos requisitos
        RouteRequirement newRequirements = new RouteRequirement(dto.getMinRangeRequired(), dto.getMinCapacityRequired());

        // 3. Atualizar a entidade
        route.updateDetails(dto.getDistance(), dto.getEstimatedFlightTime(), newRequirements);

        // 4. Gravar
        return routeRepository.save(route);
    }
}