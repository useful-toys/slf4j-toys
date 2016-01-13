/*
 * Copyright 2016 x7ws.
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
package org.usefultoys.slf4j.report;

import org.usefultoys.slf4j.internal.Config;

/**
 *
 * @author x7ws
 */
public class ReporterConfig {

    static boolean reportVM = Config.getProperty("slf4jtoys.report.vm", true);
    static boolean reportFileSystem = Config.getProperty("slf4jtoys.report.fileSystem", false);
    static boolean reportMemory = Config.getProperty("slf4jtoys.report.memory", true);
    static boolean reportUser = Config.getProperty("slf4jtoys.report.user", true);
    static boolean reportPhysicalSystem = Config.getProperty("slf4jtoys.report.physicalSystem", true);
    static boolean reportOperatingSystem = Config.getProperty("slf4jtoys.report.operatingSystem", true);
    static boolean reportCalendar = Config.getProperty("slf4jtoys.report.calendar", true);
    static boolean reportLocale = Config.getProperty("slf4jtoys.report.locale", true);
    static boolean reportCharset = Config.getProperty("slf4jtoys.report.charset", true);
    static boolean reportNetworkInterface = Config.getProperty("slf4jtoys.report.networkInterface", false);
}
