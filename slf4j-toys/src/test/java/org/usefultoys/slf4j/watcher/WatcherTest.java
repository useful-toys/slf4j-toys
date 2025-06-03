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

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.*;
class WatcherTest {
    private MockLogger mockLogger;
    private MockLogger messageLogger;
    private MockLogger dataLogger;

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

    @BeforeEach
    void setupLogger() {
        mockLogger = (MockLogger) LoggerFactory.getLogger(WatcherConfig.name);
        messageLogger = (MockLogger) LoggerFactory.getLogger(WatcherConfig.name + ".message");
        dataLogger = (MockLogger) LoggerFactory.getLogger(WatcherConfig.name + ".data");
        mockLogger.clearEvents();
        messageLogger.clearEvents();
        dataLogger.clearEvents();
    }

    @AfterEach
    void clearLogger() {
        mockLogger.setEnabled(true);
        mockLogger.clearEvents();
        messageLogger.setEnabled(true);
        messageLogger.clearEvents();
        dataLogger.setEnabled(true);
        dataLogger.clearEvents();
    }

    @Test
    void shouldIncrementPositionAndTimeUsingSeparatedTraceLogger() {
        WatcherConfig.dataEnabled = true;
        WatcherConfig.dataSuffix = ".data";
        WatcherConfig.messageSuffix = ".message";
        messageLogger.setEnabled(true);
        dataLogger.setEnabled(true);
        mockLogger.setEnabled(true);

        final Watcher watcher = new Watcher(WatcherConfig.name);
        final long position = watcher.getPosition();
        final long time = watcher.getLastCurrentTime();
        watcher.run();
        assertEquals(watcher.getPosition(), position + 1);
        assertTrue(watcher.getLastCurrentTime() > time);
        assertTrue(watcher.getRuntime_usedMemory() > 0);

        // Readable and encoded messages are written to the separated logs
        assertEquals(0, mockLogger.getEventCount());
        assertEquals(1, messageLogger.getEventCount());
        assertTrue(messageLogger.getEvent(0).getFormattedMessage().contains("Memory:"));
        assertEquals(1, dataLogger.getEventCount());
        final String json5 = dataLogger.getEvent(0).getMessage();
        assertEquals(json5, watcher.json5Message());
    }

    @Test
    void shouldIncrementPositionAndTimeUsingSameTraceLogger() {
        WatcherConfig.dataEnabled = true;
        WatcherConfig.dataSuffix = "";
        WatcherConfig.messageSuffix = "";
        messageLogger.setEnabled(true);
        dataLogger.setEnabled(true);
        mockLogger.setEnabled(true);

        final Watcher watcher = new Watcher(WatcherConfig.name);
        final long position = watcher.getPosition();
        final long time = watcher.getLastCurrentTime();
        watcher.run();
        assertEquals(watcher.getPosition(), position + 1);
        assertTrue(watcher.getLastCurrentTime() > time);
        assertTrue(watcher.getRuntime_usedMemory() > 0);

        // Readable and encoded messages are written to the same log
        assertEquals(0, messageLogger.getEventCount());
        assertEquals(0, dataLogger.getEventCount());
        assertEquals(2, mockLogger.getEventCount());
        assertTrue(mockLogger.getEvent(0).getFormattedMessage().contains("Memory:"));
        final String json5 = mockLogger.getEvent(1).getMessage();
        assertEquals(json5, watcher.json5Message());
    }

    @Test
    void shouldIncrementPositionAndTimeDisabledDataLogger() {
        WatcherConfig.dataEnabled = false;
        WatcherConfig.dataSuffix = "";
        WatcherConfig.messageSuffix = "";
        messageLogger.setEnabled(true);
        dataLogger.setEnabled(true);
        mockLogger.setEnabled(true);

        final Watcher watcher = new Watcher(WatcherConfig.name);
        final long position = watcher.getPosition();
        final long time = watcher.getLastCurrentTime();
        watcher.run();
        assertEquals(watcher.getPosition(), position + 1);
        assertTrue(watcher.getLastCurrentTime() > time);
        assertTrue(watcher.getRuntime_usedMemory() > 0);

        // Only readable message is written to log
        assertEquals(0, messageLogger.getEventCount());
        assertEquals(0, dataLogger.getEventCount());
        assertEquals(1, mockLogger.getEventCount());
        assertTrue(mockLogger.getEvent(0).getFormattedMessage().contains("Memory:"));
    }

    @Test
    void shouldIncrementPositionAndTimeInfoLogger() {
        WatcherConfig.dataEnabled = true;
        WatcherConfig.dataSuffix = "";
        WatcherConfig.messageSuffix = "";
        messageLogger.setEnabled(true);
        dataLogger.setEnabled(true);
        mockLogger.setTraceEnabled(false);
        mockLogger.setDebugEnabled(false);
        mockLogger.setInfoEnabled(true);
        mockLogger.setWarnEnabled(true);
        mockLogger.setErrorEnabled(true);

        final Watcher watcher = new Watcher(WatcherConfig.name);
        final long position = watcher.getPosition();
        final long time = watcher.getLastCurrentTime();
        watcher.run();
        assertEquals(watcher.getPosition(), position + 1);
        assertTrue(watcher.getLastCurrentTime() > time);
        assertTrue(watcher.getRuntime_usedMemory() > 0);

        // Only readable message is written to log
        assertEquals(0, messageLogger.getEventCount());
        assertEquals(0, dataLogger.getEventCount());
        assertEquals(1, mockLogger.getEventCount());
        assertTrue(mockLogger.getEvent(0).getFormattedMessage().contains("Memory:"));
    }

    @Test
    void shouldIncrementPositionAndTimeErrorLogger() {
        WatcherConfig.dataEnabled = true;
        WatcherConfig.dataSuffix = "";
        WatcherConfig.messageSuffix = "";
        messageLogger.setEnabled(true);
        dataLogger.setEnabled(true);
        mockLogger.setTraceEnabled(false);
        mockLogger.setDebugEnabled(false);
        mockLogger.setInfoEnabled(false);
        mockLogger.setWarnEnabled(false);
        mockLogger.setErrorEnabled(false);

        final Watcher watcher = new Watcher(WatcherConfig.name);
        final long position = watcher.getPosition();
        final long time = watcher.getLastCurrentTime();
        watcher.run();
        assertEquals(watcher.getPosition(), position + 1);
        assertTrue(watcher.getLastCurrentTime() > time);
        // As nothing is logged, the memory usage won't be collected and should be 0
        assertEquals(0, watcher.getRuntime_usedMemory());

        // No messages a written to log
        assertEquals(0, messageLogger.getEventCount());
        assertEquals(0, dataLogger.getEventCount());
        assertEquals(0, mockLogger.getEventCount());
    }
}