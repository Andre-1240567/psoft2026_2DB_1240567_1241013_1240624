package pt.isep.psoft.alsafe.flightroutes.domain;

import org.junit.jupiter.api.Test;
import pt.isep.psoft.alsafe.airports.domain.Airport;

import static org.junit.jupiter.api.Assertions.*;

class FlightRouteTest {

    @Test
    void ensureOriginAndDestinationCannotBeTheSame() {
        // Arrange
        Airport porto = new Airport("OPO");
        RouteRequirement req = new RouteRequirement(1500.0, 100);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            // Tentamos criar uma rota do Porto para... o Porto!
            new FlightRoute("route-123", porto, porto, 0.0, 0, req, "atcc_jose");
        });

        assertEquals("A origem e o destino não podem ser o mesmo aeroporto.", exception.getMessage());
    }

    @Test
    void ensureDeactivatedRouteCannotBeUpdated() {
        // Arrange
        Airport origin = new Airport("OPO");
        Airport destination = new Airport("LIS");
        RouteRequirement req = new RouteRequirement(1500.0, 100);
        FlightRoute route = new FlightRoute("route-123", origin, destination, 300.0, 45, req, "atcc_jose");

        // Act
        route.deactivate("atcc_jose"); // Desativamos a rota primeiro

        // Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            // Tentamos atualizar uma rota que já está morta
            route.updateDetails(350.0, 50, req, "atcc_jose");
        });

        assertEquals("Não podes atualizar uma rota desativada.", exception.getMessage());
    }
}