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
 * Unit tests for {@link StatusHighlightConverter}.
 * <p>
 * Tests validate that StatusHighlightConverter correctly maps log markers and log levels
 * to appropriate foreground color codes for terminal output highlighting.
 * <p>
 * Uses parameterized tests to efficiently verify all marker-to-color mappings.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>START_VISIBILITY (2):</b> MSG_START, MSG_PROGRESS</li>
 *   <li><b>WARN_VISIBILITY (2):</b> MSG_SLOW_PROGRESS, MSG_SLOW_OK</li>
 *   <li><b>SUCCESS_VISIBILITY (1):</b> MSG_OK</li>
 *   <li><b>REJECT_VISIBILITY (1):</b> MSG_REJECT</li>
 *   <li><b>ERROR_VISIBILITY (1):</b> MSG_FAIL</li>
 *   <li><b>LESS_VISIBILITY (8):</b> All DATA_* markers (7) + DATA_WATCHER</li>
 *   <li><b>INCONSISTENCY_VISIBILITY (5):</b> UNEXPECTED_EXCEPTION, INVALID_TRANSITION, INVALID_STATE, INVALID_EXCEPTION, INVALID_ARGUMENT</li>
 *   <li><b>WATCHER_VISIBILITY (1):</b> MSG_WATCHER</li>
 *   <li><b>Level-based fallback:</b> ERROR, WARN, INFO, DEBUG, TRACE, default</li>
 * </ul>
 *
 * @author Daniel Felix Ferber
 * @author Co-authored-by: GitHub Copilot using Claude Sonnet 4.5
 */
@DisplayName("StatusHighlightConverter")
@ValidateCharset
class StatusHighlightConverterTest {

    private StatusHighlightConverter converter;

