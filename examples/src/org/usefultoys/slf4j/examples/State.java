/*
 * Copyright 2015 Daniel Felix Ferber.
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
package org.usefultoys.slf4j.examples;

import org.slf4j.Logger;
import org.usefultoys.slf4j.LoggerFactory;
import org.usefultoys.slf4j.meter.Meter;
import org.usefultoys.slf4j.meter.MeterFactory;

/**
 *
 * @author Daniel Felix Ferber
 */
public class State {

    static {
        /* Customizes the SLF4J simple logger to display trace messages that contain
         * encoded and parsable information. */
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
        /* Enable managed bean that is able to read CPU usage.  */
        System.setProperty("profiler.usePlatformManagedBean", "true");
    }

    public static void main(final String argv[]) {
        example1(2, 3);
    }

    private static void example1(int a, int b) {
        final Logger logger = LoggerFactory.getLogger("example");
        final Meter m1 = MeterFactory.getMeter(logger, "operation1")
                .ctx("a", a).ctx("b", b).start();
        // ...
        int sum = a + b;
        // ...

        m1.ctx("sum", sum).ok();
    }
}
