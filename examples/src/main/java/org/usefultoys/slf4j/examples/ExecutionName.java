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
package org.usefultoys.slf4j.examples;

import org.slf4j.Logger;
import org.usefultoys.slf4j.LoggerFactory;
import org.usefultoys.slf4j.meter.Meter;
import org.usefultoys.slf4j.meter.MeterFactory;

/**
 *
 * @author daniel
 */
public class ExecutionName {

    static {
        /* Customizes the SLF4J simple logger to display trace messages that contain
         * encoded and parsable information. */
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
        System.setProperty("org.slf4j.simpleLogger.showDateTime", "true");
        System.setProperty("org.slf4j.simpleLogger.showThreadName", "false");
        System.setProperty("org.slf4j.simpleLogger.dateTimeFormat", "yy/MM/dd HH:mm");
        /* Enable managed bean that is able to read CPU usage.  */
        System.setProperty("profiler.usePlatformManagedBean", "true");
    }

    public static final Logger logger = LoggerFactory.getLogger("example");

    public static void main(final String argv[]) {
        for (int i = 0; i < 3; i++) {
            example1();
        }
        for (int i = 0; i < 3; i++) {
            example2();
        }
        for (int i = 0; i < 3; i++) {
            example3();
        }
        for (int i = 0; i < 3; i++) {
            example4();
        }
        for (int i = 0; i < 3; i++) {
            example5();
        }
    }

    private static void example1() {
        final Meter m = MeterFactory.getMeter(logger).start();
        runOperation();
        m.ok();
    }

    private static void example2() {
        final Meter m = MeterFactory.getMeter("category").start();
        runOperation();
        m.ok();
    }

    private static void example3() {
        final Meter m = MeterFactory.getMeter(ExecutionName.class).start();
        runOperation();
        m.ok();
    }

    private static void example4() {
        final Meter m = MeterFactory.getMeter(ExecutionName.class, "operation").start();
        runOperation();
        m.ok();
    }

    private static void example5() {
        final Meter m = MeterFactory.getMeter(logger, "operation").start();
        runOperation();
        m.ok();
    }

    private static void runOperation() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            // ignore
        }
    }
}
