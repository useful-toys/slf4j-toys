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

package org.usefultoys.slf4j.meter.legacy;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.usefultoys.slf4j.SessionConfig;
import org.usefultoys.slf4j.SystemConfig;
import org.usefultoys.slf4j.meter.MeterConfig;
import org.usefultoys.slf4j.meter.MeterData;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MeterDataReadableMessageTest {

    

    
    @BeforeAll
    public static void validateConsistentCharset() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    @BeforeAll
    public static void setupConsistentLocale() {
        // Set the default locale to English for consistent formatting
        Locale.setDefault(Locale.ENGLISH);
    }

    @BeforeEach
    void resetMeterConfigBeforeEach() {
        // Reinitialize MeterConfig to ensure clean configuration before each test
        MeterConfig.reset();
        SessionConfig.reset();
        SystemConfig.reset();
    }

    @AfterAll
    static void resetMeterConfigAfterAll() {
        // Reinitialize MeterConfig to ensure clean configuration for further tests
        MeterConfig.reset();
        SessionConfig.reset();
        SystemConfig.reset();
    }

    private static class MockMeterData extends MeterData {

        public MockMeterData(final String sessionUuid, final long position,
                             final String category, final String operation, final String parent, final String description,
                             final long createTime, final long startTime, final long stopTime, final int currentTime,
                             final long timeLimit, final long currentIteration, final long expectedIterations,
                             final String okPath, final String rejectPath, final String failPath, final String failMessage, final Map<String, String> context) {
            super(sessionUuid, position, currentTime,
                    0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0,
                    category, operation, parent, description, createTime, startTime, stopTime, timeLimit, currentIteration, expectedIterations, okPath, rejectPath, failPath, failMessage, context);
        }
    }

    private static final Map<String, String> nullContext = null;
    private static final Map<String, String> voidContext = Collections.emptyMap();
    private static final Map<String, String> sampleContext = new HashMap<>();
    static {
        sampleContext.put("a", null);
        sampleContext.put("b", "c");
    }

    private static Arguments example(final String expected, final String operation,
                                     final long createTime, final long startTime, final long stopTime, final int currentTime, final long timeLimit,
                                     final long currentIteration, final long expectedIterations) {
        return Arguments.of(new MockMeterData("uuid", 1, "cat", operation, null, null,
                createTime, startTime, stopTime, currentTime, timeLimit, currentIteration, expectedIterations,
                null, null, null, null, nullContext), expected);
    }

    private static Arguments example(final String expected, final String operation,
                                     final long createTime, final long startTime, final long stopTime, final int currentTime, final long timeLimit,
                                     final long currentIteration, final long expectedIterations, final String okPath, final String rejectPath, final String failPath, final String failMessage) {
        return Arguments.of(new MockMeterData("uuid", 1, "cat", operation, null, null,
                createTime, startTime,stopTime,currentTime,timeLimit,currentIteration,expectedIterations,
                okPath,rejectPath,failPath,failMessage, voidContext), expected);
    }
    private static Arguments exampleWithContext(final String expected, final String operation,
                                                final long createTime, final long startTime, final long stopTime, final int currentTime, final long timeLimit,
                                                final long currentIteration, final long expectedIterations) {
        return Arguments.of(new MockMeterData("uuid", 1, "cat", operation, null, null,
                createTime, startTime, stopTime, currentTime, timeLimit, currentIteration, expectedIterations,
                null, null, null, null, sampleContext), expected);
    }

    private static Arguments exampleWithContext(final String expected, final String operation,
                                                final long createTime, final long startTime, final long stopTime, final int currentTime, final long timeLimit,
                                                final long currentIteration, final long expectedIterations, final String okPath, final String rejectPath, final String failPath, final String failMessage) {
        return Arguments.of(new MockMeterData("uuid", 1, "cat", operation, null, null,
                createTime, startTime,stopTime,currentTime,timeLimit,currentIteration,expectedIterations,
                okPath,rejectPath,failPath,failMessage, sampleContext), expected);
    }

    private static Arguments exampleWithDescription(final String expected, final String operation,
                                                    final long createTime, final long startTime, final long stopTime, final int currentTime, final long timeLimit,
                                                    final long currentIteration, final long expectedIterations) {
        return Arguments.of(new MockMeterData("uuid", 1, "cat", operation, null, "desc",
                createTime, startTime, stopTime, currentTime, timeLimit, currentIteration, expectedIterations,
                null, null, null, null, voidContext), expected);
    }

    private static Arguments exampleWithDescription(final String expected, final String operation,
                                                    final long createTime, final long startTime, final long stopTime, final int currentTime, final long timeLimit,
                                                    final long currentIteration, final long expectedIterations, final String okPath, final String rejectPath, final String failPath, final String failMessage) {
        return Arguments.of(new MockMeterData("uuid", 1, "cat", operation, null, "desc",
                createTime, startTime,stopTime,currentTime,timeLimit,currentIteration,expectedIterations,
                okPath,rejectPath,failPath,failMessage, nullContext), expected);
    }

    static Stream<Arguments> provideTimeStatusTestCasesNoCategoryNoPosition() {
        return Stream.of(
                example("SCHEDULED: 190ns; uuid", null,10, 0,0, 200, 0,0,0),
                example("SCHEDULED: op 190ns; uuid", "op",10, 0,0, 200, 0,0,0),

                example("STARTED: uuid", null,10, 20,0, 200, 0,0,0),
                example("STARTED: op uuid", "op",10, 20,0, 200, 0,0,0),

                example("STARTED: 3.0us; uuid", null,10, 20,0, 3000, 0,0,0),
                example("STARTED: op 3.0us; uuid", "op",10, 20,0, 3000, 0,0,0),

                example("PROGRESS: 1/10; uuid", null,10, 20,0, 200, 0, 1,10),
                example("PROGRESS: op 1/10; uuid", "op",10, 20,0, 200, 0,1,10),
                example("PROGRESS: 1; uuid", null,10, 20,0, 200, 0, 1,0),
                example("PROGRESS: op 1; uuid", "op",10, 20,0, 200, 0,1,0),
                example("PROGRESS: 1/10; 3.0us; 335.6k/s 3.0us; uuid", null,10, 20,0, 3000, 0, 1,10),
                example("PROGRESS: op 1/10; 3.0us; 335.6k/s 3.0us; uuid", "op",10, 20,0, 3000, 0,1,10),
                example("PROGRESS: 2/10; 3.0us; 671.1k/s 1.5us; uuid", null,10, 20,0, 3000, 0, 2,10),
                example("PROGRESS: op 2/10; 3.0us; 671.1k/s 1.5us; uuid", "op",10, 20,0, 3000, 0,2,10),

                example("PROGRESS (Slow): 1/10; uuid", null,10, 20,0, 600, 500, 1,10),
                example("PROGRESS (Slow): op 1/10; uuid", "op",10, 20,0, 600, 500,1,10),
                example("PROGRESS (Slow): 1; uuid", null,10, 20,0, 600, 500, 1,0),
                example("PROGRESS (Slow): op 1; uuid", "op",10, 20,0, 600, 500,1,0),
                example("PROGRESS (Slow): 1/10; 3.0us; 335.6k/s 3.0us; uuid", null,10, 20,0, 3000, 500, 1,10),
                example("PROGRESS (Slow): op 1/10; 3.0us; 335.6k/s 3.0us; uuid", "op",10, 20,0, 3000, 500,1,10),
                example("PROGRESS (Slow): 2/10; 3.0us; 671.1k/s 1.5us; uuid", null,10, 20,0, 3000, 500, 2,10),
                example("PROGRESS (Slow): op 2/10; 3.0us; 671.1k/s 1.5us; uuid", "op",10, 20,0, 3000, 500,2,10),

                example("OK: 180ns; uuid", null,10, 20,200, 4000, 0, 0,0, null, null, null, null),
                example("OK: op 180ns; uuid", "op",10, 20,200, 4000, 0,0,0, null, null, null, null),

                example("OK (Slow): 3.0us; uuid", null,10, 20,3000, 4000, 200, 0,0, null, null, null, null),
                example("OK (Slow): op 3.0us; uuid", "op",10, 20,3000, 4000, 200,0,0, null, null, null, null),

                example("OK: 1/10; 180ns; 5.6M/s 180.0ns; uuid", null,10, 20,200, 200, 0, 1,10, null, null, null, null),
                example("OK: op 1/10; 180ns; 5.6M/s 180.0ns; uuid", "op",10, 20,200, 200, 0,1,10, null, null, null, null),
                example("OK: 1; 180ns; 5.6M/s 180.0ns; uuid", null,10, 20,200, 200, 0, 1,0, null, null, null, null),
                example("OK: op 1; 180ns; 5.6M/s 180.0ns; uuid", "op",10, 20,200, 200, 0,1,0, null, null, null, null),
                example("OK: 1/10; 3.0us; 335.6k/s 3.0us; uuid", null,10, 20,3000, 3000, 0, 1,10, null, null, null, null),
                example("OK: op 1/10; 3.0us; 335.6k/s 3.0us; uuid", "op",10, 20,3000, 3000, 0,1,10, null, null, null, null),
                example("OK: 2/10; 3.0us; 671.1k/s 1.5us; uuid", null,10, 20,3000, 3000, 0, 2,10, null, null, null, null),
                example("OK: op 2/10; 3.0us; 671.1k/s 1.5us; uuid", "op",10, 20,3000, 3000, 0,2,10, null, null, null, null),

                example("OK: 1/10; 180ns; 5.6M/s 180.0ns; uuid", null,10, 20,200, 200, 4000, 1,10, null, null, null, null),
                example("OK: op 1/10; 180ns; 5.6M/s 180.0ns; uuid", "op",10, 20,200, 200, 4000,1,10, null, null, null, null),
                example("OK: 1; 180ns; 5.6M/s 180.0ns; uuid", null,10, 20,200, 200, 4000, 1,0, null, null, null, null),
                example("OK: op 1; 180ns; 5.6M/s 180.0ns; uuid", "op",10, 20,200, 200, 4000,1,0, null, null, null, null),
                example("OK: 1/10; 3.0us; 335.6k/s 3.0us; uuid", null,10, 20,3000, 3000, 4000, 1,10, null, null, null, null),
                example("OK: op 1/10; 3.0us; 335.6k/s 3.0us; uuid", "op",10, 20,3000, 3000, 4000,1,10, null, null, null, null),
                example("OK: 2/10; 3.0us; 671.1k/s 1.5us; uuid", null,10, 20,3000, 3000, 4000, 2,10, null, null, null, null),
                example("OK: op 2/10; 3.0us; 671.1k/s 1.5us; uuid", "op",10, 20,3000, 3000, 4000,2,10, null, null, null, null),

                example("OK (Slow): 1/10; 580ns; 1.7M/s 580.0ns; uuid", null,10, 20,600, 4000, 500, 1,10, null, null, null, null),
                example("OK (Slow): op 1/10; 580ns; 1.7M/s 580.0ns; uuid", "op",10, 20,600, 4000, 500,1,10, null, null, null, null),
                example("OK (Slow): 1; 580ns; 1.7M/s 580.0ns; uuid", null,10, 20,600, 4000, 500, 1,0, null, null, null, null),
                example("OK (Slow): op 1; 580ns; 1.7M/s 580.0ns; uuid", "op",10, 20,600, 4000, 500,1,0, null, null, null, null),
                example("OK (Slow): 1/10; 3.0us; 335.6k/s 3.0us; uuid", null,10, 20,3000, 4000, 500, 1,10, null, null, null, null),
                example("OK (Slow): op 1/10; 3.0us; 335.6k/s 3.0us; uuid", "op",10, 20,3000, 4000, 500,1,10, null, null, null, null),
                example("OK (Slow): 2/10; 3.0us; 671.1k/s 1.5us; uuid", null,10, 20,3000, 4000, 500, 2,10, null, null, null, null),
                example("OK (Slow): op 2/10; 3.0us; 671.1k/s 1.5us; uuid", "op",10, 20,3000, 4000, 500,2,10, null, null, null, null),

                example("OK: [abc] 180ns; uuid", null,10, 20,200, 4000, 0, 0,0, "abc", null, null, null),
                example("OK: op[abc] 180ns; uuid", "op",10, 20,200, 4000, 0,0,0, "abc", null, null, null),

                example("OK (Slow): [abc] 3.0us; uuid", null,10, 20,3000, 4000, 200, 0,0, "abc", null, null, null),
                example("OK (Slow): op[abc] 3.0us; uuid", "op",10, 20,3000, 4000, 200,0,0, "abc", null, null, null),

                example("OK: [abc] 1/10; 180ns; 5.6M/s 180.0ns; uuid", null,10, 20,200, 200, 0, 1,10, "abc", null, null, null),
                example("OK: op[abc] 1/10; 180ns; 5.6M/s 180.0ns; uuid", "op",10, 20,200, 200, 0,1,10, "abc", null, null, null),
                example("OK: [abc] 1; 180ns; 5.6M/s 180.0ns; uuid", null,10, 20,200, 200, 0, 1,0, "abc", null, null, null),
                example("OK: op[abc] 1; 180ns; 5.6M/s 180.0ns; uuid", "op",10, 20,200, 200, 0,1,0, "abc", null, null, null),
                example("OK: [abc] 1/10; 3.0us; 335.6k/s 3.0us; uuid", null,10, 20,3000, 3000, 0, 1,10, "abc", null, null, null),
                example("OK: op[abc] 1/10; 3.0us; 335.6k/s 3.0us; uuid", "op",10, 20,3000, 3000, 0,1,10, "abc", null, null, null),
                example("OK: [abc] 2/10; 3.0us; 671.1k/s 1.5us; uuid", null,10, 20,3000, 3000, 0, 2,10, "abc", null, null, null),
                example("OK: op[abc] 2/10; 3.0us; 671.1k/s 1.5us; uuid", "op",10, 20,3000, 3000, 0,2,10, "abc", null, null, null),

                example("OK: [abc] 1/10; 180ns; 5.6M/s 180.0ns; uuid", null,10, 20,200, 200, 4000, 1,10, "abc", null, null, null),
                example("OK: op[abc] 1/10; 180ns; 5.6M/s 180.0ns; uuid", "op",10, 20,200, 200, 4000,1,10, "abc", null, null, null),
                example("OK: [abc] 1; 180ns; 5.6M/s 180.0ns; uuid", null,10, 20,200, 200, 4000, 1,0, "abc", null, null, null),
                example("OK: op[abc] 1; 180ns; 5.6M/s 180.0ns; uuid", "op",10, 20,200, 200, 4000,1,0, "abc", null, null, null),
                example("OK: [abc] 1/10; 3.0us; 335.6k/s 3.0us; uuid", null,10, 20,3000, 3000, 4000, 1,10, "abc", null, null, null),
                example("OK: op[abc] 1/10; 3.0us; 335.6k/s 3.0us; uuid", "op",10, 20,3000, 3000, 4000,1,10, "abc", null, null, null),
                example("OK: [abc] 2/10; 3.0us; 671.1k/s 1.5us; uuid", null,10, 20,3000, 3000, 4000, 2,10, "abc", null, null, null),
                example("OK: op[abc] 2/10; 3.0us; 671.1k/s 1.5us; uuid", "op",10, 20,3000, 3000, 4000,2,10, "abc", null, null, null),

                example("OK (Slow): [abc] 1/10; 580ns; 1.7M/s 580.0ns; uuid", null,10, 20,600, 4000, 500, 1,10, "abc", null, null, null),
                example("OK (Slow): op[abc] 1/10; 580ns; 1.7M/s 580.0ns; uuid", "op",10, 20,600, 4000, 500,1,10, "abc", null, null, null),
                example("OK (Slow): [abc] 1; 580ns; 1.7M/s 580.0ns; uuid", null,10, 20,600, 4000, 500, 1,0, "abc", null, null, null),
                example("OK (Slow): op[abc] 1; 580ns; 1.7M/s 580.0ns; uuid", "op",10, 20,600, 4000, 500,1,0, "abc", null, null, null),
                example("OK (Slow): [abc] 1/10; 3.0us; 335.6k/s 3.0us; uuid", null,10, 20,3000, 4000, 500, 1,10, "abc", null, null, null),
                example("OK (Slow): op[abc] 1/10; 3.0us; 335.6k/s 3.0us; uuid", "op",10, 20,3000, 4000, 500,1,10, "abc", null, null, null),
                example("OK (Slow): [abc] 2/10; 3.0us; 671.1k/s 1.5us; uuid", null,10, 20,3000, 4000, 500, 2,10, "abc", null, null, null),
                example("OK (Slow): op[abc] 2/10; 3.0us; 671.1k/s 1.5us; uuid", "op",10, 20,3000, 4000, 500,2,10, "abc", null, null, null),

                example("REJECT: [abc] 180ns; uuid", null,10, 20,200, 4000, 0, 0,0,  null, "abc", null, null),
                example("REJECT: op[abc] 180ns; uuid", "op",10, 20,200, 4000, 0,0,0,  null, "abc", null, null),

                example("REJECT: [abc] 1/10; 180ns; 5.6M/s 180.0ns; uuid", null,10, 20,200, 200, 0, 1,10,  null, "abc", null, null),
                example("REJECT: op[abc] 1/10; 180ns; 5.6M/s 180.0ns; uuid", "op",10, 20,200, 200, 0,1,10,  null, "abc", null, null),
                example("REJECT: [abc] 1; 180ns; 5.6M/s 180.0ns; uuid", null,10, 20,200, 200, 0, 1,0,  null, "abc", null, null),
                example("REJECT: op[abc] 1; 180ns; 5.6M/s 180.0ns; uuid", "op",10, 20,200, 200, 0,1,0,  null, "abc", null, null),
                example("REJECT: [abc] 1/10; 3.0us; 335.6k/s 3.0us; uuid", null,10, 20,3000, 3000, 0, 1,10,  null, "abc", null, null),
                example("REJECT: op[abc] 1/10; 3.0us; 335.6k/s 3.0us; uuid", "op",10, 20,3000, 3000, 0,1,10,  null, "abc", null, null),
                example("REJECT: [abc] 2/10; 3.0us; 671.1k/s 1.5us; uuid", null,10, 20,3000, 3000, 0, 2,10,  null, "abc", null, null),
                example("REJECT: op[abc] 2/10; 3.0us; 671.1k/s 1.5us; uuid", "op",10, 20,3000, 3000, 0,2,10,  null, "abc", null, null),

                example("FAIL: [abc] 180ns; uuid", null,10, 20,200, 4000, 0, 0,0,  null, null, "abc", null),
                example("FAIL: op[abc] 180ns; uuid", "op",10, 20,200, 4000, 0,0,0,  null, null, "abc", null),

                example("FAIL: [abc] 1/10; 180ns; 5.6M/s 180.0ns; uuid", null,10, 20,200, 200, 0, 1,10,  null, null, "abc", null),
                example("FAIL: op[abc] 1/10; 180ns; 5.6M/s 180.0ns; uuid", "op",10, 20,200, 200, 0,1,10,  null, null, "abc", null),
                example("FAIL: [abc] 1; 180ns; 5.6M/s 180.0ns; uuid", null,10, 20,200, 200, 0, 1,0,  null, null, "abc", null),
                example("FAIL: op[abc] 1; 180ns; 5.6M/s 180.0ns; uuid", "op",10, 20,200, 200, 0,1,0,  null, null, "abc", null),
                example("FAIL: [abc] 1/10; 3.0us; 335.6k/s 3.0us; uuid", null,10, 20,3000, 3000, 0, 1,10,  null, null, "abc", null),
                example("FAIL: op[abc] 1/10; 3.0us; 335.6k/s 3.0us; uuid", "op",10, 20,3000, 3000, 0,1,10,  null, null, "abc", null),
                example("FAIL: [abc] 2/10; 3.0us; 671.1k/s 1.5us; uuid", null,10, 20,3000, 3000, 0, 2,10,  null, null, "abc", null),
                example("FAIL: op[abc] 2/10; 3.0us; 671.1k/s 1.5us; uuid", "op",10, 20,3000, 3000, 0,2,10,  null, null, "abc", null),

                example("FAIL: [abc; def] 180ns; uuid", null,10, 20,200, 4000, 0, 0,0,  null, null, "abc", "def"),
                example("FAIL: op[abc; def] 180ns; uuid", "op",10, 20,200, 4000, 0,0,0,  null, null, "abc", "def"),

                example("FAIL: [abc; def] 1/10; 180ns; 5.6M/s 180.0ns; uuid", null,10, 20,200, 200, 0, 1,10,  null, null, "abc", "def"),
                example("FAIL: op[abc; def] 1/10; 180ns; 5.6M/s 180.0ns; uuid", "op",10, 20,200, 200, 0,1,10,  null, null, "abc", "def"),
                example("FAIL: [abc; def] 1; 180ns; 5.6M/s 180.0ns; uuid", null,10, 20,200, 200, 0, 1,0,  null, null, "abc", "def"),
                example("FAIL: op[abc; def] 1; 180ns; 5.6M/s 180.0ns; uuid", "op",10, 20,200, 200, 0,1,0,  null, null, "abc", "def"),
                example("FAIL: [abc; def] 1/10; 3.0us; 335.6k/s 3.0us; uuid", null,10, 20,3000, 3000, 0, 1,10,  null, null, "abc", "def"),
                example("FAIL: op[abc; def] 1/10; 3.0us; 335.6k/s 3.0us; uuid", "op",10, 20,3000, 3000, 0,1,10,  null, null, "abc", "def"),
                example("FAIL: [abc; def] 2/10; 3.0us; 671.1k/s 1.5us; uuid", null,10, 20,3000, 3000, 0, 2,10,  null, null, "abc", "def"),
                example("FAIL: op[abc; def] 2/10; 3.0us; 671.1k/s 1.5us; uuid", "op",10, 20,3000, 3000, 0,2,10,  null, null, "abc", "def")
        );
    }

    static Stream<Arguments> provideTimeStatusTestCasesNoCategoryNoPositionWithContext() {
        return Stream.of(
                exampleWithContext("SCHEDULED: 190ns; a; b=c; uuid", null,10, 0,0, 200, 0,0,0),
                exampleWithContext("SCHEDULED: op 190ns; a; b=c; uuid", "op",10, 0,0, 200, 0,0,0),

                exampleWithContext("STARTED: a; b=c; uuid", null,10, 20,0, 200, 0,0,0),
                exampleWithContext("STARTED: op a; b=c; uuid", "op",10, 20,0, 200, 0,0,0),

                exampleWithContext("STARTED: 3.0us; a; b=c; uuid", null,10, 20,0, 3000, 0,0,0),
                exampleWithContext("STARTED: op 3.0us; a; b=c; uuid", "op",10, 20,0, 3000, 0,0,0),

                exampleWithContext("PROGRESS: 1/10; a; b=c; uuid", null,10, 20,0, 200, 0, 1,10),
                exampleWithContext("PROGRESS: op 1/10; a; b=c; uuid", "op",10, 20,0, 200, 0,1,10),
                exampleWithContext("PROGRESS: 1; a; b=c; uuid", null,10, 20,0, 200, 0, 1,0),
                exampleWithContext("PROGRESS: op 1; a; b=c; uuid", "op",10, 20,0, 200, 0,1,0),
                exampleWithContext("PROGRESS: 1/10; 3.0us; 335.6k/s 3.0us; a; b=c; uuid", null,10, 20,0, 3000, 0, 1,10),
                exampleWithContext("PROGRESS: op 1/10; 3.0us; 335.6k/s 3.0us; a; b=c; uuid", "op",10, 20,0, 3000, 0,1,10),
                exampleWithContext("PROGRESS: 2/10; 3.0us; 671.1k/s 1.5us; a; b=c; uuid", null,10, 20,0, 3000, 0, 2,10),
                exampleWithContext("PROGRESS: op 2/10; 3.0us; 671.1k/s 1.5us; a; b=c; uuid", "op",10, 20,0, 3000, 0,2,10),

                exampleWithContext("PROGRESS (Slow): 1/10; a; b=c; uuid", null,10, 20,0, 600, 500, 1,10),
                exampleWithContext("PROGRESS (Slow): op 1/10; a; b=c; uuid", "op",10, 20,0, 600, 500,1,10),
                exampleWithContext("PROGRESS (Slow): 1; a; b=c; uuid", null,10, 20,0, 600, 500, 1,0),
                exampleWithContext("PROGRESS (Slow): op 1; a; b=c; uuid", "op",10, 20,0, 600, 500,1,0),
                exampleWithContext("PROGRESS (Slow): 1/10; 3.0us; 335.6k/s 3.0us; a; b=c; uuid", null,10, 20,0, 3000, 500, 1,10),
                exampleWithContext("PROGRESS (Slow): op 1/10; 3.0us; 335.6k/s 3.0us; a; b=c; uuid", "op",10, 20,0, 3000, 500,1,10),
                exampleWithContext("PROGRESS (Slow): 2/10; 3.0us; 671.1k/s 1.5us; a; b=c; uuid", null,10, 20,0, 3000, 500, 2,10),
                exampleWithContext("PROGRESS (Slow): op 2/10; 3.0us; 671.1k/s 1.5us; a; b=c; uuid", "op",10, 20,0, 3000, 500,2,10),

                exampleWithContext("OK: 180ns; a; b=c; uuid", null,10, 20,200, 4000, 0, 0,0, null, null, null, null),
                exampleWithContext("OK: op 180ns; a; b=c; uuid", "op",10, 20,200, 4000, 0,0,0, null, null, null, null),

                exampleWithContext("OK (Slow): 3.0us; a; b=c; uuid", null,10, 20,3000, 4000, 200, 0,0, null, null, null, null),
                exampleWithContext("OK (Slow): op 3.0us; a; b=c; uuid", "op",10, 20,3000, 4000, 200,0,0, null, null, null, null),

                exampleWithContext("OK: 1/10; 180ns; 5.6M/s 180.0ns; a; b=c; uuid", null,10, 20,200, 200, 0, 1,10, null, null, null, null),
                exampleWithContext("OK: op 1/10; 180ns; 5.6M/s 180.0ns; a; b=c; uuid", "op",10, 20,200, 200, 0,1,10, null, null, null, null),
                exampleWithContext("OK: 1; 180ns; 5.6M/s 180.0ns; a; b=c; uuid", null,10, 20,200, 200, 0, 1,0, null, null, null, null),
                exampleWithContext("OK: op 1; 180ns; 5.6M/s 180.0ns; a; b=c; uuid", "op",10, 20,200, 200, 0,1,0, null, null, null, null),
                exampleWithContext("OK: 1/10; 3.0us; 335.6k/s 3.0us; a; b=c; uuid", null,10, 20,3000, 3000, 0, 1,10, null, null, null, null),
                exampleWithContext("OK: op 1/10; 3.0us; 335.6k/s 3.0us; a; b=c; uuid", "op",10, 20,3000, 3000, 0,1,10, null, null, null, null),
                exampleWithContext("OK: 2/10; 3.0us; 671.1k/s 1.5us; a; b=c; uuid", null,10, 20,3000, 3000, 0, 2,10, null, null, null, null),
                exampleWithContext("OK: op 2/10; 3.0us; 671.1k/s 1.5us; a; b=c; uuid", "op",10, 20,3000, 3000, 0,2,10, null, null, null, null),

                exampleWithContext("OK: 1/10; 180ns; 5.6M/s 180.0ns; a; b=c; uuid", null,10, 20,200, 200, 4000, 1,10, null, null, null, null),
                exampleWithContext("OK: op 1/10; 180ns; 5.6M/s 180.0ns; a; b=c; uuid", "op",10, 20,200, 200, 4000,1,10, null, null, null, null),
                exampleWithContext("OK: 1; 180ns; 5.6M/s 180.0ns; a; b=c; uuid", null,10, 20,200, 200, 4000, 1,0, null, null, null, null),
                exampleWithContext("OK: op 1; 180ns; 5.6M/s 180.0ns; a; b=c; uuid", "op",10, 20,200, 200, 4000,1,0, null, null, null, null),
                exampleWithContext("OK: 1/10; 3.0us; 335.6k/s 3.0us; a; b=c; uuid", null,10, 20,3000, 3000, 4000, 1,10, null, null, null, null),
                exampleWithContext("OK: op 1/10; 3.0us; 335.6k/s 3.0us; a; b=c; uuid", "op",10, 20,3000, 3000, 4000,1,10, null, null, null, null),
                exampleWithContext("OK: 2/10; 3.0us; 671.1k/s 1.5us; a; b=c; uuid", null,10, 20,3000, 3000, 4000, 2,10, null, null, null, null),
                exampleWithContext("OK: op 2/10; 3.0us; 671.1k/s 1.5us; a; b=c; uuid", "op",10, 20,3000, 3000, 4000,2,10, null, null, null, null),

                exampleWithContext("OK (Slow): 1/10; 580ns; 1.7M/s 580.0ns; a; b=c; uuid", null,10, 20,600, 4000, 500, 1,10, null, null, null, null),
                exampleWithContext("OK (Slow): op 1/10; 580ns; 1.7M/s 580.0ns; a; b=c; uuid", "op",10, 20,600, 4000, 500,1,10, null, null, null, null),
                exampleWithContext("OK (Slow): 1; 580ns; 1.7M/s 580.0ns; a; b=c; uuid", null,10, 20,600, 4000, 500, 1,0, null, null, null, null),
                exampleWithContext("OK (Slow): op 1; 580ns; 1.7M/s 580.0ns; a; b=c; uuid", "op",10, 20,600, 4000, 500,1,0, null, null, null, null),
                exampleWithContext("OK (Slow): 1/10; 3.0us; 335.6k/s 3.0us; a; b=c; uuid", null,10, 20,3000, 4000, 500, 1,10, null, null, null, null),
                exampleWithContext("OK (Slow): op 1/10; 3.0us; 335.6k/s 3.0us; a; b=c; uuid", "op",10, 20,3000, 4000, 500,1,10, null, null, null, null),
                exampleWithContext("OK (Slow): 2/10; 3.0us; 671.1k/s 1.5us; a; b=c; uuid", null,10, 20,3000, 4000, 500, 2,10, null, null, null, null),
                exampleWithContext("OK (Slow): op 2/10; 3.0us; 671.1k/s 1.5us; a; b=c; uuid", "op",10, 20,3000, 4000, 500,2,10, null, null, null, null),

                exampleWithContext("OK: [abc] 180ns; a; b=c; uuid", null,10, 20,200, 4000, 0, 0,0, "abc", null, null, null),
                exampleWithContext("OK: op[abc] 180ns; a; b=c; uuid", "op",10, 20,200, 4000, 0,0,0, "abc", null, null, null),

                exampleWithContext("OK (Slow): [abc] 3.0us; a; b=c; uuid", null,10, 20,3000, 4000, 200, 0,0, "abc", null, null, null),
                exampleWithContext("OK (Slow): op[abc] 3.0us; a; b=c; uuid", "op",10, 20,3000, 4000, 200,0,0, "abc", null, null, null),

                exampleWithContext("OK: [abc] 1/10; 180ns; 5.6M/s 180.0ns; a; b=c; uuid", null,10, 20,200, 200, 0, 1,10, "abc", null, null, null),
                exampleWithContext("OK: op[abc] 1/10; 180ns; 5.6M/s 180.0ns; a; b=c; uuid", "op",10, 20,200, 200, 0,1,10, "abc", null, null, null),
                exampleWithContext("OK: [abc] 1; 180ns; 5.6M/s 180.0ns; a; b=c; uuid", null,10, 20,200, 200, 0, 1,0, "abc", null, null, null),
                exampleWithContext("OK: op[abc] 1; 180ns; 5.6M/s 180.0ns; a; b=c; uuid", "op",10, 20,200, 200, 0,1,0, "abc", null, null, null),
                exampleWithContext("OK: [abc] 1/10; 3.0us; 335.6k/s 3.0us; a; b=c; uuid", null,10, 20,3000, 3000, 0, 1,10, "abc", null, null, null),
                exampleWithContext("OK: op[abc] 1/10; 3.0us; 335.6k/s 3.0us; a; b=c; uuid", "op",10, 20,3000, 3000, 0,1,10, "abc", null, null, null),
                exampleWithContext("OK: [abc] 2/10; 3.0us; 671.1k/s 1.5us; a; b=c; uuid", null,10, 20,3000, 3000, 0, 2,10, "abc", null, null, null),
                exampleWithContext("OK: op[abc] 2/10; 3.0us; 671.1k/s 1.5us; a; b=c; uuid", "op",10, 20,3000, 3000, 0,2,10, "abc", null, null, null),

                exampleWithContext("OK: [abc] 1/10; 180ns; 5.6M/s 180.0ns; a; b=c; uuid", null,10, 20,200, 200, 4000, 1,10, "abc", null, null, null),
                exampleWithContext("OK: op[abc] 1/10; 180ns; 5.6M/s 180.0ns; a; b=c; uuid", "op",10, 20,200, 200, 4000,1,10, "abc", null, null, null),
                exampleWithContext("OK: [abc] 1; 180ns; 5.6M/s 180.0ns; a; b=c; uuid", null,10, 20,200, 200, 4000, 1,0, "abc", null, null, null),
                exampleWithContext("OK: op[abc] 1; 180ns; 5.6M/s 180.0ns; a; b=c; uuid", "op",10, 20,200, 200, 4000,1,0, "abc", null, null, null),
                exampleWithContext("OK: [abc] 1/10; 3.0us; 335.6k/s 3.0us; a; b=c; uuid", null,10, 20,3000, 3000, 4000, 1,10, "abc", null, null, null),
                exampleWithContext("OK: op[abc] 1/10; 3.0us; 335.6k/s 3.0us; a; b=c; uuid", "op",10, 20,3000, 3000, 4000,1,10, "abc", null, null, null),
                exampleWithContext("OK: [abc] 2/10; 3.0us; 671.1k/s 1.5us; a; b=c; uuid", null,10, 20,3000, 3000, 4000, 2,10, "abc", null, null, null),
                exampleWithContext("OK: op[abc] 2/10; 3.0us; 671.1k/s 1.5us; a; b=c; uuid", "op",10, 20,3000, 3000, 4000,2,10, "abc", null, null, null),

                exampleWithContext("OK (Slow): [abc] 1/10; 580ns; 1.7M/s 580.0ns; a; b=c; uuid", null,10, 20,600, 4000, 500, 1,10, "abc", null, null, null),
                exampleWithContext("OK (Slow): op[abc] 1/10; 580ns; 1.7M/s 580.0ns; a; b=c; uuid", "op",10, 20,600, 4000, 500,1,10, "abc", null, null, null),
                exampleWithContext("OK (Slow): [abc] 1; 580ns; 1.7M/s 580.0ns; a; b=c; uuid", null,10, 20,600, 4000, 500, 1,0, "abc", null, null, null),
                exampleWithContext("OK (Slow): op[abc] 1; 580ns; 1.7M/s 580.0ns; a; b=c; uuid", "op",10, 20,600, 4000, 500,1,0, "abc", null, null, null),
                exampleWithContext("OK (Slow): [abc] 1/10; 3.0us; 335.6k/s 3.0us; a; b=c; uuid", null,10, 20,3000, 4000, 500, 1,10, "abc", null, null, null),
                exampleWithContext("OK (Slow): op[abc] 1/10; 3.0us; 335.6k/s 3.0us; a; b=c; uuid", "op",10, 20,3000, 4000, 500,1,10, "abc", null, null, null),
                exampleWithContext("OK (Slow): [abc] 2/10; 3.0us; 671.1k/s 1.5us; a; b=c; uuid", null,10, 20,3000, 4000, 500, 2,10, "abc", null, null, null),
                exampleWithContext("OK (Slow): op[abc] 2/10; 3.0us; 671.1k/s 1.5us; a; b=c; uuid", "op",10, 20,3000, 4000, 500,2,10, "abc", null, null, null),

                exampleWithContext("REJECT: [abc] 180ns; a; b=c; uuid", null,10, 20,200, 4000, 0, 0,0,  null, "abc", null, null),
                exampleWithContext("REJECT: op[abc] 180ns; a; b=c; uuid", "op",10, 20,200, 4000, 0,0,0,  null, "abc", null, null),

                exampleWithContext("REJECT: [abc] 1/10; 180ns; 5.6M/s 180.0ns; a; b=c; uuid", null,10, 20,200, 200, 0, 1,10,  null, "abc", null, null),
                exampleWithContext("REJECT: op[abc] 1/10; 180ns; 5.6M/s 180.0ns; a; b=c; uuid", "op",10, 20,200, 200, 0,1,10,  null, "abc", null, null),
                exampleWithContext("REJECT: [abc] 1; 180ns; 5.6M/s 180.0ns; a; b=c; uuid", null,10, 20,200, 200, 0, 1,0,  null, "abc", null, null),
                exampleWithContext("REJECT: op[abc] 1; 180ns; 5.6M/s 180.0ns; a; b=c; uuid", "op",10, 20,200, 200, 0,1,0,  null, "abc", null, null),
                exampleWithContext("REJECT: [abc] 1/10; 3.0us; 335.6k/s 3.0us; a; b=c; uuid", null,10, 20,3000, 3000, 0, 1,10,  null, "abc", null, null),
                exampleWithContext("REJECT: op[abc] 1/10; 3.0us; 335.6k/s 3.0us; a; b=c; uuid", "op",10, 20,3000, 3000, 0,1,10,  null, "abc", null, null),
                exampleWithContext("REJECT: [abc] 2/10; 3.0us; 671.1k/s 1.5us; a; b=c; uuid", null,10, 20,3000, 3000, 0, 2,10,  null, "abc", null, null),
                exampleWithContext("REJECT: op[abc] 2/10; 3.0us; 671.1k/s 1.5us; a; b=c; uuid", "op",10, 20,3000, 3000, 0,2,10,  null, "abc", null, null),

                exampleWithContext("FAIL: [abc] 180ns; a; b=c; uuid", null,10, 20,200, 4000, 0, 0,0,  null, null, "abc", null),
                exampleWithContext("FAIL: op[abc] 180ns; a; b=c; uuid", "op",10, 20,200, 4000, 0,0,0,  null, null, "abc", null),

                exampleWithContext("FAIL: [abc] 1/10; 180ns; 5.6M/s 180.0ns; a; b=c; uuid", null,10, 20,200, 200, 0, 1,10,  null, null, "abc", null),
                exampleWithContext("FAIL: op[abc] 1/10; 180ns; 5.6M/s 180.0ns; a; b=c; uuid", "op",10, 20,200, 200, 0,1,10,  null, null, "abc", null),
                exampleWithContext("FAIL: [abc] 1; 180ns; 5.6M/s 180.0ns; a; b=c; uuid", null,10, 20,200, 200, 0, 1,0,  null, null, "abc", null),
                exampleWithContext("FAIL: op[abc] 1; 180ns; 5.6M/s 180.0ns; a; b=c; uuid", "op",10, 20,200, 200, 0,1,0,  null, null, "abc", null),
                exampleWithContext("FAIL: [abc] 1/10; 3.0us; 335.6k/s 3.0us; a; b=c; uuid", null,10, 20,3000, 3000, 0, 1,10,  null, null, "abc", null),
                exampleWithContext("FAIL: op[abc] 1/10; 3.0us; 335.6k/s 3.0us; a; b=c; uuid", "op",10, 20,3000, 3000, 0,1,10,  null, null, "abc", null),
                exampleWithContext("FAIL: [abc] 2/10; 3.0us; 671.1k/s 1.5us; a; b=c; uuid", null,10, 20,3000, 3000, 0, 2,10,  null, null, "abc", null),
                exampleWithContext("FAIL: op[abc] 2/10; 3.0us; 671.1k/s 1.5us; a; b=c; uuid", "op",10, 20,3000, 3000, 0,2,10,  null, null, "abc", null),

                exampleWithContext("FAIL: [abc; def] 180ns; a; b=c; uuid", null,10, 20,200, 4000, 0, 0,0,  null, null, "abc", "def"),
                exampleWithContext("FAIL: op[abc; def] 180ns; a; b=c; uuid", "op",10, 20,200, 4000, 0,0,0,  null, null, "abc", "def"),

                exampleWithContext("FAIL: [abc; def] 1/10; 180ns; 5.6M/s 180.0ns; a; b=c; uuid", null,10, 20,200, 200, 0, 1,10,  null, null, "abc", "def"),
                exampleWithContext("FAIL: op[abc; def] 1/10; 180ns; 5.6M/s 180.0ns; a; b=c; uuid", "op",10, 20,200, 200, 0,1,10,  null, null, "abc", "def"),
                exampleWithContext("FAIL: [abc; def] 1; 180ns; 5.6M/s 180.0ns; a; b=c; uuid", null,10, 20,200, 200, 0, 1,0,  null, null, "abc", "def"),
                exampleWithContext("FAIL: op[abc; def] 1; 180ns; 5.6M/s 180.0ns; a; b=c; uuid", "op",10, 20,200, 200, 0,1,0,  null, null, "abc", "def"),
                exampleWithContext("FAIL: [abc; def] 1/10; 3.0us; 335.6k/s 3.0us; a; b=c; uuid", null,10, 20,3000, 3000, 0, 1,10,  null, null, "abc", "def"),
                exampleWithContext("FAIL: op[abc; def] 1/10; 3.0us; 335.6k/s 3.0us; a; b=c; uuid", "op",10, 20,3000, 3000, 0,1,10,  null, null, "abc", "def"),
                exampleWithContext("FAIL: [abc; def] 2/10; 3.0us; 671.1k/s 1.5us; a; b=c; uuid", null,10, 20,3000, 3000, 0, 2,10,  null, null, "abc", "def"),
                exampleWithContext("FAIL: op[abc; def] 2/10; 3.0us; 671.1k/s 1.5us; a; b=c; uuid", "op",10, 20,3000, 3000, 0,2,10,  null, null, "abc", "def")
        );
    }

    static Stream<Arguments> provideTimeStatusTestCasesNoCategoryNoPositionWithDescription() {
        return Stream.of(
                exampleWithDescription("SCHEDULED: 190ns; 'desc'; uuid", null,10, 0,0, 200, 0,0,0),
                exampleWithDescription("SCHEDULED: op 190ns; 'desc'; uuid", "op",10, 0,0, 200, 0,0,0),

                exampleWithDescription("STARTED: 'desc'; uuid", null,10, 20,0, 200, 0,0,0),
                exampleWithDescription("STARTED: op 'desc'; uuid", "op",10, 20,0, 200, 0,0,0),

                exampleWithDescription("STARTED: 3.0us; 'desc'; uuid", null,10, 20,0, 3000, 0,0,0),
                exampleWithDescription("STARTED: op 3.0us; 'desc'; uuid", "op",10, 20,0, 3000, 0,0,0),

                exampleWithDescription("PROGRESS: 1/10; 'desc'; uuid", null,10, 20,0, 200, 0, 1,10),
                exampleWithDescription("PROGRESS: op 1/10; 'desc'; uuid", "op",10, 20,0, 200, 0,1,10),
                exampleWithDescription("PROGRESS: 1; 'desc'; uuid", null,10, 20,0, 200, 0, 1,0),
                exampleWithDescription("PROGRESS: op 1; 'desc'; uuid", "op",10, 20,0, 200, 0,1,0),
                exampleWithDescription("PROGRESS: 1/10; 3.0us; 335.6k/s 3.0us; 'desc'; uuid", null,10, 20,0, 3000, 0, 1,10),
                exampleWithDescription("PROGRESS: op 1/10; 3.0us; 335.6k/s 3.0us; 'desc'; uuid", "op",10, 20,0, 3000, 0,1,10),
                exampleWithDescription("PROGRESS: 2/10; 3.0us; 671.1k/s 1.5us; 'desc'; uuid", null,10, 20,0, 3000, 0, 2,10),
                exampleWithDescription("PROGRESS: op 2/10; 3.0us; 671.1k/s 1.5us; 'desc'; uuid", "op",10, 20,0, 3000, 0,2,10),

                exampleWithDescription("PROGRESS (Slow): 1/10; 'desc'; uuid", null,10, 20,0, 600, 500, 1,10),
                exampleWithDescription("PROGRESS (Slow): op 1/10; 'desc'; uuid", "op",10, 20,0, 600, 500,1,10),
                exampleWithDescription("PROGRESS (Slow): 1; 'desc'; uuid", null,10, 20,0, 600, 500, 1,0),
                exampleWithDescription("PROGRESS (Slow): op 1; 'desc'; uuid", "op",10, 20,0, 600, 500,1,0),
                exampleWithDescription("PROGRESS (Slow): 1/10; 3.0us; 335.6k/s 3.0us; 'desc'; uuid", null,10, 20,0, 3000, 500, 1,10),
                exampleWithDescription("PROGRESS (Slow): op 1/10; 3.0us; 335.6k/s 3.0us; 'desc'; uuid", "op",10, 20,0, 3000, 500,1,10),
                exampleWithDescription("PROGRESS (Slow): 2/10; 3.0us; 671.1k/s 1.5us; 'desc'; uuid", null,10, 20,0, 3000, 500, 2,10),
                exampleWithDescription("PROGRESS (Slow): op 2/10; 3.0us; 671.1k/s 1.5us; 'desc'; uuid", "op",10, 20,0, 3000, 500,2,10),

                exampleWithDescription("OK: 180ns; 'desc'; uuid", null,10, 20,200, 4000, 0, 0,0, null, null, null, null),
                exampleWithDescription("OK: op 180ns; 'desc'; uuid", "op",10, 20,200, 4000, 0,0,0, null, null, null, null),

                exampleWithDescription("OK (Slow): 3.0us; 'desc'; uuid", null,10, 20,3000, 4000, 200, 0,0, null, null, null, null),
                exampleWithDescription("OK (Slow): op 3.0us; 'desc'; uuid", "op",10, 20,3000, 4000, 200,0,0, null, null, null, null),

                exampleWithDescription("OK: 1/10; 180ns; 5.6M/s 180.0ns; 'desc'; uuid", null,10, 20,200, 200, 0, 1,10, null, null, null, null),
                exampleWithDescription("OK: op 1/10; 180ns; 5.6M/s 180.0ns; 'desc'; uuid", "op",10, 20,200, 200, 0,1,10, null, null, null, null),
                exampleWithDescription("OK: 1; 180ns; 5.6M/s 180.0ns; 'desc'; uuid", null,10, 20,200, 200, 0, 1,0, null, null, null, null),
                exampleWithDescription("OK: op 1; 180ns; 5.6M/s 180.0ns; 'desc'; uuid", "op",10, 20,200, 200, 0,1,0, null, null, null, null),
                exampleWithDescription("OK: 1/10; 3.0us; 335.6k/s 3.0us; 'desc'; uuid", null,10, 20,3000, 3000, 0, 1,10, null, null, null, null),
                exampleWithDescription("OK: op 1/10; 3.0us; 335.6k/s 3.0us; 'desc'; uuid", "op",10, 20,3000, 3000, 0,1,10, null, null, null, null),
                exampleWithDescription("OK: 2/10; 3.0us; 671.1k/s 1.5us; 'desc'; uuid", null,10, 20,3000, 3000, 0, 2,10, null, null, null, null),
                exampleWithDescription("OK: op 2/10; 3.0us; 671.1k/s 1.5us; 'desc'; uuid", "op",10, 20,3000, 3000, 0,2,10, null, null, null, null),

                exampleWithDescription("OK: 1/10; 180ns; 5.6M/s 180.0ns; 'desc'; uuid", null,10, 20,200, 200, 4000, 1,10, null, null, null, null),
                exampleWithDescription("OK: op 1/10; 180ns; 5.6M/s 180.0ns; 'desc'; uuid", "op",10, 20,200, 200, 4000,1,10, null, null, null, null),
                exampleWithDescription("OK: 1; 180ns; 5.6M/s 180.0ns; 'desc'; uuid", null,10, 20,200, 200, 4000, 1,0, null, null, null, null),
                exampleWithDescription("OK: op 1; 180ns; 5.6M/s 180.0ns; 'desc'; uuid", "op",10, 20,200, 200, 4000,1,0, null, null, null, null),
                exampleWithDescription("OK: 1/10; 3.0us; 335.6k/s 3.0us; 'desc'; uuid", null,10, 20,3000, 3000, 4000, 1,10, null, null, null, null),
                exampleWithDescription("OK: op 1/10; 3.0us; 335.6k/s 3.0us; 'desc'; uuid", "op",10, 20,3000, 3000, 4000,1,10, null, null, null, null),
                exampleWithDescription("OK: 2/10; 3.0us; 671.1k/s 1.5us; 'desc'; uuid", null,10, 20,3000, 3000, 4000, 2,10, null, null, null, null),
                exampleWithDescription("OK: op 2/10; 3.0us; 671.1k/s 1.5us; 'desc'; uuid", "op",10, 20,3000, 3000, 4000,2,10, null, null, null, null),

                exampleWithDescription("OK (Slow): 1/10; 580ns; 1.7M/s 580.0ns; 'desc'; uuid", null,10, 20,600, 4000, 500, 1,10, null, null, null, null),
                exampleWithDescription("OK (Slow): op 1/10; 580ns; 1.7M/s 580.0ns; 'desc'; uuid", "op",10, 20,600, 4000, 500,1,10, null, null, null, null),
                exampleWithDescription("OK (Slow): 1; 580ns; 1.7M/s 580.0ns; 'desc'; uuid", null,10, 20,600, 4000, 500, 1,0, null, null, null, null),
                exampleWithDescription("OK (Slow): op 1; 580ns; 1.7M/s 580.0ns; 'desc'; uuid", "op",10, 20,600, 4000, 500,1,0, null, null, null, null),
                exampleWithDescription("OK (Slow): 1/10; 3.0us; 335.6k/s 3.0us; 'desc'; uuid", null,10, 20,3000, 4000, 500, 1,10, null, null, null, null),
                exampleWithDescription("OK (Slow): op 1/10; 3.0us; 335.6k/s 3.0us; 'desc'; uuid", "op",10, 20,3000, 4000, 500,1,10, null, null, null, null),
                exampleWithDescription("OK (Slow): 2/10; 3.0us; 671.1k/s 1.5us; 'desc'; uuid", null,10, 20,3000, 4000, 500, 2,10, null, null, null, null),
                exampleWithDescription("OK (Slow): op 2/10; 3.0us; 671.1k/s 1.5us; 'desc'; uuid", "op",10, 20,3000, 4000, 500,2,10, null, null, null, null),

                exampleWithDescription("OK: [abc] 180ns; 'desc'; uuid", null,10, 20,200, 4000, 0, 0,0, "abc", null, null, null),
                exampleWithDescription("OK: op[abc] 180ns; 'desc'; uuid", "op",10, 20,200, 4000, 0,0,0, "abc", null, null, null),

                exampleWithDescription("OK (Slow): [abc] 3.0us; 'desc'; uuid", null,10, 20,3000, 4000, 200, 0,0, "abc", null, null, null),
                exampleWithDescription("OK (Slow): op[abc] 3.0us; 'desc'; uuid", "op",10, 20,3000, 4000, 200,0,0, "abc", null, null, null),

                exampleWithDescription("OK: [abc] 1/10; 180ns; 5.6M/s 180.0ns; 'desc'; uuid", null,10, 20,200, 200, 0, 1,10, "abc", null, null, null),
                exampleWithDescription("OK: op[abc] 1/10; 180ns; 5.6M/s 180.0ns; 'desc'; uuid", "op",10, 20,200, 200, 0,1,10, "abc", null, null, null),
                exampleWithDescription("OK: [abc] 1; 180ns; 5.6M/s 180.0ns; 'desc'; uuid", null,10, 20,200, 200, 0, 1,0, "abc", null, null, null),
                exampleWithDescription("OK: op[abc] 1; 180ns; 5.6M/s 180.0ns; 'desc'; uuid", "op",10, 20,200, 200, 0,1,0, "abc", null, null, null),
                exampleWithDescription("OK: [abc] 1/10; 3.0us; 335.6k/s 3.0us; 'desc'; uuid", null,10, 20,3000, 3000, 0, 1,10, "abc", null, null, null),
                exampleWithDescription("OK: op[abc] 1/10; 3.0us; 335.6k/s 3.0us; 'desc'; uuid", "op",10, 20,3000, 3000, 0,1,10, "abc", null, null, null),
                exampleWithDescription("OK: [abc] 2/10; 3.0us; 671.1k/s 1.5us; 'desc'; uuid", null,10, 20,3000, 3000, 0, 2,10, "abc", null, null, null),
                exampleWithDescription("OK: op[abc] 2/10; 3.0us; 671.1k/s 1.5us; 'desc'; uuid", "op",10, 20,3000, 3000, 0,2,10, "abc", null, null, null),

                exampleWithDescription("OK: [abc] 1/10; 180ns; 5.6M/s 180.0ns; 'desc'; uuid", null,10, 20,200, 200, 4000, 1,10, "abc", null, null, null),
                exampleWithDescription("OK: op[abc] 1/10; 180ns; 5.6M/s 180.0ns; 'desc'; uuid", "op",10, 20,200, 200, 4000,1,10, "abc", null, null, null),
                exampleWithDescription("OK: [abc] 1; 180ns; 5.6M/s 180.0ns; 'desc'; uuid", null,10, 20,200, 200, 4000, 1,0, "abc", null, null, null),
                exampleWithDescription("OK: op[abc] 1; 180ns; 5.6M/s 180.0ns; 'desc'; uuid", "op",10, 20,200, 200, 4000,1,0, "abc", null, null, null),
                exampleWithDescription("OK: [abc] 1/10; 3.0us; 335.6k/s 3.0us; 'desc'; uuid", null,10, 20,3000, 3000, 4000, 1,10, "abc", null, null, null),
                exampleWithDescription("OK: op[abc] 1/10; 3.0us; 335.6k/s 3.0us; 'desc'; uuid", "op",10, 20,3000, 3000, 4000,1,10, "abc", null, null, null),
                exampleWithDescription("OK: [abc] 2/10; 3.0us; 671.1k/s 1.5us; 'desc'; uuid", null,10, 20,3000, 3000, 4000, 2,10, "abc", null, null, null),
                exampleWithDescription("OK: op[abc] 2/10; 3.0us; 671.1k/s 1.5us; 'desc'; uuid", "op",10, 20,3000, 3000, 4000,2,10, "abc", null, null, null),

                exampleWithDescription("OK (Slow): [abc] 1/10; 580ns; 1.7M/s 580.0ns; 'desc'; uuid", null,10, 20,600, 4000, 500, 1,10, "abc", null, null, null),
                exampleWithDescription("OK (Slow): op[abc] 1/10; 580ns; 1.7M/s 580.0ns; 'desc'; uuid", "op",10, 20,600, 4000, 500,1,10, "abc", null, null, null),
                exampleWithDescription("OK (Slow): [abc] 1; 580ns; 1.7M/s 580.0ns; 'desc'; uuid", null,10, 20,600, 4000, 500, 1,0, "abc", null, null, null),
                exampleWithDescription("OK (Slow): op[abc] 1; 580ns; 1.7M/s 580.0ns; 'desc'; uuid", "op",10, 20,600, 4000, 500,1,0, "abc", null, null, null),
                exampleWithDescription("OK (Slow): [abc] 1/10; 3.0us; 335.6k/s 3.0us; 'desc'; uuid", null,10, 20,3000, 4000, 500, 1,10, "abc", null, null, null),
                exampleWithDescription("OK (Slow): op[abc] 1/10; 3.0us; 335.6k/s 3.0us; 'desc'; uuid", "op",10, 20,3000, 4000, 500,1,10, "abc", null, null, null),
                exampleWithDescription("OK (Slow): [abc] 2/10; 3.0us; 671.1k/s 1.5us; 'desc'; uuid", null,10, 20,3000, 4000, 500, 2,10, "abc", null, null, null),
                exampleWithDescription("OK (Slow): op[abc] 2/10; 3.0us; 671.1k/s 1.5us; 'desc'; uuid", "op",10, 20,3000, 4000, 500,2,10, "abc", null, null, null),

                exampleWithDescription("REJECT: [abc] 180ns; 'desc'; uuid", null,10, 20,200, 4000, 0, 0,0,  null, "abc", null, null),
                exampleWithDescription("REJECT: op[abc] 180ns; 'desc'; uuid", "op",10, 20,200, 4000, 0,0,0,  null, "abc", null, null),

                exampleWithDescription("REJECT: [abc] 1/10; 180ns; 5.6M/s 180.0ns; 'desc'; uuid", null,10, 20,200, 200, 0, 1,10,  null, "abc", null, null),
                exampleWithDescription("REJECT: op[abc] 1/10; 180ns; 5.6M/s 180.0ns; 'desc'; uuid", "op",10, 20,200, 200, 0,1,10,  null, "abc", null, null),
                exampleWithDescription("REJECT: [abc] 1; 180ns; 5.6M/s 180.0ns; 'desc'; uuid", null,10, 20,200, 200, 0, 1,0,  null, "abc", null, null),
                exampleWithDescription("REJECT: op[abc] 1; 180ns; 5.6M/s 180.0ns; 'desc'; uuid", "op",10, 20,200, 200, 0,1,0,  null, "abc", null, null),
                exampleWithDescription("REJECT: [abc] 1/10; 3.0us; 335.6k/s 3.0us; 'desc'; uuid", null,10, 20,3000, 3000, 0, 1,10,  null, "abc", null, null),
                exampleWithDescription("REJECT: op[abc] 1/10; 3.0us; 335.6k/s 3.0us; 'desc'; uuid", "op",10, 20,3000, 3000, 0,1,10,  null, "abc", null, null),
                exampleWithDescription("REJECT: [abc] 2/10; 3.0us; 671.1k/s 1.5us; 'desc'; uuid", null,10, 20,3000, 3000, 0, 2,10,  null, "abc", null, null),
                exampleWithDescription("REJECT: op[abc] 2/10; 3.0us; 671.1k/s 1.5us; 'desc'; uuid", "op",10, 20,3000, 3000, 0,2,10,  null, "abc", null, null),

                exampleWithDescription("FAIL: [abc] 180ns; 'desc'; uuid", null,10, 20,200, 4000, 0, 0,0,  null, null, "abc", null),
                exampleWithDescription("FAIL: op[abc] 180ns; 'desc'; uuid", "op",10, 20,200, 4000, 0,0,0,  null, null, "abc", null),

                exampleWithDescription("FAIL: [abc] 1/10; 180ns; 5.6M/s 180.0ns; 'desc'; uuid", null,10, 20,200, 200, 0, 1,10,  null, null, "abc", null),
                exampleWithDescription("FAIL: op[abc] 1/10; 180ns; 5.6M/s 180.0ns; 'desc'; uuid", "op",10, 20,200, 200, 0,1,10,  null, null, "abc", null),
                exampleWithDescription("FAIL: [abc] 1; 180ns; 5.6M/s 180.0ns; 'desc'; uuid", null,10, 20,200, 200, 0, 1,0,  null, null, "abc", null),
                exampleWithDescription("FAIL: op[abc] 1; 180ns; 5.6M/s 180.0ns; 'desc'; uuid", "op",10, 20,200, 200, 0,1,0,  null, null, "abc", null),
                exampleWithDescription("FAIL: [abc] 1/10; 3.0us; 335.6k/s 3.0us; 'desc'; uuid", null,10, 20,3000, 3000, 0, 1,10,  null, null, "abc", null),
                exampleWithDescription("FAIL: op[abc] 1/10; 3.0us; 335.6k/s 3.0us; 'desc'; uuid", "op",10, 20,3000, 3000, 0,1,10,  null, null, "abc", null),
                exampleWithDescription("FAIL: [abc] 2/10; 3.0us; 671.1k/s 1.5us; 'desc'; uuid", null,10, 20,3000, 3000, 0, 2,10,  null, null, "abc", null),
                exampleWithDescription("FAIL: op[abc] 2/10; 3.0us; 671.1k/s 1.5us; 'desc'; uuid", "op",10, 20,3000, 3000, 0,2,10,  null, null, "abc", null),

                exampleWithDescription("FAIL: [abc; def] 180ns; 'desc'; uuid", null,10, 20,200, 4000, 0, 0,0,  null, null, "abc", "def"),
                exampleWithDescription("FAIL: op[abc; def] 180ns; 'desc'; uuid", "op",10, 20,200, 4000, 0,0,0,  null, null, "abc", "def"),

                exampleWithDescription("FAIL: [abc; def] 1/10; 180ns; 5.6M/s 180.0ns; 'desc'; uuid", null,10, 20,200, 200, 0, 1,10,  null, null, "abc", "def"),
                exampleWithDescription("FAIL: op[abc; def] 1/10; 180ns; 5.6M/s 180.0ns; 'desc'; uuid", "op",10, 20,200, 200, 0,1,10,  null, null, "abc", "def"),
                exampleWithDescription("FAIL: [abc; def] 1; 180ns; 5.6M/s 180.0ns; 'desc'; uuid", null,10, 20,200, 200, 0, 1,0,  null, null, "abc", "def"),
                exampleWithDescription("FAIL: op[abc; def] 1; 180ns; 5.6M/s 180.0ns; 'desc'; uuid", "op",10, 20,200, 200, 0,1,0,  null, null, "abc", "def"),
                exampleWithDescription("FAIL: [abc; def] 1/10; 3.0us; 335.6k/s 3.0us; 'desc'; uuid", null,10, 20,3000, 3000, 0, 1,10,  null, null, "abc", "def"),
                exampleWithDescription("FAIL: op[abc; def] 1/10; 3.0us; 335.6k/s 3.0us; 'desc'; uuid", "op",10, 20,3000, 3000, 0,1,10,  null, null, "abc", "def"),
                exampleWithDescription("FAIL: [abc; def] 2/10; 3.0us; 671.1k/s 1.5us; 'desc'; uuid", null,10, 20,3000, 3000, 0, 2,10,  null, null, "abc", "def"),
                exampleWithDescription("FAIL: op[abc; def] 2/10; 3.0us; 671.1k/s 1.5us; 'desc'; uuid", "op",10, 20,3000, 3000, 0,2,10,  null, null, "abc", "def")
        );
    }

    static Stream<Arguments> provideTimeStatusTestCasesNoCategory() {
        return Stream.of(
                example("SCHEDULED: #1 190ns; uuid", null,10, 0,0, 200, 0,0,0),
                example("SCHEDULED: op#1 190ns; uuid", "op",10, 0,0, 200, 0,0,0),

                example("STARTED: #1 uuid", null,10, 20,0, 200, 0,0,0),
                example("STARTED: op#1 uuid", "op",10, 20,0, 200, 0,0,0),

                example("STARTED: #1 3.0us; uuid", null,10, 20,0, 3000, 0,0,0),
                example("STARTED: op#1 3.0us; uuid", "op",10, 20,0, 3000, 0,0,0),

                example("PROGRESS: #1 1/10; uuid", null,10, 20,0, 200, 0, 1,10),
                example("PROGRESS: op#1 1/10; uuid", "op",10, 20,0, 200, 0,1,10),
                example("PROGRESS: #1 1; uuid", null,10, 20,0, 200, 0, 1,0),
                example("PROGRESS: op#1 1; uuid", "op",10, 20,0, 200, 0,1,0),
                example("PROGRESS: #1 1/10; 3.0us; 335.6k/s 3.0us; uuid", null,10, 20,0, 3000, 0, 1,10),
                example("PROGRESS: op#1 1/10; 3.0us; 335.6k/s 3.0us; uuid", "op",10, 20,0, 3000, 0,1,10),
                example("PROGRESS: #1 2/10; 3.0us; 671.1k/s 1.5us; uuid", null,10, 20,0, 3000, 0, 2,10),
                example("PROGRESS: op#1 2/10; 3.0us; 671.1k/s 1.5us; uuid", "op",10, 20,0, 3000, 0,2,10),

                example("PROGRESS (Slow): #1 1/10; uuid", null,10, 20,0, 600, 500, 1,10),
                example("PROGRESS (Slow): op#1 1/10; uuid", "op",10, 20,0, 600, 500,1,10),
                example("PROGRESS (Slow): #1 1; uuid", null,10, 20,0, 600, 500, 1,0),
                example("PROGRESS (Slow): op#1 1; uuid", "op",10, 20,0, 600, 500,1,0),
                example("PROGRESS (Slow): #1 1/10; 3.0us; 335.6k/s 3.0us; uuid", null,10, 20,0, 3000, 500, 1,10),
                example("PROGRESS (Slow): op#1 1/10; 3.0us; 335.6k/s 3.0us; uuid", "op",10, 20,0, 3000, 500,1,10),
                example("PROGRESS (Slow): #1 2/10; 3.0us; 671.1k/s 1.5us; uuid", null,10, 20,0, 3000, 500, 2,10),
                example("PROGRESS (Slow): op#1 2/10; 3.0us; 671.1k/s 1.5us; uuid", "op",10, 20,0, 3000, 500,2,10),

                example("OK: #1 180ns; uuid", null,10, 20,200, 4000, 0, 0,0, null, null, null, null),
                example("OK: op#1 180ns; uuid", "op",10, 20,200, 4000, 0,0,0, null, null, null, null),

                example("OK (Slow): #1 3.0us; uuid", null,10, 20,3000, 4000, 200, 0,0, null, null, null, null),
                example("OK (Slow): op#1 3.0us; uuid", "op",10, 20,3000, 4000, 200,0,0, null, null, null, null),

                example("OK: #1 1/10; 180ns; 5.6M/s 180.0ns; uuid", null,10, 20,200, 200, 0, 1,10, null, null, null, null),
                example("OK: op#1 1/10; 180ns; 5.6M/s 180.0ns; uuid", "op",10, 20,200, 200, 0,1,10, null, null, null, null),
                example("OK: #1 1; 180ns; 5.6M/s 180.0ns; uuid", null,10, 20,200, 200, 0, 1,0, null, null, null, null),
                example("OK: op#1 1; 180ns; 5.6M/s 180.0ns; uuid", "op",10, 20,200, 200, 0,1,0, null, null, null, null),
                example("OK: #1 1/10; 3.0us; 335.6k/s 3.0us; uuid", null,10, 20,3000, 3000, 0, 1,10, null, null, null, null),
                example("OK: op#1 1/10; 3.0us; 335.6k/s 3.0us; uuid", "op",10, 20,3000, 3000, 0,1,10, null, null, null, null),
                example("OK: #1 2/10; 3.0us; 671.1k/s 1.5us; uuid", null,10, 20,3000, 3000, 0, 2,10, null, null, null, null),
                example("OK: op#1 2/10; 3.0us; 671.1k/s 1.5us; uuid", "op",10, 20,3000, 3000, 0,2,10, null, null, null, null),

                example("OK: #1 1/10; 180ns; 5.6M/s 180.0ns; uuid", null,10, 20,200, 200, 4000, 1,10, null, null, null, null),
                example("OK: op#1 1/10; 180ns; 5.6M/s 180.0ns; uuid", "op",10, 20,200, 200, 4000,1,10, null, null, null, null),
                example("OK: #1 1; 180ns; 5.6M/s 180.0ns; uuid", null,10, 20,200, 200, 4000, 1,0, null, null, null, null),
                example("OK: op#1 1; 180ns; 5.6M/s 180.0ns; uuid", "op",10, 20,200, 200, 4000,1,0, null, null, null, null),
                example("OK: #1 1/10; 3.0us; 335.6k/s 3.0us; uuid", null,10, 20,3000, 3000, 4000, 1,10, null, null, null, null),
                example("OK: op#1 1/10; 3.0us; 335.6k/s 3.0us; uuid", "op",10, 20,3000, 3000, 4000,1,10, null, null, null, null),
                example("OK: #1 2/10; 3.0us; 671.1k/s 1.5us; uuid", null,10, 20,3000, 3000, 4000, 2,10, null, null, null, null),
                example("OK: op#1 2/10; 3.0us; 671.1k/s 1.5us; uuid", "op",10, 20,3000, 3000, 4000,2,10, null, null, null, null),

                example("OK (Slow): #1 1/10; 580ns; 1.7M/s 580.0ns; uuid", null,10, 20,600, 4000, 500, 1,10, null, null, null, null),
                example("OK (Slow): op#1 1/10; 580ns; 1.7M/s 580.0ns; uuid", "op",10, 20,600, 4000, 500,1,10, null, null, null, null),
                example("OK (Slow): #1 1; 580ns; 1.7M/s 580.0ns; uuid", null,10, 20,600, 4000, 500, 1,0, null, null, null, null),
                example("OK (Slow): op#1 1; 580ns; 1.7M/s 580.0ns; uuid", "op",10, 20,600, 4000, 500,1,0, null, null, null, null),
                example("OK (Slow): #1 1/10; 3.0us; 335.6k/s 3.0us; uuid", null,10, 20,3000, 4000, 500, 1,10, null, null, null, null),
                example("OK (Slow): op#1 1/10; 3.0us; 335.6k/s 3.0us; uuid", "op",10, 20,3000, 4000, 500,1,10, null, null, null, null),
                example("OK (Slow): #1 2/10; 3.0us; 671.1k/s 1.5us; uuid", null,10, 20,3000, 4000, 500, 2,10, null, null, null, null),
                example("OK (Slow): op#1 2/10; 3.0us; 671.1k/s 1.5us; uuid", "op",10, 20,3000, 4000, 500,2,10, null, null, null, null),

                example("OK: #1[abc] 180ns; uuid", null,10, 20,200, 4000, 0, 0,0, "abc", null, null, null),
                example("OK: op#1[abc] 180ns; uuid", "op",10, 20,200, 4000, 0,0,0, "abc", null, null, null),

                example("OK (Slow): #1[abc] 3.0us; uuid", null,10, 20,3000, 4000, 200, 0,0, "abc", null, null, null),
                example("OK (Slow): op#1[abc] 3.0us; uuid", "op",10, 20,3000, 4000, 200,0,0, "abc", null, null, null),

                example("OK: #1[abc] 1/10; 180ns; 5.6M/s 180.0ns; uuid", null,10, 20,200, 200, 0, 1,10, "abc", null, null, null),
                example("OK: op#1[abc] 1/10; 180ns; 5.6M/s 180.0ns; uuid", "op",10, 20,200, 200, 0,1,10, "abc", null, null, null),
                example("OK: #1[abc] 1; 180ns; 5.6M/s 180.0ns; uuid", null,10, 20,200, 200, 0, 1,0, "abc", null, null, null),
                example("OK: op#1[abc] 1; 180ns; 5.6M/s 180.0ns; uuid", "op",10, 20,200, 200, 0,1,0, "abc", null, null, null),
                example("OK: #1[abc] 1/10; 3.0us; 335.6k/s 3.0us; uuid", null,10, 20,3000, 3000, 0, 1,10, "abc", null, null, null),
                example("OK: op#1[abc] 1/10; 3.0us; 335.6k/s 3.0us; uuid", "op",10, 20,3000, 3000, 0,1,10, "abc", null, null, null),
                example("OK: #1[abc] 2/10; 3.0us; 671.1k/s 1.5us; uuid", null,10, 20,3000, 3000, 0, 2,10, "abc", null, null, null),
                example("OK: op#1[abc] 2/10; 3.0us; 671.1k/s 1.5us; uuid", "op",10, 20,3000, 3000, 0,2,10, "abc", null, null, null),

                example("OK: #1[abc] 1/10; 180ns; 5.6M/s 180.0ns; uuid", null,10, 20,200, 200, 4000, 1,10, "abc", null, null, null),
                example("OK: op#1[abc] 1/10; 180ns; 5.6M/s 180.0ns; uuid", "op",10, 20,200, 200, 4000,1,10, "abc", null, null, null),
                example("OK: #1[abc] 1; 180ns; 5.6M/s 180.0ns; uuid", null,10, 20,200, 200, 4000, 1,0, "abc", null, null, null),
                example("OK: op#1[abc] 1; 180ns; 5.6M/s 180.0ns; uuid", "op",10, 20,200, 200, 4000,1,0, "abc", null, null, null),
                example("OK: #1[abc] 1/10; 3.0us; 335.6k/s 3.0us; uuid", null,10, 20,3000, 3000, 4000, 1,10, "abc", null, null, null),
                example("OK: op#1[abc] 1/10; 3.0us; 335.6k/s 3.0us; uuid", "op",10, 20,3000, 3000, 4000,1,10, "abc", null, null, null),
                example("OK: #1[abc] 2/10; 3.0us; 671.1k/s 1.5us; uuid", null,10, 20,3000, 3000, 4000, 2,10, "abc", null, null, null),
                example("OK: op#1[abc] 2/10; 3.0us; 671.1k/s 1.5us; uuid", "op",10, 20,3000, 3000, 4000,2,10, "abc", null, null, null),

                example("OK (Slow): #1[abc] 1/10; 580ns; 1.7M/s 580.0ns; uuid", null,10, 20,600, 4000, 500, 1,10, "abc", null, null, null),
                example("OK (Slow): op#1[abc] 1/10; 580ns; 1.7M/s 580.0ns; uuid", "op",10, 20,600, 4000, 500,1,10, "abc", null, null, null),
                example("OK (Slow): #1[abc] 1; 580ns; 1.7M/s 580.0ns; uuid", null,10, 20,600, 4000, 500, 1,0, "abc", null, null, null),
                example("OK (Slow): op#1[abc] 1; 580ns; 1.7M/s 580.0ns; uuid", "op",10, 20,600, 4000, 500,1,0, "abc", null, null, null),
                example("OK (Slow): #1[abc] 1/10; 3.0us; 335.6k/s 3.0us; uuid", null,10, 20,3000, 4000, 500, 1,10, "abc", null, null, null),
                example("OK (Slow): op#1[abc] 1/10; 3.0us; 335.6k/s 3.0us; uuid", "op",10, 20,3000, 4000, 500,1,10, "abc", null, null, null),
                example("OK (Slow): #1[abc] 2/10; 3.0us; 671.1k/s 1.5us; uuid", null,10, 20,3000, 4000, 500, 2,10, "abc", null, null, null),
                example("OK (Slow): op#1[abc] 2/10; 3.0us; 671.1k/s 1.5us; uuid", "op",10, 20,3000, 4000, 500,2,10, "abc", null, null, null),

                example("REJECT: #1[abc] 180ns; uuid", null,10, 20,200, 4000, 0, 0,0,  null, "abc", null, null),
                example("REJECT: op#1[abc] 180ns; uuid", "op",10, 20,200, 4000, 0,0,0,  null, "abc", null, null),

                example("REJECT: #1[abc] 1/10; 180ns; 5.6M/s 180.0ns; uuid", null,10, 20,200, 200, 0, 1,10,  null, "abc", null, null),
                example("REJECT: op#1[abc] 1/10; 180ns; 5.6M/s 180.0ns; uuid", "op",10, 20,200, 200, 0,1,10,  null, "abc", null, null),
                example("REJECT: #1[abc] 1; 180ns; 5.6M/s 180.0ns; uuid", null,10, 20,200, 200, 0, 1,0,  null, "abc", null, null),
                example("REJECT: op#1[abc] 1; 180ns; 5.6M/s 180.0ns; uuid", "op",10, 20,200, 200, 0,1,0,  null, "abc", null, null),
                example("REJECT: #1[abc] 1/10; 3.0us; 335.6k/s 3.0us; uuid", null,10, 20,3000, 3000, 0, 1,10,  null, "abc", null, null),
                example("REJECT: op#1[abc] 1/10; 3.0us; 335.6k/s 3.0us; uuid", "op",10, 20,3000, 3000, 0,1,10,  null, "abc", null, null),
                example("REJECT: #1[abc] 2/10; 3.0us; 671.1k/s 1.5us; uuid", null,10, 20,3000, 3000, 0, 2,10,  null, "abc", null, null),
                example("REJECT: op#1[abc] 2/10; 3.0us; 671.1k/s 1.5us; uuid", "op",10, 20,3000, 3000, 0,2,10,  null, "abc", null, null),

                example("FAIL: #1[abc] 180ns; uuid", null,10, 20,200, 4000, 0, 0,0,  null, null, "abc", null),
                example("FAIL: op#1[abc] 180ns; uuid", "op",10, 20,200, 4000, 0,0,0,  null, null, "abc", null),

                example("FAIL: #1[abc] 1/10; 180ns; 5.6M/s 180.0ns; uuid", null,10, 20,200, 200, 0, 1,10,  null, null, "abc", null),
                example("FAIL: op#1[abc] 1/10; 180ns; 5.6M/s 180.0ns; uuid", "op",10, 20,200, 200, 0,1,10,  null, null, "abc", null),
                example("FAIL: #1[abc] 1; 180ns; 5.6M/s 180.0ns; uuid", null,10, 20,200, 200, 0, 1,0,  null, null, "abc", null),
                example("FAIL: op#1[abc] 1; 180ns; 5.6M/s 180.0ns; uuid", "op",10, 20,200, 200, 0,1,0,  null, null, "abc", null),
                example("FAIL: #1[abc] 1/10; 3.0us; 335.6k/s 3.0us; uuid", null,10, 20,3000, 3000, 0, 1,10,  null, null, "abc", null),
                example("FAIL: op#1[abc] 1/10; 3.0us; 335.6k/s 3.0us; uuid", "op",10, 20,3000, 3000, 0,1,10,  null, null, "abc", null),
                example("FAIL: #1[abc] 2/10; 3.0us; 671.1k/s 1.5us; uuid", null,10, 20,3000, 3000, 0, 2,10,  null, null, "abc", null),
                example("FAIL: op#1[abc] 2/10; 3.0us; 671.1k/s 1.5us; uuid", "op",10, 20,3000, 3000, 0,2,10,  null, null, "abc", null),

                example("FAIL: #1[abc; def] 180ns; uuid", null,10, 20,200, 4000, 0, 0,0,  null, null, "abc", "def"),
                example("FAIL: op#1[abc; def] 180ns; uuid", "op",10, 20,200, 4000, 0,0,0,  null, null, "abc", "def"),

                example("FAIL: #1[abc; def] 1/10; 180ns; 5.6M/s 180.0ns; uuid", null,10, 20,200, 200, 0, 1,10,  null, null, "abc", "def"),
                example("FAIL: op#1[abc; def] 1/10; 180ns; 5.6M/s 180.0ns; uuid", "op",10, 20,200, 200, 0,1,10,  null, null, "abc", "def"),
                example("FAIL: #1[abc; def] 1; 180ns; 5.6M/s 180.0ns; uuid", null,10, 20,200, 200, 0, 1,0,  null, null, "abc", "def"),
                example("FAIL: op#1[abc; def] 1; 180ns; 5.6M/s 180.0ns; uuid", "op",10, 20,200, 200, 0,1,0,  null, null, "abc", "def"),
                example("FAIL: #1[abc; def] 1/10; 3.0us; 335.6k/s 3.0us; uuid", null,10, 20,3000, 3000, 0, 1,10,  null, null, "abc", "def"),
                example("FAIL: op#1[abc; def] 1/10; 3.0us; 335.6k/s 3.0us; uuid", "op",10, 20,3000, 3000, 0,1,10,  null, null, "abc", "def"),
                example("FAIL: #1[abc; def] 2/10; 3.0us; 671.1k/s 1.5us; uuid", null,10, 20,3000, 3000, 0, 2,10,  null, null, "abc", "def"),
                example("FAIL: op#1[abc; def] 2/10; 3.0us; 671.1k/s 1.5us; uuid", "op",10, 20,3000, 3000, 0,2,10,  null, null, "abc", "def")
        );
    }

    static Stream<Arguments> provideTimeStatusTestCasesNoStatus() {
        return Stream.of(
                example("cat#1 190ns; uuid", null,10, 0,0, 200, 0,0,0),
                example("cat/op#1 190ns; uuid", "op",10, 0,0, 200, 0,0,0),

                example("cat#1 uuid", null,10, 20,0, 200, 0,0,0),
                example("cat/op#1 uuid", "op",10, 20,0, 200, 0,0,0),

                example("cat#1 3.0us; uuid", null,10, 20,0, 3000, 0,0,0),
                example("cat/op#1 3.0us; uuid", "op",10, 20,0, 3000, 0,0,0),

                example("cat#1 1/10; uuid", null,10, 20,0, 200, 0, 1,10),
                example("cat/op#1 1/10; uuid", "op",10, 20,0, 200, 0,1,10),
                example("cat#1 1; uuid", null,10, 20,0, 200, 0, 1,0),
                example("cat/op#1 1; uuid", "op",10, 20,0, 200, 0,1,0),
                example("cat#1 1/10; 3.0us; 335.6k/s 3.0us; uuid", null,10, 20,0, 3000, 0, 1,10),
                example("cat/op#1 1/10; 3.0us; 335.6k/s 3.0us; uuid", "op",10, 20,0, 3000, 0,1,10),
                example("cat#1 2/10; 3.0us; 671.1k/s 1.5us; uuid", null,10, 20,0, 3000, 0, 2,10),
                example("cat/op#1 2/10; 3.0us; 671.1k/s 1.5us; uuid", "op",10, 20,0, 3000, 0,2,10),

                example("cat#1 1/10; uuid", null,10, 20,0, 600, 500, 1,10),
                example("cat/op#1 1/10; uuid", "op",10, 20,0, 600, 500,1,10),
                example("cat#1 1; uuid", null,10, 20,0, 600, 500, 1,0),
                example("cat/op#1 1; uuid", "op",10, 20,0, 600, 500,1,0),
                example("cat#1 1/10; 3.0us; 335.6k/s 3.0us; uuid", null,10, 20,0, 3000, 500, 1,10),
                example("cat/op#1 1/10; 3.0us; 335.6k/s 3.0us; uuid", "op",10, 20,0, 3000, 500,1,10),
                example("cat#1 2/10; 3.0us; 671.1k/s 1.5us; uuid", null,10, 20,0, 3000, 500, 2,10),
                example("cat/op#1 2/10; 3.0us; 671.1k/s 1.5us; uuid", "op",10, 20,0, 3000, 500,2,10),

                example("cat#1 180ns; uuid", null,10, 20,200, 4000, 0, 0,0, null, null, null, null),
                example("cat/op#1 180ns; uuid", "op",10, 20,200, 4000, 0,0,0, null, null, null, null),

                example("cat#1 3.0us; uuid", null,10, 20,3000, 4000, 200, 0,0, null, null, null, null),
                example("cat/op#1 3.0us; uuid", "op",10, 20,3000, 4000, 200,0,0, null, null, null, null),

                example("cat#1 1/10; 180ns; 5.6M/s 180.0ns; uuid", null,10, 20,200, 200, 0, 1,10, null, null, null, null),
                example("cat/op#1 1/10; 180ns; 5.6M/s 180.0ns; uuid", "op",10, 20,200, 200, 0,1,10, null, null, null, null),
                example("cat#1 1; 180ns; 5.6M/s 180.0ns; uuid", null,10, 20,200, 200, 0, 1,0, null, null, null, null),
                example("cat/op#1 1; 180ns; 5.6M/s 180.0ns; uuid", "op",10, 20,200, 200, 0,1,0, null, null, null, null),
                example("cat#1 1/10; 3.0us; 335.6k/s 3.0us; uuid", null,10, 20,3000, 3000, 0, 1,10, null, null, null, null),
                example("cat/op#1 1/10; 3.0us; 335.6k/s 3.0us; uuid", "op",10, 20,3000, 3000, 0,1,10, null, null, null, null),
                example("cat#1 2/10; 3.0us; 671.1k/s 1.5us; uuid", null,10, 20,3000, 3000, 0, 2,10, null, null, null, null),
                example("cat/op#1 2/10; 3.0us; 671.1k/s 1.5us; uuid", "op",10, 20,3000, 3000, 0,2,10, null, null, null, null),

                example("cat#1 1/10; 180ns; 5.6M/s 180.0ns; uuid", null,10, 20,200, 200, 4000, 1,10, null, null, null, null),
                example("cat/op#1 1/10; 180ns; 5.6M/s 180.0ns; uuid", "op",10, 20,200, 200, 4000,1,10, null, null, null, null),
                example("cat#1 1; 180ns; 5.6M/s 180.0ns; uuid", null,10, 20,200, 200, 4000, 1,0, null, null, null, null),
                example("cat/op#1 1; 180ns; 5.6M/s 180.0ns; uuid", "op",10, 20,200, 200, 4000,1,0, null, null, null, null),
                example("cat#1 1/10; 3.0us; 335.6k/s 3.0us; uuid", null,10, 20,3000, 3000, 4000, 1,10, null, null, null, null),
                example("cat/op#1 1/10; 3.0us; 335.6k/s 3.0us; uuid", "op",10, 20,3000, 3000, 4000,1,10, null, null, null, null),
                example("cat#1 2/10; 3.0us; 671.1k/s 1.5us; uuid", null,10, 20,3000, 3000, 4000, 2,10, null, null, null, null),
                example("cat/op#1 2/10; 3.0us; 671.1k/s 1.5us; uuid", "op",10, 20,3000, 3000, 4000,2,10, null, null, null, null),

                example("cat#1 1/10; 580ns; 1.7M/s 580.0ns; uuid", null,10, 20,600, 4000, 500, 1,10, null, null, null, null),
                example("cat/op#1 1/10; 580ns; 1.7M/s 580.0ns; uuid", "op",10, 20,600, 4000, 500,1,10, null, null, null, null),
                example("cat#1 1; 580ns; 1.7M/s 580.0ns; uuid", null,10, 20,600, 4000, 500, 1,0, null, null, null, null),
                example("cat/op#1 1; 580ns; 1.7M/s 580.0ns; uuid", "op",10, 20,600, 4000, 500,1,0, null, null, null, null),
                example("cat#1 1/10; 3.0us; 335.6k/s 3.0us; uuid", null,10, 20,3000, 4000, 500, 1,10, null, null, null, null),
                example("cat/op#1 1/10; 3.0us; 335.6k/s 3.0us; uuid", "op",10, 20,3000, 4000, 500,1,10, null, null, null, null),
                example("cat#1 2/10; 3.0us; 671.1k/s 1.5us; uuid", null,10, 20,3000, 4000, 500, 2,10, null, null, null, null),
                example("cat/op#1 2/10; 3.0us; 671.1k/s 1.5us; uuid", "op",10, 20,3000, 4000, 500,2,10, null, null, null, null),

                example("cat#1[abc] 180ns; uuid", null,10, 20,200, 4000, 0, 0,0, "abc", null, null, null),
                example("cat/op#1[abc] 180ns; uuid", "op",10, 20,200, 4000, 0,0,0, "abc", null, null, null),

                example("cat#1[abc] 3.0us; uuid", null,10, 20,3000, 4000, 200, 0,0, "abc", null, null, null),
                example("cat/op#1[abc] 3.0us; uuid", "op",10, 20,3000, 4000, 200,0,0, "abc", null, null, null),

                example("cat#1[abc] 1/10; 180ns; 5.6M/s 180.0ns; uuid", null,10, 20,200, 200, 0, 1,10, "abc", null, null, null),
                example("cat/op#1[abc] 1/10; 180ns; 5.6M/s 180.0ns; uuid", "op",10, 20,200, 200, 0,1,10, "abc", null, null, null),
                example("cat#1[abc] 1; 180ns; 5.6M/s 180.0ns; uuid", null,10, 20,200, 200, 0, 1,0, "abc", null, null, null),
                example("cat/op#1[abc] 1; 180ns; 5.6M/s 180.0ns; uuid", "op",10, 20,200, 200, 0,1,0, "abc", null, null, null),
                example("cat#1[abc] 1/10; 3.0us; 335.6k/s 3.0us; uuid", null,10, 20,3000, 3000, 0, 1,10, "abc", null, null, null),
                example("cat/op#1[abc] 1/10; 3.0us; 335.6k/s 3.0us; uuid", "op",10, 20,3000, 3000, 0,1,10, "abc", null, null, null),
                example("cat#1[abc] 2/10; 3.0us; 671.1k/s 1.5us; uuid", null,10, 20,3000, 3000, 0, 2,10, "abc", null, null, null),
                example("cat/op#1[abc] 2/10; 3.0us; 671.1k/s 1.5us; uuid", "op",10, 20,3000, 3000, 0,2,10, "abc", null, null, null),

                example("cat#1[abc] 1/10; 180ns; 5.6M/s 180.0ns; uuid", null,10, 20,200, 200, 4000, 1,10, "abc", null, null, null),
                example("cat/op#1[abc] 1/10; 180ns; 5.6M/s 180.0ns; uuid", "op",10, 20,200, 200, 4000,1,10, "abc", null, null, null),
                example("cat#1[abc] 1; 180ns; 5.6M/s 180.0ns; uuid", null,10, 20,200, 200, 4000, 1,0, "abc", null, null, null),
                example("cat/op#1[abc] 1; 180ns; 5.6M/s 180.0ns; uuid", "op",10, 20,200, 200, 4000,1,0, "abc", null, null, null),
                example("cat#1[abc] 1/10; 3.0us; 335.6k/s 3.0us; uuid", null,10, 20,3000, 3000, 4000, 1,10, "abc", null, null, null),
                example("cat/op#1[abc] 1/10; 3.0us; 335.6k/s 3.0us; uuid", "op",10, 20,3000, 3000, 4000,1,10, "abc", null, null, null),
                example("cat#1[abc] 2/10; 3.0us; 671.1k/s 1.5us; uuid", null,10, 20,3000, 3000, 4000, 2,10, "abc", null, null, null),
                example("cat/op#1[abc] 2/10; 3.0us; 671.1k/s 1.5us; uuid", "op",10, 20,3000, 3000, 4000,2,10, "abc", null, null, null),

                example("cat#1[abc] 1/10; 580ns; 1.7M/s 580.0ns; uuid", null,10, 20,600, 4000, 500, 1,10, "abc", null, null, null),
                example("cat/op#1[abc] 1/10; 580ns; 1.7M/s 580.0ns; uuid", "op",10, 20,600, 4000, 500,1,10, "abc", null, null, null),
                example("cat#1[abc] 1; 580ns; 1.7M/s 580.0ns; uuid", null,10, 20,600, 4000, 500, 1,0, "abc", null, null, null),
                example("cat/op#1[abc] 1; 580ns; 1.7M/s 580.0ns; uuid", "op",10, 20,600, 4000, 500,1,0, "abc", null, null, null),
                example("cat#1[abc] 1/10; 3.0us; 335.6k/s 3.0us; uuid", null,10, 20,3000, 4000, 500, 1,10, "abc", null, null, null),
                example("cat/op#1[abc] 1/10; 3.0us; 335.6k/s 3.0us; uuid", "op",10, 20,3000, 4000, 500,1,10, "abc", null, null, null),
                example("cat#1[abc] 2/10; 3.0us; 671.1k/s 1.5us; uuid", null,10, 20,3000, 4000, 500, 2,10, "abc", null, null, null),
                example("cat/op#1[abc] 2/10; 3.0us; 671.1k/s 1.5us; uuid", "op",10, 20,3000, 4000, 500,2,10, "abc", null, null, null),

                example("cat#1[abc] 180ns; uuid", null,10, 20,200, 4000, 0, 0,0,  null, "abc", null, null),
                example("cat/op#1[abc] 180ns; uuid", "op",10, 20,200, 4000, 0,0,0,  null, "abc", null, null),

                example("cat#1[abc] 1/10; 180ns; 5.6M/s 180.0ns; uuid", null,10, 20,200, 200, 0, 1,10,  null, "abc", null, null),
                example("cat/op#1[abc] 1/10; 180ns; 5.6M/s 180.0ns; uuid", "op",10, 20,200, 200, 0,1,10,  null, "abc", null, null),
                example("cat#1[abc] 1; 180ns; 5.6M/s 180.0ns; uuid", null,10, 20,200, 200, 0, 1,0,  null, "abc", null, null),
                example("cat/op#1[abc] 1; 180ns; 5.6M/s 180.0ns; uuid", "op",10, 20,200, 200, 0,1,0,  null, "abc", null, null),
                example("cat#1[abc] 1/10; 3.0us; 335.6k/s 3.0us; uuid", null,10, 20,3000, 3000, 0, 1,10,  null, "abc", null, null),
                example("cat/op#1[abc] 1/10; 3.0us; 335.6k/s 3.0us; uuid", "op",10, 20,3000, 3000, 0,1,10,  null, "abc", null, null),
                example("cat#1[abc] 2/10; 3.0us; 671.1k/s 1.5us; uuid", null,10, 20,3000, 3000, 0, 2,10,  null, "abc", null, null),
                example("cat/op#1[abc] 2/10; 3.0us; 671.1k/s 1.5us; uuid", "op",10, 20,3000, 3000, 0,2,10,  null, "abc", null, null),

                example("cat#1[abc] 180ns; uuid", null,10, 20,200, 4000, 0, 0,0,  null, null, "abc", null),
                example("cat/op#1[abc] 180ns; uuid", "op",10, 20,200, 4000, 0,0,0,  null, null, "abc", null),

                example("cat#1[abc] 1/10; 180ns; 5.6M/s 180.0ns; uuid", null,10, 20,200, 200, 0, 1,10,  null, null, "abc", null),
                example("cat/op#1[abc] 1/10; 180ns; 5.6M/s 180.0ns; uuid", "op",10, 20,200, 200, 0,1,10,  null, null, "abc", null),
                example("cat#1[abc] 1; 180ns; 5.6M/s 180.0ns; uuid", null,10, 20,200, 200, 0, 1,0,  null, null, "abc", null),
                example("cat/op#1[abc] 1; 180ns; 5.6M/s 180.0ns; uuid", "op",10, 20,200, 200, 0,1,0,  null, null, "abc", null),
                example("cat#1[abc] 1/10; 3.0us; 335.6k/s 3.0us; uuid", null,10, 20,3000, 3000, 0, 1,10,  null, null, "abc", null),
                example("cat/op#1[abc] 1/10; 3.0us; 335.6k/s 3.0us; uuid", "op",10, 20,3000, 3000, 0,1,10,  null, null, "abc", null),
                example("cat#1[abc] 2/10; 3.0us; 671.1k/s 1.5us; uuid", null,10, 20,3000, 3000, 0, 2,10,  null, null, "abc", null),
                example("cat/op#1[abc] 2/10; 3.0us; 671.1k/s 1.5us; uuid", "op",10, 20,3000, 3000, 0,2,10,  null, null, "abc", null),

                example("cat#1[abc; def] 180ns; uuid", null,10, 20,200, 4000, 0, 0,0,  null, null, "abc", "def"),
                example("cat/op#1[abc; def] 180ns; uuid", "op",10, 20,200, 4000, 0,0,0,  null, null, "abc", "def"),

                example("cat#1[abc; def] 1/10; 180ns; 5.6M/s 180.0ns; uuid", null,10, 20,200, 200, 0, 1,10,  null, null, "abc", "def"),
                example("cat/op#1[abc; def] 1/10; 180ns; 5.6M/s 180.0ns; uuid", "op",10, 20,200, 200, 0,1,10,  null, null, "abc", "def"),
                example("cat#1[abc; def] 1; 180ns; 5.6M/s 180.0ns; uuid", null,10, 20,200, 200, 0, 1,0,  null, null, "abc", "def"),
                example("cat/op#1[abc; def] 1; 180ns; 5.6M/s 180.0ns; uuid", "op",10, 20,200, 200, 0,1,0,  null, null, "abc", "def"),
                example("cat#1[abc; def] 1/10; 3.0us; 335.6k/s 3.0us; uuid", null,10, 20,3000, 3000, 0, 1,10,  null, null, "abc", "def"),
                example("cat/op#1[abc; def] 1/10; 3.0us; 335.6k/s 3.0us; uuid", "op",10, 20,3000, 3000, 0,1,10,  null, null, "abc", "def"),
                example("cat#1[abc; def] 2/10; 3.0us; 671.1k/s 1.5us; uuid", null,10, 20,3000, 3000, 0, 2,10,  null, null, "abc", "def"),
                example("cat/op#1[abc; def] 2/10; 3.0us; 671.1k/s 1.5us; uuid", "op",10, 20,3000, 3000, 0,2,10,  null, null, "abc", "def")
        );
    }

    @ParameterizedTest
    @MethodSource("provideTimeStatusTestCasesNoCategoryNoPosition")
    void testReadableMessageTimeStatusNoCategoryNoPosition(final MockMeterData value, final String expected) {
        assertEquals(expected, value.readableMessage());
    }

    @ParameterizedTest
    @MethodSource("provideTimeStatusTestCasesNoCategoryNoPositionWithDescription")
    void testReadableMessageTimeStatusNoCategoryNoPositionWithDescription(final MockMeterData value, final String expected) {
        assertEquals(expected, value.readableMessage());
    }

    @ParameterizedTest
    @MethodSource("provideTimeStatusTestCasesNoCategoryNoPositionWithContext")
    void testReadableMessageTimeStatusNoCategoryNoPositionWithContext(final MockMeterData value, final String expected) {
        assertEquals(expected, value.readableMessage());
    }

    @ParameterizedTest
    @MethodSource("provideTimeStatusTestCasesNoCategory")
    void testReadableMessageTimeStatusNoCategory(final MockMeterData value, final String expected) {
        MeterConfig.printPosition = true;
        assertEquals(expected, value.readableMessage());
    }

    @ParameterizedTest
    @MethodSource("provideTimeStatusTestCasesNoStatus")
    void testReadableMessageTimeStatusNoStatus(final MockMeterData value, final String expected) {
        MeterConfig.printPosition = true;
        MeterConfig.printCategory = true;
        MeterConfig.printStatus = false;
        assertEquals(expected, value.readableMessage());
    }
}
