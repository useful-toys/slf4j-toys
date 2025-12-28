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
 * Test annotation to automatically reset {@link org.usefultoys.slf4j.watcher.WatcherConfig}
 * before and after each test.
 * <p>
 * This annotation ensures test isolation by resetting Watcher configuration state
 * between test executions. It clears:
 * <ul>
 *   <li>{@link org.usefultoys.slf4j.utils.ConfigParser} initialization errors</li>
 *   <li>{@link org.usefultoys.slf4j.watcher.WatcherConfig} to default values</li>
 * </ul>
 * <p>
 * This is essential for tests that modify watcher configuration, preventing configuration
 * changes from one test affecting others.
 * <p>
 * <b>Usage:</b>
 * <pre>{@code
 * @ResetWatcherConfig
 * class WatcherConfigTest {
 *     @Test
 *     void testCustomWatcherConfig() {
 *         WatcherConfig.someProperty = "custom";
 *         // WatcherConfig is automatically reset after this test
 *     }
 * }
 * }</pre>
 *
 * @see ResetWatcherConfigExtension
 * @see org.usefultoys.slf4j.watcher.WatcherConfig
 * @author Daniel Felix Ferber
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ExtendWith(ResetWatcherConfigExtension.class)
public @interface ResetWatcherConfig {
}

