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

class StatusConverterTest {

    private StatusConverter converter;

    @Mock
    private ILoggingEvent mockEvent;
    @Mock
    private Marker mockMarker; // For unknown markers

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        converter = new StatusConverter();
    }

    // --- Tests for specific Markers ---

    @Test
    void testConvertWithMsgStartMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.MSG_START);
        assertEquals("START", converter.convert(mockEvent));
    }

    @Test
    void testConvertWithMsgProgressMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.MSG_PROGRESS);
        assertEquals("PROGR", converter.convert(mockEvent));
    }

    @Test
    void testConvertWithMsgOkMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.MSG_OK);
        assertEquals("OK", converter.convert(mockEvent));
    }

    @Test
    void testConvertWithMsgSlowOkMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.MSG_SLOW_OK);
        assertEquals("SLOW", converter.convert(mockEvent));
    }

    @Test
    void testConvertWithMsgRejectMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.MSG_REJECT);
        assertEquals("REJECT", converter.convert(mockEvent));
    }

    @Test
    void testConvertWithMsgFailMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.MSG_FAIL);
        assertEquals("FAIL", converter.convert(mockEvent));
    }

    // --- Tests for empty string Markers ---

    @Test
    void testConvertWithDataStartMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.DATA_START);
        assertEquals("", converter.convert(mockEvent));
    }

    @Test
    void testConvertWithDataProgressMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.DATA_PROGRESS);
        assertEquals("", converter.convert(mockEvent));
    }

    @Test
    void testConvertWithDataOkMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.DATA_OK);
        assertEquals("", converter.convert(mockEvent));
    }

    @Test
    void testConvertWithDataSlowOkMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.DATA_SLOW_OK);
        assertEquals("", converter.convert(mockEvent));
    }

    @Test
    void testConvertWithDataRejectMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.DATA_REJECT);
        assertEquals("", converter.convert(mockEvent));
    }

    @Test
    void testConvertWithDataFailMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.DATA_FAIL);
        assertEquals("", converter.convert(mockEvent));
    }

    @Test
    void testConvertWithWatcherDataMarker() {
        when(mockEvent.getMarker()).thenReturn(org.usefultoys.slf4j.watcher.Markers.DATA_WATCHER);
        assertEquals("", converter.convert(mockEvent));
    }

    // --- Tests for INCONSISTENT Markers ---

    @Test
    void testConvertWithBugMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.BUG);
        assertEquals("BUG", converter.convert(mockEvent));
    }

    @Test
    void testConvertWithIllegalMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.ILLEGAL);
        assertEquals("ILLEGAL", converter.convert(mockEvent));
    }

    @Test
    void testConvertWithInconsistentStartMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.INCONSISTENT_START);
        assertEquals("INCONSISTENT", converter.convert(mockEvent));
    }

    @Test
    void testConvertWithInconsistentIncrementMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.INCONSISTENT_INCREMENT);
        assertEquals("INCONSISTENT", converter.convert(mockEvent));
    }

    @Test
    void testConvertWithInconsistentProgressMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.INCONSISTENT_PROGRESS);
        assertEquals("INCONSISTENT", converter.convert(mockEvent));
    }

    @Test
    void testConvertWithInconsistentExceptionMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.INCONSISTENT_EXCEPTION);
        assertEquals("INCONSISTENT", converter.convert(mockEvent));
    }

    @Test
    void testConvertWithInconsistentRejectMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.INCONSISTENT_REJECT);
        assertEquals("INCONSISTENT", converter.convert(mockEvent));
    }

    @Test
    void testConvertWithInconsistentOkMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.INCONSISTENT_OK);
        assertEquals("INCONSISTENT", converter.convert(mockEvent));
    }

    @Test
    void testConvertWithInconsistentFailMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.INCONSISTENT_FAIL);
        assertEquals("INCONSISTENT", converter.convert(mockEvent));
    }

    @Test
    void testConvertWithInconsistentFinalizedMarker() {
        when(mockEvent.getMarker()).thenReturn(Markers.INCONSISTENT_FINALIZED);
        assertEquals("INCONSISTENT", converter.convert(mockEvent));
    }

    // --- Tests for WATCHER Marker ---

    @Test
    void testConvertWithWatcherMsgMarker() {
        when(mockEvent.getMarker()).thenReturn(org.usefultoys.slf4j.watcher.Markers.MSG_WATCHER);
        assertEquals("WATCHER", converter.convert(mockEvent));
    }

    // --- Tests for default behavior (no matching marker) ---

    @Test
    void testConvertWithUnknownMarkerAndInfoLevel() {
        when(mockEvent.getMarker()).thenReturn(mockMarker); // A generic mock marker not in the list
        when(mockEvent.getLevel()).thenReturn(Level.INFO);
        assertEquals("INFO", converter.convert(mockEvent));
    }

    @Test
    void testConvertWithNullMarkerAndDebugLevel() {
        when(mockEvent.getMarker()).thenReturn(null);
        when(mockEvent.getLevel()).thenReturn(Level.DEBUG);
        assertEquals("DEBUG", converter.convert(mockEvent));
    }

    @Test
    void testConvertWithNullMarkerAndErrorLevel() {
        when(mockEvent.getMarker()).thenReturn(null);
        when(mockEvent.getLevel()).thenReturn(Level.ERROR);
        assertEquals("ERROR", converter.convert(mockEvent));
    }
}
