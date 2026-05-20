package pt.isep.psoft.alsafe.airportmanagement.domain;

import jakarta.persistence.Embeddable;

@Embeddable
public class IATACode {
    private String code;

    protected IATACode() {
    }


    public IATACode(String code) {
        if (code == null || !code.matches("^[A-Z]{3}$")) {
            throw new IllegalArgumentException("Invalid code format, must have 3 uppercase letters.");
        }
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}