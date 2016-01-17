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
 * @author daniel
 */
public class ExecutionFailure {

    public static final Logger logger = LoggerFactory.getLogger("example");

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

    public static void main(final String argv[]) {
        example1();

        try {
            example2();
        } catch (RuntimeException e) {
            // ignore
        }

    }

    private static void example1() {
        final Meter m = MeterFactory.getMeter(logger, "operation1").start();
        try {
            failOperation();
            m.ok();
        } catch (RuntimeException e) {
            m.fail(e);
        }
    }

    private static void example2() {
        try (final Meter m = MeterFactory.getMeter(logger, "operation2").start()) {
            failOperation();
            m.ok();
        }
    }

    private static class CustomException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public CustomException(String message) {
            super(message);
        }
    }

    private static void failOperation() throws CustomException {
        try {
            Thread.sleep(500);
            throw new CustomException("error message");
        } catch (InterruptedException ex) {
            // ignore
        }
    }
}
