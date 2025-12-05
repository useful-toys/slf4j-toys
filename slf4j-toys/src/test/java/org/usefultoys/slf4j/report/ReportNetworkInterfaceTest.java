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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.MockLogger;
import org.usefultoys.test.CharsetConsistency;
import org.usefultoys.test.ResetReporterConfig;
import org.usefultoys.test.WithLocale;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith({CharsetConsistency.class, ResetReporterConfig.class})
@WithLocale("en")
class ReportNetworkInterfaceTest {

    private MockLogger mockLogger;

    @BeforeEach
    void setUp() {
        final Logger logger = LoggerFactory.getLogger("test.report.os");
        mockLogger = (MockLogger) logger;
        mockLogger.clearEvents();
    }

    @Test
    void testRunWithLoopbackInterface() throws Exception {
        NetworkInterface mockNif = mock(NetworkInterface.class);
        when(mockNif.getName()).thenReturn("lo");
        when(mockNif.getDisplayName()).thenReturn("Loopback");
        when(mockNif.getMTU()).thenReturn(65536);
        when(mockNif.isLoopback()).thenReturn(true);
        when(mockNif.isPointToPoint()).thenReturn(false);
        when(mockNif.isUp()).thenReturn(true);
        when(mockNif.isVirtual()).thenReturn(false);
        when(mockNif.supportsMulticast()).thenReturn(false);
        when(mockNif.getHardwareAddress()).thenReturn(null); // No hardware address
        when(mockNif.getInetAddresses()).thenReturn(Collections.emptyEnumeration());

        final ReportNetworkInterface report = new ReportNetworkInterface(mockLogger, mockNif);
        report.run();

        assertTrue(mockLogger.getEventCount() > 0);
        final String logs = mockLogger.getEvent(0).getFormattedMessage();
        assertTrue(logs.contains("Network Interface lo:"));
        assertTrue(logs.contains("display name: Loopback"));
        assertTrue(logs.contains("mtu=65536;"));
        assertTrue(logs.contains("loopback;"));
        assertTrue(logs.contains("UP;"));
        assertTrue(logs.contains("hardware address: n/a"));
    }

    @Test
    void testRunWithPhysicalInterfaceWithAddresses() throws Exception {
        Inet4Address mockIpv4 = mock(Inet4Address.class);
        when(mockIpv4.getHostAddress()).thenReturn("192.168.1.100");
        when(mockIpv4.getHostName()).thenReturn("host.local");
        when(mockIpv4.getCanonicalHostName()).thenReturn("host.local");
        when(mockIpv4.isLoopbackAddress()).thenReturn(false);
        when(mockIpv4.isSiteLocalAddress()).thenReturn(true);
        when(mockIpv4.isAnyLocalAddress()).thenReturn(false);
        when(mockIpv4.isLinkLocalAddress()).thenReturn(false);
        when(mockIpv4.isMulticastAddress()).thenReturn(false);
        when(mockIpv4.isReachable(5000)).thenReturn(true);

        Inet6Address mockIpv6 = mock(Inet6Address.class);
        when(mockIpv6.getHostAddress()).thenReturn("fe80::1");
        when(mockIpv6.getHostName()).thenReturn("host6.local");
        when(mockIpv6.getCanonicalHostName()).thenReturn("host6.local");
        when(mockIpv6.isLoopbackAddress()).thenReturn(false);
        when(mockIpv6.isSiteLocalAddress()).thenReturn(false);
        when(mockIpv6.isAnyLocalAddress()).thenReturn(false);
        when(mockIpv6.isLinkLocalAddress()).thenReturn(true);
        when(mockIpv6.isMulticastAddress()).thenReturn(false);
        when(mockIpv6.isReachable(5000)).thenReturn(false); // Not reachable

        NetworkInterface mockNif = mock(NetworkInterface.class);
        when(mockNif.getName()).thenReturn("eth0");
        when(mockNif.getDisplayName()).thenReturn("Ethernet");
        when(mockNif.getMTU()).thenReturn(1500);
        when(mockNif.isLoopback()).thenReturn(false);
        when(mockNif.isPointToPoint()).thenReturn(false);
        when(mockNif.isUp()).thenReturn(true);
        when(mockNif.isVirtual()).thenReturn(false);
        when(mockNif.supportsMulticast()).thenReturn(true);
        when(mockNif.getHardwareAddress()).thenReturn(new byte[]{0x00, 0x11, 0x22, 0x33, 0x44, 0x55});
        when(mockNif.getInetAddresses()).thenReturn(Collections.enumeration(Arrays.asList(mockIpv4, mockIpv6)));

        final ReportNetworkInterface report = new ReportNetworkInterface(mockLogger, mockNif);
        report.run();

        assertTrue(mockLogger.getEventCount() > 0);
        final String logs = mockLogger.getEvent(0).getFormattedMessage();
        assertTrue(logs.contains("Network Interface eth0:"));
        assertTrue(logs.contains("display name: Ethernet"));
        assertTrue(logs.contains("mtu=1500;"));
        assertFalse(logs.contains("loopback;"));
        assertTrue(logs.contains("UP;"));
        assertTrue(logs.contains("multicast;"));
        assertTrue(logs.contains("hardware address: 00 11 22 33 44 55"));
        assertTrue(logs.contains("NET address (IPV4): 192.168.1.100"));
        assertTrue(logs.contains("host name: host.local"));
        assertTrue(logs.contains("canonical host name : host.local"));
        assertTrue(logs.contains("site-local;"));
        assertTrue(logs.contains("reachable;"));
        assertTrue(logs.contains("NET address (IPV6): fe80::1"));
        assertTrue(logs.contains("link-local;"));
        // Find the start of the IPv6 address log section
        int ipv6LogStartIndex = logs.indexOf("NET address (IPV6): fe80::1");
        // Extract the section of the log that starts from the IPv6 address entry
        String ipv6LogSection = logs.substring(ipv6LogStartIndex);
        assertFalse(ipv6LogSection.contains("reachable;"), "IPv6 address should not be reachable in logs"); // For IPv6
    }

