package org.usefultoys.slf4j.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.color.ANSIConstants;
import ch.qos.logback.core.pattern.color.ForegroundCompositeConverterBase;
import org.slf4j.Marker;
import org.usefultoys.slf4j.meter.Markers;

public class MessageHighlightConverter extends ForegroundCompositeConverterBase<ILoggingEvent> {

    @Override
    protected String getForegroundColorCode(ILoggingEvent event) {
        final Marker marker = event.getMarker();
        if (marker == Markers.DATA_START) return ANSIConstants.BOLD + ANSIConstants.BLACK_FG;
        if (marker == Markers.DATA_PROGRESS) return ANSIConstants.BOLD + ANSIConstants.BLACK_FG;
        if (marker == Markers.DATA_OK) return ANSIConstants.BOLD + ANSIConstants.BLACK_FG;
        if (marker == Markers.DATA_SLOW_OK) return ANSIConstants.BOLD + ANSIConstants.BLACK_FG;
        if (marker == Markers.DATA_REJECT) return ANSIConstants.BOLD + ANSIConstants.BLACK_FG;
        if (marker == Markers.DATA_FAIL) return ANSIConstants.BOLD + ANSIConstants.BLACK_FG;
        if (marker == Markers.BUG) return ANSIConstants.RED_FG;
        if (marker == Markers.ILLEGAL) return ANSIConstants.RED_FG;
        if (marker == Markers.INCONSISTENT_START) return ANSIConstants.RED_FG;
        if (marker == Markers.INCONSISTENT_INCREMENT) return ANSIConstants.RED_FG;
        if (marker == Markers.INCONSISTENT_PROGRESS) return ANSIConstants.RED_FG;
        if (marker == Markers.INCONSISTENT_EXCEPTION) return ANSIConstants.RED_FG;
        if (marker == Markers.INCONSISTENT_REJECT) return ANSIConstants.RED_FG;
        if (marker == Markers.INCONSISTENT_OK) return ANSIConstants.RED_FG;
        if (marker == Markers.INCONSISTENT_FAIL) return ANSIConstants.RED_FG;
        if (marker == Markers.INCONSISTENT_FINALIZED) return ANSIConstants.RED_FG;
        if (marker == org.usefultoys.slf4j.watcher.Markers.DATA_WATCHER) return ANSIConstants.BOLD + ANSIConstants.BLACK_FG;
        return ANSIConstants.DEFAULT_FG;
    }
}