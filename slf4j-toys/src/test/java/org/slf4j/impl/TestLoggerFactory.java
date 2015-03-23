/*
 * Copyright 2015 Daniel Felix Ferber.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.slf4j.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

/**
 *
 * @author Daniel Felix Ferber
 */
public class TestLoggerFactory implements ILoggerFactory {

    private final Map<String, Logger> nameToLogger = new ConcurrentHashMap<String, Logger>();
    private static final TestLoggerFactory instance = new TestLoggerFactory();

    public static ILoggerFactory getInstance() {
        return instance;
    }

    public Logger getLogger(String name) {
        return nameToLogger.computeIfAbsent(name, new Function<String, Logger>() {
            public Logger apply(String name) {
                return new TestLogger(name);
            }
        });
    }
}