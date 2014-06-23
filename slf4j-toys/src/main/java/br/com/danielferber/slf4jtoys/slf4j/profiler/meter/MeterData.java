/*
 * Copyright 2013 Daniel Felix Ferber
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package br.com.danielferber.slf4jtoys.slf4j.profiler.meter;

import br.com.danielferber.slf4jtoys.slf4j.profiler.internal.EventData;
import br.com.danielferber.slf4jtoys.slf4j.profiler.internal.EventReader;
import br.com.danielferber.slf4jtoys.slf4j.profiler.internal.EventWriter;
import br.com.danielferber.slf4jtoys.slf4j.profiler.internal.SystemData;
import br.com.danielferber.slf4jtoys.slf4j.utils.UnitFormatter;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

public class MeterData extends SystemData {

    public MeterData() {
        super();
    }
    /**
     * An arbitrary short, human readable message to describe the task being
     * measured.
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
     * How many iteration were run by the operation.
     */
    protected long currentIteration = 0;
    /**
     * How many iteration were expected by the operation.
     */
    protected long expectedIteration = 0;
    /**
     * An arbitrary exception to signal that the task failed.
     */
    protected String exceptionClass = null;
    /**
     * Message of arbitrary exception to signal that the task failed.
     */
    protected String exceptionMessage = null;
    /**
     * If the job completed successfully, as expected (true) or failed (false).
     */
    protected boolean success = false;
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
//    protected int threadDepth;
//    protected long depthCount;
//    protected long depthContext;
    /**
     * Additionala meta data describing the job.
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

    public long getCurrentIterations() {
        return currentIteration;
    }

    public long getExpectedIteration() {
        return expectedIteration;
    }

    public String getExceptionClass() {
        return exceptionClass;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public boolean isSuccess() {
        return success;
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

    public Map<String, String> getContext() {
        if (context == null) {
            return null;
        }
        return Collections.unmodifiableMap(context);
    }

    @Override
    protected void resetImpl() {
        super.resetImpl();
        this.description = null;
        this.createTime = 0;
        this.startTime = 0;
        this.stopTime = 0;
        this.currentIteration = 0;
        this.expectedIteration = 0;
        this.exceptionClass = null;
        this.exceptionMessage = null;
        this.success = false;
        this.threadStartId = 0;
        this.threadStopId = 0;
        this.threadStartName = null;
        this.threadStopName = null;
        this.context = null;
    }

    @Override
    protected boolean isCompletelyEqualsImpl(EventData obj) {
        final MeterData other = (MeterData) obj;
        if ((this.description == null) ? (other.description != null) : !this.description.equals(other.description)) {
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
        if (this.currentIteration != other.currentIteration) {
            return false;
        }
        if (this.expectedIteration != other.expectedIteration) {
            return false;
        }
        if ((this.exceptionClass == null) ? (other.exceptionClass != null) : !this.exceptionClass.equals(other.exceptionClass)) {
            return false;
        }
        if ((this.exceptionMessage == null) ? (other.exceptionMessage != null) : !this.exceptionMessage.equals(other.exceptionMessage)) {
            return false;
        }
        if (this.success != other.success) {
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
    public StringBuilder readableString(StringBuilder buffer) {
        if (stopTime != 0) {
            buffer.append(success ? "OK" : "Failed");
        } else if (startTime != 0 && currentIteration == 0) {
            buffer.append("Started");
        } else if (startTime != 0) {
        	buffer.append("Progress ");
            buffer.append(UnitFormatter.iterations(this.currentIteration));
            if (this.expectedIteration > 0) {
               buffer.append('/');
                buffer.append(UnitFormatter.iterations(this.expectedIteration)); 
            }
        } else {
            buffer.append("Scheduled");
        }
        if (this.description != null) {
            buffer.append(": ");
            buffer.append(this.description);
        } /* else {
         buffer.append(this.eventCategory);
         } */

        if (this.startTime > 0) {
            buffer.append("; ");
            buffer.append(UnitFormatter.nanoseconds(getExecutionTime()));
            if (this.currentIteration > 0) {
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
//        return super.readableString(buffer);
        return buffer;
    }

    public long getExecutionTime() {
        if (stopTime == 0) {
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
        if (currentIteration == 0 || startTime == 0) {
            return 0;
        }
        float executionTimeNS = getExecutionTime();
        if (executionTimeNS == 0) {
            return 0;
        }
        return ((double) this.currentIteration) / executionTimeNS * 1000000000;
    }
    protected static final String DESCRIPTION = "d";
    protected static final String CREATE_TIME = "t0";
    protected static final String START_TIME = "t1";
    protected static final String STOP_TIME = "t2";
    protected static final String CURRENT_ITERATION = "i";
    protected static final String EXPECTED_ITERATION = "ei";
    protected static final String EXCEPTION = "e";
    protected static final String SUCCESS = "ok";
    protected static final String THREAD = "th";
    protected static final String CONTEXT = "ctx";

    @Override
    public void writePropertiesImpl(EventWriter w) {
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
        if (this.currentIteration != 0) {
            w.property(CURRENT_ITERATION, this.currentIteration);
        }
        if (this.expectedIteration != 0) {
            w.property(EXPECTED_ITERATION, this.expectedIteration);
        }

        /* Excetion */
        if (this.exceptionClass != null) {
            w.property(EXCEPTION, this.exceptionClass, this.exceptionMessage != null ? this.exceptionMessage : "");
        }

        w.property(SUCCESS, success);

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
    protected boolean readPropertyImpl(EventReader r, String propertyName) throws IOException {
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
            this.currentIteration = r.readLong();
            return true;
        } else if (EXCEPTION.equals(propertyName)) {
            this.exceptionClass = r.readString();
            this.exceptionMessage = r.readString();
            return true;
        } else if (SUCCESS.equals(propertyName)) {
            this.success = r.readBoolean();
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
