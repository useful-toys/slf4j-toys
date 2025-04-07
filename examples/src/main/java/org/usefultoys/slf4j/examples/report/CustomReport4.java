/*
 * Copyright 2024 Daniel Felix Ferber
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
package org.usefultoys.slf4j.examples.report;

import org.usefultoys.slf4j.report.Reporter;
import org.usefultoys.slf4j.report.ReporterConfig;

import java.util.concurrent.Executor;

/**
 * Example that demonstrates the report generated by {@link Reporter#runDefaultReport()}, with behavior customized via system properties.
 * <p>
 * The report is logged using the logger defined by {@link ReporterConfig#name}. It runs on the current thread (blocking its execution) and logs the reports
 * enabled in {@link ReporterConfig}, as determined by the configured system properties.
 *
 * @author Daniel Felix Ferber
 */
public class CustomReport4 {

    public static void main(String[] args) {
        System.setProperty(ReporterConfig.PROP_SSL_CONTEXT, "true");
        System.setProperty(ReporterConfig.PROP_DEFAULT_TRUST_KEYSTORE, "true");
        Reporter.runDefaultReport();
    }
}
