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
package org.usefultoys.slf4j.internal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.usefultoys.test.CharsetConsistencyExtension;
import org.usefultoys.test.WithLocale;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@WithLocale("en")
@ExtendWith(CharsetConsistencyExtension.class)
class EventDataJson5Test {
    // Concrete class for testing the abstract EventData
    private static class TestEventData extends EventData {
        TestEventData() {
            super();
        }

        TestEventData(String sessionUuid, long position, long lastCurrentTime) {
            super(sessionUuid, position, lastCurrentTime);
        }
    }

    static Stream<Arguments> roundTripScenarios() {
        final String uuid1 = "8ae94091";
        final String uuid2 = "ddee333e";
        return Stream.of(
                Arguments.of(
                        "Full data",
                        new TestEventData(uuid1, 1, 1000L),
                        String.format("_:%s,$:1,t:1000", uuid1)
                ),
                Arguments.of(
                        "Max values",
                        new TestEventData(uuid2, Long.MAX_VALUE, 0),
                        String.format("_:%s,$:%d,t:0", uuid2, Long.MAX_VALUE)
                ),
                Arguments.of(
                        "Zero values",
                        new TestEventData(uuid2, 0, 0),
                        String.format("_:%s,$:0,t:0",uuid2)
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("roundTripScenarios")
    @DisplayName("Should correctly serialize and deserialize (round-trip)")
    void testRoundTrip(String testName, EventData originalData, String expectedJson) {
        // 1. Test Serialization (Write)
        final StringBuilder sb = new StringBuilder();
        EventDataJson5.write(originalData, sb);
        final String actualJson = sb.toString();
        assertEquals(expectedJson, actualJson);

        // 2. Test Deserialization (Read)
        final TestEventData newData = new TestEventData();
        EventDataJson5.read(newData, "{"+actualJson+"}");

        // 3. Assert Round-trip consistency
        assertEquals(originalData.getSessionUuid(), newData.getSessionUuid());
        assertEquals(originalData.getPosition(), newData.getPosition());
        assertEquals(originalData.getLastCurrentTime(), newData.getLastCurrentTime());
    }

    static Stream<Arguments> readEdgeCaseScenarios() {
        final String uuid = "8ae94091";
        return Stream.of(
                Arguments.of(
                        "Disordered fields",
                        String.format("{t:%d, _:%s, $:%d}", 2000L, uuid, 2L),
                        new TestEventData(uuid, 2L, 2000L)
                ),
                Arguments.of(
                        "Missing fields",
                        String.format("{_:%s}", uuid),
                        new TestEventData(uuid, 0L, 0L)
                ),
                Arguments.of(
                        "Extra whitespace",
                        String.format("{ _ : %s , $ : 3, t:3000 }", uuid),
                        new TestEventData(uuid, 3L, 3000L)
                ),
                Arguments.of(
                        "Empty JSON",
                        "{}",
                        new TestEventData(null, 0L, 0L)
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("readEdgeCaseScenarios")
    @DisplayName("Should correctly read edge cases")
    void testReadEdgeCases(String testName, String inputJson, EventData expectedData) {
        final TestEventData actualData = new TestEventData();
        EventDataJson5.read(actualData, inputJson);

        assertEquals(expectedData.getSessionUuid(), actualData.getSessionUuid());
        assertEquals(expectedData.getPosition(), actualData.getPosition());
        assertEquals(expectedData.getLastCurrentTime(), actualData.getLastCurrentTime());
    }
}
