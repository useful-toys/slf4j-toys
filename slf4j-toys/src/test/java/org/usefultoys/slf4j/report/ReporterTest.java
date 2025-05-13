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
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.MockLogger;
import org.usefultoys.slf4j.SessionConfig;
import org.usefultoys.slf4j.SystemConfig;

import java.nio.charset.Charset;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class ReporterTest {

    private MockLogger mockLogger;

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
    void setUp() {
        final Logger logger = LoggerFactory.getLogger("test.report.charset");
        mockLogger = (MockLogger) logger;
        mockLogger.clearEvents();
    }

    @Test
    void shouldSubmitEnabledReportsToExecutor0() {
        // Arrange: mock do Executor
        ReporterConfig.reportVM = false;
        ReporterConfig.reportFileSystem = false;
        ReporterConfig.reportMemory = false;
        ReporterConfig.reportUser = false;
        ReporterConfig.reportProperties = false;
        ReporterConfig.reportEnvironment = false;
        ReporterConfig.reportPhysicalSystem = false;
        ReporterConfig.reportOperatingSystem = false;
        ReporterConfig.reportCalendar = false;
        ReporterConfig.reportLocale = false;
        ReporterConfig.reportCharset = false;
        ReporterConfig.reportNetworkInterface = false;
        ReporterConfig.reportSSLContext = false;
        ReporterConfig.reportDefaultTrustKeyStore = false;

        final Reporter reporter = new Reporter(mockLogger);
        final Executor executor = mock(Executor.class);
        final ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);

        // Act
        reporter.logDefaultReports(executor);

        // Assert: garante que o executor foi chamado duas vezes
        verify(executor, times(0)).execute(captor.capture());
    }

    @Test
    void shouldSubmitEnabledReportsToExecutor1() {
        // Arrange: mock do Executor
        ReporterConfig.reportVM = true;
        ReporterConfig.reportFileSystem = true;
        ReporterConfig.reportMemory = true;
        ReporterConfig.reportUser = true;
        ReporterConfig.reportProperties = false;
        ReporterConfig.reportEnvironment = false;
        ReporterConfig.reportPhysicalSystem = false;
        ReporterConfig.reportOperatingSystem = false;
        ReporterConfig.reportCalendar = false;
        ReporterConfig.reportLocale = false;
        ReporterConfig.reportCharset = false;
        ReporterConfig.reportNetworkInterface = false;
        ReporterConfig.reportSSLContext = false;
        ReporterConfig.reportDefaultTrustKeyStore = false;

        final Reporter reporter = new Reporter(mockLogger);
        final Executor executor = mock(Executor.class);
        final ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);

        // Act
        reporter.logDefaultReports(executor);

        // Assert: garante que o executor foi chamado duas vezes
        verify(executor, times(4)).execute(captor.capture());

        // Verifica que os Runnables são das classes esperadas
        final boolean foundVM = captor.getAllValues().stream().anyMatch(r -> r.getClass().getSimpleName().equals("ReportVM"));
        final boolean foundFileSystem = captor.getAllValues().stream().anyMatch(r -> r.getClass().getSimpleName().equals("ReportFileSystem"));
        final boolean foundUser = captor.getAllValues().stream().anyMatch(r -> r.getClass().getSimpleName().equals("ReportUser"));
        final boolean foundMemory = captor.getAllValues().stream().anyMatch(r -> r.getClass().getSimpleName().equals("ReportMemory"));

        assertTrue(foundVM, "Esperado que ReportVM tenha sido executado");
        assertTrue(foundFileSystem, "Esperado que ReportFileSystem tenha sido executado");
        assertTrue(foundUser, "Esperado que ReportUser tenha sido executado");
        assertTrue(foundMemory, "Esperado que ReportMemory tenha sido executado");
    }

    @Test
    void shouldSubmitEnabledReportsToExecutor2() {
        ReporterConfig.reportVM = false;
        ReporterConfig.reportFileSystem = false;
        ReporterConfig.reportMemory = false;
        ReporterConfig.reportUser = false;
        ReporterConfig.reportProperties = true;
        ReporterConfig.reportEnvironment = true;
        ReporterConfig.reportPhysicalSystem = true;
        ReporterConfig.reportOperatingSystem = true;
        ReporterConfig.reportCalendar = false;
        ReporterConfig.reportLocale = false;
        ReporterConfig.reportCharset = false;
        ReporterConfig.reportNetworkInterface = false;
        ReporterConfig.reportSSLContext = false;
        ReporterConfig.reportDefaultTrustKeyStore = false;

        final Reporter reporter = new Reporter(mockLogger);
        final Executor executor = mock(Executor.class);
        final ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);

        reporter.logDefaultReports(executor);

        verify(executor, times(4)).execute(captor.capture());

        assertTrue(captor.getAllValues().stream().anyMatch(r -> r instanceof ReportSystemProperties));
        assertTrue(captor.getAllValues().stream().anyMatch(r -> r instanceof ReportSystemEnvironment));
        assertTrue(captor.getAllValues().stream().anyMatch(r -> r instanceof ReportPhysicalSystem));
        assertTrue(captor.getAllValues().stream().anyMatch(r -> r instanceof ReportOperatingSystem));
    }

    @Test
    void shouldSubmitLocaleReportsToExecutor3() {
        ReporterConfig.reportVM = false;
        ReporterConfig.reportFileSystem = false;
        ReporterConfig.reportMemory = false;
        ReporterConfig.reportUser = false;
        ReporterConfig.reportProperties = false;
        ReporterConfig.reportEnvironment = false;
        ReporterConfig.reportPhysicalSystem = false;
        ReporterConfig.reportOperatingSystem = false;
        ReporterConfig.reportCalendar = true;
        ReporterConfig.reportLocale = true;
        ReporterConfig.reportCharset = true;
        ReporterConfig.reportNetworkInterface = true;
        ReporterConfig.reportSSLContext = false;
        ReporterConfig.reportDefaultTrustKeyStore = false;

        final Reporter reporter = new Reporter(mockLogger);
        final Executor executor = mock(Executor.class);
        final ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);

        reporter.logDefaultReports(executor);

        // A quantidade de chamadas dependerá de quantas interfaces de rede existem no sistema
        verify(executor, atLeast(3)).execute(captor.capture());

        assertTrue(captor.getAllValues().stream().anyMatch(r -> r instanceof ReportCalendar));
        assertTrue(captor.getAllValues().stream().anyMatch(r -> r instanceof ReportLocale));
        assertTrue(captor.getAllValues().stream().anyMatch(r -> r instanceof ReportCharset));
        assertTrue(captor.getAllValues().stream().anyMatch(r -> r instanceof ReportNetworkInterface));
    }

    @Test
    void shouldSubmitSystemUsageReportsToExecutor4() {
        ReporterConfig.reportVM = true;
        ReporterConfig.reportFileSystem = true;
        ReporterConfig.reportMemory = true;
        ReporterConfig.reportUser = true;
        ReporterConfig.reportProperties = false;
        ReporterConfig.reportEnvironment = false;
        ReporterConfig.reportPhysicalSystem = false;
        ReporterConfig.reportOperatingSystem = false;
        ReporterConfig.reportCalendar = false;
        ReporterConfig.reportLocale = false;
        ReporterConfig.reportCharset = false;
        ReporterConfig.reportNetworkInterface = false;
        ReporterConfig.reportSSLContext = false;
        ReporterConfig.reportDefaultTrustKeyStore = false;

        final Reporter reporter = new Reporter(mockLogger);
        final Executor executor = mock(Executor.class);
        final ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);

        reporter.logDefaultReports(executor);

        verify(executor, times(4)).execute(captor.capture());

        assertTrue(captor.getAllValues().stream().anyMatch(r -> r instanceof ReportVM));
        assertTrue(captor.getAllValues().stream().anyMatch(r -> r instanceof ReportFileSystem));
        assertTrue(captor.getAllValues().stream().anyMatch(r -> r instanceof ReportMemory));
        assertTrue(captor.getAllValues().stream().anyMatch(r -> r instanceof ReportUser));
    }

    @Test
    void shouldSubmitSecurityReportsToExecutor5() {
        ReporterConfig.reportVM = false;
        ReporterConfig.reportFileSystem = false;
        ReporterConfig.reportMemory = false;
        ReporterConfig.reportUser = false;
        ReporterConfig.reportProperties = false;
        ReporterConfig.reportEnvironment = false;
        ReporterConfig.reportPhysicalSystem = false;
        ReporterConfig.reportOperatingSystem = false;
        ReporterConfig.reportCalendar = false;
        ReporterConfig.reportLocale = false;
        ReporterConfig.reportCharset = false;
        ReporterConfig.reportNetworkInterface = false;
        ReporterConfig.reportSSLContext = true;
        ReporterConfig.reportDefaultTrustKeyStore = true;

        final Reporter reporter = new Reporter(mockLogger);
        final Executor executor = mock(Executor.class);
        final ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);

        reporter.logDefaultReports(executor);

        verify(executor, times(2)).execute(captor.capture());

        assertTrue(captor.getAllValues().stream().anyMatch(r -> r instanceof ReportSSLContext));
        assertTrue(captor.getAllValues().stream().anyMatch(r -> r instanceof ReportDefaultTrustKeyStore));
    }

}

