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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.usefultoys.slf4j.SessionConfig;
import org.usefultoys.slf4j.SystemConfig;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MeterDataReadableTest {
    private static Locale originalLocale;

    @BeforeAll
    public static void setUpLocale() {
        originalLocale = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
    }

    @AfterAll
    public static void tearDownLocale() {
        Locale.setDefault(originalLocale);
    }

    @BeforeEach
    void beforeEach() {
        MeterConfig.reset();
        SessionConfig.reset();
        SystemConfig.reset();
        MeterConfig.printCategory = true;
        MeterConfig.printPosition = true;
    }

    @AfterEach
    void afterEach() {
        MeterConfig.reset();
        SessionConfig.reset();
        SystemConfig.reset();
    }

    private static class MockMeterData extends MeterData {
        public MockMeterData(final String sessionUuid, final long position,
                             final String category, final String operation, final String parent, final String description,
                             final long createTime, final long startTime, final long stopTime, final long currentTime,
                             final long timeLimit, final long currentIteration, final long expectedIterations,
                             final String okPath, final String rejectPath, final String failPath, final String failMessage, final Map<String, String> context) {
            super(sessionUuid, position, currentTime,
                    0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0,
                    category, operation, parent, description, createTime, startTime, stopTime, timeLimit, currentIteration, expectedIterations, okPath, rejectPath, failPath, failMessage, context);
        }
    }

    static Stream<Arguments> provideReadableTestCases() {
        final Map<String, String> context = new HashMap<>();
        context.put("a", "b");
        context.put("c", "d");

        return Stream.of(
                Arguments.of("SCHEDULED: category/operation#1 1000.0ms; uuid",
                        new MockMeterData("uuid", 1, "an.old.category", "operation", null, null, 1000000000L, 0, 0, 2000000000L, 0, 0, 0, null, null, null, null, Collections.emptyMap())),
                Arguments.of("STARTED: category/operation#1 1000.0ms; uuid",
                        new MockMeterData("uuid", 1, "an.old.category", "operation", null, null, 1000000000L, 2000000000L, 0, 3000000000L, 0, 0, 0, null, null, null, null, Collections.emptyMap())),
                Arguments.of("PROGRESS: category/operation#1 1; 1000.0ms; 1.0/s 1000.0ms; uuid",
                        new MockMeterData("uuid", 1, "an.old.category", "operation", null, null, 1000000000L, 2000000000L, 0, 3000000000L, 0, 1, 0, null, null, null, null, Collections.emptyMap())),
                Arguments.of("PROGRESS: category/operation#1 1/2; 1000.0ms; 1.0/s 1000.0ms; uuid",
                        new MockMeterData("uuid", 1, "an.old.category", "operation", null, null, 1000000000L, 2000000000L, 0, 3000000000L, 0, 1, 2, null, null, null, null, Collections.emptyMap())),
                Arguments.of("PROGRESS (Slow): category/operation#1 1/2; 2.0s; 0.5/s 2.0s; uuid",
                        new MockMeterData("uuid", 1, "an.old.category", "operation", null, null, 1000000000L, 2000000000L, 0, 4000000000L, 1000000000L, 1, 2, null, null, null, null, Collections.emptyMap())),
                Arguments.of("OK: category/operation#1 1/2; 1000.0ms; 1.0/s 1000.0ms; uuid",
                        new MockMeterData("uuid", 1, "an.old.category", "operation", null, null, 1000000000L, 2000000000L, 3000000000L, 4000000000L, 0, 1, 2, null, null, null, null, Collections.emptyMap())),
                Arguments.of("OK (Slow): category/operation#1 1/2; 2.0s; 0.5/s 2.0s; uuid",
                        new MockMeterData("uuid", 1, "an.old.category", "operation", null, null, 1000000000L, 2000000000L, 4000000000L, 4000000000L, 1000000000L, 1, 2, null, null, null, null, Collections.emptyMap())),
                Arguments.of("OK: category/operation#1[path] 1/2; 1000.0ms; 1.0/s 1000.0ms; uuid",
                        new MockMeterData("uuid", 1, "an.old.category", "operation", null, null, 1000000000L, 2000000000L, 3000000000L, 4000000000L, 0, 1, 2, "path", null, null, null, Collections.emptyMap())),
                Arguments.of("REJECT: category/operation#1[path] 1/2; 1000.0ms; 1.0/s 1000.0ms; uuid",
                        new MockMeterData("uuid", 1, "an.old.category", "operation", null, null, 1000000000L, 2000000000L, 3000000000L, 4000000000L, 0, 1, 2, null, "path", null, null, Collections.emptyMap())),
                Arguments.of("FAIL: category/operation#1[path] 1/2; 1000.0ms; 1.0/s 1000.0ms; uuid",
                        new MockMeterData("uuid", 1, "an.old.category", "operation", null, null, 1000000000L, 2000000000L, 3000000000L, 4000000000L, 0, 1, 2, null, null, "path", null, Collections.emptyMap())),
                Arguments.of("FAIL: category/operation#1[path; message] 1/2; 1000.0ms; 1.0/s 1000.0ms; uuid",
                        new MockMeterData("uuid", 1, "an.old.category", "operation", null, null, 1000000000L, 2000000000L, 3000000000L, 4000000000L, 0, 1, 2, null, null, "path", "message", Collections.emptyMap())),
                Arguments.of("OK: category/operation#1 1/2; 1000.0ms; 1.0/s 1000.0ms; 'description'; uuid",
                        new MockMeterData("uuid", 1, "an.old.category", "operation", null, "description", 1000000000L, 2000000000L, 3000000000L, 4000000000L, 0, 1, 2, null, null, null, null, Collections.emptyMap())),
                Arguments.of("OK: category/operation#1 1/2; 1000.0ms; 1.0/s 1000.0ms; a=b; c=d; uuid",
                        new MockMeterData("uuid", 1, "an.old.category", "operation", null, null, 1000000000L, 2000000000L, 3000000000L, 4000000000L, 0, 1, 2, null, null, null, null, context)),
                Arguments.of("OK: category/operation#1 1/2; 1000.0ms; 1.0/s 1000.0ms; uuid",
                        new MockMeterData("uuid", 1, "an.old.category", "operation", null, null, 1000000000L, 2000000000L, 3000000000L, 4000000000L, 0, 1, 2, null, null, null, null, Collections.emptyMap()))
        );
    }

    @ParameterizedTest
    @MethodSource("provideReadableTestCases")
    void testReadable(final String expected, final MeterData data) {
        assertEquals(expected, data.readableMessage());
    }
}
