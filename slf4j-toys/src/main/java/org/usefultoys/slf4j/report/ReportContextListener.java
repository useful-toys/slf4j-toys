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
package org.usefultoys.slf4j.report;

import javax.servlet.ServletContextEvent;
import java.util.concurrent.Executor;

/**
 * A {@link javax.servlet.ServletContextListener} that triggers diagnostic reports when the web application starts.
 * <p>
 * The reports to be logged are determined by system properties and executed through a simple {@link Executor}. This is useful for logging
 * environment/configuration details at application startup time.
 * <p>
 * To use it, register this listener in <code>web.xml</code.
 *
 * <pre>{@code
 * <listener>
 *     <listener-class>org.usefultoys.slf4j.report.ReportContextListener</listener-class>
 * </listener>
 * }</pre>
 *
 * @author Daniel Felix Ferber
 */
public class ReportContextListener implements javax.servlet.ServletContextListener {

    /**
     * Invoked when the web application is shutting down. No action is taken.
     *
     * @param event the servlet context event
     */
    @Override
    public void contextDestroyed(final ServletContextEvent event) {
        // No cleanup required
    }

    /**
     * Invoked when the web application is starting up. Triggers the {@link Reporter} to log the default reports using a simple synchronous executor.
     *
     * @param event the servlet context event
     */
    @Override
    public void contextInitialized(final ServletContextEvent event) {
        new Reporter().logDefaultReports(Reporter.sameThreadExecutor);
    }
}
