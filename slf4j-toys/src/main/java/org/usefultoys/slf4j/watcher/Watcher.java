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
package org.usefultoys.slf4j.watcher;

import java.io.File;
import java.io.PrintStream;
import org.slf4j.Logger;
import org.usefultoys.slf4j.LoggerFactory;
import org.usefultoys.slf4j.ProfilingSession;
import org.usefultoys.slf4j.utils.UnitFormatter;

/**
 *
 * @author Daniel Felix Ferber
 */
public class Watcher extends WatcherData implements Runnable {

    private static final long serialVersionUID = 1L;

    transient private final Logger logger;

    public Watcher(final Logger logger) {
        super();
        this.logger = logger;
        this.sessionUuid = ProfilingSession.uuid;
        this.eventPosition = 0;
        this.eventCategory = logger.getName();
    }

    @Override
    public void run() {
        time = System.nanoTime();
        eventPosition++;

        if (logger.isInfoEnabled()) {
            collectRuntimeStatus();
            collectPlatformStatus();
            collectManagedBeanStatus();
            logger.info(readableString(new StringBuilder()).toString());
        }
        if (logger.isTraceEnabled()) {
            logger.trace(Slf4JMarkers.WATCHER, write(new StringBuilder(), 'W').toString());
        }
    }

    public void systemReport() {
        PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
        final Runtime runtime = Runtime.getRuntime();
        ps.println("System report:");
        ps.println();

        ps.println("Number of processors available to the JVM (cores): " + runtime.availableProcessors());
        ps.println();

        ps.println("Memory:");
        long maxMemory = runtime.maxMemory();
        final long totalMemory = runtime.totalMemory();
        final long freeMemory = runtime.freeMemory();
        ps.println(" - Maximum allowed (bytes): " + (maxMemory == Long.MAX_VALUE ? "no limit" : UnitFormatter.bytes(maxMemory)));
        ps.println(" - Currently allocated (bytes): " + UnitFormatter.bytes(totalMemory));
        ps.println("    - used (bytes): " + UnitFormatter.bytes(totalMemory - freeMemory));
        ps.println("    - free (bytes): " + UnitFormatter.bytes(freeMemory));
        ps.println();

        File[] roots = File.listRoots();
        for (File root : roots) {
            ps.println("File system root: " + root.getAbsolutePath());
            ps.println(" - Total space (bytes): " + UnitFormatter.bytes(root.getTotalSpace()));
            ps.println(" - Currently free space (bytes): " + UnitFormatter.bytes(root.getFreeSpace()));
            ps.println("    - usable (bytes): " + UnitFormatter.bytes(root.getUsableSpace()));
            ps.println();
        }
        ps.close();
    }
}
