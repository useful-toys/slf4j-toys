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
 * Unit tests for {@link StatusConverter}.
 * <p>
 * Tests validate that StatusConverter correctly converts log markers and log levels
 * to appropriate status strings for formatting log output.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>Message Markers:</b> Tests conversion of message-related markers to status strings</li>
 *   <li><b>Data Markers:</b> Verifies conversion of data markers (returning empty string)</li>
 *   <li><b>Error Markers:</b> Tests conversion of error-related markers to status strings</li>
 *   <li><b>Watcher Marker:</b> Covers conversion of watcher-specific markers</li>
 *   <li><b>Default Behavior:</b> Ensures fallback handling for unmapped markers</li>
 * </ul>
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

    @Nested
    @DisplayName("Message markers")
    class MessageMarkersTest {

        @Test
        @DisplayName("should convert MSG_START marker to START")
        void testMsgStartMarker() {
            // Given: logging event with MSG_START marker
            when(mockEvent.getMarker()).thenReturn(Markers.MSG_START);
            // When: convert is called
            final String result = converter.convert(mockEvent);
            // Then: should return START status
            assertEquals("START", result);
        }

        @Test
        @DisplayName("should convert MSG_PROGRESS marker to PROGR")
        void testMsgProgressMarker() {
            // Given: logging event with MSG_PROGRESS marker
            when(mockEvent.getMarker()).thenReturn(Markers.MSG_PROGRESS);
            // When: convert is called
            final String result = converter.convert(mockEvent);
            // Then: should return PROGR status
            assertEquals("PROGR", result);
        }

        @Test
        @DisplayName("should convert MSG_OK marker to OK")
        void testMsgOkMarker() {
            // Given: logging event with MSG_OK marker
            when(mockEvent.getMarker()).thenReturn(Markers.MSG_OK);
            // When: convert is called
            final String result = converter.convert(mockEvent);
            // Then: should return OK status
            assertEquals("OK", result);
        }

        @Test
        @DisplayName("should convert MSG_SLOW_OK marker to SLOW")
        void testMsgSlowOkMarker() {
            // Given: logging event with MSG_SLOW_OK marker
            when(mockEvent.getMarker()).thenReturn(Markers.MSG_SLOW_OK);
            // When: convert is called
            final String result = converter.convert(mockEvent);
            // Then: should return SLOW status
            assertEquals("SLOW", result);
        }

        @Test
        @DisplayName("should convert MSG_REJECT marker to REJECT")
        void testMsgRejectMarker() {
            // Given: logging event with MSG_REJECT marker
            when(mockEvent.getMarker()).thenReturn(Markers.MSG_REJECT);
            // When: convert is called
            final String result = converter.convert(mockEvent);
            // Then: should return REJECT status
            assertEquals("REJECT", result);
        }

        @Test
        @DisplayName("should convert MSG_FAIL marker to FAIL")
        void testMsgFailMarker() {
            // Given: logging event with MSG_FAIL marker
            when(mockEvent.getMarker()).thenReturn(Markers.MSG_FAIL);
            // When: convert is called
            final String result = converter.convert(mockEvent);
            // Then: should return FAIL status
            assertEquals("FAIL", result);
        }
    }

    @Nested
    @DisplayName("Data markers (empty string)")
    class DataMarkersTest {

        @Test
        @DisplayName("should convert DATA_START marker to empty string")
        void testDataStartMarker() {
            // Given: logging event with DATA_START marker
            when(mockEvent.getMarker()).thenReturn(Markers.DATA_START);
            // When: convert is called
            final String result = converter.convert(mockEvent);
            // Then: should return empty string
            assertEquals("", result);
        }

        @Test
        @DisplayName("should convert DATA_PROGRESS marker to empty string")
        void testDataProgressMarker() {
            // Given: logging event with DATA_PROGRESS marker
            when(mockEvent.getMarker()).thenReturn(Markers.DATA_PROGRESS);
            // When: convert is called
            final String result = converter.convert(mockEvent);
            // Then: should return empty string
            assertEquals("", result);
        }

        @Test
        @DisplayName("should convert DATA_OK marker to empty string")
        void testDataOkMarker() {
            // Given: logging event with DATA_OK marker
            when(mockEvent.getMarker()).thenReturn(Markers.DATA_OK);
            // When: convert is called
            final String result = converter.convert(mockEvent);
            // Then: should return empty string
            assertEquals("", result);
        }

        @Test
        @DisplayName("should convert DATA_SLOW_OK marker to empty string")
        void testDataSlowOkMarker() {
            // Given: logging event with DATA_SLOW_OK marker
            when(mockEvent.getMarker()).thenReturn(Markers.DATA_SLOW_OK);
            // When: convert is called
            final String result = converter.convert(mockEvent);
            // Then: should return empty string
            assertEquals("", result);
        }

        @Test
        @DisplayName("should convert DATA_REJECT marker to empty string")
        void testDataRejectMarker() {
            // Given: logging event with DATA_REJECT marker
            when(mockEvent.getMarker()).thenReturn(Markers.DATA_REJECT);
            // When: convert is called
            final String result = converter.convert(mockEvent);
            // Then: should return empty string
            assertEquals("", result);
        }

        @Test
        @DisplayName("should convert DATA_FAIL marker to empty string")
        void testDataFailMarker() {
            // Given: logging event with DATA_FAIL marker
            when(mockEvent.getMarker()).thenReturn(Markers.DATA_FAIL);
            // When: convert is called
            final String result = converter.convert(mockEvent);
            // Then: should return empty string
            assertEquals("", result);
        }

        @Test
        @DisplayName("should convert DATA_WATCHER marker to empty string")
        void testWatcherDataMarker() {
            // Given: logging event with DATA_WATCHER marker
            when(mockEvent.getMarker()).thenReturn(org.usefultoys.slf4j.watcher.Markers.DATA_WATCHER);
            // When: convert is called
            final String result = converter.convert(mockEvent);
            // Then: should return empty string
            assertEquals("", result);
        }
    }

    @Nested
    @DisplayName("Error markers")
    class ErrorMarkersTest {

        @Test
        @DisplayName("should convert BUG marker to BUG")
        void testBugMarker() {
            // Given: logging event with BUG marker
            when(mockEvent.getMarker()).thenReturn(Markers.BUG);
            // When: convert is called
            final String result = converter.convert(mockEvent);
            // Then: should return BUG status
            assertEquals("BUG", result);
        }

        @Test
        @DisplayName("should convert ILLEGAL marker to ILLEGAL")
        void testIllegalMarker() {
            // Given: logging event with ILLEGAL marker
            when(mockEvent.getMarker()).thenReturn(Markers.ILLEGAL);
            // When: convert is called
            final String result = converter.convert(mockEvent);
            // Then: should return ILLEGAL status
            assertEquals("ILLEGAL", result);
        }

        @Test
        @DisplayName("should convert INCONSISTENT_START marker to INCONSISTENT")
        void testInconsistentStartMarker() {
            // Given: logging event with INCONSISTENT_START marker
            when(mockEvent.getMarker()).thenReturn(Markers.INCONSISTENT_START);
            // When: convert is called
            final String result = converter.convert(mockEvent);
            // Then: should return INCONSISTENT status
            assertEquals("INCONSISTENT", result);
        }

        @Test
        @DisplayName("should convert INCONSISTENT_INCREMENT marker to INCONSISTENT")
        void testInconsistentIncrementMarker() {
            // Given: logging event with INCONSISTENT_INCREMENT marker
            when(mockEvent.getMarker()).thenReturn(Markers.INCONSISTENT_INCREMENT);
            // When: convert is called
            final String result = converter.convert(mockEvent);
            // Then: should return INCONSISTENT status
            assertEquals("INCONSISTENT", result);
        }

        @Test
        @DisplayName("should convert INCONSISTENT_PROGRESS marker to INCONSISTENT")
        void testInconsistentProgressMarker() {
            // Given: logging event with INCONSISTENT_PROGRESS marker
            when(mockEvent.getMarker()).thenReturn(Markers.INCONSISTENT_PROGRESS);
            // When: convert is called
            final String result = converter.convert(mockEvent);
            // Then: should return INCONSISTENT status
            assertEquals("INCONSISTENT", result);
        }

        @Test
        @DisplayName("should convert INCONSISTENT_EXCEPTION marker to INCONSISTENT")
        void testInconsistentExceptionMarker() {
            // Given: logging event with INCONSISTENT_EXCEPTION marker
            when(mockEvent.getMarker()).thenReturn(Markers.INCONSISTENT_EXCEPTION);
            // When: convert is called
            final String result = converter.convert(mockEvent);
            // Then: should return INCONSISTENT status
            assertEquals("INCONSISTENT", result);
        }

        @Test
        @DisplayName("should convert INCONSISTENT_REJECT marker to INCONSISTENT")
        void testInconsistentRejectMarker() {
            // Given: logging event with INCONSISTENT_REJECT marker
            when(mockEvent.getMarker()).thenReturn(Markers.INCONSISTENT_REJECT);
            // When: convert is called
            final String result = converter.convert(mockEvent);
            // Then: should return INCONSISTENT status
            assertEquals("INCONSISTENT", result);
        }

        @Test
        @DisplayName("should convert INCONSISTENT_OK marker to INCONSISTENT")
        void testInconsistentOkMarker() {
            // Given: logging event with INCONSISTENT_OK marker
            when(mockEvent.getMarker()).thenReturn(Markers.INCONSISTENT_OK);
            // When: convert is called
            final String result = converter.convert(mockEvent);
            // Then: should return INCONSISTENT status
            assertEquals("INCONSISTENT", result);
        }

        @Test
        @DisplayName("should convert INCONSISTENT_FAIL marker to INCONSISTENT")
        void testInconsistentFailMarker() {
            // Given: logging event with INCONSISTENT_FAIL marker
            when(mockEvent.getMarker()).thenReturn(Markers.INCONSISTENT_FAIL);
            // When: convert is called
            final String result = converter.convert(mockEvent);
            // Then: should return INCONSISTENT status
            assertEquals("INCONSISTENT", result);
        }

        @Test
        @DisplayName("should convert INCONSISTENT_FINALIZED marker to INCONSISTENT")
        void testInconsistentFinalizedMarker() {
            // Given: logging event with INCONSISTENT_FINALIZED marker
            when(mockEvent.getMarker()).thenReturn(Markers.INCONSISTENT_FINALIZED);
            // When: convert is called
            final String result = converter.convert(mockEvent);
            // Then: should return INCONSISTENT status
            assertEquals("INCONSISTENT", result);
        }
    }

    @Nested
    @DisplayName("Watcher marker")
    class WatcherMarkerTest {

        @Test
        @DisplayName("should convert MSG_WATCHER marker to WATCHER")
        void testWatcherMsgMarker() {
            // Given: logging event with MSG_WATCHER marker
            when(mockEvent.getMarker()).thenReturn(org.usefultoys.slf4j.watcher.Markers.MSG_WATCHER);
            // When: convert is called
            final String result = converter.convert(mockEvent);
            // Then: should return WATCHER status
            assertEquals("WATCHER", result);
        }
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
