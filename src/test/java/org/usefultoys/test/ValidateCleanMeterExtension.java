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

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.usefultoys.slf4j.meter.Meter;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * JUnit 5 extension that validates and ensures the Meter thread-local stack is clean.
 * <p>
 * This extension ensures that {@link Meter#getCurrentInstance()} returns the "unknown" Meter
 * (with category equal to {@code "???"}), before each test method. After each test, the behavior
 * depends on whether the test passed or failed, and the {@code expectDirtyStack} parameter.
 * <p>
 * <b>Before test:</b> Ensures the stack is clean by removing any leftover Meter instances.
 * This prevents cascade failures from previous tests and provides a clean slate.
 * <p>
 * <b>After test:</b>
 * <ul>
 *   <li><b>If the test failed:</b> Cleans the Meter stack by closing any active Meter instances,
 *       preventing cascade failures in subsequent tests. No validation is performed.</li>
 *   <li><b>If the test passed:</b>
 *     <ul>
 *       <li><b>If {@code expectDirtyStack = false} (default):</b> Validates that the stack is clean.
 *           If a Meter is still active, the test fails with a descriptive message.</li>
 *       <li><b>If {@code expectDirtyStack = true}:</b> Validates that the stack is dirty (contains
 *           a non-unknown Meter). If the stack is clean, the test fails with a descriptive message.
 *           After successful validation, cleans the Meter stack.</li>
 *     </ul>
 *   </li>
 * </ul>
 * <p>
 * The annotation can be applied at both class and method level. Method-level annotations take precedence
 * over class-level settings for determining the {@code expectDirtyStack} behavior.
 * <p>
 * This approach provides both resilience (removes leftover state) and visibility
 * (fails if current test leaves the stack dirty when it shouldn't).
 * <p>
 * <b>Determinism under garbage collection:</b> the Meter thread-local stack holds each Meter through
 * a {@link java.lang.ref.WeakReference} (so that a forgotten, never-stopped Meter can be reclaimed in
 * a server/Java EE environment without leaking memory). For a test annotated with
 * {@code expectDirtyStack = true}, the started Meter is typically only reachable through a local
 * variable that dies when the test body returns. JUnit's allocation-heavy post-test machinery could
 * then trigger a GC that clears the WeakReference before {@code afterEach} inspects the stack, making
 * the "dirty stack" assertion intermittently fail. To prevent this, this extension also implements
 * {@link InvocationInterceptor}: immediately after the test body returns — before any allocation that
 * could trigger a GC — it pins the current top of the stack in a strong reference held by the per-test
 * {@link ExtensionContext.Store}. That strong reference keeps the WeakReference valid, so the
 * {@code afterEach} validation observes the real Meter deterministically. This is a test-only
 * mechanism; the production WeakReference behavior is unchanged.
 * <p>
 * <b>Usage:</b>
 * <pre>{@code
 * @ExtendWith(ValidateCleanMeterExtension.class)
 * class MyMeterTest {
 *     @Test
 *     void testMeterOperation() {
 *         // Meter stack validation runs before and after test
 *     }
 * }
 * }</pre>
 *
 * @see ValidateCleanMeter
 * @see Meter#getCurrentInstance()
 * @see Meter#UNKNOWN_LOGGER_NAME
 * @author Daniel Felix Ferber
 */
public class ValidateCleanMeterExtension implements BeforeEachCallback, AfterEachCallback, InvocationInterceptor {

    /** Namespace for storing the per-test strong reference that pins the Meter stack against GC. */
    private static final Namespace NAMESPACE = Namespace.create(ValidateCleanMeterExtension.class);
    /** Store key for the pinned top-of-stack Meter (kept strongly reachable through afterEach). */
    private static final String PINNED_METER_KEY = "pinnedMeter";

    /**
     * Intercepts a plain {@code @Test} method to pin the Meter stack immediately after the test body
     * returns. See the class-level Javadoc for the rationale.
     *
     * @param invocation        the test method invocation to proceed
     * @param invocationContext reflective information about the invocation (unused)
     * @param extensionContext  the current extension context
     * @throws Throwable if the underlying test invocation throws
     */
    @Override
    public void interceptTestMethod(final Invocation<Void> invocation,
            final ReflectiveInvocationContext<Method> invocationContext,
            final ExtensionContext extensionContext) throws Throwable {
        try {
            invocation.proceed();
        } finally {
            pinCurrentMeterStack(extensionContext);
        }
    }

    /**
     * Intercepts template-based tests ({@code @ParameterizedTest}, {@code @RepeatedTest}) to pin the
     * Meter stack immediately after the test body returns, for the same reason as plain tests.
     *
     * @param invocation        the test method invocation to proceed
     * @param invocationContext reflective information about the invocation (unused)
     * @param extensionContext  the current extension context
     * @throws Throwable if the underlying test invocation throws
     */
    @Override
    public void interceptTestTemplateMethod(final Invocation<Void> invocation,
            final ReflectiveInvocationContext<Method> invocationContext,
            final ExtensionContext extensionContext) throws Throwable {
        try {
            invocation.proceed();
        } finally {
            pinCurrentMeterStack(extensionContext);
        }
    }

    /**
     * Pins the current top of the Meter thread-local stack in a strong reference held by the per-test
     * {@link ExtensionContext.Store}.
     * <p>
     * This runs right after the test body returns and before JUnit's allocation-heavy post-test
     * machinery. {@link Meter#getCurrentInstance()} performs no allocation when the stack is dirty,
     * and there is no allocation between the test body returning and this read, so no garbage
     * collection can clear the underlying {@code WeakReference} before the real Meter is captured.
     * Holding the strong reference also keeps that {@code WeakReference} valid through {@code afterEach},
     * which makes the dirty/clean stack validation deterministic. Pinning the dummy "unknown" Meter
     * (when the stack is clean) is harmless.
     *
     * @param context the current extension context whose store retains the strong reference
     */
    private static void pinCurrentMeterStack(final ExtensionContext context) {
        context.getStore(NAMESPACE).put(PINNED_METER_KEY, Meter.getCurrentInstance());
    }

    /**
     * Ensures that the Meter stack is clean before the test starts.
     * <p>
     * If any Meter instance with a non-unknown category is found on the thread-local stack
     * (left over from a previous test), it is automatically closed to clean up the stack.
     * This prevents cascade failures and provides a clean slate for the test.
     * <p>
     * Multiple Meters may be stacked, so this method closes them repeatedly until the
     * stack contains only the unknown Meter.
     *
     * @param context the current extension context
     */
    @Override
    public void beforeEach(final ExtensionContext context) {
        ensureMeterStackIsClean(context);
    }

    /**
     * Handles Meter stack cleanup after the test completes.
     * <p>
     * Behavior depends on whether the test passed or failed, and the {@code expectDirtyStack} parameter:
     * <ul>
     *   <li><b>Test failed:</b> Cleans the Meter stack (same as {@code beforeEach}) to prevent
     *       cascade failures in subsequent tests. No validation is performed.</li>
     *   <li><b>Test passed:</b>
     *     <ul>
     *       <li><b>If {@code expectDirtyStack = false} (default):</b> Validates that the Meter stack is clean.
     *           If a non-unknown Meter is found, fails the test with a descriptive message.</li>
     *       <li><b>If {@code expectDirtyStack = true}:</b> Validates that the Meter stack is dirty
     *           (contains a non-unknown Meter). If the stack is clean, fails the test with a descriptive message.
     *           After successful validation, cleans the Meter stack.</li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * @param context the current extension context
     * @throws AssertionError if the test passed and the stack state doesn't match expectations
     */
    @Override
    public void afterEach(final ExtensionContext context) {
        // Check if the test failed by looking at the execution exception
        final boolean testFailed = context.getExecutionException().isPresent();
        
        if (testFailed) {
            // Test failed: clean the stack to prevent cascade failures
            ensureMeterStackIsClean(context);
        } else {
            // Test passed: check if dirty stack is expected
            final boolean expectDirtyStack = getExpectDirtyStack(context);
            if (expectDirtyStack) {
                // Dirty stack is expected - validate it's dirty, then clean
                validateMeterStackIsDirty("after", context);
                ensureMeterStackIsClean(context);
            } else {
                // Dirty stack is NOT expected - validate and fail if not clean
                validateMeterStackIsClean("after", context);
            }
        }
    }

    /**
     * Retrieves the {@code expectDirtyStack} parameter from the ValidateCleanMeter annotation.
     * <p>
     * Checks method-level annotation first, then falls back to class-level annotation.
     * Method-level annotations take precedence over class-level settings.
     *
     * @param context the test execution context
     * @return {@code true} if dirty stack is expected, {@code false} otherwise (default)
     */
    private boolean getExpectDirtyStack(final ExtensionContext context) {
        // Check method-level annotation first
        final Optional<ValidateCleanMeter> methodAnnotation = context.getTestMethod()
                .flatMap(method -> Optional.ofNullable(method.getAnnotation(ValidateCleanMeter.class)));
        if (methodAnnotation.isPresent()) {
            return methodAnnotation.get().expectDirtyStack();
        }

        // Fall back to class-level annotation
        final Optional<ValidateCleanMeter> classAnnotation = context.getTestClass()
                .flatMap(clazz -> Optional.ofNullable(clazz.getAnnotation(ValidateCleanMeter.class)));
        if (classAnnotation.isPresent()) {
            return classAnnotation.get().expectDirtyStack();
        }

        // Default: dirty stack is NOT expected
        return false;
    }

    /**
     * Ensures that the Meter stack is clean by closing any active Meter instances.
     * <p>
     * If any Meter with a non-unknown category is found on the thread-local stack,
     * it is closed. This method repeatedly closes Meters until only the unknown Meter
     * remains on the stack.
     */
    private void ensureMeterStackIsClean(final ExtensionContext context) {
        Meter currentMeter = Meter.getCurrentInstance();
        while (!Meter.UNKNOWN_LOGGER_NAME.equals(currentMeter.getCategory())) {
            currentMeter.close();
            currentMeter = Meter.getCurrentInstance();
        }
    }

    /**
     * Validates that the current Meter instance has the unknown category.
     * <p>
     * If validation fails, fails the test with a clear message indicating:
     * - When the contamination was detected (after the test)
     * - What Meter category was found
     * - What was expected
     * - A suggestion to use try-with-resources or call meter.close()
     *
     * @param timing a descriptive string indicating when the validation occurred ("after")
     * @param context the JUnit extension context
     * @throws AssertionError if the current Meter's category is not the unknown logger name
     */
    private void validateMeterStackIsClean(final String timing, final ExtensionContext context) {
        final Meter currentMeter = Meter.getCurrentInstance();
        if (!Meter.UNKNOWN_LOGGER_NAME.equals(currentMeter.getCategory())) {
            final String testName = context.getDisplayName();
            final String errorMessage =
                    "Meter stack must be clean " + timing + " test '" + testName + "': " +
                    "found active Meter  '" + currentMeter.getFullID() + "'";
            ensureMeterStackIsClean(context);        
            throw new AssertionError(errorMessage);
        }
    }

    /**
     * Validates that the current Meter instance is NOT the unknown Meter (i.e., stack is dirty).
     * <p>
     * This validation is used when {@code expectDirtyStack = true} to ensure that the test
     * actually left a Meter on the stack as expected. If validation fails, the test fails
     * with a clear message indicating that the stack was unexpectedly clean.
     *
     * @param timing a descriptive string indicating when the validation occurred ("after")
     * @param context the JUnit extension context
     * @throws AssertionError if the current Meter's category is the unknown logger name (stack is clean)
     */
    private void validateMeterStackIsDirty(final String timing, final ExtensionContext context) {
        final Meter currentMeter = Meter.getCurrentInstance();
        if (Meter.UNKNOWN_LOGGER_NAME.equals(currentMeter.getCategory())) {
            final String testName = context.getDisplayName();
            final String errorMessage =
                    "Meter stack was expected to be dirty " + timing + " test '" + testName + "', " +
                    "but found clean stack (unknown Meter). " +
                    "Test is annotated with @ValidateCleanMeter(expectDirtyStack = true), " +
                    "but did not leave any Meter on the stack.";
            throw new AssertionError(errorMessage);
        }
    }
}
