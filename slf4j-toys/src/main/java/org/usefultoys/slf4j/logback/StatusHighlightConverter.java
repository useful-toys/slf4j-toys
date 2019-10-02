package org.usefultoys.slf4j.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.color.ANSIConstants;
import ch.qos.logback.core.pattern.color.ForegroundCompositeConverterBase;
import org.slf4j.Marker;
import org.usefultoys.slf4j.meter.Markers;

public class StatusHighlightConverter extends ForegroundCompositeConverterBase<ILoggingEvent> {

    @Override
    protected String getForegroundColorCode(ILoggingEvent event) {
        final Marker marker = event.getMarker();
        if (marker == Markers.MSG_START) return ANSIConstants.CYAN_FG;
        if (marker == Markers.MSG_PROGRESS) return ANSIConstants.CYAN_FG;
        if (marker == Markers.MSG_OK) return ANSIConstants.BOLD + ANSIConstants.GREEN_FG;
        if (marker == Markers.MSG_SLOW_OK) return ANSIConstants.YELLOW_FG;
        if (marker == Markers.MSG_REJECT) return ANSIConstants.BOLD + ANSIConstants.YELLOW_FG;
        if (marker == Markers.MSG_FAIL) return ANSIConstants.BOLD + ANSIConstants.RED_FG;
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
        if (marker == org.usefultoys.slf4j.watcher.Markers.MSG_WATCHER) return ANSIConstants.BLUE_FG;
        if (marker == org.usefultoys.slf4j.watcher.Markers.DATA_WATCHER) return ANSIConstants.BOLD + ANSIConstants.BLACK_FG;

        Level level = event.getLevel();
        switch (level.toInt()) {
            case Level.ERROR_INT:
                return ANSIConstants.BOLD + ANSIConstants.RED_FG;
            case Level.WARN_INT:
                return ANSIConstants.BOLD + ANSIConstants.MAGENTA_FG;
            case Level.INFO_INT:
                return ANSIConstants.BOLD + ANSIConstants.CYAN_FG;
            case Level.DEBUG_INT:
                return ANSIConstants.CYAN_FG;
            case Level.TRACE_INT:
                return ANSIConstants.BOLD + ANSIConstants.BLACK_FG;
            default:
                return ANSIConstants.DEFAULT_FG;
        }
    }

}