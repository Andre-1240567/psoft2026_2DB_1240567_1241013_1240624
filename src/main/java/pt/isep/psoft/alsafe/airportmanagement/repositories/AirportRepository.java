package pt.isep.psoft.alsafe.airportmanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.isep.psoft.alsafe.airportmanagement.domain.Airport;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface AirportRepository extends JpaRepository<Airport, Long> {

    Optional<Airport> findByIataCode_Code(String code);

    @Query("SELECT a FROM Airport a WHERE " +
           "(:city IS NULL OR a.location.city = :city) AND " +
           "(:country IS NULL OR a.location.country = :country) AND " +
           "(:name IS NULL OR a.name = :name)")
    List<Airport> searchAirports(@Param("city") String city, @Param("country") String country, @Param("name") String name);
}