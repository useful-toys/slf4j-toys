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
 * The primary purpose is to manipulate the stack trace to point to the user's code,
 * removing the library's internal stack frames. When this throwable is logged, the stack
 * trace will start at the method that incorrectly used the library, rather than showing
 * internal implementation details.
 */
@SuppressWarnings("ExtendsThrowable")
public class CallerStackTraceThrowable extends Throwable {

    private static final long serialVersionUID = 1L;
    public static final String PACKAGE_NAME = CallerStackTraceThrowable.class.getPackage().getName();

    /**
     * Constructs a new throwable, removing a specified number of stack frames to point to the caller.
     */
    public CallerStackTraceThrowable() {
        this(null);
    }

    /**
     * Constructs a new throwable with a cause, removing a specified number of stack frames to point to the caller.
     *
     * @param e The underlying cause of this throwable.
     */
    @SuppressWarnings({"AssignmentToMethodParameter", "OverridableMethodCallDuringObjectConstruction"})
    public CallerStackTraceThrowable(final Throwable e) {
        super(e);
        int framesToDiscard = 1;
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        while (stacktrace[framesToDiscard].getClassName().startsWith(PACKAGE_NAME)) {
            framesToDiscard++;
        }
        stacktrace = Arrays.copyOfRange(stacktrace, framesToDiscard, stacktrace.length);
        setStackTrace(stacktrace);
    }

    /**
     * Overrides `fillInStackTrace()` to prevent it from capturing the stack trace again,
     * as it has already been manipulated in the constructor.
     *
     * @return This `Throwable` instance.
     */
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
