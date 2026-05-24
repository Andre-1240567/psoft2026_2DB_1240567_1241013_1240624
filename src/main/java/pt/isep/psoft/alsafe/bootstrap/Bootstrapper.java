package pt.isep.psoft.alsafe.bootstrap;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import pt.isep.psoft.alsafe.aircraftmanagement.domain.Aircraft;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftModel;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.Manufacturer;
import pt.isep.psoft.alsafe.aircraftmanagement.repositories.AircraftModelRepository;
import pt.isep.psoft.alsafe.aircraftmanagement.repositories.AircraftRepository;

import pt.isep.psoft.alsafe.airportmanagement.domain.Airport;
import pt.isep.psoft.alsafe.airportmanagement.domain.GPSCoordinates;
import pt.isep.psoft.alsafe.airportmanagement.domain.IATACode;
import pt.isep.psoft.alsafe.airportmanagement.domain.Location;
import pt.isep.psoft.alsafe.airportmanagement.domain.Status;
import pt.isep.psoft.alsafe.airportmanagement.domain.Timezone;
import pt.isep.psoft.alsafe.airportmanagement.repositories.AirportRepository;

import pt.isep.psoft.alsafe.flightroutes.domain.FlightRoute;
import pt.isep.psoft.alsafe.flightroutes.domain.RouteRequirement;
import pt.isep.psoft.alsafe.flightroutes.repositories.FlightRouteRepository;

import pt.isep.psoft.alsafe.security.domain.SystemUser;
import pt.isep.psoft.alsafe.security.repositories.SystemUserRepository;

import java.time.LocalDate;
import java.util.UUID;

// #1 — duplicate imports removed (CommandLineRunner and Component were imported twice in the original)
@Component
public class Bootstrapper implements CommandLineRunner {

    private final AirportRepository airportRepository;
    private final AircraftModelRepository aircraftModelRepository;
    private final AircraftRepository aircraftRepository;
    private final FlightRouteRepository flightRouteRepository;
    private final SystemUserRepository userRepository;              // #4

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Bootstrapper(AirportRepository airportRepository,
                        AircraftModelRepository aircraftModelRepository,
                        AircraftRepository aircraftRepository,
                        FlightRouteRepository flightRouteRepository,
                        SystemUserRepository userRepository) {      // #4
        this.airportRepository       = airportRepository;
        this.aircraftModelRepository = aircraftModelRepository;
        this.aircraftRepository      = aircraftRepository;
        this.flightRouteRepository   = flightRouteRepository;
        this.userRepository          = userRepository;              // #4
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Launching Bootstrapper...");
        bootstrapUsers();          // #4 — added: initialize user credentials (required by WP#0A spec)
        bootstrapAirports();
        bootstrapAircraftModels();
        bootstrapFlightRoutes();
        bootstrapAircrafts();
        System.out.println("Bootstrapper deployed!");
    }

    // #4 — Initialize system administrators and backoffice operators (required by WP#0A spec)
    // Passwords are hashed with BCrypt — never stored in plain text
    private void bootstrapUsers() {
        if (userRepository.count() == 0) {
            userRepository.save(new SystemUser(
                    "atcc",
                    passwordEncoder.encode("atcc123"),
                    "ATCC"
            ));
            userRepository.save(new SystemUser(
                    "operator",
                    passwordEncoder.encode("operator123"),
                    "BACKOFFICE_OPERATOR"
            ));
            userRepository.save(new SystemUser(
                    "admin",
                    passwordEncoder.encode("admin123"),
                    "ADMIN,BACKOFFICE_OPERATOR,ATCC"
            ));
            System.out.println(" -> Users loaded.");
        }
    }

    // --- Colleagues' code below — not modified ---

    private void bootstrapAirports() {
    if (airportRepository.count() == 0) {
        Location locOpo = new Location("Norte", "Portugal", "Porto", new GPSCoordinates(41.2481, -8.6814));
        Airport opo = new Airport(new IATACode("OPO"), "Sá Carneiro", locOpo, new Timezone("UTC+01:00"));
        opo.changeStatus(Status.OPERATIONAL);
        airportRepository.save(opo);

        Location locLis = new Location("Centro", "Portugal", "Lisboa", new GPSCoordinates(38.7742, -9.1342));
        Airport lis = new Airport(new IATACode("LIS"), "Humberto Delgado", locLis, new Timezone("UTC+01:00"));
        lis.changeStatus(Status.OPERATIONAL);
        airportRepository.save(lis);

        Location locMad = new Location("Madrid", "Espanha", "Madrid", new GPSCoordinates(40.4719, -3.5626));
        Airport mad = new Airport(new IATACode("MAD"), "Barajas", locMad, new Timezone("UTC+02:00"));
        mad.changeStatus(Status.OPERATIONAL);
        airportRepository.save(mad);

        System.out.println(" -> Airports loaded.");
    }
}

    private void bootstrapAircraftModels() {
        if (aircraftModelRepository.count() == 0) {
            aircraftModelRepository.save(new AircraftModel(Manufacturer.BOEING, "737 MAX", 180, 26000.0, 6500.0, 840.0));
            aircraftModelRepository.save(new AircraftModel(Manufacturer.AIRBUS, "A320neo", 160, 24000.0, 6300.0, 828.0));
            aircraftModelRepository.save(new AircraftModel(Manufacturer.BOEING, "777X", 400, 35000.0, 8000.0, 900.0));
            System.out.println(" -> Aircraft Models loaded.");
        }
    }

    private void bootstrapFlightRoutes() {
        if (flightRouteRepository.count() == 0) {
            Airport opo = airportRepository.findByIataCode_Code("OPO").orElseThrow();
            Airport lis = airportRepository.findByIataCode_Code("LIS").orElseThrow();
            Airport mad = airportRepository.findByIataCode_Code("MAD").orElseThrow();

            RouteRequirement req1 = new RouteRequirement(350.0, 100);
            FlightRoute route1 = new FlightRoute(UUID.randomUUID().toString(), opo, lis, 277.0, 45, req1, "Bootstrapper");
            flightRouteRepository.save(route1);

            RouteRequirement req2 = new RouteRequirement(600.0, 150);
            FlightRoute route2 = new FlightRoute(UUID.randomUUID().toString(), lis, mad, 502.0, 80, req2, "Bootstrapper");
            flightRouteRepository.save(route2);

            RouteRequirement req3 = new RouteRequirement(500.0, 120);
            FlightRoute route3 = new FlightRoute(UUID.randomUUID().toString(), mad, opo, 420.0, 70, req3, "Bootstrapper");
            flightRouteRepository.save(route3);

            System.out.println(" -> Flight Routes loaded.");
        }
    }

    private void bootstrapAircrafts() {
        if (aircraftRepository.count() == 0) {
            AircraftModel b737 = aircraftModelRepository.findByModelName("737 MAX").orElseThrow();
            AircraftModel a320 = aircraftModelRepository.findByModelName("A320neo").orElseThrow();

            Aircraft a1 = new Aircraft("CS-TPA", a320, LocalDate.of(2024, 1, 15), "Economy");
            Aircraft a2 = new Aircraft("CS-TPB", b737, LocalDate.of(2023, 6, 20), "Business");
            Aircraft a3 = new Aircraft("CS-TPC", a320, LocalDate.of(2020, 11, 5), "Economy");

            aircraftRepository.save(a1);
            aircraftRepository.save(a2);
            aircraftRepository.save(a3);

            System.out.println(" -> Aircrafts loaded.");
        }
    }
}