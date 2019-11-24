/*
 * Copyright 2019 Daniel Felix Ferber
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
package org.usefultoys.slf4j.meter;

import org.slf4j.Logger;
import org.usefultoys.slf4j.LoggerFactory;
import org.usefultoys.slf4j.Session;

import java.io.Closeable;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.LinkedHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.usefultoys.slf4j.meter.MeterConfig.*;

/**
 * At beginning, termination of operations and on iterations, collects system status and reports it to logger. Call {@link #start()} to produce a 1-line summary
 * about operation start and current system status as debug message and an encoded event as trace message. Call {@link #ok()} to produce a 1-line summary about
 * operation successful end and current system status as information message and an encoded event as trace message. Call {@link #fail(Object)} to produce a
 * 1-line summary about operation failure and current system status as error message and an encoded event as trace message. Call {@link #progress()} to produce
 * a 1-line summary about operation progress and current system status as information message and an encoded event as trace message.
 *
 * @author Daniel Felix Ferber
 */
public class Meter extends MeterData implements Closeable {

    private static final long serialVersionUID = 1L;

    private static final String ERROR_MSG_METER_CANNOT_CREATE_EXCEPTION = "Meter cannot create exception of type {}.";
    private static final String ERROR_MSG_METER_ALREADY_STARTED = "Meter already started. id={}";
    private static final String ERROR_MSG_METER_ALREADY_STOPPED = "Meter already stopped. id={}";
    private static final String ERROR_MSG_METER_STOPPED_BUT_NOT_STARTED = "Meter stopped but not started. id={}";
    private static final String ERROR_MSG_METER_STARTED_AND_NEVER_STOPPED = "Meter started and never stopped. id={}";
    private static final String ERROR_MSG_METER_INCREMENTED_BUT_NOT_STARTED = "Meter incremented but not started. id={}";
    private static final String ERROR_MSG_METER_PROGRESS_BUT_NOT_STARTED = "Meter progress but not started. id={}";

    private static final String ERROR_MSG_METHOD_THREW_EXCEPTION = "Meter.{}(...) method threw exception. id={}";
    private static final String ERROR_MSG_ILLEGAL_ARGUMENT = "Illegal call to Meter.{}: {}. id={}";
    private static final String ERROR_MSG_METER_OUT_OF_ORDER = "Meter out of order. id={}";

    private static final String ERROR_MSG_NULL_ARGUMENT = "Null argument. id={}";
    private static final String ERROR_MSG_NON_POSITIVE_ARGUMENT = "Non positive argument";
    private static final String ERROR_MSG_ILLEGAL_STRING_FORMAT = "Illegal string format";
    private static final String ERROR_MSG_NON_FORWARD_ITERATION = "Non forward iteration";
    private static final String MY_CLASS_NAME = Meter.class.getName();
    private static final String NULL_VALUE = "<null>";
    public static final String UNKNOWN_LOGGER_NAME = "???";

    /** Logger that prints readable messages. */
    private transient final Logger messageLogger;
    /** Logger that prints enconded data. */
    private transient final Logger dataLogger;

    /**
     * Tracks many times each operation has been executed.
     */
    static final ConcurrentMap<String, AtomicLong> EVENT_COUNTER = new ConcurrentHashMap<String, AtomicLong>();
    /**
     * Timestamp when progress was reported for last time. Zero if progress was not reported yet. Used to skip progress messages and to avoid flooding the log
     * if progress is reported too fast.
     */
    private transient long lastProgressTime = 0;
    /**
     * Iteration when progress was reported for last time. Zero if progress was not reported yet. Used to skip progress messages and to avoid flooding the log
     * if progress is reported too fast.
     */
    private transient long lastProgressIteration = 0;

    /**
     * Tracks the instance of the current meters withing each thread.
     */
    private static final ThreadLocal<WeakReference<Meter>> localThreadInstance = new ThreadLocal<WeakReference<Meter>>();
    /**
     * Tracks the instance of Meter that was current before this meter became the current Meter. These references are a linked list of Meters that describe a
     * stack of Meters.
     */
    private WeakReference<Meter> previousInstance;

    /**
     * Creates a new meter for the category given by the logger's name.
     *
     * @param logger Logger that reports messages.
     */
    public Meter(final Logger logger) {
        this(logger, null);
    }

    /**
     * Creates a new Meter for the operation beloging to the category given by the logger's name.
     *
     * @param logger    Logger that reports messages.
     * @param operation The operation name or null.
     */
    public Meter(final Logger logger, final String operation) {
        this(logger, operation, null);
    }

    /**
     * Creates a new Meter for the operation beloging to the category given by the logger's name, as child for an existing Meter.
     *
     * @param logger    Logger that reports messages.
     * @param operation The operation name or null.
     * @param parent    ID of the parent Meter or null.
     */
    public Meter(final Logger logger, final String operation, final String parent) {
        super(Session.uuid, extractNextPosition(logger.getName(), operation), logger.getName(), operation, parent);
        this.messageLogger = org.slf4j.LoggerFactory.getLogger(messagePrefix + logger.getName() + messageSuffix);
        this.dataLogger = org.slf4j.LoggerFactory.getLogger(dataPrefix + logger.getName() + dataSuffix);
    }

