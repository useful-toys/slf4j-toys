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

import java.util.Arrays;
import java.util.Map;
import org.slf4j.Marker;
import org.slf4j.helpers.MessageFormatter;

/**
 *
 * @author Daniel Felix Ferber
 */
public class TestLoggerEvent {

    private final String loggerName;

    private final Level level;
    private final Map<String, String> mdc;
    private final Marker marker;
    private final Throwable throwable;
    private final String message;
    private final Object[] arguments;

    public static enum Level {

        ERROR, WARN, INFO, DEBUG, TRACE
    }

    public TestLoggerEvent(
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

    public String getLoggerName() {
        return loggerName;
    }

    public Level getLevel() {
        return level;
    }

    public Map<String, String> getMdc() {
        return mdc;
    }

    public Marker getMarker() {
        return marker;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public String getMessage() {
        return message;
    }

    public Object[] getArguments() {
        return arguments;
    }

    @Override
    public String toString() {
        return "TestLoggerEvent{" + "loggerName=" + loggerName + ", level=" + level + ", marker=" + marker + ", throwable=" + throwable + ", message=" + message + ", arguments=" + arguments + '}';
    }

    public String getFormattedMessage() {
        return MessageFormatter.arrayFormat(getMessage(), getArguments()).getMessage();
    }

}
