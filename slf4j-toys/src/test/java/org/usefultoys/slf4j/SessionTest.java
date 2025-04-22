package org.usefultoys.slf4j;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.*;

class SessionTest {

    @BeforeAll
    static void validate() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

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
