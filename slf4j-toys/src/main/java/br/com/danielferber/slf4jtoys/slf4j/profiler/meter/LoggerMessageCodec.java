/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.danielferber.slf4jtoys.slf4j.profiler.meter;

import br.com.danielferber.slf4jtoys.slf4j.profiler.logcodec.MessageReader;
import br.com.danielferber.slf4jtoys.slf4j.profiler.logcodec.MessageWriter;
import br.com.danielferber.slf4jtoys.slf4j.profiler.watcher.WatcherEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author Daniel Felix Ferber
 */
public class LoggerMessageCodec extends br.com.danielferber.slf4jtoys.slf4j.profiler.status.LoggerMessageCodec<MeterEvent> {

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

    public LoggerMessageCodec() {
        super('M');
    }

    @Override
    public void writeProperties(MessageWriter w, MeterEvent e) {
        /* Session ID */
        if (e.uuid != null) {
            w.property(UUID, e.uuid);
        }

        /* Event counter */
        if (e.counter > 0) {
            w.property(COUNTER, e.counter);
        }

        /* Name and description */
        if (e.name != null) {
            w.property(NAME, e.name);
        }
        if (e.description != null) {
            w.property(DESCRIPTION, e.description);
        }

        /* Create, start, stop time. */
        if (e.createTime != 0) {
            w.property(CREATE_TIME, e.createTime);
        }
        if (e.startTime != 0) {
            w.property(START_TIME, e.startTime);
        }
        if (e.stopTime != 0) {
            w.property(STOP_TIME, e.stopTime);
        }

        /* Excetion */
        if (e.exceptionClass != null) {
            w.property(EXCEPTION, e.exceptionClass.getClass().getName(), e.exceptionMessage != null ? e.exceptionMessage : "");
        }

        /* Thread info */
        if (e.threadStartId != 0 || e.threadStopId != 0 || e.threadStartName != null || e.threadStopName != null) {
            w.property(THREAD,
                    e.threadStartId != 0 ? Long.toString(e.threadStartId) : "",
                    e.threadStartName != null ? e.threadStartName : "",
                    e.threadStopId != 0 ? Long.toString(e.threadStopId) : "",
                    e.threadStopName != null ? e.threadStopName : "");
        }

        /* Context */
        if (e.context != null && !e.context.isEmpty()) {
            w.property(CONTEXT, e.context);
        }

        super.writeProperties(w, e);
    }

    @Override
    protected boolean readProperty(MessageReader p, String propertyName, MeterEvent e) throws IOException {
        if (COUNTER.equals(propertyName)) {
            e.counter = p.readLong();
            return true;
        } else if (UUID.equals(propertyName)) {
            e.uuid = p.readString();
            return true;
        } else if (NAME.equals(propertyName)) {
            e.name = p.readString();
            return true;
        } else if (DESCRIPTION.equals(propertyName)) {
            e.description = p.readQuotedString();
            return true;
        } else if (CREATE_TIME.equals(propertyName)) {
            e.createTime = p.readLong();
            return true;
        } else if (START_TIME.equals(propertyName)) {
            e.startTime = p.readLong();
            return true;
        } else if (STOP_TIME.equals(propertyName)) {
            e.stopTime = p.readLong();
            return true;
        } else if (EXCEPTION.equals(propertyName)) {
            e.exceptionClass = p.readString();
            e.exceptionMessage = p.readString();
            return true;
        } else if (THREAD.equals(propertyName)) {
            e.threadStartId = p.readLongOrZero();
            e.threadStopId = p.readLongOrZero();
            e.threadStartName = p.readString();
            e.threadStopName = p.readString();
            return true;
        } else if (CONTEXT.equals(propertyName)) {
            e.context = p.readMap();
        }
        return super.readProperty(p, propertyName, e);
    }
}