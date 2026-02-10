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

package org.usefultoys.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.usefultoys.slf4j.CallerStackTraceThrowable;
import org.usefultoys.slf4j.LibraryHelper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Unit tests for {@link CallerStackTraceThrowable}.
 * <p>
 * Tests validate that CallerStackTraceThrowable correctly manipulates the stack trace to point to the API entry point
 * or user code, removing internal library frames. This ensures that stack traces are useful for debugging without
 * exposing unnecessary library implementation details.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>Direct Library Call:</b> Validates stack trace when library frame is called directly from test</li>
 *   <li><b>Library Nested Calls:</b> Validates that only first library frame is kept for nested library calls (innerMethod, middleMethod, outerMethod)</li>
 *   <li><b>User Code Nested Calls:</b> Validates that user code frames are preserved along with first library frame</li>
 *   <li><b>Stack Trace Manipulation:</b> Verifies that Thread.getStackTrace() frame is always removed</li>
 *   <li><b>Frame Ordering:</b> Confirms correct frame ordering from library entry point through user code</li>
 *   <li><b>Internal Library Cleanup:</b> Validates that all internal library frames are removed except the API entry point</li>
 * </ul>
 */
@DisplayName("CallerStackTraceThrowable: Stack Trace Manipulation")
public class CallerStackTraceThrowableTest {

    @Test
    @DisplayName("should point to CallerStackTraceThrowable when directly instantiated from test")
    public void shouldPointToCallerStackTraceThrowableWhenDirectlyInstantiated() {
        // Given: a direct instantiation of CallerStackTraceThrowable
        final CallerStackTraceThrowable t = new CallerStackTraceThrowable();
        final StackTraceElement[] stack = t.getStackTrace();

        // Then: getApiMethodName() should return null since there was no API method call
        assertNull(t.getApiMethodName(), "getApiMethodName() should return null when instantiated directly by user code");
        // When: the stack trace is examined (framesToDiscard starts at 3, no library frames found before user code)
        assertEquals(getClass().getName(), stack[0].getClassName(), "Frame 0: should point to test method");
        assertEquals("shouldPointToCallerStackTraceThrowableWhenDirectlyInstantiated", stack[0].getMethodName(), "Frame 0: should be test method");
    }

    @Test
    @DisplayName("should point to LibraryHelper.innerMethod when called directly from test")
    public void shouldPointToLibraryHelperInnerMethodWhenCalledDirectly() {
        // Given: a call to LibraryHelper.innerMethod (which creates CallerStackTraceThrowable)
        final CallerStackTraceThrowable t = LibraryHelper.innerMethod();
        final StackTraceElement[] stack = t.getStackTrace();

        // When: the stack trace is examined (framesToDiscard stops at LibraryHelper.innerMethod)
        // Then: getApiMethodName() should return innerMethod
        assertEquals("innerMethod", t.getApiMethodName(), "getApiMethodName() should return innerMethod");
        // Then: first frame should be LibraryHelper.innerMethod (first library frame from user perspective)
        assertEquals("innerMethod", stack[0].getMethodName(), "Frame 0: should be innerMethod (entry point from user code)");
        assertEquals("org.usefultoys.slf4j.LibraryHelper", stack[0].getClassName(), "Frame 0: should point to LibraryHelper");
        assertEquals(getClass().getName(), stack[1].getClassName(), "Frame 1: should point to test method");
        assertEquals("shouldPointToLibraryHelperInnerMethodWhenCalledDirectly", stack[1].getMethodName(), "Frame 1: should be test method");
    }

    @Test
    @DisplayName("should point to LibraryHelper.middleMethod when called indirectly through nested library calls")
    public void shouldPointToLibraryHelperMiddleMethodWhenCalledIndirectly() {
        // Given: a call to LibraryHelper.middleMethod (which calls innerMethod -> CallerStackTraceThrowable)
        final CallerStackTraceThrowable t = LibraryHelper.middleMethod();
        final StackTraceElement[] stack = t.getStackTrace();

        // When: the stack trace is examined (framesToDiscard stops at LibraryHelper.middleMethod)
        // Then: getApiMethodName() should return middleMethod
        assertEquals("middleMethod", t.getApiMethodName(), "getApiMethodName() should return middleMethod");
        // Then: first frame should be LibraryHelper.middleMethod (library entry point from user perspective)
        assertEquals("org.usefultoys.slf4j.LibraryHelper", stack[0].getClassName(), "Frame 0: should point to LibraryHelper");
        assertEquals("middleMethod", stack[0].getMethodName(), "Frame 0: should be middleMethod (entry point from user code)");
        assertEquals(getClass().getName(), stack[1].getClassName(), "Frame 1: should point to test method");
        assertEquals("shouldPointToLibraryHelperMiddleMethodWhenCalledIndirectly", stack[1].getMethodName(), "Frame 1: should be test method");
    }

    @Test
    @DisplayName("should point to LibraryHelper.outerMethod when called through multiple nested library calls")
    public void shouldPointToLibraryHelperOuterMethodWhenCalledThroughMultipleNestedCalls() {
        // Given: a call to LibraryHelper.outerMethod (which calls middleMethod -> innerMethod -> CallerStackTraceThrowable)
        final CallerStackTraceThrowable t = LibraryHelper.outerMethod();
        final StackTraceElement[] stack = t.getStackTrace();

        // When: the stack trace is examined (framesToDiscard stops at LibraryHelper.outerMethod)
        // Then: getApiMethodName() should return outerMethod
        assertEquals("outerMethod", t.getApiMethodName(), "getApiMethodName() should return outerMethod");
        // Then: first frame should be LibraryHelper.outerMethod (library entry point from user perspective)
        assertEquals("org.usefultoys.slf4j.LibraryHelper", stack[0].getClassName(), "Frame 0: should point to LibraryHelper");
        assertEquals("outerMethod", stack[0].getMethodName(), "Frame 0: should be outerMethod (entry point from user code)");
        assertEquals(getClass().getName(), stack[1].getClassName(), "Frame 1: should point to test method");
        assertEquals("shouldPointToLibraryHelperOuterMethodWhenCalledThroughMultipleNestedCalls", stack[1].getMethodName(), "Frame 1: should be test method");
    }

