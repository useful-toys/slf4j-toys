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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import org.slf4j.Logger;
import org.usefultoys.slf4j.LoggerFactory;
import org.usefultoys.slf4j.Session;
import org.usefultoys.slf4j.internal.SystemMetrics;
import org.usefultoys.slf4j.internal.TimeSource;

import java.io.Closeable;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The `Meter` is a core component of `slf4j-toys` designed to track the **lifecycle** of application operations.
 * It collects system status and reports it to the logger at key points: operation start, progress, and termination
 * (success, rejection, or failure).
 * <p>
 * Each `Meter` instance allows you to:
 * <ul>
 *     <li>Call {@link #start()} to log the operation's beginning and current system status (DEBUG level).</li>
 *     <li>Call {@link #ok()} to log successful completion (INFO level, or WARN level if slow).</li>
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
public class Meter extends MeterData implements MeterContext<Meter>, MeterExecutor<Meter>, Closeable {

    /**
     * The serial version UID for serialization.
     */
    private static final long serialVersionUID = 1L;

    /** Placeholder string for unknown logger names. */
    public static final String UNKNOWN_LOGGER_NAME = "???";
    /** Default failure path for operations terminated by `try-with-resources`. */
    private static final String FAIL_PATH_TRY_WITH_RESOURCES = "try-with-resources";

    /** Logger for human-readable messages. */
    @Getter
    private final transient Logger messageLogger;
    /** Logger for machine-parsable data. */
    @Getter
    private final transient Logger dataLogger;

    /**
     * Tracks how many times each unique operation (category/operation name pair) has been executed.
     */
    static final ConcurrentMap<String, AtomicLong> EVENT_COUNTER = new ConcurrentHashMap<>();
    /**
     * Timestamp (in nanoseconds) when progress was last reported. Zero if progress has not been reported yet. Used to
     * control the frequency of progress messages and avoid flooding the log.
     */
    @Getter(AccessLevel.PACKAGE) // for tests
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
        messageLogger = org.slf4j.LoggerFactory.getLogger(MeterConfig.messagePrefix + logger.getName() + MeterConfig.messageSuffix);
        dataLogger = org.slf4j.LoggerFactory.getLogger(MeterConfig.dataPrefix + logger.getName() + MeterConfig.dataSuffix);
    }

    /**
     * Sets a custom time source for this meter instance.
     * <p>
     * This method is intended primarily for testing purposes to enable deterministic time-based testing.
     * By replacing the default {@link org.usefultoys.slf4j.internal.SystemTimeSource} with a controllable implementation,
     * tests can verify time-dependent behavior (such as slowness detection, progress throttling,
     * and duration calculations) without depending on actual system time or thread delays.
     * <p>
     * This method should be called immediately after constructing the meter and before calling
     * any lifecycle methods ({@link #start()}, {@link #progress()}, {@link #ok()}, etc.).
     * <p>
     * <b>Thread Safety:</b> This method should be called before the meter is used in concurrent
     * scenarios, as the time source field is not volatile.
     * <p>
     * For more details on the design rationale, see TDR-0032: Clock Abstraction Pattern
     * for Deterministic Time-Based Testing.
     *
     * @param timeSource The time source to use for collecting timestamps.
     * @return This Meter instance for method chaining.
     * @see TimeSource
     * @see org.usefultoys.slf4j.internal.SystemTimeSource
     */
    final Meter withTimeSource(final TimeSource timeSource) {
        this.timeSource = timeSource;
        return this;
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
     * Converts an object into a string representation suitable for a path identifier.
     *
     * @param o The object to convert.
     * @param useSimpleClassNameForThrowable If true, uses {@link Class#getSimpleName()} for Throwables; otherwise, uses {@link Class#getName()}.
     * @return The string representation.
     */
    private static String toPath(final Object o, final boolean useSimpleClassNameForThrowable) {
        if (o == null) {
            return null;
        }
        if (o instanceof String) {
            return (String) o;
        }
        if (o instanceof Enum) {
            return ((Enum<?>) o).name();
        }
        if (o instanceof Throwable) {
            return useSimpleClassNameForThrowable ? o.getClass().getSimpleName() : o.getClass().getName();
        }
        return o.toString();
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
        MeterValidator.validateSubCallArguments(this, suboperationName);
        String subOperation = null;
        if (operation == null) {
            subOperation = suboperationName;
        } else if (suboperationName == null) {
            subOperation = operation;
        } else {
            subOperation = operation + "/" + suboperationName;
        }
        final Meter m = new Meter(messageLogger, subOperation, getFullID());
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
        if (!MeterValidator.validateMPrecondition(this) || !MeterValidator.validateMCallArguments(this, message)) {
            return this;
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
        if (!MeterValidator.validateMPrecondition(this)) {
            return this;
        }
        description = MeterValidator.validateAndFormatMCallArguments(this, format, args);
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
        if (!MeterValidator.validateLimitMillisecondsPrecondition(this) || !MeterValidator.validateLimitMillisecondsCallArguments(this, timeLimit)) {
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
        if (!MeterValidator.validateIterationsPrecondition(this) || !MeterValidator.validateIterationsCallArguments(this, expectedIterations)) {
            return this;
        }
        this.expectedIterations = expectedIterations;
        return this;
    }

    // ========================================================================
    // ctx and unctx methods are now provided by MeterContext interface
    // ========================================================================

    @Override
    public void putContext(final String name, final Object value) {
        if (!MeterValidator.validateContextPrecondition(this)) {
            return;
        }
        super.putContext(name, value);
    }

    @Override
    public void putContext(final String name) {
        if (!MeterValidator.validateContextPrecondition(this)) {
            return;
        }
        super.putContext(name);
    }

    // ========================================================================
    // ctx and unctx methods are now provided by MeterContext interface
    // ========================================================================

    /**
     * Notifies the `Meter` that the operation has started. This method logs a **human-readable summary** (DEBUG level)
     * and a **machine-parsable data message** (TRACE level) with the current system status.
     *
     * @return Reference to this `Meter` instance, for method chaining.
     */
    public Meter start() {
        try {
            if (MeterValidator.validateStartPrecondition(this)) {
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
            MeterValidator.logBug(this, "start()", t);
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
        if (!MeterValidator.validateIncPrecondition(this)) {
            return this;
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
        if (!MeterValidator.validateIncPrecondition(this) || !MeterValidator.validateIncByArguments(this, increment)) {
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
        if (!MeterValidator.validateIncPrecondition(this) || !MeterValidator.validateIncToArguments(this, currentIteration)) {
            return this;
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
            if (!MeterValidator.validateProgressPrecondition(this)) {
                return this;
            }

            final long now = collectCurrentTime();
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
                        if (isSlow()) {
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
            MeterValidator.logBug(this, "progress()", t);
        }
        return this;
    }

    /**
     * Sets the success path identifier for the operation. This is typically used with {@link #ok()} to distinguish
     * between different successful outcomes.
     * <p>
     * <b>Precondition:</b> This method must be called after {@link #start()} and before any termination method
     * ({@link #ok()}, {@link #reject(Object)}, {@link #fail(Object)}). Calling it before starting or after stopping
     * will log an error with {@link Markers#ILLEGAL} marker and have no effect on the Meter state.
     *
     * @param pathId An object (String, Enum, Throwable, or any Object with a meaningful `toString()`) that identifies
     *               the successful execution path.
     * @return Reference to this `Meter` instance, for method chaining.
     */
    public Meter path(final Object pathId) {
        if (!MeterValidator.validatePathArgument(this, "path(pathId)", pathId)) {
            return this;
        }
        if (!MeterValidator.validatePathPrecondition(this)) {
            return this;
        }
        okPath = toPath(pathId, true);
        return this;
    }

    /**
     * Notifies the `Meter` that the operation has completed successfully. This method logs a **human-readable summary**
     * (INFO level) and a **machine-parsable data message** (TRACE level) with the current system status. If a time
     * limit was set and exceeded, a WARN level message is logged instead, indicating a slow operation.
     *
     * @return Reference to this `Meter` instance, for method chaining.
     */
    Meter commonOk(final Object pathId) {
        try {
            MeterValidator.validateStopPrecondition(this, Markers.INCONSISTENT_OK);

            stopTime = collectCurrentTime();
            /* Auto-correct: if never started, use stopTime as startTime (Tier 3) */
            if (startTime == 0) {
                startTime = stopTime;
            }
            failPath = null;
            failMessage = null;
            rejectPath = null;
            if (pathId != null) {
                okPath = toPath(pathId, true);
            }
            localThreadInstance.set(previousInstance);

            if (messageLogger.isWarnEnabled()) { // Check warn enabled to cover info as well
                SystemMetrics.getInstance().collectRuntimeStatus(this);
                SystemMetrics.getInstance().collectPlatformStatus(this);

                final boolean warnSlowness = isSlow();
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
            MeterValidator.logBug(this, "ok(...)", t);
        }
        return this;
    }

    /**
     * Checks if this `Meter` instance is the current `Meter` associated with the current thread.
     *
     * @return {@code true} if this `Meter` is not the current instance, {@code false} otherwise.
     */
    boolean checkCurrentInstance() {
        final WeakReference<Meter> ref = localThreadInstance.get();
        return ref == null || ref.get() != this;
    }

    public Meter ok() {
        commonOk(null);
        return this;
    }

    /**
     * Notifies the `Meter` that the operation has completed successfully, specifying an execution path.
     * This method logs a **human-readable summary** (INFO level) and a **machine-parsable data message** (TRACE level)
     * with the current system status. If a time limit was set and exceeded, a WARN level message is logged instead, indicating a slow operation.
     * <p>
     * If {@code pathId} is {@code null}, the method completes successfully without modifying the current path.
     *
     * @param pathId An object (String, Enum, Throwable, or any Object with a meaningful `toString()`) that identifies
     *               the successful execution path. If {@code null}, the current path is preserved.
     * @return Reference to this `Meter` instance, for method chaining.
     */
    public Meter ok(final Object pathId) {
        MeterValidator.validatePathArgument(this, "ok(pathId)", pathId);
        return commonOk(pathId);
    }

    /**
     * Notifies the `Meter` that the operation has completed successfully. This is an alias for {@link #ok()}.
     * This method logs a **human-readable summary**
     * (INFO level) and a **machine-parsable data message** (TRACE level) with the current system status. If a time
     * limit was set and exceeded, a WARN level message is logged instead, indicating a slow operation.
     *
     * @return Reference to this `Meter` instance, for method chaining.
     */
    public Meter success() {
        return commonOk(null);
    }

    /**
     * Notifies the `Meter` that the operation has completed successfully, specifying an execution path. This is an
     * alias for {@link #ok(Object)}.
     * This method logs a **human-readable summary** (INFO level) and a **machine-parsable data message** (TRACE level)
     * with the current system status. If a time limit was set and exceeded, a WARN level message is logged instead, indicating a slow operation.
     * <p>
     * If {@code pathId} is {@code null}, the method completes successfully without modifying the current path.
     *
     * @param pathId An object (String, Enum, Throwable, or any Object with a meaningful `toString()`) that identifies
     *               the successful execution path. If {@code null}, the current path is preserved.
     * @return Reference to this `Meter` instance, for method chaining.
     */
    public Meter success(final Object pathId) {
        MeterValidator.validatePathArgument(this, "success(pathId)", pathId);
        return commonOk(pathId);
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
            MeterValidator.validatePathArgument(this, "reject(cause)", cause);
            MeterValidator.validateStopPrecondition(this, Markers.INCONSISTENT_REJECT);

            stopTime = collectCurrentTime();
            /* Auto-correct: if never started, use stopTime as startTime (Tier 3) */
            if (startTime == 0) {
                startTime = stopTime;
            }
            failPath = null;
            failMessage = null;
            okPath = null;
            localThreadInstance.set(previousInstance);
            rejectPath = toPath(cause, true);

            if (messageLogger.isInfoEnabled()) {
                SystemMetrics.getInstance().collectRuntimeStatus(this);
                SystemMetrics.getInstance().collectPlatformStatus(this);
                final String message1 = readableMessage();
                messageLogger.info(Markers.MSG_REJECT, message1);
                if (dataLogger.isTraceEnabled()) {
                    final String message2 = json5Message();
                    dataLogger.trace(Markers.DATA_REJECT, message2);
                }
                if (context != null) {
                    context.clear();
                }
            }
        } catch (final Exception t) {
            MeterValidator.logBug(this, "reject(cause)", t);
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
            MeterValidator.validatePathArgument(this, "fail(cause)", cause);
            MeterValidator.validateStopPrecondition(this, Markers.INCONSISTENT_FAIL);

            stopTime = collectCurrentTime();
            /* Auto-correct: if never started, use stopTime as startTime (Tier 3) */
            if (startTime == 0) {
                startTime = stopTime;
            }
            rejectPath = null;
            okPath = null;
            localThreadInstance.set(previousInstance);
            failPath = toPath(cause, false);
            if (cause instanceof Throwable) {
                failMessage = ((Throwable)cause).getLocalizedMessage();
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
            MeterValidator.logBug(this, "fail(cause)", t);
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
        MeterValidator.validateFinalize(this);
        super.finalize();
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
            /* Already closed explicitly. */
            if (stopTime != 0) {
                return;
            }
            MeterValidator.validateStopPrecondition(this, Markers.INCONSISTENT_CLOSE);

            stopTime = collectCurrentTime();
            /* Auto-correct: if never started, use stopTime as startTime (Tier 3) */
            if (startTime == 0) {
                startTime = stopTime;
            }
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
            MeterValidator.logBug(this, "close()", t);
        }
    }
}
