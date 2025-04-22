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
package org.usefultoys.slf4j.meter;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.impl.TestLogger;
import org.slf4j.impl.TestLoggerEvent;
import org.slf4j.impl.TestLoggerEvent.Level;
import org.usefultoys.slf4j.LoggerFactory;
import org.usefultoys.slf4j.SessionConfig;

import java.nio.charset.Charset;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Daniel Felix Ferber
 */
@SuppressWarnings("UnusedAssignment")
public class MeterMessageTest {

    static final String meterCategory = "category";
    static final String meterName = "name";
    static final String meterName2 = "eman";
    final String title = "Example of execution.";
    static final TestLogger logger = (TestLogger) LoggerFactory.getLogger(meterCategory);

    static final String MESSAGE_START_PREFIX = "STARTED";
    static final String MESSAGE_PROGRESS_PREFIX = "PROGRESS";
    static final String MESSAGE_SLOW_PREFIX = "OK (Slow)";
    static final String MESSAGE_OK_PREFIX = "OK";
    static final String MESSAGE_REJECT_PREFIX = "REJECT";
    static final String MESSAGE_FAIL_PREFIX = "FAIL";

    public MeterMessageTest() {
    }

    @BeforeAll
    static void validate() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    @BeforeAll
    public static void configureMeterSettings() {
        MeterConfig.progressPeriodMilliseconds = 0;
        MeterConfig.printCategory = false;
        MeterConfig.printStatus = true;
    }

    @BeforeEach
    public void clearEvents() {
        logger.clearEvents();
        MeterConfig.printCategory = false;
    }

