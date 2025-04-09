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

import org.slf4j.Logger;
import org.usefultoys.slf4j.LoggerFactory;
import org.usefultoys.slf4j.utils.UnitFormatter;

import javax.net.ssl.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.net.*;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.Provider;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.util.*;
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
public class Reporter implements Serializable {

    /**
     * Logger that prints reports as information messages.
     */
    private final Logger logger;

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
     * Returns the SLF4J logger used by this {@code Reporter} instance.
     *
     * @return the logger associated with this reporter
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * Runs only the reports that are enabled in {@link ReporterConfig} and logs them as information-level messages.
     *
     * <p>Each enabled report is executed via the provided {@link Executor}.</p>
     *
     * @param executor the executor used to run each report
     */
    public void logDefaultReports(final Executor executor) {
        if (ReporterConfig.reportPhysicalSystem) {
            executor.execute(this.new ReportPhysicalSystem());
        }
        if (ReporterConfig.reportOperatingSystem) {
            executor.execute(this.new ReportOperatingSystem());
        }
        if (ReporterConfig.reportUser) {
            executor.execute(this.new ReportUser());
        }
        if (ReporterConfig.reportVM) {
            executor.execute(this.new ReportVM());
        }
        if (ReporterConfig.reportMemory) {
            executor.execute(this.new ReportMemory());
        }
        if (ReporterConfig.reportEnvironment) {
            executor.execute(this.new ReportSystemEnvironment());
        }
        if (ReporterConfig.reportProperties) {
            executor.execute(this.new ReportSystemProperties());
        }
        if (ReporterConfig.reportFileSystem) {
            executor.execute(this.new ReportFileSystem());
        }
        if (ReporterConfig.reportCalendar) {
            executor.execute(this.new ReportCalendar());
        }
        if (ReporterConfig.reportLocale) {
            executor.execute(this.new ReportLocale());
        }
        if (ReporterConfig.reportCharset) {
            executor.execute(this.new ReportCharset());
        }
        if (ReporterConfig.reportNetworkInterface) {
            try {
                final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                while (interfaces.hasMoreElements()) {
                    final NetworkInterface nif = interfaces.nextElement();
                    executor.execute(this.new ReportNetworkInterface(nif));
                }
            } catch (final SocketException e) {
                logger.warn("Cannot report interfaces", e);
            }
        }
        if (ReporterConfig.reportSSLContext) {
            executor.execute(this.new ReportSSLContext());
        }
        if (ReporterConfig.reportDefaultTrustKeyStore) {
            executor.execute(this.new ReportDefaultTrustKeyStore());
        }
    }

    /**
     * Reports basic information about the Java Virtual Machine (JVM), including vendor, version, and installation
     * directory.
     */
    public class ReportVM implements Runnable {

        @Override
        public void run() {
            final PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
            ps.println("Java Virtual Machine");
            ps.println(" - vendor: " + System.getProperty("java.vendor"));
            ps.println(" - version: " + System.getProperty("java.version"));
            ps.println(" - installation directory: " + System.getProperty("java.home"));
            ps.close();
        }
    }

    /**
     * Reports information about the file system roots, including total, free, and usable space.
     */
    public class ReportFileSystem implements Runnable {

        @Override
        public void run() {
            final PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
            final File[] roots = File.listRoots();
            boolean first = true;
            for (final File root : roots) {
                if (first) {
                    first = false;
                } else {
                    ps.println();
                }
                ps.println("File system root: " + root.getAbsolutePath());
                ps.println(" - total space: " + UnitFormatter.bytes(root.getTotalSpace()));
                ps.println(" - currently free space: " + UnitFormatter.bytes(root.getFreeSpace()) + " (" + UnitFormatter.bytes(root.getUsableSpace()) + " usable)");
            }
            ps.close();
        }
    }

    /**
     * Reports memory usage of the JVM, including maximum available memory, currently allocated memory, and currently
     * used memory.
     */
    public class ReportMemory implements Runnable {

        @Override
        public void run() {
            final PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
            final Runtime runtime = Runtime.getRuntime();
            ps.println("Memory:");
            final long maxMemory = runtime.maxMemory();
            final long totalMemory = runtime.totalMemory();
            final long freeMemory = runtime.freeMemory();
            ps.println(" - maximum allowed: " + (maxMemory == Long.MAX_VALUE ? "no limit" : UnitFormatter.bytes(maxMemory)));
            ps.println(" - currently allocated: " + UnitFormatter.bytes(totalMemory) + " (" + UnitFormatter.bytes(maxMemory - totalMemory) + " more available)");
            ps.println(" - currently used: " + UnitFormatter.bytes(totalMemory - freeMemory) + " (" + UnitFormatter.bytes(freeMemory) + " free)");
            ps.close();
        }
    }

