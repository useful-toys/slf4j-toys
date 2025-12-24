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

package org.usefultoys.test;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.usefultoys.slf4j.SessionConfig;
import org.usefultoys.slf4j.SystemConfig;
import org.usefultoys.slf4j.report.ReporterConfig;
import org.usefultoys.slf4j.utils.ConfigParser;


public class ResetReporterConfig implements BeforeEachCallback, AfterEachCallback {

    @Override
    public void beforeEach(ExtensionContext context) {
        ConfigParser.clearInitializationErrors();
        ReporterConfig.reset();
        SessionConfig.reset();
        SystemConfig.reset();
    }

    @Override
    public void afterEach(ExtensionContext context) {
        ConfigParser.clearInitializationErrors();
        ReporterConfig.reset();
        SessionConfig.reset();
        SystemConfig.reset();
    }
}
