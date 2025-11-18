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

import lombok.SneakyThrows;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;

import org.slf4j.Logger;

/**
 * Interface for executing functional tasks within a Meter's lifecycle control.
 * Provides default implementations for running {@link Runnable} and {@link Callable} tasks
 * with automatic lifecycle management (start, ok, reject, fail).
 * 
 * <p>This interface separates the execution concerns from the core Meter functionality,
 * allowing for better testability and composition.</p>
 * 
 * @author Daniel Felix Ferber
 */
public interface MeterExecutor<T> extends MeterAnalysis {

    /** Context key for storing the result of functional interface calls. */
    String CONTEXT_RESULT = "result";
    /** Error message for when Meter cannot create an exception of a specific type. */
    String ERROR_MSG_METER_CANNOT_CREATE_EXCEPTION = "Meter cannot create exception of type {}.";

    /**
     * Starts the meter.
     * Implementation should handle the case where meter is already started.
     * 
     * @return Reference to this instance, for method chaining.
     */
    T start();

    /**
     * Marks the operation as successful.
     * Implementation should handle the case where meter is already stopped.
     * 
     * @return Reference to this instance, for method chaining.
     */
    T ok();

    /**
     * Marks the operation as failed with the given cause.
     * This method should be implemented to handle the meter's failure lifecycle.
     * 
     * @param cause The cause of the failure
     * @return Reference to this instance, for method chaining.
     */
    T fail(Object cause);

    /**
     * Marks the operation as rejected with the given cause.
     * This method should be implemented to handle the meter's rejection lifecycle.
     * 
     * @param cause The cause of the rejection
     * @return Reference to this instance, for method chaining.
     */
    T reject(Object cause);

    /**
     * Adds a key-value pair to the meter's context.
     * This method should delegate to the underlying context implementation.
     * 
     * @param key The context key
     * @param value The context value
     * @return Reference to this instance, for method chaining.
     */
    void putContext(String key, Object value);

    Logger getMessageLogger();
    Logger getDataLogger();

    /**
     * Executes the given {@link Runnable} task within the Meter's lifecycle control. 
     * The operation is automatically started before execution and marked as {@code OK} 
     * upon successful completion. If the task throws a {@link RuntimeException}, 
     * the operation is marked as {@code FAIL}, and the exception is rethrown.
     *
     * @param runnable The {@link Runnable} task to be executed.
     */
    default void run(final Runnable runnable) {
        if (! isStarted()) start();
        try {
            runnable.run();
            if (! isStopped()) ok();
        } catch (final RuntimeException e) {
            fail(e);
            throw e;
        }
    }

    /**
     * Executes the given {@link Runnable} task within the Meter's lifecycle control. 
     * The operation is automatically started before execution and marked as {@code OK} 
     * upon successful completion. If the task throws an exception that matches one of 
     * {@code exceptionsToReject}, the operation is marked as {@code REJECT}. Otherwise, 
     * it's marked as {@code FAIL}. The exception is always rethrown.
     *
     * @param runnable           The {@link Runnable} task to be executed.
     * @param exceptionsToReject A list of exception classes that should result in a {@code REJECT} status.
     * @throws Exception The original exception thrown by the runnable.
     */
    @SuppressWarnings("unchecked")
    @SneakyThrows
    default void runOrReject(final Runnable runnable, final Class<? extends Exception>... exceptionsToReject) {
        if (! isStarted()) start();
        try {
            runnable.run();
            if (! isStopped()) ok();
        } catch (final Exception e) {
            for (final Class<? extends Exception> exceptionClass : exceptionsToReject) {
                if (exceptionClass.isAssignableFrom(e.getClass())) {
                    reject(e);
                    throw e;
                }
            }
            fail(e);
            throw e;
        }
    }

    /**
     * Executes the given {@link Callable} task within the Meter's lifecycle control. 
     * The operation is automatically started before execution and marked as {@code OK} 
     * upon successful completion. The result of the {@link Callable} is stored in the 
     * context under {@link #CONTEXT_RESULT} and returned. If the task throws an 
     * {@link Exception}, the operation is marked as {@code FAIL}, and the exception is rethrown.
     *
     * @param callable The {@link Callable} task to be executed.
     * @param <T>      The type of the result returned by the callable.
     * @return The result of the callable task.
     * @throws Exception The original exception thrown by the callable.
     */
    default <T> T call(final Callable<T> callable) throws Exception {
        if (! isStarted()) start();
        try {
            final T result = callable.call();
            putContext(CONTEXT_RESULT, result);
            if (! isStopped()) ok();
            return result;
        } catch (final Exception e) {
            fail(e);
            throw e;
        }
    }

    /**
     * Executes the given {@link Callable} task within the Meter's lifecycle control. 
     * The operation is automatically started before execution and marked as {@code OK} 
     * upon successful completion. The result of the {@link Callable} is stored in the 
     * context under {@code "result"} and returned. If the task throws a {@link RuntimeException}, 
     * the operation is marked as {@code FAIL}. If it throws any other {@link Exception}, 
     * the operation is marked as {@code REJECT}. The exception is always rethrown.
     *
     * @param callable The {@link Callable} task to be executed.
     * @param <T>      The type of the result returned by the callable.
     * @return The result of the callable task.
     * @throws Exception The original exception thrown by the callable.
     */
    @SneakyThrows
    default <T> T callOrRejectChecked(final Callable<T> callable) {
        if (! isStarted()) start();
        try {
            final T result = callable.call();
            putContext(CONTEXT_RESULT, result);
            if (! isStopped()) ok();
            return result;
        } catch (final RuntimeException e) {
            fail(e);
            throw e;
        } catch (final Exception e) {
            reject(e);
            throw e;
        }
    }