    /**
     * Reports environment variables available to the current process.
     */
    public class ReportUser implements Runnable {

        @Override
        public void run() {
            final PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
            ps.println("User:");
            ps.println(" - name: " + System.getProperty("user.name"));
            ps.println(" - home: " + System.getProperty("user.home"));
            ps.close();
        }
    }

    /**
     * Reports environment variables available to the current process.
     */
    public class ReportSystemEnvironment implements Runnable {

        @Override
        public void run() {
            final PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
            ps.println("System Environment:");
            final TreeMap<String, String> sortedProperties = new TreeMap<>(System.getenv());
            for (final Map.Entry<String, String> entry : sortedProperties.entrySet()) {
                ps.println(" - " + entry.getKey() + ": " + entry.getValue());
            }
            ps.close();
        }
    }

    /**
     * Reports all Java system properties in sorted order.
     */
    public class ReportSystemProperties implements Runnable {

        @Override
        public void run() {
            final PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
            ps.println("System Properties:");
            final TreeMap<Object, Object> sortedProperties = new TreeMap<>(System.getProperties());
            for (final Map.Entry<Object, Object> entry : sortedProperties.entrySet()) {
                ps.println(" - " + entry.getKey() + ": " + entry.getValue());
            }
            ps.close();
        }
    }

    /**
     * Reports basic information about the physical system, such as the number of available processors.
     */
    public class ReportPhysicalSystem implements Runnable {

        @Override
        public void run() {
            final Runtime runtime = Runtime.getRuntime();
            final PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
            ps.println("Physical system");
            ps.println(" - processors: " + runtime.availableProcessors());
            ps.close();
        }
    }

    /**
     * Reports properties of the operating system, including architecture, name, version, and path/file/line
     * separators.
     */
    public class ReportOperatingSystem implements Runnable {

        @Override
        public void run() {
            final PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
            ps.println("Operating System");
            ps.println(" - architecture: " + System.getProperty("os.arch"));
            ps.println(" - name: " + System.getProperty("os.name"));
            ps.println(" - version: " + System.getProperty("os.version"));
            ps.println(" - file separator: " + Integer.toHexString(System.getProperty("file.separator").charAt(0)));
            ps.println(" - path separator: " + Integer.toHexString(System.getProperty("path.separator").charAt(0)));
            ps.println(" - line separator: " + Integer.toHexString(System.getProperty("line.separator").charAt(0)));
            ps.close();
        }
    }

    /**
     * Reports the current date and time, default time zone configuration, and lists all available time zone IDs.
     */
    @SuppressWarnings("Since15")
    public class ReportCalendar implements Runnable {

        @Override
        public void run() {
            final PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
            ps.println("Calendar");
            ps.print(" - current date/time: " + DateFormat.getDateTimeInstance().format(new Date()));
            final TimeZone tz = TimeZone.getDefault();
            ps.print(" - default timezone: " + tz.getDisplayName());
            ps.print(" (" + tz.getID() + ")");
            ps.print("; DST=" + tz.getDSTSavings() / 60000 + "min");
            try {
                ps.print("; observesDT=" + tz.observesDaylightTime());
            } catch (final NoSuchMethodError ignored) {
                // Ignore property that exists only from Java 1.7 on.
            }
            ps.print("; useDT=" + tz.useDaylightTime());
            ps.print("; inDT=" + tz.inDaylightTime(new Date()));
            ps.print("; offset=" + tz.getRawOffset() / 60000 + "min");
            ps.println();
            ps.print(" - available IDs: ");
            int i = 1;
            for (final String id : TimeZone.getAvailableIDs()) {
                if (i++ % 8 == 0) {
                    ps.print("\n      ");
                }
                ps.print(id + "; ");
            }
            ps.close();
        }
    }

    @SuppressWarnings("Since15")
    /**
     * Reports the default system locale and lists all available locales
     * with their respective language, country, script, and variant.
     */
    public class ReportLocale implements Runnable {

