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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Marker;
import org.usefultoys.slf4j.meter.Markers;
import org.usefultoys.test.ValidateCharset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link StatusHighlightConverter}.
 * <p>
 * Tests validate that StatusHighlightConverter correctly maps log markers and log levels
 * to appropriate foreground color codes for terminal output highlighting.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>Message Markers:</b> Tests color mapping for message-related markers</li>
 *   <li><b>Data Markers:</b> Verifies color mapping for data markers (LESS_VISIBILITY)</li>
 *   <li><b>Inconsistency Markers:</b> Tests color mapping for inconsistency-related markers</li>
 *   <li><b>Watcher Marker:</b> Covers color mapping for watcher-specific markers</li>
 *   <li><b>Default Behavior:</b> Ensures fallback color mapping for unmapped cases</li>
 * </ul>
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

    @Nested
    @DisplayName("Message markers")
    class MessageMarkersTest {

        @Test
        @DisplayName("should return START_VISIBILITY for MSG_START marker")
        void testMsgStartMarker() {
            // Given: logging event with MSG_START marker
            when(mockEvent.getMarker()).thenReturn(Markers.MSG_START);
            // When: getForegroundColorCode is called
            final String result = converter.getForegroundColorCode(mockEvent);
            // Then: should return START_VISIBILITY code
            assertEquals(StatusHighlightConverter.START_VISIBILITY, result);
        }

        @Test
        @DisplayName("should return START_VISIBILITY for MSG_PROGRESS marker")
        void testMsgProgressMarker() {
            // Given: logging event with MSG_PROGRESS marker
            when(mockEvent.getMarker()).thenReturn(Markers.MSG_PROGRESS);
            // When: getForegroundColorCode is called
            final String result = converter.getForegroundColorCode(mockEvent);
            // Then: should return START_VISIBILITY code
            assertEquals(StatusHighlightConverter.START_VISIBILITY, result);
        }

        @Test
        @DisplayName("should return SUCCESS_VISIBILITY for MSG_OK marker")
        void testMsgOkMarker() {
            // Given: logging event with MSG_OK marker
            when(mockEvent.getMarker()).thenReturn(Markers.MSG_OK);
            // When: getForegroundColorCode is called
            final String result = converter.getForegroundColorCode(mockEvent);
            // Then: should return SUCCESS_VISIBILITY code
            assertEquals(StatusHighlightConverter.SUCCESS_VISIBILITY, result);
        }

        @Test
        @DisplayName("should return WARN_VISIBILITY for MSG_SLOW_OK marker")
        void testMsgSlowOkMarker() {
            // Given: logging event with MSG_SLOW_OK marker
            when(mockEvent.getMarker()).thenReturn(Markers.MSG_SLOW_OK);
            // When: getForegroundColorCode is called
            final String result = converter.getForegroundColorCode(mockEvent);
            // Then: should return WARN_VISIBILITY code
            assertEquals(StatusHighlightConverter.WARN_VISIBILITY, result);
        }

        @Test
        @DisplayName("should return REJECT_VISIBILITY for MSG_REJECT marker")
        void testMsgRejectMarker() {
            // Given: logging event with MSG_REJECT marker
            when(mockEvent.getMarker()).thenReturn(Markers.MSG_REJECT);
            // When: getForegroundColorCode is called
            final String result = converter.getForegroundColorCode(mockEvent);
            // Then: should return REJECT_VISIBILITY code
            assertEquals(StatusHighlightConverter.REJECT_VISIBILITY, result);
        }

        @Test
        @DisplayName("should return ERROR_VISIBILITY for MSG_FAIL marker")
        void testMsgFailMarker() {
            // Given: logging event with MSG_FAIL marker
            when(mockEvent.getMarker()).thenReturn(Markers.MSG_FAIL);
            // When: getForegroundColorCode is called
            final String result = converter.getForegroundColorCode(mockEvent);
            // Then: should return ERROR_VISIBILITY code
            assertEquals(StatusHighlightConverter.ERROR_VISIBILITY, result);
        }
    }

    @Nested
    @DisplayName("Data markers (LESS_VISIBILITY)")
    class DataMarkersTest {

        @Test
        @DisplayName("should return LESS_VISIBILITY for DATA_START marker")
        void testDataStartMarker() {
            // Given: logging event with DATA_START marker
            when(mockEvent.getMarker()).thenReturn(Markers.DATA_START);
            // When: getForegroundColorCode is called
            final String result = converter.getForegroundColorCode(mockEvent);
            // Then: should return LESS_VISIBILITY code
            assertEquals(StatusHighlightConverter.LESS_VISIBILITY, result);
        }

        @Test
        @DisplayName("should return LESS_VISIBILITY for DATA_PROGRESS marker")
        void testDataProgressMarker() {
            // Given: logging event with DATA_PROGRESS marker
            when(mockEvent.getMarker()).thenReturn(Markers.DATA_PROGRESS);
            // When: getForegroundColorCode is called
            final String result = converter.getForegroundColorCode(mockEvent);
            // Then: should return LESS_VISIBILITY code
            assertEquals(StatusHighlightConverter.LESS_VISIBILITY, result);
        }

        @Test
        @DisplayName("should return LESS_VISIBILITY for DATA_OK marker")
        void testDataOkMarker() {
            // Given: logging event with DATA_OK marker
            when(mockEvent.getMarker()).thenReturn(Markers.DATA_OK);
            // When: getForegroundColorCode is called
            final String result = converter.getForegroundColorCode(mockEvent);
            // Then: should return LESS_VISIBILITY code
            assertEquals(StatusHighlightConverter.LESS_VISIBILITY, result);
        }

        @Test
        @DisplayName("should return LESS_VISIBILITY for DATA_SLOW_OK marker")
        void testDataSlowOkMarker() {
            // Given: logging event with DATA_SLOW_OK marker
            when(mockEvent.getMarker()).thenReturn(Markers.DATA_SLOW_OK);
            // When: getForegroundColorCode is called
            final String result = converter.getForegroundColorCode(mockEvent);
            // Then: should return LESS_VISIBILITY code
            assertEquals(StatusHighlightConverter.LESS_VISIBILITY, result);
        }

        @Test
        @DisplayName("should return LESS_VISIBILITY for DATA_REJECT marker")
        void testDataRejectMarker() {
            // Given: logging event with DATA_REJECT marker
            when(mockEvent.getMarker()).thenReturn(Markers.DATA_REJECT);
            // When: getForegroundColorCode is called
            final String result = converter.getForegroundColorCode(mockEvent);
            // Then: should return LESS_VISIBILITY code
            assertEquals(StatusHighlightConverter.LESS_VISIBILITY, result);
        }

        @Test
        @DisplayName("should return LESS_VISIBILITY for DATA_FAIL marker")
        void testDataFailMarker() {
            // Given: logging event with DATA_FAIL marker
            when(mockEvent.getMarker()).thenReturn(Markers.DATA_FAIL);
            // When: getForegroundColorCode is called
            final String result = converter.getForegroundColorCode(mockEvent);
            // Then: should return LESS_VISIBILITY code
            assertEquals(StatusHighlightConverter.LESS_VISIBILITY, result);
        }

        @Test
        @DisplayName("should return LESS_VISIBILITY for DATA_WATCHER marker")
        void testWatcherDataMarker() {
            // Given: logging event with DATA_WATCHER marker
            when(mockEvent.getMarker()).thenReturn(org.usefultoys.slf4j.watcher.Markers.DATA_WATCHER);
            // When: getForegroundColorCode is called
            final String result = converter.getForegroundColorCode(mockEvent);
            // Then: should return LESS_VISIBILITY code
            assertEquals(StatusHighlightConverter.LESS_VISIBILITY, result);
        }
    }

    @Nested
    @DisplayName("Inconsistency markers")
    class InconsistencyMarkersTest {

        @Test
        @DisplayName("should return INCONSISTENCY_VISIBILITY for BUG marker")
        void testBugMarker() {
            // Given: logging event with BUG marker
            when(mockEvent.getMarker()).thenReturn(Markers.BUG);
            // When: getForegroundColorCode is called
            final String result = converter.getForegroundColorCode(mockEvent);
            // Then: should return INCONSISTENCY_VISIBILITY code
            assertEquals(StatusHighlightConverter.INCONSISTENCY_VISIBILITY, result);
        }

        @Test
        @DisplayName("should return INCONSISTENCY_VISIBILITY for ILLEGAL marker")
        void testIllegalMarker() {
            // Given: logging event with ILLEGAL marker
            when(mockEvent.getMarker()).thenReturn(Markers.ILLEGAL);
            // When: getForegroundColorCode is called
            final String result = converter.getForegroundColorCode(mockEvent);
            // Then: should return INCONSISTENCY_VISIBILITY code
            assertEquals(StatusHighlightConverter.INCONSISTENCY_VISIBILITY, result);
        }

        @Test
        @DisplayName("should return INCONSISTENCY_VISIBILITY for INCONSISTENT_START marker")
        void testInconsistentStartMarker() {
            // Given: logging event with INCONSISTENT_START marker
            when(mockEvent.getMarker()).thenReturn(Markers.INCONSISTENT_START);
            // When: getForegroundColorCode is called
            final String result = converter.getForegroundColorCode(mockEvent);
            // Then: should return INCONSISTENCY_VISIBILITY code
            assertEquals(StatusHighlightConverter.INCONSISTENCY_VISIBILITY, result);
        }

        @Test
        @DisplayName("should return INCONSISTENCY_VISIBILITY for INCONSISTENT_INCREMENT marker")
        void testInconsistentIncrementMarker() {
            // Given: logging event with INCONSISTENT_INCREMENT marker
            when(mockEvent.getMarker()).thenReturn(Markers.INCONSISTENT_INCREMENT);
            // When: getForegroundColorCode is called
            final String result = converter.getForegroundColorCode(mockEvent);
            // Then: should return INCONSISTENCY_VISIBILITY code
            assertEquals(StatusHighlightConverter.INCONSISTENCY_VISIBILITY, result);
        }

        @Test
        @DisplayName("should return INCONSISTENCY_VISIBILITY for INCONSISTENT_PROGRESS marker")
        void testInconsistentProgressMarker() {
            // Given: logging event with INCONSISTENT_PROGRESS marker
            when(mockEvent.getMarker()).thenReturn(Markers.INCONSISTENT_PROGRESS);
            // When: getForegroundColorCode is called
            final String result = converter.getForegroundColorCode(mockEvent);
            // Then: should return INCONSISTENCY_VISIBILITY code
            assertEquals(StatusHighlightConverter.INCONSISTENCY_VISIBILITY, result);
        }

        @Test
        @DisplayName("should return INCONSISTENCY_VISIBILITY for INCONSISTENT_EXCEPTION marker")
        void testInconsistentExceptionMarker() {
            // Given: logging event with INCONSISTENT_EXCEPTION marker
            when(mockEvent.getMarker()).thenReturn(Markers.INCONSISTENT_EXCEPTION);
            // When: getForegroundColorCode is called
            final String result = converter.getForegroundColorCode(mockEvent);
            // Then: should return INCONSISTENCY_VISIBILITY code
            assertEquals(StatusHighlightConverter.INCONSISTENCY_VISIBILITY, result);
        }

        @Test
        @DisplayName("should return INCONSISTENCY_VISIBILITY for INCONSISTENT_REJECT marker")
        void testInconsistentRejectMarker() {
            // Given: logging event with INCONSISTENT_REJECT marker
            when(mockEvent.getMarker()).thenReturn(Markers.INCONSISTENT_REJECT);
            // When: getForegroundColorCode is called
            final String result = converter.getForegroundColorCode(mockEvent);
            // Then: should return INCONSISTENCY_VISIBILITY code
            assertEquals(StatusHighlightConverter.INCONSISTENCY_VISIBILITY, result);
        }

        @Test
        @DisplayName("should return INCONSISTENCY_VISIBILITY for INCONSISTENT_OK marker")
        void testInconsistentOkMarker() {
            // Given: logging event with INCONSISTENT_OK marker
            when(mockEvent.getMarker()).thenReturn(Markers.INCONSISTENT_OK);
            // When: getForegroundColorCode is called
            final String result = converter.getForegroundColorCode(mockEvent);
            // Then: should return INCONSISTENCY_VISIBILITY code
            assertEquals(StatusHighlightConverter.INCONSISTENCY_VISIBILITY, result);
        }

        @Test
        @DisplayName("should return INCONSISTENCY_VISIBILITY for INCONSISTENT_FAIL marker")
        void testInconsistentFailMarker() {
            // Given: logging event with INCONSISTENT_FAIL marker
            when(mockEvent.getMarker()).thenReturn(Markers.INCONSISTENT_FAIL);
            // When: getForegroundColorCode is called
            final String result = converter.getForegroundColorCode(mockEvent);
            // Then: should return INCONSISTENCY_VISIBILITY code
            assertEquals(StatusHighlightConverter.INCONSISTENCY_VISIBILITY, result);
        }

        @Test
        @DisplayName("should return INCONSISTENCY_VISIBILITY for INCONSISTENT_FINALIZED marker")
        void testInconsistentFinalizedMarker() {
            // Given: logging event with INCONSISTENT_FINALIZED marker
            when(mockEvent.getMarker()).thenReturn(Markers.INCONSISTENT_FINALIZED);
            // When: getForegroundColorCode is called
            final String result = converter.getForegroundColorCode(mockEvent);
            // Then: should return INCONSISTENCY_VISIBILITY code
            assertEquals(StatusHighlightConverter.INCONSISTENCY_VISIBILITY, result);
        }
    }

    @Nested
    @DisplayName("Watcher marker")
    class WatcherMarkerTest {

        @Test
        @DisplayName("should return WATCHER_VISIBILITY for MSG_WATCHER marker")
        void testWatcherMsgMarker() {
            // Given: logging event with MSG_WATCHER marker
            when(mockEvent.getMarker()).thenReturn(org.usefultoys.slf4j.watcher.Markers.MSG_WATCHER);
            // When: getForegroundColorCode is called
            final String result = converter.getForegroundColorCode(mockEvent);
            // Then: should return WATCHER_VISIBILITY code
            assertEquals(StatusHighlightConverter.WATCHER_VISIBILITY, result);
        }
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
