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
package org.slf4j.impl;

import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.spi.MDCAdapter;

/**
 * A mock implementation of {@link MDCAdapter} for testing purposes.
 * <p>
 * This adapter provides a thread-local storage for MDC (Mapped Diagnostic Context) data,
 * allowing each thread to maintain its own set of key-value pairs for logging context.
 * <p>
 * The implementation uses {@link ThreadLocal} to ensure thread safety and isolation
 * of MDC data between different threads during test execution.
 * <p>
 * This class is primarily intended for use in unit tests where you need to verify
 * MDC behavior without depending on a full logging implementation.
 *
 * @author Daniel Felix Ferber
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MockMDCAdapter implements MDCAdapter {

    /**
     * Thread-local storage for MDC data. Each thread gets its own map instance.
     */
    ThreadLocal<Map<String, String>> value = ThreadLocal.withInitial(HashMap::new);

    @Override
	public void put(final String key, final String val) {
        value.get().put(key, val);
    }

    @Override
	public String get(final String key) {
        return value.get().get(key);
    }

    @Override
	public void remove(final String key) {
        value.get().remove(key);
    }

    @Override
	public void clear() {
        value.get().clear();
    }

    @Override
	public void setContextMap(final Map<String, String> contextMap) {
        value.set(new HashMap<String, String>(contextMap));
    }

    @Override
	public Map<String, String> getCopyOfContextMap() {
        return new HashMap<String, String>(value.get());
    }

}
