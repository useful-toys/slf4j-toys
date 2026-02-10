/*
 * Copyright 2026 Daniel Felix Ferber
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
package org.usefultoys.slf4j.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Marker;
import org.usefultoys.slf4j.meter.Markers;
import org.usefultoys.test.ValidateCharset;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Named.named;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link StatusConverter}.
 * <p>
 * Tests validate that StatusConverter correctly converts log markers and log levels
 * to appropriate status strings for formatting log output.
 * <p>
 * Uses parameterized tests to efficiently verify all marker-to-status mappings.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>START Status (1):</b> MSG_START</li>
 *   <li><b>PROGR Status (1):</b> MSG_PROGRESS</li>
 *   <li><b>SLOW Status (2):</b> MSG_SLOW_PROGRESS, MSG_SLOW_OK</li>
 *   <li><b>OK Status (1):</b> MSG_OK</li>
 *   <li><b>REJECT Status (1):</b> MSG_REJECT</li>
 *   <li><b>FAIL Status (1):</b> MSG_FAIL</li>
 *   <li><b>Empty String (8):</b> All DATA_* markers (7) + DATA_WATCHER</li>
 *   <li><b>INCONSISTENT Status (3):</b> INVALID_TRANSITION, INVALID_STATE, INVALID_EXCEPTION</li>
 *   <li><b>UNEXPECTED_EXCEPTION Status (1):</b> UNEXPECTED_EXCEPTION</li>
 *   <li><b>INVALID_ARGUMENT Status (1):</b> INVALID_ARGUMENT</li>
 *   <li><b>WATCHER Status (1):</b> MSG_WATCHER</li>
 *   <li><b>Default Behavior (2):</b> null marker, unknown marker</li>
 * </ul>
 *
 * @author Daniel Felix Ferber
 * @author Co-authored-by: GitHub Copilot using Claude Sonnet 4.5
 */
@DisplayName("StatusConverter")
@ValidateCharset
class StatusConverterTest {

    private StatusConverter converter;

    @Mock
    private ILoggingEvent mockEvent;
    @Mock
    private Marker mockMarker;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        converter = new StatusConverter();
    }

    @ParameterizedTest(name = "should convert {0} marker to ''{1}''")
    @MethodSource("markerToStatusMappings")
    @DisplayName("Marker to status conversions")
    void testMarkerToStatusConversions(final Marker marker, final String expectedStatus) {
        // Given: logging event with marker
        when(mockEvent.getMarker()).thenReturn(marker);
        // When: convert is called
        final String result = converter.convert(mockEvent);
        // Then: should return expected status
        assertEquals(expectedStatus, result);
    }

    static Stream<Arguments> markerToStatusMappings() {
        return Stream.of(
                // Message lifecycle markers
                Arguments.of(named("MSG_START", Markers.MSG_START), "START"),
                Arguments.of(named("MSG_PROGRESS", Markers.MSG_PROGRESS), "PROGR"),
                Arguments.of(named("MSG_SLOW_PROGRESS", Markers.MSG_SLOW_PROGRESS), "SLOW"),
                Arguments.of(named("MSG_OK", Markers.MSG_OK), "OK"),
                Arguments.of(named("MSG_SLOW_OK", Markers.MSG_SLOW_OK), "SLOW"),
                Arguments.of(named("MSG_REJECT", Markers.MSG_REJECT), "REJECT"),
                Arguments.of(named("MSG_FAIL", Markers.MSG_FAIL), "FAIL"),
                // Data markers (all return empty string)
                Arguments.of(named("DATA_START", Markers.DATA_START), ""),
                Arguments.of(named("DATA_PROGRESS", Markers.DATA_PROGRESS), ""),
                Arguments.of(named("DATA_SLOW_PROGRESS", Markers.DATA_SLOW_PROGRESS), ""),
                Arguments.of(named("DATA_OK", Markers.DATA_OK), ""),
                Arguments.of(named("DATA_SLOW_OK", Markers.DATA_SLOW_OK), ""),
                Arguments.of(named("DATA_REJECT", Markers.DATA_REJECT), ""),
                Arguments.of(named("DATA_FAIL", Markers.DATA_FAIL), ""),
                // Error/validation markers
                Arguments.of(named("INVALID_TRANSITION", Markers.INVALID_TRANSITION), "INCONSISTENT"),
                Arguments.of(named("INVALID_STATE", Markers.INVALID_STATE), "INCONSISTENT"),
                Arguments.of(named("INVALID_EXCEPTION", Markers.INVALID_EXCEPTION), "INCONSISTENT"),
                Arguments.of(named("INVALID_ARGUMENT", Markers.INVALID_ARGUMENT), "INVALID_ARGUMENT"),
                Arguments.of(named("UNEXPECTED_EXCEPTION", Markers.UNEXPECTED_EXCEPTION), "UNEXPECTED_EXCEPTION"),
                // Watcher markers
                Arguments.of(named("MSG_WATCHER", org.usefultoys.slf4j.watcher.Markers.MSG_WATCHER), "WATCHER"),
                Arguments.of(named("DATA_WATCHER", org.usefultoys.slf4j.watcher.Markers.DATA_WATCHER), "")
        );
    }

    @Nested
    @DisplayName("Default behavior (no matching marker)")
    class DefaultBehaviorTest {

        @Test
        @DisplayName("should return level string for unknown marker and INFO level")
        void testUnknownMarkerAndInfoLevel() {
            // Given: logging event with unknown marker and INFO level
            when(mockEvent.getMarker()).thenReturn(mockMarker);
            when(mockEvent.getLevel()).thenReturn(Level.INFO);
            // When: convert is called
            final String result = converter.convert(mockEvent);
            // Then: should return level name
            assertEquals("INFO", result);
        }

        @Test
        @DisplayName("should return level string for null marker and DEBUG level")
        void testNullMarkerAndDebugLevel() {
            // Given: logging event with null marker and DEBUG level
            when(mockEvent.getMarker()).thenReturn(null);
            when(mockEvent.getLevel()).thenReturn(Level.DEBUG);
            // When: convert is called
            final String result = converter.convert(mockEvent);
            // Then: should return level name
            assertEquals("DEBUG", result);
        }

        @Test
        @DisplayName("should return level string for null marker and ERROR level")
        void testNullMarkerAndErrorLevel() {
            // Given: logging event with null marker and ERROR level
            when(mockEvent.getMarker()).thenReturn(null);
            when(mockEvent.getLevel()).thenReturn(Level.ERROR);
            // When: convert is called
            final String result = converter.convert(mockEvent);
            // Then: should return level name
            assertEquals("ERROR", result);
        }
    }
}
