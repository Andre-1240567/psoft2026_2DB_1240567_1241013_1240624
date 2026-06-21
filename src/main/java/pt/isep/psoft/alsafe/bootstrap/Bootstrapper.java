package pt.isep.psoft.alsafe.bootstrap;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import pt.isep.psoft.alsafe.aircraftmanagement.domain.Aircraft;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftModel;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.Manufacturer;
import pt.isep.psoft.alsafe.aircraftmanagement.repositories.AircraftModelRepository;
import pt.isep.psoft.alsafe.aircraftmanagement.repositories.AircraftRepository;

import pt.isep.psoft.alsafe.airportmanagement.domain.Airport;
import pt.isep.psoft.alsafe.airportmanagement.domain.GPSCoordinates;
import pt.isep.psoft.alsafe.airportmanagement.domain.IATACode;
import pt.isep.psoft.alsafe.airportmanagement.domain.Location;
import pt.isep.psoft.alsafe.airportmanagement.domain.Timezone;
import pt.isep.psoft.alsafe.airportmanagement.repositories.AirportRepository;

import pt.isep.psoft.alsafe.flightroutes.domain.FlightRoute;
import pt.isep.psoft.alsafe.flightroutes.domain.RouteRequirement;
import pt.isep.psoft.alsafe.flightroutes.domain.ScheduledFlight;
import pt.isep.psoft.alsafe.flightroutes.repositories.FlightRouteRepository;
import pt.isep.psoft.alsafe.flightroutes.repositories.ScheduledFlightRepository;

import pt.isep.psoft.alsafe.maintenancemanagement.domain.MaintenanceComponent;
import pt.isep.psoft.alsafe.maintenancemanagement.domain.MaintenanceTemplate;
import pt.isep.psoft.alsafe.maintenancemanagement.domain.TemplateType;
import pt.isep.psoft.alsafe.maintenancemanagement.repositories.MaintenanceTemplateRepository;
import pt.isep.psoft.alsafe.maintenancemanagement.services.MaintenanceRecordService;

