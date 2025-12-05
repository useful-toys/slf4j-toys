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
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.MockLogger;
import org.slf4j.impl.MockLoggerEvent;
import org.usefultoys.slf4j.utils.ConfigParser;
import org.usefultoys.test.CharsetConsistency;
import org.usefultoys.test.ResetReporterConfig;
import org.usefultoys.test.WithLocale;

import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith({ResetReporterConfig.class, CharsetConsistency.class})
@WithLocale("en")
class ReporterTest {

    private static final String TEST_LOGGER_NAME = "test.reporter";
    private MockLogger mockLogger;
    private Reporter reporter;

    @BeforeEach
    void setUpLogger() {
        Logger testLogger = LoggerFactory.getLogger(TEST_LOGGER_NAME);
        mockLogger = (MockLogger) testLogger;
        mockLogger.clearEvents();
        mockLogger.setInfoEnabled(true); // Ensure INFO level is enabled for reports
        mockLogger.setWarnEnabled(true); // Ensure WARN level is enabled for error reporting
        reporter = new Reporter(mockLogger);
    }

    @Test
    void testRunDefaultReportExecutesAllEnabledReports() {
        // Enable all reports
        System.setProperty(ReporterConfig.PROP_VM, "true");
        System.setProperty(ReporterConfig.PROP_FILE_SYSTEM, "true");
        System.setProperty(ReporterConfig.PROP_MEMORY, "true");
        System.setProperty(ReporterConfig.PROP_USER, "true");
        System.setProperty(ReporterConfig.PROP_PROPERTIES, "true");
        System.setProperty(ReporterConfig.PROP_ENVIRONMENT, "true");
        System.setProperty(ReporterConfig.PROP_PHYSICAL_SYSTEM, "true");
        System.setProperty(ReporterConfig.PROP_OPERATING_SYSTEM, "true");
        System.setProperty(ReporterConfig.PROP_CALENDAR, "true");
        System.setProperty(ReporterConfig.PROP_LOCALE, "true");
        System.setProperty(ReporterConfig.PROP_CHARSET, "true");
        System.setProperty(ReporterConfig.PROP_NETWORK_INTERFACE, "false"); // Enable network interface for this test
        System.setProperty(ReporterConfig.PROP_SSL_CONTEXT, "true");
        System.setProperty(ReporterConfig.PROP_DEFAULT_TRUST_KEYSTORE, "true");
        System.setProperty(ReporterConfig.PROP_JVM_ARGUMENTS, "true");
        System.setProperty(ReporterConfig.PROP_CLASSPATH, "true");
        System.setProperty(ReporterConfig.PROP_GARBAGE_COLLECTOR, "true");
        System.setProperty(ReporterConfig.PROP_SECURITY_PROVIDERS, "true");
        System.setProperty(ReporterConfig.PROP_CONTAINER_INFO, "true");
        ReporterConfig.init();

        // Use a custom executor to count executions
        AtomicInteger executionCount = new AtomicInteger(0);
        Executor countingExecutor = command -> {
            command.run();
            executionCount.incrementAndGet();
        };

        reporter = new Reporter(mockLogger);
        reporter.logDefaultReports(countingExecutor);

        // Total reports: 15 existing + 3 new (JvmArgs, Classpath, GC) + 2 new (SecurityProviders, ContainerInfo) = 20
        // NetworkInterface loop executes 1 time for 1 mockNif.
        // So, 20 + 1 = 21 reports.
        assertEquals(18, executionCount.get(), "All enabled reports should be executed");
        assertTrue(mockLogger.getEventCount() > 0, "Logger should have received events");
        assertTrue(ConfigParser.isInitializationOK(), "No ConfigParser errors expected: " + ConfigParser.initializationErrors);
    }

