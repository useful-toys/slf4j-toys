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

import br.com.danielferber.slf4jtoys.slf4j.profiler.ProfilingSession;
import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;

public class Meter extends MeterData implements Closeable {

    private final Logger logger;
    private static final String NULL_VALUE = "<null>";
    /**
     * How many times each event has been executed.
     */
    private static final ConcurrentMap<String, AtomicLong> eventCounterByName = new ConcurrentHashMap<String, AtomicLong>();
    private long timeLimit = 0;

    public Meter(Logger logger, String name) {
        super();
        this.sessionUuid = ProfilingSession.uuid;
        this.logger = logger;
        this.eventCategory = name;
        eventCounterByName.putIfAbsent(name, new AtomicLong(0));
        this.eventPosition = eventCounterByName.get(name).incrementAndGet();
        this.createTime = System.nanoTime();
    }

    public Logger getLogger() {
        return logger;
    }

    // ========================================================================
    public Meter sub(String name) {
        final Meter m = MeterFactory.getMeter(this.logger.getName() + '.' + name);
        if (this.context != null) {
            m.context = new HashMap<String, String>(this.context);
        }
        return m;
    }

    public Meter limit(long timeLimit) {
        this.timeLimit = timeLimit;
        return this;
    }

    // ========================================================================
    public Meter m(String message) {
        this.description = message;
        return this;
    }

    public Meter m(String message, Object... args) {
        try {
            this.description = String.format(message, args);
        } catch (IllegalFormatException e) {
            logger.warn("Meter.setMessage(...)", e);
        }
        return this;
    }

    public Meter ctx(String name) {
        if (context == null) {
            this.context = new HashMap<String, String>();
        }
        context.put(name, null);
        return this;
    }

    public Meter ctx(String name, int value) {
        if (context == null) {
            this.context = new HashMap<String, String>();
        }
        context.put(name, Integer.toString(value));
        return this;
    }

    public Meter ctx(String name, long value) {
        if (context == null) {
            this.context = new HashMap<String, String>();
        }
        context.put(name, Long.toString(value));
        return this;
    }

    public Meter ctx(String name, boolean value) {
        if (context == null) {
            this.context = new HashMap<String, String>();
        }
        context.put(name, Boolean.toString(value));
        return this;
    }

    public Meter ctx(String name, float value) {
        if (context == null) {
            this.context = new HashMap<String, String>();
        }
        context.put(name, Float.toString(value));
        return this;
    }

    public Meter ctx(String name, double value) {
        if (context == null) {
            this.context = new HashMap<String, String>();
        }
        context.put(name, Double.toString(value));
        return this;
    }

    public Meter ctx(String name, Integer value) {
        if (context == null) {
            this.context = new HashMap<String, String>();
        }
        context.put(name, value == null ? NULL_VALUE : value.toString());
        return this;
    }

    public Meter ctx(String name, Long value) {
        if (context == null) {
            this.context = new HashMap<String, String>();
        }
        context.put(name, value == null ? NULL_VALUE : value.toString());
        return this;
    }

    public Meter ctx(String name, Boolean value) {
        if (context == null) {
            this.context = new HashMap<String, String>();
        }
        context.put(name, value == null ? NULL_VALUE : value.toString());
        return this;
    }

    public Meter ctx(String name, Float value) {
        if (context == null) {
            this.context = new HashMap<String, String>();
        }
        context.put(name, value == null ? NULL_VALUE : value.toString());
        return this;
    }

    public Meter ctx(String name, Double value) {
        if (context == null) {
            this.context = new HashMap<String, String>();
        }
        context.put(name, value == null ? NULL_VALUE : value.toString());
        return this;
    }

    public Meter ctx(String name, Object object) {
        if (context == null) {
            this.context = new HashMap<String, String>();
        }
        context.put(name, object == null ? NULL_VALUE : object.toString());
        return this;
    }

    public Meter ctx(String name, String value) {
        if (context == null) {
            this.context = new HashMap<String, String>();
        }
        context.put(name, value);
        return this;
    }

    public Meter ctx(String name, String format, Object... objects) {
        ctx(name, String.format(format, objects));
        return this;
    }

    public Meter unctx(String name) {
        if (context == null) {
            return this;
        }
        context.remove(name);
        return this;
    }

