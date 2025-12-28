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
 * Test annotation to automatically clear/reset a specific system property before and after each test.
 * <p>
 * This annotation ensures that system property changes made during a test don't affect other tests.
 * The property is removed before the test runs and restored to its original value after the test completes.
 * <p>
 * <b>Usage on test method:</b>
 * <pre>{@code
 * @Test
 * @ResetSystemProperty("my.custom.property")
 * void testThatModifiesSystemProperty() {
 *     System.setProperty("my.custom.property", "custom");
 *     // Property is automatically reset after this test
 * }
 * }</pre>
 * <p>
 * <b>Usage on test class:</b>
 * <pre>{@code
 * @ResetSystemProperty("my.custom.property")
 * class MySystemPropertyTest {
 *     @Test
 *     void testOne() {
 *         // my.custom.property is reset before and after each test
 *     }
 * }
 * }</pre>
 * <p>
 * <b>Multiple properties:</b>
 * You can use multiple annotations to reset multiple properties:
 * <pre>{@code
 * @ResetSystemProperty("property1")
 * @ResetSystemProperty("property2")
 * class MyTest {
 *     @Test
 *     void testModifyingMultipleProperties() {
 *         System.setProperty("property1", "value1");
 *         System.setProperty("property2", "value2");
 *         // Both properties are reset after this test
 *     }
 * }
 * }</pre>
 *
 * @see ResetSystemPropertyExtension
 * @see System#setProperty(String, String)
 * @see System#clearProperty(String)
 * @author Daniel Felix Ferber
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@ExtendWith(ResetSystemPropertyExtension.class)
public @interface ResetSystemProperty {

    /**
     * The name of the system property to reset.
     * <p>
     * The property will be saved before the test, cleared before test execution,
     * and restored to its original value after the test completes.
     *
     * @return the system property name to reset
     */
    String value();
}

