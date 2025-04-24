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
import org.slf4j.impl.MockLogger;
import org.usefultoys.slf4j.LoggerFactory;
import org.usefultoys.slf4j.SessionConfig;

import java.util.concurrent.atomic.AtomicLong;
import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Daniel
 */
public class MeterIdentifierOverflowTest {
    final String meterName = "name";
    final MockLogger logger = (MockLogger) LoggerFactory.getLogger(meterName);

    public MeterIdentifierOverflowTest() {
    }

    @BeforeAll
    static void validate() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    @BeforeAll
    public static void configureMeterSettings() {
        System.setProperty("slf4jtoys.meter.progress.period", "0ms");
    }

    @BeforeEach
    public void clearEvents() {
        logger.clearEvents();
    }

    @Test
    public void testResetImpl() {
        Meter.EVENT_COUNTER.put(meterName, new AtomicLong(Long.MAX_VALUE - 2));

        assertEvents(MeterFactory.getMeter(meterName).start().ok(), 4, Long.MAX_VALUE - 1);
        assertEvents(MeterFactory.getMeter(meterName).start().ok(), 8, Long.MAX_VALUE);
        assertEvents(MeterFactory.getMeter(meterName).start().ok(), 12, 1);
        assertEvents(MeterFactory.getMeter(meterName).start().ok(), 16, 2);
        assertEvents(MeterFactory.getMeter(meterName).start().ok(), 20, 3);
    }

    private void assertEvents(final Meter m, final int expectedMessageCount, final long expectedEventPosition) {
//        Assertions.assertEquals(expectedEventPosition, m.getPosition());
//        Assertions.assertEquals(null, m.getOperation());
//        Assertions.assertEquals(meterName, m.getCategory());
//        final MockLoggerEvent startEvent = logger.getEvent(expectedMessageCount - 4);
//        final MockLoggerEvent startDataEvent = logger.getEvent(expectedMessageCount - 3);
//        final MockLoggerEvent stopEvent = logger.getEvent(expectedMessageCount - 2);
//        final MockLoggerEvent stopDataEvent = logger.getEvent(expectedMessageCount - 1);
//
//        String str = "$=" + Long.toString(expectedEventPosition);
//        Assertions.assertEquals(expectedEventPosition, m.getPosition());
//        Assertions.assertFalse(startEvent.getFormattedMessage().contains(str));
//        Assertions.assertTrue(startDataEvent.getFormattedMessage().contains(str));
//        Assertions.assertFalse(stopEvent.getFormattedMessage().contains(str));
//        Assertions.assertTrue(stopDataEvent.getFormattedMessage().contains(str));
    }
}
