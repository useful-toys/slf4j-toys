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
 * Test annotation to automatically reset {@link org.usefultoys.slf4j.SystemConfig} before and after each test.
 * <p>
 * This annotation provides a cleaner alternative to using {@code @ExtendWith(ResetSystemConfigExtension.class)}.
 * It ensures test isolation by resetting configuration state between test executions.
 * <p>
 * <b>Usage on test class:</b>
 * <pre>{@code
 * @ResetSystem
 * class SystemConfigTest {
 *     @Test
 *     void testCustomConfig() {
 *         SystemConfig.someProperty = "custom";
 *         // Config is automatically reset after this test
 *     }
 * }
 * }</pre>
 * <p>
 * <b>Usage on test method:</b>
 * <pre>{@code
 * class MyTest {
 *     @Test
 *     @ResetSystem
 *     void testThatModifiesSystemConfig() {
 *         // SystemConfig is reset before and after this test only
 *     }
 * }
 * }</pre>
 *
 * @see ResetSystemConfigExtension
 * @see org.usefultoys.slf4j.SystemConfig
 * @author Daniel Felix Ferber
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@ExtendWith(ResetSystemConfigExtension.class)
public @interface ResetSystemConfig {
}

