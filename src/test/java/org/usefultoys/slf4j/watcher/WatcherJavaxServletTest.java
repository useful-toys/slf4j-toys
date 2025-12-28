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

import org.junit.jupiter.api.Test;
import org.slf4j.impl.MockLogger;
import org.slf4j.impl.MockLoggerEvent;
import org.usefultoys.slf4jtestmock.AssertLogger;
import org.usefultoys.slf4jtestmock.Slf4jMock;
import org.usefultoys.slf4jtestmock.WithMockLogger;
import org.usefultoys.slf4jtestmock.WithMockLoggerDebug;
import org.usefultoys.test.ResetWatcherConfig;
import org.usefultoys.test.ValidateCharset;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Unit tests for {@link WatcherJavaxServlet}.
 * <p>
 * Tests validate that WatcherJavaxServlet correctly handles HTTP requests, logs runtime state,
 * and handles exceptions appropriately. Uses javax.servlet API (Java EE / Jakarta EE 8 and earlier).
 */
@ValidateCharset
@ResetWatcherConfig
@WithMockLogger
@WithMockLoggerDebug
class WatcherJavaxServletTest {

    @Slf4jMock(type = WatcherJavaxServlet.class)
    private MockLogger mockLogger;
    @Slf4jMock("watcher") // default value of WatcherConfig.name
    private MockLogger watcherLogger;

    @Test
    void shouldLogSystemStatusSuccessfully() throws IOException {
        // Given: a WatcherJavaxServlet with enabled loggers
        final WatcherJavaxServlet servlet = new WatcherJavaxServlet();
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final StringWriter responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        // When: doGet is called
        servlet.doGet(request, response);

        // Then: response should be successful with logged state
        verify(response).setContentType("text/plain");
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertEquals("Runtime state logged successfully.", responseWriter.toString().trim());
        AssertLogger.assertEvent(mockLogger, 0, MockLoggerEvent.Level.INFO, "WatcherJavaxServlet accessed");
        AssertLogger.assertEvent(watcherLogger, 0, MockLoggerEvent.Level.INFO, "Memory:");
    }

    /**
     * Helper servlet that throws exceptions in runWatcher for testing error handling
     */
    static class ExceptionThrowingWatcherJavaxServlet extends WatcherJavaxServlet {
        private final RuntimeException exceptionToThrow;

        ExceptionThrowingWatcherJavaxServlet(final RuntimeException exceptionToThrow) {
            this.exceptionToThrow = exceptionToThrow;
        }

        @Override
        protected void runWatcher() {
            throw exceptionToThrow;
        }
    }

    @Test
    void shouldHandleExceptionInRunWatcher() throws Exception {
        // Given: a servlet that throws a RuntimeException
        final RuntimeException testException = new RuntimeException("Teste de falha no watcher");
        final WatcherJavaxServlet servlet = new ExceptionThrowingWatcherJavaxServlet(testException);
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final StringWriter responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        // When: doGet is called
        servlet.doGet(request, response);

        // Then: response should be error with logged exception
        verify(response).setContentType("text/plain");
        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        assertEquals("Failed to log runtime state.", responseWriter.toString().trim());
        AssertLogger.assertEventWithThrowable(mockLogger, 0, RuntimeException.class, "Teste de falha no watcher");
    }

    @Test
    void shouldHandleIOExceptionWhenWritingToResponse() throws Exception {
        // Given: a servlet that throws exception and response that throws IOException
        final RuntimeException testException = new RuntimeException("Teste de falha no watcher");
        final WatcherJavaxServlet servlet = new ExceptionThrowingWatcherJavaxServlet(testException);
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getWriter()).thenThrow(new IOException("Erro ao escrever na resposta"));

        // When: doGet is called
        servlet.doGet(request, response);

        // Then: response should be error with logged exception
        verify(response).setContentType("text/plain");
        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        AssertLogger.assertEventWithThrowable(mockLogger, 0, RuntimeException.class, "Teste de falha no watcher");
    }

    @Test
    void shouldHandleRuntimeExceptionInRunWatcher() throws Exception {
        // Given: a servlet that throws RuntimeException
        final RuntimeException testException = new RuntimeException("Teste de exceção simulada");
        final WatcherJavaxServlet servlet = new ExceptionThrowingWatcherJavaxServlet(testException);
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final StringWriter responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        // When: doGet is called
        servlet.doGet(request, response);

        // Then: response should be error with logged exception
        verify(response).setContentType("text/plain");
        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        assertEquals("Failed to log runtime state.", responseWriter.toString().trim());
        AssertLogger.assertEventWithThrowable(mockLogger, 0, RuntimeException.class, "Teste de exceção simulada");
    }

    @Test
    void shouldHandleNullPointerExceptionInRunWatcher() throws Exception {
        // Given: a servlet that throws NullPointerException
        final NullPointerException testException = new NullPointerException("Erro de referência nula");
        final WatcherJavaxServlet servlet = new ExceptionThrowingWatcherJavaxServlet(testException);
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final StringWriter responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        // When: doGet is called
        servlet.doGet(request, response);

        // Then: response should be error with logged exception
        verify(response).setContentType("text/plain");
        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        assertEquals("Failed to log runtime state.", responseWriter.toString().trim());
        AssertLogger.assertEventWithThrowable(mockLogger, 0, NullPointerException.class, "Erro de referência nula");
    }

    @Test
    void shouldHandleWriterExceptionInErrorCase() throws Exception {
        // Given: a servlet that throws exception and response that throws IOException
        final RuntimeException testException = new RuntimeException("Teste de exceção primária");
        final WatcherJavaxServlet servlet = new ExceptionThrowingWatcherJavaxServlet(testException);
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getWriter()).thenThrow(new IOException("Erro ao escrever na resposta"));

        // When: doGet is called
        servlet.doGet(request, response);

        // Then: response should be error with logged exception
        verify(response).setContentType("text/plain");
        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        AssertLogger.assertEventWithThrowable(mockLogger, 0, RuntimeException.class, "Teste de exceção primária");
    }

    @Test
    void shouldHandleIllegalStateExceptionInRunWatcher() throws Exception {
        // Given: a servlet that throws IllegalStateException
        final IllegalStateException testException = new IllegalStateException("Estado inválido do watcher");
        final WatcherJavaxServlet servlet = new ExceptionThrowingWatcherJavaxServlet(testException);
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final StringWriter responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        // When: doGet is called
        servlet.doGet(request, response);

        // Then: response should be error with logged exception
        verify(response).setContentType("text/plain");
        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        assertEquals("Failed to log runtime state.", responseWriter.toString().trim());
        AssertLogger.assertEventWithThrowable(mockLogger, 0, IllegalStateException.class, "Estado inválido do watcher");
    }

}
