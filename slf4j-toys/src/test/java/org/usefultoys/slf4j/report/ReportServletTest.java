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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.MockLogger;
import org.slf4j.impl.MockLoggerEvent;
import org.usefultoys.slf4j.SessionConfig;
import org.usefultoys.slf4j.SystemConfig;
import org.usefultoys.slf4j.utils.ConfigParser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class ReportServletTest {

    @BeforeAll
    static void validateConsistentCharset() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    private static final String TEST_REPORTER_NAME = "test.report";
    private ReportServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private StringWriter responseWriter;
    private MockLogger reportMockLogger; // Logger used by the reports themselves
    private MockLogger servletMockLogger; // Logger used by the servlet (Slf4j annotation)

    @BeforeEach
    void setUp() throws IOException {
        ConfigParser.clearInitializationErrors();
        ReporterConfig.reset();
        SessionConfig.reset();
        SystemConfig.reset();

        // Configure ReporterConfig to use a known logger name for reports
        System.setProperty(ReporterConfig.PROP_NAME, TEST_REPORTER_NAME);
        ReporterConfig.init();

        // Initialize MockLoggers
        Logger reportLogger = LoggerFactory.getLogger(TEST_REPORTER_NAME);
        reportMockLogger = (MockLogger) reportLogger;
        reportMockLogger.clearEvents();
        reportMockLogger.setInfoEnabled(true); // Ensure INFO level is enabled

        Logger servletLogger = LoggerFactory.getLogger(ReportServlet.class);
        servletMockLogger = (MockLogger) servletLogger;
        servletMockLogger.clearEvents();
        servletMockLogger.setWarnEnabled(true); // Ensure WARN level is enabled for servlet's internal logging

        servlet = new ReportServlet();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    @AfterEach
    void tearDown() {
        System.clearProperty(ReporterConfig.PROP_NAME);
        ReporterConfig.reset();
        SessionConfig.reset();
        SystemConfig.reset();
        ConfigParser.clearInitializationErrors();
    }

    private void assertReportLogged(String expectedContentPart) {
        assertTrue(reportMockLogger.getEventCount() > 0, "Report logger should have events");
        String logOutput = reportMockLogger.getLoggerEvents().stream()
                .map(MockLoggerEvent::getFormattedMessage)
                .collect(Collectors.joining("\n"));
        assertTrue(logOutput.contains(expectedContentPart), "Report log output should contain: " + expectedContentPart);
        assertTrue(ConfigParser.isInitializationOK(), "No ConfigParser errors expected: " + ConfigParser.initializationErrors);
    }

    @Test
    void testNoPathInfoReturnsNotFound() throws IOException {
        when(request.getPathInfo()).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        verify(response).setContentType("text/plain");
        assertTrue(responseWriter.toString().contains("No report path provided."));
        assertEquals(1, servletMockLogger.getEventCount());
        servletMockLogger.assertEvent(0, MockLoggerEvent.Level.WARN, "No report path provided.");
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    void testUnknownPathInfoReturnsNotFound() throws IOException {
        when(request.getPathInfo()).thenReturn("/unknown");

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        verify(response).setContentType("text/plain");
        assertTrue(responseWriter.toString().contains("Unknown report path: unknown"));
        assertEquals(1, servletMockLogger.getEventCount());
        servletMockLogger.assertEvent(0, MockLoggerEvent.Level.WARN, "Unrecognized report path: unknown");
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    void testVMReport() throws IOException {
        when(request.getPathInfo()).thenReturn("/VM");
        servlet.doGet(request, response);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: vm"));
        assertReportLogged("Java Virtual Machine");
    }

    @Test
    void testFileSystemReport() throws IOException {
        when(request.getPathInfo()).thenReturn("/FileSystem");
        servlet.doGet(request, response);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: filesystem"));
        assertReportLogged("File system root:");
    }

    @Test
    void testMemoryReport() throws IOException {
        when(request.getPathInfo()).thenReturn("/Memory");
        servlet.doGet(request, response);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: memory"));
        assertReportLogged("Memory:");
    }

    @Test
    void testUserReport() throws IOException {
        when(request.getPathInfo()).thenReturn("/User");
        servlet.doGet(request, response);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: user"));
        assertReportLogged("User:");
    }

    @Test
    void testPhysicalSystemReport() throws IOException {
        when(request.getPathInfo()).thenReturn("/PhysicalSystem");
        servlet.doGet(request, response);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: physicalsystem"));
        assertReportLogged("Physical system");
    }

    @Test
    void testOperatingSystemReport() throws IOException {
        when(request.getPathInfo()).thenReturn("/OperatingSystem");
        servlet.doGet(request, response);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: operatingsystem"));
        assertReportLogged("Operating System");
    }

    @Test
    void testCalendarReport() throws IOException {
        when(request.getPathInfo()).thenReturn("/Calendar");
        servlet.doGet(request, response);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: calendar"));
        assertReportLogged("Calendar");
    }

    @Test
    void testLocaleReport() throws IOException {
        when(request.getPathInfo()).thenReturn("/Locale");
        servlet.doGet(request, response);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: locale"));
        assertReportLogged("Locale");
    }

    @Test
    void testCharsetReport() throws IOException {
        when(request.getPathInfo()).thenReturn("/Charset");
        servlet.doGet(request, response);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: charset"));
        assertReportLogged("Charset");
    }

    @Test
    void testNetworkInterfaceReport() throws IOException {
        // Note: Mocking NetworkInterface.getNetworkInterfaces() is complex.
        // This test primarily checks if the servlet attempts to run the report.
        when(request.getPathInfo()).thenReturn("/NetworkInterface");
        servlet.doGet(request, response);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: networkinterface"));
        // Cannot easily assert specific content without mocking NetworkInterface
        assertTrue(ConfigParser.isInitializationOK(), "No ConfigParser errors expected: " + ConfigParser.initializationErrors);
    }

    @Test
    void testSSLContextReport() throws IOException {
        when(request.getPathInfo()).thenReturn("/SSLContext");
        servlet.doGet(request, response);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: sslcontext"));
        assertReportLogged("SSL Context");
    }

    @Test
    void testDefaultTrustKeyStoreReport() throws IOException {
        when(request.getPathInfo()).thenReturn("/DefaultTrustKeyStore");
        servlet.doGet(request, response);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: defaulttrustkeystore"));
        assertReportLogged("Trust Keystore");
    }

    @Test
    void testEnvironmentReport() throws IOException {
        when(request.getPathInfo()).thenReturn("/Environment");
        servlet.doGet(request, response);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: environment"));
        assertReportLogged("System Environment:");
    }

    @Test
    void testPropertiesReport() throws IOException {
        when(request.getPathInfo()).thenReturn("/Properties");
        servlet.doGet(request, response);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: properties"));
        assertReportLogged("System Properties:");
    }

    @Test
    void testJvmArgumentsReport() throws IOException {
        when(request.getPathInfo()).thenReturn("/JvmArguments");
        servlet.doGet(request, response);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: jvmarguments"));
        assertReportLogged("JVM Arguments:");
    }

    @Test
    void testClasspathReport() throws IOException {
        when(request.getPathInfo()).thenReturn("/Classpath");
        servlet.doGet(request, response);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: classpath"));
        assertReportLogged("Classpath:");
    }

    @Test
    void testGarbageCollectorReport() throws IOException {
        when(request.getPathInfo()).thenReturn("/GarbageCollector");
        servlet.doGet(request, response);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: garbagecollector"));
        assertReportLogged("Garbage Collectors:");
    }
}
