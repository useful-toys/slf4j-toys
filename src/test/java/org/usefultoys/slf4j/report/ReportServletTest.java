/*
 * Copyright 2026 Daniel Felix Ferber
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.impl.MockLoggerEvent;
import org.usefultoys.slf4j.utils.ConfigParser;
import org.usefultoys.slf4jtestmock.AssertLogger;
import org.usefultoys.slf4jtestmock.Slf4jMock;
import org.usefultoys.slf4jtestmock.WithMockLogger;
import org.usefultoys.test.ResetReporterConfig;
import org.usefultoys.test.ValidateCharset;
import org.usefultoys.test.WithLocale;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ReportServlet}.
 * <p>
 * Tests verify that ReportServlet correctly handles HTTP requests for various report types,
 * returns appropriate status codes, and logs reports using the configured logger.
 * Uses Jakarta Servlet API (Servlet 5.0+).
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>HTTP Request Handling:</b> Verifies proper handling of HTTP requests for different report types</li>
 *   <li><b>Status Code Responses:</b> Tests appropriate HTTP status codes for valid and invalid requests</li>
 *   <li><b>Report Generation:</b> Verifies generation of VM, filesystem, memory, user, physical system, OS, calendar, locale, charset, and network interface reports</li>
 *   <li><b>Path Handling:</b> Tests handling of valid paths, unknown paths, and missing path info</li>
 *   <li><b>Logger Integration:</b> Ensures reports are logged using the correct logger instances</li>
 * </ul>
 */
@DisplayName("ReportServlet")
@ValidateCharset
@ResetReporterConfig
@WithLocale("en")
@WithMockLogger
class ReportServletTest {

    private ReportServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private StringWriter responseWriter;

    @Slf4jMock("test.report")
    private Logger reportLogger; // Logger used by the reports themselves
    @Slf4jMock(type=ReportServlet.class)
    private Logger servletLogger; // Logger used by the servlet

