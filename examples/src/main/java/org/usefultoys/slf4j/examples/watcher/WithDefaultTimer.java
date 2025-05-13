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
package org.usefultoys.slf4j.examples.watcher;

import org.usefultoys.slf4j.SystemConfig;
import org.usefultoys.slf4j.examples.ExampleCommons;
import org.usefultoys.slf4j.watcher.WatcherConfig;
import org.usefultoys.slf4j.watcher.WatcherSingleton;

import static org.usefultoys.slf4j.examples.ExampleCommons.doWork;

/**
 *
 * @author Daniel Felix Ferber
 */
public final class WithDefaultTimer {

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
        WatcherSingleton.startDefaultWatcherTimer();
        try {
            doWork();
        } finally {
            WatcherSingleton.stopDefaultWatcherTimer();
        }
    }

}
