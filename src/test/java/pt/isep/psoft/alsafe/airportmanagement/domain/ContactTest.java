package pt.isep.psoft.alsafe.airportmanagement.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContactTest {

    @Test
    void ensureValidContactIsCreated() {
        Contact c = new Contact("+351210000000", "Operations", ContactType.PHONE);
        assertEquals("+351210000000", c.getValue());
        assertEquals("Operations", c.getDepartment());
        assertEquals(ContactType.PHONE, c.getType());
    }

    @Test
    void ensureNullValueThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Contact(null, "Ops", ContactType.PHONE));
    }

    @Test
    void ensureBlankValueThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Contact("   ", "Ops", ContactType.PHONE));
    }

    @Test
    void ensureNullTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Contact("+351210000000", "Ops", null));
    }

    @Test
    void ensureNullDepartmentIsAccepted() {
        assertDoesNotThrow(() -> new Contact("+351210000000", null, ContactType.EMAIL));
    }
}