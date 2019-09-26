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
@SuppressWarnings("UnusedAssignment")
public class MeterIterationAttributesTest {

    TestLogger logger = (TestLogger) LoggerFactory.getLogger("Test");

    public MeterIterationAttributesTest() {
        logger.setEnabled(false);
    }

    @Before
    public void clearEvents() {
        logger.clearEvents();
    }

    @Test
    public void testIterationAttributes() {
        final int iterationCount = 10;
        
        
        final Meter m1 = new Meter(logger);
        
        Assert.assertEquals(0, m1.getExpectedIterations());
        Assert.assertEquals(0, m1.getCurrentIteration());
        Assert.assertEquals(0.0d, m1.getIterationsPerSecond(), Double.MIN_VALUE);        

        m1.iterations(iterationCount);
        
        Assert.assertEquals(iterationCount, m1.getExpectedIterations());
        Assert.assertEquals(0, m1.getCurrentIteration());
        Assert.assertEquals(0.0d, m1.getIterationsPerSecond(), Double.MIN_VALUE);        

        final long now1a = System.nanoTime();
        m1.start();
        final long now1b = System.nanoTime();
        
        Assert.assertEquals(iterationCount, m1.getExpectedIterations());
        Assert.assertEquals(0, m1.getCurrentIteration());
        Assert.assertEquals(0.0d, m1.getIterationsPerSecond(), Double.MIN_VALUE);        
        
        m1.inc();
        
        Assert.assertEquals(iterationCount, m1.getExpectedIterations());
        Assert.assertEquals(1, m1.getCurrentIteration());
        Assert.assertTrue(m1.getIterationsPerSecond() > 0.0);        

        m1.incBy(2);
        Assert.assertEquals(iterationCount, m1.getExpectedIterations());
        Assert.assertTrue(m1.getIterationsPerSecond() > 0.0);        
        Assert.assertEquals(3, m1.getCurrentIteration());
        
        m1.incTo(4);
        Assert.assertEquals(iterationCount, m1.getExpectedIterations());
        Assert.assertEquals(4, m1.getCurrentIteration());
        Assert.assertTrue(m1.getIterationsPerSecond() > 0.0);        
        
        final long now2a = System.nanoTime();
        m1.ok();
        final long now2b = System.nanoTime();
        
        Assert.assertEquals(iterationCount, m1.getExpectedIterations());
        Assert.assertEquals(4, m1.getCurrentIteration());
        Assert.assertTrue(m1.getIterationsPerSecond() > 0.0);        

//        double minIterationsPerSecond = 1000000*(now2b-now1a)
    }
 }
