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

import br.com.danielferber.slf4jtoys.slf4j.profiler.watcher.Watcher;
import java.util.Timer;
import java.util.UUID;
import org.slf4j.Logger;

/**
 * Profiling session for the current JVM. Stores the UUID logged on each message
 * on the current JVM. Keeps the timer calls the watcher periodically.
 *
 * @author Daniel Felix Ferber
 */
public class ProfilingSession {
    private static Logger logger;

    private ProfilingSession() {
        // prevent instances
    }

    public static final String uuid = UUID.randomUUID().toString().replace('-', '.');
    public static final Timer timer = new Timer("br.com.danielferber.slf4jtoys.slf4j");

    public Watcher start() {
        logger.info("Watcher started. uuid={}", uuid);
        if (logger.isInfoEnabled()) {
            try {
                ProfilingSession.timer.scheduleAtFixedRate(watcherTask, 1000, 1000);
            } catch (IllegalStateException e) {
                /* WatcherTask já estava programada. */
            }
        }
        return this;
    }

    public Watcher stop() {
        watcherTask.cancel();
        logger.info("Watcher stopped. uuid={}", uuid);
        return this;
    }
}
