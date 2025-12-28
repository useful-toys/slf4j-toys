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
 * Test annotation to temporarily change the default {@link java.util.Locale} for a test class or method.
 * <p>
 * This annotation ensures that tests involving locale-sensitive operations (like number formatting,
 * date formatting, or string comparisons) run consistently across different environments and operating systems.
 * The original locale is automatically restored after the test completes.
 * <p>
 * <b>Usage on test class:</b>
 * <pre>{@code
 * @WithLocale("en-US")
 * class MyTest {
 *     @Test
 *     void testWithEnglishLocale() {
 *         // All tests in this class use en-US locale
 *     }
 * }
 * }</pre>
 * <p>
 * <b>Usage on test method (overrides class-level annotation):</b>
 * <pre>{@code
 * @WithLocale("en-US")
 * class MyTest {
 *     @Test
 *     @WithLocale("pt-BR")
 *     void testWithPortugueseLocale() {
 *         // This specific test uses pt-BR locale
 *     }
 * }
 * }</pre>
 *
 * @see WithLocaleExtension
 * @see java.util.Locale
 * @author Daniel Felix Ferber
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@ExtendWith(WithLocaleExtension.class)
public @interface WithLocale {

    /**
     * The locale to use during the test, specified in BCP 47 language tag format.
     * <p>
     * Common examples:
     * <ul>
     *   <li>"en" - English</li>
     *   <li>"en-US" - English (United States)</li>
     *   <li>"pt-BR" - Portuguese (Brazil)</li>
     *   <li>"fr-FR" - French (France)</li>
     *   <li>"de-DE" - German (Germany)</li>
     * </ul>
     *
     * @return the BCP 47 language tag for the locale to set
     * @see java.util.Locale#forLanguageTag(String)
     */
    String value();
}