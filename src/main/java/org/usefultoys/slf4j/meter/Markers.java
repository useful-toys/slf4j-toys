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
 * These markers allow for fine-grained filtering and routing of log events in logging frameworks.
 *
 * @author Daniel Felix Ferber
 * @see Meter
 */
@UtilityClass
public class Markers {

    /** Marker for machine-parsable data messages when an operation starts. */
    public final Marker DATA_START = MarkerFactory.getMarker("METER_DATA_START");
    /** Marker for machine-parsable data messages when an operation completes successfully. */
    public final Marker DATA_OK = MarkerFactory.getMarker("METER_DATA_OK");
    /** Marker for machine-parsable data messages when a successful operation exceeds its time limit. */
    public final Marker DATA_SLOW_OK = MarkerFactory.getMarker("METER_DATA_SLOW_OK");
    /** Marker for machine-parsable data messages when an operation reports progress. */
    public final Marker DATA_PROGRESS = MarkerFactory.getMarker("METER_DATA_PROGRESS");
    /** Marker for machine-parsable data messages when a progressing operation exceeds its time limit. */
    public final Marker DATA_SLOW_PROGRESS = MarkerFactory.getMarker("METER_DATA_SLOW_PROGRESS");
    /** Marker for machine-parsable data messages when an operation is rejected. */
    public final Marker DATA_REJECT = MarkerFactory.getMarker("METER_DATA_REJECT");
    /** Marker for machine-parsable data messages when an operation fails. */
    public final Marker DATA_FAIL = MarkerFactory.getMarker("METER_DATA_FAIL");

    /** Marker for human-readable messages when an operation starts. */
    public final Marker MSG_START = MarkerFactory.getMarker("METER_MSG_START");
    /** Marker for human-readable messages when an operation reports progress. */
    public final Marker MSG_PROGRESS = MarkerFactory.getMarker("METER_MSG_PROGRESS");
    /** Marker for human-readable messages when an operation completes successfully. */
    public final Marker MSG_OK = MarkerFactory.getMarker("METER_MSG_OK");
    /** Marker for human-readable messages when a successful operation exceeds its time limit. */
    public final Marker MSG_SLOW_OK = MarkerFactory.getMarker("METER_MSG_SLOW_OK");
    /** Marker for human-readable messages when an operation is rejected. */
    public final Marker MSG_REJECT = MarkerFactory.getMarker("METER_MSG_REJECT");
    /** Marker for human-readable messages when an operation fails. */
    public final Marker MSG_FAIL = MarkerFactory.getMarker("METER_MSG_FAIL");

    /** Marker for internal inconsistencies: an operation was finalized without being started. */
    public final Marker INCONSISTENT_FINALIZED = MarkerFactory.getMarker("METER_INCONSISTENT_FINALIZED");
    /** Marker for internal inconsistencies: an operation was started more than once. */
    public final Marker INCONSISTENT_START = MarkerFactory.getMarker("METER_INCONSISTENT_START");
    /** Marker for internal inconsistencies: an iteration counter was incremented without the operation being started. */
    public final Marker INCONSISTENT_INCREMENT = MarkerFactory.getMarker("INCONSISTENT_INCREMENT");
    /** Marker for internal inconsistencies: progress was reported without the operation being started. */
    public final Marker INCONSISTENT_PROGRESS = MarkerFactory.getMarker("INCONSISTENT_PROGRESS");
    /** Marker for internal inconsistencies: an operation was marked as OK without being started. */
    public final Marker INCONSISTENT_OK = MarkerFactory.getMarker("METER_INCONSISTENT_OK");
    /** Marker for internal inconsistencies: an operation was rejected without being started. */
    public final Marker INCONSISTENT_REJECT = MarkerFactory.getMarker("METER_INCONSISTENT_REJECT");
    /** Marker for internal inconsistencies: an operation failed without being started. */
    public final Marker INCONSISTENT_FAIL = MarkerFactory.getMarker("METER_INCONSISTENT_FAIL");
    /** Marker for internal inconsistencies: an exception was handled inconsistently. */
    public final Marker INCONSISTENT_EXCEPTION = MarkerFactory.getMarker("METER_INCONSISTENT_EXCEPTION");
    /** Marker for internal inconsistencies: an operation was closed inconsistently. */
    public final Marker INCONSISTENT_CLOSE = MarkerFactory.getMarker("METER_INCONSISTENT_CLOSE");

    /** Marker for internal bugs within the Meter implementation. */
    public final Marker BUG = MarkerFactory.getMarker("METER_BUG");
    /** Marker for illegal API usage of the Meter. */
    public final Marker ILLEGAL = MarkerFactory.getMarker("METER_ILLEGAL");
}
