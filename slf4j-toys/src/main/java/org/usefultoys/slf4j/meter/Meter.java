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
package org.usefultoys.slf4j.meter;

import lombok.NonNull;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.usefultoys.slf4j.LoggerFactory;
import org.usefultoys.slf4j.Session;
import org.usefultoys.slf4j.internal.SystemMetrics;

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
 * The `Meter` is a core component of `slf4j-toys` designed to track the **lifecycle** of application operations.
 * It collects system status and reports it to the logger at key points: operation start, progress, and termination
 * (success, rejection, or failure).
 * <p>
 * Each `Meter` instance allows you to:
 * <ul>
 *     <li>Call {@link #start()} to log the operation's beginning and current system status (DEBUG level).</li>
 *     <li>Call {@link #ok()} to log successful completion (INFO level).</li>
 *     <li>Call {@link #reject(Object)} to log expected termination due to business rules (INFO level).</li>
 *     <li>Call {@link #fail(Object)} to log unexpected technical failure (ERROR level).</li>
 *     <li>Call {@link #progress()} to log intermediate progress and system status (INFO level, periodically).</li>
 * </ul>
 * All **lifecycle** events also generate a **machine-parsable data message** at TRACE level.
 *
 * @author Daniel Felix Ferber
 * @see MeterData
 * @see MeterConfig
 * @see Markers
 */
@SuppressWarnings({"OverlyBroadCatchBlock", "FinalizeDeclaration"})
public class Meter extends MeterData implements Closeable {

    private static final long serialVersionUID = 1L;

    /** Error message for when Meter cannot create an exception of a specific type. */
    private static final String ERROR_MSG_METER_CANNOT_CREATE_EXCEPTION = "Meter cannot create exception of type {}.";
    /** Error message for when Meter's start() method is called multiple times. */
    private static final String ERROR_MSG_METER_ALREADY_STARTED = "Meter already started. id={}";
    /** Error message for when Meter's termination method (ok, reject, fail) is called multiple times. */
    private static final String ERROR_MSG_METER_ALREADY_STOPPED = "Meter already stopped. id={}";
    /** Error message for when Meter's termination method is called before start(). */
    private static final String ERROR_MSG_METER_STOPPED_BUT_NOT_STARTED = "Meter stopped but not started. id={}";
    /** Error message for when Meter is garbage-collected without being stopped. */
    private static final String ERROR_MSG_METER_STARTED_AND_NEVER_STOPPED = "Meter started and never stopped. id={}";
    /** Error message for when Meter's iteration increment method is called before start(). */
    private static final String ERROR_MSG_METER_INCREMENTED_BUT_NOT_STARTED = "Meter incremented but not started. id={}";
    /** Error message for when Meter's progress() method is called before start(). */
    private static final String ERROR_MSG_METER_PROGRESS_BUT_NOT_STARTED = "Meter progress but not started. id={}";
    /** Error message for when an internal Meter method throws an unexpected exception. */
    private static final String ERROR_MSG_METHOD_THREW_EXCEPTION = "Meter.{}(...) method threw exception. id={}";
    /** Error message for illegal arguments passed to Meter methods. */
    private static final String ERROR_MSG_ILLEGAL_ARGUMENT = "Illegal call to Meter.{}: {}. id={}";
    /** Error message for Meter lifecycle methods called out of expected order. */
    private static final String ERROR_MSG_METER_OUT_OF_ORDER = "Meter out of order. id={}";
    /** Generic error message for null arguments. */
    private static final String ERROR_MSG_NULL_ARGUMENT = "Null argument";
    /** Generic error message for non-positive arguments. */
    private static final String ERROR_MSG_NON_POSITIVE_ARGUMENT = "Non-positive argument";
    /** Generic error message for illegal string format. */
    private static final String ERROR_MSG_ILLEGAL_STRING_FORMAT = "Illegal string format";
    /** Error message for non-forward iteration (e.g., incTo with a smaller value). */
    private static final String ERROR_MSG_NON_FORWARD_ITERATION = "Non-forward iteration";
    /** The fully qualified name of this class, used for stack trace manipulation. */
    private static final String MY_CLASS_NAME = Meter.class.getName();
    /** Placeholder string for null values in context. */
    private static final String NULL_VALUE = "<null>";
    /** Placeholder string for unknown logger names. */
    private static final String UNKNOWN_LOGGER_NAME = "???";
    /** Default failure path for operations terminated by `try-with-resources`. */
    private static final String FAIL_PATH_TRY_WITH_RESOURCES = "try-with-resources";
    /** Context key for storing the result of functional interface calls. */
    public static final String CONTEXT_RESULT = "result";

    /** Logger for human-readable messages. */
    private final transient Logger messageLogger;
    /** Logger for machine-parsable data. */
    private final transient Logger dataLogger;

    /**
     * Tracks how many times each unique operation (category/operation name pair) has been executed.
     */
    static final ConcurrentMap<String, AtomicLong> EVENT_COUNTER = new ConcurrentHashMap<>();
    /**
     * Timestamp (in nanoseconds) when progress was last reported. Zero if progress has not been reported yet. Used to
     * control the frequency of progress messages and avoid flooding the log.
     */
    private transient long lastProgressTime = 0;
    /**
     * Iteration count when progress was last reported. Zero if progress has not been reported yet. Used to control the
     * frequency of progress messages.
     */
    private transient long lastProgressIteration = 0;

    /**
     * Tracks the `Meter` instance most recently started on the current thread.
     */
    private static final ThreadLocal<WeakReference<Meter>> localThreadInstance = new ThreadLocal<>();
    /**
     * Stores a weak reference to the `Meter` instance that was current before this `Meter` became the current one.
     * These references form a linked list representing a stack of `Meter` instances.
     */
    private WeakReference<Meter> previousInstance;

    /**
     * Creates a new `Meter` for an operation belonging to the category derived from the logger's name.
     *
     * @param logger The SLF4J logger that will report messages.
     */
    public Meter(final @NonNull Logger logger) {
        this(logger, null);
    }

    /**
     * Creates a new `Meter` for a specific operation within the category derived from the logger's name.
     *
     * @param logger    The SLF4J logger that will report messages.
     * @param operation The name of the operation, or {@code null} if the category itself describes the operation.
     */
    public Meter(final @NonNull Logger logger, final String operation) {
        this(logger, operation, null);
    }

    /**
     * Creates a new `Meter` for an operation, optionally as a child of an existing `Meter`. The category is derived
     * from the logger's name.
     *
     * @param logger    The SLF4J logger that will report messages.
     * @param operation The name of the operation, or {@code null}.
     * @param parent    The full ID of the parent `Meter`, or {@code null} if this is a top-level operation.
     */
    public Meter(final @NonNull Logger logger, final String operation, final String parent) {
        super(Session.shortSessionUuid(),
                extractNextPosition(logger.getName(), operation),
                logger.getName(), operation, parent);
        createTime = collectCurrentTime();
        messageLogger = org.slf4j.LoggerFactory.getLogger(messagePrefix + logger.getName() + messageSuffix);
        dataLogger = org.slf4j.LoggerFactory.getLogger(dataPrefix + logger.getName() + dataSuffix);
    }

    /**
     * Extracts and increments the next sequential position for a given operation. This ensures a unique, time-ordered
     * ID for each operation execution.
     *
     * @param eventCategory The category of the event.
     * @param operationName The name of the operation.
     * @return The next sequential position for the operation.
     */
    private static long extractNextPosition(final String eventCategory, final String operationName) {
        final String key = operationName == null ? eventCategory : eventCategory + "/" + operationName;
        EVENT_COUNTER.putIfAbsent(key, new AtomicLong(0));
        final AtomicLong atomicLong = EVENT_COUNTER.get(key);
        atomicLong.compareAndSet(Long.MAX_VALUE, 0);
        return atomicLong.incrementAndGet();
    }

    /**
     * Returns the `Meter` instance most recently started on the current thread. This is useful for accessing the
     * current operation's context.
     *
     * @return The current `Meter` instance, or a dummy `Meter` if none is active on the current thread.
     */
    public static Meter getCurrentInstance() {
        final WeakReference<Meter> ref = localThreadInstance.get();
        final Meter current = ref == null ? null : ref.get();
        if (current == null) {
            return new Meter(LoggerFactory.getLogger(UNKNOWN_LOGGER_NAME));
        }
        return current;
    }

    /**
     * Collects the current waiting time of the operation.
     *
     * @return The time elapsed (in nanoseconds) since the Meter was created until it started, or until now if not yet
     * started.
     */
    public long collectCurrentWaitingTime() {
        if (startTime == 0) {
            return collectCurrentTime() - createTime;
        }
        return startTime - createTime;
    }

    /**
     * Collects the current execution time of the operation.
     *
     * @return The time elapsed (in nanoseconds) since the operation started until now, or 0 if not yet started.
     */
    public long collectCurrentExecutionTime() {
        if (startTime == 0) {
            return 0;
        } else if (stopTime == 0) {
            return collectCurrentTime() - startTime;
        }
        return stopTime - startTime;
    }

    // ========================================================================

    /**
     * Creates a new `Meter` instance representing a sub-operation of this `Meter`. The new `Meter` inherits the
     * category of this `Meter` and its context.
     *
     * @param suboperationName The name of the sub-operation.
     * @return A new `Meter` instance for the sub-operation.
     */
    public Meter sub(final String suboperationName) {
        if (suboperationName == null) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            messageLogger.error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "sub(name)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
        }
        String operation = null;
        if (this.operation == null) {
            operation = suboperationName;
        } else if (suboperationName == null) {
            operation = this.operation;
        } else {
            operation = this.operation + "/" + suboperationName;
        }
        final Meter m = new Meter(messageLogger, operation, getFullID());
        if (context != null) {
            m.context = new HashMap<>(context);
        }
        return m;
    }

    // ========================================================================

    /**
     * Configures the `Meter` with a human-readable message that explains the operation's purpose.
     *
     * @param message The descriptive message.
     * @return Reference to this `Meter` instance, for method chaining.
     */
    public Meter m(final String message) {
        if (message == null) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            messageLogger.error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "m(message)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
        }
        description = message;
        return this;
    }

    /**
     * Configures the `Meter` with a human-readable message that explains the operation's purpose, using a format
     * string.
     *
     * @param format The message format string (e.g., `String.format(java.lang.String, java.lang.Object...)`).
     * @param args   The arguments for the format string.
     * @return Reference to this `Meter` instance, for method chaining.
     */
    public Meter m(final String format, final Object... args) {
        if (format == null) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            messageLogger.error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "m(message, args...)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
            description = null;
            return this;
        }
        try {
            description = String.format(format, args);
        } catch (final IllegalFormatException e) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            messageLogger.error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "m(message, args...)", ERROR_MSG_ILLEGAL_STRING_FORMAT, getFullID(), new IllegalMeterUsage(2, e));
        }
        return this;
    }

    /**
     * Configures the `Meter` with a time limit (threshold) for the operation's execution. If the operation exceeds this
     * limit, it will be flagged as "slow".
     *
     * @param timeLimit The time limit in milliseconds. Must be a positive value.
     * @return Reference to this `Meter` instance, for method chaining.
     */
    public Meter limitMilliseconds(final long timeLimit) {
        if (timeLimit <= 0) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            messageLogger.error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "limitMilliseconds(timeLimit)", ERROR_MSG_NON_POSITIVE_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
            return this;
        }
        this.timeLimit = timeLimit * 1000 * 1000; // Convert milliseconds to nanoseconds
        return this;
    }

    /**
     * Configures the `Meter` for an operation composed of multiple iterations or steps. This enables progress tracking
     * using {@link #inc()}, {@link #incBy(long)}, {@link #incTo(long)}, and {@link #progress()}.
     *
     * @param expectedIterations The total number of expected iterations or steps for the task. Must be a positive
     *                           value.
     * @return Reference to this `Meter` instance, for method chaining.
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
     * Adds a key-only entry to the context map. This is interpreted as a marker or flag.
     *
     * @param name The key of the entry to add. Must not be {@code null}.
     * @return Reference to this `Meter` instance, for method chaining.
     */
    public Meter ctx(final String name) {
        if (name == null) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            messageLogger.error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "ctx(name)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
            return this;
        }
        if (context == null) {
            context = new LinkedHashMap<>();
        }
        context.put(name, null);
        return this;
    }

    /**
     * Conditionally adds a key-only entry to the context map if the condition is {@code true}.
     *
     * @param condition The condition to evaluate.
     * @param trueName  The key of the entry to add if {@code condition} is {@code true}. Must not be {@code null}.
     * @return Reference to this `Meter` instance, for method chaining.
     */
    public Meter ctx(final boolean condition, final String trueName) {
        if (!condition) {
            return this;
        }
        if (trueName == null) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            messageLogger.error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "ctx(condition, trueName)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
            return this;
        }
        if (context == null) {
            context = new LinkedHashMap<>();
        }
        context.put(trueName, null);
        return this;
    }

    /**
     * Conditionally adds a key-only entry to the context map based on a boolean condition.
     *
     * @param condition The condition to evaluate.
     * @param trueName  The key of the entry to add if {@code condition} is {@code true}. Must not be {@code null}.
     * @param falseName The key of the entry to add if {@code condition} is {@code false}. Must not be {@code null}.
     * @return Reference to this `Meter` instance, for method chaining.
     */
    public Meter ctx(final boolean condition, final String trueName, final String falseName) {
        if (condition) {
            if (trueName == null) {
                /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
                messageLogger.error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "ctx(condition, trueName, falseName)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
                return this;
            }
            if (context == null) {
                context = new LinkedHashMap<>();
            }
            context.put(trueName, null);
        } else {
            if (falseName == null) {
                /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
                messageLogger.error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "ctx(condition, trueName, falseName)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
                return this;
            }
            if (context == null) {
                context = new LinkedHashMap<>();
            }
            context.put(falseName, null);
        }
        return this;
    }

    /**
     * Adds a key-value entry to the context map with an integer value.
     *
     * @param name  The key of the entry to add. Must not be {@code null}.
     * @param value The integer value.
     * @return Reference to this `Meter` instance, for method chaining.
     */
    public Meter ctx(final String name, final int value) {
        if (name == null) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            messageLogger.error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "ctx(name, value)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
            return this;
        }
        if (context == null) {
            context = new LinkedHashMap<>();
        }
        context.put(name, Integer.toString(value));
        return this;
    }

    /**
     * Adds a key-value entry to the context map with a long value.
     *
     * @param name  The key of the entry to add. Must not be {@code null}.
     * @param value The long value.
     * @return Reference to this `Meter` instance, for method chaining.
     */
    public Meter ctx(final String name, final long value) {
        if (name == null) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            messageLogger.error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "ctx(name, value)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
            return this;
        }
        if (context == null) {
            context = new LinkedHashMap<>();
        }
        context.put(name, Long.toString(value));
        return this;
    }

    /**
     * Adds a key-value entry to the context map with a boolean value.
     *
     * @param name  The key of the entry to add. Must not be {@code null}.
     * @param value The boolean value.
     * @return Reference to this `Meter` instance, for method chaining.
     */
    public Meter ctx(final String name, final boolean value) {
        if (name == null) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            messageLogger.error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "ctx(name, value)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
            return this;
        }
        if (context == null) {
            context = new LinkedHashMap<>();
        }
        context.put(name, Boolean.toString(value));
        return this;
    }

    /**
     * Adds a key-value entry to the context map with a float value.
     *
     * @param name  The key of the entry to add. Must not be {@code null}.
     * @param value The float value.
     * @return Reference to this `Meter` instance, for method chaining.
     */
    public Meter ctx(final String name, final float value) {
        if (name == null) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            messageLogger.error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "ctx(name, value)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
            return this;
        }
        if (context == null) {
            context = new LinkedHashMap<>();
        }
        context.put(name, Float.toString(value));
        return this;
    }

    /**
     * Adds a key-value entry to the context map with a double value.
     *
     * @param name  The key of the entry to add. Must not be {@code null}.
     * @param value The double value.
     * @return Reference to this `Meter` instance, for method chaining.
     */
    public Meter ctx(final String name, final double value) {
        if (name == null) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            messageLogger.error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "ctx(name, value)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
            return this;
        }
        if (context == null) {
            context = new LinkedHashMap<>();
        }
        context.put(name, Double.toString(value));
        return this;
    }

    /**
     * Adds a key-value entry to the context map with an {@link Integer} object value.
     *
     * @param name  The key of the entry to add. Must not be {@code null}.
     * @param value The {@link Integer} object value. {@code null} values are represented by {@code "<null>"}.
     * @return Reference to this `Meter` instance, for method chaining.
     */
    public Meter ctx(final String name, final Integer value) {
        if (name == null) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            messageLogger.error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "ctx(name, value)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
            return this;
        }
        if (context == null) {
            context = new LinkedHashMap<>();
        }
        context.put(name, value == null ? NULL_VALUE : value.toString());
        return this;
    }

    /**
     * Adds a key-value entry to the context map with a {@link Long} object value.
     *
     * @param name  The key of the entry to add. Must not be {@code null}.
     * @param value The {@link Long} object value. {@code null} values are represented by {@code "<null>"}.
     * @return Reference to this `Meter` instance, for method chaining.
     */
    public Meter ctx(final String name, final Long value) {
        if (name == null) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            messageLogger.error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "ctx(name, value)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
            return this;
        }
        if (context == null) {
            context = new LinkedHashMap<>();
        }
        context.put(name, value == null ? NULL_VALUE : value.toString());
        return this;
    }

    /**
     * Adds a key-value entry to the context map with a {@link Boolean} object value.
     *
     * @param name  The key of the entry to add. Must not be {@code null}.
     * @param value The {@link Boolean} object value. {@code null} values are represented by {@code "<null>"}.
     * @return Reference to this `Meter` instance, for method chaining.
     */
    public Meter ctx(final String name, final Boolean value) {
        if (name == null) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            messageLogger.error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "ctx(name, value)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
            return this;
        }
        if (context == null) {
            context = new LinkedHashMap<>();
        }
        context.put(name, value == null ? NULL_VALUE : value.toString());
        return this;
    }

    /**
     * Adds a key-value entry to the context map with a {@link Float} object value.
     *
     * @param name  The key of the entry to add. Must not be {@code null}.
     * @param value The {@link Float} object value. {@code null} values are represented by {@code "<null>"}.
     * @return Reference to this `Meter` instance, for method chaining.
     */
    public Meter ctx(final String name, final Float value) {
        if (name == null) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            messageLogger.error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "ctx(name, value)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
            return this;
        }
        if (context == null) {
            context = new LinkedHashMap<>();
        }
        context.put(name, value == null ? NULL_VALUE : value.toString());
        return this;
    }

    /**
     * Adds a key-value entry to the context map with a {@link Double} object value.
     *
     * @param name  The key of the entry to add. Must not be {@code null}.
     * @param value The {@link Double} object value. {@code null} values are represented by {@code "<null>"}.
     * @return Reference to this `Meter` instance, for method chaining.
     */
    public Meter ctx(final String name, final Double value) {
        if (name == null) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            messageLogger.error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "ctx(name, value)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
            return this;
        }
        if (context == null) {
            context = new LinkedHashMap<>();
        }
        context.put(name, value == null ? NULL_VALUE : value.toString());
        return this;
    }

    /**
     * Adds a key-value entry to the context map, using the {@code toString()} representation of an object as value.
     *
     * @param name   The key of the entry to add. Must not be {@code null}.
     * @param object The object whose string representation will be used as the value. {@code null} objects are
     *               represented by {@code "<null>"}.
     * @return Reference to this `Meter` instance, for method chaining.
     */
    public Meter ctx(final String name, final Object object) {
        if (name == null) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            messageLogger.error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "ctx(name, value)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
            return this;
        }
        if (context == null) {
            context = new LinkedHashMap<>();
        }
        context.put(name, object == null ? NULL_VALUE : object.toString());
        return this;
    }

    /**
     * Adds a key-value entry to the context map with a string value.
     *
     * @param name  The key of the entry to add. Must not be {@code null}.
     * @param value The string value. {@code null} values are represented by {@code "<null>"}.
     * @return Reference to this `Meter` instance, for method chaining.
     */
    public Meter ctx(final String name, final String value) {
        if (name == null) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            messageLogger.error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "ctx(name, value)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
            return this;
        }
        if (context == null) {
            context = new LinkedHashMap<>();
        }
        context.put(name, value == null ? NULL_VALUE : value);
        return this;
    }

    /**
     * Adds a key-value entry to the context map, where the value is a formatted message.
     *
     * @param name   The key of the entry to add. Must not be {@code null}.
     * @param format The message format string (e.g., `String.format(java.lang.String, java.lang.Object...)`). Must not
     *               be {@code null}.
     * @param args   The arguments for the format string.
     * @return Reference to this `Meter` instance, for method chaining.
     */
    public Meter ctx(final String name, final String format, final Object... args) {
        if (name == null || format == null) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            messageLogger.error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "ctx(name, format, args...)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
            return this;
        }
        if (context == null) {
            context = new LinkedHashMap<>();
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
     * @param name The key of the entry to remove. Must not be {@code null}.
     * @return Reference to this `Meter` instance, for method chaining.
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
     * Notifies the `Meter` that the operation has started. This method logs a **human-readable summary** (DEBUG level)
     * and a **machine-parsable data message** (TRACE level) with the current system status.
     *
     * @return Reference to this `Meter` instance, for method chaining.
     */
    public Meter start() {
        try {
            if (startTime != 0) {
                /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
                messageLogger.error(Markers.INCONSISTENT_START, ERROR_MSG_METER_ALREADY_STARTED, getFullID(), new IllegalMeterUsage(2));
            } else {
                previousInstance = localThreadInstance.get();
                localThreadInstance.set(new WeakReference<>(this));
            }

            lastProgressTime = startTime = collectCurrentTime();

            if (messageLogger.isDebugEnabled()) {
                SystemMetrics.getInstance().collectRuntimeStatus(this);
                SystemMetrics.getInstance().collectPlatformStatus(this);
                messageLogger.debug(Markers.MSG_START, readableMessage());
                if (dataLogger.isTraceEnabled()) {
                    dataLogger.trace(Markers.DATA_START, json5Message());
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
     * Notifies the `Meter` that one more iteration or step of the task has completed successfully.
     *
     * @return Reference to this `Meter` instance, for method chaining.
     */
    public Meter inc() {
        if (startTime == 0) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            messageLogger.error(Markers.INCONSISTENT_INCREMENT, ERROR_MSG_METER_INCREMENTED_BUT_NOT_STARTED, getFullID(), new IllegalMeterUsage(4));
        }
        currentIteration++;
        return this;
    }

    /**
     * Notifies the `Meter` that a specified number of iterations or steps have completed successfully.
     *
     * @param increment The number of iterations or steps to add to the current count. Must be positive.
     * @return Reference to this `Meter` instance, for method chaining.
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
        currentIteration += increment;
        return this;
    }

    /**
     * Notifies the `Meter` that the operation has reached a specific iteration count.
     *
     * @param currentIteration The new total number of iterations or steps completed. Must be greater than the previous
     *                         count.
     * @return Reference to this `Meter` instance, for method chaining.
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
     * Reports the current progress of the operation. This is only applicable for `Meter` instances configured with
     * {@link #iterations(long)}. A progress message is logged (INFO level) only periodically and if progress has
     * actually advanced, to minimize performance degradation.
     *
     * @return Reference to this `Meter` instance, for method chaining.
     */
    public Meter progress() {
        try {
            if (startTime == 0) {
                /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
                messageLogger.error(Markers.INCONSISTENT_PROGRESS, ERROR_MSG_METER_PROGRESS_BUT_NOT_STARTED, getFullID(), new IllegalMeterUsage(2));
            }

            final long now = lastCurrentTime = collectCurrentTime();
            final long meterProgressPeriodNanoseconds = MeterConfig.progressPeriodMilliseconds * 1000 * 1000;
            if (currentIteration > lastProgressIteration && (now - lastProgressTime) > meterProgressPeriodNanoseconds) {
                lastProgressIteration = currentIteration;
                lastProgressTime = now;

                if (messageLogger.isInfoEnabled()) {
                    SystemMetrics.getInstance().collectRuntimeStatus(this);
                    SystemMetrics.getInstance().collectPlatformStatus(this);
                    messageLogger.info(Markers.MSG_PROGRESS, readableMessage());
                    if (dataLogger.isTraceEnabled()) {
                        final String message2 = json5Message();
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

    /**
     * Sets the success path identifier for the operation. This is typically used with {@link #ok()} to distinguish
     * between different successful outcomes.
     *
     * @param pathId An object (String, Enum, Throwable, or any Object with a meaningful `toString()`) that identifies
     *               the successful execution path.
     * @return Reference to this `Meter` instance, for method chaining.
     */
    public Meter path(final Object pathId) {
        if (pathId instanceof String) {
            okPath = (String) pathId;
        } else if (pathId instanceof Enum) {
            okPath = ((Enum<?>) pathId).name();
        } else if (pathId instanceof Throwable) {
            okPath = pathId.getClass().getSimpleName();
        } else if (pathId != null) {
            okPath = pathId.toString();
        } else {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            messageLogger.error(Markers.ILLEGAL, ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(4));
        }
        return this;
    }

    /**
     * Notifies the `Meter` that the operation has completed successfully. This method logs a **human-readable summary**
     * (INFO level) and a **machine-parsable data message** (TRACE level) with the current system status. If a time
     * limit was set and exceeded, a WARN level message is logged instead.
     *
     * @return Reference to this `Meter` instance, for method chaining.
     */
    public Meter ok() {
        try {
            final long newStopTime = collectCurrentTime();

            /* Sanity check. Logs message and exception with stacktrace forged to the inconsistent caller method. */
            if (stopTime != 0) {
                messageLogger.error(Markers.INCONSISTENT_OK, ERROR_MSG_METER_ALREADY_STOPPED, getFullID(), new IllegalMeterUsage(2));
            } else if (startTime == 0) {
                messageLogger.error(Markers.INCONSISTENT_OK, ERROR_MSG_METER_STOPPED_BUT_NOT_STARTED, getFullID(), new IllegalMeterUsage(2));
            } else if (checkCurrentInstance()) {
                messageLogger.error(Markers.INCONSISTENT_OK, ERROR_MSG_METER_OUT_OF_ORDER, getFullID(), new IllegalMeterUsage(2));
            }

            stopTime = newStopTime;
            lastCurrentTime = newStopTime;
            failPath = null;
            failMessage = null;
            rejectPath = null;
            localThreadInstance.set(previousInstance);

            if (messageLogger.isWarnEnabled()) { // Check warn enabled to cover info as well
                SystemMetrics.getInstance().collectRuntimeStatus(this);
                SystemMetrics.getInstance().collectPlatformStatus(this);

                final boolean warnSlowness = startTime != 0 && timeLimit != 0 && stopTime - startTime > timeLimit;
                final String message1 = readableMessage();
                if (warnSlowness) {
                    messageLogger.warn(Markers.MSG_SLOW_OK, message1);
                } else if (messageLogger.isInfoEnabled()) {
                    messageLogger.info(Markers.MSG_OK, message1);
                }
                if (dataLogger.isTraceEnabled()) {
                    final String message2 = json5Message();
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
     * Checks if this `Meter` instance is the current `Meter` associated with the current thread.
     *
     * @return {@code true} if this `Meter` is not the current instance, {@code false} otherwise.
     */
    private boolean checkCurrentInstance() {
        final WeakReference<Meter> ref = localThreadInstance.get();
        return ref == null || ref.get() != this;
    }

    /**
     * Notifies the `Meter` that the operation has completed successfully, specifying an execution path.
     * This method logs a **human-readable summary** (INFO level) and a **machine-parsable data message** (TRACE level)
     * with the current system status. If a time limit was set and exceeded, a WARN level message is logged instead.
     *
     * @param pathId An object (String, Enum, Throwable, or any Object with a meaningful `toString()`) that identifies
     *               the successful execution path.
     * @return Reference to this `Meter` instance, for method chaining.
     */
    public Meter ok(final Object pathId) {
        try {
            final long newStopTime = collectCurrentTime();

            /* Sanity check. Logs message and exception with stacktrace forged to the inconsistent caller method. */
            if (stopTime != 0) {
                messageLogger.error(Markers.INCONSISTENT_OK, ERROR_MSG_METER_ALREADY_STOPPED, getFullID(), new IllegalMeterUsage(2));
            } else if (startTime == 0) {
                messageLogger.error(Markers.INCONSISTENT_OK, ERROR_MSG_METER_STOPPED_BUT_NOT_STARTED, getFullID(), new IllegalMeterUsage(2));
            } else if (checkCurrentInstance()) {
                messageLogger.error(Markers.INCONSISTENT_OK, ERROR_MSG_METER_OUT_OF_ORDER, getFullID(), new IllegalMeterUsage(2));
            }

            stopTime = newStopTime;
            lastCurrentTime = newStopTime;
            failPath = null;
            failMessage = null;
            rejectPath = null;
            localThreadInstance.set(previousInstance);
            if (pathId instanceof String) {
                okPath = (String) pathId;
            } else if (pathId instanceof Enum) {
                okPath = ((Enum<?>) pathId).name();
            } else if (pathId instanceof Throwable) {
                okPath = pathId.getClass().getSimpleName();
            } else if (pathId != null) {
                okPath = pathId.toString();
            } else {
                /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
                messageLogger.error(Markers.INCONSISTENT_OK, ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(4));
            }

            if (messageLogger.isWarnEnabled()) {
                SystemMetrics.getInstance().collectRuntimeStatus(this);
                SystemMetrics.getInstance().collectPlatformStatus(this);

                final boolean warnSlowness = startTime != 0 && timeLimit != 0 && stopTime - startTime > timeLimit;
                final String message1 = readableMessage();
                if (warnSlowness) {
                    messageLogger.warn(Markers.MSG_SLOW_OK, message1);
                } else if (messageLogger.isInfoEnabled()) {
                    messageLogger.info(Markers.MSG_OK, message1);
                }
                if (dataLogger.isTraceEnabled()) {
                    final String message2 = json5Message();
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
     * Notifies the `Meter` that the operation has completed successfully. This is an alias for {@link #ok()}.
     * This method logs a **human-readable summary**
     * (INFO level) and a **machine-parsable data message** (TRACE level) with the current system status. If a time
     * limit was set and exceeded, a WARN level message is logged instead.
     *
     * @return Reference to this `Meter` instance, for method chaining.
     */
    public Meter success() {
        return ok();
    }

    /**
     * Notifies the `Meter` that the operation has completed successfully, specifying an execution path. This is an
     * alias for {@link #ok(Object)}.
     * This method logs a **human-readable summary** (INFO level) and a **machine-parsable data message** (TRACE level)
     * with the current system status. If a time limit was set and exceeded, a WARN level message is logged instead.
     *
     * @param pathId An object (String, Enum, Throwable, or any Object with a meaningful `toString()`) that identifies
     *               the successful execution path.
     * @return Reference to this `Meter` instance, for method chaining.
     */
    public Meter success(final Object pathId) {
        return ok(pathId);
    }

    /**
     * Notifies the `Meter` that the operation has completed with a rejection (expected unsuccessful outcome). This
     * method logs a **human-readable summary** (INFO level) and a **machine-parsable data message** (TRACE level) with
     * the current system status.
     *
     * @param cause An object (String, Enum, Throwable, or any Object with a meaningful `toString()`) that describes the
     *              cause of the rejection.
     * @return Reference to this `Meter` instance, for method chaining.
     */
    public Meter reject(final Object cause) {
        try {
            final long newStopTime = collectCurrentTime();

            /* Sanity check. Logs message and exception with stacktrace forged to the inconsistent caller method. */
            if (stopTime != 0) {
                messageLogger.error(Markers.INCONSISTENT_REJECT, ERROR_MSG_METER_ALREADY_STOPPED, getFullID(), new IllegalMeterUsage(2));
            } else if (startTime == 0) {
                messageLogger.error(Markers.INCONSISTENT_REJECT, ERROR_MSG_METER_STOPPED_BUT_NOT_STARTED, getFullID(), new IllegalMeterUsage(2));
            } else if (checkCurrentInstance()) {
                messageLogger.error(Markers.INCONSISTENT_REJECT, ERROR_MSG_METER_OUT_OF_ORDER, getFullID(), new IllegalMeterUsage(2));
            }

            stopTime = newStopTime;
            lastCurrentTime = newStopTime;
            failPath = null;
            failMessage = null;
            okPath = null;
            localThreadInstance.set(previousInstance);
            if (cause instanceof String) {
                rejectPath = (String) cause;
            } else if (cause instanceof Enum) {
                rejectPath = ((Enum<?>) cause).name();
            } else if (cause instanceof Throwable) {
                rejectPath = cause.getClass().getSimpleName();
            } else if (cause != null) {
                rejectPath = cause.toString();
            } else {
                messageLogger.error(Markers.INCONSISTENT_REJECT, ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
            }

            if (messageLogger.isInfoEnabled()) {
                SystemMetrics.getInstance().collectRuntimeStatus(this);
                SystemMetrics.getInstance().collectPlatformStatus(this);
                if (messageLogger.isInfoEnabled()) {
                    final String message1 = readableMessage();
                    messageLogger.info(Markers.MSG_REJECT, message1);
                }
                if (dataLogger.isTraceEnabled()) {
                    final String message2 = json5Message();
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
     * Notifies the `Meter` that the operation has failed due to an unexpected technical error. This method logs a
     * **human-readable summary** (ERROR level) and a **machine-parsable data message** (TRACE level) with the current
     * system status.
     *
     * @param cause An object (String, Enum, Throwable, or any Object with a meaningful `toString()`) that describes the
     *              cause of the failure. If it's a {@link Throwable}, its class name is used for `failPath` and its
     *              message for `failMessage`.
     * @return Reference to this `Meter` instance, for method chaining.
     */
    public Meter fail(final Object cause) {
        try {
            final long newStopTime = collectCurrentTime();

            /* Sanity check. Logs message and exception with stacktrace forged to the inconsistent caller method. */
            if (stopTime != 0) {
                messageLogger.error(Markers.INCONSISTENT_FAIL, ERROR_MSG_METER_ALREADY_STOPPED, getFullID(), new IllegalMeterUsage(2));
            } else if (startTime == 0) {
                messageLogger.error(Markers.INCONSISTENT_FAIL, ERROR_MSG_METER_STOPPED_BUT_NOT_STARTED, getFullID(), new IllegalMeterUsage(2));
            } else if (checkCurrentInstance()) {
                messageLogger.error(Markers.INCONSISTENT_FAIL, ERROR_MSG_METER_OUT_OF_ORDER, getFullID(), new IllegalMeterUsage(2));
            }

            stopTime = newStopTime;
            lastCurrentTime = newStopTime;
            rejectPath = null;
            okPath = null;
            localThreadInstance.set(previousInstance);
            if (cause instanceof String) {
                failPath = (String) cause;
            } else if (cause instanceof Enum) {
                failPath = ((Enum<?>) cause).name();
            } else if (cause instanceof Throwable) {
                failPath = cause.getClass().getName();
                failMessage = ((Throwable)cause).getLocalizedMessage();
            } else if (cause != null) {
                failPath = cause.toString();
            } else {
                messageLogger.error(Markers.INCONSISTENT_FAIL, ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
            }

            if (messageLogger.isErrorEnabled()) {
                SystemMetrics.getInstance().collectRuntimeStatus(this);
                SystemMetrics.getInstance().collectPlatformStatus(this);
                messageLogger.error(Markers.MSG_FAIL, readableMessage());
                if (dataLogger.isTraceEnabled()) {
                    dataLogger.trace(Markers.DATA_FAIL, json5Message());
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
     * Overrides the default `finalize()` method to detect `Meter` instances that were started but never explicitly
     * stopped. If an unstopped `Meter` is garbage-collected, an error message is logged to indicate inconsistent API
     * usage.
     *
     * @throws Throwable if an error occurs during finalization.
     */
    @Override
    protected void finalize() throws Throwable {
        if (stopTime == 0 && ! category.equals(UNKNOWN_LOGGER_NAME)) {
            /* Logs only message. Stacktrace will not contain useful hints. Exception is logged only for visibility of inconsistent meter usage. */
            messageLogger.error(Markers.INCONSISTENT_FINALIZED, ERROR_MSG_METER_STARTED_AND_NEVER_STOPPED, getFullID(), new IllegalMeterUsage(1));
        }
        super.finalize();
    }

    /**
     * Base class for custom throwables used internally by `Meter` for reporting illegal usage or internal bugs. It
     * manipulates the stack trace to point to the actual caller method, making debugging easier.
     */
    @SuppressWarnings("ExtendsThrowable")
    public static class MeterThrowable extends Throwable {

        private static final long serialVersionUID = 1L;

        /**
         * Constructs a `MeterThrowable` by discarding a specified number of frames from the stack trace.
         *
         * @param framesToDiscard The number of stack frames to remove from the beginning.
         */
        MeterThrowable(final int framesToDiscard) {
            this(framesToDiscard + 1, null);
        }

        /**
         * Constructs a `MeterThrowable` with a cause, discarding a specified number of frames from the stack trace.
         *
         * @param framesToDiscard The number of stack frames to remove from the beginning.
         * @param e               The underlying cause of this throwable.
         */
        @SuppressWarnings({"AssignmentToMethodParameter", "OverridableMethodCallDuringObjectConstruction"})
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

        /**
         * Constructs a `MeterThrowable` with a default message.
         */
        MeterThrowable() {
            super("Illegal Meter usage.");
        }

        /**
         * Overrides `fillInStackTrace()` to prevent it from capturing the stack trace again, as it's already
         * manipulated in the constructor.
         *
         * @return This `Throwable` instance.
         */
        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }

    /**
     * A specific `MeterThrowable` subclass indicating illegal usage of the `Meter` API.
     */
    public static class IllegalMeterUsage extends MeterThrowable {

        private static final long serialVersionUID = 1L;

        /**
         * Constructs an `IllegalMeterUsage` by discarding a specified number of frames from the stack trace.
         *
         * @param framesToDiscard The number of stack frames to remove from the beginning.
         */
        IllegalMeterUsage(final int framesToDiscard) {
            super(framesToDiscard);
        }

        /**
         * Constructs an `IllegalMeterUsage` with a cause, discarding a specified number of frames from the stack
         * trace.
         *
         * @param framesToDiscard The number of stack frames to remove from the beginning.
         * @param e               The underlying cause of this throwable.
         */
        IllegalMeterUsage(final int framesToDiscard, final Throwable e) {
            super(framesToDiscard, e);
        }
    }

    // ========================================================================

    /**
     * Implements the {@link Closeable} interface. If the `Meter` has not been explicitly stopped (via `ok()`,
     * `reject()`, or `fail()`), this method automatically marks the operation as {@code FAIL} with the path
     * {@code "try-with-resources"}. This ensures that no operation goes untracked when used in a `try-with-resources`
     * block.
     */
    @Override
    public void close() {
        try {
            if (stopTime != 0) {
                return;
            }

            final long newStopTime = collectCurrentTime();

            /* Sanity check. Logs message and exception with stacktrace forged to the inconsistent caller method. */
            if (stopTime != 0) {
                messageLogger.error(Markers.INCONSISTENT_CLOSE, ERROR_MSG_METER_ALREADY_STOPPED, getFullID(), new IllegalMeterUsage(2));
            } else if (startTime == 0) {
                messageLogger.error(Markers.INCONSISTENT_CLOSE, ERROR_MSG_METER_STOPPED_BUT_NOT_STARTED, getFullID(), new IllegalMeterUsage(2));
            } else if (checkCurrentInstance()) {
                messageLogger.error(Markers.INCONSISTENT_CLOSE, ERROR_MSG_METER_OUT_OF_ORDER, getFullID(), new IllegalMeterUsage(2));
            }

            stopTime = newStopTime;
            lastCurrentTime = newStopTime;
            rejectPath = null;
            okPath = null;
            localThreadInstance.set(previousInstance);
            failPath = FAIL_PATH_TRY_WITH_RESOURCES;

            if (messageLogger.isErrorEnabled()) {
                SystemMetrics.getInstance().collectRuntimeStatus(this);
                SystemMetrics.getInstance().collectPlatformStatus(this);
                messageLogger.error(Markers.MSG_FAIL, readableMessage(), failPath);
                if (dataLogger.isTraceEnabled()) {
                    dataLogger.trace(Markers.DATA_FAIL, json5Message());
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

    /**
     * Executes the given {@link Runnable} task within the `Meter`'s lifecycle control. The operation is automatically
     * started before execution and marked as {@code OK} upon successful completion. If the task throws a
     * {@link RuntimeException}, the operation is marked as {@code FAIL}, and the exception is rethrown.
     *
     * @param runnable The {@link Runnable} task to be executed.
     */
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

    /**
     * Executes the given {@link Runnable} task within the `Meter`'s lifecycle control. The operation is automatically
     * started before execution and marked as {@code OK} upon successful completion. If the task throws an exception
     * that matches one of {@code exceptionsToReject}, the operation is marked as {@code REJECT}. Otherwise, it's marked
     * as {@code FAIL}. The exception is always rethrown.
     *
     * @param runnable           The {@link Runnable} task to be executed.
     * @param exceptionsToReject A list of exception classes that should result in a {@code REJECT} status.
     * @throws Exception The original exception thrown by the runnable.
     */
    @SneakyThrows
    public void runOrReject(final Runnable runnable, final Class<? extends Exception>... exceptionsToReject) {
        if (startTime == 0L) {
            start();
        }

        try {
            runnable.run();
            if (stopTime == 0L) {
                ok();
            }
        } catch (final Exception e) {
            final int length = exceptionsToReject.length;
            for (final Class<? extends Exception> ee : exceptionsToReject) {
                if (ee.isAssignableFrom(e.getClass())) {
                    reject(e);
                    throw e;
                }
            }
            fail(e);
            throw e;
        }
    }

    /**
     * Executes the given {@link Callable} task within the `Meter`'s lifecycle control. The operation is automatically
     * started before execution and marked as {@code OK} upon successful completion. The result of the {@link Callable}
     * is stored in the context under {@link #CONTEXT_RESULT} and returned. If the task throws an {@link Exception}, the
     * operation is marked as {@code FAIL}, and the exception is rethrown.
     *
     * @param callable The {@link Callable} task to be executed.
     * @param <T>      The type of the result returned by the callable.
     * @return The result of the callable task.
     * @throws Exception The original exception thrown by the callable.
     */
    public <T> T call(final Callable<T> callable) throws Exception {
        if (startTime == 0) start();
        try {
            final T result = callable.call();
            ctx(CONTEXT_RESULT, result);
            if (stopTime == 0) ok();
            return result;
        } catch (final Exception e) {
            fail(e);
            throw e;
        }
    }

    /**
     * Executes the given {@link Callable} task within the `Meter`'s lifecycle control. The operation is automatically
     * started before execution and marked as {@code OK} upon successful completion. The result of the {@link Callable}
     * is stored in the context under {@code "result"} and returned. If the task throws a {@link RuntimeException}, the
     * operation is marked as {@code FAIL}. If it throws any other {@link Exception}, the operation is marked as
     * {@code REJECT}. The exception is always rethrown.
     *
     * @param callable The {@link Callable} task to be executed.
     * @param <T>      The type of the result returned by the callable.
     * @return The result of the callable task.
     * @throws Exception The original exception thrown by the callable.
     */
    @SneakyThrows
    public <T> T callOrRejectChecked(final Callable<T> callable) {
        if (startTime == 0L) {
            start();
        }

        try {
            final T result = callable.call();
            ctx("result", result);
            if (stopTime == 0L) {
                ok();
            }

            return result;
        } catch (final RuntimeException e) {
            fail(e);
            throw e;
        } catch (final Exception e) {
            reject(e);
            throw e;
        }
    }

    /**
     * Executes the given {@link Callable} task within the `Meter`'s lifecycle control. The operation is automatically
     * started before execution and marked as {@code OK} upon successful completion. The result of the {@link Callable}
     * is stored in the context under {@code "result"} and returned. If the task throws an exception that matches one of
     * {@code exceptionsToReject}, the operation is marked as {@code REJECT}. Otherwise, it's marked as {@code FAIL}.
     * The exception is always rethrown.
     *
     * @param callable           The {@link Callable} task to be executed.
     * @param exceptionsToReject A list of exception classes that should result in a {@code REJECT} status.
     * @param <T>                The type of the result returned by the callable.
     * @return The result of the callable task.
     * @throws Exception The original exception thrown by the callable.
     */
    @SneakyThrows
    public <T> T callOrReject(final Callable<T> callable, final Class<? extends Exception>... exceptionsToReject) {
        if (startTime == 0L) {
            start();
        }

        try {
            final T result = callable.call();
            ctx("result", result);
            if (stopTime == 0L) {
                ok();
            }

            return result;
        } catch (final Exception e) {
            for (final Class<? extends Exception> ee : exceptionsToReject) {
                if (ee.isAssignableFrom(e.getClass())) {
                    reject(e);
                    throw e;
                }
            }

            fail(e);
            throw e;
        }
    }

    /**
     * Executes the given {@link Callable} task within the `Meter`'s lifecycle control, ensuring that checked exceptions
     * are wrapped into a {@link RuntimeException}. The operation is automatically started before execution and marked
     * as {@code OK} upon successful completion. The result of the {@link Callable} is stored in the context under
     * {@link #CONTEXT_RESULT} and returned. If the task throws a {@link RuntimeException}, it's rethrown. If it throws
     * any other {@link Exception}, it's wrapped in a new {@link RuntimeException} and rethrown.
     *
     * @param callable The {@link Callable} task to be executed.
     * @param <T>      The type of the result returned by the callable.
     * @return The result of the callable task.
     * @throws RuntimeException If the callable throws any exception, it will be wrapped in a RuntimeException.
     */
    public <T> T safeCall(final Callable<T> callable) {
        if (startTime == 0) start();
        try {
            final T result = callable.call();
            ctx(CONTEXT_RESULT, result);
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

    /**
     * Executes the given {@link Callable} task within the `Meter`'s lifecycle control, wrapping any non-runtime
     * {@link Exception} into a specified {@link RuntimeException} subclass. The operation is automatically started
     * before execution and marked as {@code OK} upon successful completion. The result of the {@link Callable} is
     * stored in the context under {@link #CONTEXT_RESULT} and returned. If the task throws a {@link RuntimeException},
     * it's rethrown. If it throws any other {@link Exception}, it's wrapped into an instance of `exceptionClass` and
     * rethrown.
     *
     * @param exceptionClass The {@link Class} of the {@link RuntimeException} to wrap checked exceptions into. This
     *                       class must have a constructor that accepts a `String` message and a `Throwable` cause.
     * @param callable       The {@link Callable} task to be executed.
     * @param <E>            The type of the {@link RuntimeException} to wrap checked exceptions into.
     * @param <T>            The type of the result returned by the callable.
     * @return The result of the callable task.
     * @throws E                If the callable throws any non-runtime exception, it will be wrapped in an instance of
     *                          `exceptionClass`.
     * @throws RuntimeException If the callable throws a RuntimeException, or if `exceptionClass` cannot be
     *                          instantiated.
     */
    public <E extends RuntimeException, T> T safeCall(final Class<E> exceptionClass, final Callable<T> callable) {
        if (stopTime == 0) start(); // TODO: should be changed from stopTime == 0 to startTime == 0 for consistency?
        try {
            final T result = callable.call();
            ctx(CONTEXT_RESULT, result);
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

    /**
     * Converts a given {@link Exception} into a specified {@link RuntimeException} subclass. This method attempts to
     * create an instance of `exceptionClass` using a constructor that accepts a `String` message and a `Throwable`
     * cause. If such a constructor is not found or instantiation fails, a generic {@link RuntimeException} is
     * returned.
     *
     * @param exceptionClass The {@link Class} of the {@link RuntimeException} to create.
     * @param e              The {@link Exception} to be wrapped.
     * @param <T>            The type of the {@link RuntimeException} subclass.
     * @return An instance of `exceptionClass` wrapping `e`, or a generic {@link RuntimeException}.
     */
    private <T extends RuntimeException> RuntimeException convertException(final Class<T> exceptionClass, final Exception e) {
        final String message = "Failed: " + (description != null ? description : category);
        try {
            return exceptionClass.getConstructor(String.class, Throwable.class).newInstance(message, e);
        } catch (final NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException |
                       IllegalArgumentException | InvocationTargetException ignored) {
            messageLogger.error(Markers.INCONSISTENT_EXCEPTION, ERROR_MSG_METER_CANNOT_CREATE_EXCEPTION, exceptionClass, e);
        }
        return new RuntimeException(e);
    }
}
