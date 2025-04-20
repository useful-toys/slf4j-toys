package org.usefultoys.slf4j.watcher;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WatcherDataTest {

    @Test
    void testReadableString0() {
        final WatcherData data = new WatcherData("abc",1,2,0,0,0,0,0,0,0,0,0,0,0,0,0,1024,2048,4096,0.25);
        final String output = data.readableMessage();
        assertEquals("Memory: 1024B 4,1kB 2,0kB; System load: 25%; UUID: abc", output);
    }

    @Test
    void testReadableString1() {
        final WatcherData data = new WatcherData("abc",1,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0.25);
        final String output = data.readableMessage();
        assertEquals("System load: 25%; UUID: abc", output);
    }

    @Test
    void testReadableString2() {
        final WatcherData data = new WatcherData("abc",1,2,0,0,0,0,0,0,0,0,0,0,0,0,0,1024,2048,4096,0.0);
        final String output = data.readableMessage();
        assertEquals("Memory: 1024B 4,1kB 2,0kB; UUID: abc", output);
    }

    @Test
    void testReadableString3() {
        final WatcherData data = new WatcherData("abc",1,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0.0);
        final String output = data.readableMessage();
        assertEquals("UUID: abc", output);
    }
}