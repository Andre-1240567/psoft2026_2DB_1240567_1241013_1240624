package pt.isep.psoft.alsafe.airportmanagement.domain;

import jakarta.persistence.Embeddable;
import lombok.Getter;

@Getter
@Embeddable
public class Gate {

    private String designation;

    protected Gate() {}

    public Gate(String designation) {
        if (designation == null || designation.trim().isEmpty()) {
            throw new IllegalArgumentException("Gate designation cannot be empty.");
        }
        this.designation = designation;
    }
}
