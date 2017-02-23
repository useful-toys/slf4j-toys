/**
 * Copyright 2017 Daniel Felix Ferber
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
package org.usefultoys.slf4j;

import org.usefultoys.slf4j.report.Reporter;

import java.util.UUID;
import java.util.concurrent.Executor;

/**
 * Profiling session for the current JVM.
 *
 * @author Daniel Felix Ferber
 */
public final class Session {

    private Session() {
        // prevent instances
    }

    /**
     * UUID of the current SLF4J-Toys instance. This UUID is added to all trace messages. It allows to distinguish messages from different JVM
     * instances when log files are shared. Value is assigned at application startup and cannot be changed at runtime.
     */
    public static final String uuid = UUID.randomUUID().toString().replace("-", "");

    /**
     * Runs the default report on the current thread. Intended for simple architectures. May not be suitable for JavaEE environments that do not allow
     * blocking threads for extended amount of time.
     */
    public static void runDefaultReport() {
        final Executor noThreadExecutor = new Executor() {
            @Override
            public void execute(Runnable command) {
                command.run();
            }
        };
        new Reporter().logDefaultReports(noThreadExecutor);
    }
}
