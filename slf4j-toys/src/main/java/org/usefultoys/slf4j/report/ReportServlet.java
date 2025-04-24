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

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Locale;

/**
 * A servlet that logs system reports based on the URL path provided in the HTTP request.
 * <p>
 * This servlet handles HTTP GET requests and triggers the appropriate {@link Reporter} module based on the path suffix. The corresponding system report is
 * logged using the SLF4J logger.
 * <p>
 * The following path suffixes are supported:
 * <ul>
 *   <li>{@code /VM} — Logs Java Virtual Machine information.</li>
 *   <li>{@code /FileSystem} — Logs information about available and used disk space.</li>
 *   <li>{@code /Memory} — Logs memory usage details.</li>
 *   <li>{@code /User} — Logs information about the current user.</li>
 *   <li>{@code /PhysicalSystem} — Logs physical hardware information.</li>
 *   <li>{@code /OperatingSystem} — Logs operating system details.</li>
 *   <li>{@code /Calendar} — Logs date, time, and timezone information.</li>
 *   <li>{@code /Locale} — Logs current and available locale settings.</li>
 *   <li>{@code /Charset} — Logs current and available character sets.</li>
 *   <li>{@code /NetworkInterface} — Logs information for each available network interface.</li>
 * </ul>
 * <p>
 * If the path does not match any known suffix, no action is taken and the request is silently ignored.
 * <p>
 * <strong>Security considerations</strong>
 *  <p>This servlet accesses and logs potentially sensitive system information. Therefore, the following security measures are strongly recommended:</p>
 *  <ul>
 *    <li><strong>Restrict access</strong> to the servlet endpoint using authentication or IP whitelisting.</li>
 *    <li><strong>Avoid exposing this servlet</strong> in production environments or public networks without proper protection.</li>
 *    <li>Ensure that <strong>reporting environment variables or system properties</strong> is disabled unless explicitly needed, to avoid leaking secrets like passwords, API keys, or tokens.</li>
 *    <li>Use a <strong>read-only, non-sensitive logger configuration</strong> to avoid side effects.</li>
 *    <li><strong>Use a secure logging strategy</strong> — logged reports may contain sensitive data and should not be publicly accessible or retained longer than necessary.</li>
 *    <li><strong>Be aware of log injection and log disclosure risks</strong>: an attacker could trigger this servlet and indirectly cause logs to capture sensitive or excessive system information. Limit logging verbosity and protect log files appropriately.</li>
 *   <li><strong>Protect against denial-of-service (DoS) attacks</strong>: repeated requests to expensive endpoints such as {@code /NetworkInterface} can overload the system or exhaust resources. Consider adding rate limiting or caching to mitigate abuse.</li>
 *  </ul>
 *
 * @author Daniel Felix Ferber
 */
@SuppressWarnings("ClassWithMultipleLoggers")
@Slf4j
public class ReportServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) {
        String pathinfo = request.getPathInfo();

        if (pathinfo == null) {
            log.warn("No report path provided.");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType("text/plain");
            try {
                response.getWriter().write("No report path provided.");
            } catch (final Exception ignored) {
                // no-op
            }
            return;
        }

        // Sanitize and normalize
        pathinfo = pathinfo.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z]", "");

        final Logger logger = LoggerFactory.getLogger(ReporterConfig.name);
        if ("vm".equalsIgnoreCase(pathinfo)) {
            new ReportVM(logger).run();
        } else if ("filesystem".equalsIgnoreCase(pathinfo)) {
            new ReportFileSystem(logger).run();
        } else if ("memory".equalsIgnoreCase(pathinfo)) {
            new ReportMemory(logger).run();
        } else if ("user".equalsIgnoreCase(pathinfo)) {
            new ReportUser(logger).run();
        } else if ("physicalsystem".equalsIgnoreCase(pathinfo)) {
            new ReportPhysicalSystem(logger).run();
        } else if ("operatingsystem".equalsIgnoreCase(pathinfo)) {
            new ReportOperatingSystem(logger).run();
        } else if ("calendar".equalsIgnoreCase(pathinfo)) {
            new ReportCalendar(logger).run();
        } else if ("locale".equalsIgnoreCase(pathinfo)) {
            new ReportLocale(logger).run();
        } else if ("charset".equalsIgnoreCase(pathinfo)) {
            new ReportCharset(logger).run();
        } else if ("networkinterface".equalsIgnoreCase(pathinfo)) {
            try {
                final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                while (interfaces.hasMoreElements()) {
                    final NetworkInterface nif = interfaces.nextElement();
                    new ReportNetworkInterface(logger, nif).run();
                }
            } catch (final SocketException e) {
                log.warn("Cannot report network interface", e);
            }
        } else if ("sslcontext".equalsIgnoreCase(pathinfo)) {
            new ReportSSLContext(logger).run();
        } else if ("defaulttrustkeystore".equalsIgnoreCase(pathinfo)) {
            new ReportDefaultTrustKeyStore(logger).run();
        } else {
            log.warn("Unrecognized report path: {}", pathinfo);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType("text/plain");
            try {
                response.getWriter().write(String.format("Unknown report path: %s", pathinfo));
            } catch (final Exception ignored) {
                // no-op
            }
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/plain");
        try {
            response.getWriter().write(String.format("Report logged for: %s", pathinfo));
        } catch (final Exception ignored) {
            // no-op
        }
    }
}
