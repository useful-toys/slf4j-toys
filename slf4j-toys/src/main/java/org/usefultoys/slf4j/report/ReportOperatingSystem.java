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
        ps.println(" - architecture: " + System.getProperty("os.arch"));
        ps.println(" - name: " + System.getProperty("os.name"));
        ps.println(" - version: " + System.getProperty("os.version"));
        ps.println(" - file separator: " + Integer.toHexString(System.getProperty("file.separator").charAt(0)));
        ps.println(" - path separator: " + Integer.toHexString(System.getProperty("path.separator").charAt(0)));
        ps.println(" - line separator: " + Integer.toHexString(System.getProperty("line.separator").charAt(0)));
        ps.close();
    }
}
