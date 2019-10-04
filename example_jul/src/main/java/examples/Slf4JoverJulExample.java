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
import org.usefultoys.slf4j.LoggerFactory;
import org.usefultoys.slf4j.SessionConfig;
import org.usefultoys.slf4j.SystemConfig;
import org.usefultoys.slf4j.meter.Meter;
import org.usefultoys.slf4j.meter.MeterConfig;
import org.usefultoys.slf4j.meter.MeterFactory;
import org.usefultoys.slf4j.watcher.WatcherConfig;
import org.usefultoys.slf4j.watcher.WatcherSingleton;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.logging.LogManager;

/**
 * Test SLF4J Meter using JUL as underlying framework.
 * This examample demonstrates recommended logging setting for JUL.
 *
 * @author Daniel Felix Ferber
 */
public class Slf4JoverJulExample {

    public static final Logger logger = LoggerFactory.getLogger("example");

    static {
        java.util.Locale.setDefault(Locale.ENGLISH);
        System.setProperty("java.util.logging.config.file",
                Slf4JoverJulExample.class.getClassLoader().getResource("log.properties").getFile());
        SessionConfig.uuidSize = 6;
        WatcherConfig.delayMilliseconds = 1000;
        WatcherConfig.periodMilliseconds = 500;
        SystemConfig.useClassLoadingManagedBean = true;
        SystemConfig.useCompilationManagedBean = true;
        SystemConfig.useGarbageCollectionManagedBean = true;
        SystemConfig.useMemoryManagedBean = true;
        SystemConfig.usePlatformManagedBean = true;
        MeterConfig.progressPeriodMilliseconds=300;
    }

    public static void main(final String argv[]) throws IOException {
        InputStream inputStream = Slf4JoverJulExample.class.getResourceAsStream("/log.properties");
        try {
            LogManager.getLogManager().readConfiguration(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        WatcherSingleton.startDefaultWatcherExecutor();
        runOperation(true);
        WatcherSingleton.stopDefaultWatcherExecutor();
    }

    private static boolean runOperation(boolean expectedResult) {
        try (Meter m = MeterFactory.getMeter(logger, "runOperation").iterations(3).start()) {
            Thread.sleep(1000);
            m.inc().progress();
            Thread.sleep(1000);
            m.inc().progress();
            Thread.sleep(1000);
            m.inc().ok();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        return expectedResult;
    }
}
