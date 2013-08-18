/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.danielferber.slf4jtoys.slf4j.profiler.meter;

import br.com.danielferber.slf4jtoys.slf4j.profiler.internal.ReadableMessageWriter;
import java.io.Serializable;
import java.util.Map;

public class MeterEvent implements Serializable {


    /**
     * An arbitraty ID for the job.
     */
    protected String name;
    /**
     * How many times the job has executed.
     */
    protected long counter = 0;
    protected String uuid;
    /**
     * When the job was created/scheduled.
     */
    protected long createTime;
    /**
     * When the job started execution.
     */
    protected long startTime = 0;
    /**
     * When the job finished execution.
     */
    protected long stopTime = 0;
    /**
     * An arbitrary short, human readable message to describe the task being
     * measured.
     */
    protected String message;
    /**
     * An arbitrary exception to signal that the task failed.
     */
    protected String exceptionClass;
    protected String exceptionMessage;
    /**
     * If the job completed successfully, as expected (true) or failed (false).
     */
    protected boolean success = false;
    protected long threadStartId;
    protected long threadStopId;
    protected String threadStartName;
    protected String threadStopName;
    protected int threadDepth;
    protected long depthCount;
    protected long depthContext;
    protected Map<String, String> context;

    public static void readableString(MeterEvent meter, StringBuilder buffer) {
        if (meter.message != null) {
            buffer.append(meter.message);
        } else {
            buffer.append(meter.name);
        }
        if (meter.startTime > 0 && meter.stopTime > 0) {
            buffer.append(" ");
            long duration = meter.stopTime - meter.startTime;
            buffer.append(ReadableMessageWriter.bestUnit(duration, ReadableMessageWriter.TIME_UNITS, ReadableMessageWriter.TIME_FACTORS));
        }
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
