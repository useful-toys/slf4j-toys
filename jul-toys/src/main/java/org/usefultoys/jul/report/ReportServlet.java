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
package org.usefultoys.jul.report;

import java.io.IOException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.logging.Level;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Daniel
 */
public class ReportServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
                Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                while (interfaces.hasMoreElements()) {
                    NetworkInterface nif = interfaces.nextElement();
                    new Reporter().new ReportNetworkInterface(nif).run();
                }
            } catch (SocketException e) {
                new Reporter().getLogger().log(Level.WARNING, "Cannot report interfaces", e);
            }
        }
    }
}