    @BeforeEach
    void setUp() throws IOException {
        // Configure ReporterConfig to use a known logger name for reports
        System.setProperty(ReporterConfig.PROP_NAME, "test.report");
        ReporterConfig.init();

        servlet = new ReportServlet();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    @Test
    @DisplayName("should return not found when no path info")
    void shouldReturnNotFoundWhenNoPathInfo() throws IOException {
        // Given: request with no path info
        when(request.getPathInfo()).thenReturn(null);

        // When: servlet processes GET request
        servlet.doGet(request, response);

        // Then: should return 404 and log warning
        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        verify(response).setContentType("text/plain");
        assertTrue(responseWriter.toString().contains("No report path provided."));
        AssertLogger.assertEvent(servletLogger, 0, MockLoggerEvent.Level.WARN, "No report path provided.");
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    @DisplayName("should return not found when unknown path")
    void shouldReturnNotFoundWhenUnknownPath() throws IOException {
        // Given: request with unknown path
        when(request.getPathInfo()).thenReturn("/unknown");

        // When: servlet processes GET request
        servlet.doGet(request, response);

        // Then: should return 404 and log warning
        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        verify(response).setContentType("text/plain");
        assertTrue(responseWriter.toString().contains("Unknown report path: unknown"));
        AssertLogger.assertEvent(servletLogger, 0, MockLoggerEvent.Level.WARN, "Unrecognized report path: unknown");
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    @DisplayName("should generate VM report")
    void shouldGenerateVMReport() throws IOException {
        // Given: request for VM report
        when(request.getPathInfo()).thenReturn("/VM");

        // When: servlet processes GET request
        servlet.doGet(request, response);

        // Then: should return success and log VM report
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: vm"));
        AssertLogger.assertEvent(reportLogger, 0, MockLoggerEvent.Level.INFO, "Java Virtual Machine");
    }

    @Test
    @DisplayName("should generate file system report")
    void shouldGenerateFileSystemReport() throws IOException {
        // Given: request for file system report
        when(request.getPathInfo()).thenReturn("/FileSystem");

        // When: servlet processes GET request
        servlet.doGet(request, response);

        // Then: should return success and log file system report
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: filesystem"));
        AssertLogger.assertEvent(reportLogger, 0, MockLoggerEvent.Level.INFO, "File system root:");
    }

    @Test
    @DisplayName("should generate memory report")
    void shouldGenerateMemoryReport() throws IOException {
        // Given: request for memory report
        when(request.getPathInfo()).thenReturn("/Memory");

        // When: servlet processes GET request
        servlet.doGet(request, response);

        // Then: should return success and log memory report
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: memory"));
        AssertLogger.assertEvent(reportLogger, 0, MockLoggerEvent.Level.INFO, "Memory:");
    }

    @Test
    @DisplayName("should generate user report")
    void shouldGenerateUserReport() throws IOException {
        // Given: request for user report
        when(request.getPathInfo()).thenReturn("/User");

        // When: servlet processes GET request
        servlet.doGet(request, response);

        // Then: should return success and log user report
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: user"));
        AssertLogger.assertEvent(reportLogger, 0, MockLoggerEvent.Level.INFO, "User:");
    }

    @Test
    @DisplayName("should generate physical system report")
    void shouldGeneratePhysicalSystemReport() throws IOException {
        // Given: request for physical system report
        when(request.getPathInfo()).thenReturn("/PhysicalSystem");

        // When: servlet processes GET request
        servlet.doGet(request, response);

        // Then: should return success and log physical system report
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: physicalsystem"));
        AssertLogger.assertEvent(reportLogger, 0, MockLoggerEvent.Level.INFO, "Physical system");
    }

    @Test
    @DisplayName("should generate operating system report")
    void shouldGenerateOperatingSystemReport() throws IOException {
        // Given: request for operating system report
        when(request.getPathInfo()).thenReturn("/OperatingSystem");

        // When: servlet processes GET request
        servlet.doGet(request, response);

        // Then: should return success and log operating system report
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: operatingsystem"));
        AssertLogger.assertEvent(reportLogger, 0, MockLoggerEvent.Level.INFO, "Operating System");
    }

    @Test
    @DisplayName("should generate calendar report")
    void shouldGenerateCalendarReport() throws IOException {
        // Given: request for calendar report
        when(request.getPathInfo()).thenReturn("/Calendar");

        // When: servlet processes GET request
        servlet.doGet(request, response);

        // Then: should return success and log calendar report
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: calendar"));
        AssertLogger.assertEvent(reportLogger, 0, MockLoggerEvent.Level.INFO, "Calendar");
    }

    @Test
    @DisplayName("should generate locale report")
    void shouldGenerateLocaleReport() throws IOException {
        // Given: request for locale report
        when(request.getPathInfo()).thenReturn("/Locale");

        // When: servlet processes GET request
        servlet.doGet(request, response);

        // Then: should return success and log locale report
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: locale"));
        AssertLogger.assertEvent(reportLogger, 0, MockLoggerEvent.Level.INFO, "Locale");
    }

    @Test
    @DisplayName("should generate charset report")
    void shouldGenerateCharsetReport() throws IOException {
        // Given: request for charset report
        when(request.getPathInfo()).thenReturn("/Charset");

        // When: servlet processes GET request
        servlet.doGet(request, response);

        // Then: should return success and log charset report
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: charset"));
        AssertLogger.assertEvent(reportLogger, 0, MockLoggerEvent.Level.INFO, "Charset");
    }

    @Test
    @DisplayName("should generate network interface report")
    void shouldGenerateNetworkInterfaceReport() throws IOException {
        // Given: request for NetworkInterface report
        // Note: Mocking NetworkInterface.getNetworkInterfaces() is complex.
        // This test primarily checks if the servlet attempts to run the report.

        // Given: request for network interface report
        when(request.getPathInfo()).thenReturn("/NetworkInterface");

        // When: servlet processes GET request
        servlet.doGet(request, response);

        // Then: should return success
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: networkinterface"));
        // Cannot easily assert specific content without mocking NetworkInterface
        assertTrue(ConfigParser.isInitializationOK(), "No ConfigParser errors expected: " + ConfigParser.initializationErrors);
    }

    @Test
    @DisplayName("should generate SSL context report")
    void shouldGenerateSSLContextReport() throws IOException {
        // Given: request for SSL context report
        when(request.getPathInfo()).thenReturn("/SSLContext");

        // When: servlet processes GET request
        servlet.doGet(request, response);

        // Then: should return success and log SSL context report
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: sslcontext"));
        AssertLogger.assertEvent(reportLogger, 0, MockLoggerEvent.Level.INFO, "SSL Context");
    }

    @Test
    @DisplayName("should generate default trust keystore report")
    void shouldGenerateDefaultTrustKeyStoreReport() throws IOException {
        // Given: request for default trust keystore report
        when(request.getPathInfo()).thenReturn("/DefaultTrustKeyStore");

        // When: servlet processes GET request
        servlet.doGet(request, response);

        // Then: should return success and log trust keystore report
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: defaulttrustkeystore"));
        AssertLogger.assertEvent(reportLogger, 0, MockLoggerEvent.Level.INFO, "Trust Keystore");
    }

    @Test
    @DisplayName("should generate environment report")
    void shouldGenerateEnvironmentReport() throws IOException {
        // Given: request for environment report
        when(request.getPathInfo()).thenReturn("/Environment");

        // When: servlet processes GET request
        servlet.doGet(request, response);

        // Then: should return success and log environment report
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: environment"));
        AssertLogger.assertEvent(reportLogger, 0, MockLoggerEvent.Level.INFO, "System Environment:");
    }

    @Test
    @DisplayName("should generate properties report")
    void shouldGeneratePropertiesReport() throws IOException {
        // Given: request for properties report
        when(request.getPathInfo()).thenReturn("/Properties");

        // When: servlet processes GET request
        servlet.doGet(request, response);

        // Then: should return success and log properties report
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: properties"));
        AssertLogger.assertEvent(reportLogger, 0, MockLoggerEvent.Level.INFO, "System Properties:");
    }

    @Test
    @DisplayName("should generate JVM arguments report")
    void shouldGenerateJvmArgumentsReport() throws IOException {
        // Given: request for JVM arguments report
        when(request.getPathInfo()).thenReturn("/JvmArguments");

        // When: servlet processes GET request
        servlet.doGet(request, response);

        // Then: should return success and log JVM arguments report
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: jvmarguments"));
        AssertLogger.assertEvent(reportLogger, 0, MockLoggerEvent.Level.INFO, "JVM Arguments:");
    }

    @Test
    @DisplayName("should generate classpath report")
    void shouldGenerateClasspathReport() throws IOException {
        // Given: request for classpath report
        when(request.getPathInfo()).thenReturn("/Classpath");

        // When: servlet processes GET request
        servlet.doGet(request, response);

        // Then: should return success and log classpath report
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: classpath"));
        AssertLogger.assertEvent(reportLogger, 0, MockLoggerEvent.Level.INFO, "Classpath:");
    }

    @Test
    @DisplayName("should generate garbage collector report")
    void shouldGenerateGarbageCollectorReport() throws IOException {
        // Given: request for garbage collector report
        when(request.getPathInfo()).thenReturn("/GarbageCollector");

        // When: servlet processes GET request
        servlet.doGet(request, response);

        // Then: should return success and log garbage collector report
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: garbagecollector"));
        AssertLogger.assertEvent(reportLogger, 0, MockLoggerEvent.Level.INFO, "Garbage Collectors:");
    }
}