    @Test
    @DisplayName("should preserve user code frames when called through single user code method")
    public void shouldPreserveUserCodeFramesWhenCalledThroughSingleUserCodeMethod() {
        // Given: a call to user code method innerMethod (which creates CallerStackTraceThrowable)
        final CallerStackTraceThrowable t = innerMethod();
        final StackTraceElement[] stack = t.getStackTrace();

        // When: the stack trace is examined (framesToDiscard == 3, no library frames found)
        // Then: getApiMethodName() should return null since no API method was involved
        assertNull(t.getApiMethodName(), "getApiMethodName() should return null when no API method was called");
        // Then: all user code frames should be preserved starting from innerMethod call
        assertEquals(getClass().getName(), stack[0].getClassName(), "Frame 0: should point to innerMethod");
        assertEquals("innerMethod", stack[0].getMethodName(), "Frame 0: should be innerMethod");
        assertEquals(getClass().getName(), stack[1].getClassName(), "Frame 1: should point to test method");
        assertEquals("shouldPreserveUserCodeFramesWhenCalledThroughSingleUserCodeMethod", stack[1].getMethodName(), "Frame 1: should be test method");
    }

    @Test
    @DisplayName("should preserve all user code frames when called through nested user code methods")
    public void shouldPreserveAllUserCodeFramesWhenCalledThroughNestedUserCodeMethods() {
        // Given: a call to user code method middleMethod (which calls innerMethod -> CallerStackTraceThrowable)
        final CallerStackTraceThrowable t = middleMethod();
        final StackTraceElement[] stack = t.getStackTrace();

        // When: the stack trace is examined (framesToDiscard == 3, no library frames found)
        // Then: getApiMethodName() should return null since no API method was involved
        assertNull(t.getApiMethodName(), "getApiMethodName() should return null when no API method was called");
        // Then: all user code frames should be preserved in call order
        assertEquals(getClass().getName(), stack[0].getClassName(), "Frame 0: should point to innerMethod");
        assertEquals("innerMethod", stack[0].getMethodName(), "Frame 0: should be innerMethod");
        assertEquals(getClass().getName(), stack[1].getClassName(), "Frame 1: should point to middleMethod");
        assertEquals("middleMethod", stack[1].getMethodName(), "Frame 1: should be middleMethod");
        assertEquals(getClass().getName(), stack[2].getClassName(), "Frame 2: should point to test method");
        assertEquals("shouldPreserveAllUserCodeFramesWhenCalledThroughNestedUserCodeMethods", stack[2].getMethodName(), "Frame 2: should be test method");
    }

    @Test
    @DisplayName("should preserve all user code frames when called through multiple nested user code methods")
    public void shouldPreserveAllUserCodeFramesWhenCalledThroughMultipleNestedUserCodeMethods() {
        // Given: a call to user code method outerMethod (which calls middleMethod -> innerMethod -> CallerStackTraceThrowable)
        final CallerStackTraceThrowable t = outerMethod();
        final StackTraceElement[] stack = t.getStackTrace();

        // When: the stack trace is examined (framesToDiscard == 3, no library frames found)
        // Then: getApiMethodName() should return null since no API method was involved
        assertNull(t.getApiMethodName(), "getApiMethodName() should return null when no API method was called");
        // Then: all user code frames should be preserved in call order
        assertEquals(getClass().getName(), stack[0].getClassName(), "Frame 0: should point to innerMethod");
        assertEquals("innerMethod", stack[0].getMethodName(), "Frame 0: should be innerMethod");
        assertEquals(getClass().getName(), stack[1].getClassName(), "Frame 1: should point to middleMethod");
        assertEquals("middleMethod", stack[1].getMethodName(), "Frame 1: should be middleMethod");
        assertEquals(getClass().getName(), stack[2].getClassName(), "Frame 2: should point to outerMethod");
        assertEquals("outerMethod", stack[2].getMethodName(), "Frame 2: should be outerMethod");
        assertEquals(getClass().getName(), stack[3].getClassName(), "Frame 3: should point to test method");
        assertEquals("shouldPreserveAllUserCodeFramesWhenCalledThroughMultipleNestedUserCodeMethods", stack[3].getMethodName(), "Frame 3: should be test method");
    }

    // Tests with message parameter

    @Test
    @DisplayName("should handle message when directly instantiated from test")
    public void shouldHandleMessageWhenDirectlyInstantiatedFromTest() {
        // Given: a direct instantiation of CallerStackTraceThrowable with message
        final String message = "Test error message";
        final CallerStackTraceThrowable t = new CallerStackTraceThrowable(message);
        final StackTraceElement[] stack = t.getStackTrace();

        // Then: message should be preserved and getApiMethodName() should return null
        assertEquals(message, t.getMessage(), "Message should be preserved");
        assertNull(t.getApiMethodName(), "getApiMethodName() should return null when instantiated directly by user code");
        assertEquals(getClass().getName(), stack[0].getClassName(), "Frame 0: should point to test method");
        assertEquals("shouldHandleMessageWhenDirectlyInstantiatedFromTest", stack[0].getMethodName(), "Frame 0: should be test method");
    }

