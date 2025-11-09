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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.MockLogger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class ReportServletTest {

    private ReportServlet servlet;
    private HttpServletRequest mockRequest;
    private HttpServletResponse mockResponse;
    private StringWriter responseWriter;
    private MockLogger mockLogger;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new ReportServlet();
        mockRequest = mock(HttpServletRequest.class);
        mockResponse = mock(HttpServletResponse.class);
        responseWriter = new StringWriter();
        when(mockResponse.getWriter()).thenReturn(new PrintWriter(responseWriter));

        // Correctly get the logger instance used by ReportServlet (which uses @Slf4j)
        final Logger logger = LoggerFactory.getLogger(ReportServlet.class);
        mockLogger = (MockLogger) logger;
        mockLogger.clearEvents();
    }

    @Test
    void testDoGet_NullPathInfo() throws Exception {
        when(mockRequest.getPathInfo()).thenReturn(null);

        servlet.doGet(mockRequest, mockResponse);

        verify(mockResponse).setStatus(HttpServletResponse.SC_NOT_FOUND);
        verify(mockResponse).setContentType("text/plain");
        assertTrue(responseWriter.toString().contains("No report path provided."));
        assertTrue(mockLogger.getEventCount() > 0);
        assertTrue(mockLogger.getEvent(0).getFormattedMessage().contains("No report path provided."));
    }

    @Test
    void testDoGet_UnrecognizedPathInfo() throws Exception {
        when(mockRequest.getPathInfo()).thenReturn("/unknown");

        servlet.doGet(mockRequest, mockResponse);

        verify(mockResponse).setStatus(HttpServletResponse.SC_NOT_FOUND);
        verify(mockResponse).setContentType("text/plain");
        assertTrue(responseWriter.toString().contains("Unknown report path: unknown"));
        assertTrue(mockLogger.getEventCount() > 0);
        assertTrue(mockLogger.getEvent(0).getFormattedMessage().contains("Unrecognized report path: unknown"));
    }

    @Test
    void testDoGet_ReportVM() throws Exception {
        when(mockRequest.getPathInfo()).thenReturn("/VM");

        try (MockedConstruction<ReportVM> mocked = mockConstruction(ReportVM.class)) {
            servlet.doGet(mockRequest, mockResponse);
            verify(mocked.constructed().get(0)).run();
        }

        verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: vm"));
    }

    @Test
    void testDoGet_ReportFileSystem() throws Exception {
        when(mockRequest.getPathInfo()).thenReturn("/FileSystem");

        try (MockedConstruction<ReportFileSystem> mocked = mockConstruction(ReportFileSystem.class)) {
            servlet.doGet(mockRequest, mockResponse);
            verify(mocked.constructed().get(0)).run();
        }

        verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: filesystem"));
    }

    @Test
    void testDoGet_ReportMemory() throws Exception {
        when(mockRequest.getPathInfo()).thenReturn("/Memory");

        try (MockedConstruction<ReportMemory> mocked = mockConstruction(ReportMemory.class)) {
            servlet.doGet(mockRequest, mockResponse);
            verify(mocked.constructed().get(0)).run();
        }

        verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: memory"));
    }

    @Test
    void testDoGet_ReportUser() throws Exception {
        when(mockRequest.getPathInfo()).thenReturn("/User");

        try (MockedConstruction<ReportUser> mocked = mockConstruction(ReportUser.class)) {
            servlet.doGet(mockRequest, mockResponse);
            verify(mocked.constructed().get(0)).run();
        }

        verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: user"));
    }

    @Test
    void testDoGet_ReportPhysicalSystem() throws Exception {
        when(mockRequest.getPathInfo()).thenReturn("/PhysicalSystem");

        try (MockedConstruction<ReportPhysicalSystem> mocked = mockConstruction(ReportPhysicalSystem.class)) {
            servlet.doGet(mockRequest, mockResponse);
            verify(mocked.constructed().get(0)).run();
        }

        verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: physicalsystem"));
    }

    @Test
    void testDoGet_ReportOperatingSystem() throws Exception {
        when(mockRequest.getPathInfo()).thenReturn("/OperatingSystem");

        try (MockedConstruction<ReportOperatingSystem> mocked = mockConstruction(ReportOperatingSystem.class)) {
            servlet.doGet(mockRequest, mockResponse);
            verify(mocked.constructed().get(0)).run();
        }

        verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: operatingsystem"));
    }

    @Test
    void testDoGet_ReportCalendar() throws Exception {
        when(mockRequest.getPathInfo()).thenReturn("/Calendar");

        try (MockedConstruction<ReportCalendar> mocked = mockConstruction(ReportCalendar.class)) {
            servlet.doGet(mockRequest, mockResponse);
            verify(mocked.constructed().get(0)).run();
        }

        verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: calendar"));
    }

    @Test
    void testDoGet_ReportLocale() throws Exception {
        when(mockRequest.getPathInfo()).thenReturn("/Locale");

        try (MockedConstruction<ReportLocale> mocked = mockConstruction(ReportLocale.class)) {
            servlet.doGet(mockRequest, mockResponse);
            verify(mocked.constructed().get(0)).run();
        }

        verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: locale"));
    }

    @Test
    void testDoGet_ReportCharset() throws Exception {
        when(mockRequest.getPathInfo()).thenReturn("/Charset");

        try (MockedConstruction<ReportCharset> mocked = mockConstruction(ReportCharset.class)) {
            servlet.doGet(mockRequest, mockResponse);
            verify(mocked.constructed().get(0)).run();
        }

        verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: charset"));
    }

    @Test
    void testDoGet_ReportSSLContext() throws Exception {
        when(mockRequest.getPathInfo()).thenReturn("/SSLContext");

        try (MockedConstruction<ReportSSLContext> mocked = mockConstruction(ReportSSLContext.class)) {
            servlet.doGet(mockRequest, mockResponse);
            verify(mocked.constructed().get(0)).run();
        }

        verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: sslcontext"));
    }

    @Test
    void testDoGet_ReportDefaultTrustKeyStore() throws Exception {
        when(mockRequest.getPathInfo()).thenReturn("/DefaultTrustKeyStore");

        try (MockedConstruction<ReportDefaultTrustKeyStore> mocked = mockConstruction(ReportDefaultTrustKeyStore.class)) {
            servlet.doGet(mockRequest, mockResponse);
            verify(mocked.constructed().get(0)).run();
        }

        verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: defaulttrustkeystore"));
    }

    @Test
    void testDoGet_ReportNetworkInterface_SocketException() throws Exception {
        when(mockRequest.getPathInfo()).thenReturn("/NetworkInterface");

        try (MockedStatic<NetworkInterface> mockedStatic = mockStatic(NetworkInterface.class)) {
            mockedStatic.when(NetworkInterface::getNetworkInterfaces).thenThrow(new SocketException("Mock SocketException"));

            servlet.doGet(mockRequest, mockResponse);

            assertTrue(mockLogger.getEventCount() > 0);
            // Check if any log event contains the expected message
            boolean found = mockLogger.getLoggerEvents().stream()
                    .anyMatch(event -> event.getFormattedMessage().contains("Cannot report network interface"));
            assertTrue(found, "Expected log message 'Cannot report network interface' not found.");
        }

        verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: networkinterface"));
    }

    @Test
    void testDoGet_ReportNetworkInterface_MultipleInterfaces() throws Exception {
        when(mockRequest.getPathInfo()).thenReturn("/NetworkInterface");

        NetworkInterface mockNif1 = mock(NetworkInterface.class);
        NetworkInterface mockNif2 = mock(NetworkInterface.class);
        Enumeration<NetworkInterface> mockEnumeration = Collections.enumeration(java.util.Arrays.asList(mockNif1, mockNif2));

        try (MockedStatic<NetworkInterface> mockedStatic = mockStatic(NetworkInterface.class)) {
            mockedStatic.when(NetworkInterface::getNetworkInterfaces).thenReturn(mockEnumeration);

            try (MockedConstruction<ReportNetworkInterface> mocked = mockConstruction(ReportNetworkInterface.class)) {
                servlet.doGet(mockRequest, mockResponse);

                // Verify that ReportNetworkInterface was constructed twice and run() called on each
                verify(mocked.constructed().get(0)).run();
                verify(mocked.constructed().get(1)).run();
            }
        }

        verify(mockResponse).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Report logged for: networkinterface"));
    }
}