    /**
     * Executes the given {@link Callable} task within the Meter's lifecycle control. 
     * The operation is automatically started before execution and marked as {@code OK} 
     * upon successful completion. The result of the {@link Callable} is stored in the 
     * context under {@code "result"} and returned. If the task throws an exception that 
     * matches one of {@code exceptionsToReject}, the operation is marked as {@code REJECT}. 
     * Otherwise, it's marked as {@code FAIL}. The exception is always rethrown.
     *
     * @param callable           The {@link Callable} task to be executed.
     * @param exceptionsToReject A list of exception classes that should result in a {@code REJECT} status.
     * @param <T>                The type of the result returned by the callable.
     * @return The result of the callable task.
     * @throws Exception The original exception thrown by the callable.
     */
    @SuppressWarnings("unchecked")
    @SneakyThrows
    default <T> T callOrReject(final Callable<T> callable, final Class<? extends Exception>... exceptionsToReject) {
        if (! isStarted()) start();
        try {
            final T result = callable.call();
            putContext(CONTEXT_RESULT, result);
            if (! isStopped()) ok();
            return result;
        } catch (final Exception e) {
            for (final Class<? extends Exception> exceptionClass : exceptionsToReject) {
                if (exceptionClass.isAssignableFrom(e.getClass())) {
                    reject(e);
                    throw e;
                }
            }
            fail(e);
            throw e;
        }
    }

    /**
     * Executes the given {@link Callable} task within the Meter's lifecycle control, 
     * ensuring that checked exceptions are wrapped into a {@link RuntimeException}. 
     * The operation is automatically started before execution and marked as {@code OK} 
     * upon successful completion. The result of the {@link Callable} is stored in the 
     * context under {@link #CONTEXT_RESULT} and returned. If the task throws a 
     * {@link RuntimeException}, it's rethrown. If it throws any other {@link Exception}, 
     * it's wrapped in a new {@link RuntimeException} and rethrown.
     *
     * @param callable The {@link Callable} task to be executed.
     * @param <T>      The type of the result returned by the callable.
     * @return The result of the callable task.
     * @throws RuntimeException If the callable throws any exception, it will be wrapped in a RuntimeException.
     */
    default <T> T safeCall(final Callable<T> callable) {
        if (! isStarted()) start();
        try {
            final T result = callable.call();
            putContext(CONTEXT_RESULT, result);
            if (! isStopped()) ok();
            return result;
        } catch (final RuntimeException e) {
            fail(e);
            throw e;
        } catch (final Exception e) {
            fail(e);
            throw new RuntimeException("MeterExecutor.safeCall wrapped exception.", e);
        }
    }

/**
     * Executes the given {@link Callable} task within the `Meter`'s lifecycle control, wrapping any non-runtime
     * {@link Exception} into a specified {@link RuntimeException} subclass. The operation is automatically started
     * before execution and marked as {@code OK} upon successful completion. The result of the {@link Callable} is
     * stored in the context under {@link #CONTEXT_RESULT} and returned. If the task throws a {@link RuntimeException},
     * it's rethrown. If it throws any other {@link Exception}, it's wrapped into an instance of `exceptionClass` and
     * rethrown.
     *
     * @param exceptionClass The {@link Class} of the {@link RuntimeException} to wrap checked exceptions into. This
     *                       class must have a constructor that accepts a `String` message and a `Throwable` cause.
     * @param callable       The {@link Callable} task to be executed.
     * @param <E>            The type of the {@link RuntimeException} to wrap checked exceptions into.
     * @param <T>            The type of the result returned by the callable.
     * @return The result of the callable task.
     * @throws E                If the callable throws any non-runtime exception, it will be wrapped in an instance of
     *                          `exceptionClass`.
     * @throws RuntimeException If the callable throws a RuntimeException, or if `exceptionClass` cannot be
     *                          instantiated.
     */
    default <E extends RuntimeException, T> T safeCall(final Class<E> exceptionClass, final Callable<T> callable) {
        if (! isStarted()) start();
        try {
            final T result = callable.call();
            putContext(CONTEXT_RESULT, result);
            if (! isStopped()) ok();
            return result;
        } catch (final RuntimeException e) {
            fail(e);
            throw e;
        } catch (final Exception e) {
            fail(e);
            throw convertException(exceptionClass, e);
        }
    }

    /**
     * Converts a given {@link Exception} into a specified {@link RuntimeException} subclass. This method attempts to
     * create an instance of `exceptionClass` using a constructor that accepts a `String` message and a `Throwable`
     * cause. If such a constructor is not found or instantiation fails, a generic {@link RuntimeException} is
     * returned.
     *
     * @param exceptionClass The {@link Class} of the {@link RuntimeException} to create.
     * @param e              The {@link Exception} to be wrapped.
     * @param <T>            The type of the {@link RuntimeException} subclass.
     * @return An instance of `exceptionClass` wrapping `e`, or a generic {@link RuntimeException}.
     */
    default <T extends RuntimeException> RuntimeException convertException(final Class<T> exceptionClass, final Exception e) {
        try {
            return exceptionClass.getConstructor(String.class, Throwable.class).newInstance("MeterExecutor.safeCall wrapped exception", e);
        } catch (final NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException |
                       IllegalArgumentException | InvocationTargetException ignored) {
           getMessageLogger().error(Markers.INCONSISTENT_EXCEPTION, ERROR_MSG_METER_CANNOT_CREATE_EXCEPTION, exceptionClass, e);
        }
        return new RuntimeException(e);
    }
}