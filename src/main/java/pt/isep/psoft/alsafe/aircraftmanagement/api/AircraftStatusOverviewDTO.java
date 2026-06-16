package pt.isep.psoft.alsafe.aircraftmanagement.api;

import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.springframework.hateoas.RepresentationModel;

@Getter
@Setter
public class AircraftStatusOverviewDTO extends RepresentationModel<AircraftStatusOverviewDTO> {

    private Map<String, List<AircraftResponseDTO>> aircraftsByStatus = new HashMap<>();
    
    private int totalAvailable;
    private int totalInFlight;
    private int totalUnderMaintenance;
    private int totalInactive;

    public void addAircraftToStatus(String status, AircraftResponseDTO aircraftDTO) {
        aircraftsByStatus.computeIfAbsent(status, k -> new java.util.ArrayList<>()).add(aircraftDTO);
        
        switch (status) {
            case "AVAILABLE":
                totalAvailable++;
                break;
            case "IN_FLIGHT":
                totalInFlight++;
                break;
            case "UNDER_MAINTENANCE":
                totalUnderMaintenance++;
                break;
            case "INACTIVE":
                totalInactive++;
                break;
        }
    }
}
