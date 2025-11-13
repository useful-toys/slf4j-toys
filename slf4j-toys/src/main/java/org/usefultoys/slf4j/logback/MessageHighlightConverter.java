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
import ch.qos.logback.core.pattern.color.ForegroundCompositeConverterBase;
import org.slf4j.Marker;
import org.usefultoys.slf4j.meter.Markers;

/**
 * A Logback converter that applies ANSI foreground colors to log messages based on their SLF4J {@link Marker}.
 * This converter is used to visually distinguish different types of messages, particularly those generated
 * by `slf4j-toys` components like {@link org.usefultoys.slf4j.meter.Meter} and {@link org.usefultoys.slf4j.watcher.Watcher}.
 * <p>
 * It assigns specific colors to messages indicating operation lifecycle status (START, OK, REJECT, FAIL, PROGRESS),
 * data messages, and inconsistency/bug markers.
 *
 * @author Daniel Felix Ferber
 * @see AnsiColors
 * @see Markers
 */
public class MessageHighlightConverter extends ForegroundCompositeConverterBase<ILoggingEvent> {

    /** ANSI color code for messages that should have more visibility (e.g., Meter lifecycle events). */
    public static final String MORE_VISIBILITY = AnsiColors.BRIGHT_WHITE;
    /** ANSI color code for default message visibility. */
    public static final String DEFAULT_VISIBILITY = AnsiColors.WHITE;
    /** ANSI color code for messages that should have less visibility (e.g., machine-parsable data). */
    public static final String LESS_VISIBILITY = AnsiColors.BRIGHT_BLACK;
    /** ANSI color code for error-related messages. */
    public static final String ERROR_VISIBILITY = AnsiColors.RED;

    /**
     * Determines the ANSI foreground color code based on the logging event's {@link Marker}.
     *
     * @param event The logging event.
     * @return The ANSI foreground color code as a string.
     */
    @Override
    protected String getForegroundColorCode(final ILoggingEvent event) {
        final Marker marker = event.getMarker();
        if (marker == Markers.MSG_START
                || marker == Markers.MSG_PROGRESS
                || marker == Markers.MSG_OK
                || marker == Markers.MSG_SLOW_OK
                || marker == Markers.MSG_REJECT
                || marker == Markers.MSG_FAIL)
            return MORE_VISIBILITY;

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
                || marker == Markers.INCONSISTENT_FINALIZED) return ERROR_VISIBILITY;

        return DEFAULT_VISIBILITY;
    }
}
