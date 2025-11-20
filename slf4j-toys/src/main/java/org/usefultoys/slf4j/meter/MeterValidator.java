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

    private final String ERROR_MSG_ILLEGAL_CALL = "Illegal call to Meter.{}: {}. id={}";
    private final String ERROR_MSG_NULL_ARGUMENT = "Null argument";

    private void logIllegalCall(final Meter meter, final String methodName, final String message) {
        meter.getMessageLogger().error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_CALL, methodName, message, meter.getFullID(), new CallerStackTraceThrowable());
    }

    private void logIllegalPrecondition(final Meter meter, final Marker marker, final String message) {
        meter.getMessageLogger().error(marker, "{}; id={}", message, meter.getFullID(), new CallerStackTraceThrowable());
    }

    public boolean validateStartPrecondition(final Meter meter) {
        if (meter.getStartTime() != 0) {
            logIllegalPrecondition(meter, Markers.INCONSISTENT_START, "Meter already started.");
            return false;
        }
        return true;
    }

    public void validateStopPrecondition(final Meter meter, final Marker marker) {
        if (meter.getStopTime() != 0) {
            logIllegalPrecondition(meter, marker, "Meter already stopped.");
        } else if (meter.getStartTime() == 0) {
            logIllegalPrecondition(meter, marker, "Meter stopped but not started.");
        } else if (meter.checkCurrentInstance()) {
            logIllegalPrecondition(meter, marker, "Meter out of order.");
        }
    }

    public void validateSubCallArguments(final Meter meter, final String suboperationName) {
        if (suboperationName == null) {
            logIllegalCall(meter, "sub(name)", ERROR_MSG_NULL_ARGUMENT);
        }
    }

    public boolean validateMCallArguments(final Meter meter, final String message) {
        if (message == null) {
            logIllegalCall(meter, "m(message)", ERROR_MSG_NULL_ARGUMENT);
            return false;
        }
        return true;
    }

    public String validateAndFormatMCallArguments(final Meter meter, final String format, final Object... args) {
        if (format == null) {
            logIllegalCall(meter, "m(message, args...)", ERROR_MSG_NULL_ARGUMENT);
            return null;
        }
        try {
            return String.format(format, args);
        } catch (final IllegalFormatException e) {
            logIllegalCall(meter, "m(message, args...)", "Illegal string format");
            return null;
        }
    }

    public boolean validateLimitMillisecondsCallArguments(final Meter meter, final long timeLimit) {
        if (timeLimit <= 0) {
            logIllegalCall(meter, "limitMilliseconds(timeLimit)", "Non-positive argument");
            return false;
        }
        return true;
    }

    public boolean validateIterationsCallArguments(final Meter meter, final long expectedIterations) {
        if (expectedIterations <= 0) {
            logIllegalCall(meter, "iterations(expectedIterations)", "Non-positive argument");
            return false;
        }
        return true;
    }

    public boolean validateIncBy(final Meter meter, final long increment) {
        if (increment <= 0) {
            logIllegalCall(meter, "incBy(increment)", "Non-positive increment");
            return false;
        }
        return true;
    }

    public boolean validateIncPrecondition(final Meter meter) {
        if (meter.getStartTime() == 0) {
            logIllegalPrecondition(meter, Markers.INCONSISTENT_INCREMENT, "Meter not started");
            return false;
        }
        return true;
    }

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

    public boolean validateProgressPrecondition(final Meter meter) {
        if (meter.getStartTime() == 0) {
            logIllegalPrecondition(meter, Markers.INCONSISTENT_PROGRESS, "Meter progress but not started");
            return false;
        }
        return true;
    }

    public void validatePathArgument(final Meter meter, final String methodName, final Object pathId) {
        if (pathId == null) {
            logIllegalCall(meter, methodName, ERROR_MSG_NULL_ARGUMENT);
        }
    }

    public void logBug(final Meter meter, final String methodName, final Throwable t) {
        meter.getMessageLogger().error(Markers.BUG, "Meter.{} method threw exception. id={}", methodName, meter.getFullID(), t);
    }

    public void validateFinalize(final Meter meter) {
        if (meter.getStopTime() == 0 && !meter.getCategory().equals(Meter.UNKNOWN_LOGGER_NAME)) {
            logIllegalPrecondition(meter, Markers.INCONSISTENT_FINALIZED, "Meter started and never stopped. id={}");
        }
    }
}
