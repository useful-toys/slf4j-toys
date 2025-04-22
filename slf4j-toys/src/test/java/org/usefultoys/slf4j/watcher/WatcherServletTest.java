package org.usefultoys.slf4j.watcher;

import org.junit.jupiter.api.*;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.TestLogger;
import org.usefultoys.slf4j.SessionConfig;
import org.usefultoys.slf4j.SystemConfig;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WatcherServletTest {

    @BeforeAll
    static void validate() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    @BeforeEach
    void resetWatcherConfigBeforeEach() {
        // Reinitialize WatcherConfig to ensure clean configuration before each test
        WatcherConfig.reset();
        SessionConfig.reset();
        SystemConfig.reset();
    }

    @AfterAll
    static void resetWatcherConfigAfterAll() {
        // Reinitialize WatcherConfig to ensure clean configuration for further tests
        WatcherConfig.reset();
        SessionConfig.reset();
        SystemConfig.reset();
    }

    private TestLogger testLogger = (TestLogger) LoggerFactory.getLogger(WatcherServlet.class);
    private TestLogger watcherLogger = (TestLogger) LoggerFactory.getLogger(WatcherConfig.name);

    @BeforeEach
    void setupLogger() {
        testLogger.setEnabled(true);
        testLogger.clearEvents();
        watcherLogger.setEnabled(true);
        watcherLogger.clearEvents();
    }

    @AfterEach
    void clearLogger() {
        testLogger.setEnabled(true);
        testLogger.clearEvents();
        watcherLogger.setEnabled(true);
        watcherLogger.clearEvents();
    }

    @Test
    void shouldLogSystemStatusSuccessfully() throws Exception {
        // Arrange
        WatcherServlet servlet = new WatcherServlet();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        // Act
        servlet.doGet(request, response);

        // Assert
        verify(response).setContentType("text/plain");
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertEquals("System status logged successfully.", responseWriter.toString().trim());

        assertTrue(testLogger.getEventCount() == 1);
        assertTrue(testLogger.getEvent(0).getFormattedMessage().contains("WatcherServlet accessed"));

        assertTrue(watcherLogger.getEventCount() == 1);
        assertTrue(watcherLogger.getEvent(0).getFormattedMessage().contains("Memory:"));
    }
}
