package org.usefultoys.slf4j.report;

import org.slf4j.Logger;
import org.usefultoys.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.Map;
import java.util.TreeMap;

/**
 * Reports all Java system properties in sorted order.
 */
public class ReportSystemProperties implements Runnable {

    private final Logger logger;

    public ReportSystemProperties(final Logger logger) {
        this.logger = logger;
    }

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
