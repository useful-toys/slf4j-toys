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
package org.usefultoys.slf4j.meter;

import lombok.experimental.UtilityClass;

import org.usefultoys.slf4j.CallerStackTraceThrowable;

import java.util.IllegalFormatException;

/**
 * A utility class responsible for validating the state of a {@link Meter} instance before executing an operation.
 * <p>
 * This class centralizes all validation logic, ensuring that the {@link Meter} class remains clean and focused on its
 * primary responsibility of managing the operation's lifecycle. All methods are designed to be
 * non-intrusive, logging warnings for invalid states but never throwing exceptions that could disrupt the application
 * flow.
 * 
 * @author Daniel Felix Ferber
 * @author Co-authored-by: GitHub Copilot using Claude Sonnet 4.5
 */
@UtilityClass
public class MeterValidator {

    /* ========== Validate Call Argument Methods ========== */

    /**
     * Validates the arguments for the `sub` method of a Meter.
     *
     * @param meter          The Meter instance.
     * @param suboperationName The name of the sub-operation.
     */
    void validateSubCallArgument(final Meter meter, final String suboperationName) {
        if (suboperationName == null) {
            logInvalidArgument(meter, "Null argument: suboperation name");
        }
    }

    /**
     * Validates the arguments for the `m` method (message) of a Meter.
     *
     * @param meter   The Meter instance.
     * @param message The descriptive message.
     * @return {@code true} if the message is not null, {@code false} otherwise.
     */
    boolean validateMCallArgument(final Meter meter, final String message) {
        if (message == null) {
            logInvalidArgument(meter, "Null argument: message");
            return false;
        }
        return true;
    }

    /**
     * Validates and formats the arguments for the `m` method (message with format) of a Meter.
     *
     * @param meter  The Meter instance.
     * @param format The message format string.
     * @param args   The arguments for the format string.
     * @return The formatted string, or {@code null} if the format is null or invalid.
     */
    String validateMCallArgument(final Meter meter, final String format, final Object... args) {
        if (format == null) {
            logInvalidArgument(meter, "Null argument: format");
            return null;
        }
        try {
            return String.format(format, args);
        } catch (final IllegalFormatException e) {
            logInvalidArgument(meter, "Illegal format string");
            return null;
        }
    }


    /**
     * Validates the arguments for the `limitMilliseconds` method of a Meter.
     *
     * @param meter     The Meter instance.
     * @param timeLimit The time limit in milliseconds.
     * @return {@code true} if the time limit is positive, {@code false} otherwise.
     */
    boolean validateLimitMillisecondsCallArgument(final Meter meter, final long timeLimit) {
        if (timeLimit <= 0) {
            logInvalidArgument(meter, "Non-positive argument: timeLimit");
            return false;
        }
        return true;
    }


    /**
     * Validates the arguments for the `iterations` method of a Meter.
     *
     * @param meter            The Meter instance.
     * @param expectedIterations The total number of expected iterations.
     * @return {@code true} if the expected iterations count is positive, {@code false} otherwise.
     */
    boolean validateIterationsCallArgument(final Meter meter, final long expectedIterations) {
        if (expectedIterations <= 0) {
            logInvalidArgument(meter, "Non-positive argument: expectedIterations");
            return false;
        }
        return true;
    }


    /**
     * Validates the increment value for `incBy` method.
     *
     * @param meter     The Meter instance.
     * @param increment The number of iterations to add.
     * @return {@code true} if the increment is positive, {@code false} otherwise.
     */
    boolean validateIncByCallArgument(final Meter meter, final long increment) {
        if (increment <= 0) {
            logInvalidArgument(meter, "Non-positive argument: increment");
            return false;
        }
        return true;
    }


    /**
     * Validates the arguments for the `incTo` method of a Meter.
     *
     * @param meter          The Meter instance.
     * @param currentIteration The new total number of iterations.
     * @return {@code true} if the new iteration count is positive and greater than the current count, {@code false} otherwise.
     */
    boolean validateIncToCallArgument(final Meter meter, final long currentIteration) {
        if (currentIteration <= 0) {
            logInvalidArgument(meter, "Non-positive argument: currentIteration");
            return false;
        }
        if (currentIteration <= meter.getCurrentIteration()) {
            logInvalidArgument(meter, "Non-forward argument: currentIteration");
            return false;
        }
        return true;
    }


