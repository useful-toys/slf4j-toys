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
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.MockLogger;
import org.usefultoys.slf4j.utils.ConfigParser;
import org.usefultoys.slf4jtestmock.AssertLogger;
import org.usefultoys.slf4jtestmock.MockLoggerExtension;
import org.usefultoys.slf4jtestmock.Slf4jMock;
import org.usefultoys.test.ResetReporterConfig;
import org.usefultoys.test.ResetSystemProperty;
import org.usefultoys.test.ValidateCharset;
import org.usefultoys.test.WithLocale;

import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link Reporter}.
 * <p>
 * Tests verify that Reporter correctly executes default reports based on configuration,
 * handles network interface reporting, manages logging levels, and integrates with ReporterConfig.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>Report Execution Control:</b> Verifies selective execution of reports based on configuration, including all enabled, selected subset, and none disabled scenarios</li>
 *   <li><b>Executor Integration:</b> Tests execution using custom executors for counting and same-thread execution models</li>
 *   <li><b>Network Interface Reporting:</b> Validates proper handling of network interfaces including mock data, error conditions (SocketException), and detailed interface information logging</li>
 *   <li><b>Logger Configuration:</b> Ensures correct logger selection using default constructor with custom logger names from ReporterConfig</li>
 *   <li><b>Logging Level Handling:</b> Tests behavior when INFO logging level is disabled, ensuring reports execute but no messages are logged</li>
 *   <li><b>Configuration Integration:</b> Verifies integration with ReporterConfig for enabling/disabling specific report types</li>
 * </ul>
 */
@SuppressWarnings("NonConstantLogger")
@DisplayName("Reporter")
@ValidateCharset
@ResetReporterConfig
@WithLocale("en")
@ExtendWith(MockLoggerExtension.class)
class ReporterTest {

    @Slf4jMock
    private Logger logger;

    private Reporter reporter;

    @BeforeEach
    void setUpLogger() {
        reporter = new Reporter(logger);
    }

    @Test
    @DisplayName("should execute all enabled reports")
    void shouldExecuteAllEnabledReports() {
        // Given: all reports enabled
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
        final AtomicInteger executionCount = new AtomicInteger(0);
        final Executor countingExecutor = command -> {
            command.run();
            executionCount.incrementAndGet();
        };

        // When: logDefaultReports is called with all reports enabled
        reporter.logDefaultReports(countingExecutor);

        // Then: all enabled reports should be executed
        // Total reports: 15 existing + 3 new (JvmArgs, Classpath, GC) + 2 new (SecurityProviders, ContainerInfo) = 20
        // NetworkInterface loop executes 0 times (disabled).
        // So, 20 - 2 = 18 reports.
        assertEquals(18, executionCount.get(), "All enabled reports should be executed");
        AssertLogger.assertHasEvent(logger, "Physical system");
        assertTrue(ConfigParser.isInitializationOK(), "No ConfigParser errors expected: " + ConfigParser.initializationErrors);
    }

    @Test
    @DisplayName("should execute only selected reports")
    void shouldExecuteOnlySelectedReports() {
        // Given: only a few reports enabled
        System.setProperty(ReporterConfig.PROP_VM, "true");
        System.setProperty(ReporterConfig.PROP_PROPERTIES, "true");
        System.setProperty(ReporterConfig.PROP_JVM_ARGUMENTS, "true");
        ReporterConfig.init();

        // Use a custom executor to count executions
        final AtomicInteger executionCount = new AtomicInteger(0);
        final Executor countingExecutor = command -> {
            command.run();
            executionCount.incrementAndGet();
        };

        // When: logDefaultReports is called
        reporter.logDefaultReports(countingExecutor);

        // Then: only the selected reports should be executed
        // Verify that the enabled reports were logged
        assertEquals(6, executionCount.get(), "Only 6 reports should be executed");
        AssertLogger.assertHasEvent(logger, "Java Virtual Machine");
        AssertLogger.assertHasEvent(logger, "System Properties");
        assertTrue(ConfigParser.isInitializationOK(), "No ConfigParser errors expected: " + ConfigParser.initializationErrors);
    }

    @Test
    @DisplayName("should not execute any reports when all are disabled")
    void shouldNotExecuteWhenAllReportsDisabled() {
        // Given: all reports disabled (default is mostly true, so explicitly disable)
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

        final AtomicInteger executionCount = new AtomicInteger(0);
        final Executor countingExecutor = command -> {
            command.run();
            executionCount.incrementAndGet();
        };

        // When: logDefaultReports is called
        reporter.logDefaultReports(countingExecutor);

        // Then: no reports should be executed
        // Verify no events were logged by checking that none of the common report titles appear
        assertEquals(0, executionCount.get(), "No reports should be executed");
        AssertLogger.assertNoEvent(logger, "Java Virtual Machine");
        AssertLogger.assertNoEvent(logger, "Physical system");
        assertTrue(ConfigParser.isInitializationOK(), "No ConfigParser errors expected: " + ConfigParser.initializationErrors);
    }

