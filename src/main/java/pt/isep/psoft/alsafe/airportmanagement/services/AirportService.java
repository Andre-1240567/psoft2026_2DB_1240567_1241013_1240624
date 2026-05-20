package pt.isep.psoft.alsafe.airportmanagement.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.isep.psoft.alsafe.airportmanagement.api.dto.CreateAirportRequestDTO;
import pt.isep.psoft.alsafe.airportmanagement.api.dto.CreateRunwayRequestDTO;
import pt.isep.psoft.alsafe.airportmanagement.domain.*;
import pt.isep.psoft.alsafe.airportmanagement.repositories.AirportRepository;

import java.util.Optional;

@Service
public class AirportService {
    private final AirportRepository airportRepository;

    public AirportService(AirportRepository airportRepository) {
        this.airportRepository = airportRepository;
    }

    @Transactional
    public Airport createAirport(CreateAirportRequestDTO dto){

        Optional<Airport> existingAirport = airportRepository.findByIataCode_Code(dto.getIataCode());
        if(existingAirport.isPresent()){
            throw new IllegalArgumentException("Airport with IATACode " + dto.getIataCode() + " already exists");
        }

        IATACode iataCodeVO = new IATACode(dto.getIataCode());
        GPSCoordinates gpsCoordinatesVO = new GPSCoordinates(dto.getLatitude(), dto.getLongitude());
        Location locationVO = new Location(dto.getRegion(), dto.getCountry(), dto.getCity(), gpsCoordinatesVO);

        Airport newAirport = new Airport(iataCodeVO,dto.getName(),locationVO);

        if(dto.getRunways() != null){
            for(CreateRunwayRequestDTO runwayRequestDTO : dto.getRunways()){
                Runway runway = new Runway(runwayRequestDTO.getName(), runwayRequestDTO.getLength(), runwayRequestDTO.getOrientation());
                newAirport.addRunway(runway);
            }
        }
        return  airportRepository.save(newAirport);
    }
}
