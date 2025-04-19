package org.usefultoys.slf4j;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SessionTest {

    @Test
    void testUuidIsNotNull() {
        assertNotNull(Session.uuid, "Session UUID should not be null");
    }

    @Test
    void testUuidIsImmutable() {
        String firstUuid = Session.uuid;
        String secondUuid = Session.uuid;
        assertEquals(firstUuid, secondUuid, "Session UUID should remain constant");
    }

    @Test
    void testUuidFormat() {
        assertTrue(Session.uuid.matches("^[a-f0-9]{32}$"), "Session UUID should be a 32-character hexadecimal string");
    }
}
