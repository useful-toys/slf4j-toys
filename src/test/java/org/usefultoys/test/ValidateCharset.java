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

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Test annotation to validate charset consistency before running tests.
 * <p>
 * This annotation ensures that the JVM's default charset matches the charset configured
 * in {@link org.usefultoys.slf4j.SessionConfig}. This validation is critical for tests
 * that involve character encoding operations, file I/O, or string conversions, as charset
 * mismatches can lead to subtle bugs that only appear in certain environments.
 * <p>
 * The validation runs once before all tests in the class and fails fast if there's a mismatch,
 * preventing unreliable test results.
 * <p>
 * <b>Usage on test class:</b>
 * <pre>{@code
 * @ValidateCharset
 * class CharsetSensitiveTest {
 *     @Test
 *     void testFileEncoding() {
 *         // Test runs only if charset is consistent
 *     }
 * }
 * }</pre>
 * <p>
 * <b>Common use cases:</b>
 * <ul>
 *   <li>File I/O operations with character encoding</li>
 *   <li>String to byte array conversions</li>
 *   <li>Network protocol implementations with text data</li>
 *   <li>CSV, JSON, XML parsing tests</li>
 * </ul>
 *
 * @see CharsetConsistencyExtension
 * @see org.usefultoys.slf4j.SessionConfig#charset
 * @see java.nio.charset.Charset#defaultCharset()
 * @author Daniel Felix Ferber
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ExtendWith(CharsetConsistencyExtension.class)
public @interface ValidateCharset {
}

