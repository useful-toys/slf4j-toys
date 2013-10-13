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

public class Meter extends MeterEvent implements Closeable {

    private final Logger logger;
    /**
     * How many times each event has been executed.
     */
    private static final ConcurrentMap<String, AtomicLong> eventCounterByName = new ConcurrentHashMap<String, AtomicLong>();
    private long timeLimit = 0;

    public Meter(Logger logger, String name) {
        super();
        this.name = name;
        this.logger = logger;
        this.uuid = ProfilingSession.uuid;
        eventCounterByName.putIfAbsent(name, new AtomicLong(0));
        this.counter = eventCounterByName.get(name).incrementAndGet();
        createTime = System.nanoTime();
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

    public Meter ctx(String name, String value) {
        if (context == null) {
            this.context = new HashMap<String, String>();
        }
        context.put(name, value);
        return this;
    }

    public Meter unctx(String name) {
        if (context == null) {
            return this;
        }
        context.remove(name);
        return this;
    }

    // ========================================================================
    public Meter start() {
        return startImpl(null, null);
    }

    public Meter start(String name) {
        return startImpl(name, null);
    }

    public Meter start(String name, String value) {
        return startImpl(name, value);
    }

    protected Meter startImpl(String name, String value) {
        assert createTime != 0;
        try {
            if (startTime != 0) {
                logger.error(Slf4JMarkers.INCONSISTENT_START, "Inconsistent Meter start()", new Exception("Meter.start(...): startTime != 0"));
            }
            if (name != null) {
                ctx(name, value);
            }

            Thread currentThread = Thread.currentThread();
            this.threadStartId = currentThread.getId();
            this.threadStartName = currentThread.getName();
            this.startTime = System.nanoTime();

            if (logger.isDebugEnabled()) {
                collectSystemStatus();
                logger.debug(readableString(new StringBuilder()).toString());
            }
            if (logger.isTraceEnabled()) {
                logger.trace(Slf4JMarkers.START, write(new StringBuilder()).toString());
            }

        } catch (Throwable t) {
            logger.error("Excetion thrown in Meter", t);
        }
        return this;
    }

    // ========================================================================
    public Meter ok() {
        return this.ok(null, null);
    }

    public Meter ok(String name) {
        return this.ok(name, null);
    }

    public Meter ok(String name, String value) {
        return okImpl(name, value);
    }

    protected Meter okImpl(String name, String value) {
        assert createTime != 0;
        try {
            if (stopTime != 0) {
                logger.error(Slf4JMarkers.INCONSISTENT_OK, "Inconsistent Meter ok()", new Exception("Meter.stop(...): stopTime != 0"));
            }
            stopTime = System.nanoTime();
            if (startTime == 0) {
                logger.error(Slf4JMarkers.INCONSISTENT_OK, "Inconsistent Meter ok()", new Exception("Meter.stop(...): startTime == 0"));
            }
            if (name != null) {
                ctx(name, value);
            }
            success = true;

            Thread currentThread = Thread.currentThread();
            this.threadStopId = currentThread.getId();
            this.threadStopName = currentThread.getName();

            if (logger.isInfoEnabled()) {
                collectSystemStatus();
                logger.info(readableString(new StringBuilder()).toString());
            }
            if (startTime != 0 && timeLimit != 0 && stopTime - startTime > timeLimit) {
                logger.trace(Slf4JMarkers.SLOW_OK, write(new StringBuilder()).toString());
            } else if (logger.isTraceEnabled()) {
                logger.trace(Slf4JMarkers.OK, write(new StringBuilder()).toString());
            }
        } catch (Throwable t) {
            logger.error("Excetion thrown in Meter", t);
        }
        return this;
    }

    // ========================================================================
    public Meter fail(Throwable throwable) {
        return this.fail(throwable, null, null);
    }

    public Meter fail(Throwable throwable, String name) {
        return this.fail(throwable, name, null);
    }

    public Meter fail(Throwable throwable, String name, String value) {
        return failImpl(throwable, name, value);
    }

    protected Meter failImpl(Throwable throwable, String name, String value) {
        try {
            assert createTime != 0;
            if (stopTime != 0) {
                logger.error(Slf4JMarkers.INCONSISTENT_FAIL, "Inconsistent Meter", new Exception("Meter.stop(...): stopTime != 0"));
            }
            stopTime = System.nanoTime();
            if (startTime == 0) {
                logger.error(Slf4JMarkers.INCONSISTENT_FAIL, "Inconsistent Meter", new Exception("Meter.stop(...): startTime == 0"));
            }
            if (name != null) {
                context.put(name, value);
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
                logger.warn(readableString(new StringBuilder()).toString());
            }
            if (logger.isTraceEnabled()) {
                logger.trace(Slf4JMarkers.FAIL, write(new StringBuilder()).toString());
            }
        } catch (Throwable t) {
            logger.error("Excetion thrown in Meter", t);
        }
        return this;
    }

    // ========================================================================
    @Override
    protected void finalize() throws Throwable {
        if (stopTime == 0) {
            failImpl(null, null, null);
        }
        super.finalize();
    }

    public void close() throws IOException {
        if (stopTime == 0) {
            failImpl(null, null, null);
        }
    }
}