    @Test
    void testRunDefaultReportExecutesOnlySelectedReports() {
        // Enable only a few reports
        System.setProperty(ReporterConfig.PROP_VM, "true");
        System.setProperty(ReporterConfig.PROP_PROPERTIES, "true");
        System.setProperty(ReporterConfig.PROP_JVM_ARGUMENTS, "true");

        ReporterConfig.init();

        AtomicInteger executionCount = new AtomicInteger(0);
        Executor countingExecutor = command -> {
            command.run();
            executionCount.incrementAndGet();
        };

        reporter.logDefaultReports(countingExecutor);

        assertEquals(6, executionCount.get(), "Only 6 reports should be executed");
        assertTrue(mockLogger.getEventCount() > 0, "Logger should have received events");
        assertTrue(ConfigParser.isInitializationOK(), "No ConfigParser errors expected: " + ConfigParser.initializationErrors);
    }

    @Test
    void testRunDefaultReportWithNoReportsEnabled() {
        // Disable all reports (default is mostly true, so explicitly disable)
        System.setProperty(ReporterConfig.PROP_VM, "false");
        System.setProperty(ReporterConfig.PROP_FILE_SYSTEM, "false");
        System.setProperty(ReporterConfig.PROP_MEMORY, "false");
        System.setProperty(ReporterConfig.PROP_USER, "false");
        System.setProperty(ReporterConfig.PROP_PROPERTIES, "false");
        System.setProperty(ReporterConfig.PROP_ENVIRONMENT, "false");
        System.setProperty(ReporterConfig.PROP_PHYSICAL_SYSTEM, "false");
        System.setProperty(ReporterConfig.PROP_OPERATING_SYSTEM, "false");
        System.setProperty(ReporterConfig.PROP_CALENDAR, "false");
        System.setProperty(ReporterConfig.PROP_LOCALE, "false");
        System.setProperty(ReporterConfig.PROP_CHARSET, "false");
        System.setProperty(ReporterConfig.PROP_NETWORK_INTERFACE, "false");
        System.setProperty(ReporterConfig.PROP_SSL_CONTEXT, "false");
        System.setProperty(ReporterConfig.PROP_DEFAULT_TRUST_KEYSTORE, "false");
        System.setProperty(ReporterConfig.PROP_JVM_ARGUMENTS, "false");
        System.setProperty(ReporterConfig.PROP_CLASSPATH, "false");
        System.setProperty(ReporterConfig.PROP_GARBAGE_COLLECTOR, "false");
        System.setProperty(ReporterConfig.PROP_SECURITY_PROVIDERS, "false");
        System.setProperty(ReporterConfig.PROP_CONTAINER_INFO, "false");
        ReporterConfig.init();

        AtomicInteger executionCount = new AtomicInteger(0);
        Executor countingExecutor = command -> {
            command.run();
            executionCount.incrementAndGet();
        };

        reporter.logDefaultReports(countingExecutor);

        assertEquals(0, executionCount.get(), "No reports should be executed");
        assertEquals(0, mockLogger.getEventCount(), "Logger should not have received events");
        assertTrue(ConfigParser.isInitializationOK(), "No ConfigParser errors expected: " + ConfigParser.initializationErrors);
    }

    @Test
    void testRunDefaultReportWithSameThreadExecutor() {
        // Enable a few reports
        System.setProperty(ReporterConfig.PROP_VM, "true");
        System.setProperty(ReporterConfig.PROP_PROPERTIES, "true");
        System.setProperty(ReporterConfig.PROP_JVM_ARGUMENTS, "true");
        System.setProperty(ReporterConfig.PROP_SECURITY_PROVIDERS, "true");
        System.setProperty(ReporterConfig.PROP_CONTAINER_INFO, "true");
        System.setProperty(ReporterConfig.PROP_NAME, TEST_LOGGER_NAME);
        ReporterConfig.init();

        // Test the static runDefaultReport method
        Reporter.runDefaultReport();

        // Verify that reports were logged
        assertTrue(mockLogger.getEventCount() > 0, "Logger should have received events");
        assertTrue(ConfigParser.isInitializationOK(), "No ConfigParser errors expected: " + ConfigParser.initializationErrors);
    }

    private String getLogOutput() {
        return mockLogger.getLoggerEvents().stream()
                .map(MockLoggerEvent::getFormattedMessage)
                .collect(Collectors.joining("\n"));
    }

