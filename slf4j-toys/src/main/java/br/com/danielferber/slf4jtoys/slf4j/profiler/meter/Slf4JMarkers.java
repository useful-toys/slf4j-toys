/*
 * Copyright 2013 Daniel Felix Ferber
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package br.com.danielferber.slf4jtoys.slf4j.profiler.meter;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 *
 * @author Daniel Felix Ferber
 */
public final class Slf4JMarkers {
    private Slf4JMarkers() {
        // nothing
    }
    
    public static final Marker DATA_START = MarkerFactory.getMarker("METER_DATA_START");
    public static final Marker DATA_OK = MarkerFactory.getMarker("METER_DATA_OK");
    public static final Marker DATA_SLOW_OK = MarkerFactory.getMarker("METER_DATA_SLOW_OK");
    public static final Marker DATA_FAIL = MarkerFactory.getMarker("METER_DATA_FAIL");
    public static final Marker MSG_START = MarkerFactory.getMarker("METER_MSG_START");
    public static final Marker MSG_OK = MarkerFactory.getMarker("METER_MSG_OK");
    public static final Marker MSG_SLOW_OK = MarkerFactory.getMarker("METER_MSG_SLOW_OK");
    public static final Marker MSG_FAIL = MarkerFactory.getMarker("METER_MSG_FAIL");
    public static final Marker INCONSISTENT_FINALIZED = MarkerFactory.getMarker("METER_INCONSISTENT_FINALIZED");
    public static final Marker INCONSISTENT_START = MarkerFactory.getMarker("METER_INCONSISTENT_START");
    public static final Marker INCONSISTENT_OK = MarkerFactory.getMarker("METER_INCONSISTENT_OK");
    public static final Marker INCONSISTENT_FAIL = MarkerFactory.getMarker("METER_INCONSISTENT_FAIL");
}
