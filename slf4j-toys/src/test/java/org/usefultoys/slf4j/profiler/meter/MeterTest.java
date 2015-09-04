/*
 * Copyright 2015 Daniel Felix Ferber.
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
package org.usefultoys.slf4j.profiler.meter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.impl.TestLogger;
import org.usefultoys.slf4j.logger.LoggerFactory;
import org.usefultoys.slf4j.profiler.ProfilingSession;
import org.usefultoys.slf4j.profiler.meter.Meter;

/**
 *
 * @author Daniel Felix Ferber
 */
public class MeterTest {

    TestLogger logger = (TestLogger) LoggerFactory.getLogger("Test");

    public MeterTest() {
        logger.setEnabled(false);
    }

    @Before
    public void clearEvents() {
        logger.clearEvents();
    }

    @Test
    public void testTimeAttributesOk() {
        final long now1a = System.nanoTime();
        final Meter m = new Meter(logger);
        final long createTime1 = m.getCreateTime();
        final long startTime1 = m.getStartTime();
        final long stopTime1 = m.getStopTime();
        final long waitingTime1 = m.getWaitingTime();
        final long executionTime1 = m.getExecutionTime();
        final long now1b = System.nanoTime();

        Assert.assertTrue(createTime1 >= now1a);
        Assert.assertTrue(createTime1 <= now1b);
        Assert.assertEquals(0, startTime1);
        Assert.assertEquals(0, stopTime1);
        Assert.assertTrue(waitingTime1 > 0);
        Assert.assertTrue(waitingTime1 <= now1b - now1a);
        Assert.assertEquals(0, executionTime1);
        Assert.assertFalse(m.isSuccess());

        final long now2a = System.nanoTime();
        m.start();
        final long createTime2 = m.getCreateTime();
        final long startTime2 = m.getStartTime();
        final long stopTime2 = m.getStopTime();
        final long waitingTime2 = m.getWaitingTime();
        final long executionTime2 = m.getExecutionTime();
        final long now2b = System.nanoTime();

        Assert.assertEquals(createTime1, createTime2);
        Assert.assertTrue(startTime2 >= now2a);
        Assert.assertTrue(startTime2 <= now2b);
        Assert.assertEquals(0, stopTime2);
        Assert.assertEquals(startTime2 - createTime2, waitingTime2);
        Assert.assertTrue(executionTime2 > 0);
        Assert.assertTrue(executionTime2 <= now2b - now2a);
        Assert.assertFalse(m.isSuccess());

        final long now3a = System.nanoTime();
        m.ok();
        final long createTime3 = m.getCreateTime();
        final long startTime3 = m.getStartTime();
        final long stopTime3 = m.getStopTime();
        final long waitingTime3 = m.getWaitingTime();
        final long executionTime3 = m.getExecutionTime();
        final long now3b = System.nanoTime();

        Assert.assertEquals(createTime1, createTime3);
        Assert.assertEquals(startTime2, startTime3);
        Assert.assertTrue(stopTime3 >= now3a);
        Assert.assertTrue(stopTime3 <= now3b);
        Assert.assertEquals(waitingTime3, waitingTime2);
        Assert.assertEquals(stopTime3 - startTime2, executionTime3);
        Assert.assertTrue(m.isSuccess());
    }

    @Test
    public void testTimeAttributesFail() {
        final long now1a = System.nanoTime();
        final Meter m = new Meter(logger);
        final long createTime1 = m.getCreateTime();
        final long startTime1 = m.getStartTime();
        final long stopTime1 = m.getStopTime();
        final long waitingTime1 = m.getWaitingTime();
        final long executionTime1 = m.getExecutionTime();
        final long now1b = System.nanoTime();

        Assert.assertTrue(createTime1 >= now1a);
        Assert.assertTrue(createTime1 <= now1b);
        Assert.assertEquals(0, startTime1);
        Assert.assertEquals(0, stopTime1);
        Assert.assertTrue(waitingTime1 > 0);
        Assert.assertTrue(waitingTime1 <= now1b - now1a);
        Assert.assertEquals(0, executionTime1);
        Assert.assertFalse(m.isSuccess());

        final long now2a = System.nanoTime();
        m.start();
        final long createTime2 = m.getCreateTime();
        final long startTime2 = m.getStartTime();
        final long stopTime2 = m.getStopTime();
        final long waitingTime2 = m.getWaitingTime();
        final long executionTime2 = m.getExecutionTime();
        final long now2b = System.nanoTime();

        Assert.assertEquals(createTime1, createTime2);
        Assert.assertTrue(startTime2 >= now2a);
        Assert.assertTrue(startTime2 <= now2b);
        Assert.assertEquals(0, stopTime2);
        Assert.assertEquals(startTime2 - createTime2, waitingTime2);
        Assert.assertTrue(executionTime2 > 0);
        Assert.assertTrue(executionTime2 <= now2b - now2a);
        Assert.assertFalse(m.isSuccess());

        final long now3a = System.nanoTime();
        m.fail(new IllegalStateException());
        final long createTime3 = m.getCreateTime();
        final long startTime3 = m.getStartTime();
        final long stopTime3 = m.getStopTime();
        final long waitingTime3 = m.getWaitingTime();
        final long executionTime3 = m.getExecutionTime();
        final long now3b = System.nanoTime();

        Assert.assertEquals(createTime1, createTime3);
        Assert.assertEquals(startTime2, startTime3);
        Assert.assertTrue(stopTime3 >= now3a);
        Assert.assertTrue(stopTime3 <= now3b);
        Assert.assertEquals(waitingTime3, waitingTime2);
        Assert.assertEquals(stopTime3 - startTime2, executionTime3);
        Assert.assertFalse(m.isSuccess());
    }

