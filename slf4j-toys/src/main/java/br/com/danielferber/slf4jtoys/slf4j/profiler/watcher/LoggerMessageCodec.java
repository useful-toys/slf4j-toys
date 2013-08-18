/*
 * Copyright 2012 Daniel Felix Ferber
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
package br.com.danielferber.slf4jtoys.slf4j.profiler.watcher;

import br.com.danielferber.slf4jtoys.slf4j.profiler.logcodec.MessageReader;
import br.com.danielferber.slf4jtoys.slf4j.profiler.logcodec.MessageWriter;
import br.com.danielferber.slf4jtoys.slf4j.profiler.status.SystemStatusData;
import java.io.IOException;

/**
 *
 * @author Daniel Felix Ferber
 */
public class LoggerMessageCodec extends br.com.danielferber.slf4jtoys.slf4j.profiler.status.LoggerMessageCodec<WatcherEvent> {

    private static final String COUNTER = "c";
    private static final String UUID = "u";
    private static final String TIME = "t";

    public LoggerMessageCodec() {
        super('W');
    }

    public void writeProperties(MessageWriter w, WatcherEvent e) {
        /* uuid */
        if (e.uuid != null) {
            w.property(UUID, e.uuid);
        }

        /* counter */
        if (e.counter > 0) {
            w.property(COUNTER, e.counter);
        }

        /* time */
        if (e.time > 0) {
            w.property(TIME, e.time);
        }

        super.writeProperties(w, e);
    }

    @Override
    protected boolean readProperty(MessageReader p, String propertyName, WatcherEvent e) throws IOException {
        if (COUNTER.equals(propertyName)) {
            e.counter = p.readLong();
            return true;
        } else if (UUID.equals(propertyName)) {
            e.uuid = p.readString();
            return true;
        } else if (TIME.equals(propertyName)) {
            e.time = p.readLong();
            return true;
        }
        return super.readProperty(p, propertyName, e);
    }
}
