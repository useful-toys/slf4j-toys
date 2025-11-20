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

import org.slf4j.Marker;
import org.usefultoys.slf4j.CallerStackTraceThrowable;

import java.util.IllegalFormatException;

/**
 * A utility class responsible for validating the state of a {@link Meter} instance
 * before executing an operation.
 * <p>
 * This class centralizes all validation logic, ensuring that the {@link Meter} class
 * remains clean and focused on its primary responsibility of managing the operation
s
 * lifecycle. All methods are static and designed to be non-intrusive, logging warnings
 * for invalid states but never throwing exceptions that could disrupt the application flow.
 */
public final class MeterValidator {

    private MeterValidator() {
        // Utility class
    }

    private static final String ERROR_MSG_METER_ALREADY_STARTED = "Meter already started. id={}";
    private static final String ERROR_MSG_METER_ALREADY_STOPPED = "Meter already stopped. id={}";
    private static final String ERROR_MSG_METER_STOPPED_BUT_NOT_STARTED = "Meter stopped but not started. id={}";
    private static final String ERROR_MSG_METER_INCREMENTED_BUT_NOT_STARTED = "Meter incremented but not started. id={}";
    private static final String ERROR_MSG_METER_PROGRESS_BUT_NOT_STARTED = "Meter progress but not started. id={}";
    private static final String ERROR_MSG_ILLEGAL_ARGUMENT = "Illegal call to Meter.{}: {}. id={}";
    private static final String ERROR_MSG_METER_OUT_OF_ORDER = "Meter out of order. id={}";
    private static final String ERROR_MSG_NULL_ARGUMENT = "Null argument";
    private static final String ERROR_MSG_NON_POSITIVE_ARGUMENT = "Non-positive argument";
    private static final String ERROR_MSG_ILLEGAL_STRING_FORMAT = "Illegal string format";
    private static final String ERROR_MSG_NON_FORWARD_ITERATION = "Non-forward iteration";
    private static final String ERROR_MSG_METER_STARTED_AND_NEVER_STOPPED = "Meter started and never stopped. id={}";


    public static boolean validateStart(final Meter meter) {
        if (meter.getStartTime() != 0) {
            meter.getMessageLogger().error(Markers.INCONSISTENT_START, ERROR_MSG_METER_ALREADY_STARTED, meter.getFullID(), new CallerStackTraceThrowable());
            return false;
        }
        return true;
    }

    public static boolean validateStop(final Meter meter, final Marker marker) {
        if (meter.getStopTime() != 0) {
            meter.getMessageLogger().error(marker, ERROR_MSG_METER_ALREADY_STOPPED, meter.getFullID(), new CallerStackTraceThrowable());
            return false;
        }
        if (meter.getStartTime() == 0) {
            meter.getMessageLogger().error(marker, ERROR_MSG_METER_STOPPED_BUT_NOT_STARTED, meter.getFullID(), new CallerStackTraceThrowable());
            return false;
        }
        if (meter.checkCurrentInstance()) {
            meter.getMessageLogger().error(marker, ERROR_MSG_METER_OUT_OF_ORDER, meter.getFullID(), new CallerStackTraceThrowable());
            return false;
        }
        return true;
    }

