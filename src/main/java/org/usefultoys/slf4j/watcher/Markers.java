/*
 * Copyright 2026 Daniel Felix Ferber
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
package org.usefultoys.slf4j.watcher;

import lombok.experimental.UtilityClass;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * Collection of SLF4J {@link Marker}s used to identify {@link Watcher} log messages.
 * These markers allow for fine-grained filtering and routing of log events in logging frameworks.
 *
 * @author Daniel Felix Ferber
 * @see Watcher
 */
@UtilityClass
public class Markers {
    /** Marker for human-readable {@link Watcher} log messages. */
    public final Marker MSG_WATCHER = MarkerFactory.getMarker("WATCHER");
    /** Marker for machine-parsable {@link Watcher} data log messages. */
    public final Marker DATA_WATCHER = MarkerFactory.getMarker("WATCHER_DATA");
}
