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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.usefultoys.slf4j.SessionConfig;

import java.nio.charset.Charset;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;


class WatcherDataFormatterTest {

    @BeforeAll
    static void validateConsistentCharset() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    @BeforeAll
    public static void setUpLocale() {
        Locale.setDefault(Locale.ENGLISH);
    }

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
                Arguments.of(dataWithUuid, "UUID: " + sessionUuid),
                Arguments.of(dataWithAll, "Memory: 1024B 2.0kB 4.1kB; System load: 50%; UUID: " + sessionUuid),
                Arguments.of(emptyData, "")
        );
    }

    @ParameterizedTest
    @MethodSource("provideWatcherDataForReadableStringBuilder")
    void testReadableStringBuilder(WatcherData data, String expected) {
        final StringBuilder sb = new StringBuilder();
        WatcherDataFormatter.readableStringBuilder(data, sb);
        assertEquals(expected, sb.toString());
    }
}
