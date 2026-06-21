package pt.isep.psoft.alsafe.airportmanagement.services;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pt.isep.psoft.alsafe.aircraftmanagement.repositories.AircraftModelRepository;
import pt.isep.psoft.alsafe.airportmanagement.api.dto.*;
import pt.isep.psoft.alsafe.airportmanagement.domain.*;
import pt.isep.psoft.alsafe.airportmanagement.repositories.AirportRepository;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AirportService {
    private final AirportRepository airportRepository;
    private final AircraftModelRepository aircraftModelRepository;
    private final Validator validator;

    public AirportService(AirportRepository airportRepository,
                           AircraftModelRepository aircraftModelRepository,
                           Validator validator) {
        this.airportRepository = airportRepository;
        this.aircraftModelRepository = aircraftModelRepository;
        this.validator = validator;
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

    /**
     * US225 - Bulk import airports from a CSV file.
     * <p>
     * Design notes:
     * - Each row is mapped into the SAME {@link CreateAirportRequestDTO} used by US106, then validated with the
     *   SAME Bean Validation annotations (via the injected {@link Validator}), and finally created through the
     *   SAME {@link #createAirport(CreateAirportRequestDTO)} method. No business rule or validation is duplicated.
     * - Processing is row-by-row and a bad row does NOT roll back the rows already imported: {@link #createAirport}
     *   is called via a plain "this" call (self-invocation), so its own @Transactional annotation has no effect
     *   here - but that's fine, because Spring Data JPA's repository.save() is transactional on its own, so each
     *   successful row is committed independently. This is intentional, not an oversight.
     * - Expected CSV header (exact column names, case-sensitive):
     *   iataCode,name,region,city,country,timezone,latitude,longitude,runwayName,runwayLength,runwayOrientation
     *   latitude/longitude may be left empty. Every airport needs at least one runway (domain constraint), so the
     *   three runway columns are mandatory; supporting more than one runway per imported airport was left out to
     *   keep the CSV format flat and easy to produce - it can be added later as a pipe-separated mini-list.
     */
    public AirportImportResult importAirportsFromCsv(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("The uploaded CSV file is empty.");
        }

        List<Airport> createdAirports = new ArrayList<>();
        List<AirportImportErrorDTO> errors = new ArrayList<>();
        int rowCount = 0;

        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setTrim(true)
                     .setIgnoreEmptyLines(true)
                     .build()
                     .parse(reader)) {

            for (CSVRecord record : parser) {
                rowCount++;
                String iataCodeAttempt = readColumn(record, "iataCode");

                try {
                    CreateAirportRequestDTO dto = mapToCreateAirportRequestDTO(record);

                    Set<ConstraintViolation<CreateAirportRequestDTO>> violations = validator.validate(dto);
                    if (!violations.isEmpty()) {
                        String message = violations.stream()
                                .map(ConstraintViolation::getMessage)
                                .collect(Collectors.joining("; "));
                        throw new IllegalArgumentException(message);
                    }

                    createdAirports.add(createAirport(dto));

                } catch (RuntimeException e) {
                    errors.add(new AirportImportErrorDTO(rowCount, iataCodeAttempt, e.getMessage()));
                }
            }

        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read the CSV file: " + e.getMessage());
        }

        return new AirportImportResult(rowCount, createdAirports, errors);
    }

    private CreateAirportRequestDTO mapToCreateAirportRequestDTO(CSVRecord record) {
        CreateAirportRequestDTO dto = new CreateAirportRequestDTO();
        dto.setIataCode(readColumn(record, "iataCode"));
        dto.setName(readColumn(record, "name"));
        dto.setRegion(readColumn(record, "region"));
        dto.setCity(readColumn(record, "city"));
        dto.setCountry(readColumn(record, "country"));
        dto.setTimezone(readColumn(record, "timezone"));
        dto.setLatitude(parseOptionalDouble(readColumn(record, "latitude"), "latitude"));
        dto.setLongitude(parseOptionalDouble(readColumn(record, "longitude"), "longitude"));

        String runwayName = readColumn(record, "runwayName");
        String runwayLength = readColumn(record, "runwayLength");
        String runwayOrientation = readColumn(record, "runwayOrientation");

        if (runwayName == null || runwayLength == null || runwayOrientation == null) {
            throw new IllegalArgumentException(
                    "Columns runwayName, runwayLength and runwayOrientation are mandatory " +
                    "(every airport needs at least one runway).");
        }

        CreateRunwayRequestDTO runwayDTO = new CreateRunwayRequestDTO();
        runwayDTO.setName(runwayName);
        runwayDTO.setLength(parseRequiredDouble(runwayLength, "runwayLength"));
        runwayDTO.setOrientation(parseOrientation(runwayOrientation));

        dto.setRunways(List.of(runwayDTO));
        return dto;
    }

    private String readColumn(CSVRecord record, String column) {
        String value;
        try {
            value = record.get(column);
        } catch (IllegalArgumentException e) {
            return null;
        }
        if (value == null) {
            return null;
        }
        value = value.trim();
        return value.isEmpty() ? null : value;
    }

    private Double parseOptionalDouble(String value, String fieldName) {
        if (value == null) {
            return null;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid numeric value for " + fieldName + ": '" + value + "'");
        }
    }

    private Double parseRequiredDouble(String value, String fieldName) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid numeric value for " + fieldName + ": '" + value + "'");
        }
    }

    private Orientation parseOrientation(String value) {
        try {
            return Orientation.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid runway orientation: '" + value + "'.");
        }
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