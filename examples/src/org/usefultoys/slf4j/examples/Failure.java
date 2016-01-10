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

import java.sql.SQLException;
import java.util.Random;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.usefultoys.slf4j.LoggerFactory;
import org.usefultoys.slf4j.meter.Meter;
import org.usefultoys.slf4j.meter.MeterFactory;

/**
 *
 * @author daniel
 */
public class Failure {

    static {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
    }

    public static void main(final String argv[]) {
        example1();

    }

    private static void example1() {
        final Logger logger = LoggerFactory.getLogger("example");
        final Meter m = MeterFactory.getMeter(logger, "operation").start();
        try {
            runOperation();
            m.ok();
        } catch (RuntimeException e) {
            m.fail(e);
            throw e;
        }
    }

    public static class BusinessLogicException extends Exception {

        public BusinessLogicException(Throwable cause) {
            super(cause);
        }
    }

    private static void example2() {
        final Logger logger = LoggerFactory.getLogger("example");
        final Meter m = MeterFactory.getMeter(logger, "operation").start();
        try {
            runOperation();
            m.ok();
        } catch (RuntimeException e) {
            m.fail(e);
            throw e;
        }
    }

    private static void example3() {
        final Logger logger = LoggerFactory.getLogger("example");
        try (final Meter m = MeterFactory.getMeter(logger, "operation").start()) {
            runOperation();
            m.ok();
        }
    }

    private static void example4() {
        final Logger logger = LoggerFactory.getLogger("example");
        try (final Meter m = MeterFactory.getMeter(logger, "operation").start()) {
            failOperation();
            m.ok();
        }
    }

    private static void runOperation() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            // ignore
        }
    }

    private static void failOperation() {
        try {
            Thread.sleep(500);
            throw new RuntimeException();
        } catch (InterruptedException ex) {
            // ignore
        }
    }
}