    @Test
    public void testOk() {
        final Meter m = new Meter(logger).start().ok();

        assertEquals(4, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent = logger.getEvent(3);
        assertEquals(Markers.MSG_START, startEvent.getMarker());
        assertEquals(Markers.MSG_OK, stopEvent.getMarker());
        assertEquals(Markers.DATA_START, startDataEvent.getMarker());
        assertEquals(Markers.DATA_OK, stopDataEvent.getMarker());
        assertEquals(Level.DEBUG, startEvent.getLevel());
        assertEquals(Level.INFO, stopEvent.getLevel());
        assertEquals(Level.TRACE, startDataEvent.getLevel());
        assertEquals(Level.TRACE, stopDataEvent.getLevel());
        assertTrue(startEvent.getFormattedMessage().startsWith(MESSAGE_START_PREFIX));
        assertTrue(stopEvent.getFormattedMessage().startsWith(MESSAGE_OK_PREFIX));
        assertFalse(startEvent.getFormattedMessage().contains(meterCategory));
        assertFalse(stopEvent.getFormattedMessage().contains(meterCategory));

//        Assertions.assertEquals(null, data.getOperation());
//        Assertions.assertEquals(meterCategory, data.getCategory());
//        Assertions.assertEquals(null, data.getDescription());
//        Assertions.assertEquals(null, data.getOkPath());
//        Assertions.assertEquals(null, data.getRejectPath());
//        Assertions.assertEquals(null, data.getFailPath());
//        Assertions.assertEquals(null, data.getFailMessage());
//        Assertions.assertTrue(Session.uuid.endsWith(data.getSessionUuid()));
//
//        Assertions.assertEquals(null, data.getOperation());
//        Assertions.assertEquals(meterCategory, data.getCategory());
//        Assertions.assertEquals(null, data.getDescription());
//        Assertions.assertEquals(null, data.getOkPath());
//        Assertions.assertEquals(null, data.getRejectPath());
//        Assertions.assertEquals(null, data.getFailPath());
//        Assertions.assertEquals(null, data.getFailMessage());
//        Assertions.assertTrue(Session.uuid.endsWith(data.getSessionUuid()));
    }

    @Test
    public void testOkPrintCategoryEnabled() {
        MeterConfig.printCategory = true;
        final Meter m = new Meter(logger).start().ok();

        assertEquals(4, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent = logger.getEvent(3);
        assertEquals(Markers.MSG_START, startEvent.getMarker());
        assertEquals(Markers.MSG_OK, stopEvent.getMarker());
        assertEquals(Markers.DATA_START, startDataEvent.getMarker());
        assertEquals(Markers.DATA_OK, stopDataEvent.getMarker());
        assertEquals(Level.DEBUG, startEvent.getLevel());
        assertEquals(Level.INFO, stopEvent.getLevel());
        assertEquals(Level.TRACE, startDataEvent.getLevel());
        assertEquals(Level.TRACE, stopDataEvent.getLevel());
        assertTrue(startEvent.getFormattedMessage().startsWith(MESSAGE_START_PREFIX));
        assertTrue(stopEvent.getFormattedMessage().startsWith(MESSAGE_OK_PREFIX));
        assertTrue(startEvent.getFormattedMessage().contains(meterCategory));
        assertTrue(stopEvent.getFormattedMessage().contains(meterCategory));

//        Assertions.assertEquals(null, data.getOperation());
//        Assertions.assertEquals(meterCategory, data.getCategory());
//        Assertions.assertEquals(null, data.getDescription());
//        Assertions.assertEquals(null, data.getOkPath());
//        Assertions.assertEquals(null, data.getRejectPath());
//        Assertions.assertEquals(null, data.getFailPath());
//        Assertions.assertEquals(null, data.getFailMessage());
//        Assertions.assertTrue(Session.uuid.endsWith(data.getSessionUuid()));
//
//        Assertions.assertEquals(null, data.getOperation());
//        Assertions.assertEquals(meterCategory, data.getCategory());
//        Assertions.assertEquals(null, data.getDescription());
//        Assertions.assertEquals(null, data.getOkPath());
//        Assertions.assertEquals(null, data.getRejectPath());
//        Assertions.assertEquals(null, data.getFailPath());
//        Assertions.assertEquals(null, data.getFailMessage());
//        Assertions.assertTrue(Session.uuid.endsWith(data.getSessionUuid()));
    }

    @Test
    public void testOkWithDescription() {
        final Meter m = new Meter(logger).m(title).start().ok();

        assertEquals(4, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent = logger.getEvent(3);
        assertEquals(Markers.MSG_START, startEvent.getMarker());
        assertEquals(Markers.MSG_OK, stopEvent.getMarker());
        assertEquals(Markers.DATA_START, startDataEvent.getMarker());
        assertEquals(Markers.DATA_OK, stopDataEvent.getMarker());
        assertEquals(Level.DEBUG, startEvent.getLevel());
        assertEquals(Level.INFO, stopEvent.getLevel());
        assertEquals(Level.TRACE, startDataEvent.getLevel());
        assertEquals(Level.TRACE, stopDataEvent.getLevel());
        assertTrue(startEvent.getFormattedMessage().startsWith(MESSAGE_START_PREFIX));
        assertTrue(stopEvent.getFormattedMessage().startsWith(MESSAGE_OK_PREFIX));
        assertFalse(startEvent.getFormattedMessage().contains(meterCategory));
        assertFalse(stopEvent.getFormattedMessage().contains(meterCategory));
        assertTrue(startEvent.getFormattedMessage().contains(title));
        assertTrue(stopEvent.getFormattedMessage().contains(title));

//        Assertions.assertEquals(null, data.getOperation());
//        Assertions.assertEquals(meterCategory, data.getCategory());
//        Assertions.assertEquals(title, data.getDescription());
//        Assertions.assertEquals(null, data.getOkPath());
//        Assertions.assertEquals(null, data.getRejectPath());
//        Assertions.assertEquals(null, data.getFailPath());
//        Assertions.assertEquals(null, data.getFailMessage());
//        Assertions.assertTrue(Session.uuid.endsWith(data.getSessionUuid()));
//
//        Assertions.assertEquals(null, data.getOperation());
//        Assertions.assertEquals(meterCategory, data.getCategory());
//        Assertions.assertEquals(title, data.getDescription());
//        Assertions.assertEquals(null, data.getOkPath());
//        Assertions.assertEquals(null, data.getRejectPath());
//        Assertions.assertEquals(null, data.getFailPath());
//        Assertions.assertEquals(null, data.getFailMessage());
//        Assertions.assertTrue(Session.uuid.endsWith(data.getSessionUuid()));
    }

    @Test
    public void testOkWithName() {
        final Meter m = new Meter(logger, meterName).start().ok();

        assertEquals(4, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent = logger.getEvent(3);
        assertEquals(Markers.MSG_START, startEvent.getMarker());
        assertEquals(Markers.MSG_OK, stopEvent.getMarker());
        assertEquals(Markers.DATA_START, startDataEvent.getMarker());
        assertEquals(Markers.DATA_OK, stopDataEvent.getMarker());
        assertEquals(Level.DEBUG, startEvent.getLevel());
        assertEquals(Level.INFO, stopEvent.getLevel());
        assertEquals(Level.TRACE, startDataEvent.getLevel());
        assertEquals(Level.TRACE, stopDataEvent.getLevel());
        assertTrue(startEvent.getFormattedMessage().startsWith(MESSAGE_START_PREFIX));
        assertTrue(stopEvent.getFormattedMessage().startsWith(MESSAGE_OK_PREFIX));
        assertFalse(startEvent.getFormattedMessage().contains(meterCategory));
        assertFalse(stopEvent.getFormattedMessage().contains(meterCategory));
        assertTrue(startEvent.getFormattedMessage().contains(meterName));
        assertTrue(stopEvent.getFormattedMessage().contains(meterName));

//        Assertions.assertEquals(meterName, data.getOperation());
//        Assertions.assertEquals(meterCategory, data.getCategory());
//        Assertions.assertEquals(null, data.getDescription());
//        Assertions.assertEquals(null, data.getOkPath());
//        Assertions.assertEquals(null, data.getRejectPath());
//        Assertions.assertEquals(null, data.getFailPath());
//        Assertions.assertEquals(null, data.getFailMessage());
//        Assertions.assertTrue(Session.uuid.endsWith(data.getSessionUuid()));
//
//        Assertions.assertEquals(meterName, data.getOperation());
//        Assertions.assertEquals(meterCategory, data.getCategory());
//        Assertions.assertEquals(null, data.getDescription());
//        Assertions.assertEquals(null, data.getOkPath());
//        Assertions.assertEquals(null, data.getRejectPath());
//        Assertions.assertEquals(null, data.getFailPath());
//        Assertions.assertEquals(null, data.getFailMessage());
//        Assertions.assertTrue(Session.uuid.endsWith(data.getSessionUuid()));
    }

    @Test
    public void testOkWithNamePrintCategoryEnabled() {
        MeterConfig.printCategory = true;
        final Meter m = new Meter(logger, meterName).start().ok();

        assertEquals(4, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent = logger.getEvent(3);
        assertEquals(Markers.MSG_START, startEvent.getMarker());
        assertEquals(Markers.MSG_OK, stopEvent.getMarker());
        assertEquals(Markers.DATA_START, startDataEvent.getMarker());
        assertEquals(Markers.DATA_OK, stopDataEvent.getMarker());
        assertEquals(Level.DEBUG, startEvent.getLevel());
        assertEquals(Level.INFO, stopEvent.getLevel());
        assertEquals(Level.TRACE, startDataEvent.getLevel());
        assertEquals(Level.TRACE, stopDataEvent.getLevel());
        assertTrue(startEvent.getFormattedMessage().startsWith(MESSAGE_START_PREFIX));
        assertTrue(stopEvent.getFormattedMessage().startsWith(MESSAGE_OK_PREFIX));
        assertTrue(startEvent.getFormattedMessage().contains(meterCategory));
        assertTrue(stopEvent.getFormattedMessage().contains(meterCategory));
        assertTrue(startEvent.getFormattedMessage().contains("/" + meterName));
        assertTrue(stopEvent.getFormattedMessage().contains("/" + meterName));

//        Assertions.assertEquals(meterName, data.getOperation());
//        Assertions.assertEquals(meterCategory, data.getCategory());
//        Assertions.assertEquals(null, data.getDescription());
//        Assertions.assertEquals(null, data.getOkPath());
//        Assertions.assertEquals(null, data.getRejectPath());
//        Assertions.assertEquals(null, data.getFailPath());
//        Assertions.assertEquals(null, data.getFailMessage());
//        Assertions.assertTrue(Session.uuid.endsWith(data.getSessionUuid()));
//
//        Assertions.assertEquals(meterName, data.getOperation());
//        Assertions.assertEquals(meterCategory, data.getCategory());
//        Assertions.assertEquals(null, data.getDescription());
//        Assertions.assertEquals(null, data.getOkPath());
//        Assertions.assertEquals(null, data.getRejectPath());
//        Assertions.assertEquals(null, data.getFailPath());
//        Assertions.assertEquals(null, data.getFailMessage());
//        Assertions.assertTrue(Session.uuid.endsWith(data.getSessionUuid()));
    }

    @Test
    public void testOkWithNameAndDescription() {
        final Meter m = new Meter(logger, meterName).m(title).start().ok();

        assertEquals(4, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent = logger.getEvent(3);
        assertEquals(Markers.MSG_START, startEvent.getMarker());
        assertEquals(Markers.MSG_OK, stopEvent.getMarker());
        assertEquals(Markers.DATA_START, startDataEvent.getMarker());
        assertEquals(Markers.DATA_OK, stopDataEvent.getMarker());
        assertEquals(Level.DEBUG, startEvent.getLevel());
        assertEquals(Level.INFO, stopEvent.getLevel());
        assertEquals(Level.TRACE, startDataEvent.getLevel());
        assertEquals(Level.TRACE, stopDataEvent.getLevel());
        assertTrue(startEvent.getFormattedMessage().startsWith(MESSAGE_START_PREFIX));
        assertTrue(stopEvent.getFormattedMessage().startsWith(MESSAGE_OK_PREFIX));
        assertFalse(startEvent.getFormattedMessage().contains(meterCategory));
        assertFalse(stopEvent.getFormattedMessage().contains(meterCategory));
        assertTrue(startEvent.getFormattedMessage().contains(meterName));
        assertTrue(stopEvent.getFormattedMessage().contains(meterName));
        assertTrue(startEvent.getFormattedMessage().contains(title));
        assertTrue(stopEvent.getFormattedMessage().contains(title));

//        Assertions.assertEquals(meterName, data.getOperation());
//        Assertions.assertEquals(meterCategory, data.getCategory());
//        Assertions.assertEquals(title, data.getDescription());
//        Assertions.assertEquals(null, data.getOkPath());
//        Assertions.assertEquals(null, data.getRejectPath());
//        Assertions.assertEquals(null, data.getFailPath());
//        Assertions.assertEquals(null, data.getFailMessage());
//        Assertions.assertTrue(Session.uuid.endsWith(data.getSessionUuid()));
//
//        Assertions.assertEquals(meterName, data.getOperation());
//        Assertions.assertEquals(meterCategory, data.getCategory());
//        Assertions.assertEquals(title, data.getDescription());
//        Assertions.assertEquals(null, data.getOkPath());
//        Assertions.assertEquals(null, data.getRejectPath());
//        Assertions.assertEquals(null, data.getFailPath());
//        Assertions.assertEquals(null, data.getFailMessage());
//        Assertions.assertTrue(Session.uuid.endsWith(data.getSessionUuid()));
    }

    @Test
    public void testOkWithNameAndName() {
        final Meter m = new Meter(logger, meterName).start();
        final Meter m2 = m.sub(meterName2).start().ok();
        m.ok();

        assertEquals(8, logger.getEventCount());
        final TestLoggerEvent startEvent1 = logger.getEvent(0);
        final TestLoggerEvent startDataEvent1 = logger.getEvent(1);
        final TestLoggerEvent startEvent2 = logger.getEvent(2);
        final TestLoggerEvent startDataEvent2 = logger.getEvent(3);
        final TestLoggerEvent stopEvent2 = logger.getEvent(4);
        final TestLoggerEvent stopDataEvent2 = logger.getEvent(5);
        final TestLoggerEvent stopEvent1 = logger.getEvent(6);
        final TestLoggerEvent stopDataEvent1 = logger.getEvent(7);
        assertEquals(Markers.MSG_START, startEvent2.getMarker());
        assertEquals(Markers.MSG_OK, stopEvent2.getMarker());
        assertEquals(Markers.DATA_START, startDataEvent2.getMarker());
        assertEquals(Markers.DATA_OK, stopDataEvent2.getMarker());
        assertEquals(Level.DEBUG, startEvent2.getLevel());
        assertEquals(Level.INFO, stopEvent2.getLevel());
        assertEquals(Level.TRACE, startDataEvent2.getLevel());
        assertEquals(Level.TRACE, stopDataEvent2.getLevel());
        assertTrue(startEvent2.getFormattedMessage().startsWith(MESSAGE_START_PREFIX));
        assertTrue(stopEvent2.getFormattedMessage().startsWith(MESSAGE_OK_PREFIX));
        assertFalse(startEvent2.getFormattedMessage().contains(meterCategory));
        assertFalse(stopEvent2.getFormattedMessage().contains(meterCategory));
        assertTrue(startEvent2.getFormattedMessage().contains(meterName + "/" + meterName2));
        assertTrue(stopEvent2.getFormattedMessage().contains(meterName + "/" + meterName2));

//        Assertions.assertEquals(meterName + "/" + meterName2, data.getOperation());
//        Assertions.assertEquals(meterCategory, data.getCategory());
//        Assertions.assertEquals(null, data.getDescription());
//        Assertions.assertEquals(null, data.getOkPath());
//        Assertions.assertEquals(null, data.getRejectPath());
//        Assertions.assertEquals(null, data.getFailPath());
//        Assertions.assertEquals(null, data.getFailMessage());
//        Assertions.assertTrue(Session.uuid.endsWith(data.getSessionUuid()));
//
//        Assertions.assertEquals(meterName + "/" + meterName2, data.getOperation());
//        Assertions.assertEquals(meterCategory, data.getCategory());
//        Assertions.assertEquals(null, data.getDescription());
//        Assertions.assertEquals(null, data.getOkPath());
//        Assertions.assertEquals(null, data.getRejectPath());
//        Assertions.assertEquals(null, data.getFailPath());
//        Assertions.assertEquals(null, data.getFailMessage());
//        Assertions.assertTrue(Session.uuid.endsWith(data.getSessionUuid()));
    }

    @Test
    public void testOkWithNameAndNamePrintCategoryEnabled() {
        MeterConfig.printCategory = true;
        final Meter m = new Meter(logger, meterName).start();
        final Meter m2 = m.sub(meterName2).start().ok();
        m.ok();

        assertEquals(8, logger.getEventCount());
        final TestLoggerEvent startEvent1 = logger.getEvent(0);
        final TestLoggerEvent startDataEvent1 = logger.getEvent(1);
        final TestLoggerEvent startEvent2 = logger.getEvent(2);
        final TestLoggerEvent startDataEvent2 = logger.getEvent(3);
        final TestLoggerEvent stopEvent2 = logger.getEvent(4);
        final TestLoggerEvent stopDataEvent2 = logger.getEvent(5);
        final TestLoggerEvent stopEvent1 = logger.getEvent(6);
        final TestLoggerEvent stopDataEvent1 = logger.getEvent(7);
        assertEquals(Markers.MSG_START, startEvent2.getMarker());
        assertEquals(Markers.MSG_OK, stopEvent2.getMarker());
        assertEquals(Markers.DATA_START, startDataEvent2.getMarker());
        assertEquals(Markers.DATA_OK, stopDataEvent2.getMarker());
        assertEquals(Level.DEBUG, startEvent2.getLevel());
        assertEquals(Level.INFO, stopEvent2.getLevel());
        assertEquals(Level.TRACE, startDataEvent2.getLevel());
        assertEquals(Level.TRACE, stopDataEvent2.getLevel());
        assertTrue(startEvent2.getFormattedMessage().startsWith(MESSAGE_START_PREFIX));
        assertTrue(stopEvent2.getFormattedMessage().startsWith(MESSAGE_OK_PREFIX));
        assertTrue(startEvent2.getFormattedMessage().contains(meterCategory));
        assertTrue(stopEvent2.getFormattedMessage().contains(meterCategory));
        assertTrue(startEvent2.getFormattedMessage().contains("/" + meterName + "/" + meterName2));
        assertTrue(stopEvent2.getFormattedMessage().contains("/" + meterName + "/" + meterName2));

//        Assertions.assertEquals(meterName + "/" + meterName2, data.getOperation());
//        Assertions.assertEquals(meterCategory, data.getCategory());
//        Assertions.assertEquals(null, data.getDescription());
//        Assertions.assertEquals(null, data.getOkPath());
//        Assertions.assertEquals(null, data.getRejectPath());
//        Assertions.assertEquals(null, data.getFailPath());
//        Assertions.assertEquals(null, data.getFailMessage());
//        Assertions.assertTrue(Session.uuid.endsWith(data.getSessionUuid()));
//
//        Assertions.assertEquals(meterName + "/" + meterName2, data.getOperation());
//        Assertions.assertEquals(meterCategory, data.getCategory());
//        Assertions.assertEquals(null, data.getDescription());
//        Assertions.assertEquals(null, data.getOkPath());
//        Assertions.assertEquals(null, data.getRejectPath());
//        Assertions.assertEquals(null, data.getFailPath());
//        Assertions.assertEquals(null, data.getFailMessage());
//        Assertions.assertTrue(Session.uuid.endsWith(data.getSessionUuid()));
    }

    @Test
    public void testOkPath() {
        final String path = "qwerty";
        final Meter m = new Meter(logger).start().ok(path);

        assertEquals(4, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent = logger.getEvent(3);
        assertFalse(startEvent.getFormattedMessage().contains("[" + path + "]"));
        assertTrue(stopEvent.getFormattedMessage().contains("[" + path + "]"));

//        Assertions.assertEquals(null, data.getOkPath());
//        Assertions.assertEquals(null, data.getRejectPath());
//        Assertions.assertEquals(null, data.getFailPath());
//        Assertions.assertEquals(null, data.getFailMessage());
//        Assertions.assertTrue(Session.uuid.endsWith(data.getSessionUuid()));
//
//        Assertions.assertEquals(path, data.getOkPath());
//        Assertions.assertEquals(null, data.getRejectPath());
//        Assertions.assertEquals(null, data.getFailPath());
//        Assertions.assertEquals(null, data.getFailMessage());
//        Assertions.assertTrue(Session.uuid.endsWith(data.getSessionUuid()));
    }

    @Test
    public void testPathOk() {
        final String path = "qwerty";
        final Meter m = new Meter(logger).start().path(path).ok();

        assertEquals(4, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent = logger.getEvent(3);
        assertFalse(startEvent.getFormattedMessage().contains("[" + path + "]"));
        assertTrue(stopEvent.getFormattedMessage().contains("[" + path + "]"));

//        Assertions.assertEquals(null, data.getOkPath());
//        Assertions.assertEquals(null, data.getRejectPath());
//        Assertions.assertEquals(null, data.getFailPath());
//        Assertions.assertEquals(null, data.getFailMessage());
//        Assertions.assertTrue(Session.uuid.endsWith(data.getSessionUuid()));
//
//        Assertions.assertEquals(path, data.getOkPath());
//        Assertions.assertEquals(null, data.getRejectPath());
//        Assertions.assertEquals(null, data.getFailPath());
//        Assertions.assertEquals(null, data.getFailMessage());
//        Assertions.assertTrue(Session.uuid.endsWith(data.getSessionUuid()));
    }

    @Test
    public void testPathPathOk() {
        final String path = "qwerty";
        final String path2 = "ytrewq";
        final Meter m = new Meter(logger).start().path(path).path(path2).ok();

        assertEquals(4, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent = logger.getEvent(3);
        assertFalse(startEvent.getFormattedMessage().contains("[" + path + "]"));
        assertFalse(startEvent.getFormattedMessage().contains("[" + path2 + "]"));
        assertTrue(stopEvent.getFormattedMessage().contains("[" + path2 + "]"));

//        Assertions.assertEquals(null, data.getOkPath());
//        Assertions.assertEquals(null, data.getRejectPath());
//        Assertions.assertEquals(null, data.getFailPath());
//        Assertions.assertEquals(null, data.getFailMessage());
//        Assertions.assertTrue(Session.uuid.endsWith(data.getSessionUuid()));
//
//        Assertions.assertEquals(path2, data.getOkPath());
//        Assertions.assertEquals(null, data.getRejectPath());
//        Assertions.assertEquals(null, data.getFailPath());
//        Assertions.assertEquals(null, data.getFailMessage());
//        Assertions.assertTrue(Session.uuid.endsWith(data.getSessionUuid()));
    }

    @Test
    public void testPathOkPath() {
        final String path = "qwerty";
        final String path2 = "ytrewq";
        final Meter m = new Meter(logger).start().path(path).ok(path2);

        assertEquals(4, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent = logger.getEvent(3);
        assertFalse(startEvent.getFormattedMessage().contains("[" + path + "]"));
        assertFalse(startEvent.getFormattedMessage().contains("[" + path2 + "]"));
        assertTrue(stopEvent.getFormattedMessage().contains("[" + path2 + "]"));

//        Assertions.assertEquals(null, data.getOkPath());
//        Assertions.assertEquals(null, data.getRejectPath());
//        Assertions.assertEquals(null, data.getFailPath());
//        Assertions.assertEquals(null, data.getFailMessage());
//        Assertions.assertTrue(Session.uuid.endsWith(data.getSessionUuid()));
//
//        Assertions.assertEquals(path2, data.getOkPath());
//        Assertions.assertEquals(null, data.getRejectPath());
//        Assertions.assertEquals(null, data.getFailPath());
//        Assertions.assertEquals(null, data.getFailMessage());
//        Assertions.assertTrue(Session.uuid.endsWith(data.getSessionUuid()));
    }

    @Test
    public void testReject() {
        final String reject = "qwerty";
        final Meter m = new Meter(logger).start().reject(reject);

        assertEquals(4, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent = logger.getEvent(3);
        assertEquals(Markers.MSG_START, startEvent.getMarker());
        assertEquals(Markers.MSG_REJECT, stopEvent.getMarker());
        assertEquals(Markers.DATA_START, startDataEvent.getMarker());
        assertEquals(Markers.DATA_REJECT, stopDataEvent.getMarker());
        assertEquals(Level.DEBUG, startEvent.getLevel());
        assertEquals(Level.INFO, stopEvent.getLevel());
        assertEquals(Level.TRACE, startDataEvent.getLevel());
        assertEquals(Level.TRACE, stopDataEvent.getLevel());
        assertTrue(startEvent.getFormattedMessage().contains(MESSAGE_START_PREFIX));
        assertTrue(stopEvent.getFormattedMessage().contains(MESSAGE_REJECT_PREFIX));
        assertFalse(startEvent.getFormattedMessage().contains(reject));
        assertTrue(stopEvent.getFormattedMessage().contains(reject));

//        Assertions.assertEquals(null, data.getOkPath());
//        Assertions.assertEquals(null, data.getRejectPath());
//        Assertions.assertEquals(null, data.getFailPath());
//        Assertions.assertEquals(null, data.getFailMessage());
//        Assertions.assertTrue(Session.uuid.endsWith(data.getSessionUuid()));
//
//        Assertions.assertEquals(null, data.getOperation());
//        Assertions.assertEquals(meterCategory, data.getCategory());
//        Assertions.assertEquals(null, data.getDescription());
//        Assertions.assertEquals(null, data.getOkPath());
//        Assertions.assertEquals(reject, data.getRejectPath());
//        Assertions.assertEquals(null, data.getFailPath());
//        Assertions.assertEquals(null, data.getFailMessage());
//        Assertions.assertTrue(Session.uuid.endsWith(data.getSessionUuid()));
    }

    @Test
    public void testPathReject() {
        final String path = "ytrewq";
        final String reject = "qwerty";
        final Meter m = new Meter(logger).start().path(path).reject(reject);

        assertEquals(4, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent = logger.getEvent(3);
        assertEquals(Markers.MSG_START, startEvent.getMarker());
        assertEquals(Markers.MSG_REJECT, stopEvent.getMarker());
        assertEquals(Markers.DATA_START, startDataEvent.getMarker());
        assertEquals(Markers.DATA_REJECT, stopDataEvent.getMarker());
        assertEquals(Level.DEBUG, startEvent.getLevel());
        assertEquals(Level.INFO, stopEvent.getLevel());
        assertEquals(Level.TRACE, startDataEvent.getLevel());
        assertEquals(Level.TRACE, stopDataEvent.getLevel());
        assertTrue(startEvent.getFormattedMessage().contains(MESSAGE_START_PREFIX));
        assertTrue(stopEvent.getFormattedMessage().contains(MESSAGE_REJECT_PREFIX));
        assertFalse(startEvent.getFormattedMessage().contains(reject));
        assertTrue(stopEvent.getFormattedMessage().contains(reject));
        assertFalse(startEvent.getFormattedMessage().contains(path));
        assertFalse(stopEvent.getFormattedMessage().contains(path));

//        Assertions.assertEquals(null, data.getOkPath());
//        Assertions.assertEquals(null, data.getRejectPath());
//        Assertions.assertEquals(null, data.getFailPath());
//        Assertions.assertEquals(null, data.getFailMessage());
//        Assertions.assertTrue(Session.uuid.endsWith(data.getSessionUuid()));
//
//        Assertions.assertEquals(null, data.getOperation());
//        Assertions.assertEquals(meterCategory, data.getCategory());
//        Assertions.assertEquals(null, data.getDescription());
//        Assertions.assertEquals(null, data.getOkPath());
//        Assertions.assertEquals(reject, data.getRejectPath());
//        Assertions.assertEquals(null, data.getFailPath());
//        Assertions.assertEquals(null, data.getFailMessage());
//        Assertions.assertTrue(Session.uuid.endsWith(data.getSessionUuid()));
    }

    @Test
    public void testFail() {
        final String exceptionStr = "bad exception";
        final Meter m = new Meter(logger).start().fail(new Exception(exceptionStr));

        assertEquals(4, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent = logger.getEvent(3);
        assertEquals(Markers.MSG_START, startEvent.getMarker());
        assertEquals(Markers.MSG_FAIL, stopEvent.getMarker());
        assertEquals(Markers.DATA_START, startDataEvent.getMarker());
        assertEquals(Markers.DATA_FAIL, stopDataEvent.getMarker());
        assertEquals(Level.DEBUG, startEvent.getLevel());
        assertEquals(Level.ERROR, stopEvent.getLevel());
        assertEquals(Level.TRACE, startDataEvent.getLevel());
        assertEquals(Level.TRACE, stopDataEvent.getLevel());
        assertTrue(startEvent.getFormattedMessage().contains(MESSAGE_START_PREFIX));
        assertTrue(stopEvent.getFormattedMessage().contains(MESSAGE_FAIL_PREFIX));
        assertTrue(stopEvent.getFormattedMessage().contains(Exception.class.getName()));
        assertTrue(stopEvent.getFormattedMessage().contains(exceptionStr));

//        Assertions.assertEquals(null, data.getOkPath());
//        Assertions.assertEquals(null, data.getRejectPath());
//        Assertions.assertEquals(null, data.getFailPath());
//        Assertions.assertEquals(null, data.getFailMessage());
//        Assertions.assertTrue(Session.uuid.endsWith(data.getSessionUuid()));
//
//        Assertions.assertEquals(null, data.getOkPath());
//        Assertions.assertEquals(null, data.getRejectPath());
//        Assertions.assertEquals(Exception.class.getName(), data.getFailPath());
//        Assertions.assertEquals(exceptionStr, data.getFailMessage());
//        Assertions.assertTrue(Session.uuid.endsWith(data.getSessionUuid()));
    }

    @Test
    public void testOkSlowness() {
        final String title = "Example of execution that succeeds but exceeds time limit.";
        final Meter m = new Meter(logger).m(title).limitMilliseconds(200).start();
        final String flow = "ytrewq";
        try {
            /* Run stuff that may delay. */
            m.path((Object) flow);
            Thread.sleep(220);
            m.ok();
        } catch (final Exception e) {
            m.fail(e);
        }
        assertTrue(m.isOK());
        assertFalse(m.isReject());
        assertFalse(m.isFail());
        assertTrue(m.isSlow());
        assertEquals(4, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent stopEvent = logger.getEvent(2);
        final TestLoggerEvent stopDataEvent = logger.getEvent(3);
        assertEquals(Markers.MSG_START, startEvent.getMarker());
        assertEquals(Markers.MSG_SLOW_OK, stopEvent.getMarker());
        assertEquals(Markers.DATA_START, startDataEvent.getMarker());
        assertEquals(Markers.DATA_SLOW_OK, stopDataEvent.getMarker());
        assertEquals(Level.DEBUG, startEvent.getLevel());
        assertEquals(Level.WARN, stopEvent.getLevel());
        assertEquals(Level.TRACE, startDataEvent.getLevel());
        assertEquals(Level.TRACE, stopDataEvent.getLevel());
        assertTrue(startEvent.getFormattedMessage().contains(MESSAGE_START_PREFIX));
        assertTrue(stopEvent.getFormattedMessage().contains(MESSAGE_OK_PREFIX));
        assertTrue(stopEvent.getFormattedMessage().contains(MESSAGE_SLOW_PREFIX));
        assertTrue(startEvent.getFormattedMessage().contains(title));
        assertTrue(stopEvent.getFormattedMessage().contains(title));
        assertTrue(stopEvent.getFormattedMessage().contains(flow));
    }

    @Test
    public void testIteration() {
        MeterConfig.progressPeriodMilliseconds = 0;
        System.out.println("testIteration:");
        final String title = "Example of execution that succeeds and reports progress of completed iterations.";
        final Meter m = new Meter(logger).m(title).iterations(4).start();
        try {
            /* Run stuff that repeats. */
            for (int i = 0; i < 4; i++) {
                /* Do stuff. */
                Thread.sleep(100);
                m.inc().progress();
            }
            m.ok();
        } catch (final Exception e) {
            m.fail(e);
            // may rethrow
        }
        assertTrue(m.isOK());
        assertFalse(m.isReject());
        assertFalse(m.isFail());
        assertFalse(m.isSlow());
        assertEquals(12, logger.getEventCount());
        final TestLoggerEvent startEvent = logger.getEvent(0);
        final TestLoggerEvent startDataEvent = logger.getEvent(1);
        final TestLoggerEvent progressEvent1 = logger.getEvent(2);
        final TestLoggerEvent progressDataEvent1 = logger.getEvent(3);
        final TestLoggerEvent progressEvent2 = logger.getEvent(4);
        final TestLoggerEvent progressDataEvent2 = logger.getEvent(5);
        final TestLoggerEvent progressEvent3 = logger.getEvent(6);
        final TestLoggerEvent progressDataEvent3 = logger.getEvent(7);
        final TestLoggerEvent progressEvent4 = logger.getEvent(8);
        final TestLoggerEvent progressDataEvent4 = logger.getEvent(9);
        final TestLoggerEvent stopEvent = logger.getEvent(10);
        final TestLoggerEvent stopDataEvent = logger.getEvent(11);
        assertEquals(Markers.MSG_START, startEvent.getMarker());
        assertEquals(Markers.MSG_OK, stopEvent.getMarker());
        assertEquals(Markers.MSG_PROGRESS, progressEvent1.getMarker());
        assertEquals(Markers.MSG_PROGRESS, progressEvent2.getMarker());
        assertEquals(Markers.MSG_PROGRESS, progressEvent3.getMarker());
        assertEquals(Markers.MSG_PROGRESS, progressEvent4.getMarker());
        assertEquals(Markers.DATA_START, startDataEvent.getMarker());
        assertEquals(Markers.DATA_OK, stopDataEvent.getMarker());
        assertEquals(Markers.DATA_PROGRESS, progressDataEvent1.getMarker());
        assertEquals(Markers.DATA_PROGRESS, progressDataEvent2.getMarker());
        assertEquals(Markers.DATA_PROGRESS, progressDataEvent3.getMarker());
        assertEquals(Markers.DATA_PROGRESS, progressDataEvent4.getMarker());
        assertEquals(Level.DEBUG, startEvent.getLevel());
        assertEquals(Level.INFO, progressEvent1.getLevel());
        assertEquals(Level.INFO, progressEvent2.getLevel());
        assertEquals(Level.INFO, progressEvent3.getLevel());
        assertEquals(Level.INFO, progressEvent4.getLevel());
        assertEquals(Level.INFO, stopEvent.getLevel());
        assertEquals(Level.TRACE, startDataEvent.getLevel());
        assertEquals(Level.TRACE, progressDataEvent1.getLevel());
        assertEquals(Level.TRACE, progressDataEvent2.getLevel());
        assertEquals(Level.TRACE, progressDataEvent3.getLevel());
        assertEquals(Level.TRACE, progressDataEvent4.getLevel());
        assertEquals(Level.TRACE, stopDataEvent.getLevel());
        assertTrue(startEvent.getFormattedMessage().contains(MESSAGE_START_PREFIX));
        assertTrue(progressEvent1.getFormattedMessage().contains(MESSAGE_PROGRESS_PREFIX));
        assertTrue(progressEvent2.getFormattedMessage().contains(MESSAGE_PROGRESS_PREFIX));
        assertTrue(progressEvent3.getFormattedMessage().contains(MESSAGE_PROGRESS_PREFIX));
        assertTrue(progressEvent4.getFormattedMessage().contains(MESSAGE_PROGRESS_PREFIX));
        assertTrue(progressEvent1.getFormattedMessage().contains("1/4"));
        assertTrue(progressEvent2.getFormattedMessage().contains("2/4"));
        assertTrue(progressEvent3.getFormattedMessage().contains("3/4"));
        assertTrue(progressEvent4.getFormattedMessage().contains("4/4"));
        assertTrue(stopEvent.getFormattedMessage().contains(MESSAGE_OK_PREFIX));
        assertTrue(startEvent.getFormattedMessage().contains(title));
        assertTrue(progressEvent1.getFormattedMessage().contains(title));
        assertTrue(progressEvent2.getFormattedMessage().contains(title));
        assertTrue(progressEvent3.getFormattedMessage().contains(title));
        assertTrue(progressEvent4.getFormattedMessage().contains(title));
        assertTrue(stopEvent.getFormattedMessage().contains(title));
    }

    @Test
    public void testIteration2() {
        System.out.println("testIteration2:");
        final Meter m = new Meter(logger).m("Example of execution that succeeds and reports progress of completed iterations.").iterations(30).start();
        final Random r = new Random();
        try {
            /* Run stuff that repeats, step size may vary. */
            for (int i = 0; i < 30;) {
                /* Do stuff. */
                final int increment = r.nextInt(3) + 1;
                i += increment;
                m.incBy(increment).progress();
            }
            m.ok();
        } catch (final Exception e) {
            m.fail(e);
            // may rethrow
        }
        assertTrue(m.isOK());
        assertFalse(m.isSlow());
        /* Messages were already tested by testIteration() */
    }

    @Test
    public void testIteration3() {
        System.out.println("testIteration3:");
        final Meter m = new Meter(logger).m("Example of execution that succeeds and reports progress of completed iterations.").iterations(30).start();
        final Random r = new Random();
        try {
            /* Run stuff that repeats, step size may vary. */
            for (int i = 0; i < 30;) {
                /* Do stuff. */
                final int increment = r.nextInt(3) + 1;
                i += increment;
                m.incBy(i).progress();
            }
            m.ok();
        } catch (final Exception e) {
            m.fail(e);
            // may rethrow
        }
        assertTrue(m.isOK());
        assertFalse(m.isSlow());
        /* Messages were already tested by testIteration() */
    }

    /**
     * As logger is enable for debug, it will print the start message. Hence,
     * the stop message will not include context from the input message anymore.
     */
    @Test
    public void testContext0() {
        System.out.println("testContext1:");
        final String title = "Example of execution that succeeds and reports context.";

        final TestLogger logger2 = (TestLogger) LoggerFactory.getLogger("context");
        logger2.clearEvents();
        logger2.setTraceEnabled(true);
        logger2.setDebugEnabled(true);
        logger2.setInfoEnabled(true);
        logger2.setWarnEnabled(true);
        logger2.setErrorEnabled(true);
        final Meter m = new Meter(logger2).m(title).limitMilliseconds(200);
        final String inputValue = "for example, an value received as input";
        final String outputValue = "for example, an value produced as output";
        final String causeValue = "for example, an identifier for the failure cause";
        final String detailValue = "for example, an value calculated during execution";
        m.ctx("input", inputValue).start();
        try {
            /* Run stuff. */
            m.ctx("detail", detailValue);
            /* Run stuff. */
            m.ctx("output", outputValue).ok();
        } catch (final Exception e) {
            m.ctx("cause", causeValue).fail(e);
            // may rethrow
        }
        assertTrue(m.isOK());
        assertFalse(m.isSlow());
        assertEquals(4, logger2.getEventCount());
        final TestLoggerEvent startEvent = logger2.getEvent(0);
        final TestLoggerEvent startDataEvent = logger2.getEvent(1);
        final TestLoggerEvent stopEvent = logger2.getEvent(2);
        final TestLoggerEvent stopDataEvent = logger2.getEvent(3);
        assertEquals(Markers.MSG_START, startEvent.getMarker());
        assertEquals(Markers.MSG_OK, stopEvent.getMarker());
        assertEquals(Markers.DATA_START, startDataEvent.getMarker());
        assertEquals(Markers.DATA_OK, stopDataEvent.getMarker());
        assertEquals(Level.DEBUG, startEvent.getLevel());
        assertEquals(Level.INFO, stopEvent.getLevel());
        assertEquals(Level.TRACE, startDataEvent.getLevel());
        assertEquals(Level.TRACE, stopDataEvent.getLevel());
        assertTrue(startEvent.getFormattedMessage().contains(MESSAGE_START_PREFIX));
        assertTrue(stopEvent.getFormattedMessage().contains(MESSAGE_OK_PREFIX));
        assertTrue(startEvent.getFormattedMessage().contains(title));
        assertTrue(stopEvent.getFormattedMessage().contains(title));
        assertTrue(startEvent.getFormattedMessage().contains(inputValue));
        assertFalse(stopEvent.getFormattedMessage().contains(inputValue));
        assertTrue(startEvent.getFormattedMessage().contains("input="));
        assertFalse(stopEvent.getFormattedMessage().contains("input="));
        assertFalse(startEvent.getFormattedMessage().contains(outputValue));
        assertTrue(stopEvent.getFormattedMessage().contains(outputValue));
        assertFalse(startEvent.getFormattedMessage().contains("output="));
        assertTrue(stopEvent.getFormattedMessage().contains("output="));
        assertFalse(startEvent.getFormattedMessage().contains(detailValue));
        assertTrue(stopEvent.getFormattedMessage().contains(detailValue));
        assertFalse(startEvent.getFormattedMessage().contains("detail="));
        assertTrue(stopEvent.getFormattedMessage().contains("detail="));
    }

    /**
     * As logger is not enable for debug, it will print the start message.
     * Hence, the stop message will not include context from the input message
     * anymore.
     */
    @Test
    public void testContext1() {
        System.out.println("testContext1:");
        final String title = "Example of execution that succeeds and reports context.";

        final TestLogger logger2 = (TestLogger) LoggerFactory.getLogger("context");
        logger2.clearEvents();
        logger2.setTraceEnabled(false);
        logger2.setDebugEnabled(false);
        logger2.setInfoEnabled(true);
        logger2.setWarnEnabled(true);
        logger2.setErrorEnabled(true);
        final Meter m = new Meter(logger2).m(title).limitMilliseconds(200);
        final String inputValue = "for example, an value received as input";
        final String outputValue = "for example, an value produced as output";
        final String causeValue = "for example, an identifier for the failure cause";
        final String detailValue = "for example, an value calculated during execution";
        m.ctx("input", inputValue).start();
        try {
            /* Run stuff. */
            m.ctx("detail", detailValue);
            /* Run stuff. */
            m.ctx("output", outputValue).ok();
        } catch (final Exception e) {
            m.ctx("cause", causeValue).fail(e);
            // may rethrow
        }
        assertTrue(m.isOK());
        assertFalse(m.isSlow());
        assertEquals(1, logger2.getEventCount());
        final TestLoggerEvent stopEvent = logger2.getEvent(0);
        assertEquals(Markers.MSG_OK, stopEvent.getMarker());
        assertEquals(Level.INFO, stopEvent.getLevel());
        assertTrue(stopEvent.getFormattedMessage().contains(MESSAGE_OK_PREFIX));
        assertTrue(stopEvent.getFormattedMessage().contains(title));
        assertTrue(stopEvent.getFormattedMessage().contains(inputValue));
        assertTrue(stopEvent.getFormattedMessage().contains("input="));
        assertTrue(stopEvent.getFormattedMessage().contains(outputValue));
        assertTrue(stopEvent.getFormattedMessage().contains("output="));
        assertTrue(stopEvent.getFormattedMessage().contains(detailValue));
        assertTrue(stopEvent.getFormattedMessage().contains("detail="));
    }

    /**
     * Tests context overriding
     */
    @Test
    public void testContext2() {
        System.out.println("testContext1:");
        final String title = "Example of execution that succeeds and reports context.";

        final TestLogger logger2 = (TestLogger) LoggerFactory.getLogger("context");
        logger2.clearEvents();
        logger2.setTraceEnabled(true);
        logger2.setDebugEnabled(true);
        logger2.setInfoEnabled(true);
        logger2.setWarnEnabled(true);
        logger2.setErrorEnabled(true);
        final Meter m = new Meter(logger2).m(title).limitMilliseconds(200);
        final String inputValue = "for example, an value received as input";
        final String outputValue = "for example, an value produced as output";
        final String causeValue = "for example, an identifier for the failure cause";
        final String detailValue1 = "for example, an value calculated before execution";
        final String detailValue2 = "for example, an value calculated during execution";
        final String extraValue1 = "for example, another value calculated during execution";
        final String extraValue2 = "for example, an value overriden during execution";
        final String otherValue = "for example, an value calculated and removed during execution";
        m.ctx("input", inputValue).ctx("detail", detailValue1).start();
        try {
            /* Run stuff. */
            m.ctx("detail", detailValue2);
            m.ctx("extra", extraValue1);
            /* Run stuff. */
            m.ctx("extra", extraValue2);
            m.ctx("other", otherValue);
            /* Run stuff. */
            m.unctx("other");
            /* Run stuff. */
            Thread.sleep(1);
            m.ctx("output", outputValue).ok();
        } catch (final Exception e) {
            m.ctx("cause", causeValue).fail(e);
            // may rethrow
        }

        assertTrue(m.isOK());
        assertFalse(m.isSlow());
        assertEquals(4, logger2.getEventCount());
        final TestLoggerEvent startEvent = logger2.getEvent(0);
        final TestLoggerEvent startDataEvent = logger2.getEvent(1);
        final TestLoggerEvent stopEvent = logger2.getEvent(2);
        final TestLoggerEvent stopDataEvent = logger2.getEvent(3);
        assertEquals(Markers.MSG_START, startEvent.getMarker());
        assertEquals(Markers.MSG_OK, stopEvent.getMarker());
        assertEquals(Markers.DATA_START, startDataEvent.getMarker());
        assertEquals(Markers.DATA_OK, stopDataEvent.getMarker());
        assertEquals(Level.DEBUG, startEvent.getLevel());
        assertEquals(Level.INFO, stopEvent.getLevel());
        assertEquals(Level.TRACE, startDataEvent.getLevel());
        assertEquals(Level.TRACE, stopDataEvent.getLevel());
        assertTrue(startEvent.getFormattedMessage().contains(MESSAGE_START_PREFIX));
        assertTrue(stopEvent.getFormattedMessage().contains(MESSAGE_OK_PREFIX));
        assertTrue(startEvent.getFormattedMessage().contains(title));
        assertTrue(stopEvent.getFormattedMessage().contains(title));
        assertTrue(startEvent.getFormattedMessage().contains(inputValue));
        assertFalse(stopEvent.getFormattedMessage().contains(inputValue));
        assertTrue(startEvent.getFormattedMessage().contains("input="));
        assertFalse(stopEvent.getFormattedMessage().contains("input="));
        assertFalse(startEvent.getFormattedMessage().contains(outputValue));
        assertTrue(stopEvent.getFormattedMessage().contains(outputValue));
        assertFalse(startEvent.getFormattedMessage().contains("output="));
        assertTrue(stopEvent.getFormattedMessage().contains("output="));
        assertTrue(startEvent.getFormattedMessage().contains(detailValue1));
        assertFalse(stopEvent.getFormattedMessage().contains(detailValue1));
        assertFalse(startEvent.getFormattedMessage().contains(detailValue2));
        assertTrue(stopEvent.getFormattedMessage().contains(detailValue2));
        assertTrue(startEvent.getFormattedMessage().contains("detail="));
        assertTrue(stopEvent.getFormattedMessage().contains("detail="));
        assertFalse(startEvent.getFormattedMessage().contains(extraValue1));
        assertFalse(stopEvent.getFormattedMessage().contains(extraValue1));
        assertFalse(startEvent.getFormattedMessage().contains(extraValue2));
        assertTrue(stopEvent.getFormattedMessage().contains(extraValue2));
        assertFalse(startEvent.getFormattedMessage().contains("extra="));
        assertTrue(stopEvent.getFormattedMessage().contains("extra="));
        assertFalse(startEvent.getFormattedMessage().contains(otherValue));
        assertFalse(stopEvent.getFormattedMessage().contains(otherValue));
        assertFalse(startEvent.getFormattedMessage().contains("other="));
        assertFalse(stopEvent.getFormattedMessage().contains("other="));
    }
}
