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

    /** Marker for Meter API calls that are inconsistent with lifecycle state. */
    public final Marker INVALID_STATE = MarkerFactory.getMarker("METER_INVALID_STATE");
    /** Marker for Meter API calls that are invalid state transitions. */
    public final Marker INVALID_TRANSITION = MarkerFactory.getMarker("METER_INVALID_TRANSITION");
    /** Marker for unexpected exceptions within the Meter implementation. */
    public final Marker UNEXPECTED_EXCEPTION = MarkerFactory.getMarker("METER_UNEXPECTED_EXCEPTION");
    /** Marker for illegal argument usage of the Meter API. */
    public final Marker INVALID_ARGUMENT = MarkerFactory.getMarker("INVALID_ARGUMENT");
}
