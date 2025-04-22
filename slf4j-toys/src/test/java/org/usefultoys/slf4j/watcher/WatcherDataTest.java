package org.usefultoys.slf4j.watcher;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WatcherDataTest {

    @Test
    void testReadableMessageWithMemoryAndSystemLoad() {
        // Arrange
        WatcherData watcherData = new WatcherData("test-session-uuid", 1, System.currentTimeMillis(),
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
        String readableMessage = watcherData.readableMessage();

        // Assert
        assertTrue(readableMessage.contains("Memory: 512B 514B 513B"), "Readable message should include memory details");
        assertTrue(readableMessage.contains("System load: 75%"), "Readable message should include system load details");
        assertTrue(readableMessage.contains("UUID: test-session-uuid"), "Readable message should include session UUID");
    }

    @Test
    void testReadableMessageWithoutSystemLoad() {
        // Arrange
        WatcherData watcherData = new WatcherData("test-session-uuid", 1, System.currentTimeMillis(),
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
        String readableMessage = watcherData.readableMessage();

        // Assert
        assertTrue(readableMessage.contains("Memory: 512B 514B 513B"), "Readable message should include memory details");
        assertFalse(readableMessage.contains("System load:"), "Readable message should not include system load details");
        assertTrue(readableMessage.contains("UUID: test-session-uuid"), "Readable message should include session UUID");
    }

    @Test
    void testReadableMessageWithoutMemory() {
        // Arrange
        WatcherData watcherData = new WatcherData("test-session-uuid", 1, System.currentTimeMillis(),
                1024, 2048, 512, // heap
                512, 1024, 256,  // non-heap
                12,               // finalization count
                9, 10, 11,         // class loading
                8,               // compilation time
                6, 7,            // garbage collector
                0, 0, 0, // runtime memory
                0.75);            // system load

        // Act
        String readableMessage = watcherData.readableMessage();

        // Assert
        assertFalse(readableMessage.contains("Memory:"), "Readable message should not include memory details");
        assertTrue(readableMessage.contains("System load: 75%"), "Readable message should include system load details");
        assertTrue(readableMessage.contains("UUID: test-session-uuid"), "Readable message should include session UUID");
    }

    @Test
    void testReadableMessageWithoutMemoryAndSystemLoad() {
        // Arrange
        WatcherData watcherData = new WatcherData("test-session-uuid", 1, System.currentTimeMillis(),
                1024, 2048, 512, // heap
                512, 1024, 256,  // non-heap
                12,               // finalization count
                9, 10, 11,         // class loading
                8,               // compilation time
                6, 7,            // garbage collector
                0, 0, 0, // runtime memory
                0.0);            // system load

        // Act
        String readableMessage = watcherData.readableMessage();

        // Assert
        assertFalse(readableMessage.contains("Memory:"), "Readable message should not include memory details");
        assertFalse(readableMessage.contains("System load:"), "Readable message should not  include system load details");
        assertTrue(readableMessage.contains("UUID: test-session-uuid"), "Readable message should include session UUID");
    }
}
