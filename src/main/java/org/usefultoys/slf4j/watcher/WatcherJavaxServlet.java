/*
 * Copyright 2025 Daniel Felix Ferber
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

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This class is identical to {@link WatcherServlet} but uses the javax.servlet API
 * instead of the jakarta.servlet API.
 */
public class WatcherJavaxServlet extends HttpServlet {

    private static final long serialVersionUID = 675380685122096016L;

    /**
     * Handles GET requests by invoking the default watcher to log the current runtime state.
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
     * Invokes the default {@link Watcher} to collect and report the current runtime state.
     * This method can be overridden by subclasses to customize how the watcher is run.
     */
    protected void runWatcher() {
        WatcherSingleton.getDefaultWatcher().run(); // Use the getter
    }
}
