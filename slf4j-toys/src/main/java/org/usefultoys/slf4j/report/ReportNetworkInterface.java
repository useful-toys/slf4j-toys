package org.usefultoys.slf4j.report;

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
 * Reports details of a specific network interface, including name, MTU, status flags (e.g., loopback, multicast),
 * hardware address, and associated IP addresses.
 */
@RequiredArgsConstructor
public class ReportNetworkInterface implements Runnable {

    private final Logger logger;
    private final NetworkInterface nif;

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
        ps.close();
    }

    private static void reportNetworkAddress(final PrintStream ps, final InetAddress inetAddress) {
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
