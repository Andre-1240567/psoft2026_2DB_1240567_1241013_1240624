package pt.isep.psoft.alsafe.airportmanagement.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.isep.psoft.alsafe.aircraftmanagement.repositories.AircraftModelRepository;
import pt.isep.psoft.alsafe.airportmanagement.api.dto.*;
import pt.isep.psoft.alsafe.airportmanagement.domain.*;
import pt.isep.psoft.alsafe.airportmanagement.repositories.AirportRepository;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.List;

@Service
public class AirportService {
    private final AirportRepository airportRepository;
    private final AircraftModelRepository aircraftModelRepository;

    public AirportService(AirportRepository airportRepository,  AircraftModelRepository aircraftModelRepository) {
        this.airportRepository = airportRepository;
        this.aircraftModelRepository = aircraftModelRepository;
    }

    @Transactional
    public Airport createAirport(CreateAirportRequestDTO dto) {

        Optional<Airport> existingAirport = airportRepository.findByIataCode_Code(dto.getIataCode());
        if (existingAirport.isPresent()) {
            throw new IllegalArgumentException("Airport with IATACode " + dto.getIataCode() + " already exists");
        }

        IATACode iataCodeVO = new IATACode(dto.getIataCode());
        Timezone timezoneVO = new Timezone(dto.getTimezone());

        GPSCoordinates gpsCoordinatesVO = null;
        if (dto.getLatitude() != null && dto.getLongitude() != null) {
            gpsCoordinatesVO = new GPSCoordinates(dto.getLatitude(), dto.getLongitude());
        }

        Location locationVO = new Location(dto.getRegion(), dto.getCountry(), dto.getCity(), gpsCoordinatesVO);

        Airport newAirport = new Airport(iataCodeVO, dto.getName(), locationVO, timezoneVO);

        if (dto.getRunways() != null) {
            for (CreateRunwayRequestDTO runwayRequestDTO : dto.getRunways()) {
                Runway runway = new Runway(runwayRequestDTO.getName(), runwayRequestDTO.getLength(), runwayRequestDTO.getOrientation());
                newAirport.addRunway(runway);
            }
        }
        
        if (dto.getPhotos() != null) {
            for (String photo : dto.getPhotos()) {
                newAirport.addPhoto(photo);
            }
        }
        
        if (dto.getTerminals() != null) {
            for (CreateTerminalRequestDTO terminalDTO : dto.getTerminals()) {
                Terminal terminal = new Terminal(terminalDTO.getDesignation());
                
                if (terminalDTO.getGates() != null) {
                    for (String gateDesignation : terminalDTO.getGates()) {
                        terminal.addGate(new Gate(gateDesignation));
                    }
                }
                
                if (terminalDTO.getServices() != null) {
                    for (CreateServiceDTO serviceDTO : terminalDTO.getServices()) {
                        terminal.addService(new FacilityService(serviceDTO.getServiceType(), serviceDTO.getDescription()));
                    }
                }
                
                newAirport.addTerminal(terminal);
            }
        }

        return airportRepository.save(newAirport);

    }

    public Airport getAirportDetails(String iataCode) {
        return airportRepository.findByIataCode_Code(iataCode)
                .orElseThrow(() -> new IllegalArgumentException("Airport with the code " + iataCode + " not found."));
    }
    public Airport changeOperationalStatus(String iataCode, String statusString, Long version) {

        Airport airport = getAirportDetails(iataCode);

        if (version == null || !airport.getVersion().equals(version)) {
            throw new org.springframework.orm.ObjectOptimisticLockingFailureException(pt.isep.psoft.alsafe.airportmanagement.domain.Airport.class, iataCode);
        }

        Status newStatus;
        try {
            newStatus = Status.valueOf(statusString.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status. Use OPERATIONAL, UNDER_MAINTENANCE or CLOSED.");
        }

        airport.changeStatus(newStatus);

        return airportRepository.save(airport);
    }

    public List<Airport> searchAirports(String city, String country, String name) {
        return airportRepository.searchAirports(city, country, name);
    }

    @Transactional
    public Airport addAirplaneCertification(String iataCode, String modelName, Long version) {

        if (aircraftModelRepository.findByModelName(modelName).isEmpty()) {
            throw new IllegalArgumentException("Error: The airplane model'" + modelName + "' is not registred in the system.");
        }

        Airport airport = getAirportDetails(iataCode);

        if (version == null || !airport.getVersion().equals(version)) {
            throw new org.springframework.orm.ObjectOptimisticLockingFailureException(pt.isep.psoft.alsafe.airportmanagement.domain.Airport.class, iataCode);
        }

        airport.addCertification(modelName);

        return airportRepository.save(airport);
    }

    @Transactional
    public Airport updateAirportDetails(String iataCode, UpdateAirportDetailsRequestDTO dto) {
        Airport airport = getAirportDetails(iataCode);

        if (dto.getVersion() == null || !airport.getVersion().equals(dto.getVersion())) {
            throw new org.springframework.orm.ObjectOptimisticLockingFailureException(pt.isep.psoft.alsafe.airportmanagement.domain.Airport.class, iataCode);
        }

        OperationalHours operationalHours = null;
        if (dto.getOperationalHours() != null) {
            LocalTime opening = LocalTime.parse(dto.getOperationalHours().getOpeningTime());
            LocalTime closing = LocalTime.parse(dto.getOperationalHours().getClosingTime());
            operationalHours = new OperationalHours(opening, closing);
        }

        List<Contact> contacts = null;
        if (dto.getContacts() != null) {
            contacts = new ArrayList<>();
            for (ContactDTO contactDTO : dto.getContacts()) {
                contacts.add(new Contact(contactDTO.getValue(), contactDTO.getDepartment(), contactDTO.getType()));
            }
        }

        airport.updateDetails(operationalHours, contacts);

        return airportRepository.save(airport);
    }

    @Transactional(readOnly = true)
    public java.util.Map<String, List<Airport>> getAirportsGroupedBy(String groupBy) {
        List<Airport> allAirports = airportRepository.findAll();

        if ("region".equalsIgnoreCase(groupBy)) {
            return allAirports.stream()
                    .collect(java.util.stream.Collectors.groupingBy(a -> a.getLocation().getRegion()));
        } else if ("country".equalsIgnoreCase(groupBy)) {
            return allAirports.stream()
                    .collect(java.util.stream.Collectors.groupingBy(a -> a.getLocation().getCountry()));
        } else {
            throw new IllegalArgumentException("Invalid grouping criteria. Use 'region' or 'country'.");
        }
    }

}
