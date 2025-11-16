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
    }

    @Test
    void testConstructorAndGetters0() {
        final TestEventData event = new TestEventData();
        assertNull(event.getSessionUuid());
        assertEquals(0L, event.getPosition());
        assertEquals(0L, event.getLastCurrentTime());
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
    void testCollectCurrentTime() {
        final TestEventData event = new TestEventData();
        long startTime = System.nanoTime();
        event.collectCurrentTime();
        long endTime = System.nanoTime();
        assertTrue(event.getLastCurrentTime() >= startTime);
        assertTrue(event.getLastCurrentTime() <= endTime);
    }

    @Test
    void testResetClearsFieldsAndCallsResetImpl() {
        final TestEventData event = new TestEventData("abc", 5L, 10L);
        event.reset();
        assertNull(event.getSessionUuid());
        assertEquals(0L, event.getPosition());
        assertEquals(0L, event.getLastCurrentTime());
    }

//    @Test
//    void testJson5MessageAndWriteJson5Impl() {
//        final TestEventData event = new TestEventData("abc", 5L, 10L);
//        final String json = event.json5Message();
//        // Expecting custom:value from writeJson5Impl
//        assertTrue(json.startsWith("{"));
//        assertTrue(json.contains(EventData.SESSION_UUID + ":abc"));
//        assertTrue(json.contains(EventData.EVENT_POSITION + ":5"));
//        assertTrue(json.contains(EventData.EVENT_TIME + ":10"));
//        assertTrue(json.contains(",custom:value"));
//        assertTrue(json.endsWith("}"));
//    }
//
//    @Test
//    void testEncodedMessageDelegatesToJson5Message() {
//        final TestEventData event = new TestEventData("testSession", 1L, 2L);
//        final String encoded = event.json5Message();
//        final String json5 = event.json5Message(); // Call directly to compare
//        assertEquals(json5, encoded);
//    }
//
//    @Test
//    void testReadJson5MessageAllFields() {
//        final TestEventData event = new TestEventData();
//        event.readJson5("{_:abc,$:5,t:10}");
//        assertEquals("abc", event.getSessionUuid());
//        assertEquals(5L, event.getPosition());
//        assertEquals(10L, event.getLastCurrentTime());
//    }
//
//    @Test
//    void testReadJson5MessageMissingSessionUuid() {
//        final TestEventData event = new TestEventData("initial", 1L, 2L); // Set initial values
//        event.readJson5("{$:5,t:10}"); // Missing sessionUuid
//        assertEquals("initial", event.getSessionUuid()); // Should retain initial value
//        assertEquals(5L, event.getPosition());
//        assertEquals(10L, event.getLastCurrentTime());
//    }
//
//    @Test
//    void testReadJson5MessageMissingPosition() {
//        final TestEventData event = new TestEventData("initial", 1L, 2L);
//        event.readJson5("{_:abc,t:10}"); // Missing position
//        assertEquals("abc", event.getSessionUuid());
//        assertEquals(1L, event.getPosition()); // Should retain initial value
//        assertEquals(10L, event.getLastCurrentTime());
//    }
//
//    @Test
//    void testReadJson5MessageMissingTime() {
//        final TestEventData event = new TestEventData("initial", 1L, 2L);
//        event.readJson5("{_:abc,$:5}"); // Missing time
//        assertEquals("abc", event.getSessionUuid());
//        assertEquals(5L, event.getPosition());
//        assertEquals(2L, event.getLastCurrentTime()); // Should retain initial value
//    }
//
//    @Test
//    void testReadJson5MessageFieldsOutOfOrder() {
//        final TestEventData event = new TestEventData();
//        event.readJson5("{t:10,_:abc,$:5}"); // Fields out of order
//        assertEquals("abc", event.getSessionUuid());
//        assertEquals(5L, event.getPosition());
//        assertEquals(10L, event.getLastCurrentTime());
//    }
//
//    @Test
//    void testReadJson5MessageEmptyString() {
//        final TestEventData event = new TestEventData("initial", 1L, 2L);
//        event.readJson5(""); // Empty string
//        assertEquals("initial", event.getSessionUuid());
//        assertEquals(1L, event.getPosition());
//        assertEquals(2L, event.getLastCurrentTime());
//    }
//
//    @Test
//    void testReadJson5MessagePartialString() {
//        final TestEventData event = new TestEventData("initial", 1L, 2L);
//        event.readJson5("{_:abc"); // Partial string
//        assertEquals("abc", event.getSessionUuid());
//        assertEquals(1L, event.getPosition());
//        assertEquals(2L, event.getLastCurrentTime());
//    }
}
