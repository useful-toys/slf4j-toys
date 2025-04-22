package org.usefultoys.slf4j.watcher;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.TestLogger;
import org.usefultoys.slf4j.SessionConfig;

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

    private TestLogger testLogger;
    private TestLogger watcherLogger;

    @BeforeEach
    void setUp() {
        testLogger = (TestLogger) LoggerFactory.getLogger(WatcherServlet.class);
        testLogger.clearEvents();
        watcherLogger = (TestLogger) LoggerFactory.getLogger(WatcherConfig.name);
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

        assertTrue(testLogger.getEventCount() > 0);
        assertTrue(testLogger.getEvent(0).getFormattedMessage().contains("WatcherServlet accessed"));

        assertTrue(watcherLogger.getEventCount() > 1);
        assertTrue(watcherLogger.getEvent(0).getFormattedMessage().contains("Memory:"));
        final String json5 = watcherLogger.getEvent(1).getMessage();
        final WatcherData data = new WatcherData();
        data.readJson5(json5);
        assertEquals(json5, data.json5Message());
    }
}
