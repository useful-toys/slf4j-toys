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

import org.slf4j.IMarkerFactory;
import org.slf4j.helpers.BasicMarkerFactory;
import org.slf4j.spi.MarkerFactoryBinder;

public final class StaticMarkerBinder implements MarkerFactoryBinder {

    public static final org.slf4j.impl.StaticMarkerBinder SINGLETON = new org.slf4j.impl.StaticMarkerBinder();

    private final IMarkerFactory markerFactory = new BasicMarkerFactory();

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
