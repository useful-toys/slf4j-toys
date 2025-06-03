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
import java.io.IOException;
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

    private final MockLogger mockLogger = (MockLogger) LoggerFactory.getLogger(WatcherServlet.class);
    private final MockLogger watcherLogger = (MockLogger) LoggerFactory.getLogger(WatcherConfig.name);

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

        assertEquals(1, mockLogger.getEventCount());
        assertTrue(mockLogger.getEvent(0).getFormattedMessage().contains("WatcherServlet accessed"));

        assertEquals(1, watcherLogger.getEventCount());
        assertTrue(watcherLogger.getEvent(0).getFormattedMessage().contains("Memory:"));
    }

    /**
     * Classe de teste que estende WatcherServlet para simular exceções
     */
    static class TestExceptionWatcherServlet extends WatcherServlet {
        private final RuntimeException exceptionToThrow;

        public TestExceptionWatcherServlet(final RuntimeException exceptionToThrow) {
            this.exceptionToThrow = exceptionToThrow;
        }

        @Override
        protected void runWatcher()  {
            throw exceptionToThrow;
        }
    }

    @Test
    void shouldHandleExceptionInRunWatcher() throws Exception {
        // Arrange
        final RuntimeException testException = new RuntimeException("Teste de falha no watcher");
        final WatcherServlet servlet = new TestExceptionWatcherServlet(testException);
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final StringWriter responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        // Act
        servlet.doGet(request, response);

        // Assert
        verify(response).setContentType("text/plain");
        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        assertEquals("Failed to log system status.", responseWriter.toString().trim());
        
        assertEquals(1, mockLogger.getEventCount());
        assertSame(mockLogger.getEvent(0).getThrowable(), testException);
        assertTrue(mockLogger.getEvent(0).getFormattedMessage().contains("Failed to log system status"));
    }

    @Test
    void shouldHandleIOExceptionWhenWritingToResponse() throws Exception {
        // Arrange
        final RuntimeException testException = new RuntimeException("Teste de falha no watcher");
        final WatcherServlet servlet = new TestExceptionWatcherServlet(testException);
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        
        // Simular exceção ao escrever na resposta
        when(response.getWriter()).thenThrow(new IOException("Erro ao escrever na resposta"));

        // Act
        servlet.doGet(request, response);

        // Assert
        verify(response).setContentType("text/plain");
        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        
        assertEquals(1, mockLogger.getEventCount());
        assertSame(mockLogger.getEvent(0).getThrowable(), testException);
        assertTrue(mockLogger.getEvent(0).getFormattedMessage().contains("Failed to log system status"));
    }

    /**
     * Classe estendida do WatcherServlet para simular exceções no método runWatcher
     */
    static class ExceptionThrowingWatcherServlet extends WatcherServlet {
        private final RuntimeException exceptionToThrow;

        public ExceptionThrowingWatcherServlet(final RuntimeException exceptionToThrow) {
            this.exceptionToThrow = exceptionToThrow;
        }

        @Override
        protected void runWatcher() {
            throw exceptionToThrow;
        }
    }

    @Test
    void shouldHandleRuntimeExceptionInRunWatcher() throws Exception {
        // Arrange
        final RuntimeException testException = new RuntimeException("Teste de exceção simulada");
        final WatcherServlet servlet = new ExceptionThrowingWatcherServlet(testException);
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final StringWriter responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        // Act
        servlet.doGet(request, response);

        // Assert
        verify(response).setContentType("text/plain");
        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        assertEquals("Failed to log system status.", responseWriter.toString().trim());

        assertEquals(1, mockLogger.getEventCount());
        assertSame(mockLogger.getEvent(0).getThrowable(), testException);
        assertTrue(mockLogger.getEvent(0).getFormattedMessage().contains("Failed to log system status"));
    }

    @Test
    void shouldHandleNullPointerExceptionInRunWatcher() throws Exception {
        // Arrange
        final NullPointerException testException = new NullPointerException("Erro de referência nula");
        final WatcherServlet servlet = new ExceptionThrowingWatcherServlet(testException);
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final StringWriter responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        // Act
        servlet.doGet(request, response);

        // Assert
        verify(response).setContentType("text/plain");
        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        assertEquals("Failed to log system status.", responseWriter.toString().trim());

        assertEquals(1, mockLogger.getEventCount());
        assertSame(mockLogger.getEvent(0).getThrowable(), testException);
        assertTrue(mockLogger.getEvent(0).getFormattedMessage().contains("Failed to log system status"));
    }

    @Test
    void shouldHandleWriterExceptionInErrorCase() throws Exception {
        // Arrange
        final RuntimeException testException = new RuntimeException("Teste de exceção primária");
        final WatcherServlet servlet = new ExceptionThrowingWatcherServlet(testException);
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);

        // Simular IOException ao tentar escrever na resposta
        when(response.getWriter()).thenThrow(new IOException("Erro ao escrever na resposta"));

        // Act
        servlet.doGet(request, response);

        // Assert
        verify(response).setContentType("text/plain");
        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        assertEquals(1, mockLogger.getEventCount());
        assertSame(mockLogger.getEvent(0).getThrowable(), testException);
        assertTrue(mockLogger.getEvent(0).getFormattedMessage().contains("Failed to log system status"));
    }

    @Test
    void shouldHandleIllegalStateExceptionInRunWatcher() throws Exception {
        // Arrange
        final IllegalStateException testException = new IllegalStateException("Estado inválido do watcher");
        final WatcherServlet servlet = new ExceptionThrowingWatcherServlet(testException);
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final StringWriter responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        // Act
        servlet.doGet(request, response);

        // Assert
        verify(response).setContentType("text/plain");
        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        assertEquals("Failed to log system status.", responseWriter.toString().trim());

        assertEquals(1, mockLogger.getEventCount());
        assertSame(mockLogger.getEvent(0).getThrowable(), testException);
        assertTrue(mockLogger.getEvent(0).getFormattedMessage().contains("Failed to log system status"));
    }

}
