/**
 * Copyright 2017 Daniel Felix Ferber
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

/**
 * Collection of SLF4J {@link Marker}s used to identify {@link Watcher} log messages.
 *
 * @author Daniel Felix Ferber
 */
public final class Markers {

    private Markers() {
        //nothing
    }

    public static final Marker MSG_WATCHER = MarkerFactory.getMarker("WATCHER_MSG");
    public static final Marker DATA_WATCHER = MarkerFactory.getMarker("WATCHER_DATA");
}
