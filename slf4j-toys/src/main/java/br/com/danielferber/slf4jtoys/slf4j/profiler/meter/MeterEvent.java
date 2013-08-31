/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.danielferber.slf4jtoys.slf4j.profiler.meter;

import br.com.danielferber.slf4jtoys.slf4j.profiler.internal.ReadableMessage;
import br.com.danielferber.slf4jtoys.slf4j.profiler.status.SystemStatusData;
import java.util.Map;

public class MeterEvent extends SystemStatusData {

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
}
