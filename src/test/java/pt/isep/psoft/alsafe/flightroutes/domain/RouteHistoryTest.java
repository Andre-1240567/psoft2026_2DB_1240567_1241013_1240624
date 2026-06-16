package pt.isep.psoft.alsafe.flightroutes.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RouteHistoryTest {

    @Test
    void ensureHistoryIsCreatedCorrectly() {
        RouteHistory history = new RouteHistory("Route updated", "atcc_jose");
        
        assertEquals("Route updated", history.getDescription());
        assertEquals("atcc_jose", history.getAuthor());
        assertNotNull(history.getChangeDate());
    }

    @Test
    void ensureBlankDescriptionThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new RouteHistory("", "author"));
        assertThrows(IllegalArgumentException.class, () -> new RouteHistory(null, "author"));
    }

    @Test
    void ensureBlankAuthorThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new RouteHistory("desc", "  "));
        assertThrows(IllegalArgumentException.class, () -> new RouteHistory("desc", null));
    }
}