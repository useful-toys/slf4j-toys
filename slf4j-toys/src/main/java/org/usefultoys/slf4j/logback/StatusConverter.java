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

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.slf4j.Marker;
import org.usefultoys.slf4j.meter.Markers;

public class StatusConverter extends ClassicConverter  {
    public String convert(final ILoggingEvent event) {
        final Marker marker = event.getMarker();
        if (marker == Markers.MSG_START) return "START";
        if (marker == Markers.MSG_PROGRESS) return "PROGR";
        if (marker == Markers.MSG_OK) return "OK";
        if (marker == Markers.MSG_SLOW_OK) return "SLOW";
        if (marker == Markers.MSG_REJECT) return "REJECT";
        if (marker == Markers.MSG_FAIL) return "FAIL";
        if (marker == Markers.DATA_START) return "";
        if (marker == Markers.DATA_PROGRESS) return "";
        if (marker == Markers.DATA_OK) return "";
        if (marker == Markers.DATA_SLOW_OK) return "";
        if (marker == Markers.DATA_REJECT) return "";
        if (marker == Markers.DATA_FAIL) return "";
        if (marker == Markers.BUG) return "BUG";
        if (marker == Markers.ILLEGAL) return "ILLEGAL";
        if (marker == Markers.INCONSISTENT_START) return "INCONSISTENT";
        if (marker == Markers.INCONSISTENT_INCREMENT) return "INCONSISTENT";
        if (marker == Markers.INCONSISTENT_PROGRESS) return "INCONSISTENT";
        if (marker == Markers.INCONSISTENT_EXCEPTION) return "INCONSISTENT";
        if (marker == Markers.INCONSISTENT_REJECT) return "INCONSISTENT";
        if (marker == Markers.INCONSISTENT_OK) return "INCONSISTENT";
        if (marker == Markers.INCONSISTENT_FAIL) return "INCONSISTENT";
        if (marker == Markers.INCONSISTENT_FINALIZED) return "INCONSISTENT";
        if (marker == org.usefultoys.slf4j.watcher.Markers.MSG_WATCHER) return "WATCHER";
        if (marker == org.usefultoys.slf4j.watcher.Markers.DATA_WATCHER) return "";
        return event.getLevel().toString();
    }
}