    @Test
    @DisplayName("should handle message when called through LibraryHelper.innerMethod")
    public void shouldHandleMessageWhenCalledThroughLibraryHelperInnerMethod() {
        // Given: a call to LibraryHelper.innerMethodWithMessage
        final String message = "Library API error";
        final CallerStackTraceThrowable t = LibraryHelper.innerMethodWithMessage(message);
        final StackTraceElement[] stack = t.getStackTrace();

        // Then: message should be preserved and getApiMethodName() should return innerMethodWithMessage
        assertEquals(message, t.getMessage(), "Message should be preserved");
        assertEquals("innerMethodWithMessage", t.getApiMethodName(), "getApiMethodName() should return innerMethodWithMessage");
        assertEquals("org.usefultoys.slf4j.LibraryHelper", stack[0].getClassName(), "Frame 0: should point to LibraryHelper");
        assertEquals("innerMethodWithMessage", stack[0].getMethodName(), "Frame 0: should be innerMethodWithMessage");
        assertEquals(getClass().getName(), stack[1].getClassName(), "Frame 1: should point to test method");
    }

    @Test
    @DisplayName("should handle message when called through LibraryHelper.middleMethod")
    public void shouldHandleMessageWhenCalledThroughLibraryHelperMiddleMethod() {
        // Given: a call to LibraryHelper.middleMethodWithMessage
        final String message = "Library middle method error";
        final CallerStackTraceThrowable t = LibraryHelper.middleMethodWithMessage(message);
        final StackTraceElement[] stack = t.getStackTrace();

        // Then: message should be preserved and getApiMethodName() should return middleMethodWithMessage
        assertEquals(message, t.getMessage(), "Message should be preserved");
        assertEquals("middleMethodWithMessage", t.getApiMethodName(), "getApiMethodName() should return middleMethodWithMessage");
        assertEquals("org.usefultoys.slf4j.LibraryHelper", stack[0].getClassName(), "Frame 0: should point to LibraryHelper");
        assertEquals("middleMethodWithMessage", stack[0].getMethodName(), "Frame 0: should be middleMethodWithMessage");
        assertEquals(getClass().getName(), stack[1].getClassName(), "Frame 1: should point to test method");
    }

    @Test
    @DisplayName("should handle message when called through LibraryHelper.outerMethod")
    public void shouldHandleMessageWhenCalledThroughLibraryHelperOuterMethod() {
        // Given: a call to LibraryHelper.outerMethodWithMessage
        final String message = "Library outer method error";
        final CallerStackTraceThrowable t = LibraryHelper.outerMethodWithMessage(message);
        final StackTraceElement[] stack = t.getStackTrace();

        // Then: message should be preserved and getApiMethodName() should return outerMethodWithMessage
        assertEquals(message, t.getMessage(), "Message should be preserved");
        assertEquals("outerMethodWithMessage", t.getApiMethodName(), "getApiMethodName() should return outerMethodWithMessage");
        assertEquals("org.usefultoys.slf4j.LibraryHelper", stack[0].getClassName(), "Frame 0: should point to LibraryHelper");
        assertEquals("outerMethodWithMessage", stack[0].getMethodName(), "Frame 0: should be outerMethodWithMessage");
        assertEquals(getClass().getName(), stack[1].getClassName(), "Frame 1: should point to test method");
    }

    @Test
    @DisplayName("should handle message when called through user code innerMethod")
    public void shouldHandleMessageWhenCalledThroughUserCodeInnerMethod() {
        // Given: a call to user code method innerMethodWithMessage
        final String message = "User code error";
        final CallerStackTraceThrowable t = innerMethodWithMessage(message);
        final StackTraceElement[] stack = t.getStackTrace();

        // Then: message should be preserved and getApiMethodName() should return null
        assertEquals(message, t.getMessage(), "Message should be preserved");
        assertNull(t.getApiMethodName(), "getApiMethodName() should return null when no API method was called");
        assertEquals(getClass().getName(), stack[0].getClassName(), "Frame 0: should point to innerMethodWithMessage");
        assertEquals("innerMethodWithMessage", stack[0].getMethodName(), "Frame 0: should be innerMethodWithMessage");
        assertEquals(getClass().getName(), stack[1].getClassName(), "Frame 1: should point to test method");
    }

    @Test
    @DisplayName("should handle message when called through user code middleMethod")
    public void shouldHandleMessageWhenCalledThroughUserCodeMiddleMethod() {
        // Given: a call to user code method middleMethodWithMessage
        final String message = "Middle method error";
        final CallerStackTraceThrowable t = middleMethodWithMessage(message);
        final StackTraceElement[] stack = t.getStackTrace();

        // Then: message should be preserved and all user code frames should be present
        assertEquals(message, t.getMessage(), "Message should be preserved");
        assertNull(t.getApiMethodName(), "getApiMethodName() should return null when no API method was called");
        assertEquals(getClass().getName(), stack[0].getClassName(), "Frame 0: should point to innerMethodWithMessage");
        assertEquals("innerMethodWithMessage", stack[0].getMethodName(), "Frame 0: should be innerMethodWithMessage");
        assertEquals(getClass().getName(), stack[1].getClassName(), "Frame 1: should point to middleMethodWithMessage");
        assertEquals("middleMethodWithMessage", stack[1].getMethodName(), "Frame 1: should be middleMethodWithMessage");
        assertEquals(getClass().getName(), stack[2].getClassName(), "Frame 2: should point to test method");
    }

    @Test
    @DisplayName("should handle message when called through user code outerMethod")
    public void shouldHandleMessageWhenCalledThroughUserCodeOuterMethod() {
        // Given: a call to user code method outerMethodWithMessage
        final String message = "Outer method error";
        final CallerStackTraceThrowable t = outerMethodWithMessage(message);
        final StackTraceElement[] stack = t.getStackTrace();

        // When: the stack trace and message are examined
        // Then: message should be preserved and all user code frames should be present
        assertEquals(message, t.getMessage(), "Message should be preserved");
        assertNull(t.getApiMethodName(), "getApiMethodName() should return null when no API method was called");
        assertEquals(getClass().getName(), stack[0].getClassName(), "Frame 0: should point to innerMethodWithMessage");
        assertEquals("innerMethodWithMessage", stack[0].getMethodName(), "Frame 0: should be innerMethodWithMessage");
        assertEquals(getClass().getName(), stack[1].getClassName(), "Frame 1: should point to middleMethodWithMessage");
        assertEquals("middleMethodWithMessage", stack[1].getMethodName(), "Frame 1: should be middleMethodWithMessage");
        assertEquals(getClass().getName(), stack[2].getClassName(), "Frame 2: should point to outerMethodWithMessage");
        assertEquals("outerMethodWithMessage", stack[2].getMethodName(), "Frame 2: should be outerMethodWithMessage");
        assertEquals(getClass().getName(), stack[3].getClassName(), "Frame 3: should point to test method");
        assertEquals("shouldHandleMessageWhenCalledThroughUserCodeOuterMethod", stack[3].getMethodName(), "Frame 3: should be test method");
    }

