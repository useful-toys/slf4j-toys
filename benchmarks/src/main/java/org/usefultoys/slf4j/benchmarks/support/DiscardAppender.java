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
package org.usefultoys.slf4j.benchmarks.support;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

/**
 * A logback appender that discards every event.
 * <p>
 * The point of the benchmarks is to measure the cost that <em>slf4j-toys</em>
 * itself incurs, not the cost of logback's encoder or of writing to disk/console.
 * When a log statement is enabled, {@code Meter}/{@code Watcher} eagerly build the
 * human-readable string ({@code readableMessage()}) and/or the JSON5 data string
 * ({@code json5Message()}) and pass them to SLF4J <em>before</em> any appender runs.
 * That construction cost is therefore captured regardless of the appender.
 * <p>
 * By routing enabled loggers to this discarding appender (with additivity off) we
 * keep that library-side formatting cost while removing the unrelated I/O and
 * pattern-encoding cost of a real appender, so the measurement isolates what an
 * optimization in slf4j-toys can actually change.
 */
public final class DiscardAppender extends AppenderBase<ILoggingEvent> {

    @Override
    protected void append(final ILoggingEvent eventObject) {
        /* Intentionally empty: the event is discarded. The message string was
         * already materialized by the caller, which is the cost we want to keep. */
    }
}
