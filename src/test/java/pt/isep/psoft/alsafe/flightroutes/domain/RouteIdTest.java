package pt.isep.psoft.alsafe.flightroutes.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RouteIdTest {

    @Test
    void ensureValidRouteIdIsCreated() {
        RouteId id = new RouteId("R123");
        assertEquals("R123", id.getId());
        assertEquals("R123", id.toString());
    }

    @Test
    void ensureNullOrBlankIdThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new RouteId(null));
        assertThrows(IllegalArgumentException.class, () -> new RouteId("   "));
        assertThrows(IllegalArgumentException.class, () -> new RouteId(""));
    }

    @Test
    void ensureEqualsAndHashCodeWorkCorrectly() {
        RouteId id1 = new RouteId("R123");
        RouteId id2 = new RouteId("R123");
        RouteId id3 = new RouteId("R999");

        assertEquals(id1, id1);
        assertEquals(id1, id2);
        assertNotEquals(id1, id3);
        assertNotEquals(id1, null);
        assertNotEquals(id1, new Object());
        
        assertEquals(id1.hashCode(), id2.hashCode());
    }
}