    // Tests with message and cause parameters

    @Test
    @DisplayName("should preserve message and cause when called through LibraryHelper.innerMethod")
    public void shouldPreserveMessageAndCauseWhenCalledThroughLibraryHelperInnerMethod() {
        // Given: a call to LibraryHelper.innerMethodWithMessageAndCause
        final String message = "Library inner error with cause";
        final Exception cause = new RuntimeException("Library inner cause");
        final CallerStackTraceThrowable t = LibraryHelper.innerMethodWithMessageAndCause(message, cause);
        final StackTraceElement[] stack = t.getStackTrace();

        // Then: message and cause should be preserved, getApiMethodName() should return innerMethodWithMessageAndCause
        assertEquals(message, t.getMessage(), "Message should be preserved");
        assertSame(cause, t.getCause(), "Cause should be preserved");
        assertEquals("innerMethodWithMessageAndCause", t.getApiMethodName(), "getApiMethodName() should return innerMethodWithMessageAndCause");
        assertEquals("org.usefultoys.slf4j.LibraryHelper", stack[0].getClassName(), "Frame 0: should point to LibraryHelper");
        assertEquals("innerMethodWithMessageAndCause", stack[0].getMethodName(), "Frame 0: should be innerMethodWithMessageAndCause");
        assertEquals(getClass().getName(), stack[1].getClassName(), "Frame 1: should point to test method");
    }

    @Test
    @DisplayName("should preserve message and cause when called through LibraryHelper.middleMethod")
    public void shouldPreserveMessageAndCauseWhenCalledThroughLibraryHelperMiddleMethod() {
        // Given: a call to LibraryHelper.middleMethodWithMessageAndCause
        final String message = "Library middle error with cause";
        final Exception cause = new RuntimeException("Library middle cause");
        final CallerStackTraceThrowable t = LibraryHelper.middleMethodWithMessageAndCause(message, cause);
        final StackTraceElement[] stack = t.getStackTrace();

        // Then: message and cause should be preserved, getApiMethodName() should return middleMethodWithMessageAndCause
        assertEquals(message, t.getMessage(), "Message should be preserved");
        assertSame(cause, t.getCause(), "Cause should be preserved");
        assertEquals("middleMethodWithMessageAndCause", t.getApiMethodName(), "getApiMethodName() should return middleMethodWithMessageAndCause");
        assertEquals("org.usefultoys.slf4j.LibraryHelper", stack[0].getClassName(), "Frame 0: should point to LibraryHelper");
        assertEquals("middleMethodWithMessageAndCause", stack[0].getMethodName(), "Frame 0: should be middleMethodWithMessageAndCause");
        assertEquals(getClass().getName(), stack[1].getClassName(), "Frame 1: should point to test method");
    }

    @Test
    @DisplayName("should preserve message and cause when called through LibraryHelper.outerMethod")
    public void shouldPreserveMessageAndCauseWhenCalledThroughLibraryHelperOuterMethod() {
        // Given: a call to LibraryHelper.outerMethodWithMessageAndCause
        final String message = "Library outer error with cause";
        final Exception cause = new RuntimeException("Library outer cause");
        final CallerStackTraceThrowable t = LibraryHelper.outerMethodWithMessageAndCause(message, cause);
        final StackTraceElement[] stack = t.getStackTrace();

        // Then: message and cause should be preserved, getApiMethodName() should return outerMethodWithMessageAndCause
        assertEquals(message, t.getMessage(), "Message should be preserved");
        assertSame(cause, t.getCause(), "Cause should be preserved");
        assertEquals("outerMethodWithMessageAndCause", t.getApiMethodName(), "getApiMethodName() should return outerMethodWithMessageAndCause");
        assertEquals("org.usefultoys.slf4j.LibraryHelper", stack[0].getClassName(), "Frame 0: should point to LibraryHelper");
        assertEquals("outerMethodWithMessageAndCause", stack[0].getMethodName(), "Frame 0: should be outerMethodWithMessageAndCause");
        assertEquals(getClass().getName(), stack[1].getClassName(), "Frame 1: should point to test method");
    }

    @Test
    @DisplayName("should preserve message and cause when directly instantiated from test")
    public void shouldPreserveMessageAndCauseWhenDirectlyInstantiatedFromTest() {
        // Given: a direct instantiation of CallerStackTraceThrowable with message and cause
        final String message = "Test error with cause";
        final Exception cause = new RuntimeException("Root cause");
        final CallerStackTraceThrowable t = new CallerStackTraceThrowable(message, cause);
        final StackTraceElement[] stack = t.getStackTrace();

        // Then: message and cause should be preserved
        assertEquals(message, t.getMessage(), "Message should be preserved");
        assertSame(cause, t.getCause(), "Cause should be preserved");
        assertNull(t.getApiMethodName(), "getApiMethodName() should return null when instantiated directly by user code");
        assertEquals(getClass().getName(), stack[0].getClassName(), "Frame 0: should point to test method");
        assertEquals("shouldPreserveMessageAndCauseWhenDirectlyInstantiatedFromTest", stack[0].getMethodName(), "Frame 0: should be test method");
    }

