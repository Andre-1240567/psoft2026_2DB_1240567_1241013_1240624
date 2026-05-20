package pt.isep.psoft.alsafe.airports.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Airport {

    @Id
    private String iataCode; // O identificador único do aeroporto (ex: OPO, LIS)

    // Este é um construtor básico só para podermos criar aeroportos falsos no Bootstrap
    public Airport(String iataCode) {
        this.iataCode = iataCode;
    }
}
