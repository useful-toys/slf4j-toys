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
package org.usefultoys.slf4j.examples.report;

import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.usefultoys.slf4j.report.ReportMemory;
import org.usefultoys.slf4j.report.ReportPhysicalSystem;
import org.usefultoys.slf4j.report.Reporter;
import org.usefultoys.slf4j.report.ReporterConfig;

/**
 * Example that demonstrates generating specific reports manually.
 * <p>
 * The report is logged using the logger defined by {@link org.usefultoys.slf4j.report.ReporterConfig#name}. It runs on the current thread (blocking its
 * execution).
 *
 * @author Daniel Felix Ferber
 */
public class CustomReport3 {

    public static void main(String[] args) {

        final Executor executor = Reporter.sameThreadExecutor;
        final Logger logger = LoggerFactory.getLogger(ReporterConfig.name);
        executor.execute(new ReportPhysicalSystem(logger));
        executor.execute(new ReportMemory(logger));
    }
}
