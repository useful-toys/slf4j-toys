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
package org.usefultoys.slf4j.meter;

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
import org.slf4j.Logger;
import org.usefultoys.slf4j.LoggerFactory;
import org.usefultoys.slf4j.Session;

/**
 *
 * @author Daniel Felix Ferber
 */
public class Meter extends MeterData implements Closeable {

    private static final long serialVersionUID = 1L;
    private static final String ERROR_MSG_METER_CANNOT_CREATE_EXCEPTION = "Meter cannot create exception of type {}.";
    private static final String ERROR_MSG_METER_ALREADY_STARTED = "Meter already started. id={}";
    private static final String ERROR_MSG_METER_ALREADY_STOPPED = "Meter already stopped. id={}";
    private static final String ERROR_MSG_METER_STOPPED_BUT_NOT_STARTED = "Meter stopped but not started. id={}";
    private static final String ERROR_MSG_METER_STATED_AND_NEVER_STOPPED = "Meter started and never stopped. id={}";
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

    /**
     * Logger that reports events from this Meter.
     */
    private transient final Logger logger;
    private static final String NULL_VALUE = "<null>";

    /**
     * How many times each event has been executed.
     */
    private static final ConcurrentMap<String, AtomicLong> eventCounterByName = new ConcurrentHashMap<String, AtomicLong>();
    private transient long lastProgressTime = 0;
    private transient long lastProgressIteration = 0;
    private static long meterProgressPeriodNanoseconds = Session.readMeterProgressPeriodProperty() * 1000 * 1000;

    /**
     * Most recent meter from this thread.
     */
    private static final ThreadLocal<WeakReference<Meter>> localThreadInstance = new ThreadLocal<WeakReference<Meter>>();
    private WeakReference<Meter> previousInstance;

    /**
     * Creates a new meter.
     *
     * @param logger
     */
    public Meter(final Logger logger) {
        super();
        this.sessionUuid = Session.uuid;
        this.logger = logger;
        this.eventCategory = logger.getName();
        this.eventName = null;
        eventCounterByName.putIfAbsent(this.eventCategory, new AtomicLong(0));
        this.eventPosition = eventCounterByName.get(this.eventCategory).incrementAndGet();
        this.createTime = System.nanoTime();
    }

    public Meter(final Logger logger, final String eventName) {
        super();
        this.sessionUuid = Session.uuid;
        this.logger = logger;
        this.eventCategory = logger.getName();
        final String index = this.eventCategory + "/" + eventName;
        eventCounterByName.putIfAbsent(index, new AtomicLong(0));
        this.eventPosition = eventCounterByName.get(index).incrementAndGet();
        this.eventName = eventName;
        this.createTime = System.nanoTime();
    }

    public static Meter getCurrentInstance() {
        final WeakReference<Meter> ref = localThreadInstance.get();
        final Meter current = ref == null ? null : ref.get();
        if (current == null) {
            return new Meter(LoggerFactory.getLogger("???"));
        }
        return current;
    }

    /**
     * Logger that receives messages from this meter.
     *
     * @return
     */
    public Logger getLogger() {
        return logger;
    }

    // ========================================================================
    /**
     * Creates a new mwter whose name is under the hierarchy of this meter.
     * Useful if a large task may be subdivided into smaller task and reported
     * individually. The new meter uses the name of this meter, appended my its
     * name, similar as logger do.
     *
     * @param name
     * @return
     */
    public Meter sub(final String name) {
        if (name == null) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            logger.error(Slf4JMarkers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "sub(name)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
        }
        final Meter m = new Meter(logger, eventName == null ? name : eventName + '/' + name);
        if (this.context != null) {
            m.context = new HashMap<String, String>(this.context);
        }
        return m;
    }

    // ========================================================================
    /**
     * Configures the meter with a human readable message that explains the task
     * purpose.
     *
     * @param message fixed message
     * @return reference to the meter itself.
     */
    public Meter m(final String message) {
        if (message == null) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            logger.error(Slf4JMarkers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "m(message)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
        }
        this.description = message;
        return this;
    }