import pt.isep.psoft.alsafe.security.domain.SystemUser;
import pt.isep.psoft.alsafe.security.repositories.SystemUserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class Bootstrapper implements CommandLineRunner {

    private final AirportRepository airportRepository;
    private final AircraftModelRepository aircraftModelRepository;
    private final AircraftRepository aircraftRepository;
    private final FlightRouteRepository flightRouteRepository;
    private final ScheduledFlightRepository scheduledFlightRepository;
    private final SystemUserRepository userRepository;
    private final MaintenanceTemplateRepository maintenanceTemplateRepository;
    private final MaintenanceRecordService maintenanceRecordService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Bootstrapper(AirportRepository airportRepository,
                        AircraftModelRepository aircraftModelRepository,
                        AircraftRepository aircraftRepository,
                        FlightRouteRepository flightRouteRepository,
                        ScheduledFlightRepository scheduledFlightRepository,
                        SystemUserRepository userRepository,
                        MaintenanceTemplateRepository maintenanceTemplateRepository,
                        MaintenanceRecordService maintenanceRecordService) {
        this.airportRepository        = airportRepository;
        this.aircraftModelRepository  = aircraftModelRepository;
        this.aircraftRepository       = aircraftRepository;
        this.flightRouteRepository    = flightRouteRepository;
        this.scheduledFlightRepository = scheduledFlightRepository;
        this.userRepository           = userRepository;
        this.maintenanceTemplateRepository = maintenanceTemplateRepository;
        this.maintenanceRecordService      = maintenanceRecordService;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println("Launching Bootstrapper...");
        bootstrapUsers();
        bootstrapAirports();
        bootstrapAircraftModels();
        bootstrapFlightRoutes();
        bootstrapAircrafts();
        bootstrapScheduledFlights();
        bootstrapMaintenanceTemplates();
        bootstrapMaintenanceRecords();
        System.out.println("Bootstrapper deployed!");
    }

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
                    "technician",
                    passwordEncoder.encode("technician123"),
                    "MAINTENANCE_TECHNICIAN"
            ));
            userRepository.save(new SystemUser(
                    "supervisor",
                    passwordEncoder.encode("supervisor123"),
                    "MAINTENANCE_SUPERVISOR"
            ));
            userRepository.save(new SystemUser(
                    "admin",
                    passwordEncoder.encode("admin123"),
                    "ADMIN,BACKOFFICE_OPERATOR,ATCC,MAINTENANCE_TECHNICIAN,MAINTENANCE_SUPERVISOR"
            ));
            System.out.println(" -> Users loaded.");
        }
    }

    private void bootstrapAirports() {
        if (airportRepository.count() == 0) {
            Location locOpo = new Location("Norte", "Portugal", "Porto", new GPSCoordinates(41.2481, -8.6814));
            Airport opo = new Airport(new IATACode("OPO"), "Sá Carneiro", locOpo, new Timezone("UTC+01:00"));
            airportRepository.save(opo);

            Location locLis = new Location("Centro", "Portugal", "Lisboa", new GPSCoordinates(38.7742, -9.1342));
            Airport lis = new Airport(new IATACode("LIS"), "Humberto Delgado", locLis, new Timezone("UTC+01:00"));
            airportRepository.save(lis);

            Location locMad = new Location("Madrid", "Espanha", "Madrid", new GPSCoordinates(40.4719, -3.5626));
            Airport mad = new Airport(new IATACode("MAD"), "Barajas", locMad, new Timezone("UTC+02:00"));
            airportRepository.save(mad);

            System.out.println(" -> Airports loaded.");
        }
    }

    private void bootstrapAircraftModels() {
        if (aircraftModelRepository.count() == 0) {
            aircraftModelRepository.save(new AircraftModel(Manufacturer.BOEING, "737 MAX", 180, 26000.0, 6500.0, 840.0));
            aircraftModelRepository.save(new AircraftModel(Manufacturer.AIRBUS, "A320neo", 160, 24000.0, 6300.0, 828.0));
            aircraftModelRepository.save(new AircraftModel(Manufacturer.BOEING, "777X", 400, 35000.0, 8000.0, 900.0));
            aircraftModelRepository.save(new AircraftModel(Manufacturer.AIRBUS, "A350", 350, 140000.0, 15000.0, 903.0));
            aircraftModelRepository.save(new AircraftModel(Manufacturer.EMBRAER, "E195-E2", 120, 14000.0, 4800.0, 870.0));
            aircraftModelRepository.save(new AircraftModel(Manufacturer.ATR, "ATR 72-600", 72, 5000.0, 1500.0, 510.0));
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
            AircraftModel b777 = aircraftModelRepository.findByModelName("777X").orElseThrow();
            AircraftModel a350 = aircraftModelRepository.findByModelName("A350").orElseThrow();
            AircraftModel e195 = aircraftModelRepository.findByModelName("E195-E2").orElseThrow();
            AircraftModel atr72 = aircraftModelRepository.findByModelName("ATR 72-600").orElseThrow();

            Aircraft a1 = new Aircraft("CS-TPA", a320, LocalDate.of(2024, 1, 15), "Economy");
            a1.addFlightHours(1500.0);
            a1.addAssignment(); a1.addAssignment();

            Aircraft a2 = new Aircraft("CS-TPB", b737, LocalDate.of(2023, 6, 20), "Business");
            a2.addFlightHours(3000.0);
            for(int i=0; i<5; i++) a2.addAssignment();

            Aircraft a3 = new Aircraft("CS-TPC", a320, LocalDate.of(2020, 11, 5), "Economy");
            a3.addFlightHours(5000.0);
            for(int i=0; i<10; i++) a3.addAssignment();

            Aircraft a4 = new Aircraft("CS-TPD", b777, LocalDate.of(2022, 5, 10), "First Class");
            a4.addFlightHours(8000.0);
            for(int i=0; i<15; i++) a4.addAssignment();

            Aircraft a5 = new Aircraft("CS-TPE", a350, LocalDate.of(2021, 3, 10), "Business");
            a5.addFlightHours(6000.0);
            for(int i=0; i<8; i++) a5.addAssignment();
            a5.updateStatus(pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftStatus.IN_FLIGHT);

            Aircraft a6 = new Aircraft("CS-TPF", e195, LocalDate.of(2019, 8, 20), "Economy");
            a6.addFlightHours(9000.0);
            for(int i=0; i<20; i++) a6.addAssignment();

            Aircraft a7 = new Aircraft("CS-TPG", atr72, LocalDate.of(2018, 12, 1), "Economy");
            a7.addFlightHours(12000.0);
            for(int i=0; i<30; i++) a7.addAssignment();
            a7.updateStatus(pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftStatus.INACTIVE);

            aircraftRepository.save(a1);
            aircraftRepository.save(a2);
            aircraftRepository.save(a3);
            aircraftRepository.save(a4);
            aircraftRepository.save(a5);
            aircraftRepository.save(a6);
            aircraftRepository.save(a7);

            Airport opo = airportRepository.findByIataCode_Code("OPO").orElseThrow();
            Airport lis = airportRepository.findByIataCode_Code("LIS").orElseThrow();
            Airport mad = airportRepository.findByIataCode_Code("MAD").orElseThrow();

            opo.addCertification("A320neo");
            opo.addCertification("737 MAX");
            lis.addCertification("A320neo");
            lis.addCertification("737 MAX");
            mad.addCertification("A320neo");
            mad.addCertification("737 MAX");

            airportRepository.save(opo);
            airportRepository.save(lis);
            airportRepository.save(mad);

            System.out.println(" -> Aircrafts loaded.");
            System.out.println(" -> Airport certifications loaded.");
        }
    }


    private void bootstrapScheduledFlights() {
        if (scheduledFlightRepository.count() == 0) {
            Aircraft csTPa = aircraftRepository.findById("CS-TPA").orElseThrow();
            Aircraft csTPb = aircraftRepository.findById("CS-TPB").orElseThrow();
            Aircraft csTPc = aircraftRepository.findById("CS-TPC").orElseThrow();

            FlightRoute opolis = flightRouteRepository.findAll().stream()
                    .filter(r -> r.getOrigin().getIataCode().getCode().equals("OPO")
                              && r.getDestination().getIataCode().getCode().equals("LIS"))
                    .findFirst().orElseThrow();

            FlightRoute lismad = flightRouteRepository.findAll().stream()
                    .filter(r -> r.getOrigin().getIataCode().getCode().equals("LIS")
                              && r.getDestination().getIataCode().getCode().equals("MAD"))
                    .findFirst().orElseThrow();

            FlightRoute madopo = flightRouteRepository.findAll().stream()
                    .filter(r -> r.getOrigin().getIataCode().getCode().equals("MAD")
                              && r.getDestination().getIataCode().getCode().equals("OPO"))
                    .findFirst().orElseThrow();

            scheduledFlightRepository.save(new ScheduledFlight(opolis, csTPa,
                    LocalDateTime.now().minusDays(90), LocalDateTime.now().minusDays(90).plusMinutes(45)));
            scheduledFlightRepository.save(new ScheduledFlight(lismad, csTPb,
                    LocalDateTime.now().minusDays(85), LocalDateTime.now().minusDays(85).plusMinutes(80)));
            scheduledFlightRepository.save(new ScheduledFlight(madopo, csTPc,
                    LocalDateTime.now().minusDays(80), LocalDateTime.now().minusDays(80).plusMinutes(70)));
            scheduledFlightRepository.save(new ScheduledFlight(opolis, csTPb,
                    LocalDateTime.now().minusDays(75), LocalDateTime.now().minusDays(75).plusMinutes(45)));
            scheduledFlightRepository.save(new ScheduledFlight(lismad, csTPa,
                    LocalDateTime.now().minusDays(70), LocalDateTime.now().minusDays(70).plusMinutes(80)));
            scheduledFlightRepository.save(new ScheduledFlight(madopo, csTPa,
                    LocalDateTime.now().minusDays(60), LocalDateTime.now().minusDays(60).plusMinutes(70)));
            scheduledFlightRepository.save(new ScheduledFlight(opolis, csTPc,
                    LocalDateTime.now().minusDays(45), LocalDateTime.now().minusDays(45).plusMinutes(45)));
            scheduledFlightRepository.save(new ScheduledFlight(lismad, csTPc,
                    LocalDateTime.now().minusDays(30), LocalDateTime.now().minusDays(30).plusMinutes(80)));
            scheduledFlightRepository.save(new ScheduledFlight(opolis, csTPa,
                    LocalDateTime.now().minusDays(15), LocalDateTime.now().minusDays(15).plusMinutes(45)));

            scheduledFlightRepository.save(new ScheduledFlight(lismad, csTPb,
                    LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(5).plusMinutes(80)));

            System.out.println(" -> Scheduled Flights loaded.");
        }
    }

    private void bootstrapMaintenanceTemplates() {
        if (maintenanceTemplateRepository.count() == 0) {
            AircraftModel b737 = aircraftModelRepository.findByModelName("737 MAX").orElseThrow();
            AircraftModel a320 = aircraftModelRepository.findByModelName("A320neo").orElseThrow();
            AircraftModel b777 = aircraftModelRepository.findByModelName("777X").orElseThrow();
            AircraftModel a350 = aircraftModelRepository.findByModelName("A350").orElseThrow();
            AircraftModel e195 = aircraftModelRepository.findByModelName("E195-E2").orElseThrow();
            AircraftModel atr72 = aircraftModelRepository.findByModelName("ATR 72-600").orElseThrow();

            maintenanceTemplateRepository.save(new MaintenanceTemplate(
                    "A-Check Routine Inspection",
                    TemplateType.INSPECTION,
                    8.0,
                    List.of(a320, b737, e195, atr72),
                    List.of(
                            "Visual inspection of fuselage and wings",
                            "Check tyre pressure and brake wear",
                            "Inspect cabin emergency equipment",
                            "Verify navigation lights operation"
                    )
            ));

            maintenanceTemplateRepository.save(new MaintenanceTemplate(
                    "C-Check Scheduled Maintenance",
                    TemplateType.SCHEDULED_MAINTENANCE,
                    72.0,
                    List.of(a320, b737, b777, a350),
                    List.of(
                            "Detailed structural inspection",
                            "Lubrication of moving components",
                            "Hydraulic system pressure test",
                            "Avionics software update check",
                            "Cabin interior deep cleaning"
                    )
            ));

            maintenanceTemplateRepository.save(new MaintenanceTemplate(
                    "Engine Overhaul",
                    TemplateType.OVERHAUL,
                    160.0,
                    List.of(b777, a350, b737),
                    List.of(
                            "Full engine disassembly",
                            "Turbine blade inspection and replacement",
                            "Combustion chamber inspection",
                            "Reassembly and test-bench run",
                            "Final certification test flight"
                    )
            ));

            maintenanceTemplateRepository.save(new MaintenanceTemplate(
                    "Cabin WiFi Retrofit",
                    TemplateType.MODIFICATION,
                    24.0,
                    List.of(a320, e195, atr72, b737),
                    List.of(
                            "Install satellite antenna housing",
                            "Route cabin network cabling",
                            "Install and configure WiFi access points",
                            "Functional connectivity test"
                    )
            ));

            maintenanceTemplateRepository.save(new MaintenanceTemplate(
                    "Avionics Inspection",
                    TemplateType.INSPECTION,
                    6.0,
                    List.of(atr72, e195),
                    List.of(
                            "Test cockpit display units",
                            "Check autopilot calibration",
                            "Inspect communication radios"
                    )
            ));

            System.out.println(" -> Maintenance Templates loaded.");
        }
    }

    private void bootstrapMaintenanceRecords() {
        if (maintenanceRecordService.getRecordsForAircraft("CS-TPA").isEmpty()
                && maintenanceRecordService.getRecordsForAircraft("CS-TPB").isEmpty()) {

            Long aCheckId       = maintenanceTemplateRepository.findByTemplateName("A-Check Routine Inspection").orElseThrow().getId();
            Long cCheckId        = maintenanceTemplateRepository.findByTemplateName("C-Check Scheduled Maintenance").orElseThrow().getId();
            Long overhaulId      = maintenanceTemplateRepository.findByTemplateName("Engine Overhaul").orElseThrow().getId();
            Long wifiId          = maintenanceTemplateRepository.findByTemplateName("Cabin WiFi Retrofit").orElseThrow().getId();
            Long avionicsCheckId = maintenanceTemplateRepository.findByTemplateName("Avionics Inspection").orElseThrow().getId();


            completeFlow("CS-TPC", aCheckId, "Routine A-Check before redeployment",
                    LocalDate.now().minusDays(60), MaintenanceComponent.AIRFRAME, 1200.0,
                    "All checklist items passed; no defects found.", 7.5, 1150.0);

            completeFlow("CS-TPD", cCheckId, "Scheduled C-Check, structural and avionics review",
                    LocalDate.now().minusDays(45), MaintenanceComponent.AVIONICS, 9000.0,
                    "Replaced two avionics modules; all systems nominal.", 80.0, 9650.0);

            completeFlow("CS-TPE", overhaulId, "Engine overhaul after 6000 flight hours",
                    LocalDate.now().minusDays(30), MaintenanceComponent.ENGINE, 45000.0,
                    "Turbine blades replaced; test flight successful.", 168.0, 47200.0);

            completeFlow("CS-TPG", aCheckId, "A-Check before reactivation",
                    LocalDate.now().minusDays(20), MaintenanceComponent.EXTERIOR, 900.0,
                    "Minor corrosion treated on fuselage panels.", 9.0, 980.0);

            startedFlow("CS-TPF", avionicsCheckId, "Avionics inspection following reported display glitch",
                    LocalDate.now().minusDays(2), MaintenanceComponent.AVIONICS, 600.0);

            startedFlow("CS-TPB", wifiId, "Cabin WiFi retrofit in progress",
                    LocalDate.now().minusDays(1), MaintenanceComponent.INTERIOR, 15000.0);


            maintenanceRecordService.createRecord(
                    "CS-TPA", cCheckId, "Upcoming scheduled C-Check",
                    LocalDate.now().plusDays(10), null, MaintenanceComponent.AIRFRAME, 8500.0);

            System.out.println(" -> Maintenance Records loaded.");
        }
    }

    private void completeFlow(String registrationNumber,
                              Long templateId,
                              String description,
                              LocalDate startDate,
                              MaintenanceComponent component,
                              Double estimatedCost,
                              String completionNotes,
                              Double actualDurationHours,
                              Double actualCost) {

        var record = maintenanceRecordService.createRecord(
                registrationNumber, templateId, description, startDate, null, component, estimatedCost);

        record = maintenanceRecordService.startRecord(record.getId(), record.getVersion());

        maintenanceRecordService.completeRecord(
                record.getId(), record.getVersion(), completionNotes, actualDurationHours, actualCost);
    }

    private void startedFlow(String registrationNumber,
                             Long templateId,
                             String description,
                             LocalDate startDate,
                             MaintenanceComponent component,
                             Double estimatedCost) {

        var record = maintenanceRecordService.createRecord(
                registrationNumber, templateId, description, startDate, null, component, estimatedCost);

        maintenanceRecordService.startRecord(record.getId(), record.getVersion());
    }
}