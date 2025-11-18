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

import java.util.Arrays;
import java.util.Map;

import org.slf4j.Marker;
import org.slf4j.helpers.MessageFormatter;

/**
 * Represents a single log event captured by {@link MockLogger}.
 * <p>
 * This class holds all details of a logging call, including the logger name, log level, marker, original message,
 * arguments, and optional throwable.
 * <p>
 * It also provides a formatted version of the message by applying SLF4J's {@link MessageFormatter}, as well as access
 * to raw data for assertions in test environments.
 * <p>
 * If no throwable is explicitly passed but the last argument is a {@link Throwable}, it is extracted automatically and
 * stored separately.
 * <p>
 * Example usage in tests:
 * <pre>{@code
 * MockLogger logger = ...
 * MockLoggerEvent event = logger.getEvent(0);
 * assertEquals(Level.INFO, event.getLevel());
 * assertTrue(event.getFormattedMessage().contains("Expected text"));
 * }</pre>
 * <p>
 * This class is immutable and thread-safe.
 *
 * @author Daniel Felix Ferber
 */
public class MockLoggerEvent {

    private final String loggerName;

    private final Level level;
    private final Map<String, String> mdc;
    private final Marker marker;
    private final Throwable throwable;
    private final String message;
    private final Object[] arguments;

    public enum Level {

        ERROR, WARN, INFO, DEBUG, TRACE
    }

    /**
     * Constructs a new MockLoggerEvent with the specified parameters.
     * <p>
     * If no throwable is explicitly passed but the last argument is a {@link Throwable},
     * it is extracted automatically and stored separately.
     *
     * @param loggerName the name of the logger
     * @param level      the log level
     * @param mdc        the MDC context map
     * @param marker     the optional marker
     * @param throwable  the optional throwable
     * @param message    the message format string
     * @param arguments  the message arguments
     */
    public MockLoggerEvent(
            final String loggerName,
            final Level level,
            final Map<String, String> mdc,
            final Marker marker,
            final Throwable throwable,
            final String message,
            final Object... arguments) {

        this.loggerName = loggerName;
        this.level = level;
        this.mdc = mdc;
        this.marker = marker;
        this.message = message;
        if (throwable == null && arguments != null && arguments.length > 1) {
            final Object last = arguments[arguments.length - 1];
            if (last instanceof Throwable) {
                this.throwable = (Throwable) last;
                this.arguments = Arrays.copyOfRange(arguments, 0, arguments.length - 1);
            } else {
                this.throwable = throwable;
                this.arguments = arguments;
            }
        } else {
            this.throwable = throwable;
            this.arguments = arguments;
        }
    }

    /**
     * Returns the name of the logger that created this event.
     *
     * @return the logger name
     */
    public String getLoggerName() {
        return loggerName;
    }

    /**
     * Returns the log level of this event.
     *
     * @return the log level
     */
    public Level getLevel() {
        return level;
    }

    /**
     * Returns the MDC context map at the time this event was created.
     *
     * @return the MDC context map, may be null
     */
    public Map<String, String> getMdc() {
        return mdc;
    }

    /**
     * Returns the marker associated with this event.
     *
     * @return the marker, may be null
     */
    public Marker getMarker() {
        return marker;
    }

    /**
     * Returns the throwable associated with this event.
     *
     * @return the throwable, may be null
     */
    public Throwable getThrowable() {
        return throwable;
    }

    /**
     * Returns the original message format string.
     *
     * @return the message format string
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the arguments passed to the logging method.
     *
     * @return the message arguments, may be null or empty
     */
    public Object[] getArguments() {
        return arguments;
    }

    @Override
    public String toString() {
        return "MockLoggerEvent{" + "loggerName=" + loggerName + ", level=" + level + ", marker=" + marker + ", throwable=" + throwable + ", message=" + message + ", arguments=" + Arrays.toString(arguments) + '}';
    }

    /**
     * Returns the formatted message by applying SLF4J's {@link MessageFormatter} to the message and arguments.
     * <p>
     * This method processes the message format string and substitutes placeholders with the provided arguments,
     * following SLF4J's formatting conventions.
     *
     * @return the formatted message string
     */
    public String getFormattedMessage() {
        return MessageFormatter.arrayFormat(message, arguments).getMessage();
    }

}
