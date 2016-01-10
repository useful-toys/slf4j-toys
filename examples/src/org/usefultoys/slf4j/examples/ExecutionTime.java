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

import java.util.logging.Level;
import org.slf4j.Logger;
import org.usefultoys.slf4j.LoggerFactory;
import org.usefultoys.slf4j.meter.Meter;
import org.usefultoys.slf4j.meter.MeterFactory;

/**
 *
 * @author daniel
 */
public class ExecutionTime {

    static {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
        System.setProperty("profiler.usePlatformManagedBean", "true");
    }

    public static void main(final String argv[]) {
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                int i = Integer.MIN_VALUE;
                while (! Thread.interrupted()) {
                    i++;
                }
            }
        });
        t1.setDaemon(true);
        t1.start();
        
        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                int i = Integer.MIN_VALUE;
                while (! Thread.interrupted()) {
                    i++;
                }
            }
        });
        t2.setDaemon(true);
        t2.start();
        
        try {
            Thread.sleep(3000L);
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(ExecutionTime.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        example1();
        try {
            example2();
        } catch (RuntimeException e) {

        }
        System.gc();

        example3();
        try {
            example4();
        } catch (RuntimeException e) {

        }
        System.gc();

    }

    private static void example1() {
        final Logger logger = LoggerFactory.getLogger("example");
        final Meter m = MeterFactory.getMeter(logger, "operation").start();
        runOperation();
        m.ok();
    }

    private static void example2() {
        final Logger logger = LoggerFactory.getLogger("example");
        final Meter m = MeterFactory.getMeter(logger, "operation").start();
        failOperation();
        m.ok();
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
