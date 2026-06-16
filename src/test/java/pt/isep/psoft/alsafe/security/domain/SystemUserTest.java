package pt.isep.psoft.alsafe.security.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SystemUserTest {

    @Test
    void ensureUserIsCreatedSuccessfully() {
        SystemUser user = new SystemUser("atcc_jose", "hashed_pw", "ATCC");
        assertEquals("atcc_jose", user.getUsername());
        assertEquals("hashed_pw", user.getPasswordHash());
        assertEquals("ATCC",      user.getRoles());
    }

    @Test
    void ensureExceptionWhenUsernameIsBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> new SystemUser("", "hashed_pw", "ATCC"));
    }

    @Test
    void ensureExceptionWhenPasswordHashIsBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> new SystemUser("atcc_jose", "", "ATCC"));
    }

    @Test
    void ensureExceptionWhenRolesIsBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> new SystemUser("atcc_jose", "hashed_pw", ""));
    }

    @Test
    void ensureExceptionWhenUsernameIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new SystemUser(null, "hashed_pw", "ATCC"));
    }
    @Test
    void ensureExceptionWhenPasswordHashIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new SystemUser("atcc_jose", null, "ATCC"));
    }

    @Test
    void ensureExceptionWhenRolesIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new SystemUser("atcc_jose", "hashed_pw", null));
    }
}