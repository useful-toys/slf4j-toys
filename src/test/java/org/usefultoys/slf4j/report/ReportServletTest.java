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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.impl.MockLoggerEvent;
import org.usefultoys.slf4j.utils.ConfigParser;
import org.usefultoys.slf4jtestmock.AssertLogger;
import org.usefultoys.slf4jtestmock.MockLoggerExtension;
import org.usefultoys.slf4jtestmock.Slf4jMock;
import org.usefultoys.test.CharsetConsistencyExtension;
import org.usefultoys.test.ResetReporterConfigExtension;
import org.usefultoys.test.WithLocale;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith({CharsetConsistencyExtension.class, ResetReporterConfigExtension.class, MockLoggerExtension.class})
@WithLocale("en")
class ReportServletTest {

    private ReportServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private StringWriter responseWriter;

    @Slf4jMock("test.report")
    private Logger reportLogger; // Logger used by the reports themselves
    @Slf4jMock(type=ReportServlet.class)
    private Logger servletLogger; // Logger used by the servlet (Slf4j annotation)

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

    @AfterEach
    void tearDown() {
        System.clearProperty(ReporterConfig.PROP_NAME);
    }

    @Test
    void testNoPathInfoReturnsNotFound() throws IOException {
        when(request.getPathInfo()).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        verify(response).setContentType("text/plain");
        assertTrue(responseWriter.toString().contains("No report path provided."));
        AssertLogger.assertEvent(servletLogger, 0, MockLoggerEvent.Level.WARN, "No report path provided.");
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    void testUnknownPathInfoReturnsNotFound() throws IOException {
        when(request.getPathInfo()).thenReturn("/unknown");

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        verify(response).setContentType("text/plain");
        assertTrue(responseWriter.toString().contains("Unknown report path: unknown"));
        AssertLogger.assertEvent(servletLogger, 0, MockLoggerEvent.Level.WARN, "Unrecognized report path: unknown");
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    void testVMReport() throws IOException {
        when(request.getPathInfo()).thenReturn("/VM");
        servlet.doGet(request, response);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: vm"));
        AssertLogger.assertEvent(reportLogger,0,  MockLoggerEvent.Level.INFO, "Java Virtual Machine");
    }

    @Test
    void testFileSystemReport() throws IOException {
        when(request.getPathInfo()).thenReturn("/FileSystem");
        servlet.doGet(request, response);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: filesystem"));
        AssertLogger.assertEvent(reportLogger,0,  MockLoggerEvent.Level.INFO, "File system root:");
    }

    @Test
    void testMemoryReport() throws IOException {
        when(request.getPathInfo()).thenReturn("/Memory");
        servlet.doGet(request, response);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: memory"));
        AssertLogger.assertEvent(reportLogger, 0, MockLoggerEvent.Level.INFO, "Memory:");
    }

    @Test
    void testUserReport() throws IOException {
        when(request.getPathInfo()).thenReturn("/User");
        servlet.doGet(request, response);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: user"));
        AssertLogger.assertEvent(reportLogger,0,  MockLoggerEvent.Level.INFO, "User:");
    }

    @Test
    void testPhysicalSystemReport() throws IOException {
        when(request.getPathInfo()).thenReturn("/PhysicalSystem");
        servlet.doGet(request, response);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: physicalsystem"));
        AssertLogger.assertEvent(reportLogger, 0, MockLoggerEvent.Level.INFO, "Physical system");
    }

    @Test
    void testOperatingSystemReport() throws IOException {
        when(request.getPathInfo()).thenReturn("/OperatingSystem");
        servlet.doGet(request, response);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: operatingsystem"));
        AssertLogger.assertEvent(reportLogger, 0, MockLoggerEvent.Level.INFO, "Operating System");
    }

    @Test
    void testCalendarReport() throws IOException {
        when(request.getPathInfo()).thenReturn("/Calendar");
        servlet.doGet(request, response);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: calendar"));
        AssertLogger.assertEvent(reportLogger,0,  MockLoggerEvent.Level.INFO, "Calendar");
    }

    @Test
    void testLocaleReport() throws IOException {
        when(request.getPathInfo()).thenReturn("/Locale");
        servlet.doGet(request, response);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: locale"));
        AssertLogger.assertEvent(reportLogger, 0, MockLoggerEvent.Level.INFO, "Locale");
    }

    @Test
    void testCharsetReport() throws IOException {
        when(request.getPathInfo()).thenReturn("/Charset");
        servlet.doGet(request, response);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: charset"));
        AssertLogger.assertEvent(reportLogger, 0, MockLoggerEvent.Level.INFO, "Charset");
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
        AssertLogger.assertEvent(reportLogger, 0, MockLoggerEvent.Level.INFO, "SSL Context");
    }

    @Test
    void testDefaultTrustKeyStoreReport() throws IOException {
        when(request.getPathInfo()).thenReturn("/DefaultTrustKeyStore");
        servlet.doGet(request, response);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: defaulttrustkeystore"));
        AssertLogger.assertEvent(reportLogger, 0, MockLoggerEvent.Level.INFO, "Trust Keystore");
    }

    @Test
    void testEnvironmentReport() throws IOException {
        when(request.getPathInfo()).thenReturn("/Environment");
        servlet.doGet(request, response);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: environment"));
        AssertLogger.assertEvent(reportLogger, 0, MockLoggerEvent.Level.INFO, "System Environment:");
    }

    @Test
    void testPropertiesReport() throws IOException {
        when(request.getPathInfo()).thenReturn("/Properties");
        servlet.doGet(request, response);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: properties"));
        AssertLogger.assertEvent(reportLogger, 0, MockLoggerEvent.Level.INFO, "System Properties:");
    }

    @Test
    void testJvmArgumentsReport() throws IOException {
        when(request.getPathInfo()).thenReturn("/JvmArguments");
        servlet.doGet(request, response);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: jvmarguments"));
        AssertLogger.assertEvent(reportLogger, 0, MockLoggerEvent.Level.INFO, "JVM Arguments:");
    }

    @Test
    void testClasspathReport() throws IOException {
        when(request.getPathInfo()).thenReturn("/Classpath");
        servlet.doGet(request, response);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: classpath"));
        AssertLogger.assertEvent(reportLogger, 0, MockLoggerEvent.Level.INFO, "Classpath:");
    }

    @Test
    void testGarbageCollectorReport() throws IOException {
        when(request.getPathInfo()).thenReturn("/GarbageCollector");
        servlet.doGet(request, response);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: garbagecollector"));
        AssertLogger.assertEvent(reportLogger, 0, MockLoggerEvent.Level.INFO, "Garbage Collectors:");
    }
}