    private static long extractNextPosition(final String eventCategory, final String operationName) {
        final String key = operationName == null ? eventCategory : eventCategory + "/" + operationName;
        EVENT_COUNTER.putIfAbsent(key, new AtomicLong(0));
        final AtomicLong atomicLong = EVENT_COUNTER.get(key);
        atomicLong.compareAndSet(Long.MAX_VALUE, 0);
        return atomicLong.incrementAndGet();
    }

    /**
     * @return The Meter most recently started on the current thread.
     */
    public static Meter getCurrentInstance() {
        final WeakReference<Meter> ref = localThreadInstance.get();
        final Meter current = ref == null ? null : ref.get();
        if (current == null) {
            return new Meter(LoggerFactory.getLogger(UNKNOWN_LOGGER_NAME));
        }
        return current;
    }

    // ========================================================================

    /**
     * Creates a new Meter for an operation that belongs to the opeartion of this Meter. Useful if a large operation may be subdivided into smaller operations
     * and reported individually. The new Meter uses the same category of this Meter.
     *
     * @param suboperationName Additional identification appended to this logger name.
     * @return The new Meter
     */
    public Meter sub(final String suboperationName) {
        if (suboperationName == null) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            messageLogger.error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "sub(name)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
        }
        final Meter m = new Meter(messageLogger, operation == null ? suboperationName : operation + '/' + suboperationName, this.getFullID());
        if (this.context != null) {
            m.context = new HashMap<String, String>(this.context);
        }
        return m;
    }

    // ========================================================================

    /**
     * Configures the Meter with a human readable message that explains the operations's purpose.
     *
     * @param message fixed message
     * @return reference to the meter itself.
     */
    public Meter m(final String message) {
        if (message == null) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            messageLogger.error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "m(message)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
        }
        this.description = message;
        return this;
    }

    /**
     * Configures the Meter with a human readable message that explains the operations's purpose.
     *
     * @param format message format ({@link String#format(java.lang.String, java.lang.Object...)})
     * @param args   message arguments
     * @return reference to the meter itself.
     */
    public Meter m(final String format, final Object... args) {
        if (format == null) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            messageLogger.error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "m(message, args...)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
            this.description = null;
            return this;
        }
        try {
            this.description = String.format(format, args);
        } catch (final IllegalFormatException e) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            messageLogger.error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "m(format, args...)", ERROR_MSG_ILLEGAL_STRING_FORMAT, getFullID(), new IllegalMeterUsage(2, e));
        }
        return this;
    }

    /**
     * Configures the Meter with an threshold for reasonable, typical execution time for the operation.
     *
     * @param timeLimit time threshold
     * @return reference to the meter itself.
     */
    public Meter limitMilliseconds(final long timeLimit) {
        if (timeLimit <= 0) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            messageLogger.error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "limitMilliseconds(timeLimit)", ERROR_MSG_NON_POSITIVE_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
            return this;
        }
        this.timeLimit = timeLimit * 1000 * 1000;
        return this;
    }

    /**
     * Configures the Meter for an operation made up of iterations or steps. Such Meter is allows to call {@link #progress() } an arbitrarily number of Such
     * Meter should call {@link #inc()}, {@link #incBy(long)} or {@link #incTo(long)} to advance the current iteration. times between {@link #start() } and
     * {@link #ok()}/{@link #reject(Object)}/{@link #fail(Object) } method calls.
     *
     * @param expectedIterations Number of expected iterations or steps that make up the task
     * @return reference to the meter itself.
     */
    public Meter iterations(final long expectedIterations) {
        if (expectedIterations <= 0) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            messageLogger.error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "iterations(expectedIterations)", ERROR_MSG_NON_POSITIVE_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
            return this;
        }
        this.expectedIterations = expectedIterations;
        return this;
    }

    // ========================================================================

    /**
     * Adds an entry to the context map. The entry has no value and is interpreted as a marker.
     *
     * @param name key of the entry to add.
     * @return reference to the meter itself.
     */
    public Meter ctx(final String name) {
        if (name == null) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            messageLogger.error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "ctx(name)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
            return this;
        }
        if (context == null) {
            this.context = new LinkedHashMap<String, String>();
        }
        context.put(name, null);
        return this;
    }

    /**
     * Adds an entry to the context map if conditions is true. The entry has no value and is interpreted as a marker.
     *
     * @param condition the condition
     * @param trueName  key of the entry to add if conditions is true
     * @return reference to the meter itself.
     */
    public Meter ctx(final boolean condition, final String trueName) {
        if (!condition) {
            return this;
        }
        if (trueName == null) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            messageLogger.error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "ctx(condition,name)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
            return this;
        }
        if (context == null) {
            this.context = new LinkedHashMap<String, String>();
        }
        context.put(trueName, null);
        return this;
    }

    /**
     * Adds an entry to the context map if conditions is true or false. The entry has no value and is interpreted as a marker.
     *
     * @param condition the condition
     * @param trueName  key of the entry to add if conditions is true
     * @param falseName key of the entry to add if conditions is true
     * @return reference to the meter itself.
     */
    public Meter ctx(final boolean condition, final String trueName, final String falseName) {
        if (condition) {
            if (trueName == null) {
                /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
                messageLogger.error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "ctx(condition,name,name)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
                return this;
            }
            if (context == null) {
                this.context = new LinkedHashMap<String, String>();
            }
            context.put(trueName, null);
        } else {
            if (falseName == null) {
                /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
                messageLogger.error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "ctx(condition,name,name)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
                return this;
            }
            if (context == null) {
                this.context = new LinkedHashMap<String, String>();
            }
            context.put(falseName, null);
        }
        return this;
    }

    /**
     * Adds an entry to the context map.
     *
     * @param name  key of the entry to add.
     * @param value value of the entry to add.
     * @return reference to the meter itself.
     */
    public Meter ctx(final String name, final int value) {
        if (name == null) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            messageLogger.error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "ctx(name, value)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
            return this;
        }
        if (context == null) {
            this.context = new LinkedHashMap<String, String>();
        }
        context.put(name, Integer.toString(value));
        return this;
    }

    /**
     * Adds an entry to the context map.
     *
     * @param name  key of the entry to add.
     * @param value value of the entry to add.
     * @return reference to the meter itself.
     */
    public Meter ctx(final String name, final long value) {
        if (name == null) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            messageLogger.error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "ctx(name, value)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
            return this;
        }
        if (context == null) {
            this.context = new LinkedHashMap<String, String>();
        }
        context.put(name, Long.toString(value));
        return this;
    }

    /**
     * Adds an entry to the context map.
     *
     * @param name  key of the entry to add.
     * @param value value of the entry to add.
     * @return reference to the meter itself.
     */
    public Meter ctx(final String name, final boolean value) {
        if (name == null) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            messageLogger.error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "ctx(name, value)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
            return this;
        }
        if (context == null) {
            this.context = new LinkedHashMap<String, String>();
        }
        context.put(name, Boolean.toString(value));
        return this;
    }

    /**
     * Adds an entry to the context map.
     *
     * @param name  key of the entry to add.
     * @param value value of the entry to add.
     * @return reference to the meter itself.
     */
    public Meter ctx(final String name, final float value) {
        if (name == null) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            messageLogger.error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "ctx(name, value)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
            return this;
        }
        if (context == null) {
            this.context = new LinkedHashMap<String, String>();
        }
        context.put(name, Float.toString(value));
        return this;
    }

    /**
     * Adds an entry to the context map.
     *
     * @param name  key of the entry to add.
     * @param value value of the entry to add.
     * @return reference to the meter itself.
     */
    public Meter ctx(final String name, final double value) {
        if (name == null) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            messageLogger.error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "ctx(name, value)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
            return this;
        }
        if (context == null) {
            this.context = new LinkedHashMap<String, String>();
        }
        context.put(name, Double.toString(value));
        return this;
    }

    /**
     * Adds an entry to the context map.
     *
     * @param name  key of the entry to add.
     * @param value value of the entry to add.
     * @return reference to the meter itself.
     */
    public Meter ctx(final String name, final Integer value) {
        if (name == null) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            messageLogger.error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "ctx(name, value)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
            return this;
        }
        if (context == null) {
            this.context = new LinkedHashMap<String, String>();
        }
        context.put(name, value == null ? NULL_VALUE : value.toString());
        return this;
    }

    /**
     * Adds an entry to the context map.
     *
     * @param name  key of the entry to add.
     * @param value value of the entry to add.
     * @return reference to the meter itself.
     */
    public Meter ctx(final String name, final Long value) {
        if (name == null) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            messageLogger.error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "ctx(name, value)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
            return this;
        }
        if (context == null) {
            this.context = new LinkedHashMap<String, String>();
        }
        context.put(name, value == null ? NULL_VALUE : value.toString());
        return this;
    }

    /**
     * Adds an entry to the context map.
     *
     * @param name  key of the entry to add.
     * @param value value of the entry to add.
     * @return reference to the meter itself.
     */
    public Meter ctx(final String name, final Boolean value) {
        if (name == null) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            messageLogger.error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "ctx(name, value)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
            return this;
        }
        if (context == null) {
            this.context = new LinkedHashMap<String, String>();
        }
        context.put(name, value == null ? NULL_VALUE : value.toString());
        return this;
    }

    /**
     * Adds an entry to the context map.
     *
     * @param name  key of the entry to add.
     * @param value value of the entry to add.
     * @return reference to the meter itself.
     */
    public Meter ctx(final String name, final Float value) {
        if (name == null) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            messageLogger.error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "ctx(name, value)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
            return this;
        }
        if (context == null) {
            this.context = new LinkedHashMap<String, String>();
        }
        context.put(name, value == null ? NULL_VALUE : value.toString());
        return this;
    }

    /**
     * Adds an entry to the context map.
     *
     * @param name  key of the entry to add.
     * @param value value of the entry to add.
     * @return reference to the meter itself.
     */
    public Meter ctx(final String name, final Double value) {
        if (name == null) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            messageLogger.error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "ctx(name, value)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
            return this;
        }
        if (context == null) {
            this.context = new LinkedHashMap<String, String>();
        }
        context.put(name, value == null ? NULL_VALUE : value.toString());
        return this;
    }

    /**
     * Adds an entry to the context map.
     *
     * @param name   key of the entry to add.
     * @param object object which string representation is used for the value of the entry to add
     * @return reference to the meter itself.
     */
    public Meter ctx(final String name, final Object object) {
        if (name == null) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            messageLogger.error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "ctx(name, value)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
            return this;
        }
        if (context == null) {
            this.context = new LinkedHashMap<String, String>();
        }
        context.put(name, object == null ? NULL_VALUE : object.toString());
        return this;
    }

    /**
     * Adds an entry to the context map.
     *
     * @param name  key of the entry to add.
     * @param value value of the entry to add.
     * @return reference to the meter itself.
     */
    public Meter ctx(final String name, final String value) {
        if (name == null) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            messageLogger.error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "ctx(name, value)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
            return this;
        }
        if (context == null) {
            this.context = new LinkedHashMap<String, String>();
        }
        context.put(name, value == null ? NULL_VALUE : value);
        return this;
    }

    /**
     * Adds an entry to the context map. The entry value is made up of a formatted message with arguments.
     *
     * @param name   key of the entry to add.
     * @param format message format ({@link String#format(java.lang.String, java.lang.Object...) })
     * @param args   message arguments
     * @return reference to the meter itself.
     */
    public Meter ctx(final String name, final String format, final Object... args) {
        if (name == null || format == null) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            messageLogger.error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "ctx(name, format, args...)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
            return this;
        }
        if (context == null) {
            this.context = new LinkedHashMap<String, String>();
        }
        try {
            ctx(name, String.format(format, args));
        } catch (final IllegalFormatException e) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            messageLogger.error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "ctx(name, format, args...)", ERROR_MSG_ILLEGAL_STRING_FORMAT, getFullID(), new IllegalMeterUsage(2, e));
        }
        return this;
    }

    /**
     * Removes an entry from the context map.
     *
     * @param name key of the entry to remove.
     * @return reference to the meter itself.
     */
    public Meter unctx(final String name) {
        if (name == null) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            messageLogger.error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "unctx(name)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
            return this;
        }
        if (context == null) {
            return this;
        }
        context.remove(name);
        return this;
    }

    // ========================================================================

    /**
     * Notifies the meter in order to claim immediate execution start of the task represented by the meter. Sends a message to logger using debug level. Sends a
     * message with system status and partial context to log using trace level.
     *
     * @return reference to the meter itself.
     */
    public Meter start() {
        try {
            if (startTime != 0) {
                /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
                messageLogger.error(Markers.INCONSISTENT_START, ERROR_MSG_METER_ALREADY_STARTED, getFullID(), new IllegalMeterUsage(2));
            } else {
                previousInstance = localThreadInstance.get();
                localThreadInstance.set(new WeakReference<Meter>(this));
            }

            this.lastProgressTime = this.startTime = System.nanoTime();

            if (messageLogger.isDebugEnabled()) {
                collectRuntimeStatus();
                collectPlatformStatus();
                messageLogger.debug(Markers.MSG_START, readableWrite());
                if (dataLogger.isTraceEnabled()) {
                    dataLogger.trace(Markers.DATA_START, write());
                }
                if (context != null) {
                    context.clear();
                }
            }

        } catch (final Exception t) {
            /* Prevents bugs from disrupting the application. Logs message with exception to provide stacktrace to bug. */
            messageLogger.error(Markers.BUG, ERROR_MSG_METHOD_THREW_EXCEPTION, "start", getFullID(), t);
        }
        return this;
    }

    // ========================================================================

    /**
     * Notifies the meter that one more iteration or step completed that make up the task successfully.
     *
     * @return reference to the meter itself.
     */
    public Meter inc() {
        if (startTime == 0) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            messageLogger.error(Markers.INCONSISTENT_INCREMENT, ERROR_MSG_METER_INCREMENTED_BUT_NOT_STARTED, getFullID(), new IllegalMeterUsage(4));
        }
        this.currentIteration++;
        return this;
    }

    /**
     * Notifies the meter that more of iterations or steps that make up the task completed successfully.
     *
     * @param increment the number of iterations or steps
     * @return reference to the meter itself.
     */
    public Meter incBy(final long increment) {
        if (startTime == 0) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            messageLogger.error(Markers.INCONSISTENT_INCREMENT, ERROR_MSG_METER_INCREMENTED_BUT_NOT_STARTED, getFullID(), new IllegalMeterUsage(4));
        }
        if (increment <= 0) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            messageLogger.error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "incBy(increment)", ERROR_MSG_NON_POSITIVE_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
            return this;
        }
        this.currentIteration += increment;
        return this;
    }

    /**
     * Notifies the meter that a number of iterations or steps that make up the task already completed successfully.
     *
     * @param currentIteration the number of iterations or steps
     * @return reference to the meter itself.
     */
    public Meter incTo(final long currentIteration) {
        if (startTime == 0) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            messageLogger.error(Markers.INCONSISTENT_INCREMENT, ERROR_MSG_METER_INCREMENTED_BUT_NOT_STARTED, getFullID(), new IllegalMeterUsage(4));
        }
        if (currentIteration <= 0) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            messageLogger.error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "incTo(currentIteration)", ERROR_MSG_NON_POSITIVE_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
            return this;
        }
        if (currentIteration <= this.currentIteration) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            messageLogger.error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "incTo(currentIteration)", ERROR_MSG_NON_FORWARD_ITERATION, getFullID(), new IllegalMeterUsage(2));
        }
        this.currentIteration = currentIteration;
        return this;
    }

    /**
     * Allow informing about successful completion of iterations or steps making up the task represented by the meter. Only applicable for meters that called
     * {@link #iterations(long i)} before calling {@link #start() }. Sends a message to logger using info level, only periodically and if progress was observed,
     * to minimize performance degradation.
     *
     * @return reference to the meter itself.
     */
    public Meter progress() {
        try {
            if (startTime == 0) {
                /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
                messageLogger.error(Markers.INCONSISTENT_PROGRESS, ERROR_MSG_METER_PROGRESS_BUT_NOT_STARTED, getFullID(), new IllegalMeterUsage(2));
            }

            final long now;
            final long meterProgressPeriodNanoseconds = MeterConfig.progressPeriodMilliseconds * 1000 * 1000;
            if (currentIteration > lastProgressIteration && ((now = System.nanoTime()) - lastProgressTime) > meterProgressPeriodNanoseconds) {
                lastProgressIteration = currentIteration;
                lastProgressTime = now;

                if (messageLogger.isInfoEnabled()) {
                    collectRuntimeStatus();
                    collectPlatformStatus();
                    messageLogger.info(Markers.MSG_PROGRESS, readableWrite());
                    if (dataLogger.isTraceEnabled()) {
                        final String message2 = write();
                        if (startTime != 0 && timeLimit != 0 && (now - startTime) > timeLimit) {
                            dataLogger.trace(Markers.DATA_SLOW_PROGRESS, message2);
                        } else if (dataLogger.isTraceEnabled()) {
                            dataLogger.trace(Markers.DATA_PROGRESS, message2);
                        }
                    }
                    if (context != null) {
                        context.clear();
                    }
                }
            }
        } catch (final Exception t) {
            /* Prevents bugs from disrupting the application. Logs message with exception to provide stacktrace to bug. */
            messageLogger.error(Markers.BUG, ERROR_MSG_METHOD_THREW_EXCEPTION, "progress", getFullID(), t);
        }
        return this;
    }

    public Meter path(final Object pathId) {
        if (pathId instanceof String) {
            this.okPath = (String) pathId;
        } else if (pathId instanceof Enum) {
            this.okPath = ((Enum<?>) pathId).name();
        } else if (pathId instanceof Throwable) {
            this.okPath = pathId.getClass().getSimpleName();
        } else if (pathId != null) {
            this.okPath = pathId.toString();
        } else {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            messageLogger.error(Markers.ILLEGAL, ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(4));
        }
        return this;
    }

    /**
     * Confirms the meter in order to claim successful completion of the task represented by the meter. Sends a message to logger using info level. If a time
     * limit was given and execution exceeded this limit, sends a message using warn level instead. Sends a message with system status and partial context to
     * log using trace level.
     *
     * @return reference to the meter itself.
     */
    public Meter ok() {
        try {
            final long newStopTime = System.nanoTime();

            /* Sanity check. Logs message and exception with stacktrace forged to the inconsistent caller method. */
            if (stopTime != 0) {
                messageLogger.error(Markers.INCONSISTENT_OK, ERROR_MSG_METER_ALREADY_STOPPED, getFullID(), new IllegalMeterUsage(2));
            } else if (startTime == 0) {
                messageLogger.error(Markers.INCONSISTENT_OK, ERROR_MSG_METER_STOPPED_BUT_NOT_STARTED, getFullID(), new IllegalMeterUsage(2));
            } else if (checkCurrentInstance()) {
                messageLogger.error(Markers.INCONSISTENT_OK, ERROR_MSG_METER_OUT_OF_ORDER, getFullID(), new IllegalMeterUsage(2));
            }

            stopTime = newStopTime;
            failPath = null;
            failMessage = null;
            rejectPath = null;
            localThreadInstance.set(previousInstance);

            if (messageLogger.isWarnEnabled()) {
                collectRuntimeStatus();
                collectPlatformStatus();

                final boolean warnSlowness = startTime != 0 && timeLimit != 0 && stopTime - startTime > timeLimit;
                final String message1 = readableWrite();
                if (warnSlowness) {
                    messageLogger.warn(Markers.MSG_SLOW_OK, message1);
                } else if (messageLogger.isInfoEnabled()) {
                    messageLogger.info(Markers.MSG_OK, message1);
                }
                if (dataLogger.isTraceEnabled()) {
                    final String message2 = write();
                    if (warnSlowness) {
                        dataLogger.trace(Markers.DATA_SLOW_OK, message2);
                    } else {
                        dataLogger.trace(Markers.DATA_OK, message2);
                    }
                }
                if (context != null) {
                    context.clear();
                }
            }
        } catch (final Exception t) {
            /* Prevents bugs from disrupting the application. Logs message with exception to provide stacktrace to bug. */
            messageLogger.error(Markers.BUG, ERROR_MSG_METHOD_THREW_EXCEPTION, "ok", getFullID(), t);
        }
        return this;
    }

    public boolean checkCurrentInstance() {
        final WeakReference<Meter> ref = localThreadInstance.get();
        return ref == null || ref.get() != this;
    }

    /**
     * Confirms the meter in order to claim successful completion of the task represented by the meter. Sends a message to logger using info level. If a time
     * limit was given and execution exceeded this limit, sends a message using warn level instead. Sends a message with system status and partial context to
     * log using trace level.
     *
     * @param pathId A token, enum or exception that describes the successful pathId.
     * @return reference to the meter itself.
     */
    public Meter ok(final Object pathId) {
        try {
            final long newStopTime = System.nanoTime();

            /* Sanity check. Logs message and exception with stacktrace forged to the inconsistent caller method. */
            if (stopTime != 0) {
                messageLogger.error(Markers.INCONSISTENT_OK, ERROR_MSG_METER_ALREADY_STOPPED, getFullID(), new IllegalMeterUsage(2));
            } else if (startTime == 0) {
                messageLogger.error(Markers.INCONSISTENT_OK, ERROR_MSG_METER_STOPPED_BUT_NOT_STARTED, getFullID(), new IllegalMeterUsage(2));
            } else if (checkCurrentInstance()) {
                messageLogger.error(Markers.INCONSISTENT_OK, ERROR_MSG_METER_OUT_OF_ORDER, getFullID(), new IllegalMeterUsage(2));
            }

            stopTime = newStopTime;
            failPath = null;
            failMessage = null;
            rejectPath = null;
            localThreadInstance.set(previousInstance);
            if (pathId instanceof String) {
                this.okPath = (String) pathId;
            } else if (pathId instanceof Enum) {
                this.okPath = ((Enum<?>) pathId).name();
            } else if (pathId instanceof Throwable) {
                this.okPath = pathId.getClass().getSimpleName();
            } else if (pathId != null) {
                this.okPath = pathId.toString();
            } else {
                /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
                messageLogger.error(Markers.INCONSISTENT_OK, ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(4));
            }

            if (messageLogger.isWarnEnabled()) {
                collectRuntimeStatus();
                collectPlatformStatus();

                final boolean warnSlowness = startTime != 0 && timeLimit != 0 && stopTime - startTime > timeLimit;
                final String message1 = readableWrite();
                if (warnSlowness) {
                    messageLogger.warn(Markers.MSG_SLOW_OK, message1);
                } else if (messageLogger.isInfoEnabled()) {
                    messageLogger.info(Markers.MSG_OK, message1);
                }
                if (dataLogger.isTraceEnabled()) {
                    final String message2 = write();
                    if (warnSlowness) {
                        dataLogger.trace(Markers.DATA_SLOW_OK, message2);
                    } else {
                        dataLogger.trace(Markers.DATA_OK, message2);
                    }
                }
                if (context != null) {
                    context.clear();
                }
            }
        } catch (final Exception t) {
            /* Prevents bugs from disrupting the application. Logs message with exception to provide stacktrace to bug. */
            messageLogger.error(Markers.BUG, ERROR_MSG_METHOD_THREW_EXCEPTION, "ok", getFullID(), t);
        }
        return this;
    }

    /**
     * Confirms the meter in order to claim unsuccessful completion of the task represented by the meter. Sends a message to logger using info level. If a time
     * limit was given and execution exceeded this limit, sends a message using warn level instead. Sends a message with system status and partial context to
     * log using trace level.
     *
     * @param cause A token, enum or exception that describes the cause of rejection.
     * @return reference to the meter itself.
     */
    public Meter reject(final Object cause) {
        try {
            final long newStopTime = System.nanoTime();

            /* Sanity check. Logs message and exception with stacktrace forged to the inconsistent caller method. */
            if (stopTime != 0) {
                messageLogger.error(Markers.INCONSISTENT_REJECT, ERROR_MSG_METER_ALREADY_STOPPED, getFullID(), new IllegalMeterUsage(2));
            } else if (startTime == 0) {
                messageLogger.error(Markers.INCONSISTENT_REJECT, ERROR_MSG_METER_STOPPED_BUT_NOT_STARTED, getFullID(), new IllegalMeterUsage(2));
            } else if (checkCurrentInstance()) {
                messageLogger.error(Markers.INCONSISTENT_REJECT, ERROR_MSG_METER_OUT_OF_ORDER, getFullID(), new IllegalMeterUsage(2));
            }

            stopTime = newStopTime;
            failPath = null;
            failMessage = null;
            okPath = null;
            localThreadInstance.set(previousInstance);
            if (cause instanceof String) {
                this.rejectPath = (String) cause;
            } else if (cause instanceof Enum) {
                this.rejectPath = ((Enum<?>) cause).name();
            } else if (cause instanceof Throwable) {
                this.rejectPath = cause.getClass().getSimpleName();
            } else if (cause != null) {
                this.rejectPath = cause.toString();
            } else {
                messageLogger.error(Markers.INCONSISTENT_REJECT, ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
            }

            if (messageLogger.isInfoEnabled()) {
                collectRuntimeStatus();
                collectPlatformStatus();
                if (messageLogger.isInfoEnabled()) {
                    final String message1 = readableWrite();
                    messageLogger.info(Markers.MSG_REJECT, message1);
                }
                if (dataLogger.isTraceEnabled()) {
                    final String message2 = write();
                    dataLogger.trace(Markers.DATA_REJECT, message2);
                }
                if (context != null) {
                    context.clear();
                }
            }
        } catch (final Exception t) {
            /* Prevents bugs from disrupting the application. Logs message with exception to provide stacktrace to bug. */
            messageLogger.error(Markers.BUG, ERROR_MSG_METHOD_THREW_EXCEPTION, "reject", getFullID(), t);
        }
        return this;
    }

    // ========================================================================

    /**
     * Refuses the meter in order to claim incomplete or inconsistent execution of the task represented by the meter. Sends a message with the the exception to
     * logger using warn level. Sends a message with system status, statistics and complete context to log using trace level.
     *
     * @param cause Exception that represents the failure. May be null if no exception applies.
     * @return reference to the meter itself.
     */
    public Meter fail(final Object cause) {
        try {
            final long newStopTime = System.nanoTime();

            /* Sanity check. Logs message and exception with stacktrace forged to the inconsistent caller method. */
            if (stopTime != 0) {
                messageLogger.error(Markers.INCONSISTENT_FAIL, ERROR_MSG_METER_ALREADY_STOPPED, getFullID(), new IllegalMeterUsage(2));
            } else if (startTime == 0) {
                messageLogger.error(Markers.INCONSISTENT_FAIL, ERROR_MSG_METER_STOPPED_BUT_NOT_STARTED, getFullID(), new IllegalMeterUsage(2));
            } else if (checkCurrentInstance()) {
                messageLogger.error(Markers.INCONSISTENT_FAIL, ERROR_MSG_METER_OUT_OF_ORDER, getFullID(), new IllegalMeterUsage(2));
            }

            stopTime = newStopTime;
            rejectPath = null;
            okPath = null;
            localThreadInstance.set(previousInstance);
            if (cause instanceof String) {
                this.failPath = (String) cause;
            } else if (cause instanceof Enum) {
                this.failPath = ((Enum<?>) cause).name();
            } else if (cause instanceof Throwable) {
                failPath = cause.getClass().getName();
                failMessage = ((Throwable)cause).getLocalizedMessage();
            } else if (cause != null) {
                this.failPath = cause.toString();
            } else {
                messageLogger.error(Markers.INCONSISTENT_FAIL, ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
            }

            if (messageLogger.isErrorEnabled()) {
                collectRuntimeStatus();
                collectPlatformStatus();
                messageLogger.error(Markers.MSG_FAIL, readableWrite(), cause);
                if (dataLogger.isTraceEnabled()) {
                    dataLogger.trace(Markers.DATA_FAIL, write());
                }
                if (context != null) {
                    context.clear();
                }
            }
        } catch (final Exception t) {
            /* Prevents bugs from disrupting the application. Logs message with exception to provide stacktrace to bug. */
            messageLogger.error(Markers.BUG, ERROR_MSG_METHOD_THREW_EXCEPTION, "fail", getFullID(), t);
        }
        return this;
    }

    // ========================================================================

    /**
     * Checks if meters the meter has been forgotten to be confirmed or refused. Useful to track those meters that do not follow the start(), ok()/fail() idiom
     * for all execution flows. Meters created to represent unknown instance in {@link #checkCurrentInstance()} are not considered for check.
     */
    @Override
    protected void finalize() throws Throwable {
        if (stopTime == 0 && ! category.equals(UNKNOWN_LOGGER_NAME)) {
            /* Logs only message. Stacktrace will not contain useful hints. Exception is logged only for visibility of inconsistent meter usage. */
            messageLogger.error(Markers.INCONSISTENT_FINALIZED, ERROR_MSG_METER_STARTED_AND_NEVER_STOPPED, getFullID(), new IllegalMeterUsage(1));
        }
        super.finalize();
    }

    @SuppressWarnings("ExtendsThrowable")
    public static class MeterThrowable extends Throwable {

        private static final long serialVersionUID = 1L;

        MeterThrowable(final int framesToDiscard) {
            this(framesToDiscard + 1, null);
        }

        @SuppressWarnings("AssignmentToMethodParameter")
        MeterThrowable(int framesToDiscard, final Throwable e) {
            super(e);
            framesToDiscard++;
            StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
            while (MY_CLASS_NAME.equals(stacktrace[framesToDiscard].getClassName())) {
                framesToDiscard++;
            }
            stacktrace = Arrays.copyOfRange(stacktrace, framesToDiscard, stacktrace.length);
            setStackTrace(stacktrace);
        }

        MeterThrowable() {
            super("Illegal Meter usage.");
        }

        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }

    public static class IllegalMeterUsage extends MeterThrowable {

        private static final long serialVersionUID = 1L;

        IllegalMeterUsage(final int framesToDiscard) {
            super(framesToDiscard);
        }

        IllegalMeterUsage(final int framesToDiscard, final Throwable e) {
            super(framesToDiscard, e);
        }
    }

    // ========================================================================

    /**
     * Compliance with {@link Closeable}. Assumes failure and refuses the meter if the meter has not yet been marked as confirmed.
     */
    @Override
    public void close() {
        try {
            if (stopTime != 0) {
                return;
            }

            final long newStopTime = System.nanoTime();

            /* Sanity check. Logs message and exception with stacktrace forged to the inconsistent caller method. */
            if (stopTime != 0) {
                messageLogger.error(Markers.INCONSISTENT_CLOSE, ERROR_MSG_METER_ALREADY_STOPPED, getFullID(), new IllegalMeterUsage(2));
            } else if (startTime == 0) {
                messageLogger.error(Markers.INCONSISTENT_CLOSE, ERROR_MSG_METER_STOPPED_BUT_NOT_STARTED, getFullID(), new IllegalMeterUsage(2));
            } else if (checkCurrentInstance()) {
                messageLogger.error(Markers.INCONSISTENT_CLOSE, ERROR_MSG_METER_OUT_OF_ORDER, getFullID(), new IllegalMeterUsage(2));
            }

            stopTime = newStopTime;
            rejectPath = null;
            okPath = null;
            localThreadInstance.set(previousInstance);
            failPath = "try-with-resources";

            if (messageLogger.isErrorEnabled()) {
                collectRuntimeStatus();
                collectPlatformStatus();
                messageLogger.error(Markers.MSG_FAIL, readableWrite(), failPath);
                if (dataLogger.isTraceEnabled()) {
                    dataLogger.trace(Markers.DATA_FAIL, write());
                }
                if (context != null) {
                    context.clear();
                }
            }
        } catch (final Exception t) {
            /* Prevents bugs from disrupting the application. Logs message with exception to provide stacktrace to bug. */
            messageLogger.error(Markers.BUG, ERROR_MSG_METHOD_THREW_EXCEPTION, "close", getFullID(), t);
        }
    }

    // ========================================================================
    public void run(final Runnable runnable) {
        if (startTime == 0) start();
        try {
            runnable.run();
            if (stopTime == 0) ok();
        } catch (final RuntimeException e) {
            fail(e);
            throw e;
        }
    }

    public <T> T call(final Callable<T> callable) throws Exception {
        if (startTime == 0) start();
        try {
            final T result = callable.call();
            ctx("result", result);
            if (stopTime == 0) ok();
            return result;
        } catch (final Exception e) {
            fail(e);
            throw e;
        }
    }

    public <T> T safeCall(final Callable<T> callable) {
        if (startTime == 0) start();
        try {
            final T result = callable.call();
            ctx("result", result);
            if (stopTime == 0) ok();
            return result;
        } catch (final RuntimeException e) {
            fail(e);
            throw e;
        } catch (final Exception e) {
            fail(e);
            throw new RuntimeException("Meter.safeCall wrapped exception.", e);
        }
    }

    public <E extends RuntimeException, T> T safeCall(final Class<E> exceptionClass, final Callable<T> callable) {
        if (stopTime == 0) start();
        try {
            final T result = callable.call();
            ctx("result", result);
            if (stopTime == 0) ok();
            return result;
        } catch (final RuntimeException e) {
            fail(e);
            throw e;
        } catch (final Exception e) {
            fail(e);
            throw convertException(exceptionClass, e);
        }
    }

    private <T extends RuntimeException> RuntimeException convertException(final Class<T> exceptionClass, final Exception e) {
        final String message = "Failed: " + (this.description != null ? this.description : this.category);
        try {
            return exceptionClass.getConstructor(String.class, Throwable.class).newInstance(message, e);
        } catch (final NoSuchMethodException ignored) {
            messageLogger.error(Markers.INCONSISTENT_EXCEPTION, ERROR_MSG_METER_CANNOT_CREATE_EXCEPTION, exceptionClass, e);
        } catch (final SecurityException ignored) {
            messageLogger.error(Markers.INCONSISTENT_EXCEPTION, ERROR_MSG_METER_CANNOT_CREATE_EXCEPTION, exceptionClass, e);
        } catch (final InstantiationException ignored) {
            messageLogger.error(Markers.INCONSISTENT_EXCEPTION, ERROR_MSG_METER_CANNOT_CREATE_EXCEPTION, exceptionClass, e);
        } catch (final IllegalAccessException ignored) {
            messageLogger.error(Markers.INCONSISTENT_EXCEPTION, ERROR_MSG_METER_CANNOT_CREATE_EXCEPTION, exceptionClass, e);
        } catch (final IllegalArgumentException ignored) {
            messageLogger.error(Markers.INCONSISTENT_EXCEPTION, ERROR_MSG_METER_CANNOT_CREATE_EXCEPTION, exceptionClass, e);
        } catch (final InvocationTargetException ignored) {
            messageLogger.error(Markers.INCONSISTENT_EXCEPTION, ERROR_MSG_METER_CANNOT_CREATE_EXCEPTION, exceptionClass, e);
        }
        return new RuntimeException(e);
    }
}
