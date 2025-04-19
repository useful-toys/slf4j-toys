/*
 * Copyright 2024 Daniel Felix Ferber
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
package org.usefultoys.slf4j.watcher;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import lombok.experimental.UtilityClass;

/**
 * Utility class that provides a collection of SLF4J {@link Marker}s 
 * specifically used to categorize and identify log messages generated 
 * by the {@link Watcher} component.
 * 
 * <p>
 * Markers are used to enrich log messages with additional metadata, 
 * enabling better filtering and analysis of logs.
 * </p>
 * 
 * <ul>
 *   <li>{@link #MSG_WATCHER} - Marker for general {@link Watcher} log messages.</li>
 *   <li>{@link #DATA_WATCHER} - Marker for {@link Watcher} data-related log messages.</li>
 * </ul>
 * 
 * <p>
 * These markers can be used in conjunction with SLF4J logging frameworks 
 * to provide structured and meaningful log categorization.
 * </p>
 * 
 * @author Daniel Felix Ferber
 */
@UtilityClass
public final class Markers {
    public final Marker MSG_WATCHER = MarkerFactory.getMarker("WATCHER");
    public final Marker DATA_WATCHER = MarkerFactory.getMarker("WATCHER_DATA");
}
