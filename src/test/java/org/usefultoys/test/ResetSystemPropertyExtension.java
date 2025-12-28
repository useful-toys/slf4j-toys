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

/**
 * JUnit 5 extension that implements the {@link ResetSystemProperty} annotation behavior.
 * <p>
 * This extension ensures that system properties specified by {@link ResetSystemProperty}
 * annotations do not exist before or after each test. This guarantees that tests start
 * with a clean state and don't leave side effects on system properties.
 * <p>
 * <b>Implementation notes:</b>
 * <ul>
 *   <li>Supports multiple {@code @ResetSystemProperty} annotations on the same test</li>
 *   <li>Works with both class-level and method-level annotations</li>
 *   <li>Simply clears properties before and after test execution</li>
 * </ul>
 *
 * @see ResetSystemProperty
 * @author Daniel Felix Ferber
 */
public class ResetSystemPropertyExtension implements BeforeEachCallback, AfterEachCallback {

    /**
     * Clears system properties before each test.
     * <p>
     * Removes all system properties specified by {@link ResetSystemProperty} annotations
     * from both the test method and test class. This ensures tests start with clean state.
     *
     * @param context the current extension context
     */
    @Override
    public void beforeEach(ExtensionContext context) {
        // Clear method-level annotated properties
        context.getTestMethod().ifPresent(method -> {
            final ResetSystemProperty[] annotations = method.getAnnotationsByType(ResetSystemProperty.class);
            for (final ResetSystemProperty annotation : annotations) {
                System.clearProperty(annotation.value());
            }
        });

        // Clear class-level annotated properties
        context.getTestClass().ifPresent(testClass -> {
            final ResetSystemProperty[] annotations = testClass.getAnnotationsByType(ResetSystemProperty.class);
            for (final ResetSystemProperty annotation : annotations) {
                System.clearProperty(annotation.value());
            }
        });
    }

    /**
     * Clears system properties after each test.
     * <p>
     * Removes all system properties specified by {@link ResetSystemProperty} annotations
     * from both the test method and test class. This ensures no side effects leak to other tests.
     *
     * @param context the current extension context
     */
    @Override
    public void afterEach(ExtensionContext context) {
        // Clear method-level annotated properties
        context.getTestMethod().ifPresent(method -> {
            final ResetSystemProperty[] annotations = method.getAnnotationsByType(ResetSystemProperty.class);
            for (final ResetSystemProperty annotation : annotations) {
                System.clearProperty(annotation.value());
            }
        });

        // Clear class-level annotated properties
        context.getTestClass().ifPresent(testClass -> {
            final ResetSystemProperty[] annotations = testClass.getAnnotationsByType(ResetSystemProperty.class);
            for (final ResetSystemProperty annotation : annotations) {
                System.clearProperty(annotation.value());
            }
        });
    }
}