    /**
     * Validates the path argument for methods like `ok(pathId)`, `reject(cause)`, `fail(cause)`.
     *
     * @param meter  The Meter instance.
     * @param pathId The path identifier object.
     * @return {@code true} if the argument is valid, {@code false} otherwise.
     */
    boolean validatePathCallArgument(final Meter meter, final Object pathId) {
        if (pathId == null) {
            logInvalidArgument(meter, "Null argument: pathId");
            return false;
        }
        return true;
    }


    /* ========== Validate Precondition Methods ========== */

    /**
     * Validates the precondition for starting a Meter operation.
     * An operation cannot be started if it has already been started.
     *
     * @param meter The Meter instance to validate.
     * @return {@code true} if the precondition is met, {@code false} otherwise.
     */
    boolean validateStartPrecondition(final Meter meter) {
        if (meter.getStopTime() != 0) {
            logInvalidTransition(meter, "Meter already stopped, must use instance only once");
            return false;
        }
        if (meter.getStartTime() != 0) {
            logInvalidTransition(meter, "Meter already started, must call start() only once");
            return false;
        }
        return true;
    }

    /**
     * Validates the precondition for setting a description on a Meter with the `m` method.
     * The meter must not have been stopped yet (not in terminal state: OK, Rejected, or Failed).
     *
     * @param meter The Meter instance to validate.
     * @return {@code true} if the precondition is met, {@code false} otherwise.
     */
    boolean validateMPrecondition(final Meter meter) {
        if (meter.getStopTime() != 0) {
            logInvalidStateAlreadyStopped(meter);
            return false;
        }
        return true;
    }

    /**
     * Validates the preconditions for stopping a Meter operation.
     * Checks if the meter has already been stopped, if it was started, and if it's the current instance on the thread.
     *
     * @param meter  The Meter instance to validate.
     * @return {@code true} if the meter can proceed to stop (even with warnings), {@code false} if the meter is already stopped.
     */
    boolean validateStopPrecondition(final Meter meter) {
        if (meter.getStopTime() != 0) {
            logInvalidTransition(meter, "Meter already stopped, must call ok/reject/fail/success() only once");
            return false;
        } else if (meter.getStartTime() == 0) {
            logInvalidTransition(meter, "Meter not started, should call start() first");
        } else if (meter.checkCurrentInstance()) {
            logInvalidTransition(meter, "Meter stopped before the current instance on the thread, possible mismatched start/stop calls");
        }
        return true;
    }

    /**
     * Validates the precondition for adding context to a Meter.
     * The meter must not have already been stopped before context can be added.
     *
     * @param meter The Meter instance to validate.
     * @return {@code true} if the precondition is met (not stopped), {@code false} otherwise.
     */
    boolean validateContextPrecondition(final Meter meter) {
        if (meter.getStopTime() != 0) {
            logInvalidStateAlreadyStopped(meter);
            return false;
        }
        return true;
    }

    /**
     * Validates the precondition for configuring a time limit on a Meter.
     * The meter must not have already been stopped before a time limit can be configured.
     *
     * @param meter The Meter instance to validate.
     * @return {@code true} if the precondition is met (not stopped), {@code false} otherwise.
     */
    boolean validateLimitMillisecondsPrecondition(final Meter meter) {
        if (meter.getStopTime() != 0) {
            logInvalidStateAlreadyStopped(meter);
            return false;
        }
        return true;
    }

    /**
     * Validates the precondition for configuring expected iterations on a Meter.
     * The meter must not have already been stopped before iterations can be configured.
     *
     * @param meter The Meter instance to validate.
     * @return {@code true} if the precondition is met (not stopped), {@code false} otherwise.
     */
    boolean validateIterationsPrecondition(final Meter meter) {
        if (meter.getStopTime() != 0) {
            logInvalidStateAlreadyStopped(meter);
            return false;
        }
        return true;
    }

