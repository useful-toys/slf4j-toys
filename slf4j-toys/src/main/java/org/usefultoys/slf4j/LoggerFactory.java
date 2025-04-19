/*
 * Copyright 2024 Daniel Felix Ferber
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

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.slf4j.LoggerFactory.getILoggerFactory;

/**
 * An alternative to {@link org.slf4j.LoggerFactory}, providing additional utility methods for creating and managing
 * loggers.
 *
 * <p>This class is final and cannot be instantiated.</p>
 *
 * @author Daniel Felix Ferber
 */
@UtilityClass
public class LoggerFactory {
    /**
     * Retrieves a logger with the specified name using the statically bound
     * {@link ILoggerFactory} instance.
     *
     * <p>This is recommended for obtaining a logger with a custom name that
     * does not follow the fully qualified class name convention.</p>
     *
     * @param name the name of the logger
     * @return the logger instance
     */
    public Logger getLogger(final @NonNull String name) {
        return getILoggerFactory().getLogger(name);
    }

    /**
     * Retrieves a logger named after the specified class using the statically
     * bound {@link ILoggerFactory} instance.
     *
     * <p>This is recommended for obtaining a logger that tracks features
     * provided by the specified class.</p>
     *
     * @param clazz the class whose name will be used for the logger
     * @return the logger instance
     */
    public Logger getLogger(final @NonNull Class<?> clazz) {
        return getILoggerFactory().getLogger(clazz.getName());
    }

    /**
     * Retrieves a logger named after a parent class and a specific operation
     * or feature, using the statically bound {@link ILoggerFactory} instance.
     *
     * <p>This is recommended for obtaining a logger that is subordinate to
     * an existing logger named after a class.</p>
     *
     * @param clazz the parent class whose name will be used as a base
     * @param name the name of the operation or feature appended to the parent logger name
     * @return the logger instance
     */
    public Logger getLogger(final @NonNull Class<?> clazz, final @NonNull String name) {
        return getILoggerFactory().getLogger(String.format("%s.%s", clazz.getName(), name));
    }


    /**
     * Retrieves a logger named after a parent logger and a specific operation
     * or feature, using the statically bound {@link ILoggerFactory} instance.
     *
     * <p>This is recommended for obtaining a logger that is subordinate to
     * an existing logger.</p>
     *
     * @param logger the parent logger
     * @param name the name of the operation or feature appended to the parent logger name
     * @return the logger instance
     */
    public Logger getLogger(final @NonNull Logger logger, final @NonNull String name) {
        return getILoggerFactory().getLogger(String.format("%s.%s", logger.getName(), name));
    }

    /**
     * Returns a {@link PrintStream} that writes structured text as trace-level messages to the specified logger.
     *
     * <p>The {@code close} and {@code flush} methods of the returned {@link PrintStream} trigger the logging of the
     *  accumulated text.</p>
     *
     * @param logger the logger to which messages will be written
     * @return a PrintStream for trace-level logging
     */
    @SneakyThrows
    public PrintStream getTracePrintStream(final @NonNull Logger logger) {
        if (!logger.isTraceEnabled()) {
            return new NullPrintStream();
        }
        return new PrintStream(getTraceOutputStream(logger), false, SessionConfig.charset);
    }

    /**
     * Returns a {@link PrintStream} that writes structured text as  debug-level messages to the specified logger.
     *
     * <p>The {@code close} and {@code flush} methods of the returned {@link PrintStream} trigger the logging of the
     *  accumulated text.</p>
     *
     * @param logger the logger to which messages will be written
     * @return a PrintStream for debug-level logging
     */
    @SneakyThrows
    public PrintStream getDebugPrintStream(final @NonNull Logger logger) {
        if (!logger.isDebugEnabled()) {
            return new NullPrintStream();
        }
        return new PrintStream(getDebugOutputStream(logger), false, SessionConfig.charset);
    }

