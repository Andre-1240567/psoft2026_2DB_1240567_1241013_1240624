package pt.isep.psoft.alsafe.airportmanagement.domain;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;

@Getter
@Embeddable
public class Contact {
    private String value;
    private String department;

    @Enumerated(EnumType.STRING)
    private ContactType type;

    protected Contact() {}

    public Contact(String value, String department, ContactType type) {
        if (value == null || value.trim().isEmpty() || type == null) {
            throw new IllegalArgumentException("Contact value and type are mandatory.");
        }
        this.value = value;
        this.department = department;
        this.type = type;
    }
}
