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

import lombok.Cleanup;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.usefultoys.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * A report module that provides detailed information about a specific {@link NetworkInterface}.
 * It includes details such as the interface name, MTU, status flags (e.g., loopback, multicast),
 * hardware address, and associated IP addresses (IPv4 and IPv6).
 * This report is essential for troubleshooting network connectivity issues.
 *
 * @author Daniel Felix Ferber
 * @see Reporter
 * @see ReporterConfig#reportNetworkInterface
 */
@SuppressWarnings("NonConstantLogger")
@RequiredArgsConstructor
public class ReportNetworkInterface implements Runnable {

    private final @NonNull Logger logger;
    private final @NonNull NetworkInterface nif;

    /**
     * Executes the report for the specific network interface, writing information to the configured logger.
     * The output is formatted as human-readable INFO messages.
     */
    @Override
    public void run() {
        @Cleanup
        final PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
        try {
            ps.printf("Network Interface %s:%n", nif.getName());
            ps.printf(" - display name: %s%n", nif.getDisplayName());
            ps.print(" - properties: ");
            ps.printf("mtu=%d; ", nif.getMTU());
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
                    ps.printf("%1$02X ", b);
                }
                ps.println();
            }
            final Enumeration<InetAddress> inetAddresses = nif.getInetAddresses();
            while (inetAddresses.hasMoreElements()) {
                reportNetworkAddress(ps, inetAddresses.nextElement());
            }
        } catch (final IOException e) {
            ps.printf("   Cannot read property: %s%n", e.getLocalizedMessage());
        }
        ps.println(); // Ensure a newline at the end of the report
    }

    /**
     * Reports details of a specific {@link InetAddress} associated with a network interface.
     *
     * @param ps The PrintStream to write the report to.
     * @param inetAddress The InetAddress to report.
     */
    private static void reportNetworkAddress(final PrintStream ps, final InetAddress inetAddress) {
        try {
            if (inetAddress instanceof Inet4Address) {
                ps.printf(" - NET address (IPV4): %s%n", inetAddress.getHostAddress());
            } else if (inetAddress instanceof Inet6Address) {
                ps.printf(" - NET address (IPV6): %s%n", inetAddress.getHostAddress());
            }
            String hostName = inetAddress.getHostName();
            ps.printf("      host name: %s%n", hostName != null ? hostName : "n/a");
            String canonicalHostName = inetAddress.getCanonicalHostName();
            ps.printf("      canonical host name : %s%n", canonicalHostName != null ? canonicalHostName : "n/a");
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
            ps.printf("   Cannot read property: %s%n", e.getLocalizedMessage());
        }
    }
}
