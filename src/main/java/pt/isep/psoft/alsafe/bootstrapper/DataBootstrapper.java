package pt.isep.psoft.alsafe.bootstrapper;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftModel;
import pt.isep.psoft.alsafe.aircraftmanagement.repositories.AircraftModelRepository;

@Component
public class DataBootstrapper implements CommandLineRunner {

    private final AircraftModelRepository aircraftModelRepository;

    public DataBootstrapper(AircraftModelRepository aircraftModelRepository) {
        this.aircraftModelRepository = aircraftModelRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        if (aircraftModelRepository.count() == 0) {
            System.out.println("Bootstrapper: Starting the injection of fake data");

            AircraftModel boeing737 = new AircraftModel("Boeing", "737 MAX", 25941.0, 6570.0, 839.0);
            AircraftModel airbusA320 = new AircraftModel("Airbus", "A320neo", 26730.0, 6300.0, 828.0);
            AircraftModel embraerE195 = new AircraftModel("Embraer", "E195-E2", 13690.0, 4815.0, 870.0);

            aircraftModelRepository.save(boeing737);
            aircraftModelRepository.save(airbusA320);
            aircraftModelRepository.save(embraerE195);

            System.out.println("Bootstrapper: Airplane Models Successfully Molded");
        }
    }
}