    @Test
    @DisplayName("should preserve message and cause when called through user code innerMethod")
    public void shouldPreserveMessageAndCauseWhenCalledThroughUserCodeInnerMethod() {
        // Given: a call to user code method innerMethodWithMessageAndCause
        final String message = "Inner method error with cause";
        final Exception cause = new RuntimeException("Inner cause");
        final CallerStackTraceThrowable t = innerMethodWithMessageAndCause(message, cause);
        final StackTraceElement[] stack = t.getStackTrace();

        // Then: message and cause should be preserved, and user code frames should be present
        assertEquals(message, t.getMessage(), "Message should be preserved");
        assertSame(cause, t.getCause(), "Cause should be preserved");
        assertNull(t.getApiMethodName(), "getApiMethodName() should return null when no API method was called");
        assertEquals(getClass().getName(), stack[0].getClassName(), "Frame 0: should point to innerMethodWithMessageAndCause");
        assertEquals("innerMethodWithMessageAndCause", stack[0].getMethodName(), "Frame 0: should be innerMethodWithMessageAndCause");
        assertEquals(getClass().getName(), stack[1].getClassName(), "Frame 1: should point to test method");
    }

    @Test
    @DisplayName("should preserve message and cause when called through user code middleMethod")
    public void shouldPreserveMessageAndCauseWhenCalledThroughUserCodeMiddleMethod() {
        // Given: a call to user code method middleMethodWithMessageAndCause
        final String message = "Middle method error with cause";
        final Exception cause = new RuntimeException("Middle cause");
        final CallerStackTraceThrowable t = middleMethodWithMessageAndCause(message, cause);
        final StackTraceElement[] stack = t.getStackTrace();

        // Then: message and cause should be preserved, and all user code frames should be present
        assertEquals(message, t.getMessage(), "Message should be preserved");
        assertSame(cause, t.getCause(), "Cause should be preserved");
        assertNull(t.getApiMethodName(), "getApiMethodName() should return null when no API method was called");
        assertEquals(getClass().getName(), stack[0].getClassName(), "Frame 0: should point to innerMethodWithMessageAndCause");
        assertEquals("innerMethodWithMessageAndCause", stack[0].getMethodName(), "Frame 0: should be innerMethodWithMessageAndCause");
        assertEquals(getClass().getName(), stack[1].getClassName(), "Frame 1: should point to middleMethodWithMessageAndCause");
        assertEquals("middleMethodWithMessageAndCause", stack[1].getMethodName(), "Frame 1: should be middleMethodWithMessageAndCause");
        assertEquals(getClass().getName(), stack[2].getClassName(), "Frame 2: should point to test method");
    }

    @Test
    @DisplayName("should preserve message and cause when called through user code outerMethod")
    public void shouldPreserveMessageAndCauseWhenCalledThroughUserCodeOuterMethod() {
        // Given: a call to user code method outerMethodWithMessageAndCause
        final String message = "Outer method error with cause";
        final Exception cause = new RuntimeException("Outer cause");
        final CallerStackTraceThrowable t = outerMethodWithMessageAndCause(message, cause);
        final StackTraceElement[] stack = t.getStackTrace();

        // Then: message and cause should be preserved, and all user code frames should be present
        assertEquals(message, t.getMessage(), "Message should be preserved");
        assertSame(cause, t.getCause(), "Cause should be preserved");
        assertNull(t.getApiMethodName(), "getApiMethodName() should return null when no API method was called");
        assertEquals(getClass().getName(), stack[0].getClassName(), "Frame 0: should point to innerMethodWithMessageAndCause");
        assertEquals("innerMethodWithMessageAndCause", stack[0].getMethodName(), "Frame 0: should be innerMethodWithMessageAndCause");
        assertEquals(getClass().getName(), stack[1].getClassName(), "Frame 1: should point to middleMethodWithMessageAndCause");
        assertEquals("middleMethodWithMessageAndCause", stack[1].getMethodName(), "Frame 1: should be middleMethodWithMessageAndCause");
        assertEquals(getClass().getName(), stack[2].getClassName(), "Frame 2: should point to outerMethodWithMessageAndCause");
        assertEquals("outerMethodWithMessageAndCause", stack[2].getMethodName(), "Frame 2: should be outerMethodWithMessageAndCause");
        assertEquals(getClass().getName(), stack[3].getClassName(), "Frame 3: should point to test method");
    }

    // Tests with cause parameter only

    @Test
    @DisplayName("should preserve cause when directly instantiated from test")
    public void shouldPreserveCauseWhenDirectlyInstantiatedFromTest() {
        // Given: a direct instantiation of CallerStackTraceThrowable with cause
        final Exception cause = new RuntimeException("Root cause");
        final CallerStackTraceThrowable t = new CallerStackTraceThrowable(cause);
        final StackTraceElement[] stack = t.getStackTrace();

        // When: the stack trace and cause are examined
        // Then: cause should be preserved and getApiMethodName() should return null
        assertSame(cause, t.getCause(), "Cause should be preserved");
        assertNull(t.getApiMethodName(), "getApiMethodName() should return null when instantiated directly by user code");
        assertEquals(getClass().getName(), stack[0].getClassName(), "Frame 0: should point to test method");
        assertEquals("shouldPreserveCauseWhenDirectlyInstantiatedFromTest", stack[0].getMethodName(), "Frame 0: should be test method");
    }

    @Test
    @DisplayName("should preserve cause when called through LibraryHelper.innerMethod")
    public void shouldPreserveCauseWhenCalledThroughLibraryHelperInnerMethod() {
        // Given: a call to LibraryHelper.innerMethodWithCause
        final Exception cause = new RuntimeException("Library inner cause");
        final CallerStackTraceThrowable t = LibraryHelper.innerMethodWithCause(cause);
        final StackTraceElement[] stack = t.getStackTrace();

        // When: the stack trace and cause are examined
        // Then: cause should be preserved and getApiMethodName() should return innerMethodWithCause
        assertSame(cause, t.getCause(), "Cause should be preserved");
        assertEquals("innerMethodWithCause", t.getApiMethodName(), "getApiMethodName() should return innerMethodWithCause");
        assertEquals("org.usefultoys.slf4j.LibraryHelper", stack[0].getClassName(), "Frame 0: should point to LibraryHelper");
        assertEquals("innerMethodWithCause", stack[0].getMethodName(), "Frame 0: should be innerMethodWithCause");
        assertEquals(getClass().getName(), stack[1].getClassName(), "Frame 1: should point to test method");
        assertEquals("shouldPreserveCauseWhenCalledThroughLibraryHelperInnerMethod", stack[1].getMethodName(), "Frame 1: should be test method");
    }

