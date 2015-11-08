/*
 * Copyright 2015 Daniel.
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
package org.usefultoys.slf4j;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import static org.usefultoys.slf4j.ProfilingSession.getProperty;

/**
 *
 * @author Daniel
 */
public class ReportServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final Logger logger = LoggerFactory.getLogger(getProperty("report.name", "report"));
        final String pathinfo = request.getPathInfo();
        if ("/VM".equalsIgnoreCase(pathinfo)) {
            new Report(logger).new ReportVM().run();
        }
        if ("/User".equalsIgnoreCase(pathinfo)) {
            new Report(logger).new ReportUser().run();
        }
        if ("/PhysicalSystem".equalsIgnoreCase(pathinfo)) {
            new Report(logger).new ReportPhysicalSystem().run();
        }
        if ("/OperatingSystem".equalsIgnoreCase(pathinfo)) {
            new Report(logger).new ReportOperatingSystem().run();
        }
        if ("/Memory".equalsIgnoreCase(pathinfo)) {
            new Report(logger).new ReportMemory().run();
        }
        if ("/Locale".equalsIgnoreCase(pathinfo)) {
            new Report(logger).new ReportLocale().run();
        }
        if ("/FileSystem".equalsIgnoreCase(pathinfo)) {
            new Report(logger).new ReportFileSystem().run();
        }
        if ("/Calendar".equalsIgnoreCase(pathinfo)) {
            new Report(logger).new ReportCalendar().run();
        }
    }

}
