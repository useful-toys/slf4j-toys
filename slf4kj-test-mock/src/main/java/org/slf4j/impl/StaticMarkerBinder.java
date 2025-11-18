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
import org.slf4j.IMarkerFactory;
import org.slf4j.helpers.BasicMarkerFactory;
import org.slf4j.spi.MarkerFactoryBinder;

/**
 * Static binding for the SLF4J marker factory in the mock logging implementation.
 * <p>
 * This class provides the binding between SLF4J's marker facade and the marker factory
 * implementation. It uses the standard {@link BasicMarkerFactory} from SLF4J helpers.
 * <p>
 * This class is part of the SLF4J service provider interface and should not be used directly
 * by application code. SLF4J will automatically discover and use this binding when it's
 * present on the classpath.
 *
 * @author Daniel Felix Ferber
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class StaticMarkerBinder implements MarkerFactoryBinder {

    /**
     * The singleton instance of this binder.
     */
    public static final StaticMarkerBinder SINGLETON = new StaticMarkerBinder();

    /**
     * The marker factory instance.
     */
    IMarkerFactory markerFactory = new BasicMarkerFactory();

    /**
     * Private constructor to enforce singleton pattern.
     */
    private StaticMarkerBinder() { }

    @Override
	public IMarkerFactory getMarkerFactory() {
        return markerFactory;
    }

    @Override
	public String getMarkerFactoryClassStr() {
        return BasicMarkerFactory.class.getName();
    }
}
