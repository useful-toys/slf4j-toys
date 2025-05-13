/*
 * Copyright 2025 Daniel Felix Ferber
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
public final class ExecutionPath {

    public static final Logger logger = LoggerFactory.getLogger("example");

    static {
        /* Customizes the SLF4J simple logger to display trace messages that contain
         * encoded and parsable information. */
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
        /* Enable managed bean that is able to read CPU usage.  */
        System.setProperty("profiler.usePlatformManagedBean", "true");
    }

    public static void main(final String argv[]) {
        example1();
        example2A();
        example2C();
        example2B();
        example2D();
        example3A();
        example3B();
        example3C();
    }

    private static void example1() {
        /* Simplistic usage. */
        final Meter m1 = MeterFactory.getMeter(logger, "operation1").start();
        runOperation(true);
        m1.ok();
    }

    private static void example2A() {
        /* Report execution flow with Meter.ok() with string parameter. */
        final Meter m2 = MeterFactory.getMeter(logger, "operation2").start();
        // Check if user exists in database
        final boolean exist = runOperation(true);
        if (exist) {
            // Update user in database
            someOperation();
            m2.ok("Update");
        } else {
            // Insert user into database
            otherOperation();
            m2.ok("Insert");
        }
    }

    static enum OperationResult {

        UPDATE, INSERT
    }

    private static void example2C() {
        /* Report execution flow with Meter.ok() with enum parameter. */
        final Meter m2 = MeterFactory.getMeter(logger, "operation2").start();
        // Check if user exists in database
        final boolean exist = runOperation(true);
        if (exist) {
            // Update user in database
            someOperation();
            m2.ok(OperationResult.UPDATE);
        } else {
            // Insert user into database
            otherOperation();
            m2.ok(OperationResult.INSERT);
        }
    }

    private static void example2B() {
        /* Report execution flow with Meter.flow() with string parameter. */
        final Meter m2 = MeterFactory.getMeter(logger, "operation2").start();
        // Check if user exists in database
        final boolean exist = runOperation(true);
        if (exist) {
            // Update user in database
            someOperation();
            m2.path((Object) "Update");
        } else {
            // Insert user into database
            otherOperation();
            m2.path((Object) "Insert");
        }
        m2.ok();
    }

    private static void example2D() {
        /* Report execution flow with Meter.flow() with enum parameter. */
        final Meter m2 = MeterFactory.getMeter(logger, "operation2").start();
        // Check if user exists in database
        final boolean exist = runOperation(true);
        if (exist) {
            // Update user in database
            someOperation();
            m2.path((Object) OperationResult.UPDATE);
        } else {
            // Insert user into database
            otherOperation();
            m2.path((Object) OperationResult.INSERT);
        }
        m2.ok();
    }

    private static void example3A() {
        /* Report execution flow with Meter.ok() and Meter.reject(). */
        final Meter m2 = MeterFactory.getMeter(logger, "operation3").start();
        // Check if user has permissions
        final boolean authorized = runOperation(false);
        if (authorized) {
            // Proceed
            someOperation();
            m2.ok("Update");
        } else {
            m2.reject("Unauthorized");
        }
    }

    static enum OperationFailure {

        UNAUTHORIZED
    }

    private static void example3B() {
        /* Report execution flow with Meter.ok() and Meter.reject() with enum parameter. */
        final Meter m2 = MeterFactory.getMeter(logger, "operation3").start();
        // Check if user has permissions
        final boolean authorized = runOperation(false);
        if (authorized) {
            // Proceed
            someOperation();
            m2.ok("Update");
        } else {
            m2.reject(OperationFailure.UNAUTHORIZED);
        }
    }

    public static class UnauthorizedException extends Exception {
    }

    private static void example3C() {
        /* Report execution flow with Meter.ok() and Meter.reject() with exception parameter. */
        final Meter m2 = MeterFactory.getMeter(logger, "operation3").start();
        try {
            // Check if user has permissions
            final boolean authorized = runOperation(false);
            if (!authorized) {
                throw new UnauthorizedException();
            }
            // Proceed
            someOperation();
            m2.ok("Update");
        } catch (final UnauthorizedException e) {
            m2.reject(e);
        }
    }

    private static boolean runOperation(final boolean expectedResult) {
        try {
            Thread.sleep(1000);
        } catch (final InterruptedException ex) {
            // ignore
        }
        return expectedResult;
    }

    private static void someOperation() {
        try {
            Thread.sleep(1000);
        } catch (final InterruptedException ex) {
            // ignore
        }
    }

    private static void otherOperation() {
        try {
            Thread.sleep(1000);
        } catch (final InterruptedException ex) {
            // ignore
        }
    }
}
