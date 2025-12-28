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
import org.usefultoys.slf4j.utils.ConfigParser;

/**
 * JUnit 5 extension that clears {@link ConfigParser} initialization errors before and after each test.
 * <p>
 * This extension ensures that tests using {@link ConfigParser} start with a clean state,
 * preventing error accumulation from previous test executions. This is particularly important
 * for tests that intentionally trigger parsing errors to validate error handling behavior.
 * <p>
 * <b>Usage:</b>
 * <pre>{@code
 * @ExtendWith(ClearConfigParser.class)
 * class ConfigParserTest {
 *     @Test
 *     void testInvalidConfig() {
 *         // ConfigParser starts with no accumulated errors
 *     }
 * }
 * }</pre>
 *
 * @see ConfigParser
 * @author Daniel Felix Ferber
 */
public class ClearConfigParserExtension implements BeforeEachCallback, AfterEachCallback {

    /**
     * Clears any accumulated initialization errors before each test execution.
     * <p>
     * This ensures tests start with a clean {@link ConfigParser} state.
     *
     * @param context the current extension context
     */
    @Override
    public void beforeEach(ExtensionContext context) {
        // Clear any errors from previous test runs
        ConfigParser.clearInitializationErrors();
    }

    /**
     * Clears any initialization errors accumulated during test execution.
     * <p>
     * This cleanup ensures that errors from one test don't affect subsequent tests,
     * even if the test fails or throws an exception.
     *
     * @param context the current extension context
     */
    @Override
    public void afterEach(ExtensionContext context) {
        // Clean up errors that may have been accumulated during this test
        ConfigParser.clearInitializationErrors();
    }
}