    @Test
    void testRunWithVirtualInterface() throws Exception {
        NetworkInterface mockNif = mock(NetworkInterface.class);
        when(mockNif.getName()).thenReturn("virbr0");
        when(mockNif.getDisplayName()).thenReturn("Virtual Bridge");
        when(mockNif.getMTU()).thenReturn(1500);
        when(mockNif.isLoopback()).thenReturn(false);
        when(mockNif.isPointToPoint()).thenReturn(false);
        when(mockNif.isUp()).thenReturn(true);
        when(mockNif.isVirtual()).thenReturn(true); // Virtual interface
        when(mockNif.supportsMulticast()).thenReturn(false);
        when(mockNif.getHardwareAddress()).thenReturn(null);
        when(mockNif.getInetAddresses()).thenReturn(Collections.emptyEnumeration());

        final ReportNetworkInterface report = new ReportNetworkInterface(mockLogger, mockNif);
        report.run();

        assertTrue(mockLogger.getEventCount() > 0);
        final String logs = mockLogger.getEvent(0).getFormattedMessage();
        assertTrue(logs.contains("Network Interface virbr0:"));
        assertTrue(logs.contains("virtual;"));
    }

    @Test
    void testRunWithHardwareAddressNull() throws Exception {
        NetworkInterface mockNif = mock(NetworkInterface.class);
        when(mockNif.getName()).thenReturn("no_mac");
        when(mockNif.getDisplayName()).thenReturn("No MAC");
        when(mockNif.getMTU()).thenReturn(1500);
        when(mockNif.isLoopback()).thenReturn(false);
        when(mockNif.isPointToPoint()).thenReturn(false);
        when(mockNif.isUp()).thenReturn(true);
        when(mockNif.isVirtual()).thenReturn(false);
        when(mockNif.supportsMulticast()).thenReturn(false);
        when(mockNif.getHardwareAddress()).thenReturn(null); // Null hardware address
        when(mockNif.getInetAddresses()).thenReturn(Collections.emptyEnumeration());

        final ReportNetworkInterface report = new ReportNetworkInterface(mockLogger, mockNif);
        report.run();

        assertTrue(mockLogger.getEventCount() > 0);
        final String logs = mockLogger.getEvent(0).getFormattedMessage();
        assertTrue(logs.contains("hardware address: n/a"));
    }

    @Test
    void testRunWithIOExceptionOnNifProperty() throws Exception {
        NetworkInterface mockNif = mock(NetworkInterface.class);
        when(mockNif.getName()).thenReturn("error_nif");
        when(mockNif.getDisplayName()).thenReturn("Error NIF");
        when(mockNif.getMTU()).thenThrow(new SocketException("Mock MTU exception")); // Simulate IOException
        when(mockNif.getInetAddresses()).thenReturn(Collections.emptyEnumeration()); // Avoid NPE for addresses

        final ReportNetworkInterface report = new ReportNetworkInterface(mockLogger, mockNif);
        report.run();

        assertTrue(mockLogger.getEventCount() > 0);
        final String logs = mockLogger.getEvent(0).getFormattedMessage();
        assertTrue(logs.contains("Cannot read property: Mock MTU exception"));
    }

