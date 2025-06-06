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
package org.usefultoys.slf4j.internal;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.usefultoys.slf4j.SessionConfig;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.*;

class EventDataTest {

    @BeforeAll
    static void validate() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    static class TestEventData extends EventData {
        boolean resetCalled = false;

        public TestEventData() {
        }

        public TestEventData(final String sessionUuid) {
            super(sessionUuid);
        }

        public TestEventData(final String sessionUuid, final long position) {
            super(sessionUuid, position);
        }

        public TestEventData(final String sessionUuid, final long position, final long time) {
            super(sessionUuid, position, time);
        }

        @Override
        protected StringBuilder readableStringBuilder(final StringBuilder sb) {
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
        assertEquals(0L, event.getLastCurrentTime()); // time should be 0 before nextPosition
    }

    @Test
    void testConstructorAndGetters1() {
        final TestEventData event = new TestEventData("abc");
        assertEquals("abc", event.getSessionUuid());
        assertEquals(0L, event.getPosition());
        assertEquals(0L, event.getLastCurrentTime());
    }

    @Test
    void testConstructorAndGetters2() {
        final TestEventData event = new TestEventData("abc", 5L);
        assertEquals("abc", event.getSessionUuid());
        assertEquals(5L, event.getPosition());
        assertEquals(0L, event.getLastCurrentTime());
    }

    @Test
    void testConstructorAndGetters3() {
        final TestEventData event = new TestEventData("abc", 5L, 10L);
        assertEquals("abc", event.getSessionUuid());
        assertEquals(5L, event.getPosition());
        assertEquals(10L, event.getLastCurrentTime());
    }

    @Test
    void testResetClearsFields() {
        final TestEventData event = new TestEventData("abc", 5L,10L);
        event.reset();
        assertNull(event.getSessionUuid());
        assertEquals(0L, event.getPosition());
        assertEquals(0L, event.getLastCurrentTime());
        assertTrue(event.resetCalled);
    }

    @Test
    void testReadableMessage() {
        final TestEventData event = new TestEventData("abc", 5L);
        final String message = event.readableMessage();
        assertEquals("a", message);
    }

    @Test
    void testWriteJson5Message() {
        final TestEventData event = new TestEventData("abc", 5L, 10L);
        final String json = event.json5Message();
        assertEquals("{_:abc,$:5,t:10}", json);
    }

    @Test
    void testReadJson5Message() {
        final TestEventData event = new TestEventData();
        event.readJson5("{_:abc,$:5,t:10}");
        assertEquals("abc", event.getSessionUuid());
        assertEquals(5L, event.getPosition());
        assertEquals(10L, event.getLastCurrentTime());
    }
}
