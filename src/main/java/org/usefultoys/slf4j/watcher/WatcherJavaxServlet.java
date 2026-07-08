/*
 * Copyright 2026 Daniel Felix Ferber
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.usefultoys.slf4j.watcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.concurrent.locks.ReentrantLock;

/**
 * This class is identical to {@link WatcherServlet} but uses the javax.servlet API
 * instead of the jakarta.servlet API.
 *
 * <p>Because this servlet owns its own {@link Watcher} instance, it maintains its own internal event
 * {@link org.usefultoys.slf4j.internal.EventData#position position} sequence. If another
 * {@code Watcher} instance with the same name writes to the same logger, the sequences will
 * interleave. Use a unique name for each watcher instance when consumers depend on a single ordered
 * sequence per name.
 *
 * @author Daniel Felix Ferber
 * @author Co-authored-by: Claude Sonnet 5 using claude-sonnet-5
 * @author Co-authored-by: GitHub Copilot using Kimi K2.7 Code
 * @see WatcherServlet
 * @see Watcher
 * @see WatcherConfig
 */
public class WatcherJavaxServlet extends HttpServlet {

    private static final long serialVersionUID = 675380685122096016L;

    /** Logger for servlet lifecycle and request handling messages. */
    private static final Logger LOGGER = LoggerFactory.getLogger(WatcherJavaxServlet.class);

    /** HTTP 429 Too Many Requests, returned when a collection is already in progress on this instance. */
    private static final int HTTP_TOO_MANY_REQUESTS = 429;

    /**
     * The watcher instance owned by this servlet. It is created during {@link #init(ServletConfig)}
     * and captures the effective name and {@link WatcherConfig} settings at that moment.
     * Marked {@code transient} so servlet serialization does not attempt to serialize it.
     */
    private transient Watcher watcher;

    /**
     * Private lock that guards {@link #runWatcher()} against concurrent execution. The servlet
     * container may invoke {@code doGet} concurrently on the same instance; {@link #runWatcher()}
     * uses a non-blocking {@link ReentrantLock#tryLock()} on this lock so that a request arriving
     * while a collection is in progress is skipped instead of queued, avoiding the race condition
     * in the watcher pull path without tying up servlet-container threads.
     */
    private final ReentrantLock watcherLock = new ReentrantLock();

    /**
     * Initializes the servlet and creates the watcher instance owned by this servlet.
     * <p>
     * If the servlet configuration provides the {@code slf4jtoys.watcher.name} init-param, its value
     * is used as the watcher name; otherwise {@link WatcherConfig#name} is used. The watcher logger
     * prefixes/suffixes are read from {@link WatcherConfig} at construction time.
     *
     * @param config The servlet configuration.
     * @throws ServletException if initialization fails.
     */
    @Override
    public void init(final ServletConfig config) throws ServletException {
        super.init(config);
        final String configuredName = config.getInitParameter(WatcherConfig.PROP_NAME);
        final String trimmedName = (configuredName == null) ? null : configuredName.trim();
        final String watcherName = (trimmedName == null || trimmedName.isEmpty())
                ? WatcherConfig.name
                : trimmedName;
        this.watcher = new Watcher(watcherName);
        LOGGER.info("WatcherJavaxServlet initialized with watcher name '{}'.", watcherName);
    }

    /**
     * Handles GET requests by invoking the watcher to log the current runtime state. Responds with
     * {@code 200 OK} when the state was collected, {@code 429 Too Many Requests} when a collection
     * was already in progress on this servlet instance, or {@code 500 Internal Server Error} on
     * failure.
     *
     * @param request  The HTTP request.
     * @param response The HTTP response.
     */
    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) {
        try {
            final boolean collected = runWatcher();
            if (collected) {
                LOGGER.info("WatcherJavaxServlet accessed. Logging current runtime state.");
                response.setContentType("text/plain");
                response.getWriter().write("Runtime state logged successfully.");
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                LOGGER.info("WatcherJavaxServlet accessed while a collection is already in progress. Skipped.");
                response.setContentType("text/plain");
                response.getWriter().write("Runtime state already being collected. Try again later.");
                response.setStatus(HTTP_TOO_MANY_REQUESTS);
            }
        } catch (final Exception e) {
            LOGGER.error("Failed to log runtime state.", e);
            response.setContentType("text/plain");
            try {
                response.getWriter().write("Failed to log runtime state.");
            } catch (final Exception ignored) {
                // Ignore failures when writing to the response
            }
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Invokes the watcher owned by this servlet to collect and report the current runtime state.
     * <p>
     * Uses a non-blocking try-lock on an internal lock so that concurrent HTTP requests do not
     * execute {@link Watcher#run()} concurrently on the same instance. If a collection is already
     * in progress on this servlet instance, this method returns immediately without collecting,
     * so that the calling servlet-container thread is not queued. Subclasses may override this
     * method, but should preserve the thread-safety contract if the same {@link Watcher} instance
     * is reused.
     *
     * @return {@code true} if the watcher was executed; {@code false} if a collection was already
     *         in progress and this call was skipped.
     */
    protected boolean runWatcher() {
        if (!watcherLock.tryLock()) {
            return false;
        }
        try {
            watcher.run();
            return true;
        } finally {
            watcherLock.unlock();
        }
    }
}