    @Test
    @DisplayName("should preserve cause when called through LibraryHelper.middleMethod")
    public void shouldPreserveCauseWhenCalledThroughLibraryHelperMiddleMethod() {
        // Given: a call to LibraryHelper.middleMethodWithCause
        final Exception cause = new RuntimeException("Library middle cause");
        final CallerStackTraceThrowable t = LibraryHelper.middleMethodWithCause(cause);
        final StackTraceElement[] stack = t.getStackTrace();

        // When: the stack trace and cause are examined
        // Then: cause should be preserved and getApiMethodName() should return middleMethodWithCause
        assertSame(cause, t.getCause(), "Cause should be preserved");
        assertEquals("middleMethodWithCause", t.getApiMethodName(), "getApiMethodName() should return middleMethodWithCause");
        assertEquals("org.usefultoys.slf4j.LibraryHelper", stack[0].getClassName(), "Frame 0: should point to LibraryHelper");
        assertEquals("middleMethodWithCause", stack[0].getMethodName(), "Frame 0: should be middleMethodWithCause");
        assertEquals(getClass().getName(), stack[1].getClassName(), "Frame 1: should point to test method");
        assertEquals("shouldPreserveCauseWhenCalledThroughLibraryHelperMiddleMethod", stack[1].getMethodName(), "Frame 1: should be test method");
    }

    @Test
    @DisplayName("should preserve cause when called through LibraryHelper.outerMethod")
    public void shouldPreserveCauseWhenCalledThroughLibraryHelperOuterMethod() {
        // Given: a call to LibraryHelper.outerMethodWithCause
        final Exception cause = new RuntimeException("Library outer cause");
        final CallerStackTraceThrowable t = LibraryHelper.outerMethodWithCause(cause);
        final StackTraceElement[] stack = t.getStackTrace();

        // When: the stack trace and cause are examined
        // Then: cause should be preserved and getApiMethodName() should return outerMethodWithCause
        assertSame(cause, t.getCause(), "Cause should be preserved");
        assertEquals("outerMethodWithCause", t.getApiMethodName(), "getApiMethodName() should return outerMethodWithCause");
        assertEquals("org.usefultoys.slf4j.LibraryHelper", stack[0].getClassName(), "Frame 0: should point to LibraryHelper");
        assertEquals("outerMethodWithCause", stack[0].getMethodName(), "Frame 0: should be outerMethodWithCause");
        assertEquals(getClass().getName(), stack[1].getClassName(), "Frame 1: should point to test method");
        assertEquals("shouldPreserveCauseWhenCalledThroughLibraryHelperOuterMethod", stack[1].getMethodName(), "Frame 1: should be test method");
    }

    @Test
    @DisplayName("should preserve cause when called through user code innerMethod")
    public void shouldPreserveCauseWhenCalledThroughUserCodeInnerMethod() {
        // Given: a call to user code method innerMethodWithCause
        final Exception cause = new RuntimeException("User inner cause");
        final CallerStackTraceThrowable t = innerMethodWithCause(cause);
        final StackTraceElement[] stack = t.getStackTrace();

        // When: the stack trace and cause are examined
        // Then: cause should be preserved and getApiMethodName() should return null
        assertSame(cause, t.getCause(), "Cause should be preserved");
        assertNull(t.getApiMethodName(), "getApiMethodName() should return null when no API method was called");
        assertEquals(getClass().getName(), stack[0].getClassName(), "Frame 0: should point to innerMethodWithCause");
        assertEquals("innerMethodWithCause", stack[0].getMethodName(), "Frame 0: should be innerMethodWithCause");
        assertEquals(getClass().getName(), stack[1].getClassName(), "Frame 1: should point to test method");
        assertEquals("shouldPreserveCauseWhenCalledThroughUserCodeInnerMethod", stack[1].getMethodName(), "Frame 1: should be test method");
    }

    @Test
    @DisplayName("should preserve cause when called through user code middleMethod")
    public void shouldPreserveCauseWhenCalledThroughUserCodeMiddleMethod() {
        // Given: a call to user code method middleMethodWithCause
        final Exception cause = new RuntimeException("User middle cause");
        final CallerStackTraceThrowable t = middleMethodWithCause(cause);
        final StackTraceElement[] stack = t.getStackTrace();

        // When: the stack trace and cause are examined
        // Then: cause should be preserved and all user code frames should be present
        assertSame(cause, t.getCause(), "Cause should be preserved");
        assertNull(t.getApiMethodName(), "getApiMethodName() should return null when no API method was called");
        assertEquals(getClass().getName(), stack[0].getClassName(), "Frame 0: should point to innerMethodWithCause");
        assertEquals("innerMethodWithCause", stack[0].getMethodName(), "Frame 0: should be innerMethodWithCause");
        assertEquals(getClass().getName(), stack[1].getClassName(), "Frame 1: should point to middleMethodWithCause");
        assertEquals("middleMethodWithCause", stack[1].getMethodName(), "Frame 1: should be middleMethodWithCause");
        assertEquals(getClass().getName(), stack[2].getClassName(), "Frame 2: should point to test method");
        assertEquals("shouldPreserveCauseWhenCalledThroughUserCodeMiddleMethod", stack[2].getMethodName(), "Frame 2: should be test method");
    }