    @Test
    void testReportNetworkAddressWithIOExceptionOnReachable() throws Exception {
        Inet4Address mockIpv4 = mock(Inet4Address.class);
        when(mockIpv4.getHostAddress()).thenReturn("127.0.0.1");
        when(mockIpv4.getHostName()).thenReturn("localhost");
        when(mockIpv4.getCanonicalHostName()).thenReturn("localhost");
        when(mockIpv4.isLoopbackAddress()).thenReturn(true);
        when(mockIpv4.isSiteLocalAddress()).thenReturn(false);
        when(mockIpv4.isAnyLocalAddress()).thenReturn(true);
        when(mockIpv4.isLinkLocalAddress()).thenReturn(false);
        when(mockIpv4.isMulticastAddress()).thenReturn(false);
        when(mockIpv4.isReachable(5000)).thenThrow(new IOException("Mock reachable exception")); // Simulate IOException

        NetworkInterface mockNif = mock(NetworkInterface.class);
        when(mockNif.getName()).thenReturn("lo_err");
        when(mockNif.getDisplayName()).thenReturn("Loopback Error");
        when(mockNif.getMTU()).thenReturn(65536);
        when(mockNif.isLoopback()).thenReturn(true);
        when(mockNif.isPointToPoint()).thenReturn(false);
        when(mockNif.isUp()).thenReturn(true);
        when(mockNif.isVirtual()).thenReturn(false);
        when(mockNif.supportsMulticast()).thenReturn(false);
        when(mockNif.getHardwareAddress()).thenReturn(null);
        when(mockNif.getInetAddresses()).thenReturn(Collections.enumeration(Collections.singletonList(mockIpv4)));

        final ReportNetworkInterface report = new ReportNetworkInterface(mockLogger, mockNif);
        report.run();

        assertTrue(mockLogger.getEventCount() > 0);
        final String logs = mockLogger.getEvent(0).getFormattedMessage();
        assertTrue(logs.contains("NET address (IPV4): 127.0.0.1"));
        assertTrue(logs.contains("Cannot read property: Mock reachable exception"));
    }

