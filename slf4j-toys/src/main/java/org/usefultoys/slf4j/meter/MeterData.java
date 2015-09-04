/*
 * Copyright 2015 Daniel Felix Ferber.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.usefultoys.slf4j.meter;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import org.usefultoys.slf4j.ProfilingSession;
import org.usefultoys.slf4j.internal.EventData;
import org.usefultoys.slf4j.internal.EventReader;
import org.usefultoys.slf4j.internal.EventWriter;
import org.usefultoys.slf4j.internal.SystemData;
import org.usefultoys.slf4j.utils.UnitFormatter;

public class MeterData extends SystemData {

    private static final long serialVersionUID = 2L;
    private static final boolean meterPrintCategory = ProfilingSession.readMeterPrintCategoryProperty();

    public MeterData() {
        super();
    }
    
    public static enum Result {
    	OK, BAD, FAIL
    }
    
    /**
     * Execution result.
     */
    protected Result result;
    /**
     * For bad result, the string token that identifies the cause.
     */
    protected String cause;
    /**
     * An arbitrary short, human readable message to describe the task being measured.
     */
    protected String description = null;
    /**
     * When the job was created/scheduled.
     */
    protected long createTime = 0;
    /**
     * When the job started execution.
     */
    protected long startTime = 0;
    /**
     * When the job finished execution.
     */
    protected long stopTime = 0;
    /**
     * How many iterations were run by the operation.
     */
    protected long iteration = 0;
    /**
     * How many iteration were expected by the operation.
     */
    protected long expectedIterations = 0;
    /**
     * For failure result, the exception class that caused the failure.
     */
    protected String exceptionClass = null;
    /**
     * For failure result, the exception message that caused the failure.
     */
    protected String exceptionMessage = null;
    /**
     * Time considered reasonable for successful execution.
     */
    protected long timeLimitNanoseconds = 0;
    /**
     * Thread that notified job start.
     */
    protected long threadStartId = 0;
    /**
     * Thread that notified job stop.
     */
    protected long threadStopId = 0;
    /**
     * Thread that notified job start.
     */
    protected String threadStartName = null;
    /**
     * Thread that notified job stop.
     */
    protected String threadStopName = null;

    /**
     * Additional meta data describing the job.
     */
    protected Map<String, String> context;

    public String getDescription() {
        return description;
    }

    public long getCreateTime() {
        return createTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getStopTime() {
        return stopTime;
    }

    public long getCurrentIteration() {
        return iteration;
    }

    public long getExpectedIteration() {
        return expectedIterations;
    }

    public String getExceptionClass() {
        return exceptionClass;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public boolean isOK() {
        return result == Result.OK;
    }

    public boolean isSuccess() {
        return result == Result.OK;
    }

    public boolean isBad() {
        return result == Result.BAD;
    }

    public boolean isFailure() {
        return result == Result.FAIL;
    }

    public long getTimeLimitNanoseconds() {
        return timeLimitNanoseconds;
    }

    public long getThreadStartId() {
        return threadStartId;
    }

    public long getThreadStopId() {
        return threadStopId;
    }

    public String getThreadStartName() {
        return threadStartName;
    }

    public String getThreadStopName() {
        return threadStopName;
    }

    public Map<String, String> getContext() {
        if (context == null) {
            return null;
        }
        return Collections.unmodifiableMap(context);
    }

    @Override
    protected void resetImpl() {
        super.resetImpl();
        this.result = null;
        this.cause = null;
        this.description = null;
        this.createTime = 0;
        this.startTime = 0;
        this.stopTime = 0;
        this.iteration = 0;
        this.expectedIterations = 0;
        this.exceptionClass = null;
        this.exceptionMessage = null;
        this.timeLimitNanoseconds = 0;
        this.threadStartId = 0;
        this.threadStopId = 0;
        this.threadStartName = null;
        this.threadStopName = null;
        this.context = null;
    }

    @Override
    protected boolean isCompletelyEqualsImpl(final EventData obj) {
        final MeterData other = (MeterData) obj;
        if ((this.description == null) ? (other.description != null) : !this.description.equals(other.description)) {
            return false;
        }
        if ((this.cause == null) ? (other.cause != null) : !this.cause.equals(other.cause)) {
            return false;
        }
        if (this.createTime != other.createTime) {
            return false;
        }
        if (this.startTime != other.startTime) {
            return false;
        }
        if (this.stopTime != other.stopTime) {
            return false;
        }
        if (this.iteration != other.iteration) {
            return false;
        }
        if (this.expectedIterations != other.expectedIterations) {
            return false;
        }
        if ((this.exceptionClass == null) ? (other.exceptionClass != null) : !this.exceptionClass.equals(other.exceptionClass)) {
            return false;
        }
        if ((this.exceptionMessage == null) ? (other.exceptionMessage != null) : !this.exceptionMessage.equals(other.exceptionMessage)) {
            return false;
        }
        if (this.result != other.result) {
            return false;
        }
        if (this.timeLimitNanoseconds != other.timeLimitNanoseconds) {
            return false;
        }
        if (this.threadStartId != other.threadStartId) {
            return false;
        }
        if (this.threadStopId != other.threadStopId) {
            return false;
        }
        if ((this.threadStartName == null) ? (other.threadStartName != null) : !this.threadStartName.equals(other.threadStartName)) {
            return false;
        }
        if ((this.threadStopName == null) ? (other.threadStopName != null) : !this.threadStopName.equals(other.threadStopName)) {
            return false;
        }
        if (this.context != other.context && (this.context == null || !this.context.equals(other.context))) {
            return false;
        }
        return super.isCompletelyEqualsImpl(obj);
    }

    @Override
    public StringBuilder readableString(final StringBuilder buffer) {
        if (stopTime != 0) {
            if (isOK()) {
                if (isSlow()) {
                    buffer.append("OK (Slow)");
                } else {
                    buffer.append("OK");
                }
            } else if (isBad()) {
                    if (isSlow()) {
                        buffer.append("BAD (Slow)");
                    } else {
                        buffer.append("BAD");
                    }
                    if (cause != null) {
                        buffer.append(" [");
                        buffer.append(cause);
                        buffer.append(']');
                    }
            } else {
                buffer.append("FAIL");
                if (exceptionClass != null || exceptionMessage != null) {
                    buffer.append(" [");
                    if (exceptionClass != null) {
                    	buffer.append(exceptionClass);
                    }
                    if (exceptionClass != null && exceptionMessage != null) {
                        buffer.append("; ");                    	
                    }
                    if (exceptionMessage != null) {
                    	buffer.append(exceptionMessage);
                    }
                    buffer.append(']');
                }
            }
        } else if (startTime != 0 && iteration == 0) {
            buffer.append("Started");
        } else if (startTime != 0) {
            buffer.append("Progress ");
            buffer.append(UnitFormatter.iterations(this.iteration));
            if (this.expectedIterations > 0) {
                buffer.append('/');
                buffer.append(UnitFormatter.iterations(this.expectedIterations));
            }
        } else {
            buffer.append("Scheduled");
        }
        buffer.append(": ");
        if (this.description != null) {
            buffer.append(this.description);
        } else {
            if (eventName == null || meterPrintCategory) {
                final int index = eventCategory.lastIndexOf('.') + 1;
                buffer.append(eventCategory.substring(index));
            }
            if (eventName != null) {
                buffer.append('/');
                buffer.append(eventName);
            }
        }

        if (this.startTime > 0) {
            buffer.append("; ");
            buffer.append(UnitFormatter.nanoseconds(getExecutionTime()));
            if (this.iteration > 0) {
                buffer.append("; ");
                final double iterationsPerSecond = getIterationsPerSecond();
                buffer.append(UnitFormatter.iterationsPerSecond(iterationsPerSecond));
                buffer.append(' ');
                final double nanoSecondsPerIteration = 1.0F / iterationsPerSecond * 1000000000;
                buffer.append(UnitFormatter.nanoseconds(nanoSecondsPerIteration));
            }
        } else {
            buffer.append("; ");
            buffer.append(UnitFormatter.nanoseconds(getWaitingTime()));
        }
        if (this.runtime_usedMemory > 0) {
            buffer.append("; ");
            buffer.append(UnitFormatter.bytes(this.runtime_usedMemory));
        }
        if (context != null) {
            for (final Entry<String, String> entry : context.entrySet()) {
                buffer.append("; ");
                buffer.append(entry.getKey());
                if (entry.getValue() != null) {
                    buffer.append("=");
                    buffer.append(entry.getValue());
                }
            }
        }
        return buffer;
    }

    public boolean isSlow() {
        return timeLimitNanoseconds != 0 && stopTime != 0 && startTime != 0 && stopTime - startTime > timeLimitNanoseconds;
    }

    public long getExecutionTime() {
        if (startTime == 0) {
            return 0;
        } else if (stopTime == 0) {
            return System.nanoTime() - startTime;
        }
        return stopTime - startTime;
    }

    public long getWaitingTime() {
        if (startTime == 0) {
            return System.nanoTime() - createTime;
        }
        return startTime - createTime;
    }

    public double getIterationsPerSecond() {
        if (iteration == 0 || startTime == 0) {
            return 0;
        }
        final float executionTimeNS = getExecutionTime();
        if (executionTimeNS == 0) {
            return 0;
        }
        return ((double) this.iteration) / executionTimeNS * 1000000000;
    }
    protected static final String DESCRIPTION = "d";
    protected static final String CREATE_TIME = "t0";
    protected static final String START_TIME = "t1";
    protected static final String STOP_TIME = "t2";
    protected static final String CURRENT_ITERATION = "i";
    protected static final String EXPECTED_ITERATION = "ei";
    protected static final String RESULT = "r";
    protected static final String EXCEPTION = "re";
    protected static final String CAUSE= "rc";
    protected static final String LIMIT_TIME = "tl";
    protected static final String THREAD = "th";
    protected static final String CONTEXT = "ctx";

    @Override
    public void writePropertiesImpl(final EventWriter w) {
        if (this.result != null) {
        	w.property(RESULT, this.result);
        }
        if (this.description != null) {
            w.property(DESCRIPTION, this.description);
        }

        /* Create, start, stop time. */
        if (this.createTime != 0) {
            w.property(CREATE_TIME, this.createTime);
        }
        if (this.startTime != 0) {
            w.property(START_TIME, this.startTime);
        }
        if (this.stopTime != 0) {
            w.property(STOP_TIME, this.stopTime);
        }
        if (this.iteration != 0) {
            w.property(CURRENT_ITERATION, this.iteration);
        }
        if (this.expectedIterations != 0) {
            w.property(EXPECTED_ITERATION, this.expectedIterations);
        }

        /* Exception and cause */
        if (this.exceptionClass != null) {
            w.property(EXCEPTION, this.exceptionClass, this.exceptionMessage != null ? this.exceptionMessage : "");
        }
        if (this.cause != null) {
            w.property(CAUSE, this.cause);
        }

        if (this.timeLimitNanoseconds != 0) {
            w.property(LIMIT_TIME, timeLimitNanoseconds);
        }

        /* Thread info */
        if (this.threadStartId != 0 || this.threadStopId != 0 || this.threadStartName != null || this.threadStopName != null) {
            w.property(THREAD,
                this.threadStartId != 0 ? Long.toString(this.threadStartId) : "",
                this.threadStartName != null ? this.threadStartName : "",
                this.threadStopId != 0 ? Long.toString(this.threadStopId) : "",
                this.threadStopName != null ? this.threadStopName : "");
        }

        /* Context */
        if (this.context != null && !this.context.isEmpty()) {
            w.property(CONTEXT, this.context);
        }

        super.writePropertiesImpl(w);
    }

    @Override
    protected boolean readPropertyImpl(final EventReader r, final String propertyName) throws IOException {
        if (DESCRIPTION.equals(propertyName)) {
            this.description = r.readString();
            return true;
        } else if (CREATE_TIME.equals(propertyName)) {
            this.createTime = r.readLong();
            return true;
        } else if (START_TIME.equals(propertyName)) {
            this.startTime = r.readLong();
            return true;
        } else if (STOP_TIME.equals(propertyName)) {
            this.stopTime = r.readLong();
            return true;
        } else if (CURRENT_ITERATION.equals(propertyName)) {
            this.iteration = r.readLong();
            return true;
        } else if (EXCEPTION.equals(propertyName)) {
            this.exceptionClass = r.readString();
            this.exceptionMessage = r.readString();
            return true;
        } else if (RESULT.equals(propertyName)) {
            this.result = r.readEnum(Result.class);
            return true;
        } else if (CAUSE.equals(propertyName)) {
            this.cause = r.readString();
            return true;
        } else if (LIMIT_TIME.equals(propertyName)) {
            this.timeLimitNanoseconds = r.readLong();
            return true;
        } else if (THREAD.equals(propertyName)) {
            this.threadStartId = r.readLongOrZero();
            this.threadStartName = r.readString();
            this.threadStopId = r.readLongOrZero();
            this.threadStopName = r.readString();
            return true;
        } else if (CONTEXT.equals(propertyName)) {
            this.context = r.readMap();
            return true;
        }
        return super.readPropertyImpl(r, propertyName);
    }

    void resetBridge() {
        this.reset();
    }
}
