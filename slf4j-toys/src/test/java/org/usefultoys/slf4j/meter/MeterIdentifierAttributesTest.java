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
package org.usefultoys.slf4j.meter;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.impl.MockLogger;
import org.slf4j.impl.MockLoggerEvent;
import org.usefultoys.slf4j.LoggerFactory;
import org.usefultoys.slf4j.Session;
import org.usefultoys.slf4j.SessionConfig;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Daniel Felix Ferber
 */
class MeterIdentifierAttributesTest {

    @BeforeAll
    static void validate() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    @org.junit.jupiter.api.Test
    void testIdentifierAttributes() {
        final String category1 = "TestCategory1";
        final MockLogger logger1 = (MockLogger) LoggerFactory.getLogger(category1);
        logger1.setEnabled(false);
        logger1.clearEvents();

        final Meter m1 = new Meter(logger1);
        assertTrue(Session.uuid.endsWith(m1.getSessionUuid()));
        assertEquals(category1, m1.getCategory());
        assertNull(m1.getOperation());
        assertEquals(1L, m1.getPosition());

        final Meter m2 = new Meter(logger1);
        assertTrue(Session.uuid.endsWith(m2.getSessionUuid()));
        assertEquals(category1, m2.getCategory());
        assertNull(m2.getOperation());
        assertEquals(2L, m2.getPosition());

        final String category2 = "TestCategory2";
        final MockLogger logger2 = (MockLogger) LoggerFactory.getLogger(category2);
        logger2.setEnabled(false);
        logger2.clearEvents();

        final Meter m3 = new Meter(logger2);
        assertTrue(Session.uuid.endsWith(m3.getSessionUuid()));
        assertEquals(category2, m3.getCategory());
        assertNull(m3.getOperation());
        assertEquals(1L, m3.getPosition());

        final Meter m4 = new Meter(logger2);
        assertTrue(Session.uuid.endsWith(m4.getSessionUuid()));
        assertEquals(category2, m4.getCategory());
        assertNull(m3.getOperation());
        assertEquals(2L, m4.getPosition());
    }
    
    @Test
    void testIdentifierAttributesWithName() {
        final String category = "TestCategory3";
        final MockLogger logger = (MockLogger) LoggerFactory.getLogger(category);
        logger.setEnabled(false);
        logger.clearEvents();

        final Meter m1 = new Meter(logger);
        assertTrue(Session.uuid.endsWith(m1.getSessionUuid()));
        assertEquals(category, m1.getCategory());
        assertNull(m1.getOperation());
        assertEquals(1L, m1.getPosition());

        final Meter m2 = new Meter(logger, "op");
        assertTrue(Session.uuid.endsWith(m2.getSessionUuid()));
        assertEquals(category, m2.getCategory());
        assertEquals("op", m2.getOperation());
        assertEquals(1L, m2.getPosition());

        final Meter m3 = new Meter(logger, "op");
        assertTrue(Session.uuid.endsWith(m3.getSessionUuid()));
        assertEquals(category, m3.getCategory());
        assertEquals("op", m3.getOperation());
        assertEquals(2L, m3.getPosition());

        final Meter m4 = new Meter(logger, "po");
        assertTrue(Session.uuid.endsWith(m4.getSessionUuid()));
        assertEquals(category, m4.getCategory());
        assertEquals("po", m4.getOperation());
        assertEquals(1L, m4.getPosition());

        final Meter m5 = new Meter(logger, "po");
        assertTrue(Session.uuid.endsWith(m5.getSessionUuid()));
        assertEquals(category, m5.getCategory());
        assertEquals("po", m5.getOperation());
        assertEquals(2L, m5.getPosition());
    }
    
    @Test
    void testIdentifierAttributesWithSubmeter() {
        final String category = "TestCategory4";
        final MockLogger logger = (MockLogger) LoggerFactory.getLogger(category);
        logger.setEnabled(false);
        logger.clearEvents();

        final Meter m1 = new Meter(logger);
        assertTrue(Session.uuid.endsWith(m1.getSessionUuid()));
        assertEquals(category, m1.getCategory());
        assertNull(m1.getOperation());
        assertEquals(1L, m1.getPosition());

        final Meter m2 = m1.sub("rs");
        assertTrue(Session.uuid.endsWith(m2.getSessionUuid()));
        assertEquals(category, m2.getCategory());
        assertEquals("rs", m2.getOperation());
        assertEquals(1L, m2.getPosition());

        final Meter m3 = m1.sub("rs");
        assertTrue(Session.uuid.endsWith(m3.getSessionUuid()));
        assertEquals(category, m3.getCategory());
        assertEquals("rs", m3.getOperation());
        assertEquals(2L, m3.getPosition());
        
        final Meter m4 = m1.sub("sr");
        assertTrue(Session.uuid.endsWith(m4.getSessionUuid()));
        assertEquals(category, m4.getCategory());
        assertEquals("sr", m4.getOperation());
        assertEquals(1L, m4.getPosition());

        final Meter m5 = m1.sub("sr");
        assertTrue(Session.uuid.endsWith(m5.getSessionUuid()));
        assertEquals(category, m5.getCategory());
        assertEquals("sr", m5.getOperation());
        assertEquals(2L, m5.getPosition());

        // Meter with null operation create a meter with the same category as the parent meter.
        // Therefore, the position will be incremented.
        logger.clearEvents();
        logger.setEnabled(true);
        final Meter m6 = m1.sub(null);
        assertTrue(Session.uuid.endsWith(m6.getSessionUuid()));
        assertEquals(category, m6.getCategory());
        assertNull(m6.getOperation());
        assertEquals(2L, m6.getPosition());
        logger.assertEvent(0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL, "Illegal call to Meter.sub(name): Null argument. id=TestCategory4#1");
        logger.clearEvents();

    }
    
