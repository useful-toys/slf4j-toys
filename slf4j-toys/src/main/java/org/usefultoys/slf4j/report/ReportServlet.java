/*
 * Copyright 2019 Daniel Felix Ferber
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

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * A servlet that logs a report according to the URL suffix.
 * 
 * @author Daniel Felix Ferber
 */
public class ReportServlet extends HttpServlet {

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) {
        final String pathinfo = request.getPathInfo();
        if ("/VM".equalsIgnoreCase(pathinfo)) {
            new Reporter().new ReportVM().run();
        }
        if ("/FileSystem".equalsIgnoreCase(pathinfo)) {
            new Reporter().new ReportFileSystem().run();
        }
        if ("/Memory".equalsIgnoreCase(pathinfo)) {
            new Reporter().new ReportMemory().run();
        }
        if ("/User".equalsIgnoreCase(pathinfo)) {
            new Reporter().new ReportUser().run();
        }
        if ("/PhysicalSystem".equalsIgnoreCase(pathinfo)) {
            new Reporter().new ReportPhysicalSystem().run();
        }
        if ("/OperatingSystem".equalsIgnoreCase(pathinfo)) {
            new Reporter().new ReportOperatingSystem().run();
        }
        if ("/Calendar".equalsIgnoreCase(pathinfo)) {
            new Reporter().new ReportCalendar().run();
        }
        if ("/Locale".equalsIgnoreCase(pathinfo)) {
            new Reporter().new ReportLocale().run();
        }
        if ("/Charset".equalsIgnoreCase(pathinfo)) {
            new Reporter().new ReportCharset().run();
        }
        if ("/NetworkInterface".equalsIgnoreCase(pathinfo)) {
            try {
                final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                while (interfaces.hasMoreElements()) {
                    final NetworkInterface nif = interfaces.nextElement();
                    new Reporter().new ReportNetworkInterface(nif).run();
                }
            } catch (final SocketException e) {
                new Reporter().getLogger().warn("Cannot report interfaces", e);
            }
        }
    }
}