    @Mock
    private ILoggingEvent mockEvent;
    @Mock
    private Marker mockMarker;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        converter = new StatusHighlightConverter();
    }

    @ParameterizedTest(name = "should return {1} for {0} marker")
    @MethodSource("markerToColorMappings")
    @DisplayName("Marker to color conversions")
    void testMarkerToColorConversions(final Marker marker, final String expectedColor) {
        // Given: logging event with marker
        when(mockEvent.getMarker()).thenReturn(marker);
        // When: getForegroundColorCode is called
        final String result = converter.getForegroundColorCode(mockEvent);
        // Then: should return expected color
        assertEquals(expectedColor, result);
    }

    static Stream<Arguments> markerToColorMappings() {
        return Stream.of(
                // Message lifecycle markers
                Arguments.of(named("MSG_START", Markers.MSG_START), StatusHighlightConverter.START_VISIBILITY),
                Arguments.of(named("MSG_PROGRESS", Markers.MSG_PROGRESS), StatusHighlightConverter.START_VISIBILITY),
                Arguments.of(named("MSG_SLOW_PROGRESS", Markers.MSG_SLOW_PROGRESS), StatusHighlightConverter.WARN_VISIBILITY),
                Arguments.of(named("MSG_OK", Markers.MSG_OK), StatusHighlightConverter.SUCCESS_VISIBILITY),
                Arguments.of(named("MSG_SLOW_OK", Markers.MSG_SLOW_OK), StatusHighlightConverter.WARN_VISIBILITY),
                Arguments.of(named("MSG_REJECT", Markers.MSG_REJECT), StatusHighlightConverter.REJECT_VISIBILITY),
                Arguments.of(named("MSG_FAIL", Markers.MSG_FAIL), StatusHighlightConverter.ERROR_VISIBILITY),
                // Data markers
                Arguments.of(named("DATA_START", Markers.DATA_START), StatusHighlightConverter.LESS_VISIBILITY),
                Arguments.of(named("DATA_PROGRESS", Markers.DATA_PROGRESS), StatusHighlightConverter.LESS_VISIBILITY),
                Arguments.of(named("DATA_SLOW_PROGRESS", Markers.DATA_SLOW_PROGRESS), StatusHighlightConverter.LESS_VISIBILITY),
                Arguments.of(named("DATA_OK", Markers.DATA_OK), StatusHighlightConverter.LESS_VISIBILITY),
                Arguments.of(named("DATA_SLOW_OK", Markers.DATA_SLOW_OK), StatusHighlightConverter.LESS_VISIBILITY),
                Arguments.of(named("DATA_REJECT", Markers.DATA_REJECT), StatusHighlightConverter.LESS_VISIBILITY),
                Arguments.of(named("DATA_FAIL", Markers.DATA_FAIL), StatusHighlightConverter.LESS_VISIBILITY),
                // Inconsistency/error markers
                Arguments.of(named("UNEXPECTED_EXCEPTION", Markers.UNEXPECTED_EXCEPTION), StatusHighlightConverter.INCONSISTENCY_VISIBILITY),
                Arguments.of(named("INVALID_TRANSITION", Markers.INVALID_TRANSITION), StatusHighlightConverter.INCONSISTENCY_VISIBILITY),
                Arguments.of(named("INVALID_STATE", Markers.INVALID_STATE), StatusHighlightConverter.INCONSISTENCY_VISIBILITY),
                Arguments.of(named("INVALID_EXCEPTION", Markers.INVALID_EXCEPTION), StatusHighlightConverter.INCONSISTENCY_VISIBILITY),
                Arguments.of(named("INVALID_ARGUMENT", Markers.INVALID_ARGUMENT), StatusHighlightConverter.INCONSISTENCY_VISIBILITY),
                // Watcher markers
                Arguments.of(named("MSG_WATCHER", org.usefultoys.slf4j.watcher.Markers.MSG_WATCHER), StatusHighlightConverter.WATCHER_VISIBILITY),
                Arguments.of(named("DATA_WATCHER", org.usefultoys.slf4j.watcher.Markers.DATA_WATCHER), StatusHighlightConverter.LESS_VISIBILITY)
        );
    }
    @Nested
    @DisplayName("Level-based fallback (no matching marker)")
    class LevelBasedFallbackTest {

        @Test
        @DisplayName("should return ERROR_VISIBILITY for null marker and ERROR level")
        void testNullMarkerAndErrorLevel() {
            // Given: logging event with null marker and ERROR level
            when(mockEvent.getMarker()).thenReturn(null);
            when(mockEvent.getLevel()).thenReturn(Level.ERROR);
            // When: getForegroundColorCode is called
            final String result = converter.getForegroundColorCode(mockEvent);
            // Then: should return ERROR_VISIBILITY code
            assertEquals(StatusHighlightConverter.ERROR_VISIBILITY, result);
        }

        @Test
        @DisplayName("should return WARN_VISIBILITY for null marker and WARN level")
        void testNullMarkerAndWarnLevel() {
            // Given: logging event with null marker and WARN level
            when(mockEvent.getMarker()).thenReturn(null);
            when(mockEvent.getLevel()).thenReturn(Level.WARN);
            // When: getForegroundColorCode is called
            final String result = converter.getForegroundColorCode(mockEvent);
            // Then: should return WARN_VISIBILITY code
            assertEquals(StatusHighlightConverter.WARN_VISIBILITY, result);
        }

        @Test
        @DisplayName("should return INFO_VISIBILITY for null marker and INFO level")
        void testNullMarkerAndInfoLevel() {
            // Given: logging event with null marker and INFO level
            when(mockEvent.getMarker()).thenReturn(null);
            when(mockEvent.getLevel()).thenReturn(Level.INFO);
            // When: getForegroundColorCode is called
            final String result = converter.getForegroundColorCode(mockEvent);
            // Then: should return INFO_VISIBILITY code
            assertEquals(StatusHighlightConverter.INFO_VISIBILITY, result);
        }

        @Test
        @DisplayName("should return DEBUG_VISIBILITY for null marker and DEBUG level")
        void testNullMarkerAndDebugLevel() {
            // Given: logging event with null marker and DEBUG level
            when(mockEvent.getMarker()).thenReturn(null);
            when(mockEvent.getLevel()).thenReturn(Level.DEBUG);
            // When: getForegroundColorCode is called
            final String result = converter.getForegroundColorCode(mockEvent);
            // Then: should return DEBUG_VISIBILITY code
            assertEquals(StatusHighlightConverter.DEBUG_VISIBILITY, result);
        }

        @Test
        @DisplayName("should return TRACE_VISIBILITY for null marker and TRACE level")
        void testNullMarkerAndTraceLevel() {
            // Given: logging event with null marker and TRACE level
            when(mockEvent.getMarker()).thenReturn(null);
            when(mockEvent.getLevel()).thenReturn(Level.TRACE);
            // When: getForegroundColorCode is called
            final String result = converter.getForegroundColorCode(mockEvent);
            // Then: should return TRACE_VISIBILITY code
            assertEquals(StatusHighlightConverter.TRACE_VISIBILITY, result);
        }

        @Test
        @DisplayName("should return DEFAULT_VISIBILITY for unknown marker and OFF level")
        void testUnknownMarkerAndDefaultLevel() {
            // Given: logging event with unknown marker and OFF level
            when(mockEvent.getMarker()).thenReturn(mockMarker);
            when(mockEvent.getLevel()).thenReturn(Level.OFF);
            // When: getForegroundColorCode is called
            final String result = converter.getForegroundColorCode(mockEvent);
            // Then: should return DEFAULT_VISIBILITY code
            assertEquals(StatusHighlightConverter.DEFAULT_VISIBILITY, result);
        }

        @Test
        @DisplayName("should return ERROR_VISIBILITY for unknown marker and ERROR level")
        void testUnknownMarkerAndErrorLevel() {
            // Given: logging event with unknown marker and ERROR level
            when(mockEvent.getMarker()).thenReturn(mockMarker);
            when(mockEvent.getLevel()).thenReturn(Level.ERROR);
            // When: getForegroundColorCode is called
            final String result = converter.getForegroundColorCode(mockEvent);
            // Then: should return ERROR_VISIBILITY code
            assertEquals(StatusHighlightConverter.ERROR_VISIBILITY, result);
        }
    }
}
