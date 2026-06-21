package pt.isep.psoft.alsafe.maintenancemanagement.api;

import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;
import pt.isep.psoft.alsafe.maintenancemanagement.api.dto.*;
import pt.isep.psoft.alsafe.maintenancemanagement.domain.MaintenanceRecord;
import pt.isep.psoft.alsafe.maintenancemanagement.domain.MaintenanceStatus;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class MaintenanceRecordModelAssembler
        extends RepresentationModelAssemblerSupport<MaintenanceRecord, MaintenanceRecordResponseDTO> {

    public MaintenanceRecordModelAssembler() {
        super(MaintenanceRecordController.class, MaintenanceRecordResponseDTO.class);
    }

    @Override
    public MaintenanceRecordResponseDTO toModel(MaintenanceRecord record) {
        MaintenanceRecordResponseDTO dto = new MaintenanceRecordResponseDTO(record);

        MaintenanceRecordController ctrl = methodOn(MaintenanceRecordController.class);

        dto.add(linkTo(ctrl.getRecordById(record.getId())).withSelfRel());
        dto.add(linkTo(ctrl.getRecordsForAircraft(record.getAircraft().getRegistrationNumber()))
                .withRel("aircraft-records"));

        MaintenanceStatus status = record.getStatus();

        if (status == MaintenanceStatus.PLANNED) {
            dto.add(linkTo(ctrl.startRecord(record.getId(), new VersionedActionDTO())).withRel("start"));
            dto.add(linkTo(ctrl.cancelRecord(record.getId(), new CancelMaintenanceRecordDTO())).withRel("cancel"));
        }

        if (status == MaintenanceStatus.IN_PROGRESS) {
            dto.add(linkTo(ctrl.completeRecord(record.getId(), new CompleteMaintenanceRecordDTO())).withRel("complete"));
            dto.add(linkTo(ctrl.cancelRecord(record.getId(), new CancelMaintenanceRecordDTO())).withRel("cancel"));
        }

        return dto;
    }
}