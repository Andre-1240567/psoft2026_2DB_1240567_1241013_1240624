package pt.isep.psoft.alsafe.bootstrap;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import pt.isep.psoft.alsafe.airportmanagement.domain.Timezone;
import pt.isep.psoft.alsafe.airportmanagement.domain.Airport;
import pt.isep.psoft.alsafe.airportmanagement.domain.GPSCoordinates;
import pt.isep.psoft.alsafe.airportmanagement.domain.IATACode;
import pt.isep.psoft.alsafe.airportmanagement.domain.Location;
import pt.isep.psoft.alsafe.airportmanagement.repositories.AirportRepository;

import pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftModel;
import pt.isep.psoft.alsafe.aircraftmanagement.repositories.AircraftModelRepository;

import pt.isep.psoft.alsafe.flightroutes.domain.FlightRoute;
import pt.isep.psoft.alsafe.flightroutes.domain.RouteRequirement;
import pt.isep.psoft.alsafe.flightroutes.repositories.FlightRouteRepository;
import java.util.UUID;

@Component
public class Bootstrapper implements CommandLineRunner {

    private final AirportRepository airportRepository;
    private final AircraftModelRepository aircraftModelRepository;
    private final FlightRouteRepository flightRouteRepository;

    public Bootstrapper(AirportRepository airportRepository, 
                        AircraftModelRepository aircraftModelRepository,
                        FlightRouteRepository flightRouteRepository) { 
        this.airportRepository = airportRepository;
        this.aircraftModelRepository = aircraftModelRepository;
        this.flightRouteRepository = flightRouteRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Launching Bootstrapper...");
        bootstrapAirports();
        bootstrapAircraftModels();
        bootstrapFlightRoutes();
        bootstrapAircrafts();
        System.out.println("Bootstrapper deployed!");
    }

    private void bootstrapAirports() {
        if (airportRepository.count() == 0) {
            Location locOpo = new Location("Norte", "Portugal", "Porto", new GPSCoordinates(41.2481, -8.6814));
            airportRepository.save(new Airport(new IATACode("OPO"), "Sá Carneiro", locOpo, new Timezone("UTC+01:00")));

            Location locLis = new Location("Centro", "Portugal", "Lisboa", new GPSCoordinates(38.7742, -9.1342));
            airportRepository.save(new Airport(new IATACode("LIS"), "Humberto Delgado", locLis, new Timezone("UTC+01:00")));

            Location locMad = new Location("Madrid", "Espanha", "Madrid", new GPSCoordinates(40.4719, -3.5626));
            airportRepository.save(new Airport(new IATACode("MAD"), "Barajas", locMad,  new Timezone("UTC+02:00")));

            System.out.println(" -> Airports loaded.");
        }
    }

    private void bootstrapAircraftModels() {
        if (aircraftModelRepository.count() == 0) {
            aircraftModelRepository.save(new AircraftModel("Boeing", "737 MAX", 26000.0, 6500.0, 840.0));
            aircraftModelRepository.save(new AircraftModel("Airbus", "A320neo", 24000.0, 6300.0, 828.0));
            aircraftModelRepository.save(new AircraftModel("Boeing", "777X", 35000.0, 8000.0, 900.0));

            System.out.println(" -> Aircraft Models loaded.");
        }
    }

    private void bootstrapFlightRoutes() {
        if (flightRouteRepository.count() == 0) {
            Airport opo = airportRepository.findByIataCode_Code("OPO").orElseThrow();
            Airport lis = airportRepository.findByIataCode_Code("LIS").orElseThrow();
            Airport mad = airportRepository.findByIataCode_Code("MAD").orElseThrow();

            // Route 1: Porto -> Lisbon
            RouteRequirement req1 = new RouteRequirement(350.0, 100);
            FlightRoute route1 = new FlightRoute(UUID.randomUUID().toString(), opo, lis, 277.0, 45, req1, "Bootstrapper");
            flightRouteRepository.save(route1);

            // Route 2: Lisbon -> Madrid
            RouteRequirement req2 = new RouteRequirement(600.0, 150);
            FlightRoute route2 = new FlightRoute(UUID.randomUUID().toString(), lis, mad, 502.0, 80, req2, "Bootstrapper");
            flightRouteRepository.save(route2);

            // Route 3: Madrid -> Porto
            RouteRequirement req3 = new RouteRequirement(500.0, 120);
            FlightRoute route3 = new FlightRoute(UUID.randomUUID().toString(), mad, opo, 420.0, 70, req3, "Bootstrapper");
            flightRouteRepository.save(route3);

            System.out.println(" -> Flight Routes loaded.");
        }
    }

    private void bootstrapAircrafts() {}
}