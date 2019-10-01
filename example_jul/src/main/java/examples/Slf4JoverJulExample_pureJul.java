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
package examples;

import org.slf4j.Logger;
import org.usefultoys.slf4j.LoggerConfig;
import org.usefultoys.slf4j.LoggerFactory;
import org.usefultoys.slf4j.internal.SystemConfig;
import org.usefultoys.slf4j.watcher.WatcherConfig;
import org.usefultoys.slf4j.watcher.WatcherSingleton;

import java.io.IOException;
import java.util.logging.LogManager;

/**
 * @author Daniel Felix Ferber
 */
public class Slf4JoverJulExample_pureJul {

    public static final Logger logger = LoggerFactory.getLogger("example");

    static {
        System.setProperty("java.util.logging.config.file",
                Slf4JoverJulExample_pureJul.class.getClassLoader().getResource("log.properties").getFile());
        LoggerConfig.hackJulEnable = true;
        WatcherConfig.delayMilliseconds = 500;
        WatcherConfig.periodMilliseconds = 500;
        SystemConfig.usePlatformManagedBean = true;
    }

    public static void main(final String argv[]) throws IOException {
        LogManager.getLogManager().readConfiguration();

        WatcherSingleton.startDefaultWatcherExecutor();
        runOperation(true);
        WatcherSingleton.stopDefaultWatcherExecutor();
    }

    private static boolean runOperation(boolean expectedResult) {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ex) {
            // ignore
        }
        return expectedResult;
    }
}