    @Test
    public void testIdentifierAttributes() {
        final String name1 = "TestAttributes1";
        final TestLogger logger1 = (TestLogger) LoggerFactory.getLogger(name1);
        logger1.setEnabled(false);

        final Meter m = new Meter(logger1);
        Assert.assertEquals(ProfilingSession.uuid, m.getSessionUuid());
        Assert.assertEquals(m.getLogger(), logger1);
        Assert.assertEquals(name1, m.getEventCategory());
        Assert.assertNull(m.getEventName());
        Assert.assertEquals(1L, m.getEventPosition());

        final Meter m2 = new Meter(logger1);
        Assert.assertEquals(ProfilingSession.uuid, m2.getSessionUuid());
        Assert.assertEquals(m2.getLogger(), logger1);
        Assert.assertEquals(name1, m2.getEventCategory());
        Assert.assertNull(m2.getEventName());
        Assert.assertEquals(2L, m2.getEventPosition());

        final String name2 = "TestAttributes2";
        final TestLogger logger2 = (TestLogger) LoggerFactory.getLogger(name2);
        logger2.setEnabled(false);

        final Meter m3 = new Meter(logger2);
        Assert.assertEquals(ProfilingSession.uuid, m3.getSessionUuid());
        Assert.assertEquals(m3.getLogger(), logger2);
        Assert.assertEquals(name2, m3.getEventCategory());
        Assert.assertNull(m3.getEventName());
        Assert.assertEquals(1L, m3.getEventPosition());

        final Meter m4 = new Meter(logger2, "op");
        Assert.assertEquals(ProfilingSession.uuid, m4.getSessionUuid());
        Assert.assertEquals(m4.getLogger(), logger2);
        Assert.assertEquals(name2, m4.getEventCategory());
        Assert.assertEquals("op", m4.getEventName());
        Assert.assertEquals(1L, m4.getEventPosition());

        final Meter m5 = new Meter(logger2, "op");
        Assert.assertEquals(ProfilingSession.uuid, m5.getSessionUuid());
        Assert.assertEquals(m5.getLogger(), logger2);
        Assert.assertEquals(name2, m5.getEventCategory());
        Assert.assertEquals("op", m5.getEventName());
        Assert.assertEquals(2L, m5.getEventPosition());

        final Meter m6 = m5.sub("rs");
        Assert.assertEquals(ProfilingSession.uuid, m6.getSessionUuid());
        Assert.assertEquals(m6.getLogger(), logger2);
        Assert.assertEquals(name2, m6.getEventCategory());
        Assert.assertEquals("op/rs", m6.getEventName());
        Assert.assertEquals(1L, m6.getEventPosition());

        final Meter m7 = m5.sub("rs");
        Assert.assertEquals(ProfilingSession.uuid, m7.getSessionUuid());
        Assert.assertEquals(m7.getLogger(), logger2);
        Assert.assertEquals(name2, m7.getEventCategory());
        Assert.assertEquals("op/rs", m7.getEventName());
        Assert.assertEquals(2L, m7.getEventPosition());
    }

    @Test
    public void testThreadAttributes() throws InterruptedException {
        final Meter m1 = new Meter(logger);
        Assert.assertEquals(0, m1.getThreadStartId());
        Assert.assertEquals(null, m1.getThreadStartName());

        m1.start();
        Assert.assertEquals(Thread.currentThread().getId(), m1.getThreadStartId());
        Assert.assertEquals(Thread.currentThread().getName(), m1.getThreadStartName());
        final Thread t = new Thread() {
            @Override
            public void run() {
                m1.ok();
            }
        };
        t.start();
        t.join();
        Assert.assertEquals(t.getId(), m1.getThreadStopId());
        Assert.assertEquals(t.getName(), m1.getThreadStopName());
    }

    @Test
    public void testMessageAttributes() {
        final String description1 = "Test Message";
        final Meter m1 = new Meter(logger).m(description1);
        Assert.assertEquals(description1, m1.getDescription());

        final String description2 = "Test  %d Message";
        final Meter m2 = new Meter(logger).m(description2, 10);
        Assert.assertEquals(String.format(description2, 10), m2.getDescription());
    }

    @Test
    public void testIterationAttributes() {
        final int iterationCount = 10;
        final Meter m1 = new Meter(logger).iterations(iterationCount).start();
        Assert.assertEquals(iterationCount, m1.getExpectedIteration());
        Assert.assertEquals(0, m1.getCurrentIteration());
        m1.inc();
        Assert.assertEquals(iterationCount, m1.getExpectedIteration());
        Assert.assertEquals(1, m1.getCurrentIteration());
        m1.incBy(2);
        Assert.assertEquals(iterationCount, m1.getExpectedIteration());
        Assert.assertEquals(3, m1.getCurrentIteration());
        m1.incTo(4);
        Assert.assertEquals(iterationCount, m1.getExpectedIteration());
        Assert.assertEquals(4, m1.getCurrentIteration());
    }

    @Test
    public void testExceptionAttributes() {
        final Meter m1 = new Meter(logger).start();
        Assert.assertNull(m1.getExceptionClass());
        Assert.assertNull(m1.getExceptionMessage());

        final RuntimeException e = new RuntimeException("Test error message");
        m1.fail(e);
        Assert.assertEquals(e.getClass().getName(), m1.getExceptionClass());
        Assert.assertEquals(e.getMessage(), m1.getExceptionMessage());
    }
}