    /**
     * Returns a {@link PrintStream} that writes structured text as  info-level messages to the specified logger.
     *
     * <p>The {@code close} and {@code flush} methods of the returned {@link PrintStream} trigger the logging of the
     *  accumulated text.</p>
     *
     * @param logger the logger to which messages will be written
     * @return a PrintStream for info-level logging
     */
    @SneakyThrows
    public PrintStream getInfoPrintStream(final @NonNull Logger logger) {
        if (!logger.isInfoEnabled()) {
            return new NullPrintStream();
        }
        return new PrintStream(getInfoOutputStream(logger), false, SessionConfig.charset);
    }

    /**
     * Returns a {@link PrintStream} that writes structured text as  warn-level messages to the specified logger.
     *
     * <p>The {@code close} and {@code flush} methods of the returned {@link PrintStream} trigger the logging of the
     *  accumulated text.</p>
     *
     * @param logger the logger to which messages will be written
     * @return a PrintStream for warn-level logging
     */
    @SneakyThrows
    public PrintStream getWarnPrintStream(final @NonNull Logger logger) {
        if (!logger.isWarnEnabled()) {
            return new NullPrintStream();
        }
        return new PrintStream(getWarnOutputStream(logger), false, SessionConfig.charset);
    }

    /**
     * Returns a {@link PrintStream} that writes structured text as  error-level messages to the specified logger.
     *
     * <p>The {@code close} and {@code flush} methods of the returned {@link PrintStream} trigger the logging of the
     *  accumulated text.</p>
     *  
     * @param logger the logger to which messages will be written
     * @return a PrintStream for error-level logging
     */
    @SneakyThrows
    public PrintStream getErrorPrintStream(final @NonNull Logger logger) {
        if (!logger.isErrorEnabled()) {
            return new NullPrintStream();
        }
        return new PrintStream(getErrorOutputStream(logger), false, SessionConfig.charset);
    }

    /**
     * Returns a {@link OutputStream} that writes trace-level messages to the specified logger.
     *
     * @param logger the logger to which messages will be written
     * @return a OutputStream for trace-level logging
     */
    public OutputStream getTraceOutputStream(final @NonNull Logger logger) {
        if (!logger.isTraceEnabled()) {
            return new NullOutputStream();
        }
        return new LoggerOutputStream() {
            @Override
            protected void writeToLogger() {
                logger.trace(extractString());
            }
        };
    }

    /**
     * Returns a {@link OutputStream} that writes debug-level messages to the specified logger.
     *
     * @param logger the logger to which messages will be written
     * @return a OutputStream for debug-level logging
     */
    public OutputStream getDebugOutputStream(final @NonNull Logger logger) {
        if (!logger.isDebugEnabled()) {
            return new NullOutputStream();
        }
        return new LoggerOutputStream() {
            @Override
            protected void writeToLogger() {
                logger.debug(extractString());
            }
        };
    }

    /**
     * Returns a {@link OutputStream} that writes info-level messages to the specified logger.
     *
     * @param logger the logger to which messages will be written
     * @return a OutputStream for info-level logging
     */
    public OutputStream getInfoOutputStream(final @NonNull Logger logger) {
        if (!logger.isInfoEnabled()) {
            return new NullOutputStream();
        }
        return new LoggerOutputStream() {
            @Override
            protected void writeToLogger() {
                logger.info(extractString());
            }
        };
    }

    /**
     * Returns a {@link OutputStream} that writes warn-level messages to the specified logger.
     *
     * @param logger the logger to which messages will be written
     * @return a OutputStream for warn-level logging
     */
    public OutputStream getWarnOutputStream(final @NonNull Logger logger) {
        if (!logger.isWarnEnabled()) {
            return new NullOutputStream();
        }
        return new LoggerOutputStream() {
            @Override
            protected void writeToLogger() {
                logger.warn(extractString());
            }
        };
    }

    /**
     * Returns a {@link OutputStream} that writes error-level messages to the specified logger.
     *
     * @param logger the logger to which messages will be written
     * @return a OutputStream for error-level logging
     */
    public OutputStream getErrorOutputStream(final @NonNull Logger logger) {
        if (!logger.isErrorEnabled()) {
            return new NullOutputStream();
        }
        return new LoggerOutputStream() {
            @Override
            protected void writeToLogger() {
                logger.error(extractString());
            }
        };
    }
}
