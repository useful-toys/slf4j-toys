package org.usefultoys.slf4j.report;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.TestLogger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.usefultoys.slf4j.SessionConfig;

class ReportServletTest {
    private ReportServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private StringWriter responseWriter;
    private TestLogger reportLogger;
    private TestLogger servletLogger;

    @BeforeAll
    static void validate() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    @BeforeEach
    void setUp() throws Exception {
        servlet = new ReportServlet();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);

        responseWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(writer);

        reportLogger = (TestLogger) (TestLogger) LoggerFactory.getLogger(ReporterConfig.name);
        reportLogger.clearEvents();
        servletLogger = (TestLogger) LoggerFactory.getLogger(ReportServlet.class);
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
        String fullLog = reportLogger.getEvent(0).getFormattedMessage();
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
    void shouldRespondWithOkAndLogReport(String path) throws Exception {
        // Arrange
        ReportServlet servlet = new ReportServlet();

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getPathInfo()).thenReturn(path);

        StringWriter responseWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(writer);

        TestLogger logger = (TestLogger) LoggerFactory.getLogger("org.usefultoys.slf4j.report.ReportServlet");
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
