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
package org.usefultoys.slf4j.watcher;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.usefultoys.test.ValidateCharset;
import org.usefultoys.test.WithLocale;

import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Unit tests for {@link WatcherDataFormatter}.
 * <p>
 * Tests validate that WatcherDataFormatter correctly formats WatcherData into human-readable strings,
 * handling memory values, system load, and UUID information with proper locale-specific formatting.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>Data Formatting:</b> Verifies formatting of WatcherData with various memory configurations (used, total, free)</li>
 *   <li><b>Locale Handling:</b> Ensures consistent formatting across different data scenarios</li>
 * </ul>
 */
@DisplayName("WatcherDataFormatter")
@ValidateCharset
@WithLocale("en")
class WatcherDataFormatterTest {

    private static Stream<Arguments> provideWatcherDataForReadableStringBuilder() {
        final String sessionUuid = UUID.randomUUID().toString();
        final WatcherData dataWithMemory = new WatcherData(null, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1024, 4096, 2048, 0.0);
        final WatcherData dataWithUsedMemoryOnly = new WatcherData(null, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1024, 0, 0, 0.0);
        final WatcherData dataWithTotalMemoryOnly = new WatcherData(null, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2048, 0.0);
        final WatcherData dataWithMaxMemoryOnly = new WatcherData(null, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4096, 0, 0.0);
        final WatcherData dataWithSystemLoad = new WatcherData(null, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.5);
        final WatcherData dataWithUuid = new WatcherData(sessionUuid, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0);
        final WatcherData dataWithAll = new WatcherData(sessionUuid, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1024, 4096, 2048, 0.5);
        final WatcherData emptyData = new WatcherData(null, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.0);

        return Stream.of(
                Arguments.of(dataWithMemory, "Memory: 1024B 2.0kB 4.1kB"),
                Arguments.of(dataWithUsedMemoryOnly, "Memory: 1024B 0B 0B"),
                Arguments.of(dataWithTotalMemoryOnly, "Memory: 0B 2.0kB 0B"),
                Arguments.of(dataWithMaxMemoryOnly, "Memory: 0B 0B 4.1kB"),
                Arguments.of(dataWithSystemLoad, "System load: 50%"),
                Arguments.of(dataWithUuid, String.format("UUID: %s", sessionUuid)),
                Arguments.of(dataWithAll, String.format("Memory: 1024B 2.0kB 4.1kB; System load: 50%%; UUID: %s", sessionUuid)),
                Arguments.of(emptyData, "")
        );
    }

    @ParameterizedTest
    @MethodSource("provideWatcherDataForReadableStringBuilder")
    @DisplayName("should format watcher data to readable string")
    void testReadableStringBuilder(final WatcherData data, final String expected) {
        // Given: WatcherData with various combinations of memory, system load, and UUID
        final StringBuilder sb = new StringBuilder(128);

        // When: readableStringBuilder is called
        WatcherDataFormatter.readableStringBuilder(data, sb);

        // Then: should produce the expected formatted string with proper locale-specific formatting
        assertEquals(expected, sb.toString(), "should format watcher data correctly");
    }
}
