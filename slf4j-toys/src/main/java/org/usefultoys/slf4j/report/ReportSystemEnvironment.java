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
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Reports environment variables available to the current process.
 */
@SuppressWarnings("NonConstantLogger")
@RequiredArgsConstructor
public class ReportSystemEnvironment implements Runnable {

    private final @NonNull Logger logger;

    @Override
    public void run() {
        @Cleanup
        final PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
        final SortedMap<String, String> sortedProperties;
        try {
            sortedProperties = new TreeMap<>(System.getenv());
        } catch (final SecurityException ignored) {
            ps.println("System Environment: access denied");
            return;
        }
        ps.println("System Environment:");
        for (final Map.Entry<String, String> entry : sortedProperties.entrySet()) {
            ps.printf(" - %s: %s%n", entry.getKey(), entry.getValue());
        }
    }
}
