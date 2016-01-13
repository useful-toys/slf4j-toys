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

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.usefultoys.slf4j.watcher.WatcherSingleton;

/**
 *
 * @author Daniel Felix Ferber
 */
public class DefaultWatcher {

    static Random random = new Random(System.currentTimeMillis());

    static class HeavyCalculation extends Thread {

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

    static {
        /* Customizes the Watcher for more frequent messages for a faster execution
         * of this example. Also enable managed bean that is able to read CPU usage.  */
        System.setProperty("slf4jtoys.usePlatformManagedBean", "true");
        System.setProperty("slf4jtoys.watcher.period", "1s");
        System.setProperty("slf4jtoys.watcher.delay", "1s");
        /* Customizes the SLF4J simple logger to display trace messages that contain
         * encoded and parsable information. Enable additional information that allow better 
         * undestanding of the log output. */
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
        System.setProperty("org.slf4j.simpleLogger.showDateTime", "true");
        System.setProperty("org.slf4j.simpleLogger.showThreadName", "false");
        System.setProperty("org.slf4j.simpleLogger.dateTimeFormat", "yy/MM/dd HH:mm");
    }

    public static void main(final String[] args) {
        WatcherSingleton.startDefaultWatcherExecutor();
        doWork();
        WatcherSingleton.stopDefaultWatcherExecutor();
    }

    static void doWork() {
        /* Start some calculation on separated thread to see CPU usage on Watcher log output. */
        HeavyCalculation c1 = new HeavyCalculation();
        HeavyCalculation c2 = new HeavyCalculation();
        c1.start();
        c2.start();

        try {
            Thread.sleep(10000L);
        } catch (InterruptedException ex) {
            Logger.getLogger(DefaultWatcher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
