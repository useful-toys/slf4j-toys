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
import org.usefultoys.slf4j.Session;

/**
 *
 * @author Daniel Felix Ferber
 */
public class MeterTimeAttributesTest {

    TestLogger logger = (TestLogger) LoggerFactory.getLogger("Test");

    public MeterTimeAttributesTest() {
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
    }
    
    @Test
    public void testTimeAttributesOkFlow() {
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

        final long now3a = System.nanoTime();
        m.ok("Flow");
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
    }
  
    @Test
    public void testTimeAttributesReject() {
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

        final long now3a = System.nanoTime();
        m.reject("Reject");
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

        final long now3a = System.nanoTime();
        m.fail(new IllegalStateException("ISE"));
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
    }
 }
