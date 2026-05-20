package pt.isep.psoft.alsafe.flightroutes.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.isep.psoft.alsafe.flightroutes.domain.FlightRoute;
import pt.isep.psoft.alsafe.flightroutes.services.FlightRouteService;

// O @RestController avisa o Spring Boot que esta classe vai lidar com pedidos HTTP (web)
@RestController
@RequestMapping("/api/flight-routes") // Este é o URL base para todas as operações com rotas
public class FlightRouteController {

    private final FlightRouteService flightRouteService;

    // Injetamos o nosso "cérebro" (Service) aqui dentro
    public FlightRouteController(FlightRouteService flightRouteService) {
        this.flightRouteService = flightRouteService;
    }

    // O @PostMapping diz que este método vai responder quando alguém fizer um HTTP POST
    @PostMapping
    public ResponseEntity<?> createRoute(@RequestBody CreateFlightRouteDTO dto) {
        try {
            // Tentamos criar a rota chamando o Service
            FlightRoute newRoute = flightRouteService.createFlightRoute(dto);
            
            // Se correr tudo bem, devolvemos a rota criada com o status HTTP 201 (Created)
            return new ResponseEntity<>(newRoute, HttpStatus.CREATED);
            
        } catch (IllegalArgumentException e) {
            // O enunciado exige o tratamento apropriado de erros e status codes. 
            // Se o aeroporto não existir ou os dados forem inválidos (ex: capacidade <= 0), 
            // apanhamos a exceção e devolvemos um HTTP 400 (Bad Request) com a mensagem de erro.
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // O @PatchMapping com {id} permite-nos ler o ID a partir do URL (ex: /api/flight-routes/123/deactivate)
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateRoute(@PathVariable("id") String routeId) {
        try {
            FlightRoute updatedRoute = flightRouteService.deactivateRoute(routeId);
            return ResponseEntity.ok(updatedRoute); // Devolve status 200 OK
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // O @PutMapping recebe o ID no URL e os novos dados no "Body" (JSON)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateRoute(@PathVariable("id") String routeId, @RequestBody UpdateFlightRouteDTO dto) {
        try {
            FlightRoute updatedRoute = flightRouteService.updateRoute(routeId, dto);
            return ResponseEntity.ok(updatedRoute);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}