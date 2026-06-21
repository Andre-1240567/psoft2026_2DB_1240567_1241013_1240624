package pt.isep.psoft.alsafe.maintenancemanagement.services;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.Aircraft;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftStatus;
import pt.isep.psoft.alsafe.aircraftmanagement.repositories.AircraftRepository;
import pt.isep.psoft.alsafe.maintenancemanagement.domain.MaintenanceComponent;
import pt.isep.psoft.alsafe.maintenancemanagement.domain.MaintenanceRecord;
import pt.isep.psoft.alsafe.maintenancemanagement.domain.MaintenanceTemplate;
import pt.isep.psoft.alsafe.maintenancemanagement.repositories.MaintenanceRecordRepository;
import pt.isep.psoft.alsafe.maintenancemanagement.repositories.MaintenanceTemplateRepository;
import pt.isep.psoft.alsafe.shared.exceptions.ResourceNotFoundException;

import java.time.LocalDate;
import java.util.List;

@Service
public class MaintenanceRecordService {

    private final MaintenanceRecordRepository recordRepository;
    private final MaintenanceTemplateRepository templateRepository;
    private final AircraftRepository aircraftRepository;

    public MaintenanceRecordService(MaintenanceRecordRepository recordRepository,
                                    MaintenanceTemplateRepository templateRepository,
                                    AircraftRepository aircraftRepository) {
        this.recordRepository = recordRepository;
        this.templateRepository = templateRepository;
        this.aircraftRepository = aircraftRepository;
    }

    @Transactional
    public MaintenanceRecord createRecord(String registrationNumber,
                                          Long templateId,
                                          String description,
                                          LocalDate startDate,
                                          Double expectedDurationHours,
                                          MaintenanceComponent component,
                                          Double estimatedCost) {

        Aircraft aircraft = aircraftRepository.findById(registrationNumber.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Aircraft '" + registrationNumber + "' not found."));

        if (aircraft.getStatus() == AircraftStatus.UNDER_MAINTENANCE) {
            throw new IllegalStateException(
                    "Aircraft '" + registrationNumber + "' is already under maintenance.");
        }

        MaintenanceTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Maintenance template with id '" + templateId + "' not found."));

        MaintenanceRecord record = new MaintenanceRecord(
                aircraft,
                template,
                description,
                startDate,
                expectedDurationHours,
                component,
                estimatedCost
        );

        aircraft.updateStatus(AircraftStatus.UNDER_MAINTENANCE);
        aircraftRepository.save(aircraft);

        return recordRepository.save(record);
    }

    @Transactional(readOnly = true)
    public List<MaintenanceRecord> getRecordsForAircraft(String registrationNumber) {
        if (!aircraftRepository.existsById(registrationNumber.toUpperCase())) {
            throw new ResourceNotFoundException(
                    "Aircraft '" + registrationNumber + "' not found.");
        }
        return recordRepository.findByAircraft_RegistrationNumber(registrationNumber.toUpperCase());
    }

    @Transactional(readOnly = true)
    public List<MaintenanceHoursSummary> getTotalMaintenanceHoursPerAircraft() {
        return recordRepository.findTotalMaintenanceHoursPerAircraft()
                .stream()
                .map(row -> new MaintenanceHoursSummary(
                        (String) row[0],
                        row[1] != null ? ((Number) row[1]).doubleValue() : 0.0
                ))
                .toList();
    }

    @Transactional
    public MaintenanceRecord completeRecord(Long recordId,
                                            Long clientVersion,
                                            String completionNotes,
                                            Double actualDurationHours,
                                            Double actualCost) {

        MaintenanceRecord record = getRecordOrThrow(recordId);

        if (!record.getVersion().equals(clientVersion)) {
            throw new ObjectOptimisticLockingFailureException(MaintenanceRecord.class, recordId);
        }

        record.markAsCompleted(completionNotes, actualDurationHours, actualCost);

        Aircraft aircraft = record.getAircraft();
        aircraft.updateStatus(AircraftStatus.AVAILABLE);
        aircraftRepository.save(aircraft);

        return recordRepository.save(record);
    }

    @Transactional
    public MaintenanceRecord startRecord(Long recordId, Long clientVersion) {
        MaintenanceRecord record = getRecordOrThrow(recordId);

        if (!record.getVersion().equals(clientVersion)) {
            throw new ObjectOptimisticLockingFailureException(MaintenanceRecord.class, recordId);
        }

        record.startWork();
        return recordRepository.save(record);
    }

    @Transactional
    public MaintenanceRecord cancelRecord(Long recordId, Long clientVersion, String reason) {
        MaintenanceRecord record = getRecordOrThrow(recordId);

        if (!record.getVersion().equals(clientVersion)) {
            throw new ObjectOptimisticLockingFailureException(MaintenanceRecord.class, recordId);
        }

        record.cancel(reason);

        //  aircraft availability.
        Aircraft aircraft = record.getAircraft();
        aircraft.updateStatus(AircraftStatus.AVAILABLE);
        aircraftRepository.save(aircraft);

        return recordRepository.save(record);
    }

    @Transactional(readOnly = true)
    public List<MaintenanceRecord> searchRecords(String registrationNumber,
                                                  String componentStr,
                                                  LocalDate from,
                                                  LocalDate to) {

        MaintenanceComponent component = null;
        if (componentStr != null && !componentStr.trim().isEmpty()) {
            try {
                component = MaintenanceComponent.valueOf(componentStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid maintenance component: '" + componentStr
                        + "'. Valid values are: ENGINE, AIRFRAME, AVIONICS, INTERIOR, EXTERIOR.");
            }
        }

        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("Start date 'from' cannot be after end date 'to'.");
        }

        String reg = (registrationNumber != null) ? registrationNumber.toUpperCase() : null;

        return recordRepository.search(reg, component, from, to);
    }