        @Override
        public void run() {
            final PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
            final Locale loc = Locale.getDefault();
            ps.println("Locale");
            ps.print(" - default locale: " + loc.getDisplayName());
            ps.print("; language=" + loc.getDisplayLanguage() + " (" + loc.getLanguage() + ")");
            ps.print("; country=" + loc.getDisplayCountry() + " (" + loc.getCountry() + ")");
            try {
                ps.print("; script=" + loc.getDisplayScript() + " (" + loc.getScript() + ")");
            } catch (final NoSuchMethodError ignored) {
                // Ignore property that exists only from Java 1.7 on.
            }
            ps.print("; variant=" + loc.getDisplayVariant() + " (" + loc.getVariant() + ")");
            ps.println();
            ps.print(" - available locales: ");
            int i = 1;
            for (final Locale l : Locale.getAvailableLocales()) {
                if (i++ % 8 == 0) {
                    ps.print("\n      ");
                }
                ps.print(l.getDisplayName() + "; ");
            }
            ps.close();
        }
    }

    /**
     * Reports the default system locale and lists all available locales with their respective language, country,
     * script, and variant.
     */
    public class ReportCharset implements Runnable {

        @Override
        public void run() {
            final PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
            final Charset charset = Charset.defaultCharset();
            ps.println("Charset");
            ps.print(" - default charset: " + charset.displayName());
            ps.print("; name=" + charset.name());
            ps.print("; canEncode=" + charset.canEncode());
            ps.println();
            ps.print(" - available charsets: ");
            int i = 1;
            for (final Charset l : Charset.availableCharsets().values()) {
                if (i++ % 15 == 0) {
                    ps.print("\n      ");
                }
                ps.print(l.displayName() + "; ");
            }
            ps.close();
        }
    }

    /**
     * Reports details of a specific network interface, including name, MTU, status flags (e.g., loopback, multicast),
     * hardware address, and associated IP addresses.
     */
    public class ReportNetworkInterface implements Runnable {

        private final NetworkInterface nif;

        public ReportNetworkInterface(final NetworkInterface nif) {
            this.nif = nif;
        }

        @Override
        public void run() {
            final PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
            try {
                ps.println("Network Interface " + nif.getName() + ":");
                ps.println(" - display name: " + nif.getDisplayName());
                ps.print(" - properties: ");
                ps.print("mtu=" + nif.getMTU() + "; ");
                if (nif.isLoopback()) {
                    ps.print("loopback; ");
                }
                if (nif.isPointToPoint()) {
                    ps.print("point-to-point; ");
                }
                if (nif.isUp()) {
                    ps.print("UP; ");
                }
                if (nif.isVirtual()) {
                    ps.print("virtual; ");
                }
                if (nif.supportsMulticast()) {
                    ps.print("multicast; ");
                }
                ps.println();
                ps.print(" - hardware address: ");
                final byte[] macAddress = nif.getHardwareAddress();
                if (macAddress == null) {
                    ps.println("n/a");
                } else {
                    for (final byte b : macAddress) {
                        ps.print(String.format("%1$02X ", b));
                    }
                    ps.println();
                }
                final Enumeration<InetAddress> inetAddresses = nif.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    reportNetworkAddress(ps, inetAddresses.nextElement());
                }
            } catch (final IOException e) {
                ps.println("   Cannot read property: " + e.getLocalizedMessage());
            }
            ps.close();
        }

