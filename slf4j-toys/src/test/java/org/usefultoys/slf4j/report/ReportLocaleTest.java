package org.usefultoys.slf4j.report;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.MockLogger;
import org.usefultoys.slf4j.SessionConfig;

import java.nio.charset.Charset;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class ReportLocaleTest {

    private MockLogger mockLogger;

    @BeforeAll
    static void validate() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    @BeforeEach
    void setUp() {
        Logger logger = LoggerFactory.getLogger("test.report.locale");
        mockLogger = (MockLogger) logger;
        mockLogger.clearEvents();
    }

    @Test
    void shouldLogLocaleInformation() {
        // Arrange
        ReportLocale report = new ReportLocale(mockLogger);
        Locale defaultLocale = Locale.getDefault();

        // Act
        report.run();

        // Assert
        assertTrue(mockLogger.getEventCount() > 0);
        final String logs = mockLogger.getEvent(0).getFormattedMessage();

        assertTrue(logs.contains("Locale"));
        assertTrue(logs.contains("default locale: " + defaultLocale.getDisplayName()));
        assertTrue(logs.contains("language=" + defaultLocale.getDisplayLanguage()));
        assertTrue(logs.contains("country=" + defaultLocale.getDisplayCountry()));
        assertTrue(logs.contains("variant=" + defaultLocale.getDisplayVariant()));
        assertTrue(logs.contains("available locales:"));
    }
}
