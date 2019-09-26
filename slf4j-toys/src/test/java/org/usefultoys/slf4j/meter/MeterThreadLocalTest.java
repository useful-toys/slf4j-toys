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
import org.junit.Test;
import org.slf4j.impl.TestLogger;
import org.usefultoys.slf4j.LoggerFactory;

/**
 * Validates use cases for meter and threadlocal.
 * @author Daniel Felix Ferber
 */
public class MeterThreadLocalTest {

    final String meterName = "name";
    final TestLogger loggerName = (TestLogger) LoggerFactory.getLogger(meterName);
    final String meterOther = "other";
    final TestLogger loggerOther = (TestLogger) LoggerFactory.getLogger(meterOther);

    @Before
    public void clearEvents() {
        loggerOther.clearEvents();
    }

    @Test
    public void testCurrentMeter1() {
        Assert.assertEquals("???", Meter.getCurrentInstance().getEventCategory());

        final Meter m1 = MeterFactory.getMeter(loggerName);
        Assert.assertEquals("???", Meter.getCurrentInstance().getEventCategory());
        m1.start();
        Assert.assertEquals(meterName, Meter.getCurrentInstance().getEventCategory());

        final Meter m2 = MeterFactory.getMeter(loggerOther);
        Assert.assertEquals(meterName, Meter.getCurrentInstance().getEventCategory());
        m2.start();
        Assert.assertEquals(meterOther, Meter.getCurrentInstance().getEventCategory());

        m2.ok();
        Assert.assertEquals(meterName, Meter.getCurrentInstance().getEventCategory());

        m1.ok();
        Assert.assertEquals("???", Meter.getCurrentInstance().getEventCategory());
    }

    @Test
    public void testCurrentMeter2() {
        Assert.assertEquals("???", Meter.getCurrentInstance().getEventCategory());

        final Meter m1 = MeterFactory.getMeter(loggerName);
        Assert.assertEquals("???", Meter.getCurrentInstance().getEventCategory());
        m1.start();
        Assert.assertEquals(meterName, Meter.getCurrentInstance().getEventCategory());

        final Meter m2 = MeterFactory.getMeter(loggerOther);
        Assert.assertEquals(meterName, Meter.getCurrentInstance().getEventCategory());
        m2.start();
        Assert.assertEquals(meterOther, Meter.getCurrentInstance().getEventCategory());

        m2.fail(new IllegalStateException());
        Assert.assertEquals(meterName, Meter.getCurrentInstance().getEventCategory());

        m1.fail(new IllegalStateException());
        Assert.assertEquals("???", Meter.getCurrentInstance().getEventCategory());
    }

    @Test
    public void testCurrentMeter3() throws InterruptedException {
        Assert.assertEquals("???", Meter.getCurrentInstance().getEventCategory());

        final Meter m1 = MeterFactory.getMeter(loggerName);
        Assert.assertEquals("???", Meter.getCurrentInstance().getEventCategory());
        m1.start();
        Assert.assertEquals(meterName, Meter.getCurrentInstance().getEventCategory());

        final Thread t = new Thread() {
            @Override
            public void run() {
                final Meter m2 = MeterFactory.getMeter(loggerOther);
                Assert.assertEquals("???", Meter.getCurrentInstance().getEventCategory());
                m2.start();
                Assert.assertEquals(meterOther, Meter.getCurrentInstance().getEventCategory());
                m2.ok();
                Assert.assertEquals("???", Meter.getCurrentInstance().getEventCategory());
            }
        };
        t.start();
        t.join();
        Assert.assertEquals(meterName, Meter.getCurrentInstance().getEventCategory());

        m1.fail(new IllegalStateException());
        Assert.assertEquals("???", Meter.getCurrentInstance().getEventCategory());
    }
}
