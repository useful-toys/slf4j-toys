package org.usefultoys.slf4j.report;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.usefultoys.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.Map;
import java.util.TreeMap;

/**
 * Reports all Java system properties in sorted order.
 */
@RequiredArgsConstructor
public class ReportSystemProperties implements Runnable {

    private final Logger logger;

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
