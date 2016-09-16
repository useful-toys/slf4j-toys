/*
 * Copyright 2016 Daniel Felix Ferber.
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
package org.usefultoys.slf4j.meter;

import org.usefultoys.slf4j.internal.Config;
import org.usefultoys.slf4j.watcher.Watcher;
import org.usefultoys.slf4j.watcher.WatcherData;

/**
 * Collection of properties that drive {@link Meter} and {@link MeterData} behavior.
 * Initial values are read from system properties, if available.
 * Some properties allow reassigning their values at runtime.
 * 
 * @author Daniel Felix Ferber
 */
public class MeterConfig {

    /**
     * If {@link Meter} and {@link MeterData} print the category on the 1-line summary message.
     * The category is the same as logger name. On the usual logger configuration, the logger prints its name.
     * Printing also the category name would be redundant. 
     * Setting this property to true only makes sense if logger is not configured to print its name. 
     * Value is read from system property {@code slf4jtoys.meter.print.category} at application startup, defaults to {@code false}.
     * You may assign a new value at runtime.
     */
    public static boolean printCategory = Config.getProperty("slf4jtoys.meter.print.category", false);
    /**
     * Time to wait before reporting next progress status, in milliseconds.
     * Value is read from system property {@code slf4jtoys.meter.progress.period} at application startup and defaults to {@code 2 seconds}.
     * You may assign a new value at runtime.
     */
    public static long progressPeriodMilliseconds = Config.getMillisecondsProperty("slf4jtoys.meter.progress.period", 2000L);
}
