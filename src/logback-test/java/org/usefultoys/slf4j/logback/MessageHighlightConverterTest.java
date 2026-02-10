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

import ch.qos.logback.classic.spi.ILoggingEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
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
 * Unit tests for {@link MessageHighlightConverter}.
 * <p>
 * Tests validate that MessageHighlightConverter correctly maps log markers to appropriate
 * foreground color codes, supporting visibility levels for message highlighting.
 * <p>
 * Uses parameterized tests to efficiently verify all marker-to-color mappings.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>MORE_VISIBILITY Markers (8):</b> MSG_START, MSG_PROGRESS, MSG_SLOW_PROGRESS, MSG_OK, MSG_SLOW_OK, MSG_REJECT, MSG_FAIL, MSG_WATCHER</li>
 *   <li><b>LESS_VISIBILITY Markers (8):</b> DATA_START, DATA_PROGRESS, DATA_SLOW_PROGRESS, DATA_OK, DATA_SLOW_OK, DATA_REJECT, DATA_FAIL, DATA_WATCHER</li>
 *   <li><b>ERROR_VISIBILITY Markers (5):</b> UNEXPECTED_EXCEPTION, INVALID_TRANSITION, INVALID_STATE, INVALID_EXCEPTION, INVALID_ARGUMENT</li>
 *   <li><b>DEFAULT_VISIBILITY (2):</b> null marker, unknown marker</li>
 * </ul>
 */
@DisplayName("MessageHighlightConverter")
@ValidateCharset
class MessageHighlightConverterTest {

    private MessageHighlightConverter converter;

    @Mock
    private ILoggingEvent mockEvent;
    @Mock
    private Marker mockMarker;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        converter = new MessageHighlightConverter();
    }

    @ParameterizedTest(name = "should return MORE_VISIBILITY for {0} marker")
    @MethodSource("moreVisibilityMarkers")
    @DisplayName("MORE_VISIBILITY markers")
    void testMoreVisibilityMarkers(final Marker marker) {
        // Given: logging event with marker
        when(mockEvent.getMarker()).thenReturn(marker);
        // When: getForegroundColorCode is called
        final String result = converter.getForegroundColorCode(mockEvent);
        // Then: should return MORE_VISIBILITY code
        assertEquals(MessageHighlightConverter.MORE_VISIBILITY, result);
    }

    static Stream<Arguments> moreVisibilityMarkers() {
        return Stream.of(
                Arguments.of(named("MSG_START", Markers.MSG_START)),
                Arguments.of(named("MSG_PROGRESS", Markers.MSG_PROGRESS)),
                Arguments.of(named("MSG_SLOW_PROGRESS", Markers.MSG_SLOW_PROGRESS)),
                Arguments.of(named("MSG_OK", Markers.MSG_OK)),
                Arguments.of(named("MSG_SLOW_OK", Markers.MSG_SLOW_OK)),
                Arguments.of(named("MSG_REJECT", Markers.MSG_REJECT)),
                Arguments.of(named("MSG_FAIL", Markers.MSG_FAIL)),
                Arguments.of(named("MSG_WATCHER", org.usefultoys.slf4j.watcher.Markers.MSG_WATCHER))
        );
    }

    @ParameterizedTest(name = "should return LESS_VISIBILITY for {0} marker")
    @MethodSource("lessVisibilityMarkers")
    @DisplayName("LESS_VISIBILITY markers")
    void testLessVisibilityMarkers(final Marker marker) {
        // Given: logging event with marker
        when(mockEvent.getMarker()).thenReturn(marker);
        // When: getForegroundColorCode is called
        final String result = converter.getForegroundColorCode(mockEvent);
        // Then: should return LESS_VISIBILITY code
        assertEquals(MessageHighlightConverter.LESS_VISIBILITY, result);
    }

    static Stream<Arguments> lessVisibilityMarkers() {
        return Stream.of(
                Arguments.of(named("DATA_START", Markers.DATA_START)),
                Arguments.of(named("DATA_PROGRESS", Markers.DATA_PROGRESS)),
                Arguments.of(named("DATA_SLOW_PROGRESS", Markers.DATA_SLOW_PROGRESS)),
                Arguments.of(named("DATA_OK", Markers.DATA_OK)),
                Arguments.of(named("DATA_SLOW_OK", Markers.DATA_SLOW_OK)),
                Arguments.of(named("DATA_REJECT", Markers.DATA_REJECT)),
                Arguments.of(named("DATA_FAIL", Markers.DATA_FAIL)),
                Arguments.of(named("DATA_WATCHER", org.usefultoys.slf4j.watcher.Markers.DATA_WATCHER))
        );
    }

    @ParameterizedTest(name = "should return ERROR_VISIBILITY for {0} marker")
    @MethodSource("errorVisibilityMarkers")
    @DisplayName("ERROR_VISIBILITY markers")
    void testErrorVisibilityMarkers(final Marker marker) {
        // Given: logging event with marker
        when(mockEvent.getMarker()).thenReturn(marker);
        // When: getForegroundColorCode is called
        final String result = converter.getForegroundColorCode(mockEvent);
        // Then: should return ERROR_VISIBILITY code
        assertEquals(MessageHighlightConverter.ERROR_VISIBILITY, result);
    }

    static Stream<Arguments> errorVisibilityMarkers() {
        return Stream.of(
                Arguments.of(named("UNEXPECTED_EXCEPTION", Markers.UNEXPECTED_EXCEPTION)),
                Arguments.of(named("INVALID_TRANSITION", Markers.INVALID_TRANSITION)),
                Arguments.of(named("INVALID_STATE", Markers.INVALID_STATE)),
                Arguments.of(named("INVALID_EXCEPTION", Markers.INVALID_EXCEPTION)),
                Arguments.of(named("INVALID_ARGUMENT", Markers.INVALID_ARGUMENT))
        );
    }

    @Nested
    @DisplayName("DEFAULT_VISIBILITY (fallback)")
    class DefaultVisibilityTest {

        @Test
        @DisplayName("should return DEFAULT_VISIBILITY for null marker")
        void testNullMarker() {
            // Given: logging event with null marker
            when(mockEvent.getMarker()).thenReturn(null);
            // When: getForegroundColorCode is called
            final String result = converter.getForegroundColorCode(mockEvent);
            // Then: should return DEFAULT_VISIBILITY code
            assertEquals(MessageHighlightConverter.DEFAULT_VISIBILITY, result);
        }

        @Test
        @DisplayName("should return DEFAULT_VISIBILITY for unknown marker")
        void testUnknownMarker() {
            // Given: logging event with unknown marker not in any specific list
            when(mockEvent.getMarker()).thenReturn(mockMarker);
            // When: getForegroundColorCode is called
            final String result = converter.getForegroundColorCode(mockEvent);
            // Then: should return DEFAULT_VISIBILITY code
            assertEquals(MessageHighlightConverter.DEFAULT_VISIBILITY, result);
        }
    }
}
