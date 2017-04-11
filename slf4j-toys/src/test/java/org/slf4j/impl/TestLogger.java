/*
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
package org.slf4j.impl;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.impl.TestLoggerEvent.Level;

/**
 * @author Daniel Felix Ferber
 */
public class TestLogger implements Logger {

    private final String name;
    private boolean traceEnabled = true;
    private boolean debugEnabled = true;
    private boolean infoEnabled = true;
    private boolean warnEnabled = true;
    private boolean errorEnabled = true;

    private final List<TestLoggerEvent> loggerEvents = new ArrayList<TestLoggerEvent>();

    public TestLogger(final String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isTraceEnabled() {
        return traceEnabled;
    }

    private void addEvent(final TestLoggerEvent event) {
        loggerEvents.add(event);
    }

    public void clearEvents() {
        loggerEvents.clear();
    }

    public void setTraceEnabled(final boolean traceEnabled) {
        this.traceEnabled = traceEnabled;
    }

    public void setDebugEnabled(final boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
    }

    public void setInfoEnabled(final boolean infoEnabled) {
        this.infoEnabled = infoEnabled;
    }

    public void setWarnEnabled(final boolean warnEnabled) {
        this.warnEnabled = warnEnabled;
    }

    public void setErrorEnabled(final boolean errorEnabled) {
        this.errorEnabled = errorEnabled;
    }

    public void setEnabled(final boolean enabled) {
        this.errorEnabled = enabled;
        this.warnEnabled = enabled;
        this.infoEnabled = enabled;
        this.debugEnabled = enabled;
        this.traceEnabled = enabled;
    }

    @Override
    public void trace(final String message) {
        log(Level.TRACE, message);
    }

    @Override
    public void trace(final String format, final Object arg) {
        log(Level.TRACE, format, arg);
    }

    @Override
    public void trace(final String format, final Object arg1, final Object arg2) {
        log(Level.TRACE, format, arg1, arg2);
    }

    @Override
    public void trace(final String format, final Object... args) {
        log(Level.TRACE, format, args);
    }

    @Override
    public void trace(final String msg, final Throwable throwable) {
        log(Level.TRACE, msg, throwable);
    }

    @Override
    public boolean isTraceEnabled(final Marker marker) {
        return traceEnabled;
    }

    @Override
    public void trace(final Marker marker, final String msg) {
        log(Level.TRACE, marker, msg);
    }

    @Override
    public void trace(final Marker marker, final String format, final Object arg) {
        log(Level.TRACE, marker, format, arg);
    }

    @Override
    public void trace(final Marker marker, final String format, final Object arg1, final Object arg2) {
        log(Level.TRACE, marker, format, arg1, arg2);
    }

    @Override
    public void trace(final Marker marker, final String format, final Object... args) {
        log(Level.TRACE, marker, format, args);
    }

    @Override
    public void trace(final Marker marker, final String msg, final Throwable throwable) {
        log(Level.TRACE, marker, msg, throwable);
    }

    @Override
    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    @Override
    public void debug(final String message) {
        log(Level.DEBUG, message);
    }

    @Override
    public void debug(final String format, final Object arg) {
        log(Level.DEBUG, format, arg);
    }

    @Override
    public void debug(final String format, final Object arg1, final Object arg2) {
        log(Level.DEBUG, format, arg1, arg2);
    }

    @Override
    public void debug(final String format, final Object... args) {
        log(Level.DEBUG, format, args);
    }

    @Override
    public void debug(final String msg, final Throwable throwable) {
        log(Level.DEBUG, msg, throwable);
    }

    @Override
    public boolean isDebugEnabled(final Marker marker) {
        return debugEnabled;
    }

    @Override
    public void debug(final Marker marker, final String msg) {
        log(Level.DEBUG, marker, msg);
    }

    @Override
    public void debug(final Marker marker, final String format, final Object arg) {
        log(Level.DEBUG, marker, format, arg);
    }

    @Override
    public void debug(final Marker marker, final String format, final Object arg1, final Object arg2) {
        log(Level.DEBUG, marker, format, arg1, arg2);
    }

    @Override
    public void debug(final Marker marker, final String format, final Object... args) {
        log(Level.DEBUG, marker, format, args);
    }

    @Override
    public void debug(final Marker marker, final String msg, final Throwable throwable) {
        log(Level.DEBUG, marker, msg, throwable);
    }

    @Override
    public boolean isInfoEnabled() {
        return infoEnabled;
    }

    @Override
    public void info(final String message) {
        log(Level.INFO, message);
    }

    @Override
    public void info(final String format, final Object arg) {
        log(Level.INFO, format, arg);
    }

    @Override
    public void info(final String format, final Object arg1, final Object arg2) {
        log(Level.INFO, format, arg1, arg2);
    }

    @Override
    public void info(final String format, final Object... args) {
        log(Level.INFO, format, args);
    }

    @Override
    public void info(final String msg, final Throwable throwable) {
        log(Level.INFO, msg, throwable);
    }

    @Override
    public boolean isInfoEnabled(final Marker marker) {
        return infoEnabled;
    }

    @Override
    public void info(final Marker marker, final String msg) {
        log(Level.INFO, marker, msg);
    }

    @Override
    public void info(final Marker marker, final String format, final Object arg) {
        log(Level.INFO, marker, format, arg);
    }

    @Override
    public void info(final Marker marker, final String format, final Object arg1, final Object arg2) {
        log(Level.INFO, marker, format, arg1, arg2);
    }

    @Override
    public void info(final Marker marker, final String format, final Object... args) {
        log(Level.INFO, marker, format, args);
    }

    @Override
    public void info(final Marker marker, final String msg, final Throwable throwable) {
        log(Level.INFO, marker, msg, throwable);
    }

    @Override
    public boolean isWarnEnabled() {
        return warnEnabled;
    }

    @Override
    public void warn(final String message) {
        log(Level.WARN, message);
    }

    @Override
    public void warn(final String format, final Object arg) {
        log(Level.WARN, format, arg);
    }

    @Override
    public void warn(final String format, final Object arg1, final Object arg2) {
        log(Level.WARN, format, arg1, arg2);
    }

    @Override
    public void warn(final String format, final Object... args) {
        log(Level.WARN, format, args);
    }

    @Override
    public void warn(final String msg, final Throwable throwable) {
        log(Level.WARN, msg, throwable);
    }

    @Override
    public boolean isWarnEnabled(final Marker marker) {
        return warnEnabled;
    }

    @Override
    public void warn(final Marker marker, final String msg) {
        log(Level.WARN, marker, msg);
    }

    @Override
    public void warn(final Marker marker, final String format, final Object arg) {
        log(Level.WARN, marker, format, arg);
    }

    @Override
    public void warn(final Marker marker, final String format, final Object arg1, final Object arg2) {
        log(Level.WARN, marker, format, arg1, arg2);
    }

    @Override
    public void warn(final Marker marker, final String format, final Object... args) {
        log(Level.WARN, marker, format, args);
    }

    @Override
    public void warn(final Marker marker, final String msg, final Throwable throwable) {
        log(Level.WARN, marker, msg, throwable);
    }

    @Override
    public boolean isErrorEnabled() {
        return errorEnabled;
    }

    @Override
    public void error(final String message) {
        log(Level.ERROR, message);
    }

    @Override
    public void error(final String format, final Object arg) {
        log(Level.ERROR, format, arg);
    }

    @Override
    public void error(final String format, final Object arg1, final Object arg2) {
        log(Level.ERROR, format, arg1, arg2);
    }

    @Override
    public void error(final String format, final Object... args) {
        log(Level.ERROR, format, args);
    }

    @Override
    public void error(final String msg, final Throwable throwable) {
        log(Level.ERROR, msg, throwable);
    }

    @Override
    public boolean isErrorEnabled(final Marker marker) {
        return errorEnabled;
    }

    @Override
    public void error(final Marker marker, final String msg) {
        log(Level.ERROR, marker, msg);
    }

    @Override
    public void error(final Marker marker, final String format, final Object arg) {
        log(Level.ERROR, marker, format, arg);
    }

    @Override
    public void error(final Marker marker, final String format, final Object arg1, final Object arg2) {
        log(Level.ERROR, marker, format, arg1, arg2);
    }

    @Override
    public void error(final Marker marker, final String format, final Object... args) {
        log(Level.ERROR, marker, format, args);
    }

    @Override
    public void error(final Marker marker, final String msg, final Throwable throwable) {
        log(Level.ERROR, marker, msg, throwable);
    }

    private void log(final Level level, final String format, final Object... args) {
        addLoggingEvent(level, null, null, format, args);
    }

    private void log(final Level level, final String msg, final Throwable throwable) {
        addLoggingEvent(level, null, throwable, msg, (Object[]) null);
    }

    private void log(final Level level, final Marker marker, final String format, final Object... args) {
        addLoggingEvent(level, marker, null, format, args);
    }

    private void log(final Level level, final Marker marker, final String msg, final Throwable throwable) {
        addLoggingEvent(level, marker, throwable, msg, (Object[]) null);
    }

    private void log(final Level level, final String format, final Marker marker, final Object[] args) {
        addLoggingEvent(level, marker, null, format, args);
    }

    private void addLoggingEvent(
            final Level level,
            final Marker marker,
            final Throwable throwable,
            final String format,
            final Object... args) {
        final TestLoggerEvent event = new TestLoggerEvent(name, level, null, marker, throwable, format, args);
        loggerEvents.add(event);
        print(event);
    }

    private void print(final TestLoggerEvent event) {
        final PrintStream ps = event.getLevel() == Level.ERROR || event.getLevel() == Level.WARN ? System.err : System.out;
        ps.println(formatLogStatement(event));
        ps.flush();
    }

    private String formatLogStatement(final TestLoggerEvent event) {
        if (event.getThrowable() == null) {
            return event.getLevel() + " " + event.getLoggerName() + ": " + event.getFormattedMessage();
        }
        ByteArrayOutputStream s = new ByteArrayOutputStream();
        event.getThrowable().printStackTrace(new PrintStream(s));
        String st = new String(s.toByteArray());
        return event.getLevel() + " " + event.getLoggerName() + ": " + event.getFormattedMessage() + "\n" + st;
    }

    public int getEventCount() {
        return loggerEvents.size();
    }

    public TestLoggerEvent getEvent(final int i) {
        return loggerEvents.get(i);
    }
}
