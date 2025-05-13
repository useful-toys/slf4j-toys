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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.MockLogger;
import org.usefultoys.slf4j.SessionConfig;
import org.usefultoys.slf4j.SystemConfig;

import java.net.NetworkInterface;
import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.*;

class ReportNetworkInterfaceTest {

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
        final Logger logger = LoggerFactory.getLogger("test.report.os");
        mockLogger = (MockLogger) logger;
        mockLogger.clearEvents();
    }

    @Test
    void testRunWithLoopbackInterfaceLo() throws Exception {
        // Arrange using "lo" (Linux/macOS) interface
        final NetworkInterface nif = NetworkInterface.getByName("lo");
        assertNotNull(nif, "A interface 'lo' não foi encontrada. Verifique se o teste está rodando em Linux/macOS.");

        // Act
        final ReportNetworkInterface report = new ReportNetworkInterface(mockLogger, nif);
        report.run(); // A execução imprimirá no logger via LoggerFactory.getInfoPrintStream

        // Assert
        assertTrue(mockLogger.getEventCount() > 0);
        final String logs = mockLogger.getEvent(0).getFormattedMessage();

        assertTrue(logs.toLowerCase().contains("loopback"));
    }
}
