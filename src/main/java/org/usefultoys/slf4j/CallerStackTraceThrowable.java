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

package org.usefultoys.slf4j;

import java.util.Arrays;

/**
 * A custom, artificial throwable used to report invalid library usage.
 * <p>
 * This is an artificial throwable. It is not intended to be thrown in error situations.
 * Instead, it is created and logged as a warning to provide a stack trace that points
 * directly to the client method that made an invalid call to the library. This makes it
 * much easier to locate the source of the incorrect usage.
 * <p>
 * The primary purpose is to manipulate the stack trace to point to the user's code or
 * the first Meter method call, removing the library's internal stack frames. When this
 * throwable is logged, the stack trace will start at the first Meter method call (if available),
 * or at the method that incorrectly used the library, rather than showing internal
 * implementation details.
 */
@SuppressWarnings("ExtendsThrowable")
public class CallerStackTraceThrowable extends Throwable {

    private static final long serialVersionUID = 1L;
    public static final String PACKAGE_NAME = CallerStackTraceThrowable.class.getPackage().getName();
    public static final StackTraceElement[] EMPTY_STACK_TRACE = new StackTraceElement[0];

    /**
     * Constructs a new instance with no detail message.
     */
    public CallerStackTraceThrowable() {
    }

    /**
     * Constructs a new instance with the specified detail message.
     *
     * @param message the detail message
     */
    public CallerStackTraceThrowable(final String message) {
        super(message);
    }

    /**
     * Constructs a new instance with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public CallerStackTraceThrowable(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new instance with the specified cause.
     *
     * @param cause the cause
     */
    public CallerStackTraceThrowable(final Throwable cause) {
        super(cause);
    }

    /**
     * Returns the name of the API method from the stack trace, if available.
     * <p>
     * This method examines the first stack trace element and returns the method name
     * if it belongs to the library package. Otherwise, returns null.
     *
     * @return the API method name, or null if not found or stack trace is empty
     */
    public String getApiMethodName() {
        final StackTraceElement[] stacktrace = getStackTrace();
        if (stacktrace.length == 0) {
            return null;
        }
        if (stacktrace[0].getClassName().startsWith(PACKAGE_NAME)) {
            return stacktrace[0].getMethodName();
        }
        return null;
    }

    /**
     * Overrides `fillInStackTrace()` to prevent it from capturing the stack trace again,
     * as it has already been manipulated in the constructor.
     *
     * @return This `Throwable` instance.
     */
    @Override
    public synchronized Throwable fillInStackTrace() {
        final StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        final StackTraceElement[] cleanedStackTrace = removeInternalFrames(stacktrace);
        setStackTrace(cleanedStackTrace);
        return this;
    }

    /**
     * Removes internal library frames from the stack trace, keeping only the API entry point
     * and user code frames.
     * <p>
     * This method processes the raw stack trace to produce a clean stack trace that:
     * <ul>
     *   <li>Skips Thread.getStackTrace(), fillInStackTrace(), and constructor frames (first 3 frames)</li>
     *   <li>Removes internal library frames while keeping the first library method (API entry point)</li>
     *   <li>Filters out reflection and Mockito frames during testing</li>
     *   <li>Preserves all user code frames</li>
     * </ul>
     *
     * @param stacktrace the raw stack trace from Thread.currentThread().getStackTrace()
     * @return the cleaned stack trace with internal frames removed
     */
    StackTraceElement[] removeInternalFrames(final StackTraceElement[] stacktrace) {
        final int framesToDiscard = calculateFramesToDiscard(stacktrace);

        if (framesToDiscard >= stacktrace.length) {
            /* All stack trace frames are from the library itself, or array is too short. */
            return EMPTY_STACK_TRACE;
        } else if (framesToDiscard == 3) {
            /* No library frames found (only user code), keep all from index 3 onwards */
            return Arrays.copyOfRange(stacktrace, 3, stacktrace.length);
        } else if (stacktrace[framesToDiscard - 1].getClassName().equals(CallerStackTraceThrowable.class.getName())) {
            /* Found exception constructor called directly from user code. */
            return Arrays.copyOfRange(stacktrace, framesToDiscard, stacktrace.length);
        } else {
            /* Library frames found, keep first library frame + all user code.
               Decrease by 1 to include the last library method (API entry point). */
            return Arrays.copyOfRange(stacktrace, framesToDiscard - 1, stacktrace.length);
        }
    }

    /**
     * Determines how many leading frames of the raw stack trace belong to system, library,
     * reflection or Mockito internals and should be discarded.
     * <p>
     * A proper handling is required while running under tests, since the stack trace may be under
     * influence of Mockito additional reflection calls, and the method may be thrown from the test
     * class, which is under the same package, but is not considered library itself.
     *
     * @param stacktrace the raw stack trace from Thread.currentThread().getStackTrace()
     * @return the index of the first frame that is not an internal frame to be discarded
     */
    private int calculateFramesToDiscard(final StackTraceElement[] stacktrace) {
        /* Always skip index 0 (Thread.getStackTrace, fillInStackTrace itself and Exception constructor) and start from index 3 */
        int framesToDiscard = 3;

        while (framesToDiscard < stacktrace.length && isInternalFrame(stacktrace[framesToDiscard])) {
            framesToDiscard++;
        }

        return framesToDiscard;
    }

    /**
     * Determines whether a single stack trace frame belongs to system, library, reflection or
     * Mockito internals and should therefore be discarded by {@link #calculateFramesToDiscard}.
     *
     * @param frame the stack trace frame to classify
     * @return true if the frame is internal and should be discarded, false if it belongs to user code
     */
    private static boolean isInternalFrame(final StackTraceElement frame) {
        final String className = frame.getClassName();
        return (className.startsWith(PACKAGE_NAME) ||
                    /* During tests, skip artificial stack trace calls generated by mockito */
                    className.contains("org.mockito") ||
                    className.contains("sun.reflect") ||
                    className.contains("java.lang.invoke") ||
                    className.contains("java.lang.reflect")
               ) &&
               /* During tests, the test class appears as the same package name, but ist not considered being part of the library itself. */
               !(className.endsWith("Test") || className.endsWith("Tests"));
    }
}
