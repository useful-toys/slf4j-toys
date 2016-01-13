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

/**
 *
 * @author Daniel Felix Ferber
 */
public class MeterConfig {

    public static boolean printCategory = Config.getProperty("slf4jtoys.meter.print.category", true);
    public static long progressPeriodMilliseconds = Config.getMillisecondsProperty("slf4jtoys.meter.progress.period", 2000L);
}
