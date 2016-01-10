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
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.usefultoys.slf4j.LoggerFactory;

import org.usefultoys.slf4j.Session;
import org.usefultoys.slf4j.watcher.Watcher;

/**
 *
 * @author Daniel Felix Ferber
 */
public class CustomWatcher2 {

    static Random random = new Random(System.currentTimeMillis());

    private static class HeavyCalculation extends Thread {

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


    public static void main(final String[] args) {
        System.setProperty("slf4jtoys.usePlatformManagedBean", "true");
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
        System.setProperty("org.slf4j.simpleLogger.showDateTime", "true");
        System.setProperty("org.slf4j.simpleLogger.showThreadName", "false");
        System.setProperty("org.slf4j.simpleLogger.dateTimeFormat", "yy/MM/dd HH:mm");

        // Custom watcher
        final Watcher watcher = new Watcher(LoggerFactory.getLogger("customwatcher"));
        // Platform specific periodic scheduled job to call watcher.
        final Timer timer = new Timer("timer", true);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    watcher.logCurrentStatus();
                }
            }, 1000, 1000);

        try {
            DefaultWatcher.doWork();
        } finally {
            timer.cancel();
        }
    }
}
