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

import br.com.danielferber.slf4jtoys.slf4j.profiler.internal.Parser;
import br.com.danielferber.slf4jtoys.slf4j.profiler.internal.Session;
import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class Watcher extends WatcherEvent {
    private final Logger logger;
    private final WatcherTask watcherTask;
    public static final Marker WATCHER_MARKER = MarkerFactory.getMarker("WATCHER");
    /**
     * Configuração padrão do parser usado para ler novamente a mensagem do log.
     */
    private static final Parser parser = new Parser();

    public class WatcherTask extends TimerTask {

        @Override
        public void run() {
            Watcher.this.update();

            if (logger.isInfoEnabled()) {
                final StringBuilder buffer = new StringBuilder();
                WatcherEvent.readableString(Watcher.this, buffer);
                logger.info(buffer.toString());
            }
            if (logger.isTraceEnabled()) {
                final StringBuilder buffer = new StringBuilder();
                WatcherEvent.writeToString(Watcher.parser, Watcher.this, buffer);
                logger.trace(Watcher.WATCHER_MARKER, buffer.toString());
            }
        }
    }

    protected Watcher(final Logger logger) {
        super();
        this.logger = logger;
        this.watcherTask = new WatcherTask();
    }

    public Watcher start() {
        try {
            Session.timer.scheduleAtFixedRate(watcherTask, 1000, 1000);
        } catch (IllegalStateException e) {
            /* WatcherTask já estava programada. */
        }
        return this;
    }

    public Watcher stop() {
        watcherTask.cancel();
        return this;
    }

}
