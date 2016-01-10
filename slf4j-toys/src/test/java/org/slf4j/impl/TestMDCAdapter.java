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

import java.util.HashMap;
import java.util.Map;
import org.slf4j.spi.MDCAdapter;

/**
 *
 * @author Daniel Felix Ferber
 */
public class TestMDCAdapter implements MDCAdapter {

    private final ThreadLocal<Map<String, String>> value = new ThreadLocal<Map<String, String>>() {
        @Override
        protected Map<String, String> initialValue() {
            return new HashMap<String, String>();
        }
    };

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
