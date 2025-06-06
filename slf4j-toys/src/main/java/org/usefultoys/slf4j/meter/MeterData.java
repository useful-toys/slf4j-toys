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
import org.usefultoys.slf4j.utils.UnitFormatter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Augments the {@link SystemData} with start, stop, failure and path information for an operation measured by Meter.
 *
 * @author Daniel Felix Ferber
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MeterData extends SystemData {

    private static final long serialVersionUID = 2L;

    protected MeterData(final String uuid, final long position, final String category, final String operation, final String parent) {
        super(uuid, position);
        this.category = category;
        this.operation = operation;
        this.parent = parent;
        createTime = collectCurrentTime();
    }

    // for tests only
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
     * Name of the category that the operation being measured belongs to. By default, it is the last name of the logger.
     */
    @Getter
    protected String category = null;
    /**
     * Name of the operation. May be null if the category already describes the operation itself.
     */
    @Getter
    protected String operation = null;
    /**
     * If the Meter was created as a child for another Meter, references category name, operation name and position of these other Meter. Null otherwise.
     */
    @Getter
    protected String parent = null;
    /**
     * An arbitrary short, human readable message to describe the operation being measured.
     */
    @Getter
    protected String description = null;
    /**
     * Timestamp when the operation was created/scheduled (nanoseconds).
     */
    @Getter
    protected long createTime = 0;
    /**
     * Timestamp when the operation started execution (nanoseconds). Zero if the operation has not yet started.
     */
    @Getter
    protected long startTime = 0;
    /**
     * Timestamp when the operation finished execution (either success, rejection or failure) (nanoseconds). Zero if the operation has not yet finished.
     */
    @Getter
    protected long stopTime = 0;
    /**
     * Time limit considered reasonable for successful execution of the operation (nanoseconds). Zero if there is no time limit for the operation.
     */
    @Getter
    protected long timeLimit = 0;
    /**
     * How many iterations were run by the operation. Zero if no iteration has yet run.
     */
    @Getter
    protected long currentIteration = 0;
    /**
     * How many iterations were expected by the operation. Zero if no iterations were expected.
     */
    @Getter
    protected long expectedIterations = 0;
    /**
     * For successful execution, the string that identifies the execution path. Mutually exclusive with {@link #rejectPath} and {@link #failPath}. Set only when
     * operation finishes with success and a path was given.
     */
    @Getter
    protected String okPath = null;
    /**
     * For rejected execution, the string that identifies the rejection cause. Mutually exclusive with {@link #okPath} and {@link #failPath}. Set only when
     * operation finishes with rejection.
     */
    @Getter
    protected String rejectPath = null;
    /**
     * For failed execution, the string that identifies the failure caused. Usually, the class name of the exception that describes the failure. Mutually
     * exclusive with {@link #okPath} and {@link #rejectPath}. Set only when operation finishes with failure.
     */
    @Getter
    protected String failPath = null;
    /**
     * For failed execution, an optional message that caused the failure. Usually, the message of the exception. Only set in conjuction with {@link #failPath}.
     */
    @Getter
    protected String failMessage = null;

    /**
     * Additional meta data describing the job.
     */
    protected Map<String, String> context = null;

    @SuppressWarnings("MagicCharacter")
    public String getFullID() {
        if (operation == null) {
            return category + '#' + position;
        }
        return String.format("%s/%s#%d", category, operation, position);
    }

    public boolean isStarted() {
        return startTime != 0;
    }

    public boolean isStopped() {
        return stopTime != 0;
    }

    public boolean isOK() {
        return (stopTime != 0) && (failPath == null && rejectPath == null);
    }

    public boolean isReject() {
        return (stopTime != 0) && (rejectPath != null);
    }

    public boolean isFail() {
        return (stopTime != 0) && (failPath != null);
    }

    public Map<String, String> getContext() {
        if (context == null) {
            return null;
        }
        return Collections.unmodifiableMap(context);
    }

    /**
     * @return If the operation execution time has exceeded (finished execution) or is exceeding (ongoing execution) the time limit.
     */

    public String getPath() {
        if (failPath != null) return failPath;
        if (rejectPath != null) return rejectPath;
        return okPath;
    }

    public double getIterationsPerSecond() {
        if (currentIteration == 0 || startTime == 0) {
            return 0.0d;
        } else if (stopTime == 0) {
            return ((double) currentIteration) / (lastCurrentTime - startTime) * 1000000000;
        }
        return ((double) currentIteration) / (stopTime - startTime) * 1000000000;
    }

    /**
     * @return The execution time after start (until stop for finished or until now for ongoing execution).
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
     * @return The waiting time since Meter was created and before it was started.
     */
    public long getWaitingTime() {
        if (startTime == 0) {
            return lastCurrentTime - createTime;
        }
        return startTime - createTime;
    }

    public boolean isSlow() {
        return timeLimit != 0 && startTime != 0 && getExecutionTime() > timeLimit;
    }

    @Override
    protected void resetImpl() {
        super.resetImpl();
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

    @Override
    protected void collectRuntimeStatus() {
        super.collectRuntimeStatus();
    }

    private static boolean separator(final StringBuilder sb, final boolean hasPrevious) {
        if (hasPrevious) {
            sb.append("; ");
        }
        return true;
    }

    @SuppressWarnings("MagicCharacter")
    @Override
    public StringBuilder readableStringBuilder(final StringBuilder builder) {
        final long executionTime;
        if (startTime == 0) {
            executionTime = 0;
        } else if (stopTime == 0) {
            executionTime = lastCurrentTime - startTime;
        } else {
            executionTime = stopTime - startTime;
        }
        final boolean slow = timeLimit > 0 && executionTime > timeLimit;
        final boolean progressInfoRequired = executionTime > MeterConfig.progressPeriodMilliseconds;

        if (MeterConfig.printStatus) {
            if (stopTime != 0) {
                //noinspection VariableNotUsedInsideIf
                if (rejectPath != null) {
                    builder.append("REJECT");
                } else //noinspection VariableNotUsedInsideIf
                    if (failPath != null) {
                    builder.append("FAIL");
                } else if (slow) {
                    builder.append("OK (Slow)");
                } else {
                    builder.append("OK");
                }
            } else if (startTime != 0) {
                if (currentIteration == 0) {
                    builder.append("STARTED");
                } else if (slow) {
                    builder.append("PROGRESS (Slow)");
                } else {
                    builder.append("PROGRESS");
                }
            } else {
                builder.append("SCHEDULED");
            }
            builder.append(": ");
        }


        /* Identification. */
        boolean hasId = false;
        if (MeterConfig.printCategory) {
            final int index = category.lastIndexOf('.') + 1;
            builder.append(category.substring(index));
            hasId = true;
        }
        if (operation != null) {
            if (MeterConfig.printCategory) {
                builder.append('/');
            }
            builder.append(operation);
            hasId = true;
        }
        if (MeterConfig.printPosition) {
            builder.append('#');
            builder.append(position);
            hasId = true;
        }

        /* Execution path, if any. */
        if (okPath != null) {
            builder.append("[");
            builder.append(okPath);
            builder.append(']');
            hasId = true;
        }
        if (rejectPath != null) {
            builder.append("[");
            builder.append(rejectPath);
            builder.append(']');
            hasId = true;
        }
        if (failPath != null) {
            builder.append("[");
            builder.append(failPath);
            if (failMessage != null) {
                builder.append("; ");
                builder.append(failMessage);
            }
            builder.append(']');
            hasId = true;
        }
        if (hasId) {
            builder.append(' ');
        }

        /* Number of iterations. */
        boolean hasPrevious = false;
        if (startTime != 0 && currentIteration > 0) {
            hasPrevious = separator(builder, hasPrevious);
            builder.append(UnitFormatter.iterations(currentIteration));
            if (expectedIterations > 0) {
                builder.append('/');
                builder.append(UnitFormatter.iterations(expectedIterations));
            }
        }

        /* Timing. */
        if (startTime == 0) {
            /* Not yet started, report waiting time. */
            hasPrevious = separator(builder, hasPrevious);
            builder.append(UnitFormatter.nanoseconds(lastCurrentTime -createTime));
        } else {
            if (stopTime != 0 || progressInfoRequired) {
                /* Started, report elapsed time if waiting for a considerable amount of time. */
                /* Or stopped, retport total time. */
                hasPrevious = separator(builder, hasPrevious);
                builder.append(UnitFormatter.nanoseconds(executionTime));
            }

            if (currentIteration > 0 && (stopTime != 0 || progressInfoRequired)) {
                /* Doing iterations, report speed if waiting for a considerable amount of time of if stopped. */
                hasPrevious = separator(builder, hasPrevious);
                final double iterationsPerSecond = getIterationsPerSecond();
                builder.append(UnitFormatter.iterationsPerSecond(iterationsPerSecond));
                builder.append(' ');
                final double nanoSecondsPerIteration = 1.0F / iterationsPerSecond * 1000000000;
                builder.append(UnitFormatter.nanoseconds(nanoSecondsPerIteration));
            }
        }

        /* Meta data. */
        if (description != null) {
            hasPrevious = separator(builder, hasPrevious);
            builder.append('\'');
            builder.append(description);
            builder.append('\'');
        }
        if (context != null && ! context.isEmpty()) {
            for (final Entry<String, String> entry : context.entrySet()) {
                hasPrevious = separator(builder, hasPrevious);
                builder.append(entry.getKey());
                if (entry.getValue() != null) {
                    builder.append("=");
                    builder.append(entry.getValue());
                }
            }
        }

        /* System Info */
        if (MeterConfig.printMemory && runtime_usedMemory > 0) {
            hasPrevious = separator(builder, hasPrevious);
            builder.append(UnitFormatter.bytes(runtime_usedMemory));
        }
        if (MeterConfig.printLoad && systemLoad > 0) {
            hasPrevious = separator(builder, hasPrevious);
            builder.append(Math.round(systemLoad * 100));
            builder.append("%");
        }
        if (sessionUuid != null) {
            hasPrevious = separator(builder, hasPrevious);
            builder.append(sessionUuid);
        }

        return builder;
    }

    private static final String PROP_DESCRIPTION = "d";
    private static final String PROP_PATH_ID = "p";
    private static final String PROP_REJECT_ID = "r";
    private static final String PROP_FAIL_ID = "f";
    private static final String PROP_FAIL_MESSAGE = "fm";
    private static final String PROP_CREATE_TIME = "t0";
    private static final String PROP_START_TIME = "t1";
    private static final String PROP_STOP_TIME = "t2";
    private static final String PROP_ITERATION = "i";
    private static final String PROP_EXPECTED_ITERATION = "ei";
    private static final String PROP_LIMIT_TIME = "tl";
    private static final String PROP_CONTEXT = "ctx";
    private static final String EVENT_CATEGORY = "c";
    private static final String EVENT_NAME = "n";
    private static final String EVENT_PARENT = "ep";

    private static final Pattern patternDescription = Pattern.compile(REGEX_START +PROP_DESCRIPTION + REGEX_STRING_VALUE);
    private static final Pattern patternPathId = Pattern.compile(REGEX_START +PROP_PATH_ID + REGEX_WORD_VALUE);
    private static final Pattern patternRejectId = Pattern.compile(REGEX_START +PROP_REJECT_ID + REGEX_WORD_VALUE);
    private static final Pattern patternFailId = Pattern.compile(REGEX_START +PROP_FAIL_ID + REGEX_WORD_VALUE);
    private static final Pattern patternFailMessage = Pattern.compile(REGEX_START +PROP_FAIL_MESSAGE + REGEX_STRING_VALUE);
    private static final Pattern patternCreateTime = Pattern.compile(REGEX_START +PROP_CREATE_TIME + REGEX_WORD_VALUE);
    private static final Pattern patternStartTime = Pattern.compile(REGEX_START +PROP_START_TIME + REGEX_WORD_VALUE);
    private static final Pattern patternStopTime = Pattern.compile(REGEX_START +PROP_STOP_TIME + REGEX_WORD_VALUE);
    private static final Pattern patternIteration = Pattern.compile(REGEX_START +PROP_ITERATION + REGEX_WORD_VALUE);
    private static final Pattern patternExpectedIteration = Pattern.compile(REGEX_START +PROP_EXPECTED_ITERATION + REGEX_WORD_VALUE);
    private static final Pattern patternLimitTime = Pattern.compile(REGEX_START +PROP_LIMIT_TIME + REGEX_WORD_VALUE);
    private static final Pattern patternEventCategory = Pattern.compile(REGEX_START +EVENT_CATEGORY + REGEX_WORD_VALUE);
    private static final Pattern patternEventName = Pattern.compile(REGEX_START +EVENT_NAME + REGEX_WORD_VALUE);
    private static final Pattern patternEventParent = Pattern.compile(REGEX_START +EVENT_PARENT + REGEX_WORD_VALUE);
    private static final Pattern patternContext = Pattern.compile(REGEX_START +PROP_CONTEXT + "\\s*:\\s*\\{([^}]*)\\}");

    @SuppressWarnings("CallToSuspiciousStringMethod")
    public void readJson5(final String json5) {
        super.readJson5(json5);

        final Matcher matcherDescription = patternDescription.matcher(json5);
        if (matcherDescription.find()) {
            description = matcherDescription.group(1);
        }

        final Matcher matcherPathId = patternPathId.matcher(json5);
        if (matcherPathId.find()) {
            okPath = matcherPathId.group(1);
        }

        final Matcher matcherRejectId = patternRejectId.matcher(json5);
        if (matcherRejectId.find()) {
            rejectPath = matcherRejectId.group(1);
        }

        final Matcher matcherFailId = patternFailId.matcher(json5);
        if (matcherFailId.find()) {
            failPath = matcherFailId.group(1);
        }

        final Matcher matcherFailMessage = patternFailMessage.matcher(json5);
        if (matcherFailMessage.find()) {
            failMessage = matcherFailMessage.group(1);
        }

        final Matcher matcherCreateTime = patternCreateTime.matcher(json5);
        if (matcherCreateTime.find()) {
            createTime = Long.parseLong(matcherCreateTime.group(1));
        }

        final Matcher matcherStartTime = patternStartTime.matcher(json5);
        if (matcherStartTime.find()) {
            startTime = Long.parseLong(matcherStartTime.group(1));
        }

        final Matcher matcherStopTime = patternStopTime.matcher(json5);
        if (matcherStopTime.find()) {
            stopTime = Long.parseLong(matcherStopTime.group(1));
        }

        final Matcher matcherIteration = patternIteration.matcher(json5);
        if (matcherIteration.find()) {
            currentIteration = Long.parseLong(matcherIteration.group(1));
        }

        final Matcher matcherExpectedIteration = patternExpectedIteration.matcher(json5);
        if (matcherExpectedIteration.find()) {
            expectedIterations = Long.parseLong(matcherExpectedIteration.group(1));
        }

        final Matcher matcherLimitTime = patternLimitTime.matcher(json5);
        if (matcherLimitTime.find()) {
            timeLimit = Long.parseLong(matcherLimitTime.group(1));
        }

        final Matcher matcherEventCategory = patternEventCategory.matcher(json5);
        if (matcherEventCategory.find()) {
            category = matcherEventCategory.group(1);
        }

        final Matcher matcherEventName = patternEventName.matcher(json5);
        if (matcherEventName.find()) {
            operation = matcherEventName.group(1);
        }

        final Matcher matcherEventParent = patternEventParent.matcher(json5);
        if (matcherEventParent.find()) {
            parent = matcherEventParent.group(1);
        }

        final Matcher matcherContext = patternContext.matcher(json5);
        if (matcherContext.find()) {
            final String contextString = matcherContext.group(1);
            if (contextString != null && !contextString.isEmpty()) {
                final String[] contextEntries = contextString.split(",");
                context = new HashMap<>(10);
                for (final String entry : contextEntries) {
                    final String[] keyValue = entry.split(":");
                    if (keyValue.length >= 1) {
                        final String key = keyValue[0].trim();
                        final String value = (keyValue.length > 1) ? keyValue[1].trim() : null;
                        context.put(key, value);
                    }
                }
            }
        }
    }

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

    @SuppressWarnings("NonFinalFieldReferencedInHashCode")
    @Override
    public int hashCode() {
        int result = category != null ? category.hashCode() : 0;
        result = 31 * result + (operation != null ? operation.hashCode() : 0);
        result = 31 * result + (int) (position ^ (position >>> 32));
        result = 31 * result + (sessionUuid != null ? sessionUuid.hashCode() : 0);
        return result;
    }

    @SuppressWarnings("MagicCharacter")
    @Override
    public void writeJson5Impl(final StringBuilder sb) {
        super.writeJson5Impl(sb);
        if (description != null) {
            sb.append(String.format(",%s:'%s'", PROP_DESCRIPTION, description));
        }
        if (rejectPath != null) {
            sb.append(String.format(",%s:%s",PROP_REJECT_ID, rejectPath));
        }
        if (okPath != null) {
            sb.append(String.format(",%s:%s",PROP_PATH_ID, okPath));
        }
        if (failPath != null) {
            sb.append(String.format(",%s:%s",PROP_FAIL_ID, failPath));
        }
        if (failMessage != null) {
            sb.append(String.format(",%s:'%s'", PROP_FAIL_MESSAGE, failMessage));
        }
        if (category != null) {
            sb.append(String.format(",%s:%s",EVENT_CATEGORY, category));
        }
        if (operation != null) {
            sb.append(String.format(",%s:%s",EVENT_NAME, operation));
        }
        if (parent != null) {
            sb.append(String.format(",%s:%s",EVENT_PARENT, parent));
        }

        /* Create, start, stop time. */
        if (createTime != 0) {
            sb.append(String.format(",%s:%d",PROP_CREATE_TIME, createTime));
        }
        if (startTime != 0) {
            sb.append(String.format(",%s:%d",PROP_START_TIME, startTime));
        }
        if (stopTime != 0) {
            sb.append(String.format(",%s:%d",PROP_STOP_TIME, stopTime));
        }
        if (currentIteration != 0) {
            sb.append(String.format(",%s:%d",PROP_ITERATION, currentIteration));
        }
        if (expectedIterations != 0) {
            sb.append(String.format(",%s:%d",PROP_EXPECTED_ITERATION, expectedIterations));
        }
        if (timeLimit != 0) {
            sb.append(String.format(",%s:%d",PROP_LIMIT_TIME, timeLimit));
        }

        /* Context */
        if (context != null && !context.isEmpty()) {
            sb.append(',');
            sb.append(PROP_CONTEXT);
            sb.append(":{");
            boolean separatorNeeded = false;
            for (final Entry<String, String> entry : context.entrySet()) {
                if (separatorNeeded) {
                    sb.append(',');
                } else {
                    separatorNeeded = true;
                }
                sb.append(entry.getKey());
                sb.append(':');
                if (entry.getValue() != null) {
                    sb.append(entry.getValue());
                }
            }
            sb.append('}');
        }
    }
}
