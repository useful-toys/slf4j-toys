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
 * Test annotation to automatically reset {@link org.usefultoys.slf4j.report.ReporterConfig} before and after each test.
 * <p>
 * This annotation provides a cleaner alternative to using {@code @ExtendWith(ResetReporterConfig.class)}.
 * It ensures test isolation by resetting all reporter-related configuration state between test executions.
 * This is the most comprehensive reset annotation, resetting all three configuration levels
 * (Reporter, Session, and System).
 * <p>
 * <b>Usage on test class:</b>
 * <pre>{@code
 * @ResetReporter
 * class ReporterConfigTest {
 *     @Test
 *     void testCustomConfig() {
 *         ReporterConfig.someProperty = "custom";
 *         // All configs are automatically reset after this test
 *     }
 * }
 * }</pre>
 * <p>
 * <b>Usage on test method:</b>
 * <pre>{@code
 * class MyTest {
 *     @Test
 *     @ResetReporter
 *     void testThatModifiesReporterConfig() {
 *         // ReporterConfig is reset before and after this test only
 *     }
 * }
 * }</pre>
 *
 * @see ResetReporterConfig
 * @see org.usefultoys.slf4j.report.ReporterConfig
 * @author Daniel Felix Ferber
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@ExtendWith(ResetReporterConfig.class)
public @interface ResetReporter {
}

