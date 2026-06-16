package pt.isep.psoft.alsafe.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret",
                "thisIsAVeryLongSecretKeyForTestingPurposesOnly1234567890");
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 3600000);
    }

    @Test
    void ensureTokenIsGeneratedSuccessfully() {
        String token = jwtUtils.generateJwtToken("atcc_jose", "ATCC");
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void ensureUsernameIsExtractedFromToken() {
        String token = jwtUtils.generateJwtToken("atcc_jose", "ATCC");
        assertEquals("atcc_jose", jwtUtils.getUserNameFromJwtToken(token));
    }

    @Test
    void ensureRoleIsExtractedFromToken() {
        String token = jwtUtils.generateJwtToken("atcc_jose", "ATCC");
        assertEquals("ATCC", jwtUtils.getRoleFromJwtToken(token));
    }

    @Test
    void ensureValidTokenPassesValidation() {
        String token = jwtUtils.generateJwtToken("atcc_jose", "ATCC");
        assertTrue(jwtUtils.validateJwtToken(token));
    }

    @Test
    void ensureInvalidTokenFailsValidation() {
        assertFalse(jwtUtils.validateJwtToken("this.is.not.a.valid.token"));
    }

    @Test
    void ensureExpiredTokenFailsValidation() {
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", -1000);
        String token = jwtUtils.generateJwtToken("atcc_jose", "ATCC");
        assertFalse(jwtUtils.validateJwtToken(token));
    }

    @Test
    void ensureMultipleRolesArePreservedInToken() {
        String token = jwtUtils.generateJwtToken("admin", "ADMIN,ATCC,BACKOFFICE_OPERATOR");
        assertEquals("ADMIN,ATCC,BACKOFFICE_OPERATOR", jwtUtils.getRoleFromJwtToken(token));
    }
}