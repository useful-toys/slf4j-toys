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
 * A simple servlet that responds to GET requests by invoking the default watcher to report the system status.
 * This servlet can be bound to a specific URL and triggered periodically, for example, by a CRON job.
 *
 * <p>Usage: Bind this servlet to a URL in your web application configuration, and ensure the URL is called
 * periodically to log the current system status.
 *
 * @author Daniel Felix Ferber
 */
public class WatcherServlet extends HttpServlet {

    private static final long serialVersionUID = 675380685122096016L;

    /**
     * Handles GET requests by invoking the default watcher to log the current system status.
     * Responds with a success or error message depending on the operation result.
     *
     * @param request  The HTTP request object.
     * @param response The HTTP response object.
     */
    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) {
        final Logger logger = LoggerFactory.getLogger(WatcherServlet.class);
        try {
            WatcherSingleton.DEFAULT_WATCHER.run();
            logger.info("WatcherServlet accessed. Logging current system status.");
            response.setContentType("text/plain");
            response.getWriter().write("System status logged successfully.");
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (final Exception e) {
            logger.error("Failed to log system status.", e);
            response.setContentType("text/plain");
            try {
                response.getWriter().write("Failed to log system status.");
            } catch (final Exception ignored) {
                // Ignorar falhas ao escrever na resposta
            }
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
