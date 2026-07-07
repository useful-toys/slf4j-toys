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

/**
 * This class is identical to {@link WatcherServlet} but uses the javax.servlet API
 * instead of the jakarta.servlet API.
 *
 * @see WatcherServlet
 * @see Watcher
 * @see WatcherConfig
 */
public class WatcherJavaxServlet extends HttpServlet {

    private static final long serialVersionUID = 675380685122096016L;

    /**
     * The watcher instance owned by this servlet. It is created during {@link #init(ServletConfig)}
     * and captures the effective name and {@link WatcherConfig} settings at that moment.
     * Marked {@code transient} so servlet serialization does not attempt to serialize it.
     */
    private transient Watcher watcher;

    /**
     * Private lock used to serialize concurrent calls to {@link #runWatcher()}. The servlet container
     * may invoke {@code doGet} concurrently on the same instance; this lock ensures that the
     * underlying {@link Watcher#run()} is not executed concurrently, avoiding the race condition
     * described in the watcher pull path.
     */
    private final Object watcherLock = new Object();

    /**
     * Initializes the servlet and creates the watcher instance owned by this servlet.
     * <p>
     * If the servlet configuration provides the {@code slf4jtoys.watcher.name} init-param, its value
     * is used as the watcher name; otherwise {@link WatcherConfig#name} is used. The watcher logger
     * prefixes/suffixes and data logger flag are read from {@link WatcherConfig} at construction
     * time.
     *
     * @param config The servlet configuration.
     * @throws ServletException if initialization fails.
     */
    @Override
    public void init(final ServletConfig config) throws ServletException {
        super.init(config);
        final Logger logger = LoggerFactory.getLogger(WatcherJavaxServlet.class);
        final String configuredName = config.getInitParameter(WatcherConfig.PROP_NAME);
        final String watcherName = (configuredName == null || configuredName.trim().isEmpty())
                ? WatcherConfig.name
                : configuredName;
        this.watcher = new Watcher(watcherName);
        logger.info("WatcherJavaxServlet initialized with watcher name '{}'.", watcherName);
    }

    /**
     * Handles GET requests by invoking the watcher to log the current runtime state.
     * It responds with a success or error message depending on the outcome.
     *
     * @param request  The HTTP request.
     * @param response The HTTP response.
     */
    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) {
        final Logger logger = LoggerFactory.getLogger(WatcherJavaxServlet.class);
        try {
            runWatcher();
            logger.info("WatcherJavaxServlet accessed. Logging current runtime state.");
            response.setContentType("text/plain");
            response.getWriter().write("Runtime state logged successfully.");
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (final Exception e) {
            logger.error("Failed to log runtime state.", e);
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
     * Calls are serialized with an internal lock so that concurrent HTTP requests do not execute
     * {@link Watcher#run()} concurrently on the same instance. Subclasses may override this method,
     * but should preserve the thread-safety contract if the same {@link Watcher} instance is reused.
     */
    protected void runWatcher() {
        synchronized (watcherLock) {
            watcher.run();
        }
    }
}
