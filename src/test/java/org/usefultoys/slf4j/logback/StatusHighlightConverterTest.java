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
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Marker;
import org.usefultoys.slf4j.meter.Markers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class StatusHighlightConverterTest {

    private StatusHighlightConverter converter;

    @Mock
    private ILoggingEvent mockEvent;
    @Mock
    private Marker mockMarker; // For unknown markers

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        converter = new StatusHighlightConverter();
    }

    // --- Tests for specific Markers ---

    @Test
    void testConvertWithMsgStartMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.MSG_START);
        assertEquals(StatusHighlightConverter.START_VISIBILITY, converter.getForegroundColorCode(mockEvent));
    }

    @Test
    void testConvertWithMsgProgressMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.MSG_PROGRESS);
        assertEquals(StatusHighlightConverter.START_VISIBILITY, converter.getForegroundColorCode(mockEvent));
    }

    @Test
    void testConvertWithMsgOkMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.MSG_OK);
        assertEquals(StatusHighlightConverter.SUCCESS_VISIBILITY, converter.getForegroundColorCode(mockEvent));
    }

    @Test
    void testConvertWithMsgSlowOkMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.MSG_SLOW_OK);
        assertEquals(StatusHighlightConverter.WARN_VISIBILITY, converter.getForegroundColorCode(mockEvent));
    }

    @Test
    void testConvertWithMsgRejectMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.MSG_REJECT);
        assertEquals(StatusHighlightConverter.REJECT_VISIBILITY, converter.getForegroundColorCode(mockEvent));
    }

    @Test
    void testConvertWithMsgFailMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.MSG_FAIL);
        assertEquals(StatusHighlightConverter.ERROR_VISIBILITY, converter.getForegroundColorCode(mockEvent));
    }

    // --- Tests for LESS_VISIBILITY Markers ---

    @Test
    void testConvertWithDataStartMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.DATA_START);
        assertEquals(StatusHighlightConverter.LESS_VISIBILITY, converter.getForegroundColorCode(mockEvent));
    }

    @Test
    void testConvertWithDataProgressMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.DATA_PROGRESS);
        assertEquals(StatusHighlightConverter.LESS_VISIBILITY, converter.getForegroundColorCode(mockEvent));
    }

    @Test
    void testConvertWithDataOkMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.DATA_OK);
        assertEquals(StatusHighlightConverter.LESS_VISIBILITY, converter.getForegroundColorCode(mockEvent));
    }

    @Test
    void testConvertWithDataSlowOkMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.DATA_SLOW_OK);
        assertEquals(StatusHighlightConverter.LESS_VISIBILITY, converter.getForegroundColorCode(mockEvent));
    }

    @Test
    void testConvertWithDataRejectMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.DATA_REJECT);
        assertEquals(StatusHighlightConverter.LESS_VISIBILITY, converter.getForegroundColorCode(mockEvent));
    }

    @Test
    void testConvertWithDataFailMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.DATA_FAIL);
        assertEquals(StatusHighlightConverter.LESS_VISIBILITY, converter.getForegroundColorCode(mockEvent));
    }

    @Test
    void testConvertWithWatcherDataMarker() {
        when(mockEvent.getMarker()).thenReturn(org.usefultoys.slf4j.watcher.Markers.DATA_WATCHER);
        assertEquals(StatusHighlightConverter.LESS_VISIBILITY, converter.getForegroundColorCode(mockEvent));
    }

    // --- Tests for INCONSISTENCY_VISIBILITY Markers ---

    @Test
    void testConvertWithBugMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.BUG);
        assertEquals(StatusHighlightConverter.INCONSISTENCY_VISIBILITY, converter.getForegroundColorCode(mockEvent));
    }

    @Test
    void testConvertWithIllegalMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.ILLEGAL);
        assertEquals(StatusHighlightConverter.INCONSISTENCY_VISIBILITY, converter.getForegroundColorCode(mockEvent));
    }

    @Test
    void testConvertWithInconsistentStartMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.INCONSISTENT_START);
        assertEquals(StatusHighlightConverter.INCONSISTENCY_VISIBILITY, converter.getForegroundColorCode(mockEvent));
    }

    @Test
    void testConvertWithInconsistentIncrementMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.INCONSISTENT_INCREMENT);
        assertEquals(StatusHighlightConverter.INCONSISTENCY_VISIBILITY, converter.getForegroundColorCode(mockEvent));
    }

    @Test
    void testConvertWithInconsistentProgressMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.INCONSISTENT_PROGRESS);
        assertEquals(StatusHighlightConverter.INCONSISTENCY_VISIBILITY, converter.getForegroundColorCode(mockEvent));
    }

    @Test
    void testConvertWithInconsistentExceptionMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.INCONSISTENT_EXCEPTION);
        assertEquals(StatusHighlightConverter.INCONSISTENCY_VISIBILITY, converter.getForegroundColorCode(mockEvent));
    }

    @Test
    void testConvertWithInconsistentRejectMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.INCONSISTENT_REJECT);
        assertEquals(StatusHighlightConverter.INCONSISTENCY_VISIBILITY, converter.getForegroundColorCode(mockEvent));
    }

    @Test
    void testConvertWithInconsistentOkMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.INCONSISTENT_OK);
        assertEquals(StatusHighlightConverter.INCONSISTENCY_VISIBILITY, converter.getForegroundColorCode(mockEvent));
    }

    @Test
    void testConvertWithInconsistentFailMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.INCONSISTENT_FAIL);
        assertEquals(StatusHighlightConverter.INCONSISTENCY_VISIBILITY, converter.getForegroundColorCode(mockEvent));
    }

    @Test
    void testConvertWithInconsistentFinalizedMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.INCONSISTENT_FINALIZED);
        assertEquals(StatusHighlightConverter.INCONSISTENCY_VISIBILITY, converter.getForegroundColorCode(mockEvent));
    }

    // --- Tests for WATCHER_VISIBILITY Marker ---

    @Test
    void testConvertWithWatcherMsgMarker() {
        when(mockEvent.getMarker()).thenReturn(org.usefultoys.slf4j.watcher.Markers.MSG_WATCHER);
        assertEquals(StatusHighlightConverter.WATCHER_VISIBILITY, converter.getForegroundColorCode(mockEvent));
    }

    // --- Tests for Level-based fallback ---

    @Test
    void testConvertWithNullMarkerAndErrorLevel() {
        when(mockEvent.getMarker()).thenReturn(null);
        when(mockEvent.getLevel()).thenReturn(Level.ERROR);
        assertEquals(StatusHighlightConverter.ERROR_VISIBILITY, converter.getForegroundColorCode(mockEvent));
    }

    @Test
    void testConvertWithNullMarkerAndWarnLevel() {
        when(mockEvent.getMarker()).thenReturn(null);
        when(mockEvent.getLevel()).thenReturn(Level.WARN);
        assertEquals(StatusHighlightConverter.WARN_VISIBILITY, converter.getForegroundColorCode(mockEvent));
    }

    @Test
    void testConvertWithNullMarkerAndInfoLevel() {
        when(mockEvent.getMarker()).thenReturn(null);
        when(mockEvent.getLevel()).thenReturn(Level.INFO);
        assertEquals(StatusHighlightConverter.INFO_VISIBILITY, converter.getForegroundColorCode(mockEvent));
    }

    @Test
    void testConvertWithNullMarkerAndDebugLevel() {
        when(mockEvent.getMarker()).thenReturn(null);
        when(mockEvent.getLevel()).thenReturn(Level.DEBUG);
        assertEquals(StatusHighlightConverter.DEBUG_VISIBILITY, converter.getForegroundColorCode(mockEvent));
    }

    @Test
    void testConvertWithNullMarkerAndTraceLevel() {
        when(mockEvent.getMarker()).thenReturn(null);
        when(mockEvent.getLevel()).thenReturn(Level.TRACE);
        assertEquals(StatusHighlightConverter.TRACE_VISIBILITY, converter.getForegroundColorCode(mockEvent));
    }

    @Test
    void testConvertWithUnknownMarkerAndDefaultLevel() {
        when(mockEvent.getMarker()).thenReturn(mockMarker); // A generic mock marker not in the list
        when(mockEvent.getLevel()).thenReturn(Level.OFF); // A level not explicitly handled by switch
        assertEquals(StatusHighlightConverter.DEFAULT_VISIBILITY, converter.getForegroundColorCode(mockEvent));
    }

    @Test
    void testConvertWithUnknownMarkerAndErrorLevel() {
        when(mockEvent.getMarker()).thenReturn(mockMarker); // A generic mock marker not in the list
        when(mockEvent.getLevel()).thenReturn(Level.ERROR);
        assertEquals(StatusHighlightConverter.ERROR_VISIBILITY, converter.getForegroundColorCode(mockEvent));
    }
}
