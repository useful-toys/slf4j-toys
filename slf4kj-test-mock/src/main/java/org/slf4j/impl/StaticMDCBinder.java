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
import org.slf4j.spi.MDCAdapter;

/**
 * Static binding for the SLF4J MDC (Mapped Diagnostic Context) adapter in the mock logging implementation.
 * <p>
 * This class provides the binding between SLF4J's MDC facade and the mock MDC adapter implementation.
 * It follows the SLF4J binding pattern where a static singleton provides access to the actual
 * MDC implementation.
 * <p>
 * This class is part of the SLF4J service provider interface and should not be used directly
 * by application code.
 *
 * @author Daniel Felix Ferber
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class StaticMDCBinder {

    /**
     * The singleton instance of this binder.
     */
    public static final StaticMDCBinder SINGLETON = new StaticMDCBinder();

    /**
     * The mock MDC adapter instance.
     */
    MockMDCAdapter mockMDCAdapter = new MockMDCAdapter();

    /**
     * Private constructor to enforce singleton pattern.
     */
    private StaticMDCBinder() {
    }

    /**
     * Returns the MDC adapter instance.
     *
     * @return the MDC adapter
     */
    public MDCAdapter getMDCA() {
        return mockMDCAdapter;
    }

    /**
     * Returns the class name of the MDC adapter.
     *
     * @return the fully qualified class name of the MDC adapter
     */
    public String getMDCAdapterClassStr() {
        return MockMDCAdapter.class.getName();
    }
}
