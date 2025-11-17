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
@Getter
public class MeterData extends SystemData implements MeterAnalysis {

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
    String category = null;
    /**
     * The name of the operation. May be {@code null} if the category itself sufficiently describes the operation.
     */
    String operation = null;
    /**
     * The full ID of the parent operation if this Meter was created as a sub-operation. {@code null} otherwise.
     */
    String parent = null;
    /**
     * An arbitrary short, human-readable message describing the operation.
     */
    String description = null;
    /**
     * The timestamp (in nanoseconds) when the operation was created or scheduled.
     */
    long createTime = 0;
    /**
     * The timestamp (in nanoseconds) when the operation started execution. Zero if the operation has not yet started.
     */
    long startTime = 0;
    /**
     * The timestamp (in nanoseconds) when the operation finished execution (success, rejection, or failure). Zero if
     * the operation has not yet finished.
     */
    long stopTime = 0;
    /**
     * The time limit (in nanoseconds) considered reasonable for successful execution of the operation. Zero if no time
     * limit is defined.
     */
    long timeLimit = 0;
    /**
     * The number of iterations completed by the operation. Zero if no iterations have run yet.
     */
    long currentIteration = 0;
    /**
     * The total number of iterations expected for the operation. Zero if iterations are not applicable or not
     * specified.
     */
    long expectedIterations = 0;
    /**
     * For successful execution, a string identifying the execution path. Mutually exclusive with {@link #rejectPath}
     * and {@link #failPath}. Set only when the operation finishes successfully and a path was provided.
     */
    String okPath = null;
    /**
     * For rejected execution, a string identifying the rejection cause. Mutually exclusive with {@link #okPath} and
     * {@link #failPath}. Set only when the operation finishes with a rejection.
     */
    String rejectPath = null;
    /**
     * For failed execution, a string identifying the failure cause (e.g., the class name of the exception). Mutually
     * exclusive with {@link #okPath} and {@link #rejectPath}. Set only when the operation finishes with a failure.
     */
    String failPath = null;
    /**
     * For failed execution, an optional message detailing the cause of the failure. Typically, the message from the
     * exception. Only set in conjunction with {@link #failPath}.
     */
    String failMessage = null;

    /**
     * Additional key-value pairs providing context for the operation.
     */
    Map<String, String> context = null;

    public Map<String, String> getContext() {
        return context == null ? null : Collections.unmodifiableMap(context);
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
        if (!Objects.equals(getSessionUuid(), meterData.getSessionUuid()))
            return false;
        if (getPosition() != meterData.getPosition()) return false;
        return Objects.equals(operation, meterData.operation);
    }

    @SuppressWarnings("NonFinalFieldReferencedInHashCode")
    @Override
    public int hashCode() {
        int result = category != null ? category.hashCode() : 0;
        result = 31 * result + (operation != null ? operation.hashCode() : 0);
        result = 31 * result + (int) (getPosition() ^ (getPosition() >>> 32));
        result = 31 * result + (getSessionUuid() != null ? getSessionUuid().hashCode() : 0);
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