    @Test
    void testIdentifierAttributesWithSubSubmeter() {
        final String category = "TestCategory5";
        final MockLogger logger = (MockLogger) LoggerFactory.getLogger(category);
        logger.setEnabled(false);
        logger.clearEvents();

        final Meter m1 = new Meter(logger);
        assertTrue(Session.uuid.endsWith(m1.getSessionUuid()));
        assertEquals(category, m1.getCategory());
        assertNull(m1.getOperation());
        assertEquals(1L, m1.getPosition());

        final Meter m2 = m1.sub("rs");
        assertTrue(Session.uuid.endsWith(m2.getSessionUuid()));
        assertEquals(category, m2.getCategory());
        assertEquals("rs", m2.getOperation());
        assertEquals(1L, m2.getPosition());

        final Meter m3 = m2.sub("sr");
        assertTrue(Session.uuid.endsWith(m3.getSessionUuid()));
        assertEquals(category, m3.getCategory());
        assertEquals("rs/sr", m3.getOperation());
        assertEquals(1L, m3.getPosition());

        final Meter m4 = m2.sub("sr");
        assertTrue(Session.uuid.endsWith(m4.getSessionUuid()));
        assertEquals(category, m4.getCategory());
        assertEquals("rs/sr", m4.getOperation());
        assertEquals(2L, m4.getPosition());

        final Meter m5 = m4.sub("sr");
        assertTrue(Session.uuid.endsWith(m5.getSessionUuid()));
        assertEquals(category, m5.getCategory());
        assertEquals("rs/sr/sr", m5.getOperation());
        assertEquals(1L, m5.getPosition());

        // Meter with null operation create a meter with the same category as the parent meter.
        // Therefore, the position will be incremented.
        logger.clearEvents();
        logger.setEnabled(true);
        final Meter m6 = m4.sub(null);
        assertTrue(Session.uuid.endsWith(m6.getSessionUuid()));
        assertEquals(category, m6.getCategory());
        assertEquals("rs/sr", m6.getOperation());
        assertEquals(3L, m6.getPosition());
        logger.assertEvent(0, MockLoggerEvent.Level.ERROR, Markers.ILLEGAL, "Illegal call to Meter.sub(name): Null argument. id=TestCategory5/rs/sr#2");
        logger.clearEvents();
    }
    
    @Test
    void testIdentifierAttributesWithNameAndSubmeter() {
        final String category = "TestCategory6";
        final MockLogger logger = (MockLogger) LoggerFactory.getLogger(category);
        logger.setEnabled(false);
        logger.clearEvents();

        final Meter m1 = new Meter(logger, "n");
        assertTrue(Session.uuid.endsWith(m1.getSessionUuid()));
        assertEquals(category, m1.getCategory());
        assertEquals("n", m1.getOperation());
        assertEquals(1L, m1.getPosition());

        final Meter m2 = m1.sub("rs");
        assertTrue(Session.uuid.endsWith(m2.getSessionUuid()));
        assertEquals(category, m2.getCategory());
        assertEquals("n/rs", m2.getOperation());
        assertEquals(1L, m2.getPosition());

        final Meter m3 = m1.sub("rs");
        assertTrue(Session.uuid.endsWith(m3.getSessionUuid()));
        assertEquals(category, m3.getCategory());
        assertEquals("n/rs", m3.getOperation());
        assertEquals(2L, m3.getPosition());
        
        final Meter m4 = m1.sub("sr");
        assertTrue(Session.uuid.endsWith(m4.getSessionUuid()));
        assertEquals(category, m4.getCategory());
        assertEquals("n/sr", m4.getOperation());
        assertEquals(1L, m4.getPosition());

        final Meter m5 = m1.sub("sr");
        assertTrue(Session.uuid.endsWith(m5.getSessionUuid()));
        assertEquals(category, m5.getCategory());
        assertEquals("n/sr", m5.getOperation());
        assertEquals(2L, m5.getPosition());
    }

 }
