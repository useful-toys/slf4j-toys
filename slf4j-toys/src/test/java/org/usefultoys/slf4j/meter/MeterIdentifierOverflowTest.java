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

import java.util.concurrent.atomic.AtomicLong;
import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Daniel
 */
class MeterIdentifierOverflowTest {
    final String meterName = "MeterIdentifierOverflowTest";
    final MockLogger logger = (MockLogger) LoggerFactory.getLogger(meterName);

    @BeforeAll
    static void validateConsistentCharset() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    @BeforeAll
    static void configureMeterSettings() {
        System.setProperty("slf4jtoys.meter.progress.period", "0ms");
    }

    @BeforeEach
    void clearEvents() {
        logger.clearEvents();
    }

    @Test
    void testResetImpl() {
        Meter.EVENT_COUNTER.put(meterName, new AtomicLong(Long.MAX_VALUE - 2));

        assertEquals(Long.MAX_VALUE - 1, MeterFactory.getMeter(meterName).getPosition());
        assertEquals(Long.MAX_VALUE , MeterFactory.getMeter(meterName).getPosition());
        assertEquals(1, MeterFactory.getMeter(meterName).getPosition());
        assertEquals(2, MeterFactory.getMeter(meterName).getPosition());
    }

}
