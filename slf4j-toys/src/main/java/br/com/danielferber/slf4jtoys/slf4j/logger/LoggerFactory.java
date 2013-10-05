/*
 * Copyright 2013 Daniel Felix Ferber
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package br.com.danielferber.slf4jtoys.slf4j.logger;

import java.io.OutputStream;
import java.io.PrintStream;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getILoggerFactory;

/**
 * An alternative to {@link org.slf4j.LoggerFactory}, with additional useful
 * methods.
 *
 * @author Daniel Felix Ferber
 */
public class LoggerFactory {

    /**
     * Returns a logger named according to the name parameter using the
     * statically bound {@link ILoggerFactory} instance. Equivalent to
     * {@link org.slf4j.LoggerFactory#getLogger(String)}.
     * <p>
     * Recommended to get a special purpose logger defined by the application,
     * whose name does not follow de fully qualified name convention and that
     * tracks some globally available feature.
     *
     * @param name The name of the logger.
     * @return the logger
     */
    public static Logger getLogger(String name) {
        return getILoggerFactory().getLogger(name);
    }

    /**
     * Returns a logger named according to the class passed as parameter, using
     * the statically bound {@link ILoggerFactory} instance.
     * <p>
     * Recommended to get a logger that tracks features provided by the class.
     *
     * @param clazz the returned logger will be named after clazz
     * @return the logger
     */
    public static Logger getLogger(Class<?> clazz) {
        return getILoggerFactory().getLogger(clazz.getName());
    }

    /**
     * Returns a logger named according to the operation or feature and provided
     * by the class passed as parameter, using the statically bound
     * {@link ILoggerFactory} instance.
     * <p>
     * Recommended to get a logger that tracks separatedly a specific operation
     * or feature provided by the class.
     *
     * @param clazz the returned logger will be named after clazz
     * @param name the name of the operation or feature provided by the class.
     * @return the logger
     */
    public static Logger getLogger(Class<?> clazz, String name) {
        return getILoggerFactory().getLogger(clazz.getName() + '.' + name);
    }

    /**
     * Returns a logger named according to and operation or feature tracked by
     * the the logger passed as parameter, using the statically bound
     * {@link ILoggerFactory} instance.
     *
     * Recommended to get a logger that tracks separatedly a step, operation or
     * feature being reported by an existing logger.
     *
     * @param clazz the returned logger will be named after logger
     * @param name the name of the operation provided by the class.
     * @return the logger
     */
    public static Logger getLogger(Logger logger, String name) {
        return getILoggerFactory().getLogger(logger.getName() + '.' + name);
    }

    /**
     * Returns a {@link PrintStream} whose close and flush write text to a trace
     * logger.
     *
     * @param logger the logger written to.
     * @return the PrintStream
     */
    public static PrintStream getTracePrintStream(final Logger logger) {
        if (!logger.isTraceEnabled()) {
            return new NullPrintStream();
        }
        return new PrintStream(LoggerFactory.getTraceOutputStream(logger));
    }

    /**
     * Returns a {@link PrintStream} whose close and flush write text to a debug
     * logger.
     *
     * @param logger the logger written to.
     * @return the PrintStream
     */
    public static PrintStream getDebugPrintStream(final Logger logger) {
        if (!logger.isDebugEnabled()) {
            return new NullPrintStream();
        }
        return new PrintStream(LoggerFactory.getDebugOutputStream(logger));
    }

    /**
     * Returns a {@link PrintStream} whose close and flush write text to a info
     * logger.
     *
     * @param logger the logger written to.
     * @return the PrintStream
     */
    public static PrintStream getInfoPrintStream(final Logger logger) {
        if (!logger.isInfoEnabled()) {
            return new NullPrintStream();
        }
        return new PrintStream(LoggerFactory.getInfoOutputStream(logger));
    }

    /**
     * Returns a {@link PrintStream} whose close and flush write text to a warn
     * logger.
     *
     * @param logger the logger written to.
     * @return the PrintStream
     */
    public static PrintStream getWarnPrintStream(final Logger logger) {
        if (!logger.isWarnEnabled()) {
            return new NullPrintStream();
        }
        return new PrintStream(LoggerFactory.getWarnOutputStream(logger));
    }

    /**
     * Returns a {@link PrintStream} whose close and flush write text to a error
     * logger.
     *
     * @param logger the logger written to.
     * @return the PrintStream
     */
    public static PrintStream getErrorPrintStream(final Logger logger) {
        if (!logger.isErrorEnabled()) {
            return new NullPrintStream();
        }
        return new PrintStream(LoggerFactory.getErrorOutputStream(logger));
    }

    /**
     * Returns a {@link PrintStream} whose close and flush write unformatted
     * data to a trace logger.
     *
     * @param logger the logger written to.
     * @return the PrintStream
     */
    public static OutputStream getTraceOutputStream(final Logger logger) {
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
     * Returns a {@link PrintStream} whose close and flush write unformatted
     * data to a debug logger.
     *
     * @param logger the logger written to.
     * @return the PrintStream
     */
    public static OutputStream getDebugOutputStream(final Logger logger) {
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
     * Returns a {@link PrintStream} whose close and flush write unformatted
     * data to a info logger.
     *
     * @param logger the logger written to.
     * @return the PrintStream
     */
    public static OutputStream getInfoOutputStream(final Logger logger) {
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
     * Returns a {@link PrintStream} whose close and flush write unformatted
     * data to a warn logger.
     *
     * @param logger the logger written to.
     * @return the PrintStream
     */
    public static OutputStream getWarnOutputStream(final Logger logger) {
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
     * Returns a {@link PrintStream} whose close and flush write unformatted
     * data to a error logger.
     *
     * @param logger the logger written to.
     * @return the PrintStream
     */
    public static OutputStream getErrorOutputStream(final Logger logger) {
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
