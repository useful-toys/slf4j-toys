/*
 * Copyright 2025 Daniel Felix Ferber
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.usefultoys.slf4j.watcher;

import org.junit.jupiter.api.*;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.MockLogger;
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

    private MockLogger mockLogger = (MockLogger) LoggerFactory.getLogger(WatcherServlet.class);
    private MockLogger watcherLogger = (MockLogger) LoggerFactory.getLogger(WatcherConfig.name);

    @BeforeEach
    void setupLogger() {
        mockLogger.setEnabled(true);
        mockLogger.clearEvents();
        watcherLogger.setEnabled(true);
        watcherLogger.clearEvents();
    }

    @AfterEach
    void clearLogger() {
        mockLogger.setEnabled(true);
        mockLogger.clearEvents();
        watcherLogger.setEnabled(true);
        watcherLogger.clearEvents();
    }

    @Test
    void shouldLogSystemStatusSuccessfully() throws Exception {
        // Arrange
        final WatcherServlet servlet = new WatcherServlet();
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final StringWriter responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        // Act
        servlet.doGet(request, response);

        // Assert
        verify(response).setContentType("text/plain");
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertEquals("System status logged successfully.", responseWriter.toString().trim());

        assertTrue(mockLogger.getEventCount() == 1);
        assertTrue(mockLogger.getEvent(0).getFormattedMessage().contains("WatcherServlet accessed"));

        assertTrue(watcherLogger.getEventCount() == 1);
        assertTrue(watcherLogger.getEvent(0).getFormattedMessage().contains("Memory:"));
    }
}
