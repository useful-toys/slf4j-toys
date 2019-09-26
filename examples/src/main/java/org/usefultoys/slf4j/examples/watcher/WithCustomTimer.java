/*
 * Copyright 2019 Daniel Felix Ferber
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
package org.usefultoys.slf4j.examples.watcher;

import java.util.Timer;
import java.util.TimerTask;
import org.usefultoys.slf4j.examples.ExampleCommons;
import static org.usefultoys.slf4j.examples.ExampleCommons.doWork;
import org.usefultoys.slf4j.internal.SystemConfig;
import org.usefultoys.slf4j.watcher.WatcherConfig;
import org.usefultoys.slf4j.watcher.WatcherSingleton;

/**
 *
 * @author Daniel Felix Ferber
 */
public class WithCustomTimer {

    static {
        ExampleCommons.configureSLF4J();

        WatcherConfig.delayMilliseconds = 2000;
        WatcherConfig.periodMilliseconds = 1000;
        SystemConfig.useClassLoadingManagedBean = true;
        SystemConfig.useCompilationManagedBean = true;
        SystemConfig.useGarbageCollectionManagedBean = true;
        SystemConfig.useMemoryManagedBean = true;
        SystemConfig.usePlatformManagedBean = true;
    }

    public static void main(final String[] args) {
        /* Create a timer that perodically calls the Watcher to report current status. */
        final Timer timer = new Timer("watcher-timer", true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                WatcherSingleton.DEFAULT_WATCHER.logCurrentStatus();
            }
        }, 1000, 1000);

        try {
            doWork();
        } finally {
            timer.cancel();
        }
    }
}
