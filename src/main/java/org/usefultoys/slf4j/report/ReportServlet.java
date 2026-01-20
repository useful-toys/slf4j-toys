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
package org.usefultoys.slf4j.report;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Locale;

/**
 * A servlet that triggers and logs system reports based on the URL path provided in the HTTP request.
 * <p>
 * This servlet handles HTTP GET requests and executes the appropriate {@link Reporter} module
 * based on the path suffix. The corresponding system report is logged using an SLF4J logger.
 * <p>
 * The following path suffixes are supported, each triggering a specific report module:
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
 *   <li>{@code /SSLContext} — Logs details about SSL contexts.</li>
 *   <li>{@code /DefaultTrustKeyStore} — Logs information about the default trusted keystore.</li>
 *   <li>{@code /Environment} — Logs environment variables.</li>
 *   <li>{@code /Properties} — Logs system properties.</li>
 *   <li>{@code /JvmArguments} — Logs JVM input arguments.</li>
 *   <li>{@code /Classpath} — Logs classpath entries.</li>
 *   <li>{@code /GarbageCollector} — Logs garbage collector information.</li>
 *   <li>{@code /SecurityProviders} — Logs security providers information.</li>
 *   <li>{@code /ContainerInfo} — Logs container information.</li>
 * </ul>
 * <p>
 * If the path does not match any known suffix, no action is taken and the request is silently ignored.
 * <p>
 * **Security Considerations:**
 * <p>This servlet accesses and logs potentially sensitive system information. Therefore, the following
 * security measures are strongly recommended:
 * <ul>
 *   <li>**Restrict access** to the servlet endpoint using authentication or IP whitelisting.</li>
 *   <li>**Avoid exposing this servlet** in production environments or public networks without proper protection.</li>
 *   <li>Ensure that **reporting environment variables or system properties** is disabled unless explicitly needed,
 *       to avoid leaking secrets like passwords, API keys, or tokens.</li>
 *   <li>Use a **read-only, non-sensitive logger configuration** to avoid side effects.</li>
 *   <li>**Use a secure logging strategy** — logged reports may contain sensitive data and should not be
 *       publicly accessible or retained longer than necessary.</li>
 *   <li>**Be aware of log injection and log disclosure risks**: an attacker could trigger this servlet and
 *       indirectly cause logs to capture sensitive or excessive system information. Limit logging verbosity
 *       and protect log files appropriately.</li>
 *   <li>**Protect against denial-of-service (DoS) attacks**: repeated requests to expensive endpoints such as
 *       {@code /NetworkInterface} can overload the system or exhaust resources. Consider adding rate limiting
 *       or caching to mitigate abuse.</li>
 * </ul>
 *
 * @author Daniel Felix Ferber
 * @see Reporter
 * @see ReporterConfig
 */
@SuppressWarnings("ClassWithMultipleLoggers")
@Slf4j
public class ReportServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /*
     * Provides network interfaces for reporting.
     * Can be overridden in tests to simulate exceptions or control behavior.
     */
    protected NetworkInterfaceProvider networkInterfaceProvider = NetworkInterface::getNetworkInterfaces;

    /**
     * Handles HTTP GET requests by triggering the appropriate report module based on the URL path.
     * The report is logged, and a response indicating success or failure is sent.
     *
     * @param request The HTTP request object.
     * @param response The HTTP response object.
     */
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
                final Enumeration<NetworkInterface> interfaces = this.getNetworkInterfaces();
                while (interfaces.hasMoreElements()) {
                    final NetworkInterface nif = interfaces.nextElement();
                    new ReportNetworkInterface(logger, nif).run();
                }
            } catch (final SocketException e) {
                log.warn("Cannot report network interface: {}", e.getMessage());
            }
        } else if ("sslcontext".equalsIgnoreCase(pathinfo)) {
            new ReportSSLContext(logger).run();
        } else if ("defaulttrustkeystore".equalsIgnoreCase(pathinfo)) {
            new ReportDefaultTrustKeyStore(logger).run();
        } else if ("environment".equalsIgnoreCase(pathinfo)) {
            new ReportSystemEnvironment(logger).run();
        } else if ("properties".equalsIgnoreCase(pathinfo)) {
            new ReportSystemProperties(logger).run();
        } else if ("jvmarguments".equalsIgnoreCase(pathinfo)) {
            new ReportJvmArguments(logger).run();
        } else if ("classpath".equalsIgnoreCase(pathinfo)) {
            new ReportClasspath(logger).run();
        } else if ("garbagecollector".equalsIgnoreCase(pathinfo)) {
            new ReportGarbageCollector(logger).run();
        } else if ("securityproviders".equalsIgnoreCase(pathinfo)) {
            new ReportSecurityProviders(logger).run();
        } else if ("containerinfo".equalsIgnoreCase(pathinfo)) {
            new ReportContainerInfo(logger).run();
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

    /*
     * Retrieves the enumeration of network interfaces.
     * Protected to allow override in tests for dependency injection.
     *
     * @return Enumeration of available network interfaces.
     * @throws SocketException If an I/O error occurs.
     */
    protected Enumeration<NetworkInterface> getNetworkInterfaces() throws SocketException {
        return this.networkInterfaceProvider.getNetworkInterfaces();
    }

    /*
     * Functional interface for providing network interfaces.
     * Allows injection of mock implementations in tests.
     */
    @FunctionalInterface
    protected interface NetworkInterfaceProvider {
        /*
         * Gets the enumeration of network interfaces.
         *
         * @return Enumeration of available network interfaces.
         * @throws SocketException If an I/O error occurs.
         */
        Enumeration<NetworkInterface> getNetworkInterfaces() throws SocketException;
    }
}
