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
package org.usefultoys.slf4j;

import org.slf4j.Logger;
import org.slf4j.Marker;

/**
 * A {@link Logger} implementation that silently discards all log events.
 * <p>
 * Used as a performance optimization when log output is unnecessary (e.g., when the logging level disables output).
 * This avoids the overhead of formatting and I/O operations.
 * <p>
 * Implements the Null Object pattern for SLF4J's {@link Logger} interface.
 * All {@code is...Enabled()} methods return {@code false}, and all logging methods are no-ops.
 * <p>
 * This class is package-private and not intended for use outside this library.
 *
 * @author Daniel Felix Ferber
 * @see <a href="../../../../../doc/TDR-0011-null-object-pattern-for-optional-logging.md">TDR-0011</a>
 */
public class NullLogger implements Logger {

    /* Singleton instance to avoid repeated object creation */
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
    public void trace(final String msg) {
    }

    @Override
    public void trace(final String format, final Object arg) {
    }

    @Override
    public void trace(final String format, final Object arg1, final Object arg2) {
    }

    @Override
    public void trace(final String format, final Object... arguments) {
    }

    @Override
    public void trace(final String msg, final Throwable t) {
    }

    @Override
    public boolean isTraceEnabled(final Marker marker) {
        return false;
    }

    @Override
    public void trace(final Marker marker, final String msg) {
    }

    @Override
    public void trace(final Marker marker, final String format, final Object arg) {
    }

    @Override
    public void trace(final Marker marker, final String format, final Object arg1, final Object arg2) {
    }

    @Override
    public void trace(final Marker marker, final String format, final Object... arguments) {
    }

    @Override
    public void trace(final Marker marker, final String msg, final Throwable t) {
    }

    @Override
    public boolean isDebugEnabled() {
        return false;
    }

    @Override
    public void debug(final String msg) {
    }

    @Override
    public void debug(final String format, final Object arg) {
    }

    @Override
    public void debug(final String format, final Object arg1, final Object arg2) {
    }

    @Override
    public void debug(final String format, final Object... arguments) {
    }

    @Override
    public void debug(final String msg, final Throwable t) {
    }

    @Override
    public boolean isDebugEnabled(final Marker marker) {
        return false;
    }

    @Override
    public void debug(final Marker marker, final String msg) {
    }

    @Override
    public void debug(final Marker marker, final String format, final Object arg) {
    }

    @Override
    public void debug(final Marker marker, final String format, final Object arg1, final Object arg2) {
    }

    @Override
    public void debug(final Marker marker, final String format, final Object... arguments) {
    }

    @Override
    public void debug(final Marker marker, final String msg, final Throwable t) {
    }

    @Override
    public boolean isInfoEnabled() {
        return false;
    }

    @Override
    public void info(final String msg) {
    }

    @Override
    public void info(final String format, final Object arg) {
    }

    @Override
    public void info(final String format, final Object arg1, final Object arg2) {
    }

    @Override
    public void info(final String format, final Object... arguments) {
    }

    @Override
    public void info(final String msg, final Throwable t) {
    }

    @Override
    public boolean isInfoEnabled(final Marker marker) {
        return false;
    }

    @Override
    public void info(final Marker marker, final String msg) {
    }

    @Override
    public void info(final Marker marker, final String format, final Object arg) {
    }

    @Override
    public void info(final Marker marker, final String format, final Object arg1, final Object arg2) {
    }

    @Override
    public void info(final Marker marker, final String format, final Object... arguments) {
    }

    @Override
    public void info(final Marker marker, final String msg, final Throwable t) {
    }

    @Override
    public boolean isWarnEnabled() {
        return false;
    }

    @Override
    public void warn(final String msg) {
    }

    @Override
    public void warn(final String format, final Object arg) {
    }

    @Override
    public void warn(final String format, final Object arg1, final Object arg2) {
    }

    @Override
    public void warn(final String format, final Object... arguments) {
    }

    @Override
    public void warn(final String msg, final Throwable t) {
    }

    @Override
    public boolean isWarnEnabled(final Marker marker) {
        return false;
    }

    @Override
    public void warn(final Marker marker, final String msg) {
    }

    @Override
    public void warn(final Marker marker, final String format, final Object arg) {
    }

    @Override
    public void warn(final Marker marker, final String format, final Object arg1, final Object arg2) {
    }

    @Override
    public void warn(final Marker marker, final String format, final Object... arguments) {
    }

    @Override
    public void warn(final Marker marker, final String msg, final Throwable t) {
    }

    @Override
    public boolean isErrorEnabled() {
        return false;
    }

    @Override
    public void error(final String msg) {
    }

    @Override
    public void error(final String format, final Object arg) {
    }

    @Override
    public void error(final String format, final Object arg1, final Object arg2) {
    }

    @Override
    public void error(final String format, final Object... arguments) {
    }

    @Override
    public void error(final String msg, final Throwable t) {
    }

    @Override
    public boolean isErrorEnabled(final Marker marker) {
        return false;
    }

    @Override
    public void error(final Marker marker, final String msg) {
    }

    @Override
    public void error(final Marker marker, final String format, final Object arg) {
    }

    @Override
    public void error(final Marker marker, final String format, final Object arg1, final Object arg2) {
    }

    @Override
    public void error(final Marker marker, final String format, final Object... arguments) {
    }

    @Override
    public void error(final Marker marker, final String msg, final Throwable t) {
    }
}
