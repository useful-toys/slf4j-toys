package org.usefultoys.slf4j.report;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.usefultoys.slf4j.LoggerFactory;

import java.io.PrintStream;

/**
 * Reports properties of the operating system, including architecture, name, version, and path/file/line separators.
 */
@RequiredArgsConstructor
public class ReportOperatingSystem implements Runnable {

    private final Logger logger;

    @Override
    public void run() {
        final PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
        ps.println("Operating System");
        ps.printf(" - architecture: %s%n", System.getProperty("os.arch"));
        ps.printf(" - name: %s%n", System.getProperty("os.name"));
        ps.printf(" - version: %s%n", System.getProperty("os.version"));
        ps.printf(" - file separator: %s%n", Integer.toHexString(System.getProperty("file.separator").charAt(0)));
        ps.printf(" - path separator: %s%n", Integer.toHexString(System.getProperty("path.separator").charAt(0)));
        ps.printf(" - line separator: %s%n", Integer.toHexString(System.getProperty("line.separator").charAt(0)));
        ps.close();
    }
}
