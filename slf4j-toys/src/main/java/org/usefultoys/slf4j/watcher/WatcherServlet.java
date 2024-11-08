/*
 * Copyright 2024 Daniel Felix Ferber
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.usefultoys.slf4j.watcher;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Simple servlet that reacts to GET methods by invoking the default watcher to report system status. You may bind this servlet to an URL for this special
 * purpose and calls this URK by a CRON job.
 *
 * @author Daniel Felix Ferber
 */
public class WatcherServlet extends HttpServlet {

    private static final long serialVersionUID = 675380685122096016L;

    @SuppressWarnings("MethodMayBeStatic")
    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) {
        WatcherSingleton.DEFAULT_WATCHER.logCurrentStatus();
    }
}
