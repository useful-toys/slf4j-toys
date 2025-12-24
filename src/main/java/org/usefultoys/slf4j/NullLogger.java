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
package org.usefultoys.slf4j;

import org.slf4j.Logger;
import org.slf4j.Marker;

/**
 * A {@link Logger} implementation that silently discards all log events.
 * <p>
 * This class implements the Null Object pattern for SLF4J's {@link Logger} interface.
 * It is used as a performance optimization when log output is unnecessary (e.g., when the logging level
 * disables output), avoiding the overhead of formatting and I/O operations.
 * <p>
 * All {@code is...Enabled()} methods return {@code false}, and all {@code log()} methods do nothing.
 * <p>
 * This class is package-private and not intended for use outside this library.
 *
 * @author Daniel Felix Ferber
 */
public class NullLogger implements Logger {

    // Singleton instance to avoid repeated object creation
    public static final NullLogger INSTANCE = new NullLogger();

    private NullLogger() {
        // prevent instances outside this library
    }

    @Override
    public String getName() {
        return "NullLogger";
    }

    @Override
    public boolean isTraceEnabled() {
        return false;
    }

    @Override
    public void trace(String msg) {
        // Do nothing
    }

    @Override
    public void trace(String format, Object arg) {
        // Do nothing
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        // Do nothing
    }

    @Override
    public void trace(String format, Object... arguments) {
        // Do nothing
    }

    @Override
    public void trace(String msg, Throwable t) {
        // Do nothing
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return false;
    }

    @Override
    public void trace(Marker marker, String msg) {
        // Do nothing
    }

    @Override
    public void trace(Marker marker, String format, Object arg) {
        // Do nothing
    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        // Do nothing
    }

    @Override
    public void trace(Marker marker, String format, Object... arguments) {
        // Do nothing
    }

    @Override
    public void trace(Marker marker, String msg, Throwable t) {
        // Do nothing
    }

    @Override
    public boolean isDebugEnabled() {
        return false;
    }

    @Override
    public void debug(String msg) {
        // Do nothing
    }

    @Override
    public void debug(String format, Object arg) {
        // Do nothing
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        // Do nothing
    }

    @Override
    public void debug(String format, Object... arguments) {
        // Do nothing
    }

    @Override
    public void debug(String msg, Throwable t) {
        // Do nothing
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return false;
    }

    @Override
    public void debug(Marker marker, String msg) {
        // Do nothing
    }

    @Override
    public void debug(Marker marker, String format, Object arg) {
        // Do nothing
    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        // Do nothing
    }

    @Override
    public void debug(Marker marker, String format, Object... arguments) {
        // Do nothing
    }

    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        // Do nothing
    }

    @Override
    public boolean isInfoEnabled() {
        return false;
    }

    @Override
    public void info(String msg) {
        // Do nothing
    }

    @Override
    public void info(String format, Object arg) {
        // Do nothing
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        // Do nothing
    }

    @Override
    public void info(String format, Object... arguments) {
        // Do nothing
    }

    @Override
    public void info(String msg, Throwable t) {
        // Do nothing
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return false;
    }

    @Override
    public void info(Marker marker, String msg) {
        // Do nothing
    }

    @Override
    public void info(Marker marker, String format, Object arg) {
        // Do nothing
    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        // Do nothing
    }

    @Override
    public void info(Marker marker, String format, Object... arguments) {
        // Do nothing
    }

    @Override
    public void info(Marker marker, String msg, Throwable t) {
        // Do nothing
    }

    @Override
    public boolean isWarnEnabled() {
        return false;
    }

    @Override
    public void warn(String msg) {
        // Do nothing
    }

    @Override
    public void warn(String format, Object arg) {
        // Do nothing
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        // Do nothing
    }

    @Override
    public void warn(String format, Object... arguments) {
        // Do nothing
    }

    @Override
    public void warn(String msg, Throwable t) {
        // Do nothing
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return false;
    }

    @Override
    public void warn(Marker marker, String msg) {
        // Do nothing
    }

    @Override
    public void warn(Marker marker, String format, Object arg) {
        // Do nothing
    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        // Do nothing
    }

    @Override
    public void warn(Marker marker, String format, Object... arguments) {
        // Do nothing
    }

    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        // Do nothing
    }

    @Override
    public boolean isErrorEnabled() {
        return false;
    }

    @Override
    public void error(String msg) {
        // Do nothing
    }

    @Override
    public void error(String format, Object arg) {
        // Do nothing
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        // Do nothing
    }

    @Override
    public void error(String format, Object... arguments) {
        // Do nothing
    }

    @Override
    public void error(String msg, Throwable t) {
        // Do nothing
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return false;
    }

    @Override
    public void error(Marker marker, String msg) {
        // Do nothing
    }

    @Override
    public void error(Marker marker, String format, Object arg) {
        // Do nothing
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        // Do nothing
    }

    @Override
    public void error(Marker marker, String format, Object... arguments) {
        // Do nothing
    }

    @Override
    public void error(Marker marker, String msg, Throwable t) {
        // Do nothing
    }
}