    @Test
    @DisplayName("should preserve cause when called through user code outerMethod")
    public void shouldPreserveCauseWhenCalledThroughUserCodeOuterMethod() {
        // Given: a call to user code method outerMethodWithCause
        final Exception cause = new RuntimeException("User outer cause");
        final CallerStackTraceThrowable t = outerMethodWithCause(cause);
        final StackTraceElement[] stack = t.getStackTrace();

        // When: the stack trace and cause are examined
        // Then: cause should be preserved and all user code frames should be present
        assertSame(cause, t.getCause(), "Cause should be preserved");
        assertNull(t.getApiMethodName(), "getApiMethodName() should return null when no API method was called");
        assertEquals(getClass().getName(), stack[0].getClassName(), "Frame 0: should point to innerMethodWithCause");
        assertEquals("innerMethodWithCause", stack[0].getMethodName(), "Frame 0: should be innerMethodWithCause");
        assertEquals(getClass().getName(), stack[1].getClassName(), "Frame 1: should point to middleMethodWithCause");
        assertEquals("middleMethodWithCause", stack[1].getMethodName(), "Frame 1: should be middleMethodWithCause");
        assertEquals(getClass().getName(), stack[2].getClassName(), "Frame 2: should point to outerMethodWithCause");
        assertEquals("outerMethodWithCause", stack[2].getMethodName(), "Frame 2: should be outerMethodWithCause");
        assertEquals(getClass().getName(), stack[3].getClassName(), "Frame 3: should point to test method");
        assertEquals("shouldPreserveCauseWhenCalledThroughUserCodeOuterMethod", stack[3].getMethodName(), "Frame 3: should be test method");
    }

    private static CallerStackTraceThrowable outerMethod() {
        return middleMethod();
    }

    private static CallerStackTraceThrowable middleMethod() {
        return innerMethod();
    }

    private static CallerStackTraceThrowable innerMethod() {
        return new CallerStackTraceThrowable();
    }

    private static CallerStackTraceThrowable outerMethodWithCause(final Exception cause) {
        return middleMethodWithCause(cause);
    }

    private static CallerStackTraceThrowable middleMethodWithCause(final Exception cause) {
        return innerMethodWithCause(cause);
    }

    private static CallerStackTraceThrowable innerMethodWithCause(final Exception cause) {
        return new CallerStackTraceThrowable(cause);
    }

    private static CallerStackTraceThrowable innerMethodWithMessage(final String message) {
        return new CallerStackTraceThrowable(message);
    }

    private static CallerStackTraceThrowable middleMethodWithMessage(final String message) {
        return innerMethodWithMessage(message);
    }

    private static CallerStackTraceThrowable outerMethodWithMessage(final String message) {
        return middleMethodWithMessage(message);
    }

    private static CallerStackTraceThrowable innerMethodWithMessageAndCause(final String message, final Exception cause) {
        return new CallerStackTraceThrowable(message, cause);
    }

    private static CallerStackTraceThrowable middleMethodWithMessageAndCause(final String message, final Exception cause) {
        return innerMethodWithMessageAndCause(message, cause);
    }

    private static CallerStackTraceThrowable outerMethodWithMessageAndCause(final String message, final Exception cause) {
        return middleMethodWithMessageAndCause(message, cause);
    }

    // ========================================
    // Edge Case Tests for getApiMethodName()
    // ========================================

    @Test
    @DisplayName("getApiMethodName should return null when stack trace is empty")
    public void shouldReturnNullWhenStackTraceIsEmpty() {
        // Given: a throwable with explicitly set empty stack trace
        final CallerStackTraceThrowable t = new CallerStackTraceThrowable();
        t.setStackTrace(CallerStackTraceThrowable.EMPTY_STACK_TRACE);

        // When: getApiMethodName() is called
        final String apiMethodName = t.getApiMethodName();

        // Then: should return null
        assertNull(apiMethodName, "getApiMethodName() should return null when stack trace is empty");
        assertEquals(0, t.getStackTrace().length, "Stack trace should be empty");
    }

    @Test
    @DisplayName("getApiMethodName should return null when first frame is not library code")
    public void shouldReturnNullWhenFirstFrameIsNotLibraryCode() {
        // Given: a throwable with user code as first frame
        final CallerStackTraceThrowable t = new CallerStackTraceThrowable();
        t.setStackTrace(new StackTraceElement[] {
            new StackTraceElement("com.example.UserClass", "userMethod", "UserClass.java", 10),
            new StackTraceElement("com.example.AnotherClass", "anotherMethod", "AnotherClass.java", 20)
        });

        // When: getApiMethodName() is called
        final String apiMethodName = t.getApiMethodName();

        // Then: should return null since first frame is not from library package
        assertNull(apiMethodName, "getApiMethodName() should return null when first frame is not library code");
        assertEquals(2, t.getStackTrace().length, "Stack trace should have 2 frames");
    }

    @Test
    @DisplayName("getApiMethodName should return method name when first frame is library code")
    public void shouldReturnMethodNameWhenFirstFrameIsLibraryCode() {
        // Given: a throwable with library code as first frame
        final CallerStackTraceThrowable t = new CallerStackTraceThrowable();
        t.setStackTrace(new StackTraceElement[] {
            new StackTraceElement("org.usefultoys.slf4j.meter.Meter", "start", "Meter.java", 42),
            new StackTraceElement("com.example.UserClass", "userMethod", "UserClass.java", 10)
        });

        // When: getApiMethodName() is called
        final String apiMethodName = t.getApiMethodName();

        // Then: should return the library method name
        assertEquals("start", apiMethodName, "getApiMethodName() should return 'start'");
        assertEquals(2, t.getStackTrace().length, "Stack trace should have 2 frames");
    }

