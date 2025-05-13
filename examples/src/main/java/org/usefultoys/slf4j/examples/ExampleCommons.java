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

import org.usefultoys.slf4j.examples.watcher.WithDefaultExecutor;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author x7ws
 */
public final class ExampleCommons {

    public static Random random = new Random(System.currentTimeMillis());

    /**
     * Customizes the SLF4J simple logger to display trace messages that contain detailed and parsable information. Enable additional information that
     * allow better undestanding of the log output.
     */
    public static void configureSLF4J() {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
        System.setProperty("org.slf4j.simpleLogger.showDateTime", "true");
        System.setProperty("org.slf4j.simpleLogger.showThreadName", "false");
        System.setProperty("org.slf4j.simpleLogger.dateTimeFormat", "yy/MM/dd HH:mm");
    }

    public static class HeavyCalculation extends Thread {

        {
            setDaemon(true);
        }

        @Override
        public void run() {
            int i = Integer.MIN_VALUE;
            while (!Thread.interrupted()) {
                i++;
            }
        }
    };

    public static void doWork() {
        /* Start some calculation on separated thread to see CPU usage on Watcher log output. */
        final HeavyCalculation c1 = new HeavyCalculation();
        final HeavyCalculation c2 = new HeavyCalculation();
        c1.start();
        c2.start();

        try {
            Thread.sleep(10000L);
        } catch (final InterruptedException ex) {
            Logger.getLogger(WithDefaultExecutor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
