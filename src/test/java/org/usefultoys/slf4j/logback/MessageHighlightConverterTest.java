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
 * Unit tests for {@link MessageHighlightConverter}.
 * <p>
 * Tests validate that MessageHighlightConverter correctly maps log markers to appropriate
 * foreground color codes, supporting visibility levels for message highlighting.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>MORE_VISIBILITY Markers:</b> Tests color mapping for markers requiring high visibility</li>
 *   <li><b>LESS_VISIBILITY Markers:</b> Tests color mapping for markers requiring low visibility</li>
 *   <li><b>ERROR_VISIBILITY Markers:</b> Tests color mapping for error-related markers</li>
 *   <li><b>DEFAULT_VISIBILITY:</b> Verifies fallback color mapping for unmapped markers</li>
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

    @Nested
    @DisplayName("MORE_VISIBILITY markers")
    class MoreVisibilityMarkersTest {

        @Test
        @DisplayName("should return MORE_VISIBILITY for MSG_START marker")
        void testMsgStartMarker() {
            // Given: logging event with MSG_START marker
            when(mockEvent.getMarker()).thenReturn(Markers.MSG_START);
            // When: getForegroundColorCode is called
            final String result = converter.getForegroundColorCode(mockEvent);
            // Then: should return MORE_VISIBILITY code
            assertEquals(MessageHighlightConverter.MORE_VISIBILITY, result);
        }

        @Test
        @DisplayName("should return MORE_VISIBILITY for MSG_PROGRESS marker")
        void testMsgProgressMarker() {
            // Given: logging event with MSG_PROGRESS marker
            when(mockEvent.getMarker()).thenReturn(Markers.MSG_PROGRESS);
            // When: getForegroundColorCode is called
            final String result = converter.getForegroundColorCode(mockEvent);
            // Then: should return MORE_VISIBILITY code
            assertEquals(MessageHighlightConverter.MORE_VISIBILITY, result);
        }

        @Test
        @DisplayName("should return MORE_VISIBILITY for MSG_OK marker")
        void testMsgOkMarker() {
            // Given: logging event with MSG_OK marker
            when(mockEvent.getMarker()).thenReturn(Markers.MSG_OK);
            // When: getForegroundColorCode is called
            final String result = converter.getForegroundColorCode(mockEvent);
            // Then: should return MORE_VISIBILITY code
            assertEquals(MessageHighlightConverter.MORE_VISIBILITY, result);
        }

        @Test
        @DisplayName("should return MORE_VISIBILITY for MSG_SLOW_OK marker")
        void testMsgSlowOkMarker() {
            // Given: logging event with MSG_SLOW_OK marker
            when(mockEvent.getMarker()).thenReturn(Markers.MSG_SLOW_OK);
            // When: getForegroundColorCode is called
            final String result = converter.getForegroundColorCode(mockEvent);
            // Then: should return MORE_VISIBILITY code
            assertEquals(MessageHighlightConverter.MORE_VISIBILITY, result);
        }

        @Test
        @DisplayName("should return MORE_VISIBILITY for MSG_REJECT marker")
        void testMsgRejectMarker() {
            // Given: logging event with MSG_REJECT marker
            when(mockEvent.getMarker()).thenReturn(Markers.MSG_REJECT);
            // When: getForegroundColorCode is called
            final String result = converter.getForegroundColorCode(mockEvent);
            // Then: should return MORE_VISIBILITY code
            assertEquals(MessageHighlightConverter.MORE_VISIBILITY, result);
        }

        @Test
        @DisplayName("should return MORE_VISIBILITY for MSG_FAIL marker")
        void testMsgFailMarker() {
            // Given: logging event with MSG_FAIL marker
            when(mockEvent.getMarker()).thenReturn(Markers.MSG_FAIL);
            // When: getForegroundColorCode is called
            final String result = converter.getForegroundColorCode(mockEvent);
            // Then: should return MORE_VISIBILITY code
            assertEquals(MessageHighlightConverter.MORE_VISIBILITY, result);
        }
    }

    @Nested
    @DisplayName("LESS_VISIBILITY markers")
    class LessVisibilityMarkersTest {

        @Test
        @DisplayName("should return LESS_VISIBILITY for DATA_START marker")
        void testDataStartMarker() {
            // Given: logging event with DATA_START marker
            when(mockEvent.getMarker()).thenReturn(Markers.DATA_START);
            // When: getForegroundColorCode is called
            final String result = converter.getForegroundColorCode(mockEvent);
            // Then: should return LESS_VISIBILITY code
            assertEquals(MessageHighlightConverter.LESS_VISIBILITY, result);
        }

        @Test
        @DisplayName("should return LESS_VISIBILITY for DATA_PROGRESS marker")
        void testDataProgressMarker() {
            // Given: logging event with DATA_PROGRESS marker
            when(mockEvent.getMarker()).thenReturn(Markers.DATA_PROGRESS);
            // When: getForegroundColorCode is called
            final String result = converter.getForegroundColorCode(mockEvent);
            // Then: should return LESS_VISIBILITY code
            assertEquals(MessageHighlightConverter.LESS_VISIBILITY, result);
        }

        @Test
        @DisplayName("should return LESS_VISIBILITY for DATA_OK marker")
        void testDataOkMarker() {
            // Given: logging event with DATA_OK marker
            when(mockEvent.getMarker()).thenReturn(Markers.DATA_OK);
            // When: getForegroundColorCode is called
            final String result = converter.getForegroundColorCode(mockEvent);
            // Then: should return LESS_VISIBILITY code
            assertEquals(MessageHighlightConverter.LESS_VISIBILITY, result);
        }

        @Test
        @DisplayName("should return LESS_VISIBILITY for DATA_SLOW_OK marker")
        void testDataSlowOkMarker() {
            // Given: logging event with DATA_SLOW_OK marker
            when(mockEvent.getMarker()).thenReturn(Markers.DATA_SLOW_OK);
            // When: getForegroundColorCode is called
            final String result = converter.getForegroundColorCode(mockEvent);
            // Then: should return LESS_VISIBILITY code
            assertEquals(MessageHighlightConverter.LESS_VISIBILITY, result);
        }

        @Test
        @DisplayName("should return LESS_VISIBILITY for DATA_REJECT marker")
        void testDataRejectMarker() {
            // Given: logging event with DATA_REJECT marker
            when(mockEvent.getMarker()).thenReturn(Markers.DATA_REJECT);
            // When: getForegroundColorCode is called
            final String result = converter.getForegroundColorCode(mockEvent);
            // Then: should return LESS_VISIBILITY code
            assertEquals(MessageHighlightConverter.LESS_VISIBILITY, result);
        }

        @Test
        @DisplayName("should return LESS_VISIBILITY for DATA_FAIL marker")
        void testDataFailMarker() {
            // Given: logging event with DATA_FAIL marker
            when(mockEvent.getMarker()).thenReturn(Markers.DATA_FAIL);
            // When: getForegroundColorCode is called
            final String result = converter.getForegroundColorCode(mockEvent);
            // Then: should return LESS_VISIBILITY code
            assertEquals(MessageHighlightConverter.LESS_VISIBILITY, result);
        }

        @Test
        @DisplayName("should return LESS_VISIBILITY for DATA_WATCHER marker")
        void testWatcherDataMarker() {
            // Given: logging event with DATA_WATCHER marker
            when(mockEvent.getMarker()).thenReturn(org.usefultoys.slf4j.watcher.Markers.DATA_WATCHER);
            // When: getForegroundColorCode is called
            final String result = converter.getForegroundColorCode(mockEvent);
            // Then: should return LESS_VISIBILITY code
            assertEquals(MessageHighlightConverter.LESS_VISIBILITY, result);
        }
    }

    @Nested
    @DisplayName("ERROR_VISIBILITY markers")
    class ErrorVisibilityMarkersTest {

        @Test
        @DisplayName("should return ERROR_VISIBILITY for BUG marker")
        void testBugMarker() {
            // Given: logging event with BUG marker
            when(mockEvent.getMarker()).thenReturn(Markers.BUG);
            // When: getForegroundColorCode is called
            final String result = converter.getForegroundColorCode(mockEvent);
            // Then: should return ERROR_VISIBILITY code
            assertEquals(MessageHighlightConverter.ERROR_VISIBILITY, result);
        }

        @Test
        @DisplayName("should return ERROR_VISIBILITY for ILLEGAL marker")
        void testIllegalMarker() {
            // Given: logging event with ILLEGAL marker
            when(mockEvent.getMarker()).thenReturn(Markers.ILLEGAL);
            // When: getForegroundColorCode is called
            final String result = converter.getForegroundColorCode(mockEvent);
            // Then: should return ERROR_VISIBILITY code
            assertEquals(MessageHighlightConverter.ERROR_VISIBILITY, result);
        }

        @Test
        @DisplayName("should return ERROR_VISIBILITY for INCONSISTENT_START marker")
        void testInconsistentStartMarker() {
            // Given: logging event with INCONSISTENT_START marker
            when(mockEvent.getMarker()).thenReturn(Markers.INCONSISTENT_START);
            // When: getForegroundColorCode is called
            final String result = converter.getForegroundColorCode(mockEvent);
            // Then: should return ERROR_VISIBILITY code
            assertEquals(MessageHighlightConverter.ERROR_VISIBILITY, result);
        }

        @Test
        @DisplayName("should return ERROR_VISIBILITY for INCONSISTENT_INCREMENT marker")
        void testInconsistentIncrementMarker() {
            // Given: logging event with INCONSISTENT_INCREMENT marker
            when(mockEvent.getMarker()).thenReturn(Markers.INCONSISTENT_INCREMENT);
            // When: getForegroundColorCode is called
            final String result = converter.getForegroundColorCode(mockEvent);
            // Then: should return ERROR_VISIBILITY code
            assertEquals(MessageHighlightConverter.ERROR_VISIBILITY, result);
        }

        @Test
        @DisplayName("should return ERROR_VISIBILITY for INCONSISTENT_PROGRESS marker")
        void testInconsistentProgressMarker() {
            // Given: logging event with INCONSISTENT_PROGRESS marker
            when(mockEvent.getMarker()).thenReturn(Markers.INCONSISTENT_PROGRESS);
            // When: getForegroundColorCode is called
            final String result = converter.getForegroundColorCode(mockEvent);
            // Then: should return ERROR_VISIBILITY code
            assertEquals(MessageHighlightConverter.ERROR_VISIBILITY, result);
        }

        @Test
        @DisplayName("should return ERROR_VISIBILITY for INCONSISTENT_EXCEPTION marker")
        void testInconsistentExceptionMarker() {
            // Given: logging event with INCONSISTENT_EXCEPTION marker
            when(mockEvent.getMarker()).thenReturn(Markers.INCONSISTENT_EXCEPTION);
            // When: getForegroundColorCode is called
            final String result = converter.getForegroundColorCode(mockEvent);
            // Then: should return ERROR_VISIBILITY code
            assertEquals(MessageHighlightConverter.ERROR_VISIBILITY, result);
        }

        @Test
        @DisplayName("should return ERROR_VISIBILITY for INCONSISTENT_REJECT marker")
        void testInconsistentRejectMarker() {
            // Given: logging event with INCONSISTENT_REJECT marker
            when(mockEvent.getMarker()).thenReturn(Markers.INCONSISTENT_REJECT);
            // When: getForegroundColorCode is called
            final String result = converter.getForegroundColorCode(mockEvent);
            // Then: should return ERROR_VISIBILITY code
            assertEquals(MessageHighlightConverter.ERROR_VISIBILITY, result);
        }

        @Test
        @DisplayName("should return ERROR_VISIBILITY for INCONSISTENT_OK marker")
        void testInconsistentOkMarker() {
            // Given: logging event with INCONSISTENT_OK marker
            when(mockEvent.getMarker()).thenReturn(Markers.INCONSISTENT_OK);
            // When: getForegroundColorCode is called
            final String result = converter.getForegroundColorCode(mockEvent);
            // Then: should return ERROR_VISIBILITY code
            assertEquals(MessageHighlightConverter.ERROR_VISIBILITY, result);
        }

        @Test
        @DisplayName("should return ERROR_VISIBILITY for INCONSISTENT_FAIL marker")
        void testInconsistentFailMarker() {
            // Given: logging event with INCONSISTENT_FAIL marker
            when(mockEvent.getMarker()).thenReturn(Markers.INCONSISTENT_FAIL);
            // When: getForegroundColorCode is called
            final String result = converter.getForegroundColorCode(mockEvent);
            // Then: should return ERROR_VISIBILITY code
            assertEquals(MessageHighlightConverter.ERROR_VISIBILITY, result);
        }

        @Test
        @DisplayName("should return ERROR_VISIBILITY for INCONSISTENT_FINALIZED marker")
        void testInconsistentFinalizedMarker() {
            // Given: logging event with INCONSISTENT_FINALIZED marker
            when(mockEvent.getMarker()).thenReturn(Markers.INCONSISTENT_FINALIZED);
            // When: getForegroundColorCode is called
            final String result = converter.getForegroundColorCode(mockEvent);
            // Then: should return ERROR_VISIBILITY code
            assertEquals(MessageHighlightConverter.ERROR_VISIBILITY, result);
        }
    }

    @Nested
    @DisplayName("DEFAULT_VISIBILITY (fallback)")
    class DefaultVisibilityTest {

        @Test
        @DisplayName("should return DEFAULT_VISIBILITY for MSG_WATCHER marker")
        void testWatcherMsgMarker() {
            // Given: logging event with MSG_WATCHER marker
            when(mockEvent.getMarker()).thenReturn(org.usefultoys.slf4j.watcher.Markers.MSG_WATCHER);
            // When: getForegroundColorCode is called
            final String result = converter.getForegroundColorCode(mockEvent);
            // Then: should return DEFAULT_VISIBILITY code
            assertEquals(MessageHighlightConverter.DEFAULT_VISIBILITY, result);
        }

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
