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

import lombok.experimental.UtilityClass;
import org.slf4j.Marker;
import org.usefultoys.slf4j.CallerStackTraceThrowable;

import java.util.IllegalFormatException;

/**
 * A utility class responsible for validating the state of a {@link Meter} instance before executing an operation.
 * <p>
 * This class centralizes all validation logic, ensuring that the {@link Meter} class remains clean and focused on its
 * primary responsibility of managing the operation s lifecycle. All methods are and designed to be
 * non-intrusive, logging warnings for invalid states but never throwing exceptions that could disrupt the application
 * flow.
 */
@UtilityClass
public class MeterValidator {

    /**
     * Logs an illegal call to a Meter method.
     *
     * @param meter      The Meter instance on which the illegal call was made.
     * @param methodName The name of the method that was called illegally.
     * @param message    A descriptive message about the illegal call.
     */
    private void logIllegalCall(final Meter meter, final String methodName, final String message) {
        meter.getMessageLogger().error(Markers.ILLEGAL, "Illegal call to Meter.{}: {}; id={}", methodName, message, meter.getFullID(), new CallerStackTraceThrowable());
    }

    /**
     * Logs an illegal precondition for a Meter operation.
     *
     * @param meter   The Meter instance with the illegal precondition.
     * @param marker  The SLF4J marker to use for the log message.
     * @param message A descriptive message about the illegal precondition.
     */
    private void logIllegalPrecondition(final Meter meter, final Marker marker, final String message) {
        meter.getMessageLogger().error(marker, "{}; id={}", message, meter.getFullID(), new CallerStackTraceThrowable());
    }

    /**
     * Validates the precondition for starting a Meter operation.
     * An operation cannot be started if it has already been started.
     *
     * @param meter The Meter instance to validate.
     * @return {@code true} if the precondition is met, {@code false} otherwise.
     */
    public boolean validateStartPrecondition(final Meter meter) {
        if (meter.getStartTime() != 0) {
            logIllegalPrecondition(meter, Markers.INCONSISTENT_START, "Meter already started");
            return false;
        }
        return true;
    }

    /**
     * Validates the preconditions for stopping a Meter operation.
     * Checks if the meter has already been stopped, if it was started, and if it's the current instance on the thread.
     *
     * @param meter  The Meter instance to validate.
     * @param marker The SLF4J marker to use for the log message in case of inconsistency.
     */
    public void validateStopPrecondition(final Meter meter, final Marker marker) {
        if (meter.getStopTime() != 0) {
            logIllegalPrecondition(meter, marker, "Meter already stopped");
        } else if (meter.getStartTime() == 0) {
            logIllegalPrecondition(meter, marker, "Meter stopped but not started");
        } else if (meter.checkCurrentInstance()) {
            logIllegalPrecondition(meter, marker, "Meter out of order");
        }
    }

    /**
     * Validates the arguments for the `sub` method of a Meter.
     *
     * @param meter          The Meter instance.
     * @param suboperationName The name of the sub-operation.
     */
    public void validateSubCallArguments(final Meter meter, final String suboperationName) {
        if (suboperationName == null) {
            logIllegalCall(meter, "sub(name)", "Null argument");
        }
    }

    /**
     * Validates the arguments for the `m` method (message) of a Meter.
     *
     * @param meter   The Meter instance.
     * @param message The descriptive message.
     * @return {@code true} if the message is not null, {@code false} otherwise.
     */
    public boolean validateMCallArguments(final Meter meter, final String message) {
        if (message == null) {
            logIllegalCall(meter, "m(message)", "Null argument");
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
    public String validateAndFormatMCallArguments(final Meter meter, final String format, final Object... args) {
        if (format == null) {
            logIllegalCall(meter, "m(message, args...)", "Null argument");
            return null;
        }
        try {
            return String.format(format, args);
        } catch (final IllegalFormatException e) {
            logIllegalCall(meter, "m(message, args...)", "Illegal string format");
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
    public boolean validateLimitMillisecondsCallArguments(final Meter meter, final long timeLimit) {
        if (timeLimit <= 0) {
            logIllegalCall(meter, "limitMilliseconds(timeLimit)", "Non-positive argument");
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
    public boolean validateIterationsCallArguments(final Meter meter, final long expectedIterations) {
        if (expectedIterations <= 0) {
            logIllegalCall(meter, "iterations(expectedIterations)", "Non-positive argument");
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
    public boolean validateIncBy(final Meter meter, final long increment) {
        if (increment <= 0) {
            logIllegalCall(meter, "incBy(increment)", "Non-positive increment");
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
    public boolean validateIncPrecondition(final Meter meter) {
        if (meter.getStartTime() == 0) {
            logIllegalPrecondition(meter, Markers.INCONSISTENT_INCREMENT, "Meter not started");
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
    public boolean validateIncToArguments(final Meter meter, final long currentIteration) {
        if (currentIteration <= 0) {
            logIllegalCall(meter, "incTo(currentIteration)", "Non-positive argument");
            return false;
        }
        if (currentIteration <= meter.getCurrentIteration()) {
            logIllegalCall(meter, "incTo(currentIteration)", "Non-forward increment");
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
    public boolean validateProgressPrecondition(final Meter meter) {
        if (meter.getStartTime() == 0) {
            logIllegalPrecondition(meter, Markers.INCONSISTENT_PROGRESS, "Meter progress but not started");
            return false;
        }
        return true;
    }

    /**
     * Validates the precondition for setting a path on a Meter.
     * A path cannot be set if the meter has not been started.
     *
     * @param meter The Meter instance to validate.
     * @return {@code true} if the precondition is met (started), {@code false} otherwise.
     */
    public boolean validatePathPrecondition(final Meter meter) {
        if (meter.getStartTime() == 0) {
            logIllegalPrecondition(meter, Markers.ILLEGAL, "Meter path but not started");
            return false;
        }
        return true;
    }

    /**
     * Validates the path argument for methods like `ok(pathId)`, `reject(cause)`, `fail(cause)`.
     *
     * @param meter      The Meter instance.
     * @param methodName The name of the method being called.
     * @param pathId     The path identifier object.
     */
    public void validatePathArgument(final Meter meter, final String methodName, final Object pathId) {
        if (pathId == null) {
            logIllegalCall(meter, methodName, "Null argument");
        }
    }

    /**
     * Logs a bug when a Meter method throws an unexpected exception.
     *
     * @param meter      The Meter instance where the exception occurred.
     * @param methodName The name of the method that threw the exception.
     * @param t          The Throwable that was caught.
     */
    public void logBug(final Meter meter, final String methodName, final Throwable t) {
        meter.getMessageLogger().error(Markers.BUG, "Meter.{} method threw exception; id={}", methodName, meter.getFullID(), t);
    }

    /**
     * Validates the state of a Meter instance during finalization.
     * Logs an error if a Meter was started but never explicitly stopped.
     *
     * @param meter The Meter instance being finalized.
     */
    public void validateFinalize(final Meter meter) {
        if (meter.getStopTime() == 0 && !meter.getCategory().equals(Meter.UNKNOWN_LOGGER_NAME)) {
            logIllegalPrecondition(meter, Markers.INCONSISTENT_FINALIZED, "Meter started and never stopped; id={}");
        }
    }
}
