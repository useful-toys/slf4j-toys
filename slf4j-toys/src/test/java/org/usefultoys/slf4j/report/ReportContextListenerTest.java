package org.usefultoys.slf4j.report;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.TestLogger;
import org.usefultoys.slf4j.SessionConfig;

import javax.servlet.ServletContextEvent;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ReportContextListenerTest {
    private TestLogger testLogger;
    private ReportContextListener listener;

    @BeforeAll
    static void validate() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    @BeforeEach
    void setUp() {
        testLogger = (TestLogger) LoggerFactory.getLogger(ReporterConfig.name);
        testLogger.clearEvents();
        listener = new ReportContextListener();

        // Enable only one report to simplify the test
        ReporterConfig.reportVM = true;
        ReporterConfig.reportMemory = false;
        ReporterConfig.reportUser = false;
        ReporterConfig.reportOperatingSystem = false;
        ReporterConfig.reportPhysicalSystem = false;
        ReporterConfig.reportEnvironment = false;
        ReporterConfig.reportProperties = false;
        ReporterConfig.reportFileSystem = false;
        ReporterConfig.reportCalendar = false;
        ReporterConfig.reportLocale = false;
        ReporterConfig.reportCharset = false;
        ReporterConfig.reportNetworkInterface = false;
        ReporterConfig.reportSSLContext = false;
        ReporterConfig.reportDefaultTrustKeyStore = false;
    }

    @Test
    void shouldLogReportsOnContextInitialization() {
        ServletContextEvent event = mock(ServletContextEvent.class);

        // Act
        listener.contextInitialized(event);

        // Assert
        assertTrue(testLogger.getEventCount() > 0, "Expected at least one log event");

        boolean vmReported = testLogger.getEvent(0).getFormattedMessage().contains("Java Virtual Machine");
        assertTrue(vmReported, "Expected VM report to be logged");
    }

    @Test
    void shouldDoNothingOnContextDestroyed() {
        ServletContextEvent event = mock(ServletContextEvent.class);

        // Act
        listener.contextDestroyed(event);

        // Assert
        // No side effect expected — especially no logging
        assertEquals(0, testLogger.getEventCount(), "Expected no log output on contextDestroyed");
    }
}