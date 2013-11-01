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

import br.com.danielferber.slf4jtoys.slf4j.profiler.internal.EventReader;
import br.com.danielferber.slf4jtoys.slf4j.profiler.internal.EventWriter;
import br.com.danielferber.slf4jtoys.slf4j.profiler.internal.SystemData;
import br.com.danielferber.slf4jtoys.slf4j.utils.UnitFormatter;
import java.io.IOException;
import java.util.Map;

public class MeterData extends SystemData {

    protected MeterData() {
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
    protected long iterations = 0;
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

    @Override
    public void reset() {
        super.reset();
        this.createTime = 0;
        this.startTime = 0;
        this.stopTime = 0;
        this.description = null;
        this.exceptionClass = null;
        this.exceptionMessage = null;
        this.success = false;
        this.threadStartId = 0;
        this.threadStopId = 0;
        this.threadStartName = null;
        this.threadStopName = null;
    }

    @Override
    public StringBuilder readableString(StringBuilder buffer) {
        if (stopTime != 0) {
            buffer.append(success ? "OK" : "Failed");
        } else if (startTime != 0) {
            buffer.append("Started");
        } else {
            buffer.append("Scheduled");
        }
        buffer.append(": ");
        if (this.description != null) {
            buffer.append(this.description);
        } else {
            buffer.append(this.eventCategory);
        }
        if (this.startTime > 0) {
            buffer.append("; ");
            buffer.append(UnitFormatter.nanoseconds(getExecutionTime()));
            if (this.iterations > 0) {
                buffer.append("; ");
                buffer.append(UnitFormatter.iterations(this.iterations));
                buffer.append(' ');
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
            buffer.append(UnitFormatter.nanoseconds(this.runtime_usedMemory));

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
        if (iterations == 0 || startTime == 0) {
            return 0;
        }
        float executionTimeNS = getExecutionTime();
        if (executionTimeNS == 0) {
            return 0;
        }
        return ((double) this.iterations) / executionTimeNS * 1000000000;
    }

    protected static final String DESCRIPTION = "d";
    protected static final String CREATE_TIME = "t0";
    protected static final String START_TIME = "t1";
    protected static final String STOP_TIME = "t2";
    protected static final String ITERATIONS = "i";
    protected static final String EXCEPTION = "e";
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
        if (this.iterations != 0) {
            w.property(ITERATIONS, this.iterations);
        }

        /* Excetion */
        if (this.exceptionClass != null) {
            w.property(EXCEPTION, this.exceptionClass.getClass().getName(), this.exceptionMessage != null ? this.exceptionMessage : "");
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
        } else if (ITERATIONS.equals(propertyName)) {
            this.iterations = r.readLong();
            return true;
        } else if (EXCEPTION.equals(propertyName)) {
            this.exceptionClass = r.readString();
            this.exceptionMessage = r.readString();
            return true;
        } else if (THREAD.equals(propertyName)) {
            this.threadStartId = r.readLongOrZero();
            this.threadStopId = r.readLongOrZero();
            this.threadStartName = r.readString();
            this.threadStopName = r.readString();
            return true;
        } else if (CONTEXT.equals(propertyName)) {
            this.context = r.readMap();
        }
        return super.readPropertyImpl(r, propertyName);
    }
}