    @Test
    void testReportNetworkAddressWithAllInetAddressProperties() throws Exception {
        Inet4Address mockIpv4 = mock(Inet4Address.class);
        when(mockIpv4.getHostAddress()).thenReturn("10.0.0.1");
        when(mockIpv4.getHostName()).thenReturn("internal-host");
        when(mockIpv4.getCanonicalHostName()).thenReturn("internal-host.domain.com");
        when(mockIpv4.isLoopbackAddress()).thenReturn(false);
        when(mockIpv4.isSiteLocalAddress()).thenReturn(true); // Site local
        when(mockIpv4.isAnyLocalAddress()).thenReturn(false);
        when(mockIpv4.isLinkLocalAddress()).thenReturn(false);
        when(mockIpv4.isMulticastAddress()).thenReturn(true); // Multicast
        when(mockIpv4.isReachable(5000)).thenReturn(true); // Reachable

        NetworkInterface mockNif = mock(NetworkInterface.class);
        when(mockNif.getName()).thenReturn("eth1");
        when(mockNif.getDisplayName()).thenReturn("Internal Network");
        when(mockNif.getMTU()).thenReturn(9000);
        when(mockNif.isLoopback()).thenReturn(false);
        when(mockNif.isPointToPoint()).thenReturn(false);
        when(mockNif.isUp()).thenReturn(true);
        when(mockNif.isVirtual()).thenReturn(false);
        when(mockNif.supportsMulticast()).thenReturn(true);
        when(mockNif.getHardwareAddress()).thenReturn(new byte[]{(byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD, (byte) 0xEE, (byte) 0xFF});
        when(mockNif.getInetAddresses()).thenReturn(Collections.enumeration(Collections.singletonList(mockIpv4)));
        final ReportNetworkInterface report = new ReportNetworkInterface(mockLogger, mockNif);
        report.run();

        assertTrue(mockLogger.getEventCount() > 0);
        final String logs = mockLogger.getEvent(0).getFormattedMessage();
        assertTrue(logs.contains("NET address (IPV4): 10.0.0.1"));
        assertTrue(logs.contains("host name: internal-host"));
        assertTrue(logs.contains("canonical host name : internal-host.domain.com"));
        assertTrue(logs.contains("site-local;"));
        assertTrue(logs.contains("multicast;"));
        assertTrue(logs.contains("reachable;"));
    }

    @Test
    void testReportNetworkAddressWithAllInetAddressPropertiesIPv6() throws Exception {
        Inet6Address mockIpv6 = mock(Inet6Address.class);
        when(mockIpv6.getHostAddress()).thenReturn("fe80::abcd:1234:5678:90ef");
        when(mockIpv6.getHostName()).thenReturn("ipv6-host");
        when(mockIpv6.getCanonicalHostName()).thenReturn("ipv6-host.domain.com");
        when(mockIpv6.isLoopbackAddress()).thenReturn(false);
        when(mockIpv6.isSiteLocalAddress()).thenReturn(false);
        when(mockIpv6.isAnyLocalAddress()).thenReturn(false);
        when(mockIpv6.isLinkLocalAddress()).thenReturn(true); // Link local
        when(mockIpv6.isMulticastAddress()).thenReturn(true); // Multicast
        when(mockIpv6.isReachable(5000)).thenReturn(true); // Reachable

        NetworkInterface mockNif = mock(NetworkInterface.class);
        when(mockNif.getName()).thenReturn("eth2");
        when(mockNif.getDisplayName()).thenReturn("IPv6 Network");
        when(mockNif.getMTU()).thenReturn(1500);
        when(mockNif.isLoopback()).thenReturn(false);
        when(mockNif.isPointToPoint()).thenReturn(false);
        when(mockNif.isUp()).thenReturn(true);
        when(mockNif.isVirtual()).thenReturn(false);
        when(mockNif.supportsMulticast()).thenReturn(true);
        when(mockNif.getHardwareAddress()).thenReturn(new byte[]{0x11, 0x22, 0x33, 0x44, 0x55, 0x66});
        when(mockNif.getInetAddresses()).thenReturn(Collections.enumeration(Collections.singletonList(mockIpv6)));

        final ReportNetworkInterface report = new ReportNetworkInterface(mockLogger, mockNif);
        report.run();

        assertTrue(mockLogger.getEventCount() > 0);
        final String logs = mockLogger.getEvent(0).getFormattedMessage();
        assertTrue(logs.contains("NET address (IPV6): fe80::abcd:1234:5678:90ef"));
        assertTrue(logs.contains("host name: ipv6-host"));
        assertTrue(logs.contains("canonical host name : ipv6-host.domain.com"));
        assertTrue(logs.contains("link-local;"));
        assertTrue(logs.contains("multicast;"));
        assertTrue(logs.contains("reachable;"));
    }

    @Test
    void testReportNetworkAddressWithNullHostNames() throws Exception {
        Inet4Address mockIpv4 = mock(Inet4Address.class);
        when(mockIpv4.getHostAddress()).thenReturn("127.0.0.1");
        when(mockIpv4.getHostName()).thenReturn(null); // Simulate null host name
        when(mockIpv4.getCanonicalHostName()).thenReturn(null); // Simulate null canonical host name
        when(mockIpv4.isLoopbackAddress()).thenReturn(true);
        when(mockIpv4.isSiteLocalAddress()).thenReturn(false);
        when(mockIpv4.isAnyLocalAddress()).thenReturn(true);
        when(mockIpv4.isLinkLocalAddress()).thenReturn(false);
        when(mockIpv4.isMulticastAddress()).thenReturn(false);
        when(mockIpv4.isReachable(5000)).thenReturn(true);

        NetworkInterface mockNif = mock(NetworkInterface.class);
        when(mockNif.getName()).thenReturn("lo_null_host");
        when(mockNif.getDisplayName()).thenReturn("Loopback Null Host");
        when(mockNif.getMTU()).thenReturn(65536);
        when(mockNif.isLoopback()).thenReturn(true);
        when(mockNif.isPointToPoint()).thenReturn(false);
        when(mockNif.isUp()).thenReturn(true);
        when(mockNif.isVirtual()).thenReturn(false);
        when(mockNif.supportsMulticast()).thenReturn(false);
        when(mockNif.getHardwareAddress()).thenReturn(null);
        when(mockNif.getInetAddresses()).thenReturn(Collections.enumeration(Collections.singletonList(mockIpv4)));

        final ReportNetworkInterface report = new ReportNetworkInterface(mockLogger, mockNif);
        report.run();

        assertTrue(mockLogger.getEventCount() > 0);
        final String logs = mockLogger.getEvent(0).getFormattedMessage();
        assertTrue(logs.contains("NET address (IPV4): 127.0.0.1"));
        assertTrue(logs.contains("host name: n/a"));
        assertTrue(logs.contains("canonical host name : n/a"));
        assertTrue(logs.contains("loopback;"));
        assertTrue(logs.contains("any-local;"));
        assertTrue(logs.contains("reachable;"));
    }
}
