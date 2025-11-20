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

import org.junit.jupiter.api.Test;
import org.usefultoys.slf4j.CallerStackTraceThrowable;
import org.usefultoys.slf4j.LibraryHelper;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link CallerStackTraceThrowable}.
 * This test class is intentionally outside the library package namespace (`org.usefultoys.slf4j`)
 * to verify that the stack trace manipulation correctly distinguishes between library code
 * and client code.
 */
public class CallerStackTraceThrowableTest {

    @Test
    public void testDirectCall() {
        // Scenario: The client code directly creates the throwable.
        final Throwable t = new CallerStackTraceThrowable();
        final StackTraceElement[] stack = t.getStackTrace();

        // Verification: The top of the stack should be this exact method.
        // This test is expected to FAIL with the original implementation.
        assertEquals(this.getClass().getName(), stack[0].getClassName());
        assertEquals("testDirectCall", stack[0].getMethodName());
    }

    @Test
    public void testIndirectCall() {
        // Scenario: The client calls a library method, which internally creates the throwable.
        final Throwable t = LibraryHelper.create();
        final StackTraceElement[] stack = t.getStackTrace();

        // Verification: The top of the stack should be this test method,
        // not the helper method from the library package.
        assertEquals(this.getClass().getName(), stack[0].getClassName());
        assertEquals("testIndirectCall", stack[0].getMethodName());
    }

    @Test
    public void testNestedCall() {
        // Scenario: The client creates the throwable within a nested private method call.
        final Throwable t = outerMethod();
        final StackTraceElement[] stack = t.getStackTrace();

        // Verification: The top of the stack should be the innermost method that created the throwable.
        assertEquals(this.getClass().getName(), stack[0].getClassName());
        assertEquals("innerMethod", stack[0].getMethodName());
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
}