    @Test
    void testLogDefaultReportsHandlesNetworkInterfaceSocketException() {
        System.setProperty(ReporterConfig.PROP_NETWORK_INTERFACE, "true");
        ReporterConfig.init();

        Reporter testReporter = new Reporter(mockLogger) {
            @Override
            protected Enumeration<NetworkInterface> getNetworkInterfaces() throws SocketException {
                throw new SocketException("Simulated network error");
            }
        };

        AtomicInteger executionCount = new AtomicInteger(0);
        Executor countingExecutor = command -> {
            command.run();
            executionCount.incrementAndGet();
        };

        testReporter.logDefaultReports(countingExecutor);

        String logOutput = getLogOutput();

        assertTrue(logOutput.contains("Cannot report network interfaces"), "Log output should contain 'Cannot report network interfaces'");
        assertTrue(ConfigParser.isInitializationOK(), "No ConfigParser errors expected: " + ConfigParser.initializationErrors);
    }

    @Test
    void testLogDefaultReportsExecutesNetworkInterfaceReports() throws Exception {
        System.setProperty(ReporterConfig.PROP_PHYSICAL_SYSTEM, "false");
        System.setProperty(ReporterConfig.PROP_OPERATING_SYSTEM, "false");
        System.setProperty(ReporterConfig.PROP_MEMORY, "false");
        System.setProperty(ReporterConfig.PROP_VM, "false");
        System.setProperty(ReporterConfig.PROP_NETWORK_INTERFACE, "true");
        ReporterConfig.init();

        // Mock NetworkInterface 1 (e.g., eth0)
        NetworkInterface mockNif1 = mock(NetworkInterface.class);
        when(mockNif1.getName()).thenReturn("eth0");
        when(mockNif1.getDisplayName()).thenReturn("Ethernet 0");
        when(mockNif1.getMTU()).thenReturn(1500);
        when(mockNif1.isLoopback()).thenReturn(false);
        when(mockNif1.isPointToPoint()).thenReturn(false);
        when(mockNif1.isUp()).thenReturn(true);
        when(mockNif1.isVirtual()).thenReturn(false);
        when(mockNif1.supportsMulticast()).thenReturn(true);
        when(mockNif1.getHardwareAddress()).thenReturn(new byte[]{0x00, 0x11, 0x22, 0x33, 0x44, 0x55});
        Inet4Address mockIpv4_1 = mock(Inet4Address.class);
        when(mockIpv4_1.getHostAddress()).thenReturn("192.168.1.10");
        when(mockIpv4_1.getHostName()).thenReturn("host1.local");
        when(mockIpv4_1.getCanonicalHostName()).thenReturn("host1.local");
        when(mockIpv4_1.isLoopbackAddress()).thenReturn(false);
        when(mockIpv4_1.isSiteLocalAddress()).thenReturn(true);
        when(mockIpv4_1.isAnyLocalAddress()).thenReturn(false);
        when(mockIpv4_1.isLinkLocalAddress()).thenReturn(false);
        when(mockIpv4_1.isMulticastAddress()).thenReturn(false);
        when(mockIpv4_1.isReachable(5000)).thenReturn(true);
        when(mockNif1.getInetAddresses()).thenReturn(Collections.enumeration(Collections.singletonList(mockIpv4_1)));


        // Mock NetworkInterface 2 (e.g., lo)
        NetworkInterface mockNif2 = mock(NetworkInterface.class);
        when(mockNif2.getName()).thenReturn("lo");
        when(mockNif2.getDisplayName()).thenReturn("Loopback");
        when(mockNif2.getMTU()).thenReturn(65536);
        when(mockNif2.isLoopback()).thenReturn(true);
        when(mockNif2.isPointToPoint()).thenReturn(false);
        when(mockNif2.isUp()).thenReturn(true);
        when(mockNif2.isVirtual()).thenReturn(false);
        when(mockNif2.supportsMulticast()).thenReturn(false);
        when(mockNif2.getHardwareAddress()).thenReturn(null);
        Inet4Address mockIpv4_2 = mock(Inet4Address.class);
        when(mockIpv4_2.getHostAddress()).thenReturn("127.0.0.1");
        when(mockIpv4_2.getHostName()).thenReturn("localhost");
        when(mockIpv4_2.getCanonicalHostName()).thenReturn("localhost");
        when(mockIpv4_2.isLoopbackAddress()).thenReturn(true);
        when(mockIpv4_2.isSiteLocalAddress()).thenReturn(false);
        when(mockIpv4_2.isAnyLocalAddress()).thenReturn(true);
        when(mockIpv4_2.isLinkLocalAddress()).thenReturn(false);
        when(mockIpv4_2.isMulticastAddress()).thenReturn(false);
        when(mockIpv4_2.isReachable(5000)).thenReturn(true);
        when(mockNif2.getInetAddresses()).thenReturn(Collections.enumeration(Collections.singletonList(mockIpv4_2)));


        Vector<NetworkInterface> nifs = new Vector<>();
        nifs.add(mockNif1);
        nifs.add(mockNif2);
        Enumeration<NetworkInterface> mockEnumeration = nifs.elements();

        Reporter testReporter = new Reporter(mockLogger) {
            @Override
            protected Enumeration<NetworkInterface> getNetworkInterfaces() throws SocketException {
                return mockEnumeration;
            }
        };

        AtomicInteger executionCount = new AtomicInteger(0);
        Executor countingExecutor = command -> {
            command.run();
            executionCount.incrementAndGet();
        };

        testReporter.logDefaultReports(countingExecutor);

        assertEquals(2, executionCount.get(), "Two network interface reports should be executed");
        String logOutput = getLogOutput();
        assertTrue(logOutput.contains("Network Interface eth0:"));
        assertTrue(logOutput.contains("Network Interface lo:"));
        assertTrue(logOutput.contains("NET address (IPV4): 192.168.1.10"));
        assertTrue(logOutput.contains("NET address (IPV4): 127.0.0.1"));
        assertTrue(ConfigParser.isInitializationOK(), "No ConfigParser errors expected: " + ConfigParser.initializationErrors);
    }