    public Meter inc() {
        this.iterations++;
        return this;
    }

    public Meter inc(long i) {
        this.iterations += i;
        return this;
    }

    public Meter interations(long i) {
        this.iterations = i;
        return this;
    }

    // ========================================================================
    public Meter start() {
        assert createTime != 0;
        try {
            if (startTime != 0) {
                logger.error(Slf4JMarkers.INCONSISTENT_START, "Inconsistent Meter start()", new Exception("Meter.start(...): startTime != 0"));
            }

            Thread currentThread = Thread.currentThread();
            this.threadStartId = currentThread.getId();
            this.threadStartName = currentThread.getName();
            this.startTime = System.nanoTime();

            if (logger.isDebugEnabled()) {
                collectSystemStatus();
                logger.debug(Slf4JMarkers.MSG_START, readableString(new StringBuilder()).toString());
            }
            if (logger.isTraceEnabled()) {
                logger.trace(Slf4JMarkers.DATA_START, write(new StringBuilder(), 'M').toString());
            }

        } catch (Exception t) {
            logger.error("Exception thrown in Meter", t);
        }
        return this;
    }

    // ========================================================================
    public Meter ok() {
        assert createTime != 0;
        try {
            if (stopTime != 0) {
                logger.error(Slf4JMarkers.INCONSISTENT_OK, "Inconsistent Meter)", new Exception("Meter.okImpl(...): stopTime != 0"));
            }
            stopTime = System.nanoTime();
            if (startTime == 0) {
                logger.error(Slf4JMarkers.INCONSISTENT_OK, "Inconsistent Meter", new Exception("Meter.okImpl(...): startTime == 0"));
            }
            success = true;

            Thread currentThread = Thread.currentThread();
            this.threadStopId = currentThread.getId();
            this.threadStopName = currentThread.getName();

            if (logger.isInfoEnabled()) {
                collectSystemStatus();
                logger.info(Slf4JMarkers.MSG_OK, readableString(new StringBuilder()).toString());
            }
            if (startTime != 0 && timeLimit != 0 && stopTime - startTime > timeLimit) {
                logger.trace(Slf4JMarkers.DATA_SLOW_OK, write(new StringBuilder(), 'M').toString());
            } else if (logger.isTraceEnabled()) {
                logger.trace(Slf4JMarkers.DATA_OK, write(new StringBuilder(), 'M').toString());
            }
        } catch (Exception t) {
            logger.error("Exception thrown in Meter", t);
        }
        return this;
    }

    // ========================================================================
    public Meter fail(Throwable throwable) {
        try {
            assert createTime != 0;
            if (stopTime != 0) {
                logger.error(Slf4JMarkers.INCONSISTENT_FAIL, "Inconsistent Meter", new Exception("Meter.failImpl(...): stopTime != 0"));
            }
            stopTime = System.nanoTime();
            if (startTime == 0) {
                logger.error(Slf4JMarkers.INCONSISTENT_FAIL, "Inconsistent Meter", new Exception("Meter.failImpl(...): startTime == 0"));
            }
            if (throwable != null) {
                exceptionClass = throwable.getClass().getName();
                exceptionMessage = throwable.getLocalizedMessage();
            }
            success = false;

            Thread currentThread = Thread.currentThread();
            this.threadStopId = currentThread.getId();
            this.threadStopName = currentThread.getName();

            if (logger.isWarnEnabled()) {
                collectSystemStatus();
                logger.warn(Slf4JMarkers.MSG_FAIL, readableString(new StringBuilder()).toString());
            }
            if (logger.isTraceEnabled()) {
                logger.trace(Slf4JMarkers.DATA_FAIL, write(new StringBuilder(), 'M').toString());
            }
        } catch (Exception t) {
            logger.error("Exception thrown in Meter", t);
        }
        return this;
    }

    // ========================================================================
    @Override
    protected void finalize() throws Throwable {
        if (stopTime == 0) {
            logger.error(Slf4JMarkers.INCONSISTENT_FINALIZED, "Inconsistent Meter", new Exception("Meter.finalize(...): stopTime == 0"));
        }
        super.finalize();
    }

    public void close() throws IOException {
        if (stopTime == 0) {
            fail(null);
        }
    }
}
