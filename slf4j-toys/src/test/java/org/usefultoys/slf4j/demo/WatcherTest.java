/*
 * Copyright 2017 Daniel Felix Ferber
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
package org.usefultoys.slf4j.demo;

import org.junit.Test;
import org.usefultoys.slf4j.meter.Meter;
import org.usefultoys.slf4j.meter.MeterFactory;
import org.usefultoys.slf4j.watcher.Watcher;
import org.usefultoys.slf4j.watcher.WatcherConfig;
import org.usefultoys.slf4j.watcher.WatcherSingleton;

/**
 *
 * @author Daniel Felix Ferber
 */
public class WatcherTest {
    static {
        WatcherConfig.delayMilliseconds = 500;
        WatcherConfig.periodMilliseconds = 500;
        WatcherConfig.dataPrefix = "a.";
        WatcherConfig.dataSuffix = ".b";
        WatcherConfig.name = "mywatcher";
        WatcherConfig.dataIncludeUuid = false;
    }

    @Test
    public void test1() {
        WatcherSingleton.startDefaultWatcherExecutor();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        WatcherSingleton.stopDefaultWatcherExecutor();
    }

    @Test
    public void test2() {
        WatcherSingleton.startDefaultWatcherTimer();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        WatcherSingleton.stopDefaultWatcherTimer();
    }

}
