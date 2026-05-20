package pt.isep.psoft.alsafe.bootstrap;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import pt.isep.psoft.alsafe.airportmanagement.domain.Airport;
import pt.isep.psoft.alsafe.airportmanagement.domain.GPSCoordinates;
import pt.isep.psoft.alsafe.airportmanagement.domain.IATACode;
import pt.isep.psoft.alsafe.airportmanagement.domain.Location;
import pt.isep.psoft.alsafe.airportmanagement.repositories.AirportRepository;

@Component
public class Bootstrapper implements CommandLineRunner {

    private final AirportRepository airportRepository;
    
    public Bootstrapper(AirportRepository airportRepository) {
        this.airportRepository = airportRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Launching Bootstrapper...");
        bootstrapAirports();
        bootstrapFlightRoutes();
        bootstrapAircrafts();
        System.out.println("Bootstraper deployed!");
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

    private void bootstrapFlightRoutes() {}

    private void bootstrapAircrafts() {}
}