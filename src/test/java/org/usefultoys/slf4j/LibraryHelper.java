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

/**
 * A helper class located in the library's package (`org.usefultoys.slf4j`) for testing purposes. It simulates an
 * internal library method that creates a {@link CallerStackTraceThrowable}.
 */
public final class LibraryHelper {
    private LibraryHelper() {
        // Utility class
    }

    public static CallerStackTraceThrowable innerMethod() {
        return new CallerStackTraceThrowable();
    }

    public static CallerStackTraceThrowable outerMethod() {
        return middleMethod();
    }

    public static CallerStackTraceThrowable middleMethod() {
        return innerMethod();
    }

    public static CallerStackTraceThrowable outerMethodWithCause(final Exception cause) {
        return middleMethodWithCause(cause);
    }

    public static CallerStackTraceThrowable middleMethodWithCause(final Exception cause) {
        return innerMethodWithCause(cause);
    }

    public static CallerStackTraceThrowable innerMethodWithCause(final Exception cause) {
        return new CallerStackTraceThrowable(cause);
    }

    public static CallerStackTraceThrowable innerMethodWithMessage(final String message) {
        return new CallerStackTraceThrowable(message);
    }

    public static CallerStackTraceThrowable middleMethodWithMessage(final String message) {
        return innerMethodWithMessage(message);
    }

    public static CallerStackTraceThrowable outerMethodWithMessage(final String message) {
        return middleMethodWithMessage(message);
    }

    public static CallerStackTraceThrowable innerMethodWithMessageAndCause(final String message, final Exception cause) {
        return new CallerStackTraceThrowable(message, cause);
    }

    public static CallerStackTraceThrowable middleMethodWithMessageAndCause(final String message, final Exception cause) {
        return innerMethodWithMessageAndCause(message, cause);
    }

    public static CallerStackTraceThrowable outerMethodWithMessageAndCause(final String message, final Exception cause) {
        return middleMethodWithMessageAndCause(message, cause);
    }
}
