/*
 * Copyright 2016 x7ws.
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

import org.usefultoys.slf4j.internal.Config;

/**
 *
 * @author x7ws
 */
public class WatcherConfig {
    public static long delay = Config.getMillisecondsProperty("slf4jtoys.watcher.delay", 60000L);
    public static long period = Config.getMillisecondsProperty("slf4jtoys.watcher.period", 600000L);
}
