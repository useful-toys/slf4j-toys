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
package org.usefultoys.slf4j.watcher;

import org.usefultoys.slf4j.internal.SystemData;
import org.usefultoys.slf4j.utils.UnitFormatter;

/**
 *
 * @author Daniel Felix Ferber
 */
public class WatcherData extends SystemData {

    private static final long serialVersionUID = 1L;

    protected WatcherData() {
        super();
    }

    @Override
    public StringBuilder readableString(final StringBuilder builder) {
        if (this.runtime_usedMemory > 0 || this.runtime_maxMemory > 0 || this.runtime_totalMemory > 0) {
            builder.append("Memory: ");
            builder.append(UnitFormatter.bytes(this.runtime_usedMemory));
            builder.append(' ');
            builder.append(UnitFormatter.bytes(this.runtime_totalMemory));
            builder.append(' ');
            builder.append(UnitFormatter.bytes(this.runtime_maxMemory));
        } else {
            builder.append("No memory status.");
        }
        return builder;
    }
}