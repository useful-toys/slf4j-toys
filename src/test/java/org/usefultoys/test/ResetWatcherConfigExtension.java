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
import org.usefultoys.slf4j.utils.ConfigParser;
import org.usefultoys.slf4j.watcher.WatcherConfig;

/**
 * JUnit 5 extension that resets {@link WatcherConfig} before and after each test.
 * <p>
 * This extension ensures test isolation by resetting all watcher-related configuration state
 * between test executions. It clears:
 * <ul>
 *   <li>{@link ConfigParser} initialization errors</li>
 *   <li>{@link WatcherConfig} to default values</li>
 * </ul>
 * <p>
 * This is essential for tests that modify watcher configuration, preventing configuration
 * changes from one test affecting others.
 * <p>
 * <b>Usage:</b>
 * <pre>{@code
 * @ExtendWith(ResetWatcherConfigExtension.class)
 * class WatcherConfigTest {
 *     @Test
 *     void testCustomWatcherConfig() {
 *         WatcherConfig.someProperty = "custom";
 *         // WatcherConfig is automatically reset after this test
 *     }
 * }
 * }</pre>
 *
 * @see ResetWatcherConfig
 * @see WatcherConfig
 * @see ConfigParser
 * @author Daniel Felix Ferber
 */
public class ResetWatcherConfigExtension implements BeforeEachCallback, AfterEachCallback {

    /**
     * Resets watcher configuration before each test execution.
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
        WatcherConfig.reset();
        SessionConfig.reset();
        SystemConfig.reset();
    }

    /**
     * Resets watcher configuration after each test execution.
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
        WatcherConfig.reset();
        SessionConfig.reset();
        SystemConfig.reset();
    }
}

