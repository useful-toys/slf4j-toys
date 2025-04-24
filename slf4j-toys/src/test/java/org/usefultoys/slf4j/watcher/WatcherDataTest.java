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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.usefultoys.slf4j.SessionConfig;
import org.usefultoys.slf4j.SystemConfig;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.*;

class WatcherDataTest {

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

    @Test
    void testReadableMessageWithMemoryAndSystemLoad() {
        // Arrange
        final WatcherData watcherData = new WatcherData("test-session-uuid", 1, System.currentTimeMillis(),
                1024, 2048, 512, // heap
                512, 1024, 256,  // non-heap
                0,               // finalization count
                0, 0, 0,         // class loading
                0,               // compilation time
                0, 0,            // garbage collector
                // use small values as formatting for larger values will be different on different locales
                512, 513, 514,  // non-heap
                0.75);           // system load

        // Act
        final String readableMessage = watcherData.readableMessage();

        // Assert
        assertTrue(readableMessage.contains("Memory: 512B 514B 513B"), "Readable message should include memory details");
        assertTrue(readableMessage.contains("System load: 75%"), "Readable message should include system load details");
        assertTrue(readableMessage.contains("UUID: test-session-uuid"), "Readable message should include session UUID");
    }

    @Test
    void testReadableMessageWithoutSystemLoad() {
        // Arrange
        final WatcherData watcherData = new WatcherData("test-session-uuid", 1, System.currentTimeMillis(),
                1024, 2048, 512, // heap
                512, 1024, 256,  // non-heap
                0,               // finalization count
                0, 0, 0,         // class loading
                0,               // compilation time
                0, 0,            // garbage collector
                // use small values as formatting for larger values will be different on different locales
                512, 513, 514,  // non-heap
                0.0);            // system load

        // Act
        final String readableMessage = watcherData.readableMessage();

        // Assert
        assertTrue(readableMessage.contains("Memory: 512B 514B 513B"), "Readable message should include memory details");
        assertFalse(readableMessage.contains("System load:"), "Readable message should not include system load details");
        assertTrue(readableMessage.contains("UUID: test-session-uuid"), "Readable message should include session UUID");
    }

    @Test
    void testReadableMessageWithoutMemory() {
        // Arrange
        final WatcherData watcherData = new WatcherData("test-session-uuid", 1, System.currentTimeMillis(),
                1024, 2048, 512, // heap
                512, 1024, 256,  // non-heap
                12,               // finalization count
                9, 10, 11,         // class loading
                8,               // compilation time
                6, 7,            // garbage collector
                0, 0, 0, // runtime memory
                0.75);            // system load

        // Act
        final String readableMessage = watcherData.readableMessage();

        // Assert
        assertFalse(readableMessage.contains("Memory:"), "Readable message should not include memory details");
        assertTrue(readableMessage.contains("System load: 75%"), "Readable message should include system load details");
        assertTrue(readableMessage.contains("UUID: test-session-uuid"), "Readable message should include session UUID");
    }

    @Test
    void testReadableMessageWithoutMemoryAndSystemLoad() {
        // Arrange
        final WatcherData watcherData = new WatcherData("test-session-uuid", 1, System.currentTimeMillis(),
                1024, 2048, 512, // heap
                512, 1024, 256,  // non-heap
                12,               // finalization count
                9, 10, 11,         // class loading
                8,               // compilation time
                6, 7,            // garbage collector
                0, 0, 0, // runtime memory
                0.0);            // system load

        // Act
        final String readableMessage = watcherData.readableMessage();

        // Assert
        assertFalse(readableMessage.contains("Memory:"), "Readable message should not include memory details");
        assertFalse(readableMessage.contains("System load:"), "Readable message should not  include system load details");
        assertTrue(readableMessage.contains("UUID: test-session-uuid"), "Readable message should include session UUID");
    }
}
