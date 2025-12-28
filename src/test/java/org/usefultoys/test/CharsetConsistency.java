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

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.usefultoys.slf4j.SessionConfig;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * JUnit 5 extension that validates charset consistency before running tests.
 * <p>
 * This extension ensures that the JVM's default charset matches the charset configured
 * in {@link SessionConfig}. This validation is critical for tests that involve character
 * encoding operations, file I/O, or string conversions, as charset mismatches can lead to
 * subtle bugs that only appear in certain environments.
 * <p>
 * The extension runs once before all tests in the class and fails fast if there's a mismatch,
 * preventing unreliable test results.
 * <p>
 * <b>Usage:</b>
 * <pre>{@code
 * @ExtendWith(CharsetConsistency.class)
 * class MyCharsetSensitiveTest {
 *     @Test
 *     void testStringEncoding() {
 *         // Test runs only if default charset matches SessionConfig.charset
 *     }
 * }
 * }</pre>
 *
 * @see SessionConfig#charset
 * @see Charset#defaultCharset()
 * @author Daniel Felix Ferber
 */
public class CharsetConsistency implements BeforeAllCallback {

    /**
     * Validates that the default charset matches {@link SessionConfig#charset}.
     * <p>
     * This check runs once before all tests in the class. If the charsets don't match,
     * the test class fails immediately with a descriptive error message.
     *
     * @param context the current extension context
     * @throws AssertionError if the default charset doesn't match SessionConfig.charset
     */
    @Override
    public void beforeAll(ExtensionContext context) {
        // Fail fast if charset mismatch detected - prevents unreliable test results
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset,
                "Test requires SessionConfig.charset = default charset");
    }
}
