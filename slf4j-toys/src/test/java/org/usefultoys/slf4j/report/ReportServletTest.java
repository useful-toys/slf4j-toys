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

package org.usefultoys.slf4j.report;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.MockLogger;
import org.usefultoys.slf4j.SessionConfig;
import org.usefultoys.slf4j.SystemConfig;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class ReportServletTest {
    private ReportServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private StringWriter responseWriter;
    private MockLogger reportLogger;
    private MockLogger servletLogger;

    @BeforeAll
    static void validate() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    @BeforeEach
    void resetWatcherConfigBeforeEach() {
        // Reinitialize each configuration to ensure a clean configuration before each test
        ReporterConfig.reset();
        SessionConfig.reset();
        SystemConfig.reset();
    }

    @AfterAll
    static void resetWatcherConfigAfterAll() {
        // Reinitialize each configuration to ensure a clean configuration before each test
        ReporterConfig.reset();
        SessionConfig.reset();
        SystemConfig.reset();
    }

    @BeforeEach
    void setUp() throws Exception {
        servlet = new ReportServlet();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);

        responseWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(writer);

        reportLogger = (MockLogger) (MockLogger) LoggerFactory.getLogger(ReporterConfig.name);
        reportLogger.clearEvents();
        servletLogger = (MockLogger) LoggerFactory.getLogger(ReportServlet.class);
        servletLogger.clearEvents();
    }

    @Test
    void shouldRespondWith404WhenPathIsNull() throws Exception {
        when(request.getPathInfo()).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        assertTrue(responseWriter.toString().contains("No report path provided"));
    }

    @Test
    void shouldRespondWith404ForUnknownPath() throws Exception {
        when(request.getPathInfo()).thenReturn("/unknown");

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        assertTrue(responseWriter.toString().contains("Unknown report path"));
    }

    @Test
    void shouldTriggerVmReportAndRespondOk() throws Exception {
        when(request.getPathInfo()).thenReturn("/VM");

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: vm"));

        // Verifica se o log contém algo da JVM
        assertTrue(reportLogger.getEventCount() > 0);
        final String fullLog = reportLogger.getEvent(0).getFormattedMessage();
        assertTrue(fullLog.contains("Java Virtual Machine"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/VM",
            "/FileSystem",
            "/Memory",
            "/User",
            "/PhysicalSystem",
            "/OperatingSystem",
            "/Calendar",
            "/Locale",
            "/Charset",
            "/SSLContext",
            "/DefaultTrustKeyStore"
            // "/NetworkInterface" pode ser separado se quiser mockar interfaces
    })
    void shouldRespondWithOkAndLogReport(final String path) throws Exception {
        // Arrange
        final ReportServlet servlet = new ReportServlet();

        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getPathInfo()).thenReturn(path);

        final StringWriter responseWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(writer);

        final MockLogger logger = (MockLogger) LoggerFactory.getLogger("org.usefultoys.slf4j.report.ReportServlet");
        logger.clearEvents();

        // Act
        servlet.doGet(request, response);
        writer.flush();

        // Assert
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: " + path.toLowerCase().substring(1)));

        assertTrue(reportLogger.getEventCount() > 0, "Expected some logs for path: " + path);
    }
}
