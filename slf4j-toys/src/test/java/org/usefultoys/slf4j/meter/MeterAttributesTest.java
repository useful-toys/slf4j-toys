/*
 * Copyright 2017 Daniel Felix Ferber
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
public class MeterAttributesTest {

    TestLogger logger = (TestLogger) LoggerFactory.getLogger("Test");

    public MeterAttributesTest() {
        logger.setEnabled(false);
    }

    @Before
    public void clearEvents() {
        logger.clearEvents();
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
        Assert.assertEquals(iterationCount, m1.getExpectedIterations());
        Assert.assertEquals(0, m1.getCurrentIteration());
        m1.inc();
        Assert.assertEquals(iterationCount, m1.getExpectedIterations());
        Assert.assertEquals(1, m1.getCurrentIteration());
        m1.incBy(2);
        Assert.assertEquals(iterationCount, m1.getExpectedIterations());
        Assert.assertEquals(3, m1.getCurrentIteration());
        m1.incTo(4);
        Assert.assertEquals(iterationCount, m1.getExpectedIterations());
        Assert.assertEquals(4, m1.getCurrentIteration());
    }
 }
