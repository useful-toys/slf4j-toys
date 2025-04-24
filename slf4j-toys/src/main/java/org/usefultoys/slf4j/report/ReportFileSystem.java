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

package org.usefultoys.slf4j.report;

import lombok.Cleanup;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.usefultoys.slf4j.LoggerFactory;
import org.usefultoys.slf4j.utils.UnitFormatter;

import java.io.File;
import java.io.PrintStream;

/**
 * Reports information about the file system roots, including total, free, and usable space.
 */
@SuppressWarnings("NonConstantLogger")
@RequiredArgsConstructor
public class ReportFileSystem implements Runnable {

    private final @NonNull Logger logger;

    @Override
    public void run() {
        @Cleanup
        final PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
        final File[] roots = File.listRoots();
        boolean first = true;
        for (final File root : roots) {
            if (first) {
                first = false;
            } else {
                ps.println();
            }
            ps.printf("File system root: %s%n", root.getAbsolutePath());
            ps.printf(" - total space: %s%n", UnitFormatter.bytes(root.getTotalSpace()));
            ps.printf(" - currently free space: %s (%s usable)%n", UnitFormatter.bytes(root.getFreeSpace()), UnitFormatter.bytes(root.getUsableSpace()));
        }
    }
}
