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

import lombok.Getter;
import org.usefultoys.slf4j.SessionConfig;
import org.usefultoys.slf4j.internal.SystemData;
import org.usefultoys.slf4j.utils.UnitFormatter;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Augments the {@link SystemData} with start, stop, failure and path information for an operation measured by Meter.
 *
 * @author Daniel Felix Ferber
 */
public class MeterData extends SystemData {

    private static final long serialVersionUID = 2L;
    public static final char DETAILED_MESSAGE_PREFIX = 'M';

    public MeterData() {
    }

    public MeterData(final String uuid) {
        super(uuid);
    }


    protected MeterData(final String uuid, final long position, final String category, final String operation, final String parent) {
        super(uuid, position);
        this.category = category;
        this.operation = operation;
        this.parent = parent;
        this.createTime = System.nanoTime();
    }

    public MeterData(final String sessionUuid, final long position, final long time, final long heap_commited, final long heap_max, final long heap_used, final long nonHeap_commited, final long nonHeap_max, final long nonHeap_used, final long objectPendingFinalizationCount, final long classLoading_loaded, final long classLoading_total, final long classLoading_unloaded, final long compilationTime, final long garbageCollector_count, final long garbageCollector_time, final long runtime_usedMemory, final long runtime_maxMemory, final long runtime_totalMemory, final double systemLoad, final String category, final String operation, final String parent, final String description, final long createTime, final long startTime, final long stopTime, final long timeLimit, final long currentIteration, final long expectedIterations, final String okPath, final String rejectPath, final String failPath, final String failMessage, final Map<String, String> context) {
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

    public String getFullID() {
        if (operation == null) {
            return category + '/' + position;
        }
        return category + '/' + operation + '#' + position;
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
    public boolean isSlow() {
        return timeLimit != 0 && stopTime != 0 && startTime != 0 && stopTime - startTime > timeLimit;
    }

    public String getPath() {
        if (failPath != null) return failPath;
        if (rejectPath != null) return rejectPath;
        return okPath;
    }

    /**
     * @return The execution time after start (until stop for finished or until now for ongoing execution).
     */
    public long getExecutionTime() {
        if (startTime == 0) {
            return 0;
        } else if (stopTime == 0) {
            return System.nanoTime() - startTime;
        }
        return stopTime - startTime;
    }

    /**
     * @return The waiting time since Meter was created and before it was started.
     */
    public long getWaitingTime() {
        if (startTime == 0) {
            return System.nanoTime() - createTime;
        }
        return startTime - createTime;
    }

    public double getIterationsPerSecond() {
        if (currentIteration == 0 || startTime == 0) {
            return 0.0d;
        }
        final float executionTimeNS = getExecutionTime();
        if (executionTimeNS == 0) {
            return 0.0d;
        }
        return ((double) this.currentIteration) / executionTimeNS * 1000000000;
    }

    @Override
    protected void resetImpl() {
        super.resetImpl();
        this.category = null;
        this.operation = null;
        this.parent = null;
        this.description = null;
        this.okPath = null;
        this.rejectPath = null;
        this.createTime = 0;
        this.startTime = 0;
        this.stopTime = 0;
        this.currentIteration = 0;
        this.expectedIterations = 0;
        this.failPath = null;
        this.failMessage = null;
        this.timeLimit = 0;
        this.context = null;
    }

    @Override
    protected void collectRuntimeStatus() {
        super.collectRuntimeStatus();
    }

    @Override
    public StringBuilder readableStringBuilder(final StringBuilder builder) {
        if (MeterConfig.printStatus) {
            if (stopTime != 0) {
                if (failPath == null && rejectPath == null) {
                    if (timeLimit != 0 && startTime != 0 && stopTime - startTime > timeLimit) {
                        builder.append("OK (Slow)");
                    } else {
                        builder.append("OK");
                    }
                } else if (rejectPath != null) {
                    builder.append("REJECT");
                } else if (failPath != null) {
                    builder.append("FAIL");
                }
            } else if (startTime != 0 && currentIteration == 0) {
                builder.append("STARTED");
            } else if (startTime != 0) {
                builder.append("PROGRESS");
            } else {
                builder.append("SCHEDULED");
            }
            builder.append(": ");
        }

        if (MeterConfig.printCategory) {
            final int index = category.lastIndexOf('.') + 1;
            builder.append(category.substring(index));
        }
        if (operation != null) {
            if (MeterConfig.printCategory) {
                builder.append('/');
            }
            builder.append(operation);
        }
        if (MeterConfig.printPosition) {
            builder.append('#');
            builder.append(position);
        }

        if (okPath != null) {
            builder.append(" [");
            builder.append(okPath);
            builder.append(']');
        }
        if (rejectPath != null) {
            builder.append(" [");
            builder.append(rejectPath);
            builder.append(']');
        }
        if (failPath != null || failMessage != null) {
            builder.append(" [");
            if (failPath != null) {
                builder.append(failPath);
            }
            if (failPath != null && failMessage != null) {
                builder.append("; ");
            }
            if (failMessage != null) {
                builder.append(failMessage);
            }
            builder.append(']');
        }

        if (startTime != 0 && currentIteration > 0) {
            builder.append(' ');
            builder.append(UnitFormatter.iterations(this.currentIteration));
            if (this.expectedIterations > 0) {
                builder.append('/');
                builder.append(UnitFormatter.iterations(this.expectedIterations));
            }
        }

        if (this.description != null) {
            builder.append(" '");
            builder.append(this.description);
            builder.append('\'');
        }

        if (context != null) {
            for (final Entry<String, String> entry : context.entrySet()) {
                builder.append("; ");
                builder.append(entry.getKey());
                if (entry.getValue() != null) {
                    builder.append("=");
                    builder.append(entry.getValue());
                }
            }
        }

        if (this.startTime != 0) {
            final long executionTime = getExecutionTime();
            if (executionTime > MeterConfig.progressPeriodMilliseconds) {
                builder.append("; ");
                builder.append(UnitFormatter.nanoseconds(executionTime));
            }
            if (this.currentIteration > 0) {
                builder.append("; ");
                final double iterationsPerSecond = getIterationsPerSecond();
                builder.append(UnitFormatter.iterationsPerSecond(iterationsPerSecond));
                builder.append(' ');
                final double nanoSecondsPerIteration = 1.0F / iterationsPerSecond * 1000000000;
                builder.append(UnitFormatter.nanoseconds(nanoSecondsPerIteration));
            }
        } else {
            builder.append("; ");
            builder.append(UnitFormatter.nanoseconds(getWaitingTime()));
        }

        if (MeterConfig.printMemory && this.runtime_usedMemory > 0) {
            builder.append("; ");
            builder.append(UnitFormatter.bytes(this.runtime_usedMemory));
        }
        if (MeterConfig.printLoad && this.systemLoad > 0) {
            builder.append("; ");
            builder.append(Math.round(this.systemLoad * 100));
            builder.append("%");
        }
        if (SessionConfig.uuidSize != 0 && this.sessionUuid != null) {
            builder.append("; ");
            builder.append(this.sessionUuid.substring(SessionConfig.UUID_LENGTH - SessionConfig.uuidSize));
        }

        return builder;
    }

    public static final String PROP_DESCRIPTION = "d";
    public static final String PROP_PATH_ID = "p";
    public static final String PROP_REJECT_ID = "r";
    public static final String PROP_FAIL_ID = "f";
    public static final String PROP_CREATE_TIME = "t0";
    public static final String PROP_START_TIME = "t1";
    public static final String PROP_STOP_TIME = "t2";
    public static final String PROP_ITERATION = "i";
    public static final String PROP_EXPECTED_ITERATION = "ei";
    public static final String PROP_LIMIT_TIME = "tl";
    public static final String PROP_CONTEXT = "ctx";
    public static final String EVENT_CATEGORY = "c";
    public static final String EVENT_NAME = "n";
    public static final String EVENT_PARENT = "ep";

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final MeterData meterData = (MeterData) o;

        if (category != null ? !category.equals(meterData.category) : meterData.category != null)
            return false;
        if (sessionUuid != null ? !sessionUuid.equals(meterData.sessionUuid) : meterData.sessionUuid != null)
            return false;
        if (position != meterData.position) return false;
        return operation != null ? operation.equals(meterData.operation) : meterData.operation == null;
    }

    @Override
    public int hashCode() {
        int result = category != null ? category.hashCode() : 0;
        result = 31 * result + (operation != null ? operation.hashCode() : 0);
        result = 31 * result + (int) (position ^ (position >>> 32));
        result = 31 * result + (sessionUuid != null ? sessionUuid.hashCode() : 0);
        return result;
    }

    @Override
    public void writeJson5Impl(final StringBuilder sb) {
        super.writeJson5Impl(sb);
        if (description != null) {
            sb.append(String.format(",%s:%s", PROP_DESCRIPTION, description));
        }
        if (rejectPath != null) {
            sb.append(String.format(",%s:%s",PROP_REJECT_ID, rejectPath));
        }
        if (okPath != null) {
            sb.append(String.format(",%s:%s",PROP_PATH_ID, okPath));
        }
        if (failPath != null) {
            sb.append(String.format(",%s:%s",PROP_FAIL_ID, failPath, failMessage != null ? failMessage : ""));
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
