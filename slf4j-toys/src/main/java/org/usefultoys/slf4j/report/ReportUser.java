package org.usefultoys.slf4j.report;

import org.slf4j.Logger;
import org.usefultoys.slf4j.LoggerFactory;

import java.io.PrintStream;

/**
 * Reports environment variables available to the current process.
 */
public class ReportUser implements Runnable {

    private final Logger logger;

    public ReportUser(final Logger logger) {
        this.logger = logger;
    }

    @Override
    public void run() {
        final PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
        ps.println("User:");
        ps.println(" - name: " + System.getProperty("user.name"));
        ps.println(" - home: " + System.getProperty("user.home"));
        ps.close();
    }
}
