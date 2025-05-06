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
package org.usefultoys.slf4j.meter;

import lombok.experimental.UtilityClass;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * Collection of SLF4J {@link Marker}s used to identify {@link Meter} log messages.
 * @author Daniel Felix Ferber
 */
@UtilityClass
public class Markers {

    public final Marker DATA_START = MarkerFactory.getMarker("METER_DATA_START");
    public final Marker DATA_OK = MarkerFactory.getMarker("METER_DATA_OK");
    public final Marker DATA_SLOW_OK = MarkerFactory.getMarker("METER_DATA_SLOW_OK");
    public final Marker DATA_PROGRESS = MarkerFactory.getMarker("METER_DATA_PROGRESS");
    public final Marker DATA_SLOW_PROGRESS = MarkerFactory.getMarker("METER_DATA_SLOW_PROGRESS");
    public final Marker DATA_REJECT = MarkerFactory.getMarker("METER_DATA_REJECT");
    public final Marker DATA_FAIL = MarkerFactory.getMarker("METER_DATA_FAIL");
    public final Marker MSG_START = MarkerFactory.getMarker("METER_MSG_START");
    public final Marker MSG_PROGRESS = MarkerFactory.getMarker("METER_MSG_PROGRESS");
    public final Marker MSG_OK = MarkerFactory.getMarker("METER_MSG_OK");
    public final Marker MSG_SLOW_OK = MarkerFactory.getMarker("METER_MSG_SLOW_OK");
    public final Marker MSG_REJECT = MarkerFactory.getMarker("METER_MSG_REJECT");
    public final Marker MSG_FAIL = MarkerFactory.getMarker("METER_MSG_FAIL");
    public final Marker INCONSISTENT_FINALIZED = MarkerFactory.getMarker("METER_INCONSISTENT_FINALIZED");
    public final Marker INCONSISTENT_START = MarkerFactory.getMarker("METER_INCONSISTENT_START");
    public final Marker INCONSISTENT_INCREMENT = MarkerFactory.getMarker("INCONSISTENT_INCREMENT");
    public final Marker INCONSISTENT_PROGRESS = MarkerFactory.getMarker("INCONSISTENT_PROGRESS");
    public final Marker INCONSISTENT_OK = MarkerFactory.getMarker("METER_INCONSISTENT_OK");
    public final Marker INCONSISTENT_REJECT = MarkerFactory.getMarker("METER_INCONSISTENT_REJECT");
    public final Marker INCONSISTENT_FAIL = MarkerFactory.getMarker("METER_INCONSISTENT_FAIL");
    public final Marker INCONSISTENT_EXCEPTION = MarkerFactory.getMarker("METER_INCONSISTENT_EXCEPTION");
    public final Marker INCONSISTENT_CLOSE = MarkerFactory.getMarker("METER_INCONSISTENT_CLOSE");
    public final Marker BUG = MarkerFactory.getMarker("METER_BUG");
    public final Marker ILLEGAL = MarkerFactory.getMarker("METER_ILLEGAL");
}
