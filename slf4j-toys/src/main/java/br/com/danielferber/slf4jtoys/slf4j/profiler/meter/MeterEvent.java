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
import br.com.danielferber.slf4jtoys.slf4j.profiler.internal.ReadableMessage;
import br.com.danielferber.slf4jtoys.slf4j.profiler.status.SystemStatusEventData;
import java.io.IOException;
import java.util.Map;

public class MeterEvent extends SystemStatusEventData {

    protected MeterEvent() {
        super('M');
    }

    /**
     * Unique ID of session that reporting jobs.
     */
    protected String uuid = null;
    /**
     * How many times this job has benn reported since session creation.
     */
    protected long counter = 0;
    /**
     * An arbitraty ID for the job.
     */
    protected String name = null;
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
        this.uuid = null;
        this.counter = 0;
        this.name = null;
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
        buffer.append(this.name);
        if (this.description != null) {
            buffer.append("; ");
            buffer.append(this.description);
        }
        if (this.startTime > 0) {
            buffer.append("; ");
            buffer.append(ReadableMessage.bestUnit(getExecutionTime(), ReadableMessage.TIME_UNITS, ReadableMessage.TIME_FACTORS));
        } else {
            buffer.append("; ");
            buffer.append(ReadableMessage.bestUnit(getWaitingTime(), ReadableMessage.TIME_UNITS, ReadableMessage.TIME_FACTORS));
        }
        buffer.append("; ");
        return super.readableString(buffer);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (counter ^ (counter >>> 32));
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MeterEvent other = (MeterEvent) obj;
        if (counter != other.counter) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (uuid == null) {
            if (other.uuid != null) {
                return false;
            }
        } else if (!uuid.equals(other.uuid)) {
            return false;
        }
        return true;
    }

    public long getExecutionTime() {
        if (startTime == 0) {
            return 0;
        }
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

    @Override
    public String toString() {
        return this.uuid + ":" + this.name + ":" + this.counter;
    }

    protected static final String UUID = "uuid";
    protected static final String COUNTER = "c";
    protected static final String NAME = "n";
    protected static final String DESCRIPTION = "d";
    protected static final String CREATE_TIME = "t0";
    protected static final String START_TIME = "t1";
    protected static final String STOP_TIME = "t2";
    protected static final String EXCEPTION = "e";
    protected static final String THREAD = "e";
    protected static final String CONTEXT = "ctx";

    @Override
    public void writeProperties(EventWriter w) {
        /* Session ID */
        if (this.uuid != null) {
            w.property(UUID, this.uuid);
        }

        /* Event counter */
        if (this.counter > 0) {
            w.property(COUNTER, this.counter);
        }

        /* Name and description */
        if (this.name != null) {
            w.property(NAME, this.name);
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

        super.writeProperties(w);
    }

    @Override
    protected boolean readProperty(EventReader r, String propertyName) throws IOException {
        if (COUNTER.equals(propertyName)) {
            this.counter = r.readLong();
            return true;
        } else if (UUID.equals(propertyName)) {
            this.uuid = r.readString();
            return true;
        } else if (NAME.equals(propertyName)) {
            this.name = r.readString();
            return true;
        } else if (DESCRIPTION.equals(propertyName)) {
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
        return super.readProperty(r, propertyName);
    }
}
