package pt.isep.psoft.alsafe.bootstrap;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import pt.isep.psoft.alsafe.airportmanagement.domain.Airport;
import pt.isep.psoft.alsafe.airportmanagement.domain.GPSCoordinates;
import pt.isep.psoft.alsafe.airportmanagement.domain.IATACode;
import pt.isep.psoft.alsafe.airportmanagement.domain.Location;
import pt.isep.psoft.alsafe.airportmanagement.repositories.AirportRepository;

import pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftModel;
import pt.isep.psoft.alsafe.aircraftmanagement.repositories.AircraftModelRepository;

@Component
public class Bootstrapper implements CommandLineRunner {

    private final AirportRepository airportRepository;
    private final AircraftModelRepository aircraftModelRepository;

    public Bootstrapper(AirportRepository airportRepository, AircraftModelRepository aircraftModelRepository) {
        this.airportRepository = airportRepository;
        this.aircraftModelRepository = aircraftModelRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Launching Bootstrapper...");
        bootstrapAirports();
        bootstrapFlightRoutes();
        bootstrapAircraftModels();
        bootstrapAircrafts();
        System.out.println("Bootstrapper deployed!");
    }

    private void bootstrapAirports() {
        if (airportRepository.count() == 0) {
            Location locOpo = new Location("Norte", "Portugal", "Porto", new GPSCoordinates(41.2481, -8.6814));
            airportRepository.save(new Airport(new IATACode("OPO"), "Sá Carneiro", locOpo));

            Location locLis = new Location("Centro", "Portugal", "Lisboa", new GPSCoordinates(38.7742, -9.1342));
            airportRepository.save(new Airport(new IATACode("LIS"), "Humberto Delgado", locLis));

            Location locMad = new Location("Madrid", "Espanha", "Madrid", new GPSCoordinates(40.4719, -3.5626));
            airportRepository.save(new Airport(new IATACode("MAD"), "Barajas", locMad));

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

    private void bootstrapFlightRoutes() {}

    private void bootstrapAircrafts() {}
}