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

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Validates use cases for meter and threadlocal.
 * @author Daniel Felix Ferber
 */
public class MeterThreadLocalTest {

    @BeforeAll
    public static void validate() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    final String meterName = "name";
    final MockLogger loggerName = (MockLogger) LoggerFactory.getLogger(meterName);
    final String meterOther = "other";
    final MockLogger loggerOther = (MockLogger) LoggerFactory.getLogger(meterOther);

    @BeforeEach
    public void clearEvents() {
        loggerOther.clearEvents();
    }

    @Test
    public void testCurrentMeter1() {
        assertEquals("???", Meter.getCurrentInstance().getCategory());

        final Meter m1 = MeterFactory.getMeter(loggerName);
        assertEquals("???", Meter.getCurrentInstance().getCategory());
        m1.start();
        assertEquals(meterName, Meter.getCurrentInstance().getCategory());

        final Meter m2 = MeterFactory.getMeter(loggerOther);
        assertEquals(meterName, Meter.getCurrentInstance().getCategory());
        m2.start();
        assertEquals(meterOther, Meter.getCurrentInstance().getCategory());

        m2.ok();
        assertEquals(meterName, Meter.getCurrentInstance().getCategory());

        m1.ok();
        assertEquals("???", Meter.getCurrentInstance().getCategory());
    }

    @Test
    public void testCurrentMeter2() {
        assertEquals("???", Meter.getCurrentInstance().getCategory());

        final Meter m1 = MeterFactory.getMeter(loggerName);
        assertEquals("???", Meter.getCurrentInstance().getCategory());
        m1.start();
        assertEquals(meterName, Meter.getCurrentInstance().getCategory());

        final Meter m2 = MeterFactory.getMeter(loggerOther);
        assertEquals(meterName, Meter.getCurrentInstance().getCategory());
        m2.start();
        assertEquals(meterOther, Meter.getCurrentInstance().getCategory());

        m2.fail(new IllegalStateException());
        assertEquals(meterName, Meter.getCurrentInstance().getCategory());

        m1.fail(new IllegalStateException());
        assertEquals("???", Meter.getCurrentInstance().getCategory());
    }

    @Test
    public void testCurrentMeter3() throws InterruptedException {
        assertEquals("???", Meter.getCurrentInstance().getCategory());

        final Meter m1 = MeterFactory.getMeter(loggerName);
        assertEquals("???", Meter.getCurrentInstance().getCategory());
        m1.start();
        assertEquals(meterName, Meter.getCurrentInstance().getCategory());

        final Thread t = new Thread() {
            @Override
            public void run() {
                final Meter m2 = MeterFactory.getMeter(loggerOther);
                assertEquals("???", Meter.getCurrentInstance().getCategory());
                m2.start();
                assertEquals(meterOther, Meter.getCurrentInstance().getCategory());
                m2.ok();
                assertEquals("???", Meter.getCurrentInstance().getCategory());
            }
        };
        t.start();
        t.join();
        assertEquals(meterName, Meter.getCurrentInstance().getCategory());

        m1.fail(new IllegalStateException());
        assertEquals("???", Meter.getCurrentInstance().getCategory());
    }
}
