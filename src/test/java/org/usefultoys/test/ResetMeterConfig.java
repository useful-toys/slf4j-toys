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
 * Test annotation to automatically reset {@link org.usefultoys.slf4j.meter.MeterConfig}
 * before and after each test.
 * <p>
 * This annotation ensures test isolation by resetting Meter configuration state
 * between test executions. It clears:
 * <ul>
 *   <li>{@link org.usefultoys.slf4j.utils.ConfigParser} initialization errors</li>
 *   <li>{@link org.usefultoys.slf4j.meter.MeterConfig} to default values</li>
 *   <li>{@link org.usefultoys.slf4j.SessionConfig} to default values</li>
 *   <li>{@link org.usefultoys.slf4j.SystemConfig} to default values</li>
 * </ul>
 * <p>
 * This is essential for tests that modify meter configuration, preventing configuration
 * changes from one test affecting others.
 * <p>
 * <b>Usage:</b>
 * <pre>{@code
 * @ResetMeterConfig
 * class MeterConfigTest {
 *     @Test
 *     void testCustomMeterConfig() {
 *         MeterConfig.someProperty = "custom";
 *         // MeterConfig is automatically reset after this test
 *     }
 * }
 * }</pre>
 *
 * @see ResetMeterConfigExtension
 * @see org.usefultoys.slf4j.meter.MeterConfig
 * @author Daniel Felix Ferber
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ExtendWith(ResetMeterConfigExtension.class)
public @interface ResetMeterConfig {
}

