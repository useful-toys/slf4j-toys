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

import java.io.PrintStream;

import static org.usefultoys.slf4j.report.ReporterConfig.getPropertySafely;

/**
 * Reports properties of the operating system, including architecture, name, version, and path/file/line separators.
 */
@SuppressWarnings("NonConstantLogger")
@RequiredArgsConstructor
public class ReportOperatingSystem implements Runnable {

    private final @NonNull Logger logger;

    @Override
    public void run() {
        @Cleanup
        final PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
        ps.println("Operating System");
        ps.printf(" - architecture: %s%n", getPropertySafely("os.arch"));
        ps.printf(" - name: %s%n", getPropertySafely("os.name"));
        ps.printf(" - version: %s%n", getPropertySafely("os.version"));
        ps.printf(" - file separator: %s%n", Integer.toHexString(getPropertySafely("file.separator").charAt(0)));
        ps.printf(" - path separator: %s%n", Integer.toHexString(getPropertySafely("path.separator").charAt(0)));
        ps.printf(" - line separator: %s%n", Integer.toHexString(getPropertySafely("line.separator").charAt(0)));
    }
}
