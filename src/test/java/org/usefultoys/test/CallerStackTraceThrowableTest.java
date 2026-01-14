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
 
package org.usefultoys.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.usefultoys.slf4j.CallerStackTraceThrowable;
import org.usefultoys.slf4j.LibraryHelper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Unit tests for {@link CallerStackTraceThrowable}.
 * This test class is intentionally in a subpackage of the library (`org.usefultoys.slf4j.test`)
 * to verify that the stack trace manipulation correctly distinguishes between library code
 * and client code.
 */
@DisplayName("CallerStackTraceThrowable: Stack Trace Manipulation")
public class CallerStackTraceThrowableTest {

    @Test
    @DisplayName("Should point to the direct caller method")
    public void testDirectCall() {
        // Scenario: The client code directly creates the throwable.
        final Throwable t = new CallerStackTraceThrowable();
        final StackTraceElement[] stack = t.getStackTrace();

        // Verification: The top of the stack should be this exact method.
        // This test is expected to FAIL with the original implementation because the test package
        // also starts with "org.usefultoys.slf4j".
        assertEquals(getClass().getName(), stack[0].getClassName(), "Stack trace should point to the correct class of methods the method that instantiated CallerStackTraceThrowable");
        assertEquals("testDirectCall", stack[0].getMethodName(), "Stack trace should point to the method that instantiated CallerStackTraceThrowable");
    }

    @Test
    @DisplayName("Should call library and point to direct caller method")
    public void testIndirectCall() {
        // Scenario: The client calls a library method, which internally creates the throwable.
        final Throwable t = LibraryHelper.create();
        final StackTraceElement[] stack = t.getStackTrace();

        // Verification: The top of the stack should be this test method,
        // not the helper method from the library package.
        assertEquals(getClass().getName(), stack[0].getClassName(), "Stack trace should point to the correct class of the method that actually called LibraryHelper.create()");
        assertEquals("testIndirectCall", stack[0].getMethodName(), "Stack trace should point to the method that actually called LibraryHelper.create()");
    }

    @Test
    @DisplayName("Should call library and point to inderect caller")
    public void testNestedCall() {
        // Scenario: The client creates the throwable within a nested private method call.
        final Throwable t = outerMethod();
        final StackTraceElement[] stack = t.getStackTrace();

        // Verification: The top of the stack should be the innermost method that created the throwable.
        assertEquals(getClass().getName(), stack[0].getClassName(), "Stack trace should point to the correct class of the inner method that actually called LibraryHelper.create()");
        assertEquals("innerMethod", stack[0].getMethodName(), "Stack trace should point to the inner method that actually called LibraryHelper.create()");
    }

    @Test
    @DisplayName("Should call library and preserve the cause and still point to the direct caller")
    public void testDirectCallWithCause() {
        // Scenario: The client creates the throwable with an underlying cause.
        final Exception cause = new RuntimeException("Original cause");
        final Throwable t = new CallerStackTraceThrowable(cause);
        final StackTraceElement[] stack = t.getStackTrace();

        // Verification 1: The cause must be correctly preserved.
        assertSame(cause, t.getCause(), "The original cause should be preserved");

        // Verification 2: The stack trace must still point to the caller.
        assertEquals(getClass().getName(), stack[0].getClassName(), "Stack trace should point to the correct class of methods the method that instantiated CallerStackTraceThrowable");
        assertEquals("testDirectCallWithCause", stack[0].getMethodName(), "Stack trace should point to the method that instantiated CallerStackTraceThrowable");
    }

    @Test
    @DisplayName("Should call library and preserve the cause and still point to the indirect caller")
    public void testNestedCallWithCause() {
        // Scenario: The client creates the throwable with an underlying cause.
        final Exception cause = new RuntimeException("Original cause");
        final Throwable t = outerMethodWithCause(cause);
        final StackTraceElement[] stack = t.getStackTrace();

        // Verification 1: The cause must be correctly preserved.
        assertSame(cause, t.getCause(), "The original cause should be preserved");

        // Verification: The top of the stack should be the innermost method that created the throwable.
        assertEquals(getClass().getName(), stack[0].getClassName(), "Stack trace should point to the correct class of the inner method that actually called LibraryHelper.create()");
        assertEquals("innerMethodWithCause", stack[0].getMethodName(), "Stack trace should point to the inner method that actually called LibraryHelper.create()");
    }

    private Throwable outerMethod() {
        return middleMethod();
    }

    private Throwable middleMethod() {
        return innerMethod();
    }

    private Throwable innerMethod() {
        return new CallerStackTraceThrowable();
    }

    private Throwable outerMethodWithCause(final Exception cause) {
        return middleMethodWithCause(cause);
    }

    private Throwable middleMethodWithCause(final Exception cause) {
        return innerMethodWithCause(cause);
    }

    private Throwable innerMethodWithCause(final Exception cause) {
        return new CallerStackTraceThrowable(cause);
    }
}
