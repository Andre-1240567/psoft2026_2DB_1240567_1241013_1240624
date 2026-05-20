package pt.isep.psoft.alsafe.flightroutes.api;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.isep.psoft.alsafe.flightroutes.domain.FlightRoute;
import pt.isep.psoft.alsafe.flightroutes.services.FlightRouteService;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.MethodArgumentNotValidException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/flight-routes")
public class FlightRouteController {

    private final FlightRouteService flightRouteService;

    public FlightRouteController(FlightRouteService flightRouteService) {
        this.flightRouteService = flightRouteService;
    }

    // --- US110: Criar Rota ---
    @PostMapping
    public ResponseEntity<?> createRoute(@Valid @RequestBody CreateFlightRouteDTO dto) {
        try {
            FlightRoute newRoute = flightRouteService.createFlightRoute(dto);
            return new ResponseEntity<>(new FlightRouteResponseDTO(newRoute), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("erro", e.getMessage()));
        }
    }

    // --- US112: Desativar Rota ---
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateRoute(@PathVariable("id") String routeId) {
        try {
            FlightRoute updatedRoute = flightRouteService.deactivateRoute(routeId);
            return ResponseEntity.ok(new FlightRouteResponseDTO(updatedRoute));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("erro", e.getMessage()));
        }
    }

    // --- US112: Atualizar Rota ---
    @PutMapping("/{id}")
    public ResponseEntity<?> updateRoute(@PathVariable("id") String routeId, @Valid @RequestBody UpdateFlightRouteDTO dto) {
        try {
            FlightRoute updatedRoute = flightRouteService.updateRoute(routeId, dto);
            return ResponseEntity.ok(new FlightRouteResponseDTO(updatedRoute));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("erro", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("erro", e.getMessage())); 
        }
    }

    // --- US113: Ver detalhes por ID ---
    @GetMapping("/{id}")
    public ResponseEntity<?> getRouteById(@PathVariable("id") String routeId) {
        try {
            FlightRoute route = flightRouteService.getRouteById(routeId);
            return ResponseEntity.ok(new FlightRouteResponseDTO(route));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erro", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<Page<FlightRouteResponseDTO>> searchRoutes(
            @RequestParam(required = false) String originIata,
            @RequestParam(required = false) String destinationIata,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        // 1. Criamos as instruções de paginação a partir dos parâmetros do URL
        Pageable pageable = PageRequest.of(page, size);
        
        // 2. Pedimos a "fatia" (Página) ao Service
        Page<FlightRoute> routePage = flightRouteService.searchRoutes(originIata, destinationIata, pageable);
        
        // 3. A classe Page tem um método .map() brilhante que converte as rotas 
        // para os teus DTOs com HATEOAS, mantendo os dados da paginação intactos!
        Page<FlightRouteResponseDTO> responsePage = routePage.map(FlightRouteResponseDTO::new);

        return ResponseEntity.ok(responsePage);
    }

    // Este método "apanha" especificamente os erros gerados pelo @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        
        // Vai buscar todas as regras que falharam e extrai o campo e a nossa mensagem
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage()));
            
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
}