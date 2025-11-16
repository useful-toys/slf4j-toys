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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.usefultoys.slf4j.internal.SystemData;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Extends {@link SystemData} with specific attributes for an operation measured by a {@link Meter}. This class holds
 * data related to the operation's lifecycle, performance, and outcome.
 *
 * @author Daniel Felix Ferber
 * @see Meter
 * @see SystemData
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MeterData extends SystemData {

    private static final long serialVersionUID = 2L;

    /**
     * Constructs a MeterData instance for a new operation.
     *
     * @param uuid      The session UUID.
     * @param position  The sequential position of the operation.
     * @param category  The category name of the operation.
     * @param operation The name of the operation.
     * @param parent    The full ID of the parent operation, if this is a sub-operation.
     */
    protected MeterData(final String uuid, final long position, final String category, final String operation, final String parent) {
        super(uuid, position);
        this.category = category;
        this.operation = operation;
        this.parent = parent;
        createTime = collectCurrentTime();
    }

    /**
     * Constructs a MeterData instance with all fields, primarily for testing or deserialization.
     *
     * @param sessionUuid                    The session UUID.
     * @param position                       The sequential position of the operation.
     * @param time                           The timestamp when the data was collected.
     * @param heap_commited                  The committed heap memory in bytes.
     * @param heap_max                       The maximum heap memory in bytes.
     * @param heap_used                      The used heap memory in bytes.
     * @param nonHeap_commited               The committed non-heap memory in bytes.
     * @param nonHeap_max                    The maximum non-heap memory in bytes.
     * @param nonHeap_used                   The used non-heap memory in bytes.
     * @param objectPendingFinalizationCount The number of objects pending finalization.
     * @param classLoading_loaded            The number of classes currently loaded.
     * @param classLoading_total             The total number of classes loaded since JVM start.
     * @param classLoading_unloaded          The total number of classes unloaded since JVM start.
     * @param compilationTime                The total time spent in compilation.
     * @param garbageCollector_count         The total number of garbage collections.
     * @param garbageCollector_time          The total time spent in garbage collection.
     * @param runtime_usedMemory             The used memory reported by Runtime.
     * @param runtime_maxMemory              The maximum memory reported by Runtime.
     * @param runtime_totalMemory            The total memory reported by Runtime.
     * @param systemLoad                     The system CPU load.
     * @param category                       The category name of the operation.
     * @param operation                      The name of the operation.
     * @param parent                         The full ID of the parent operation.
     * @param description                    A human-readable description of the operation.
     * @param createTime                     Timestamp when the operation was created.
     * @param startTime                      Timestamp when the operation started execution.
     * @param stopTime                       Timestamp when the operation finished execution.
     * @param timeLimit                      The time limit for the operation in nanoseconds.
     * @param currentIteration               The current iteration count.
     * @param expectedIterations             The total expected iterations.
     * @param okPath                         The path taken for a successful execution.
     * @param rejectPath                     The reason for rejection.
     * @param failPath                       The reason for failure (e.g., exception class name).
     * @param failMessage                    An optional message detailing the failure.
     * @param context                        Additional key-value context data.
     */
    @SuppressWarnings("ConstructorWithTooManyParameters")
    protected MeterData(final String sessionUuid, final long position, final long time, final long heap_commited, final long heap_max, final long heap_used, final long nonHeap_commited, final long nonHeap_max, final long nonHeap_used, final long objectPendingFinalizationCount, final long classLoading_loaded, final long classLoading_total, final long classLoading_unloaded, final long compilationTime, final long garbageCollector_count, final long garbageCollector_time, final long runtime_usedMemory, final long runtime_maxMemory, final long runtime_totalMemory, final double systemLoad, final String category, final String operation, final String parent, final String description, final long createTime, final long startTime, final long stopTime, final long timeLimit, final long currentIteration, final long expectedIterations, final String okPath, final String rejectPath, final String failPath, final String failMessage, final Map<String, String> context) {
        super(sessionUuid, position, time, heap_commited, heap_max, heap_used, nonHeap_commited, nonHeap_max, nonHeap_used, objectPendingFinalizationCount, classLoading_loaded, classLoading_total, classLoading_unloaded, compilationTime, garbageCollector_count, garbageCollector_time, runtime_usedMemory, runtime_maxMemory, runtime_totalMemory, systemLoad);
        this.category = category;
        this.operation = operation;
        this.parent = parent;
        this.description = description;
        this.createTime = createTime;
        this.startTime = startTime;
        this.stopTime = stopTime;
        this.timeLimit = timeLimit;
        this.currentIteration = currentIteration;
        this.expectedIterations = expectedIterations;
        this.okPath = okPath;
        this.rejectPath = rejectPath;
        this.failPath = failPath;
        this.failMessage = failMessage;
        this.context = context;
    }

    /**
     * The category name of the operation being measured. By default, this is derived from the logger name.
     */
    @Getter
    protected String category = null;
    /**
     * The name of the operation. May be {@code null} if the category itself sufficiently describes the operation.
     */
    @Getter
    protected String operation = null;
    /**
     * The full ID of the parent operation if this Meter was created as a sub-operation. {@code null} otherwise.
     */
    @Getter
    protected String parent = null;
    /**
     * An arbitrary short, human-readable message describing the operation.
     */
    @Getter
    protected String description = null;
    /**
     * The timestamp (in nanoseconds) when the operation was created or scheduled.
     */
    @Getter
    protected long createTime = 0;
    /**
     * The timestamp (in nanoseconds) when the operation started execution. Zero if the operation has not yet started.
     */
    @Getter
    protected long startTime = 0;
    /**
     * The timestamp (in nanoseconds) when the operation finished execution (success, rejection, or failure). Zero if
     * the operation has not yet finished.
     */
    @Getter
    protected long stopTime = 0;
    /**
     * The time limit (in nanoseconds) considered reasonable for successful execution of the operation. Zero if no time
     * limit is defined.
     */
    @Getter
    protected long timeLimit = 0;
    /**
     * The number of iterations completed by the operation. Zero if no iterations have run yet.
     */
    @Getter
    protected long currentIteration = 0;
    /**
     * The total number of iterations expected for the operation. Zero if iterations are not applicable or not
     * specified.
     */
    @Getter
    protected long expectedIterations = 0;
    /**
     * For successful execution, a string identifying the execution path. Mutually exclusive with {@link #rejectPath}
     * and {@link #failPath}. Set only when the operation finishes successfully and a path was provided.
     */
    @Getter
    protected String okPath = null;
    /**
     * For rejected execution, a string identifying the rejection cause. Mutually exclusive with {@link #okPath} and
     * {@link #failPath}. Set only when the operation finishes with a rejection.
     */
    @Getter
    protected String rejectPath = null;
    /**
     * For failed execution, a string identifying the failure cause (e.g., the class name of the exception). Mutually
     * exclusive with {@link #okPath} and {@link #rejectPath}. Set only when the operation finishes with a failure.
     */
    @Getter
    protected String failPath = null;
    /**
     * For failed execution, an optional message detailing the cause of the failure. Typically, the message from the
     * exception. Only set in conjunction with {@link #failPath}.
     */
    @Getter
    protected String failMessage = null;

    /**
     * Additional key-value pairs providing context for the operation.
     */
    protected Map<String, String> context = null;

    /**
     * Returns a unique identifier for this MeterData instance, combining category, operation, and position.
     *
     * @return A string representing the full ID of the MeterData.
     */
    @SuppressWarnings("MagicCharacter")
    public String getFullID() {
        if (operation == null) {
            return category + '#' + position;
        }
        return String.format("%s/%s#%d", category, operation, position);
    }

    /**
     * Checks if the operation has started.
     *
     * @return {@code true} if {@code startTime} is not zero, {@code false} otherwise.
     */
    public boolean isStarted() {
        return startTime != 0;
    }

    /**
     * Checks if the operation has finished (either successfully, rejected, or failed).
     *
     * @return {@code true} if {@code stopTime} is not zero, {@code false} otherwise.
     */
    public boolean isStopped() {
        return stopTime != 0;
    }

    /**
     * Checks if the operation completed successfully.
     *
     * @return {@code true} if the operation is stopped and neither rejected nor failed.
     */
    public boolean isOK() {
        return (stopTime != 0) && (failPath == null && rejectPath == null);
    }

    /**
     * Checks if the operation was rejected.
     *
     * @return {@code true} if the operation is stopped and has a rejection path.
     */
    public boolean isReject() {
        return (stopTime != 0) && (rejectPath != null);
    }

    /**
     * Checks if the operation failed.
     *
     * @return {@code true} if the operation is stopped and has a failure path.
     */
    public boolean isFail() {
        return (stopTime != 0) && (failPath != null);
    }

    /**
     * Returns an unmodifiable map of the context data associated with this operation.
     *
     * @return An unmodifiable map of context key-value pairs, or {@code null} if no context is set.
     */
    public Map<String, String> getContext() {
        if (context == null) {
            return null;
        }
        return Collections.unmodifiableMap(context);
    }

    /**
     * Returns the path of the operation's outcome (success, rejection, or failure).
     *
     * @return The {@code failPath}, {@code rejectPath}, or {@code okPath}, in that order of precedence.
     */
    public String getPath() {
        if (failPath != null) return failPath;
        if (rejectPath != null) return rejectPath;
        return okPath;
    }

    /**
     * Calculates the iterations per second for the operation.
     *
     * @return The rate of iterations per second, or 0.0 if the operation has not started or has no iterations.
     */
    public double getIterationsPerSecond() {
        if (currentIteration == 0 || startTime == 0) {
            return 0.0d;
        } else if (stopTime == 0) {
            return ((double) currentIteration) / (lastCurrentTime - startTime) * 1000000000;
        }
        return ((double) currentIteration) / (stopTime - startTime) * 1000000000;
    }

    /**
     * Returns the execution time of the operation.
     *
     * @return The duration from {@code startTime} to {@code stopTime} (if stopped) or to {@code lastCurrentTime} (if
     * ongoing), in nanoseconds.
     */
    public long getExecutionTime() {
        if (startTime == 0) {
            return 0;
        } else if (stopTime == 0) {
            return lastCurrentTime - startTime;
        }
        return stopTime - startTime;
    }

    /**
     * Returns the waiting time before the operation started.
     *
     * @return The duration from {@code createTime} to {@code startTime} (if started) or to {@code lastCurrentTime} (if
     * not yet started), in nanoseconds.
     */
    public long getWaitingTime() {
        if (startTime == 0) {
            return lastCurrentTime - createTime;
        }
        return startTime - createTime;
    }

    /**
     * Checks if the operation is considered slow based on its {@code timeLimit}.
     *
     * @return {@code true} if a {@code timeLimit} is set, the operation has started, and its execution time exceeds the
     * limit.
     */
    public boolean isSlow() {
        return timeLimit != 0 && startTime != 0 && getExecutionTime() > timeLimit;
    }

    @Override
    public void reset() {
        super.reset();
        category = null;
        operation = null;
        parent = null;
        description = null;
        okPath = null;
        rejectPath = null;
        createTime = 0;
        startTime = 0;
        stopTime = 0;
        currentIteration = 0;
        expectedIterations = 0;
        failPath = null;
        failMessage = null;
        timeLimit = 0;
        context = null;
    }

    /**
     * Generates a human-readable string representation.
     *
     * @return A string containing the human-readable message.
     */
    public final String readableMessage() {
        final StringBuilder builder = new StringBuilder(200);
        MeterDataFormatter.readableStringBuilder(this, builder);
        return builder.toString();
    }

     /**
     * Returns the machine-parsable, JSON5-encoded representation of the event.
     *
     * @return A string containing the JSON5-encoded message.
     */
    public final String json5Message() {
        final StringBuilder sb = new StringBuilder(200);
        sb.append("{");
        writeJson5(sb);
        sb.append("}");
        return sb.toString();
    }

    /**
     * Compares this MeterData object to the specified object. The comparison is based on {@code category},
     * {@code operation}, {@code position}, and {@code sessionUuid}.
     *
     * @param o The object to compare against.
     * @return {@code true} if the objects are equal, {@code false} otherwise.
     */
    @SuppressWarnings("NonFinalFieldReferenceInEquals")
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final MeterData meterData = (MeterData) o;

        if (!Objects.equals(category, meterData.category))
            return false;
        if (!Objects.equals(sessionUuid, meterData.sessionUuid))
            return false;
        if (position != meterData.position) return false;
        return Objects.equals(operation, meterData.operation);
    }

    /**
     * Returns a hash code for this MeterData object.
     *
     * @return A hash code value for this object.
     */
    @SuppressWarnings("NonFinalFieldReferencedInHashCode")
    @Override
    public int hashCode() {
        int result = category != null ? category.hashCode() : 0;
        result = 31 * result + (operation != null ? operation.hashCode() : 0);
        result = 31 * result + (int) (position ^ (position >>> 32));
        result = 31 * result + (sessionUuid != null ? sessionUuid.hashCode() : 0);
        return result;
    }

    @Override
    protected void writeJson5(final StringBuilder sb) {
        super.writeJson5(sb);
        MeterDataJson5.write(this, sb);
    }

    @Override
    public void readJson5(final String json5) {
        super.readJson5(json5);
        MeterDataJson5.read(this, json5);
    }
}
