/*
 * Copyright 2015 Daniel Felix Ferber.
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
package org.usefultoys.slf4j;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.TimeZone;
import org.slf4j.Logger;
import org.usefultoys.slf4j.utils.UnitFormatter;

/**
 *
 * @author Daniel Felix Ferber
 */
public class Report implements Serializable {

    private static final long serialVersionUID = 1L;

    transient private final Logger logger;

    public Report(Logger logger) {
        this.logger = logger;
    }

    public class ReportVM implements Runnable {

        @Override
        public void run() {
            PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
            ps.println("Java Virtual Machine");
            ps.println(" - vendor: " + System.getProperty("java.vendor"));
            ps.println(" - version: " + System.getProperty("java.version"));
            ps.println(" - installation directory: " + System.getProperty("java.home"));
            ps.close();
        }
    }

    public class ReportFileSystem implements Runnable {

        @Override
        public void run() {
            PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
            File[] roots = File.listRoots();
            boolean first = true;
            for (File root : roots) {
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

    public class ReportMemory implements Runnable {

        @Override
        public void run() {
            PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
            final Runtime runtime = Runtime.getRuntime();
            ps.println("Memory:");
            long maxMemory = runtime.maxMemory();
            final long totalMemory = runtime.totalMemory();
            final long freeMemory = runtime.freeMemory();
            ps.println(" - maximum allowed: " + (maxMemory == Long.MAX_VALUE ? "no limit" : UnitFormatter.bytes(maxMemory)));
            ps.println(" - currently allocated: " + UnitFormatter.bytes(totalMemory) + " (" + UnitFormatter.bytes(maxMemory - totalMemory) + " more available)");
            ps.println(" - currently used: " + UnitFormatter.bytes(totalMemory - freeMemory) + " (" + UnitFormatter.bytes(freeMemory) + " free)");
            ps.close();
        }
    }

    public class ReportUser implements Runnable {

        @Override
        public void run() {
            PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
            ps.println("User:");
            ps.println(" - name: " + System.getProperty("user.name"));
            ps.println(" - home: " + System.getProperty("user.home"));
            ps.close();
        }
    }

    public class ReportPhysicalSystem implements Runnable {

        @Override
        public void run() {
            final Runtime runtime = Runtime.getRuntime();
            PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
            ps.println("Physical system");
            ps.println(" - processors: " + runtime.availableProcessors());
            ps.close();
        }
    }

    public class ReportOperatingSystem implements Runnable {

        @Override
        public void run() {
            PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
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

    public class ReportCalendar implements Runnable {

        @Override
        public void run() {
            final PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
            ps.println("Calendar");
            ps.print(" - current date/time: " + SimpleDateFormat.getDateTimeInstance().format(new Date()));
            final TimeZone tz = TimeZone.getDefault();
            ps.print(" - default timezone: " + tz.getDisplayName());
            ps.print(" (" + tz.getID() + ")");
            ps.print("; DST=" + tz.getDSTSavings() / 60000 + "min");
            ps.print("; observesDT=" + tz.observesDaylightTime());
            ps.print("; useDT=" + tz.useDaylightTime());
            ps.print("; inDT=" + tz.inDaylightTime(new Date()));
            ps.print("; offset=" + tz.getRawOffset() / 60000 + "min");
            ps.println();
            ps.print(" - available IDs:");
            int i = 1;
            for (String id : TimeZone.getAvailableIDs()) {
                if (i++ % 5 ==0) ps.print("\n      ");
                ps.print(id + "; ");
            }
            ps.close();
        }
    }

    public class ReportLocale implements Runnable {

        @Override
        public void run() {
            PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
            Locale loc = Locale.getDefault();
            ps.println("Locale");
            ps.print(" - default locale: " + loc.getDisplayName());
            ps.print("; language=" + loc.getDisplayLanguage() + " (" + loc.getLanguage() + ")");
            ps.print("; country=" + loc.getDisplayCountry() + " (" + loc.getCountry() + ")");
            ps.print("; script=" + loc.getDisplayScript() + " (" + loc.getScript() + ")");
            ps.print("; variant=" + loc.getDisplayVariant() + " (" + loc.getVariant() + ")");
            ps.println();
            ps.print(" - available locales:");
            int i = 1;
            for (Locale l : Locale.getAvailableLocales()) {
                if (i++ % 5 ==0) ps.print("\n      ");
                ps.print(l.getDisplayName() + "; ");
            }
            ps.close();
        }
    }

    public class ReportNetworkInterface implements Runnable {

        private final NetworkInterface nif;

        public ReportNetworkInterface(NetworkInterface nif) {
            this.nif = nif;
        }

        @Override
        public void run() {
            PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
            try {
                ps.println("Network Interface " + nif.getName() + " :");
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
                byte[] macAddress = nif.getHardwareAddress();
                if (macAddress != null) {
                    for (byte b : macAddress) {
                        ps.print(String.format("%1$02X ", b));
                    }
                    ps.println();
                } else {
                    ps.println("n/a");
                }
                Enumeration<InetAddress> inetAddresses = nif.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    reportNetworkAddress(ps, inetAddresses.nextElement());
                }
            } catch (IOException e) {
                ps.println("   Cannot read property: " + e.getLocalizedMessage());
            }
            ps.close();
        }

        private void reportNetworkAddress(PrintStream ps, InetAddress inetAddress) {
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
                    ps.print("multicas; ");
                }
                if (inetAddress.isReachable(5000)) {
                    ps.print("reachable; ");
                }
                ps.println();
            } catch (IOException e) {
                ps.println("   Cannot read property: " + e.getLocalizedMessage());
            }
        }
    }
}
