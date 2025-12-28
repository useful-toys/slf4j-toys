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
import org.usefultoys.slf4j.meter.MeterConfig;
import org.usefultoys.slf4j.utils.ConfigParser;

/**
 * JUnit 5 extension that resets {@link MeterConfig} before and after each test.
 * <p>
 * This extension ensures test isolation by resetting all meter-related configuration state
 * between test executions. It clears:
 * <ul>
 *   <li>{@link ConfigParser} initialization errors</li>
 *   <li>{@link MeterConfig} to default values</li>
 *   <li>{@link SessionConfig} to default values</li>
 *   <li>{@link SystemConfig} to default values</li>
 * </ul>
 * <p>
 * This is essential for tests that modify meter configuration, preventing configuration
 * changes from one test affecting others.
 * <p>
 * <b>Usage:</b>
 * <pre>{@code
 * @ExtendWith(ResetMeterConfigExtension.class)
 * class MeterConfigTest {
 *     @Test
 *     void testCustomMeterConfig() {
 *         MeterConfig.someProperty = "custom";
 *         // MeterConfig is automatically reset after this test
 *     }
 * }
 * }</pre>
 *
 * @see ResetMeterConfig
 * @see MeterConfig
 * @see ConfigParser
 * @author Daniel Felix Ferber
 */
public class ResetMeterConfigExtension implements BeforeEachCallback, AfterEachCallback {

    /**
     * Resets meter configuration before each test execution.
     * <p>
     * Ensures the test starts with clean, default configuration state by resetting all three config levels.
     *
     * @param context the current extension context
     */
    @Override
    public void beforeEach(final ExtensionContext context) {
        // Clear any accumulated parsing errors
        ConfigParser.clearInitializationErrors();
        // Reset all three config levels to their default values
        MeterConfig.init();
        SessionConfig.reset();
        SystemConfig.reset();
    }

    /**
     * Resets meter configuration after each test execution.
     * <p>
     * Ensures configuration changes don't leak to subsequent tests,
     * even if the test fails or throws an exception.
     *
     * @param context the current extension context
     */
    @Override
    public void afterEach(final ExtensionContext context) {
        // Clean up any errors or config changes made during the test
        ConfigParser.clearInitializationErrors();
        // Reset all three config levels to their default values
        MeterConfig.init();
        SessionConfig.reset();
        SystemConfig.reset();
    }
}

