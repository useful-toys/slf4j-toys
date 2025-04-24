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

import lombok.NonNull;
import org.slf4j.Logger;
import org.usefultoys.slf4j.LoggerFactory;

import java.io.Serializable;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.concurrent.Executor;

/**
 * Produces diagnostic reports about system resources and the current runtime environment.
 * <p>
 * Reports are logged as information-level messages using SLF4J. The set of reports to be generated is controlled by
 * {@link ReporterConfig}, including which sections are enabled and the default logger name (unless overridden).
 *
 * <p>This is useful for troubleshooting or recording system configuration at application startup or runtime.</p>
 *
 * @author Daniel Felix Ferber
 */
@SuppressWarnings("NonConstantLogger")
public class Reporter implements Serializable {

    /**
     * Logger that prints reports as information messages.
     */
    private final @NonNull Logger logger;

    private static final long serialVersionUID = 1L;

    /**
     * An {@link Executor} that runs tasks synchronously on the current thread. Useful for environments where
     * multithreading is restricted or undesired.
     */
    public static final Executor sameThreadExecutor = new Executor() {
        @Override
        public void execute(final Runnable command) {
            command.run();
        }
    };

    /**
     * Runs the default report on the current thread using {@link #sameThreadExecutor}.
     * <p>
     * Intended for simple applications or environments where blocking the current thread is acceptable. May not be
     * suitable for JavaEE or reactive environments that restrict long-running tasks on request threads.
     */
    public static void runDefaultReport() {
        new Reporter().logDefaultReports(sameThreadExecutor);
    }

    /**
     * Creates a new {@code Reporter} using the logger defined by {@link ReporterConfig#name}.
     */
    public Reporter() {
        logger = LoggerFactory.getLogger(ReporterConfig.name);
    }

    /**
     * Creates a new {@code Reporter} instance that logs messages to the specified {@link Logger}.
     *
     * @param logger the SLF4J logger to use for reporting
     */
    public Reporter(final Logger logger) {
        this.logger = logger;
    }

    /**
     * Runs only the reports that are enabled in {@link ReporterConfig} and logs them as information-level messages.
     *
     * <p>Each enabled report is executed via the provided {@link Executor}.</p>
     *
     * @param executor the executor used to run each report
     */
    public void logDefaultReports(final @NonNull Executor executor) {
        if (ReporterConfig.reportPhysicalSystem) {
            executor.execute(new ReportPhysicalSystem(logger));
        }
        if (ReporterConfig.reportOperatingSystem) {
            executor.execute(new ReportOperatingSystem(logger));
        }
        if (ReporterConfig.reportUser) {
            executor.execute(new ReportUser(logger));
        }
        if (ReporterConfig.reportVM) {
            executor.execute(new ReportVM(logger));
        }
        if (ReporterConfig.reportMemory) {
            executor.execute(new ReportMemory(logger));
        }
        if (ReporterConfig.reportEnvironment) {
            executor.execute(new ReportSystemEnvironment(logger));
        }
        if (ReporterConfig.reportProperties) {
            executor.execute(new ReportSystemProperties(logger));
        }
        if (ReporterConfig.reportFileSystem) {
            executor.execute(new ReportFileSystem(logger));
        }
        if (ReporterConfig.reportCalendar) {
            executor.execute(new ReportCalendar(logger));
        }
        if (ReporterConfig.reportLocale) {
            executor.execute(new ReportLocale(logger));
        }
        if (ReporterConfig.reportCharset) {
            executor.execute(new ReportCharset(logger));
        }
        if (ReporterConfig.reportNetworkInterface) {
            try {
                final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                while (interfaces.hasMoreElements()) {
                    final NetworkInterface nif = interfaces.nextElement();
                    executor.execute(new ReportNetworkInterface(logger, nif));
                }
            } catch (final SocketException e) {
                logger.warn("Cannot report interfaces", e);
            }
        }
        if (ReporterConfig.reportSSLContext) {
            executor.execute(new ReportSSLContext(logger));
        }
        if (ReporterConfig.reportDefaultTrustKeyStore) {
            executor.execute(new ReportDefaultTrustKeyStore(logger));
        }
    }
}