    /**
     * Configures the meter with a human readable message that explains the task
     * purpose.
     *
     * @param format message format ({@link String#format(java.lang.String, java.lang.Object...)
     * })
     * @param args message arguments
     * @return reference to the meter itself.
     */
    public Meter m(final String format, final Object... args) {
        if (format == null) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            logger.error(Slf4JMarkers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "m(message, args...)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
            this.description = null;
            return this;
        }
        try {
            this.description = String.format(format, args);
        } catch (final IllegalFormatException e) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            logger.error(Slf4JMarkers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "m(format, args...)", ERROR_MSG_ILLEGAL_STRING_FORMAT, getFullID(), new IllegalMeterUsage(2, e));
        }
        return this;
    }

    /**
     * Configures the meter with an threshold for reasonable, typical execution
     * time for the task represented by the meter.
     *
     * @param timeLimitMilliseconds time threshold
     * @return reference to the meter itself.
     */
    public Meter limitMilliseconds(final long timeLimitMilliseconds) {
        if (timeLimitMilliseconds <= 0) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            logger.error(Slf4JMarkers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "limitMilliseconds(timeLimitMilliseconds)", ERROR_MSG_NON_POSITIVE_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
            return this;
        }
        this.timeLimitNanoseconds = timeLimitMilliseconds * 1000 * 1000;
        return this;
    }

    /**
     * Configures the meter as representing a task made up of iterations or
     * steps. Such meters are allows to call {@link #progress()
     * } an arbitrarily number of times between {@link #start() } and {@link #ok() }/{@link #fail(java.lang.Throwable)
     * } method calls.
     *
     * @param expectedIterations Number of expected iterations or steps that
     * make up the task
     * @return reference to the meter itself.
     */
    public Meter iterations(final long expectedIterations) {
        if (expectedIterations <= 0) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            logger.error(Slf4JMarkers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "iterations(expectedIterations)", ERROR_MSG_NON_POSITIVE_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
            return this;
        }
        this.expectedIterations = expectedIterations;
        return this;
    }

    // ========================================================================
    /**
     * Adds an entry to the context map. The entry has no value and is
     * interpreted as a marker.
     *
     * @param name key of the entry to add.
     * @return reference to the meter itself.
     */
    public Meter ctx(final String name) {
        if (name == null) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            logger.error(Slf4JMarkers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "ctx(name)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
            return this;
        }
        if (context == null) {
            this.context = new LinkedHashMap<String, String>();
        }
        context.put(name, null);
        return this;
    }

    /**
     * Adds an entry to the context map if conditions is true. The entry has no
     * value and is interpreted as a marker.
     *
     * @param condition the condition
     * @param trueName key of the entry to add if conditions is true
     * @return reference to the meter itself.
     */
    public Meter ctx(final boolean condition, final String trueName) {
        if (!condition) {
            return this;
        }
        if (trueName == null) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            logger.error(Slf4JMarkers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "ctx(condition,name)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
            return this;
        }
        if (context == null) {
            this.context = new LinkedHashMap<String, String>();
        }
        context.put(trueName, null);
        return this;
    }

    /**
     * Adds an entry to the context map if conditions is true or false. The
     * entry has no value and is interpreted as a marker.
     *
     * @param condition the condition
     * @param trueName key of the entry to add if conditions is true
     * @param falseName key of the entry to add if conditions is true
     * @return reference to the meter itself.
     */
    public Meter ctx(final boolean condition, final String trueName, final String falseName) {
        if (condition) {
            if (trueName == null) {
                /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
                logger.error(Slf4JMarkers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "ctx(condition,name,name)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
                return this;
            }
            if (context == null) {
                this.context = new LinkedHashMap<String, String>();
            }
            context.put(trueName, null);
        } else {
            if (falseName == null) {
                /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
                logger.error(Slf4JMarkers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "ctx(condition,name,name)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
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
     * @param name key of the entry to add.
     * @param value value of the entry to add.
     * @return reference to the meter itself.
     */
    public Meter ctx(final String name, final int value) {
        if (name == null) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            logger.error(Slf4JMarkers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "ctx(name, value)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
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
     * @param name key of the entry to add.
     * @param value value of the entry to add.
     * @return reference to the meter itself.
     */
    public Meter ctx(final String name, final long value) {
        if (name == null) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            logger.error(Slf4JMarkers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "ctx(name, value)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
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
     * @param name key of the entry to add.
     * @param value value of the entry to add.
     * @return reference to the meter itself.
     */
    public Meter ctx(final String name, final boolean value) {
        if (name == null) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            logger.error(Slf4JMarkers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "ctx(name, value)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
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
     * @param name key of the entry to add.
     * @param value value of the entry to add.
     * @return reference to the meter itself.
     */
    public Meter ctx(final String name, final float value) {
        if (name == null) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            logger.error(Slf4JMarkers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "ctx(name, value)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
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
     * @param name key of the entry to add.
     * @param value value of the entry to add.
     * @return reference to the meter itself.
     */
    public Meter ctx(final String name, final double value) {
        if (name == null) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            logger.error(Slf4JMarkers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "ctx(name, value)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
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
     * @param name key of the entry to add.
     * @param value value of the entry to add.
     * @return reference to the meter itself.
     */
    public Meter ctx(final String name, final Integer value) {
        if (name == null) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            logger.error(Slf4JMarkers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "ctx(name, value)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
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
     * @param name key of the entry to add.
     * @param value value of the entry to add.
     * @return reference to the meter itself.
     */
    public Meter ctx(final String name, final Long value) {
        if (name == null) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            logger.error(Slf4JMarkers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "ctx(name, value)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
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
     * @param name key of the entry to add.
     * @param value value of the entry to add.
     * @return reference to the meter itself.
     */
    public Meter ctx(final String name, final Boolean value) {
        if (name == null) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            logger.error(Slf4JMarkers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "ctx(name, value)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
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
     * @param name key of the entry to add.
     * @param value value of the entry to add.
     * @return reference to the meter itself.
     */
    public Meter ctx(final String name, final Float value) {
        if (name == null) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            logger.error(Slf4JMarkers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "ctx(name, value)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
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
     * @param name key of the entry to add.
     * @param value value of the entry to add.
     * @return reference to the meter itself.
     */
    public Meter ctx(final String name, final Double value) {
        if (name == null) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            logger.error(Slf4JMarkers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "ctx(name, value)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
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
     * @param name key of the entry to add.
     * @param object object which string representation is used for the value of
     * the entry to add
     * @return reference to the meter itself.
     */
    public Meter ctx(final String name, final Object object) {
        if (name == null) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            logger.error(Slf4JMarkers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "ctx(name, value)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
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
     * @param name key of the entry to add.
     * @param value value of the entry to add.
     * @return reference to the meter itself.
     */
    public Meter ctx(final String name, final String value) {
        if (name == null) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            logger.error(Slf4JMarkers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "ctx(name, value)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
            return this;
        }
        if (context == null) {
            this.context = new LinkedHashMap<String, String>();
        }
        context.put(name, value);
        return this;
    }

    /**
     * Adds an entry to the context map. The entry value is made up of a
     * formatted message with arguments.
     *
     * @param name key of the entry to add.
     * @param format message format ({@link String#format(java.lang.String, java.lang.Object...)
     * })
     * @param args message arguments
     * @return reference to the meter itself.
     */
    public Meter ctx(final String name, final String format, final Object... args) {
        if (name == null || format == null) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            logger.error(Slf4JMarkers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "ctx(name, format, args...)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
            return this;
        }
        if (context == null) {
            this.context = new LinkedHashMap<String, String>();
        }
        try {
            ctx(name, String.format(format, args));
        } catch (final IllegalFormatException e) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            logger.error(Slf4JMarkers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "ctx(name, format, args...)", ERROR_MSG_ILLEGAL_STRING_FORMAT, getFullID(), new IllegalMeterUsage(2, e));
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
            logger.error(Slf4JMarkers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "unctx(name)", ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
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
     * Notifies the meter in order to claim immediate execution start of the
     * task represented by the meter. Sends a message to logger using debug
     * level. Sends a message with system status and partial context to log
     * using trace level.
     *
     * @return reference to the meter itself.
     */
    public Meter start() {
        try {
            if (startTime != 0) {
                /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
                logger.error(Slf4JMarkers.INCONSISTENT_START, ERROR_MSG_METER_ALREADY_STARTED, getFullID(), new IllegalMeterUsage(2)
                );
            } else {
                previousInstance = localThreadInstance.get();
                localThreadInstance.set(new WeakReference<Meter>(this));
            }

//            final Thread currentThread = Thread.currentThread();
//            this.threadStartId = currentThread.getId();
//            this.threadStartName = currentThread.getName();
            this.lastProgressTime = this.startTime = System.nanoTime();

            if (logger.isDebugEnabled()) {
                collectRuntimeStatus();
                collectPlatformStatus();
                logger.debug(Slf4JMarkers.MSG_START, readableString(new StringBuilder()).toString());
                if (logger.isTraceEnabled()) {
                    logger.trace(Slf4JMarkers.DATA_START, write(new StringBuilder(), 'M').toString());
                }
                if (context != null) {
                    context.clear();
                }
            }

        } catch (final Exception t) {
            /* Prevents bugs from disrupting the application. Logs message with exception to provide stacktrace to bug. */
            logger.error(Slf4JMarkers.BUG, ERROR_MSG_METHOD_THREW_EXCEPTION, "start", getFullID(), t);
        }
        return this;
    }

    // ========================================================================
    /**
     * Notifies the meter that one more iteration or step completed that make up
     * the task successfully.
     *
     * @return reference to the meter itself.
     */
    public Meter inc() {
        if (startTime == 0) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            logger.error(Slf4JMarkers.INCONSISTENT_INCREMENT, ERROR_MSG_METER_INCREMENTED_BUT_NOT_STARTED, getFullID(), new IllegalMeterUsage(4));
        }
        this.iteration++;
        return this;
    }

    /**
     * Notifies the meter that more of iterations or steps that make up the task
     * completed successfully.
     *
     * @param increment the number of iterations or steps
     * @return reference to the meter itself.
     */
    public Meter incBy(final long increment) {
        if (startTime == 0) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            logger.error(Slf4JMarkers.INCONSISTENT_INCREMENT, ERROR_MSG_METER_INCREMENTED_BUT_NOT_STARTED, getFullID(), new IllegalMeterUsage(4));
        }
        if (increment <= 0) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            logger.error(Slf4JMarkers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "incBy(increment)", ERROR_MSG_NON_POSITIVE_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
            return this;
        }
        this.iteration += increment;
        return this;
    }

    /**
     * Notifies the meter that a number of iterations or steps that make up the
     * task already completed successfully.
     *
     * @param currentIteration the number of iterations or steps
     * @return reference to the meter itself.
     */
    public Meter incTo(final long currentIteration) {
        if (startTime == 0) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            logger.error(Slf4JMarkers.INCONSISTENT_INCREMENT, ERROR_MSG_METER_INCREMENTED_BUT_NOT_STARTED, getFullID(), new IllegalMeterUsage(4));
        }
        if (currentIteration <= 0) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            logger.error(Slf4JMarkers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "incTo(currentIteration)", ERROR_MSG_NON_POSITIVE_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
            return this;
        }
        if (currentIteration <= this.iteration) {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            logger.error(Slf4JMarkers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "incTo(currentIteration)", ERROR_MSG_NON_FORWARD_ITERATION, getFullID(), new IllegalMeterUsage(2));
        }
        this.iteration = currentIteration;
        return this;
    }

    /**
     * Allow informing about successful completion of iterations or steps making
     * up the task represented by the meter. Only applicable for meters that
     * called {@link #iterations(long i)} before calling {@link #start() }.
     * Sends a message to logger using info level, only periodically and if
     * progress was observed, to minimize performance degradation.
     *
     * @return reference to the meter itself.
     */
    public Meter progress() {
        try {
            if (startTime == 0) {
                /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
                logger.error(Slf4JMarkers.INCONSISTENT_PROGRESS, ERROR_MSG_METER_PROGRESS_BUT_NOT_STARTED, getFullID(), new IllegalMeterUsage(2));
            }

            long now;
            if (iteration > lastProgressIteration && ((now = System.nanoTime()) - lastProgressTime) > meterProgressPeriodNanoseconds) {
                lastProgressIteration = iteration;
                lastProgressTime = now;

                if (logger.isInfoEnabled()) {
                    collectRuntimeStatus();
                    collectPlatformStatus();
                    logger.info(Slf4JMarkers.MSG_PROGRESS, readableString(new StringBuilder()).toString());
                    if (logger.isTraceEnabled()) {
                        if (startTime != 0 && timeLimitNanoseconds != 0 && (now - startTime) > timeLimitNanoseconds) {
                            logger.trace(Slf4JMarkers.DATA_SLOW_PROGRESS, write(new StringBuilder(), 'M').toString());
                        } else if (logger.isTraceEnabled()) {
                            logger.trace(Slf4JMarkers.DATA_PROGRESS, write(new StringBuilder(), 'M').toString());
                        }
                    }
                    if (context != null) {
                        context.clear();
                    }
                }
            }
        } catch (final Exception t) {
            /* Prevents bugs from disrupting the application. Logs message with exception to provide stacktrace to bug. */
            logger.error(Slf4JMarkers.BUG, ERROR_MSG_METHOD_THREW_EXCEPTION, "progress", getFullID(), t);
        }
        return this;
    }

    // ========================================================================
    public Meter flow(Object flow) {
        if (flow instanceof String) {
            this.pathId = (String) flow;
        } else if (flow instanceof Enum) {
            this.pathId = ((Enum<?>) flow).name();
        } else if (flow instanceof Throwable) {
            this.pathId = flow.getClass().getSimpleName();
        } else if (flow != null) {
            this.pathId = flow.toString();
        } else {
            /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
            logger.error(Slf4JMarkers.ILLEGAL, ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(4));
        }
        return this;
    }

    /**
     * Confirms the meter in order to claim successful completion of the task
     * represented by the meter. Sends a message to logger using info level. If
     * a time limit was given and execution exceeded this limit, sends a message
     * using warn level instead. Sends a message with system status and partial
     * context to log using trace level.
     *
     * @return reference to the meter itself.
     */
    public Meter ok() {
        try {
            final long newStopTime = System.nanoTime();

            /* Sanity check. Logs message and exception with stacktrace forged to the inconsistent caller method. */
            if (stopTime != 0) {
                logger.error(Slf4JMarkers.INCONSISTENT_OK, ERROR_MSG_METER_ALREADY_STOPPED, getFullID(), new IllegalMeterUsage(2));
            } else if (checkCurrentInstance()) {
                logger.error(Slf4JMarkers.INCONSISTENT_OK, ERROR_MSG_METER_OUT_OF_ORDER, getFullID(), new IllegalMeterUsage(2));
            } else if (startTime == 0) {
                logger.error(Slf4JMarkers.INCONSISTENT_OK, ERROR_MSG_METER_STOPPED_BUT_NOT_STARTED, getFullID(), new IllegalMeterUsage(2));
            }

            stopTime = newStopTime;
            failClass = null;
            failMessage = null;
            rejectId = null;
            localThreadInstance.set(previousInstance);

//            final Thread currentThread = Thread.currentThread();
//            this.threadStopId = currentThread.getId();
//            this.threadStopName = currentThread.getName();
            if (logger.isWarnEnabled()) {
                collectRuntimeStatus();
                collectPlatformStatus();

                final boolean warnSlowness = startTime != 0 && timeLimitNanoseconds != 0 && stopTime - startTime > timeLimitNanoseconds;
                if (warnSlowness) {
                    logger.warn(Slf4JMarkers.MSG_SLOW_OK, readableString(new StringBuilder()).toString());
                } else if (logger.isInfoEnabled()) {
                    logger.info(Slf4JMarkers.MSG_OK, readableString(new StringBuilder()).toString());
                }
                if (logger.isTraceEnabled()) {
                    if (warnSlowness) {
                        logger.trace(Slf4JMarkers.DATA_SLOW_OK, write(new StringBuilder(), 'M').toString());
                    } else {
                        logger.trace(Slf4JMarkers.DATA_OK, write(new StringBuilder(), 'M').toString());
                    }
                }
                if (context != null) {
                    context.clear();
                }
            }
        } catch (final Exception t) {
            /* Prevents bugs from disrupting the application. Logs message with exception to provide stacktrace to bug. */
            logger.error(Slf4JMarkers.BUG, ERROR_MSG_METHOD_THREW_EXCEPTION, "ok", getFullID(), t);
        }
        return this;
    }

    public boolean checkCurrentInstance() {
        final WeakReference<Meter> ref = localThreadInstance.get();
        return ref == null || ref.get() != this;
    }

    /**
     * Confirms the meter in order to claim successful completion of the task
     * represented by the meter. Sends a message to logger using info level. If
     * a time limit was given and execution exceeded this limit, sends a message
     * using warn level instead. Sends a message with system status and partial
     * context to log using trace level.
     *
     * @param flow A token, enum or exception that describes the successful
     * pathId.
     * @return reference to the meter itself.
     */
    public Meter ok(Object flow) {
        try {
            final long newStopTime = System.nanoTime();

            /* Sanity check. Logs message and exception with stacktrace forged to the inconsistent caller method. */
            if (stopTime != 0) {
                logger.error(Slf4JMarkers.INCONSISTENT_OK, ERROR_MSG_METER_ALREADY_STOPPED, getFullID(), new IllegalMeterUsage(2));
            } else if (checkCurrentInstance()) {
                logger.error(Slf4JMarkers.INCONSISTENT_OK, ERROR_MSG_METER_OUT_OF_ORDER, getFullID(), new IllegalMeterUsage(2));
            } else if (startTime == 0) {
                logger.error(Slf4JMarkers.INCONSISTENT_OK, ERROR_MSG_METER_STOPPED_BUT_NOT_STARTED, getFullID(), new IllegalMeterUsage(2));
            }

            stopTime = newStopTime;
            failClass = null;
            failMessage = null;
            rejectId = null;
            localThreadInstance.set(previousInstance);
            if (flow instanceof String) {
                this.pathId = (String) flow;
            } else if (flow instanceof Enum) {
                this.pathId = ((Enum<?>) flow).name();
            } else if (flow instanceof Throwable) {
                this.pathId = flow.getClass().getSimpleName();
            } else if (flow != null) {
                this.pathId = flow.toString();
            } else {
                /* Logs message and exception with stacktrace forged to the inconsistent caller method. */
                logger.error(Slf4JMarkers.INCONSISTENT_OK, ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(4));
            }

//            final Thread currentThread = Thread.currentThread();
//            this.threadStopId = currentThread.getId();
//            this.threadStopName = currentThread.getName();
            if (logger.isWarnEnabled()) {
                collectRuntimeStatus();
                collectPlatformStatus();

                final boolean warnSlowness = startTime != 0 && timeLimitNanoseconds != 0 && stopTime - startTime > timeLimitNanoseconds;
                if (warnSlowness) {
                    logger.warn(Slf4JMarkers.MSG_SLOW_OK, readableString(new StringBuilder()).toString());
                } else if (logger.isInfoEnabled()) {
                    logger.info(Slf4JMarkers.MSG_OK, readableString(new StringBuilder()).toString());
                }
                if (logger.isTraceEnabled()) {
                    if (warnSlowness) {
                        logger.trace(Slf4JMarkers.DATA_SLOW_OK, write(new StringBuilder(), 'M').toString());
                    } else {
                        logger.trace(Slf4JMarkers.DATA_OK, write(new StringBuilder(), 'M').toString());
                    }
                }
                if (context != null) {
                    context.clear();
                }
            }
        } catch (final Exception t) {
            /* Prevents bugs from disrupting the application. Logs message with exception to provide stacktrace to bug. */
            logger.error(Slf4JMarkers.BUG, ERROR_MSG_METHOD_THREW_EXCEPTION, "ok", getFullID(), t);
        }
        return this;
    }

    /**
     * Confirms the meter in order to claim unsuccessful completion of the task
     * represented by the meter. Sends a message to logger using info level. If
     * a time limit was given and execution exceeded this limit, sends a message
     * using warn level instead. Sends a message with system status and partial
     * context to log using trace level.
     *
     * @param cause A token, enum or exception that describes the cause of
     * rejection.
     * @return reference to the meter itself.
     */
    public Meter reject(Object cause) {
        try {
            final long newStopTime = System.nanoTime();

            /* Sanity check. Logs message and exception with stacktrace forged to the inconsistent caller method. */
            if (stopTime != 0) {
                logger.error(Slf4JMarkers.INCONSISTENT_REJECT, ERROR_MSG_METER_ALREADY_STOPPED, getFullID(), new IllegalMeterUsage(2));
            } else if (checkCurrentInstance()) {
                logger.error(Slf4JMarkers.INCONSISTENT_REJECT, ERROR_MSG_METER_OUT_OF_ORDER, getFullID(), new IllegalMeterUsage(2));
            } else if (startTime == 0) {
                logger.error(Slf4JMarkers.INCONSISTENT_REJECT, ERROR_MSG_METER_STOPPED_BUT_NOT_STARTED, getFullID(), new IllegalMeterUsage(2));
            }

            stopTime = newStopTime;
            failClass = null;
            failMessage = null;
            pathId = null;
            localThreadInstance.set(previousInstance);
            if (cause instanceof String) {
                this.rejectId = (String) cause;
            } else if (cause instanceof Enum) {
                this.rejectId = ((Enum<?>) cause).name();
            } else if (cause instanceof Throwable) {
                this.rejectId = cause.getClass().getSimpleName();
            } else if (cause != null) {
                this.rejectId = cause.toString();
            } else {
                logger.error(Slf4JMarkers.INCONSISTENT_REJECT, ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
            }

//            final Thread currentThread = Thread.currentThread();
//            this.threadStopId = currentThread.getId();
//            this.threadStopName = currentThread.getName();
            if (logger.isInfoEnabled()) {
                collectRuntimeStatus();
                collectPlatformStatus();
                if (logger.isInfoEnabled()) {
                    logger.info(Slf4JMarkers.MSG_REJECT, readableString(new StringBuilder()).toString());
                }
                if (logger.isTraceEnabled()) {
                    logger.trace(Slf4JMarkers.DATA_REJECT, write(new StringBuilder(), 'M').toString());
                }
                if (context != null) {
                    context.clear();
                }
            }
        } catch (final Exception t) {
            /* Prevents bugs from disrupting the application. Logs message with exception to provide stacktrace to bug. */
            logger.error(Slf4JMarkers.BUG, ERROR_MSG_METHOD_THREW_EXCEPTION, "reject", getFullID(), t);
        }
        return this;
    }

    // ========================================================================
    /**
     * Refuses the meter in order to claim incomplete or inconsistent execution
     * of the task represented by the meter. Sends a message with the the
     * exception to logger using warn level. Sends a message with system status,
     * statistics and complete context to log using trace level.
     *
     * @param cause Exception that represents the failure. May be null if no
     * exception applies.
     * @return reference to the meter itself.
     */
    public Meter fail(final Throwable cause) {
        try {
            final long newStopTime = System.nanoTime();

            /* Sanity check. Logs message and exception with stacktrace forged to the inconsistent caller method. */
            if (stopTime != 0) {
                logger.error(Slf4JMarkers.INCONSISTENT_FAIL, ERROR_MSG_METER_ALREADY_STOPPED, getFullID(), new IllegalMeterUsage(2));
            } else if (checkCurrentInstance()) {
                logger.error(Slf4JMarkers.INCONSISTENT_FAIL, ERROR_MSG_METER_OUT_OF_ORDER, getFullID(), new IllegalMeterUsage(2));
            } else if (startTime == 0) {
                logger.error(Slf4JMarkers.INCONSISTENT_FAIL, ERROR_MSG_METER_STOPPED_BUT_NOT_STARTED, getFullID(), new IllegalMeterUsage(2));
            }

            stopTime = newStopTime;
            rejectId = null;
            pathId = null;
            localThreadInstance.set(previousInstance);
            if (cause instanceof TryWithResourcesFailed) {
                failClass = null;
                failMessage = "try-with-resources";
            } else if (cause != null) {
                failClass = cause.getClass().getName();
                failMessage = cause.getLocalizedMessage();
            } else {
                logger.error(Slf4JMarkers.INCONSISTENT_FAIL, ERROR_MSG_NULL_ARGUMENT, getFullID(), new IllegalMeterUsage(2));
            }

//            final Thread currentThread = Thread.currentThread();
//            this.threadStopId = currentThread.getId();
//            this.threadStopName = currentThread.getName();
            if (logger.isErrorEnabled()) {
                collectRuntimeStatus();
                collectPlatformStatus();
                logger.error(Slf4JMarkers.MSG_FAIL, readableString(new StringBuilder()).toString(), cause);
                if (logger.isTraceEnabled()) {
                    logger.trace(Slf4JMarkers.DATA_FAIL, write(new StringBuilder(), 'M').toString());
                }
                if (context != null) {
                    context.clear();
                }
            }
        } catch (final Exception t) {
            /* Prevents bugs from disrupting the application. Logs message with exception to provide stacktrace to bug. */
            logger.error(Slf4JMarkers.BUG, ERROR_MSG_METHOD_THREW_EXCEPTION, "fail", getFullID(), t);
        }
        return this;
    }

    // ========================================================================
    /**
     * Checks if meters the meter has been forgotten to be confirmed or refused.
     * Useful to track those meters that do not follow the start(), ok()/fail()
     * idiom for all execution flows
     */
    @Override
    protected void finalize() throws Throwable {
        if (stopTime == 0) {
            /* Logs only message. Stacktrace will not contain useful hints. Exception is logged only for visibility of inconsistent meter usage. */
            logger.error(Slf4JMarkers.INCONSISTENT_FINALIZED, ERROR_MSG_METER_STATED_AND_NEVER_STOPPED, getFullID(), new IllegalMeterUsage(1));
        }
        super.finalize();
    }

    @SuppressWarnings("ExtendsThrowable")
    public static class MeterThrowable extends Throwable {

        private static final long serialVersionUID = 1L;

        MeterThrowable(final int framesToDiscard) {
            this(framesToDiscard + 1, null);
        }

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

        IllegalMeterUsage(int framesToDiscard) {
            super(framesToDiscard);
        }

        IllegalMeterUsage(int framesToDiscard, Throwable e) {
            super(framesToDiscard, e);
        }
    }

    public static class TryWithResourcesFailed extends MeterThrowable {

        private static final long serialVersionUID = 1L;

        TryWithResourcesFailed(int framesToDiscard) {
            super(framesToDiscard);
        }

    }

    // ========================================================================
    /**
     * Compliance with {@link Closeable}. Assumes failure and refuses the meter
     * if the meter has not yet been marked as confirmed.
     */
    @Override
    public void close() {
        if (stopTime == 0) {
            fail(new TryWithResourcesFailed((2)));
        }
    }

    // ========================================================================
    public void run(final Runnable runnable) {
        this.start();
        try {
            runnable.run();
            this.ok();
        } catch (final RuntimeException e) {
            this.fail(e);
            throw e;
        }
    }

    public <T> T call(final Callable<T> callable) throws Exception {
        this.start();
        try {
            final T result = callable.call();
            this.ctx("result", result).ok();
            return result;
        } catch (final Exception e) {
            this.fail(e);
            throw e;
        }
    }

    public <T> T safeCall(final Callable<T> callable) {
        this.start();
        try {
            final T result = callable.call();
            this.ctx("result", result).ok();
            return result;
        } catch (final RuntimeException e) {
            this.fail(e);
            throw e;
        } catch (final Exception e) {
            this.fail(e);
            throw new RuntimeException(e);
        }
    }

    public <E extends RuntimeException, T> T safeCall(final Class<E> exceptionClass, final Callable<T> callable) {
        this.start();
        try {
            final T result = callable.call();
            this.ok();
            return result;
        } catch (final RuntimeException e) {
            this.fail(e);
            throw e;
        } catch (final Exception e) {
            this.fail(e);
            throw convertException(exceptionClass, e);
        }
    }

    private <T extends RuntimeException> RuntimeException convertException(final Class<T> exceptionClass, final Exception e) {
        final String message = "Failed: " + (this.description != null ? this.description : this.eventCategory);
        try {
            return exceptionClass.getConstructor(String.class, Throwable.class).newInstance(message, e);
        } catch (final NoSuchMethodException ignored) {
            logger.error(Slf4JMarkers.INCONSISTENT_EXCEPTION, ERROR_MSG_METER_CANNOT_CREATE_EXCEPTION, exceptionClass, e);
        } catch (final SecurityException ignored) {
            logger.error(Slf4JMarkers.INCONSISTENT_EXCEPTION, ERROR_MSG_METER_CANNOT_CREATE_EXCEPTION, exceptionClass, e);
        } catch (final InstantiationException ignored) {
            logger.error(Slf4JMarkers.INCONSISTENT_EXCEPTION, ERROR_MSG_METER_CANNOT_CREATE_EXCEPTION, exceptionClass, e);
        } catch (final IllegalAccessException ignored) {
            logger.error(Slf4JMarkers.INCONSISTENT_EXCEPTION, ERROR_MSG_METER_CANNOT_CREATE_EXCEPTION, exceptionClass, e);
        } catch (final IllegalArgumentException ignored) {
            logger.error(Slf4JMarkers.INCONSISTENT_EXCEPTION, ERROR_MSG_METER_CANNOT_CREATE_EXCEPTION, exceptionClass, e);
        } catch (final InvocationTargetException ignored) {
            logger.error(Slf4JMarkers.INCONSISTENT_EXCEPTION, ERROR_MSG_METER_CANNOT_CREATE_EXCEPTION, exceptionClass, e);
        }
        return new RuntimeException(e);
    }
}
