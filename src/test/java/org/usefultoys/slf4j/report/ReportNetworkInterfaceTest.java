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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.impl.MockLoggerEvent;
import org.usefultoys.slf4jtestmock.AssertLogger;
import org.usefultoys.slf4jtestmock.MockLoggerExtension;
import org.usefultoys.slf4jtestmock.Slf4jMock;
import org.usefultoys.test.CharsetConsistencyExtension;
import org.usefultoys.test.ResetReporterConfigExtension;
import org.usefultoys.test.WithLocale;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith({ResetReporterConfigExtension.class, CharsetConsistencyExtension.class, MockLoggerExtension.class})
@WithLocale("en")
class ReportNetworkInterfaceTest {

    @Slf4jMock("test.report.os")
    private Logger logger;

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

        final ReportNetworkInterface report = new ReportNetworkInterface(logger, mockNif);
        report.run();

        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
                "Network Interface lo:",
                "display name: Loopback",
                "mtu=65536;",
                "loopback;",
                "UP;",
                "hardware address: n/a");
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

        final ReportNetworkInterface report = new ReportNetworkInterface(logger, mockNif);
        report.run();

        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
                "Network Interface eth0:",
                "display name: Ethernet",
                "mtu=1500;",
                "UP;",
                "multicast;",
                "hardware address: 00 11 22 33 44 55",
                "NET address (IPV4): 192.168.1.100",
                "host name: host.local",
                "canonical host name : host.local",
                "site-local;",
                "reachable;",
                "NET address (IPV6): fe80::1",
                "link-local;");
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

        final ReportNetworkInterface report = new ReportNetworkInterface(logger, mockNif);
        report.run();

        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
                "Network Interface virbr0:",
                "virtual;");
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

        final ReportNetworkInterface report = new ReportNetworkInterface(logger, mockNif);
        report.run();

        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
                "hardware address: n/a");
    }

    @Test
    void testRunWithIOExceptionOnNifProperty() throws Exception {
        NetworkInterface mockNif = mock(NetworkInterface.class);
        when(mockNif.getName()).thenReturn("error_nif");
        when(mockNif.getDisplayName()).thenReturn("Error NIF");
        when(mockNif.getMTU()).thenThrow(new SocketException("Mock MTU exception")); // Simulate IOException
        when(mockNif.getInetAddresses()).thenReturn(Collections.emptyEnumeration()); // Avoid NPE for addresses

        final ReportNetworkInterface report = new ReportNetworkInterface(logger, mockNif);
        report.run();

        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
                "Cannot read property: Mock MTU exception");
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

        final ReportNetworkInterface report = new ReportNetworkInterface(logger, mockNif);
        report.run();

        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
                "NET address (IPV4): 127.0.0.1",
                "Cannot read property: Mock reachable exception");
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
        final ReportNetworkInterface report = new ReportNetworkInterface(logger, mockNif);
        report.run();

        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
                "NET address (IPV4): 10.0.0.1",
                "host name: internal-host",
                "canonical host name : internal-host.domain.com",
                "site-local;",
                "multicast;",
                "reachable;");
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

        final ReportNetworkInterface report = new ReportNetworkInterface(logger, mockNif);
        report.run();

        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
                "NET address (IPV6): fe80::abcd:1234:5678:90ef",
                "host name: ipv6-host",
                "canonical host name : ipv6-host.domain.com",
                "link-local;",
                "multicast;",
                "reachable;");
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

        final ReportNetworkInterface report = new ReportNetworkInterface(logger, mockNif);
        report.run();

        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
                "NET address (IPV4): 127.0.0.1",
                "host name: n/a",
                "canonical host name : n/a",
                "loopback;",
                "any-local;",
                "reachable;");
    }
}
