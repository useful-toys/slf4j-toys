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
import org.slf4j.ILoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;

/**
 * Static binding for the SLF4J logger factory in the mock logging implementation.
 * <p>
 * This class provides the binding between SLF4J's logging facade and the mock logger factory
 * implementation. It follows the SLF4J binding pattern where a static singleton provides
 * access to the actual logger factory.
 * <p>
 * This class is part of the SLF4J service provider interface and should not be used directly
 * by application code. SLF4J will automatically discover and use this binding when it's
 * present on the classpath.
 *
 * @author Daniel Felix Ferber
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class StaticLoggerBinder implements LoggerFactoryBinder {

    /**
     * The requested SLF4J API version that this binding supports.
     */
    public static final String REQUESTED_API_VERSION = "1.6";

    /**
     * The singleton instance of this binder.
     */
    static StaticLoggerBinder SINGLETON = new StaticLoggerBinder();

    /**
     * Returns the singleton instance of this binder.
     *
     * @return the singleton instance
     */
    public static StaticLoggerBinder getSingleton() {
        return SINGLETON;
    }

    @Override
	public ILoggerFactory getLoggerFactory() {
        return MockLoggerFactory.getInstance();
    }

    @Override
	public String getLoggerFactoryClassStr() {
        return MockLoggerFactory.class.getName();
    }
}
