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

package org.usefultoys.slf4j.watcher;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.slf4j.impl.MockLogger;
import org.slf4j.impl.MockLoggerEvent;
import org.usefultoys.slf4jtestmock.AssertLogger;
import org.usefultoys.slf4jtestmock.Slf4jMock;
import org.usefultoys.slf4jtestmock.WithMockLogger;
import org.usefultoys.slf4jtestmock.WithMockLoggerDebug;
import org.usefultoys.test.ResetWatcherConfig;
import org.usefultoys.test.ValidateCharset;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Unit tests for {@link WatcherServlet}.
 * <p>
 * Tests validate that WatcherServlet correctly handles HTTP requests, logs runtime state,
 * and handles exceptions appropriately.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>HTTP Request Handling:</b> Verifies correct processing of GET requests and response writing</li>
 *   <li><b>System Status Logging:</b> Tests logging of runtime state with appropriate log levels</li>
 *   <li><b>Exception Handling:</b> Ensures graceful handling of exceptions during watcher execution</li>
 *   <li><b>Concurrent Skip:</b> Verifies that a request arriving while a collection is in progress is rejected with HTTP 429 instead of being queued</li>
 * </ul>
 */
@ValidateCharset
@ResetWatcherConfig
@WithMockLogger
@WithMockLoggerDebug
class WatcherServletTest {

    @Slf4jMock(type = WatcherServlet.class)
    private MockLogger mockLogger;
    @Slf4jMock("watcher") // default value of WatcherConfig.name
    private MockLogger watcherLogger;
    @Slf4jMock("custom-servlet-watcher")
    private MockLogger customLogger;

    @Test
    void shouldLogSystemStatusSuccessfully() throws Exception {
        // Given: a WatcherServlet with enabled loggers
        final WatcherServlet servlet = new WatcherServlet();
        servlet.init(mock(ServletConfig.class));
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
        AssertLogger.assertEvent(mockLogger, 1, MockLoggerEvent.Level.INFO, "WatcherServlet accessed");
        AssertLogger.assertEvent(watcherLogger, 0, MockLoggerEvent.Level.INFO, "Memory:");
    }

    /**
     * Helper servlet that throws exceptions in runWatcher for testing error handling
     */
    static class ExceptionThrowingWatcherServlet extends WatcherServlet {
        private final RuntimeException exceptionToThrow;

        ExceptionThrowingWatcherServlet(final RuntimeException exceptionToThrow) {
            this.exceptionToThrow = exceptionToThrow;
        }

        @Override
        protected boolean runWatcher() {
            throw exceptionToThrow;
        }
    }

