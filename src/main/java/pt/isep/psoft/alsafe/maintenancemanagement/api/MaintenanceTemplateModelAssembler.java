package pt.isep.psoft.alsafe.maintenancemanagement.api;

import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;
import pt.isep.psoft.alsafe.maintenancemanagement.api.dto.MaintenanceTemplateResponseDTO;
import pt.isep.psoft.alsafe.maintenancemanagement.api.dto.UpdateMaintenanceTemplateDTO;
import pt.isep.psoft.alsafe.maintenancemanagement.domain.MaintenanceTemplate;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class MaintenanceTemplateModelAssembler
        extends RepresentationModelAssemblerSupport<MaintenanceTemplate, MaintenanceTemplateResponseDTO> {

    public MaintenanceTemplateModelAssembler() {
        super(MaintenanceTemplateController.class, MaintenanceTemplateResponseDTO.class);
    }

    @Override
    public MaintenanceTemplateResponseDTO toModel(MaintenanceTemplate template) {
        MaintenanceTemplateResponseDTO dto = new MaintenanceTemplateResponseDTO(template);

        MaintenanceTemplateController ctrl = methodOn(MaintenanceTemplateController.class);

        dto.add(linkTo(ctrl.getTemplateById(template.getId())).withSelfRel());
        dto.add(linkTo(ctrl.updateTemplate(template.getId(), new UpdateMaintenanceTemplateDTO())).withRel("update"));

        return dto;
    }
}