    @Transactional(readOnly = true)
    public List<MaintenanceRecord> getOngoingActivities() {
        return recordRepository.findAllOngoing();
    }

    @Transactional(readOnly = true)
    public List<MaintenanceCostSummary> getCostReportPerAircraft() {
        return recordRepository.findCostReportPerAircraft()
                .stream()
                .map(row -> new MaintenanceCostSummary(
                        (String) row[0],
                        row[1] != null ? ((Number) row[1]).doubleValue() : null,
                        row[2] != null ? ((Number) row[2]).doubleValue() : null
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MaintenanceCostSummary> getCostReportPerAircraftModel() {
        return recordRepository.findCostReportPerAircraftModel()
                .stream()
                .map(row -> new MaintenanceCostSummary(
                        (String) row[0],
                        row[1] != null ? ((Number) row[1]).doubleValue() : null,
                        row[2] != null ? ((Number) row[2]).doubleValue() : null
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MaintenanceTurnaroundSummary> getAverageTurnaroundPerAircraftModel() {
        return recordRepository.findAverageTurnaroundPerAircraftModel()
                .stream()
                .map(row -> new MaintenanceTurnaroundSummary(
                        (String) row[0],
                        row[1] != null ? ((Number) row[1]).doubleValue() : 0.0
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MaintenanceRecord> getAlertsDueByDate() {
        return recordRepository.findDueForMaintenanceByDate(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<MaintenanceRecord> getAlertsDueByFlightHours() {
        return recordRepository.findDueForMaintenanceByFlightHours();
    }

    @Transactional(readOnly = true)
    public MaintenanceRecord getRecordById(Long id) {
        return getRecordOrThrow(id);
    }

    public record MaintenanceHoursSummary(String registrationNumber, Double totalHours) {}

    public record MaintenanceCostSummary(String key, Double estimatedCost, Double actualCost) {}

    public record MaintenanceTurnaroundSummary(String modelName, Double avgDurationHours) {}

    private MaintenanceRecord getRecordOrThrow(Long id) {
        return recordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Maintenance record with id '" + id + "' not found."));
    }
}