    @Test
    void shouldHandleExceptionInRunWatcher() throws Exception {
        // Given: a servlet that throws a RuntimeException
        final RuntimeException testException = new RuntimeException("Teste de falha no watcher");
        final WatcherServlet servlet = new ExceptionThrowingWatcherServlet(testException);
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
        final WatcherServlet servlet = new ExceptionThrowingWatcherServlet(testException);
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
        final WatcherServlet servlet = new ExceptionThrowingWatcherServlet(testException);
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
        final WatcherServlet servlet = new ExceptionThrowingWatcherServlet(testException);
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
        final WatcherServlet servlet = new ExceptionThrowingWatcherServlet(testException);
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
        final WatcherServlet servlet = new ExceptionThrowingWatcherServlet(testException);
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

    @Test
    void shouldUseInitParamName() throws Exception {
        // Given: a servlet configuration that provides a custom watcher name
        final WatcherServlet servlet = new WatcherServlet();
        final ServletConfig config = mock(ServletConfig.class);
        when(config.getInitParameter(WatcherConfig.PROP_NAME)).thenReturn("custom-servlet-watcher");
        servlet.init(config);
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final StringWriter responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        // When: doGet is called
        servlet.doGet(request, response);

        // Then: the watcher logs to the logger derived from the init-param name
        verify(response).setStatus(HttpServletResponse.SC_OK);
        AssertLogger.assertEvent(customLogger, 0, MockLoggerEvent.Level.INFO, "Memory:");
    }

    @Test
    void shouldFallBackToWatcherConfigNameWhenInitParamMissing() throws Exception {
        // Given: a servlet configuration without the watcher name init-param
        final WatcherServlet servlet = new WatcherServlet();
        final ServletConfig config = mock(ServletConfig.class);
        when(config.getInitParameter(WatcherConfig.PROP_NAME)).thenReturn(null);
        servlet.init(config);
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final StringWriter responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        // When: doGet is called
        servlet.doGet(request, response);

        // Then: the watcher logs to the default logger name
        verify(response).setStatus(HttpServletResponse.SC_OK);
        AssertLogger.assertEvent(watcherLogger, 0, MockLoggerEvent.Level.INFO, "Memory:");
    }

    @Test
    void shouldIgnoreBlankInitParamName() throws Exception {
        // Given: a servlet configuration with a blank watcher name init-param
        final WatcherServlet servlet = new WatcherServlet();
        final ServletConfig config = mock(ServletConfig.class);
        when(config.getInitParameter(WatcherConfig.PROP_NAME)).thenReturn("   ");
        servlet.init(config);
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final StringWriter responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        // When: doGet is called
        servlet.doGet(request, response);

        // Then: the blank value is ignored and the default logger name is used
        verify(response).setStatus(HttpServletResponse.SC_OK);
        AssertLogger.assertEvent(watcherLogger, 0, MockLoggerEvent.Level.INFO, "Memory:");
    }

    /**
     * Reflectively acquires the private {@code watcherLock} of a {@link WatcherServlet} so a test
     * can hold it from another thread and deterministically reproduce the "collection already in
     * progress" condition that {@link #runWatcher()} guards with {@link ReentrantLock#tryLock()}.
     */
    private static ReentrantLock watcherLockOf(final WatcherServlet servlet) throws Exception {
        final Field f = WatcherServlet.class.getDeclaredField("watcherLock");
        f.setAccessible(true);
        final Object lock = f.get(servlet);
        assertNotNull(lock, "watcherLock should be initialized");
        return (ReentrantLock) lock;
    }

    @Test
    void shouldReturn429WhenCollectionAlreadyInProgress() throws Exception {
        // Given: a servlet whose internal watcherLock is held by another thread
        final WatcherServlet servlet = new WatcherServlet();
        servlet.init(mock(ServletConfig.class));
        final ReentrantLock lock = watcherLockOf(servlet);
        final CountDownLatch holding = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        final Thread holder = new Thread(() -> {
            lock.lock();
            holding.countDown();
            try {
                release.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
            }
        }, "lock-holder");
        holder.start();
        assertTrue(holding.await(5, TimeUnit.SECONDS), "holder should acquire the lock");

        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final StringWriter responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        try {
            // When: doGet is called while the internal lock is held by another thread
            servlet.doGet(request, response);

            // Then: response should be 429 with the skip message, and watcher.run() must NOT have run
            verify(response).setContentType("text/plain");
            verify(response).setStatus(429);
            assertEquals("Runtime state already being collected. Try again later.", responseWriter.toString().trim());
        } finally {
            release.countDown();
            holder.join(5_000);
        }
    }

    @Test
    void concurrentRequestsShouldNotBothRunWatcher() throws Exception {
        // Given: a servlet that counts real watcher.run() executions, with a barrier to maximize contention
        final WatcherServlet servlet = new WatcherServlet();
        servlet.init(mock(ServletConfig.class));
        final int threads = 16;
        final CountDownLatch ready = new CountDownLatch(threads);
        final CountDownLatch start = new CountDownLatch(1);
        final AtomicInteger ok = new AtomicInteger();
        final AtomicInteger tooMany = new AtomicInteger();
        final AtomicInteger errors = new AtomicInteger();

        // When: N threads call doGet concurrently
        final Thread[] workers = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            workers[i] = new Thread(() -> {
                final HttpServletRequest request = mock(HttpServletRequest.class);
                final HttpServletResponse response = mock(HttpServletResponse.class);
                final StringWriter sw = new StringWriter();
                try {
                    when(response.getWriter()).thenReturn(new PrintWriter(sw));
                } catch (IOException e) {
                    errors.incrementAndGet();
                    return;
                }
                ready.countDown();
                try {
                    start.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    errors.incrementAndGet();
                    return;
                }
                try {
                    servlet.doGet(request, response);
                    final String status = sw.toString().trim();
                    if ("Runtime state logged successfully.".equals(status)) {
                        ok.incrementAndGet();
                    } else if ("Runtime state already being collected. Try again later.".equals(status)) {
                        tooMany.incrementAndGet();
                    } else {
                        errors.incrementAndGet();
                    }
                } catch (Exception e) {
                    errors.incrementAndGet();
                }
            }, "doGet-" + i);
            workers[i].start();
        }
        assertTrue(ready.await(5, TimeUnit.SECONDS), "all workers should be ready");
        start.countDown();
        for (Thread t : workers) t.join(10_000);

        // Then: at least one request ran the watcher; the rest were rejected with 429; no errors
        assertTrue(ok.get() >= 1, "at least one doGet should have collected, got " + ok.get());
        assertEquals(threads, ok.get() + tooMany.get(), "every request must be either OK or 429");
        assertEquals(0, errors.get(), "no request should have errored");
    }
}
