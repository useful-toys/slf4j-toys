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
public class ExecutionPath {

    static {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
    }

    public static void main(final String argv[]) {
        example1();
        example2();
        example3();
    }

    private static void example1() {
        final Logger logger = LoggerFactory.getLogger("example");
        final Meter m1 = MeterFactory.getMeter(logger, "operation1").start();
        runOperation(true);
        m1.ok();
    }

    private static void example2() {
        final Logger logger = LoggerFactory.getLogger("example");
        final Meter m2 = MeterFactory.getMeter(logger, "operation2").start();
        // Check if user exists in database
        boolean exist = runOperation(true);
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
    
    private static void example3() {
        final Logger logger = LoggerFactory.getLogger("example");
        final Meter m2 = MeterFactory.getMeter(logger, "operation3").start();
        // Check if user has permissions
        boolean authorized = runOperation(false);
        if (authorized) {
            // Proceed
            someOperation();
            m2.ok("Update");
        } else {
            m2.reject("Unauthorized");
        }
    }

    private static boolean runOperation(boolean expectedResult) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            // ignore
        }
        return expectedResult;
    }

    private static void someOperation() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            // ignore
        }
    }

    private static void otherOperation() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            // ignore
        }
    }
}
