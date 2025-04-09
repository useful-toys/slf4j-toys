package org.usefultoys.slf4j.report;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.TestLogger;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class ReportLocaleTest {

    private TestLogger testLogger;

    @BeforeEach
    void setUp() {
        Logger logger = LoggerFactory.getLogger("test.report.locale");
        testLogger = (TestLogger) logger;
        testLogger.clearEvents();
    }

    @Test
    void shouldLogLocaleInformation() {
        // Arrange
        ReportLocale report = new ReportLocale(testLogger);
        Locale defaultLocale = Locale.getDefault();

        // Act
        report.run();

        // Assert
        assertTrue(testLogger.getEventCount() > 0);
        final String logs = testLogger.getEvent(0).getFormattedMessage();

        assertTrue(logs.contains("Locale"));
        assertTrue(logs.contains("default locale: " + defaultLocale.getDisplayName()));
        assertTrue(logs.contains("language=" + defaultLocale.getDisplayLanguage()));
        assertTrue(logs.contains("country=" + defaultLocale.getDisplayCountry()));
        assertTrue(logs.contains("variant=" + defaultLocale.getDisplayVariant()));
        assertTrue(logs.contains("available locales:"));
    }
}
