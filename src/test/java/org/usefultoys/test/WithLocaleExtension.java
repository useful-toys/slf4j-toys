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
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;

import java.util.Locale;
import java.util.Optional;

/**
 * JUnit 5 extension that implements the {@link WithLocale} annotation behavior.
 * <p>
 * This extension temporarily changes the default {@link Locale} for tests, ensuring that
 * locale-sensitive operations produce consistent results across different environments.
 * The original locale is saved before each test and restored after each test completes.
 * <p>
 * <b>Precedence rules:</b>
 * <ul>
 *   <li>Method-level {@code @WithLocale} takes precedence over class-level annotation</li>
 *   <li>If no annotation is found, an {@link IllegalStateException} is thrown</li>
 * </ul>
 * <p>
 * <b>Thread safety:</b> This extension uses JUnit's {@link ExtensionContext.Store} to isolate
 * locale changes per test class, preventing interference between concurrent test executions.
 *
 * @see WithLocale
 * @author Daniel Felix Ferber
 */
public class WithLocaleExtension implements BeforeEachCallback, AfterEachCallback {

    /** Namespace used to store extension data in JUnit's ExtensionContext.Store */
    private static final Namespace NAMESPACE =
            Namespace.create(WithLocaleExtension.class);

    /** Key used to store the original locale in the extension store */
    private static final String ORIGINAL_LOCALE_KEY = "originalLocale";

    /**
     * Saves the current default locale and sets a new locale based on the {@link WithLocale} annotation.
     * <p>
     * This method is called before each test method execution.
     *
     * @param context the current extension context
     * @throws IllegalStateException if no {@link WithLocale} annotation is found on the test method or class
     */
    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        // Save the original locale to restore it later
        Locale original = Locale.getDefault();
        getStore(context).put(ORIGINAL_LOCALE_KEY, original);

        // Find the desired locale (method annotation takes precedence over class annotation)
        Optional<WithLocale> withLocale = findWithLocaleAnnotation(context);

        if (withLocale.isPresent()) {
            // Parse the BCP 47 language tag and set as default locale
            Locale newLocale = Locale.forLanguageTag(withLocale.get().value());
            Locale.setDefault(newLocale);
        } else {
            // Extension is misconfigured - annotation must be present
            throw new IllegalStateException(
                    "@WithLocale annotation not found on test method or class.");
        }
    }

    /**
     * Restores the original default locale that was saved before the test.
     * <p>
     * This method is called after each test method execution, ensuring the locale
     * is properly reset even if the test fails.
     *
     * @param context the current extension context
     */
    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        Store store = getStore(context);
        // Remove and retrieve the saved locale
        Locale original = store.remove(ORIGINAL_LOCALE_KEY, Locale.class);

        if (original != null) {
            // Restore the original locale
            Locale.setDefault(original);
        }
    }

    /**
     * Gets the extension's private storage from the current test context.
     * <p>
     * Uses a class-specific namespace to prevent conflicts with other extensions
     * or concurrent test executions.
     *
     * @param context the current extension context
     * @return the extension's private store
     */
    private Store getStore(ExtensionContext context) {
        return context.getStore(NAMESPACE);
    }

    /**
     * Finds the {@link WithLocale} annotation on the current test method or class.
     * <p>
     * Search priority:
     * <ol>
     *   <li>Test method annotation (highest priority)</li>
     *   <li>Test class annotation (fallback)</li>
     * </ol>
     *
     * @param context the current extension context
     * @return an {@link Optional} containing the annotation if found, or empty if not found
     */
    private Optional<WithLocale> findWithLocaleAnnotation(ExtensionContext context) {
        // 1) First, check if the annotation is on the test method
        Optional<WithLocale> methodAnnotation = context.getTestMethod()
                .map(m -> m.getAnnotation(WithLocale.class));
        if (methodAnnotation.isPresent()) {
            return methodAnnotation;
        }

        // 2) If not on method, check if it's on the test class
        return context.getTestClass()
                .map(c -> c.getAnnotation(WithLocale.class));
    }
}
