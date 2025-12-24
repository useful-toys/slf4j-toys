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

/**
 * An interface defining analytical methods for {@link MeterData}.
 * This interface provides default implementations for common queries and calculations
 * based on the raw data available in {@link MeterData} (and its superclasses).
 * By implementing this interface, {@link MeterData} can expose rich behavior
 * while keeping its core data-holding responsibilities clean.
 *
 * @author Daniel Felix Ferber
 */
public interface MeterAnalysis {

    // --- Abstract Getters (from EventData, SystemData, MeterData) ---
    // These methods must be implemented by the class implementing this interface
    // to provide the raw data for the default methods.

    long getLastCurrentTime();

    String getCategory();
    String getOperation();
    String getParent();
    long getCreateTime();
    long getStartTime();
    long getStopTime();
    long getTimeLimit();
    long getCurrentIteration();
    String getOkPath();
    String getRejectPath();
    String getFailPath();

    // --- Default Methods (Business Logic and Calculations) ---

    /**
     * Checks if the operation has started.
     *
     * @return {@code true} if {@code startTime} is not zero, {@code false} otherwise.
     */
    default boolean isStarted() {
        return getStartTime() != 0;
    }

    /**
     * Checks if the operation has finished (either successfully, rejected, or failed).
     *
     * @return {@code true} if {@code stopTime} is not zero, {@code false} otherwise.
     */
    default boolean isStopped() {
        return getStopTime() != 0;
    }

    /**
     * Checks if the operation completed successfully.
     *
     * @return {@code true} if the operation is stopped and neither rejected nor failed.
     */
    default boolean isOK() {
        return (isStopped()) && (getFailPath() == null && getRejectPath() == null);
    }

    /**
     * Checks if the operation was rejected.
     *
     * @return {@code true} if the operation is stopped and has a rejection path.
     */
    default boolean isReject() {
        return (isStopped()) && (getRejectPath() != null);
    }

    /**
     * Checks if the operation failed.
     *
     * @return {@code true} if the operation is stopped and has a failure path.
     */
    default boolean isFail() {
        return (isStopped()) && (getFailPath() != null);
    }

    /**
     * Returns the path of the operation's outcome (success, rejection, or failure).
     *
     * @return The {@code failPath}, {@code rejectPath}, or {@code okPath}, in that order of precedence.
     */
    default String getPath() {
        if (getFailPath() != null) return getFailPath();
        if (getRejectPath() != null) return getRejectPath();
        return getOkPath();
    }

    /**
     * Calculates the iterations per second for the operation.
     *
     * @return The rate of iterations per second, or 0.0 if the operation has not started or has no iterations.
     */
    default double getIterationsPerSecond() {
        if (getCurrentIteration() == 0 || getStartTime() == 0) {
            return 0.0d;
        } else if (getStopTime() == 0) {
            return ((double) getCurrentIteration()) / (getLastCurrentTime() - getStartTime()) * 1000000000;
        }
        return ((double) getCurrentIteration()) / (getStopTime() - getStartTime()) * 1000000000;
    }

    /**
     * Returns the execution time of the operation.
     *
     * @return The duration from {@code startTime} to {@code stopTime} (if stopped) or to {@code lastCurrentTime} (if
     * ongoing), in nanoseconds.
     */
    default long getExecutionTime() {
        if (getStartTime() == 0) {
            return 0;
        } else if (getStopTime() == 0) {
            return getLastCurrentTime() - getStartTime();
        }
        return getStopTime() - getStartTime();
    }

    /**
     * Returns the waiting time before the operation started.
     *
     * @return The duration from {@code createTime} to {@code startTime} (if started) or to {@code lastCurrentTime} (if
     * not yet started), in nanoseconds.
     */
    default long getWaitingTime() {
        if (getStartTime() == 0) {
            return getLastCurrentTime() - getCreateTime();
        }
        return getStartTime() - getCreateTime();
    }

    /**
     * Checks if the operation is considered slow based on its {@code timeLimit}.
     *
     * @return {@code true} if a {@code timeLimit} is set, the operation has started, and its execution time exceeds the
     * limit.
     */
    default boolean isSlow() {
        return getTimeLimit() != 0 && isStarted() && getExecutionTime() > getTimeLimit();
    }
}