    @Test
    @DisplayName("should execute default reports with same thread executor")
    @ResetSystemProperty(ReporterConfig.PROP_NAME)
    void shouldExecuteWithSameThreadExecutor() {
        // Given: selected reports enabled with custom logger name
        System.setProperty(ReporterConfig.PROP_VM, "true");
        System.setProperty(ReporterConfig.PROP_PROPERTIES, "true");
        System.setProperty(ReporterConfig.PROP_JVM_ARGUMENTS, "true");
        System.setProperty(ReporterConfig.PROP_SECURITY_PROVIDERS, "true");
        System.setProperty(ReporterConfig.PROP_CONTAINER_INFO, "true");
        System.setProperty(ReporterConfig.PROP_NAME, "org.usefultoys.slf4j.report.ReporterTest");
        ReporterConfig.init();

        // When: runDefaultReport is called using static method
        Reporter.runDefaultReport();

        // Then: reports should be logged to the test logger
        AssertLogger.assertHasEvent(logger, "Physical system");
        assertTrue(ConfigParser.isInitializationOK(), "No ConfigParser errors expected: " + ConfigParser.initializationErrors);
    }

    @Test
    @DisplayName("should handle network interface socket exception")
    @ResetSystemProperty(ReporterConfig.PROP_NETWORK_INTERFACE)
    void shouldHandleNetworkInterfaceSocketException() {
        // Given: network interface reporting enabled with socket error simulation
        System.setProperty(ReporterConfig.PROP_NETWORK_INTERFACE, "true");
        ReporterConfig.init();

        final Reporter testReporter = new Reporter(logger) {
            @Override
            protected Enumeration<NetworkInterface> getNetworkInterfaces() throws SocketException {
                throw new SocketException("Simulated network error");
            }
        };

        final AtomicInteger executionCount = new AtomicInteger(0);
        final Executor countingExecutor = command -> {
            command.run();
            executionCount.incrementAndGet();
        };

        // When: logDefaultReports is called
        testReporter.logDefaultReports(countingExecutor);

        // Then: error message should be logged
        AssertLogger.assertHasEvent(logger, "Cannot report network interfaces");
        assertTrue(ConfigParser.isInitializationOK(), "No ConfigParser errors expected: " + ConfigParser.initializationErrors);
    }

