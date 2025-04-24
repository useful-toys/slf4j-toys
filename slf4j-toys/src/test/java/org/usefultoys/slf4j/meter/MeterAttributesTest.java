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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.impl.MockLogger;
import org.usefultoys.slf4j.LoggerFactory;
import org.usefultoys.slf4j.SessionConfig;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Daniel Felix Ferber
 */
public class MeterAttributesTest {

    MockLogger logger = (MockLogger) LoggerFactory.getLogger("Test");

    public MeterAttributesTest() {
        logger.setEnabled(false);
    }

    @BeforeAll
    static void validate() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    @BeforeEach
    public void clearEvents() {
        logger.clearEvents();
    }

    @Test
    public void testMessageAttributes() {
        final String description1 = "Test Message";
        final Meter m1 = new Meter(logger).m(description1);
        assertEquals(description1, m1.getDescription());

        final String description2 = "Test  %d Message";
        final Meter m2 = new Meter(logger).m(description2, 10);
        assertEquals(String.format(description2, 10), m2.getDescription());
    }

    @Test
    public void testIterationAttributes() {
        final int iterationCount = 10;
        final Meter m1 = new Meter(logger).iterations(iterationCount).start();
        assertEquals(iterationCount, m1.getExpectedIterations());
        assertEquals(0, m1.getCurrentIteration());
        m1.inc();
        assertEquals(iterationCount, m1.getExpectedIterations());
        assertEquals(1, m1.getCurrentIteration());
        m1.incBy(2);
        assertEquals(iterationCount, m1.getExpectedIterations());
        assertEquals(3, m1.getCurrentIteration());
        m1.incTo(4);
        assertEquals(iterationCount, m1.getExpectedIterations());
        assertEquals(4, m1.getCurrentIteration());
        m1.ok();
    }
}
