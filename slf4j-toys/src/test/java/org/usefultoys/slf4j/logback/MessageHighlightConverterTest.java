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
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Marker;
import org.usefultoys.slf4j.meter.Markers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class MessageHighlightConverterTest {

    private MessageHighlightConverter converter;

    @Mock
    private ILoggingEvent mockEvent;
    @Mock
    private Marker mockMarker; // For unknown markers

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        converter = new MessageHighlightConverter();
    }

    // --- Tests for MORE_VISIBILITY Markers ---

    @Test
    void testConvertWithMsgStartMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.MSG_START);
        assertEquals(MessageHighlightConverter.MORE_VISIBILITY, converter.getForegroundColorCode(mockEvent));
    }

    @Test
    void testConvertWithMsgProgressMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.MSG_PROGRESS);
        assertEquals(MessageHighlightConverter.MORE_VISIBILITY, converter.getForegroundColorCode(mockEvent));
    }

    @Test
    void testConvertWithMsgOkMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.MSG_OK);
        assertEquals(MessageHighlightConverter.MORE_VISIBILITY, converter.getForegroundColorCode(mockEvent));
    }

    @Test
    void testConvertWithMsgSlowOkMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.MSG_SLOW_OK);
        assertEquals(MessageHighlightConverter.MORE_VISIBILITY, converter.getForegroundColorCode(mockEvent));
    }

    @Test
    void testConvertWithMsgRejectMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.MSG_REJECT);
        assertEquals(MessageHighlightConverter.MORE_VISIBILITY, converter.getForegroundColorCode(mockEvent));
    }

    @Test
    void testConvertWithMsgFailMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.MSG_FAIL);
        assertEquals(MessageHighlightConverter.MORE_VISIBILITY, converter.getForegroundColorCode(mockEvent));
    }

    // --- Tests for LESS_VISIBILITY Markers ---

    @Test
    void testConvertWithDataStartMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.DATA_START);
        assertEquals(MessageHighlightConverter.LESS_VISIBILITY, converter.getForegroundColorCode(mockEvent));
    }

    @Test
    void testConvertWithDataProgressMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.DATA_PROGRESS);
        assertEquals(MessageHighlightConverter.LESS_VISIBILITY, converter.getForegroundColorCode(mockEvent));
    }

    @Test
    void testConvertWithDataOkMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.DATA_OK);
        assertEquals(MessageHighlightConverter.LESS_VISIBILITY, converter.getForegroundColorCode(mockEvent));
    }

    @Test
    void testConvertWithDataSlowOkMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.DATA_SLOW_OK);
        assertEquals(MessageHighlightConverter.LESS_VISIBILITY, converter.getForegroundColorCode(mockEvent));
    }

    @Test
    void testConvertWithDataRejectMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.DATA_REJECT);
        assertEquals(MessageHighlightConverter.LESS_VISIBILITY, converter.getForegroundColorCode(mockEvent));
    }

    @Test
    void testConvertWithDataFailMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.DATA_FAIL);
        assertEquals(MessageHighlightConverter.LESS_VISIBILITY, converter.getForegroundColorCode(mockEvent));
    }

    @Test
    void testConvertWithWatcherDataMarker() {
        when(mockEvent.getMarker()).thenReturn(org.usefultoys.slf4j.watcher.Markers.DATA_WATCHER);
        assertEquals(MessageHighlightConverter.LESS_VISIBILITY, converter.getForegroundColorCode(mockEvent));
    }

    // --- Tests for ERROR_VISIBILITY Markers ---

    @Test
    void testConvertWithBugMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.BUG);
        assertEquals(MessageHighlightConverter.ERROR_VISIBILITY, converter.getForegroundColorCode(mockEvent));
    }

    @Test
    void testConvertWithIllegalMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.ILLEGAL);
        assertEquals(MessageHighlightConverter.ERROR_VISIBILITY, converter.getForegroundColorCode(mockEvent));
    }

    @Test
    void testConvertWithInconsistentStartMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.INCONSISTENT_START);
        assertEquals(MessageHighlightConverter.ERROR_VISIBILITY, converter.getForegroundColorCode(mockEvent));
    }

    @Test
    void testConvertWithInconsistentIncrementMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.INCONSISTENT_INCREMENT);
        assertEquals(MessageHighlightConverter.ERROR_VISIBILITY, converter.getForegroundColorCode(mockEvent));
    }

    @Test
    void testConvertWithInconsistentProgressMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.INCONSISTENT_PROGRESS);
        assertEquals(MessageHighlightConverter.ERROR_VISIBILITY, converter.getForegroundColorCode(mockEvent));
    }

    @Test
    void testConvertWithInconsistentExceptionMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.INCONSISTENT_EXCEPTION);
        assertEquals(MessageHighlightConverter.ERROR_VISIBILITY, converter.getForegroundColorCode(mockEvent));
    }

    @Test
    void testConvertWithInconsistentRejectMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.INCONSISTENT_REJECT);
        assertEquals(MessageHighlightConverter.ERROR_VISIBILITY, converter.getForegroundColorCode(mockEvent));
    }

    @Test
    void testConvertWithInconsistentOkMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.INCONSISTENT_OK);
        assertEquals(MessageHighlightConverter.ERROR_VISIBILITY, converter.getForegroundColorCode(mockEvent));
    }

    @Test
    void testConvertWithInconsistentFailMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.INCONSISTENT_FAIL);
        assertEquals(MessageHighlightConverter.ERROR_VISIBILITY, converter.getForegroundColorCode(mockEvent));
    }

    @Test
    void testConvertWithInconsistentFinalizedMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.INCONSISTENT_FINALIZED);
        assertEquals(MessageHighlightConverter.ERROR_VISIBILITY, converter.getForegroundColorCode(mockEvent));
    }

    // --- Tests for DEFAULT_VISIBILITY (fallback) ---

    @Test
    void testConvertWithWatcherMsgMarker() {
        when(mockEvent.getMarker()).thenReturn(org.usefultoys.slf4j.watcher.Markers.MSG_WATCHER);
        assertEquals(MessageHighlightConverter.DEFAULT_VISIBILITY, converter.getForegroundColorCode(mockEvent));
    }

    @Test
    void testConvertWithNullMarker() {
        when(mockEvent.getMarker()).thenReturn(null);
        assertEquals(MessageHighlightConverter.DEFAULT_VISIBILITY, converter.getForegroundColorCode(mockEvent));
    }

    @Test
    void testConvertWithUnknownMarker() {
        when(mockEvent.getMarker()).thenReturn(mockMarker); // A generic mock marker not in any specific list
        assertEquals(MessageHighlightConverter.DEFAULT_VISIBILITY, converter.getForegroundColorCode(mockEvent));
    }
}