    /**
     * Validates the precondition for incrementing a Meter's iteration count.
     * An increment cannot occur if the meter has not been started.
     *
     * @param meter The Meter instance to validate.
     * @return {@code true} if the precondition is met, {@code false} otherwise.
     */
    boolean validateIncPrecondition(final Meter meter) {
        if (meter.getStartTime() == 0) {
            logInvalidStateNotStarted(meter);
            return false;
        }
        if (meter.getStopTime() != 0) {
            logInvalidStateAlreadyStopped(meter);
            return false;
        }
        return true;
    }

    /**
     * Validates the precondition for reporting progress on a Meter.
     * Progress cannot be reported if the meter has not been started.
     *
     * @param meter The Meter instance to validate.
     * @return {@code true} if the precondition is met, {@code false} otherwise.
     */
    boolean validateProgressPrecondition(final Meter meter) {
        if (meter.getStartTime() == 0) {
            logInvalidStateNotStarted(meter);
            return false;
        }
        if (meter.getStopTime() != 0) {
            logInvalidStateAlreadyStopped(meter);
            return false;
        }
        return true;
    }

    /**
     * Validates the precondition for setting a path on a Meter.
     * A path cannot be set if the meter has not been started or has already been stopped.
     *
     * @param meter The Meter instance to validate.
     * @return {@code true} if the precondition is met (started and not stopped), {@code false} otherwise.
     */
    boolean validatePathPrecondition(final Meter meter) {
        if (meter.getStartTime() == 0) {
            logInvalidStateNotStarted(meter);
            return false;
        }
        if (meter.getStopTime() != 0) {
            logInvalidStateAlreadyStopped(meter);
            return false;
        }
        return true;
    }


    /* ========== Utility Methods ========== */

    void logInvalidStateAlreadyStopped(final Meter meter) {
        logInvalidState(meter, "Meter already stopped, must call before ok/reject/fail/success()");
    }

    void logInvalidStateNotStarted(final Meter meter) {
        logInvalidState(meter, "Meter not yet started, must call start() first");
    }

    /**
     * Logs an illegal argument call to a Meter method.
     *
     * @param meter      The Meter instance on which the illegal call was made.
     * @param message    A descriptive message about the illegal argument.
     */
    void logInvalidArgument(final Meter meter, final String message) {
        final CallerStackTraceThrowable throwable = new CallerStackTraceThrowable();
        meter.getMessageLogger().error(Markers.INVALID_ARGUMENT, "Meter.{} - {}; id={}", throwable.getApiMethodName(), message, meter.getFullID(), throwable);
    }

    /**
     * Logs an illegal precondition for a Meter operation.
     * @param meter   The Meter instance with the illegal precondition.
     * @param message A descriptive message about the illegal precondition.
     */
    void logInvalidState(final Meter meter, final String message) {
        final CallerStackTraceThrowable throwable = new CallerStackTraceThrowable();
        meter.getMessageLogger().error(Markers.INVALID_STATE, "Meter.{} - {}; id={}", throwable.getApiMethodName(), message, meter.getFullID(), throwable);
    }

    void logInvalidTransition(final Meter meter, final String message) {
        final CallerStackTraceThrowable throwable = new CallerStackTraceThrowable();
        meter.getMessageLogger().error(Markers.INVALID_TRANSITION, "Meter.{} - {}; id={}", throwable.getApiMethodName(), message, meter.getFullID(), throwable);
    }

    /**
     * Logs an unexpected exception that occurred within the Meter implementation.
     *
     * @param meter The Meter instance where the exception occurred.
     * @param t     The Throwable that was caught.
     */
    void logUnexpectedException(final Meter meter, final Throwable t) {
        final CallerStackTraceThrowable e = new CallerStackTraceThrowable();
        meter.getMessageLogger().error(Markers.UNEXPECTED_EXCEPTION, "Meter.{} - Unexpected exception; id={}", e.getApiMethodName(),  meter.getFullID(), t);
    }
}
