package org.usefultoys.slf4j.internal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EventDataTest {

    static class TestEventData extends EventData {
        boolean resetCalled = false;

        public TestEventData() {
            super();
        }

        public TestEventData(String sessionUuid) {
            super(sessionUuid);
        }

        public TestEventData(String sessionUuid, long position) {
            super(sessionUuid, position);
        }

        public TestEventData(final String sessionUuid, final long position, final long time) {
            super(sessionUuid, position, time);
        }

        @Override
        protected StringBuilder readableString(StringBuilder sb) {
            sb.append("a");
            return sb;
        }

        @Override
        protected void resetImpl() {
            resetCalled = true;
        }
    }

    @Test
    void testConstructorAndGetters0() {
        final TestEventData event = new TestEventData();
        assertNull(event.getSessionUuid());
        assertEquals(0L, event.getPosition());
        assertEquals(0L, event.getTime()); // time should be 0 before nextPosition
    }

    @Test
    void testConstructorAndGetters1() {
        final TestEventData event = new TestEventData("abc");
        assertEquals("abc", event.getSessionUuid());
        assertEquals(0L, event.getPosition());
        assertEquals(0L, event.getTime());
    }

    @Test
    void testConstructorAndGetters2() {
        final TestEventData event = new TestEventData("abc", 5L);
        assertEquals("abc", event.getSessionUuid());
        assertEquals(5L, event.getPosition());
        assertEquals(0L, event.getTime());
    }

    @Test
    void testConstructorAndGetters3() {
        final TestEventData event = new TestEventData("abc", 5L, 10L);
        assertEquals("abc", event.getSessionUuid());
        assertEquals(5L, event.getPosition());
        assertEquals(10L, event.getTime());
    }

    @Test
    void testNextPositionUpdatesPositionAndTime() {
        final TestEventData event = new TestEventData("abc", 5L);
        long before = System.nanoTime();
        event.nextPosition();
        long after = System.nanoTime();

        assertEquals(6L, event.getPosition());
        assertTrue(event.getTime() >= before && event.getTime() <= after);
    }

    @Test
    void testResetClearsFields() {
        final TestEventData event = new TestEventData("abc", 5L);
        event.nextPosition(); // just to update time/position
        event.reset();
        assertNull(event.getSessionUuid());
        assertEquals(0L, event.getPosition());
        assertEquals(0L, event.getTime());
        assertTrue(event.resetCalled);
    }

    @Test
    void testReadableMessage() {
        final TestEventData event = new TestEventData("abc", 5L);
        final String message = event.readableMessage();
        assertTrue(message.equals("a"));
    }

    @Test
    void testJsonMessage() {
        final TestEventData event = new TestEventData("abc", 5L);
        final String json = event.jsonMessage();
        assertEquals("{s:abc,p:5,t:0}", json);
    }
}