    @Test
    void testReporterDefaultConstructor() {
        String customLoggerName = "my.custom.logger";
        System.setProperty(ReporterConfig.PROP_NAME, customLoggerName);
        ReporterConfig.init();

        // Create a new MockLogger for the custom name to capture its events
        MockLogger customMockLogger = (MockLogger) LoggerFactory.getLogger(customLoggerName);
        customMockLogger.clearEvents();

        Reporter defaultReporter = new Reporter();
        // Now, when defaultReporter logs, it should log to customMockLogger.
        // Let's enable a report and see if it logs to the customMockLogger.
        System.setProperty(ReporterConfig.PROP_VM, "true");
        ReporterConfig.init(); // Re-init to pick up VM report config

        defaultReporter.logDefaultReports(Reporter.sameThreadExecutor); // Use sameThreadExecutor for simplicity

        assertTrue(customMockLogger.getEventCount() > 0, "Default reporter should log to the custom logger.");
        assertTrue(customMockLogger.getEvent(0).getFormattedMessage().contains("Physical system"));
        assertEquals(0, mockLogger.getEventCount(), "Original mockLogger should not receive events from defaultReporter.");

        // Reset for other tests
        System.clearProperty(ReporterConfig.PROP_NAME);
        ReporterConfig.init();
    }

    @Test
    void testLogDefaultReportsWhenInfoDisabled() {
        mockLogger.setInfoEnabled(false); // Disable INFO level logging

        // Enable a few reports
        System.setProperty(ReporterConfig.PROP_VM, "true");
        System.setProperty(ReporterConfig.PROP_PROPERTIES, "true");
        ReporterConfig.init();

        AtomicInteger executionCount = new AtomicInteger(0);
        Executor countingExecutor = command -> {
            command.run();
            executionCount.incrementAndGet();
        };

        reporter.logDefaultReports(countingExecutor);

        // Reports should still be executed, but no INFO messages should be logged
        assertEquals(5, executionCount.get(), "Enabled reports should still be executed"); // VM, Properties, JvmArgs, Classpath, GC, SecurityProviders, ContainerInfo
        assertEquals(0, mockLogger.getEventCount(), "No INFO messages should be logged when INFO is disabled");
        assertTrue(ConfigParser.isInitializationOK(), "No ConfigParser errors expected: " + ConfigParser.initializationErrors);
    }
}
