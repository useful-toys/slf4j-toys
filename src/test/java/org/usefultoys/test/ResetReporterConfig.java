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

/**
 * JUnit 5 extension that resets {@link ReporterConfig} and related configurations before and after each test.
 * <p>
 * This extension ensures test isolation by resetting all reporter-related configuration state
 * between test executions. It clears:
 * <ul>
 *   <li>{@link ConfigParser} initialization errors</li>
 *   <li>{@link ReporterConfig} to default values</li>
 *   <li>{@link SessionConfig} to default values</li>
 *   <li>{@link SystemConfig} to default values</li>
 * </ul>
 * <p>
 * This is essential for tests that modify reporter configuration, preventing configuration
 * changes from one test affecting others. This is the most comprehensive reset extension,
 * resetting all three configuration levels.
 * <p>
 * <b>Usage:</b>
 * <pre>{@code
 * @ExtendWith(ResetReporterConfig.class)
 * class ReporterConfigTest {
 *     @Test
 *     void testCustomReporter() {
 *         ReporterConfig.someProperty = "custom";
 *         // All configs are automatically reset after this test
 *     }
 * }
 * }</pre>
 *
 * @see ReporterConfig
 * @see SessionConfig
 * @see SystemConfig
 * @see ConfigParser
 * @author Daniel Felix Ferber
 */
public class ResetReporterConfig implements BeforeEachCallback, AfterEachCallback {

    /**
     * Resets all configuration before each test execution.
     * <p>
     * Ensures the test starts with clean, default configuration state across
     * all three configuration levels (Reporter, Session, System).
     *
     * @param context the current extension context
     */
    @Override
    public void beforeEach(ExtensionContext context) {
        // Clear any accumulated parsing errors
        ConfigParser.clearInitializationErrors();
        // Reset all three config levels to their default values
        ReporterConfig.reset();
        SessionConfig.reset();
        SystemConfig.reset();
    }

    /**
     * Resets all configuration after each test execution.
     * <p>
     * Ensures configuration changes don't leak to subsequent tests,
     * even if the test fails or throws an exception.
     *
     * @param context the current extension context
     */
    @Override
    public void afterEach(ExtensionContext context) {
        // Clean up any errors or config changes made during the test
        ConfigParser.clearInitializationErrors();
        ReporterConfig.reset();
        SessionConfig.reset();
        SystemConfig.reset();
    }
}
