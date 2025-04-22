package org.usefultoys.slf4j;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.*;

class SessionConfigTest {

    @BeforeAll
    static void validate() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    @BeforeEach
    void setUp() {
        System.clearProperty(SessionConfig.PROP_PRINT_UUID_SIZE);
        System.clearProperty(SessionConfig.PROP_PRINT_CHARSET);

        // Reinitialize SessionConfig to ensure clean state for each test
        SessionConfig.init();
    }

    @AfterAll
    static void tearDown() {
        System.clearProperty(SessionConfig.PROP_PRINT_UUID_SIZE);
        System.clearProperty(SessionConfig.PROP_PRINT_CHARSET);

        // Reinitialize SessionConfig to ensure clean state for each test
        SessionConfig.init();
    }

    @Test
    void testDefaultValues() {
        assertEquals(5, SessionConfig.uuidSize);
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset);
    }

    @Test
    void testUuidSizeProperty() {
        System.setProperty(SessionConfig.PROP_PRINT_UUID_SIZE, "10");
        SessionConfig.init(); // Reinitialize to apply new system properties
        assertEquals(10, SessionConfig.uuidSize, "uuidSize should reflect the system property value");
    }

    @Test
    void testCharsetProperty() {
        System.setProperty(SessionConfig.PROP_PRINT_CHARSET, "ISO-8859-1");
        SessionConfig.init(); // Reinitialize to apply new system properties
        assertEquals("ISO-8859-1", SessionConfig.charset, "charset should reflect the system property value");
    }
}
