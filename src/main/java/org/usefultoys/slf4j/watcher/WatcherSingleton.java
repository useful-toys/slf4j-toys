/*
 * Copyright 2026 Daniel Felix Ferber
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
package org.usefultoys.slf4j.watcher;

import lombok.experimental.UtilityClass;

/**
 * Provides the default {@link Watcher} instance for pull-mode execution (e.g., {@link WatcherServlet}).
 * <p>
 * This class no longer manages periodic "push" execution. For scheduled watchers, use
 * {@link WatcherExecutorController} or {@link WatcherTimerController} instead.
 * <p>
 * The default watcher instance is created lazily upon first access and named using
 * {@link WatcherConfig#name}. It is intended only as a temporary compatibility bridge for
 * the servlet pull path; future versions will migrate the servlet away from this global default.
 *
 * @deprecated Use {@link WatcherExecutorController} or {@link WatcherTimerController} for push,
 * or create a dedicated {@link Watcher} instance for pull. This class will be removed once
 * the servlet pull path is migrated.
 * @author Daniel Felix Ferber
 * @see Watcher
 * @see WatcherConfig
 * @see WatcherExecutorController
 * @see WatcherTimerController
 */
@Deprecated
@UtilityClass
public final class WatcherSingleton {

    /**
     * The default watcher instance. It is created lazily upon first access and named using
     * {@link WatcherConfig#name}, which defaults to "watcher".
     */
    private static Watcher DEFAULT_WATCHER_INSTANCE;

    /**
     * Returns the default {@link Watcher} instance, creating it if it hasn't been initialized yet.
     * <p>
     * This method reads {@link WatcherConfig#name} at the moment of first access. Any runtime
     * changes to {@link WatcherConfig#name} made after the first call will not be reflected.
     *
     * @return The default Watcher instance.
     * @deprecated For new code, create a {@link Watcher} or use a {@link WatcherExecutorController}
     * / {@link WatcherTimerController}.
     */
    @Deprecated
    public static synchronized Watcher getDefaultWatcher() {
        if (DEFAULT_WATCHER_INSTANCE == null) {
            DEFAULT_WATCHER_INSTANCE = new Watcher(WatcherConfig.name);
        }
        return DEFAULT_WATCHER_INSTANCE;
    }
}
