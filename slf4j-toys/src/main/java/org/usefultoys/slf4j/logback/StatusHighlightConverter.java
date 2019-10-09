/*
 * Copyright 2019 Daniel Felix Ferber
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import ch.qos.logback.core.pattern.color.ForegroundCompositeConverterBase;
import org.slf4j.Marker;
import org.usefultoys.slf4j.meter.Markers;

public class StatusHighlightConverter extends ForegroundCompositeConverterBase<ILoggingEvent> {
    public static final String ERROR_VISIBILITY = AnsiColors.BRIGHT_RED;
    public static final String WARN_VISIBILITY = AnsiColors.BRIGHT_YELLOW;
    public static final String INFO_VISIBILITY = AnsiColors.BRIGHT_GREEN;
    public static final String DEBUG_VISIBILITY = AnsiColors.CYAN;
    public static final String TRACE_VISIBILITY = AnsiColors.WHITE;
    public static final String WATCHER_VISIBILITY = AnsiColors.BLUE;
    public static final String DEFAULT_VISIBILITY = AnsiColors.DEFAULT;
    public static final String LESS_VISIBILITY = AnsiColors.BRIGHT_BLACK;
    public static final String INCONSISTENCY_VISIBILITY = AnsiColors.RED;
    public static final String REJECT_VISIBILITY = AnsiColors.BRIGHT_MAGENTA;
    public static final String SUCCESS_VISIBILITY = AnsiColors.BRIGHT_GREEN;
    public static final String START_VISIBILITY = AnsiColors.CYAN;

    @Override
    protected String getForegroundColorCode(final ILoggingEvent event) {
        final Marker marker = event.getMarker();
        if (marker == Markers.MSG_START) return START_VISIBILITY;
        if (marker == Markers.MSG_PROGRESS) return START_VISIBILITY;
        if (marker == Markers.MSG_OK) return SUCCESS_VISIBILITY;
        if (marker == Markers.MSG_SLOW_OK) return WARN_VISIBILITY;
        if (marker == Markers.MSG_REJECT) return REJECT_VISIBILITY;
        if (marker == Markers.MSG_FAIL) return ERROR_VISIBILITY;
        if (marker == Markers.DATA_START
                || marker == Markers.DATA_PROGRESS
                || marker == Markers.DATA_OK
                || marker == Markers.DATA_SLOW_OK
                || marker == Markers.DATA_REJECT
                || marker == Markers.DATA_FAIL
                || marker == org.usefultoys.slf4j.watcher.Markers.DATA_WATCHER) return LESS_VISIBILITY;
        if (marker == Markers.BUG
                || marker == Markers.ILLEGAL
                || marker == Markers.INCONSISTENT_START
                || marker == Markers.INCONSISTENT_INCREMENT
                || marker == Markers.INCONSISTENT_PROGRESS
                || marker == Markers.INCONSISTENT_EXCEPTION
                || marker == Markers.INCONSISTENT_REJECT
                || marker == Markers.INCONSISTENT_OK
                || marker == Markers.INCONSISTENT_FAIL
                || marker == Markers.INCONSISTENT_FINALIZED) return INCONSISTENCY_VISIBILITY;
        if (marker == org.usefultoys.slf4j.watcher.Markers.MSG_WATCHER) return WATCHER_VISIBILITY;

        final Level level = event.getLevel();
        switch (level.toInt()) {
            case Level.ERROR_INT:
                return ERROR_VISIBILITY;
            case Level.WARN_INT:
                return WARN_VISIBILITY;
            case Level.INFO_INT:
                return INFO_VISIBILITY;
            case Level.DEBUG_INT:
                return DEBUG_VISIBILITY;
            case Level.TRACE_INT:
                return TRACE_VISIBILITY;
            default:
                return DEFAULT_VISIBILITY;
        }
    }

}