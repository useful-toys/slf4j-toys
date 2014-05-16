/*
 * Copyright 2013 Daniel Felix Ferber
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package br.com.danielferber.slf4jtoys.slf4j.profiler;

import br.com.danielferber.slf4jtoys.slf4j.logger.LoggerFactory;
import br.com.danielferber.slf4jtoys.slf4j.profiler.watcher.Watcher;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Profiling session for the current JVM. Stores the UUID logged on each message
 * on the current JVM. Keeps the timer calls the watcher periodically.
 *
 * @author Daniel Felix Ferber
 */
public final class ProfilingSession {

    private ProfilingSession() {
        // prevent instances
    }

    public static final String uuid = UUID.randomUUID().toString().replace("-", "");
    static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    static ScheduledFuture<?> scheduledWatcher;

    public static synchronized void startWatcher() {
        if (scheduledWatcher == null) {
            Watcher watcher = new Watcher(LoggerFactory.getLogger(getProperty("watcher.name", "watcher")));
            scheduledWatcher = executor.scheduleAtFixedRate(watcher, getProperty("watcher.initialDelay", 5), getProperty("watcher.period", 5), TimeUnit.SECONDS);
        }
    }

    public static synchronized void stopWatcher() {
        if (scheduledWatcher != null) {
            scheduledWatcher.cancel(true);
        }
    }
    
    public static  synchronized void startExecutor() {
        if (executor == null) {
            executor = Executors.newSingleThreadScheduledExecutor();
        }
    }
    
    public static synchronized void stopExecutor() {
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }

    public static String getProperty(String name, String defaultValue) {
        String value = System.getProperty(name);
        return value == null ? defaultValue : value;
    }
    
    public static int getProperty(String name, int defaultValue) {
        String value = System.getProperty(name);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