        private void reportNetworkAddress(final PrintStream ps, final InetAddress inetAddress) {
            try {
                if (inetAddress instanceof Inet4Address) {
                    ps.println(" - NET address (IPV4): " + inetAddress.getHostAddress());
                } else if (inetAddress instanceof Inet6Address) {
                    ps.println(" - NET address (IPV6): " + inetAddress.getHostAddress());
                }
                ps.println("      host name: " + inetAddress.getHostName());
                ps.println("      canonical host name : " + inetAddress.getCanonicalHostName());
                ps.print("      properties: ");
                if (inetAddress.isLoopbackAddress()) {
                    ps.print("loopback; ");
                }
                if (inetAddress.isSiteLocalAddress()) {
                    ps.print("site-local; ");
                }
                if (inetAddress.isAnyLocalAddress()) {
                    ps.print("any-local; ");
                }
                if (inetAddress.isLinkLocalAddress()) {
                    ps.print("link-local; ");
                }
                if (inetAddress.isMulticastAddress()) {
                    ps.print("multicast; ");
                }
                if (inetAddress.isReachable(5000)) {
                    ps.print("reachable; ");
                }
                ps.println();
            } catch (final IOException e) {
                ps.println("   Cannot read property: " + e.getLocalizedMessage());
            }
        }
    }

    /**
     * Reports details of SSL contexts supported by the JVM, including cipher suites, protocols, and supported SSL
     * parameters for each context.
     */
    public class ReportSSLContext implements Runnable {
        final String contextNames[] = {
                "Default", "SSL", "SSLv2", "SSLv3", "TLS", "TLSv1", "TLSv1.1", "TLSv1.2"
        };

        private void printList(final PrintStream ps, final String[] list, final String newLineSpace) {
            int i = 1;
            for (final String s : list) {
                if (i++ % 10 == 0) {
                    ps.println();
                    ps.print(newLineSpace);
                }
                ps.print(s);
                ps.print("; ");
            }
            ps.println();
        }

        @Override
        public void run() {
            for (final String contextName : contextNames) {
                final PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
                ps.println("SSL Context " + contextName);

                try {
                    SSLContext sslContext = SSLContext.getInstance(contextName);
                    ps.println("   Protocol: " + sslContext.getProtocol());
                    ps.println("   Class: " + sslContext.getClass());
                    ps.println("   Provider:");
                    final Provider provider = sslContext.getProvider();
                    final TreeMap<Object, Object> sortedProperties = new TreeMap<>(provider);
                    for (Map.Entry<Object, Object> entry : sortedProperties.entrySet()) {
                        ps.println("    - " + entry.getKey() + ": " + entry.getValue());
                    }
                    ps.println("   SocketFactory: ");
                    ps.print("      Default Cipher Suites:");
                    printList(ps, sslContext.getSocketFactory().getDefaultCipherSuites(), "          ");
                    ps.print("      Supported Cipher Suites: ");
                    printList(ps, sslContext.getSocketFactory().getSupportedCipherSuites(), "          ");
                    ps.println("   ServerSocketFactory: ");
                    ps.print("      Default Cipher Suites:");
                    printList(ps, sslContext.getServerSocketFactory().getDefaultCipherSuites(), "          ");
                    ps.print("      Supported Cipher Suites:");
                    printList(ps, sslContext.getServerSocketFactory().getSupportedCipherSuites(), "          ");
                    SSLParameters p = sslContext.getDefaultSSLParameters();
                    ps.println("   Default SSL Parameters:");
                    ps.println("      EndpointIdentificationAlgorithm: " + p.getEndpointIdentificationAlgorithm());
                    ps.println("      Need Client Auth: " + p.getNeedClientAuth());
                    ps.println("      Want Client Auth: " + p.getWantClientAuth());
                    ps.print("      Protocols: ");
                    printList(ps, p.getProtocols(), "          ");
                    ps.print("      Cipher Suites: ");
                    printList(ps, p.getCipherSuites(), "          ");
                    p = sslContext.getSupportedSSLParameters();
                    ps.println("   Supported SSL Parameters:");
                    ps.println("      EndpointIdentificationAlgorithm: " + p.getEndpointIdentificationAlgorithm());
                    ps.println("      Need Client Auth: " + p.getNeedClientAuth());
                    ps.println("      Want Client Auth: " + p.getWantClientAuth());
                    ps.print("      Protocols: ");
                    printList(ps, p.getProtocols(), "          ");
                    ps.print("      Cipher Suites: ");
                    printList(ps, p.getCipherSuites(), "          ");
                } catch (Exception e) {
                    ps.println("Falha ao detalhar SSLContext: " + e.getMessage());
                }
                ps.close();
            }
        }
    }


    /**
     * Reports the trusted certificate authorities from the default JVM trust store.
     */
    public class ReportDefaultTrustKeyStore implements Runnable {

        @Override
        public void run() {
            final PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
            ps.println("Trust Keystore");

            try {
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init((KeyStore) null);
                TrustManager[] trustManagers = tmf.getTrustManagers();

                for (int i = 0; i < trustManagers.length; ++i) {
                    TrustManager tm = trustManagers[i];
                    ps.println(" - TrustManager: " + i + " (" + tm.getClass() + ")");
                    if (tm instanceof X509TrustManager) {
                        X509TrustManager x509tm = (X509TrustManager) tm;
                        X509Certificate[] certificates = x509tm.getAcceptedIssuers();
                        for (int j = 0; j < certificates.length; j++) {
                            final X509Certificate cert = certificates[j];
                            ps.println("   - Certificate #" + j);
                            ps.println("       Subject: " + cert.getSubjectX500Principal());
                            ps.println("       Issuer: " + cert.getIssuerX500Principal());
                            ps.println("       #: " + cert.getSerialNumber() + " From: " + cert.getNotBefore() + " Until: " + cert.getNotAfter());
                        }
                    }
                }
            } catch (Exception e) {
                ps.println("Cannot read TrustManager: " + e.getMessage());
            }
            ps.close();
        }
    }
}
