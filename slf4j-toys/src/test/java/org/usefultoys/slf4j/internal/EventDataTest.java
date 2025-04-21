/*
 * Copyright 2024 Daniel Felix Ferber
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.usefultoys.slf4j.internal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Daniel
 */
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
        protected StringBuilder readableStringBuilder(StringBuilder sb) {
            sb.append("a");
            return sb;
        }

        @Override
        protected void resetImpl() {
            resetCalled = true;
        }

        @Override
        public String encodedMessage() {
            return "";
        }

        @Override
        protected void writePropertiesImpl(final EventWriter w) {
            // no-op
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
    void testResetClearsFields() {
        final TestEventData event = new TestEventData("abc", 5L,10L);
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
    void testWriteJson5Message() {
        final TestEventData event = new TestEventData("abc", 5L, 10L);
        final String json = event.jsonMessage5();
        assertEquals("{_:abc,$:5,t:10}", json);
    }

    @Test
    void testReadJson5Message() {
        final TestEventData event = new TestEventData();
        event.readJson5("{_:abc,$:5,t:10}");
        assertEquals("abc", event.getSessionUuid());
        assertEquals(5L, event.getPosition());
        assertEquals(10L, event.getTime());
    }
}