    public static boolean validateSubCallArguments(final Meter meter, final String suboperationName) {
        if (suboperationName == null) {
            meter.getMessageLogger().error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "sub(name)", ERROR_MSG_NULL_ARGUMENT, meter.getFullID(), new CallerStackTraceThrowable());
            return false;
        }
        return true;
    }

    public static boolean validateMCallArguments(final Meter meter, final String message) {
        if (message == null) {
            meter.getMessageLogger().error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "m(message)", ERROR_MSG_NULL_ARGUMENT, meter.getFullID(), new CallerStackTraceThrowable());
            return false;
        }
        return true;
    }

    public static String validateMCallArguments(final Meter meter, final String format, final Object... args) {
        if (format == null) {
            meter.getMessageLogger().error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "m(message, args...)", ERROR_MSG_NULL_ARGUMENT, meter.getFullID(), new CallerStackTraceThrowable());
            return null;
        }
        try {
            return String.format(format, args);
        } catch (final IllegalFormatException e) {
            meter.getMessageLogger().error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "m(message, args...)", ERROR_MSG_ILLEGAL_STRING_FORMAT, meter.getFullID(), new CallerStackTraceThrowable(e));
            return null;
        }
    }

    public static boolean validateLimitMillisecondsCallArguments(final Meter meter, final long timeLimit) {
        if (timeLimit <= 0) {
            meter.getMessageLogger().error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "limitMilliseconds(timeLimit)", ERROR_MSG_NON_POSITIVE_ARGUMENT, meter.getFullID(), new CallerStackTraceThrowable());
            return false;
        }
        return true;
    }

    public static boolean validateIterationsCallArguments(final Meter meter, final long expectedIterations) {
        if (expectedIterations <= 0) {
            meter.getMessageLogger().error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "iterations(expectedIterations)", ERROR_MSG_NON_POSITIVE_ARGUMENT, meter.getFullID(), new CallerStackTraceThrowable());
            return false;
        }
        return true;
    }

    public static boolean validateInc(final Meter meter) {
        if (meter.getStartTime() == 0) {
            meter.getMessageLogger().error(Markers.INCONSISTENT_INCREMENT, ERROR_MSG_METER_INCREMENTED_BUT_NOT_STARTED, meter.getFullID(), new CallerStackTraceThrowable());
            return false;
        }
        return true;
    }

    public static boolean validateIncBy(final Meter meter, final long increment) {
        if (meter.getStartTime() == 0) {
            meter.getMessageLogger().error(Markers.INCONSISTENT_INCREMENT, ERROR_MSG_METER_INCREMENTED_BUT_NOT_STARTED, meter.getFullID(), new CallerStackTraceThrowable());
            return false;
        }
        if (increment <= 0) {
            meter.getMessageLogger().error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "incBy(increment)", ERROR_MSG_NON_POSITIVE_ARGUMENT, meter.getFullID(), new CallerStackTraceThrowable());
            return false;
        }
        return true;
    }

    public static boolean validateIncTo(final Meter meter, final long currentIteration) {
        if (meter.getStartTime() == 0) {
            meter.getMessageLogger().error(Markers.INCONSISTENT_INCREMENT, ERROR_MSG_METER_INCREMENTED_BUT_NOT_STARTED, meter.getFullID(), new CallerStackTraceThrowable());
            return false;
        }
        if (currentIteration <= 0) {
            meter.getMessageLogger().error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "incTo(currentIteration)", ERROR_MSG_NON_POSITIVE_ARGUMENT, meter.getFullID(), new CallerStackTraceThrowable());
            return false;
        }
        if (currentIteration <= meter.getCurrentIteration()) {
            meter.getMessageLogger().error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, "incTo(currentIteration)", ERROR_MSG_NON_FORWARD_ITERATION, meter.getFullID(), new CallerStackTraceThrowable());
            return false;
        }
        return true;
    }

    public static boolean validateProgressPrecondition(final Meter meter) {
        if (meter.getStartTime() == 0) {
            meter.getMessageLogger().error(Markers.INCONSISTENT_PROGRESS, ERROR_MSG_METER_PROGRESS_BUT_NOT_STARTED, meter.getFullID(), new CallerStackTraceThrowable());
            return false;
        }
        return true;
    }

    public static void validatePathArgument(final Meter meter, final String methodName, final Object pathId) {
        if (pathId == null) {
            meter.getMessageLogger().error(Markers.ILLEGAL, ERROR_MSG_ILLEGAL_ARGUMENT, methodName,
                    ERROR_MSG_NULL_ARGUMENT, meter.getFullID(), new CallerStackTraceThrowable());
        }
    }

    public static void logBug(final Meter meter, final String methodName, final Throwable t) {
        meter.getMessageLogger().error(Markers.BUG, "Meter.{} method threw exception. id={}", methodName, meter.getFullID(), t);
    }

    public static void validateFinalize(final Meter meter) {
        if (meter.getStopTime() == 0 && !meter.getCategory().equals(Meter.UNKNOWN_LOGGER_NAME)) {
            meter.getMessageLogger().error(Markers.INCONSISTENT_FINALIZED, ERROR_MSG_METER_STARTED_AND_NEVER_STOPPED, meter.getFullID(), new CallerStackTraceThrowable());
        }
    }
}
