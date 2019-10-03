/*
 * Copyright 2019 Daniel Felix Ferber
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

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.impl.TestLogger;
import org.slf4j.impl.TestLoggerEvent;
import org.usefultoys.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Daniel
 */
public class MeterIdentifierOverflowTest {
    final String meterName = "name";
    final TestLogger logger = (TestLogger) LoggerFactory.getLogger(meterName);

    public MeterIdentifierOverflowTest() {
    }

    @BeforeClass
    public static void configureMeterSettings() {
        System.setProperty("slf4jtoys.meter.progress.period", "0ms");
    }

    @Before
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

    private void assertEvents(Meter m, int expectedMessageCount, long expectedEventPosition) {
        Assert.assertEquals(expectedEventPosition, m.getPosition());
        Assert.assertEquals(null, m.getEventName());
        Assert.assertEquals(meterName, m.getEventCategory());
        final TestLoggerEvent startEvent = logger.getEvent(expectedMessageCount - 4);
        final TestLoggerEvent startDataEvent = logger.getEvent(expectedMessageCount - 3);
        final TestLoggerEvent stopEvent = logger.getEvent(expectedMessageCount - 2);
        final TestLoggerEvent stopDataEvent = logger.getEvent(expectedMessageCount - 1);

        String str = "#=" + Long.toString(expectedEventPosition);
        Assert.assertEquals(expectedEventPosition, m.getPosition());
        Assert.assertFalse(startEvent.getFormattedMessage().contains(str));
        Assert.assertTrue(startDataEvent.getFormattedMessage().contains(str));
        Assert.assertFalse(stopEvent.getFormattedMessage().contains(str));
        Assert.assertTrue(stopDataEvent.getFormattedMessage().contains(str));
    }
}
