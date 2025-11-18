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

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * A mock implementation of {@link ILoggerFactory} intended for use in unit tests.
 *
 * <p>
 * Provides {@link MockLogger} instances that capture log output in-memory, allowing assertions on logged content
 * without requiring access to external files or consoles.
 * <p>
 * This factory is discovered automatically by SLF4J when present on the test classpath, and should not be referenced or
 * instantiated directly in test code.
 * <p>
 * To use this in tests, ensure the service provider configuration is in place:
 * {@code META-INF/services/org.slf4j.ILoggerFactory} should contain:
 * <pre>
 * org.usefultoys.slf4j.report.MockLoggerFactory
 * </pre>
 * <p>
 * No other SLF4J implementation should be present on the classepath.
 * <p>
 * When configured, all SLF4J logger requests in test code will return {@link MockLogger} instances.
 * 
 *
 * @author Daniel Felix Ferber
 */
public class MockLoggerFactory implements ILoggerFactory {

    private final Map<String, Logger> nameToLogger = new HashMap<String, Logger>();
    private static final MockLoggerFactory instance = new MockLoggerFactory();

    /**
     * Returns the singleton instance of this factory.
     * <p>
     * This method is used by SLF4J's internal binding mechanism and should not be called directly
     * by application code.
     *
     * @return the singleton MockLoggerFactory instance
     */
    public static ILoggerFactory getInstance() {
        return instance;
    }

    @Override
    public Logger getLogger(final String name) {
        Logger logger = nameToLogger.get(name);
        if (logger != null) {
            return logger;
        }
        nameToLogger.put(name, logger = new MockLogger(name));
        return logger;
    }
}
