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
import org.usefultoys.slf4jtestmock.AssertLogger;
import org.usefultoys.slf4jtestmock.MockLoggerExtension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Test annotation to enable automatic MockLogger management via {@link MockLoggerExtension}.
 * <p>
 * This annotation simplifies the use of {@link MockLoggerExtension} by automatically registering
 * the extension. Use this annotation on test classes that have Logger fields to enable automatic
 * MockLogger injection and lifecycle management via the `@Slf4jMock` field annotation.
 * <p>
 * The extension automatically:
 * <ul>
 *   <li>Initializes MockLogger instances for fields annotated with `@Slf4jMock`</li>
 *   <li>Enables the MockLogger with `setEnabled(true)` before each test</li>
 *   <li>Resets (clears events) before and after each test</li>
 * </ul>
 * <p>
 * <b>Usage:</b>
 * <pre>{@code
 * @WithMockLogger
 * @ValidateCharset
 * class MyTestClass {
 *     @Slf4jMock
 *     private Logger logger;
 *
 *     @Test
 *     void testSomething() {
 *         // logger is automatically initialized and managed
 *         // Use AssertLogger for assertions on log events
 *     }
 * }
 * }</pre>
 *
 * @see MockLoggerExtension
 * @see AssertLogger
 * @author Daniel Felix Ferber
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ExtendWith(MockLoggerExtension.class)
public @interface WithMockLogger {
}