    @Test
    @DisplayName("getApiMethodName should handle stack trace with single library frame")
    public void shouldHandleStackTraceWithSingleLibraryFrame() {
        // Given: a throwable with exactly one frame from library code
        final CallerStackTraceThrowable t = new CallerStackTraceThrowable();
        t.setStackTrace(new StackTraceElement[] {
            new StackTraceElement("org.usefultoys.slf4j.LoggerFactory", "getLogger", "LoggerFactory.java", 100)
        });

        // When: getApiMethodName() is called
        final String apiMethodName = t.getApiMethodName();

        // Then: should return the library method name
        assertEquals("getLogger", apiMethodName, "getApiMethodName() should return 'getLogger'");
        assertEquals(1, t.getStackTrace().length, "Stack trace should have 1 frame");
    }

    @Test
    @DisplayName("getApiMethodName should handle stack trace with single non-library frame")
    public void shouldHandleStackTraceWithSingleNonLibraryFrame() {
        // Given: a throwable with exactly one frame NOT from library code
        final CallerStackTraceThrowable t = new CallerStackTraceThrowable();
        t.setStackTrace(new StackTraceElement[] {
            new StackTraceElement("com.example.UserClass", "userMethod", "UserClass.java", 10)
        });

        // When: getApiMethodName() is called
        final String apiMethodName = t.getApiMethodName();

        // Then: should return null
        assertNull(apiMethodName, "getApiMethodName() should return null when single frame is not library code");
        assertEquals(1, t.getStackTrace().length, "Stack trace should have 1 frame");
    }

    @Test
    @DisplayName("getApiMethodName should work with different library classes")
    public void shouldWorkWithDifferentLibraryClasses() {
        // Given: throwables with different library classes as first frame
        final CallerStackTraceThrowable t1 = new CallerStackTraceThrowable();
        t1.setStackTrace(new StackTraceElement[] {
            new StackTraceElement("org.usefultoys.slf4j.watcher.Watcher", "watch", "Watcher.java", 50)
        });

        final CallerStackTraceThrowable t2 = new CallerStackTraceThrowable();
        t2.setStackTrace(new StackTraceElement[] {
            new StackTraceElement("org.usefultoys.slf4j.reporter.Reporter", "report", "Reporter.java", 30)
        });

        final CallerStackTraceThrowable t3 = new CallerStackTraceThrowable();
        t3.setStackTrace(new StackTraceElement[] {
            new StackTraceElement("org.usefultoys.slf4j.CallerStackTraceThrowable", "getApiMethodName", "CallerStackTraceThrowable.java", 85)
        });

        // When: getApiMethodName() is called on each
        final String apiMethodName1 = t1.getApiMethodName();
        final String apiMethodName2 = t2.getApiMethodName();
        final String apiMethodName3 = t3.getApiMethodName();

        // Then: each should return the correct method name
        assertEquals("watch", apiMethodName1, "Should return 'watch' for Watcher class");
        assertEquals("report", apiMethodName2, "Should return 'report' for Reporter class");
        assertEquals("getApiMethodName", apiMethodName3, "Should return 'getApiMethodName' for CallerStackTraceThrowable class");
    }

    @Test
    @DisplayName("getApiMethodName should handle edge case of package boundary")
    public void shouldHandlePackageBoundaryEdgeCase() {
        // Given: a throwable with class name that starts with library package but is not actually in it
        final CallerStackTraceThrowable t = new CallerStackTraceThrowable();
        t.setStackTrace(new StackTraceElement[] {
            new StackTraceElement("org.usefultoys.slf4j.extended.CustomClass", "customMethod", "CustomClass.java", 15)
        });

        // When: getApiMethodName() is called
        final String apiMethodName = t.getApiMethodName();

        // Then: should return the method name since it starts with PACKAGE_NAME
        assertEquals("customMethod", apiMethodName, "getApiMethodName() should return 'customMethod' for subpackage class");
    }

    @Test
    @DisplayName("getApiMethodName should handle null message and cause with custom stack trace")
    public void shouldHandleNullMessageAndCauseWithCustomStackTrace() {
        // Given: a throwable with null message and cause, but custom stack trace
        final CallerStackTraceThrowable t = new CallerStackTraceThrowable((String) null);
        t.setStackTrace(new StackTraceElement[] {
            new StackTraceElement("org.usefultoys.slf4j.meter.Meter", "ok", "Meter.java", 200)
        });

        // When: getApiMethodName() is called
        final String apiMethodName = t.getApiMethodName();

        // Then: should return the method name regardless of null message
        assertEquals("ok", apiMethodName, "getApiMethodName() should work with null message");
        assertNull(t.getMessage(), "Message should be null");
        assertNull(t.getCause(), "Cause should be null");
    }

    @Test
    @DisplayName("getApiMethodName should work correctly after fillInStackTrace overwrites custom stack trace")
    public void shouldWorkCorrectlyAfterFillInStackTraceOverwritesCustomStackTrace() {
        // Given: a throwable with custom stack trace
        final CallerStackTraceThrowable t = new CallerStackTraceThrowable();
        t.setStackTrace(new StackTraceElement[] {
            new StackTraceElement("org.usefultoys.slf4j.meter.Meter", "reject", "Meter.java", 150)
        });

        // When: the custom stack trace is verified, then fillInStackTrace() is called
        final String apiMethodNameBefore = t.getApiMethodName();
        assertEquals("reject", apiMethodNameBefore, "Initial getApiMethodName() should return 'reject' from custom stack trace");

        // When: fillInStackTrace() is called (recalculates stack trace from current thread context)
        t.fillInStackTrace();
        final String apiMethodNameAfter1 = t.getApiMethodName();

        // Then: getApiMethodName() should return null since test method is not library code
        assertNull(apiMethodNameAfter1, "getApiMethodName() should return null after fillInStackTrace() recalculates from test context");

        // When: fillInStackTrace() is called again
        t.fillInStackTrace();
        final String apiMethodNameAfter2 = t.getApiMethodName();

        // Then: should still return null (consistent behavior)
        assertNull(apiMethodNameAfter2, "getApiMethodName() should remain null after second fillInStackTrace()");
        assertEquals(apiMethodNameAfter1, apiMethodNameAfter2, "Multiple fillInStackTrace() calls should produce consistent results");
    }
}
