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
package org.usefultoys.slf4j.examples;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.usefultoys.slf4j.LoggerFactory;
import org.usefultoys.slf4j.Session;
import org.usefultoys.slf4j.report.Reporter;

/**
 * Demonstrates the default report generated by
 * {@link Session#runDefaultReport()}.
 *
 * @author Daniel Felix Ferber
 */
public class CustomReport1 {

    public static void main(String[] args) {
//        The default report may be customized with system properties, for example:
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");

        Executor executor = Executors.newFixedThreadPool(3);
        Reporter reporter = new Reporter(LoggerFactory.getLogger("customreport"));
        reporter.logDefaultReports(executor);
    }
}
