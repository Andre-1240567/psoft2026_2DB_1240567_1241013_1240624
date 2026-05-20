package pt.isep.psoft.alsafe.airports.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.isep.psoft.alsafe.airports.domain.Airport;

// O JpaRepository precisa de saber duas coisas: a Entidade (Airport) e o tipo da chave primária (String, porque é o iataCode)
public interface AirportRepository extends JpaRepository<Airport, String> {
}