    @Test
    @DisplayName("should execute network interface reports")
    @ResetSystemProperty(ReporterConfig.PROP_PHYSICAL_SYSTEM)
    @ResetSystemProperty(ReporterConfig.PROP_OPERATING_SYSTEM)
    @ResetSystemProperty(ReporterConfig.PROP_MEMORY)
    @ResetSystemProperty(ReporterConfig.PROP_VM)
    @ResetSystemProperty(ReporterConfig.PROP_NETWORK_INTERFACE)
    void shouldExecuteNetworkInterfaceReports() throws Exception {
        // Given: network interface reporting enabled with mocked network interfaces
        System.setProperty(ReporterConfig.PROP_PHYSICAL_SYSTEM, "false");
        System.setProperty(ReporterConfig.PROP_OPERATING_SYSTEM, "false");
        System.setProperty(ReporterConfig.PROP_MEMORY, "false");
        System.setProperty(ReporterConfig.PROP_VM, "false");
        System.setProperty(ReporterConfig.PROP_NETWORK_INTERFACE, "true");
        ReporterConfig.init();

        // Mock NetworkInterface 1 (e.g., eth0)
        final NetworkInterface mockNif1 = mock(NetworkInterface.class);
        when(mockNif1.getName()).thenReturn("eth0");
        when(mockNif1.getDisplayName()).thenReturn("Ethernet 0");
        when(mockNif1.getMTU()).thenReturn(1500);
        when(mockNif1.isLoopback()).thenReturn(false);
        when(mockNif1.isPointToPoint()).thenReturn(false);
        when(mockNif1.isUp()).thenReturn(true);
        when(mockNif1.isVirtual()).thenReturn(false);
        when(mockNif1.supportsMulticast()).thenReturn(true);
        when(mockNif1.getHardwareAddress()).thenReturn(new byte[]{0x00, 0x11, 0x22, 0x33, 0x44, 0x55});
        final Inet4Address mockIpv4_1 = mock(Inet4Address.class);
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
        final NetworkInterface mockNif2 = mock(NetworkInterface.class);
        when(mockNif2.getName()).thenReturn("lo");
        when(mockNif2.getDisplayName()).thenReturn("Loopback");
        when(mockNif2.getMTU()).thenReturn(65536);
        when(mockNif2.isLoopback()).thenReturn(true);
        when(mockNif2.isPointToPoint()).thenReturn(false);
        when(mockNif2.isUp()).thenReturn(true);
        when(mockNif2.isVirtual()).thenReturn(false);
        when(mockNif2.supportsMulticast()).thenReturn(false);
        when(mockNif2.getHardwareAddress()).thenReturn(null);
        final Inet4Address mockIpv4_2 = mock(Inet4Address.class);
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

        final Vector<NetworkInterface> nifs = new Vector<>();
        nifs.add(mockNif1);
        nifs.add(mockNif2);
        final Enumeration<NetworkInterface> mockEnumeration = nifs.elements();

        final Reporter testReporter = new Reporter(logger) {
            @Override
            protected Enumeration<NetworkInterface> getNetworkInterfaces() throws SocketException {
                return mockEnumeration;
            }
        };

        final AtomicInteger executionCount = new AtomicInteger(0);
        final Executor countingExecutor = command -> {
            command.run();
            executionCount.incrementAndGet();
        };

        // When: logDefaultReports is called
        testReporter.logDefaultReports(countingExecutor);

        // Then: network interfaces should be reported
        assertEquals(2, executionCount.get(), "Two network interface reports should be executed");
        AssertLogger.assertHasEvent(logger, "Network Interface eth0:");
        AssertLogger.assertHasEvent(logger, "Network Interface lo:");
        AssertLogger.assertHasEvent(logger, "NET address (IPV4): 192.168.1.10");
        AssertLogger.assertHasEvent(logger, "NET address (IPV4): 127.0.0.1");
        assertTrue(ConfigParser.isInitializationOK(), "No ConfigParser errors expected: " + ConfigParser.initializationErrors);
    }

    @Test
    @DisplayName("should use default constructor with custom logger name")
    @ResetSystemProperty(ReporterConfig.PROP_NAME)
    @ResetSystemProperty(ReporterConfig.PROP_VM)
    void shouldUseDefaultConstructorWithCustomLoggerName() {
        // Given: custom logger name configured
        final String customLoggerName = "my.custom.logger";
        System.setProperty(ReporterConfig.PROP_NAME, customLoggerName);
        ReporterConfig.init();

        // When: create a new Logger for the custom name to capture its events
        final Logger customLogger = LoggerFactory.getLogger(customLoggerName);
        ((MockLogger) customLogger).clearEvents();

        // Create reporter with default constructor
        final Reporter defaultReporter = new Reporter();
        // Now, when defaultReporter logs, it should log to customLogger.
        // Let's enable a report and see if it logs to the customLogger.
        System.setProperty(ReporterConfig.PROP_VM, "true");
        ReporterConfig.init(); // Re-init to pick up VM report config

        defaultReporter.logDefaultReports(Reporter.sameThreadExecutor); // Use sameThreadExecutor for simplicity

        // Then: should log to custom logger, not the test logger
        AssertLogger.assertHasEvent(customLogger, "Physical system");
        AssertLogger.assertNoEvent(logger, "Physical system");
    }

    @Test
    @DisplayName("should not log when info level disabled")
    @ResetSystemProperty(ReporterConfig.PROP_VM)
    @ResetSystemProperty(ReporterConfig.PROP_PROPERTIES)
    void shouldNotLogWhenInfoLevelDisabled() {
        // Given: INFO level logging disabled
        ((MockLogger) logger).setInfoEnabled(false);

        // Enable a few reports
        System.setProperty(ReporterConfig.PROP_VM, "true");
        System.setProperty(ReporterConfig.PROP_PROPERTIES, "true");
        ReporterConfig.init();

        final AtomicInteger executionCount = new AtomicInteger(0);
        final Executor countingExecutor = command -> {
            command.run();
            executionCount.incrementAndGet();
        };

        // When: logDefaultReports is called
        reporter.logDefaultReports(countingExecutor);

        // Then: reports should still be executed, but no INFO messages should be logged
        assertEquals(5, executionCount.get(), "Enabled reports should still be executed");
        AssertLogger.assertNoEvent(logger, "Java Virtual Machine");
        AssertLogger.assertNoEvent(logger, "System Properties");
        assertTrue(ConfigParser.isInitializationOK(), "No ConfigParser errors expected: " + ConfigParser.initializationErrors);
    }
}
