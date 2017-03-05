/**
 * Copyright 2017 Daniel Felix Ferber
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.usefultoys.slf4j.meter;

import org.usefultoys.slf4j.internal.Config;

/**
 * Collection of properties that drive {@link Meter} and {@link MeterData} behavior.
 * Initial values are read from system properties at application startup, if available.
 * They may be assigned at application startup, before calling any {@link Meter} methods.
 * Some properties allow reassigning their values at runtime.
 *
 * @author Daniel Felix Ferber
 */
public class MeterConfig {

    /**
     * Time to wait before reporting next progress status, in milliseconds. Meter allows reporting progress status of incremental operations by
     * calling the {@link Meter#inc()}, {@link Meter#incBy(long)} and {@link Meter#incTo(long)} method on each step. To prevent crowding the log file and
     * to prevent performance degradation, Meter waits a minimal amount of time before printing the next status message. Value is read from system
     * property {@code slf4jtoys.meter.progress.period} at application startup and defaults to {@code 2 seconds}. The number represents a long integer
     * that represents milliseconds. The system property allows the number suffixed with 'ms', 's', 'm' and 'h' to represent milliseconds, seconds,
     * minutes and hours. You may assign a new value at runtime.
     */
    public static long progressPeriodMilliseconds = Config.getMillisecondsProperty("slf4jtoys.meter.progress.period", 2000L);

    /**
     * If {@link Meter} and {@link MeterData} print the category on the 1-line summary message. The category groups operations that are closely
     * related. Usually, the category is the same as logger name declared within the class that creates the `Meter`. The usual logger configuration
     * already includes the logger name. But if your logger configuration omits the logger name, then you may set this property to true. Value is read
     * from system property {@code slf4jtoys.meter.print.category} at application startup, defaults to {@code false}. You may assign a new value at
     * runtime.
     */
    public static boolean printCategory = Config.getProperty("slf4jtoys.meter.print.category", false);
}
