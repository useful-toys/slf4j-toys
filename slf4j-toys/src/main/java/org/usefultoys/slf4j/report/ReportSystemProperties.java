package org.usefultoys.slf4j.report;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.usefultoys.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.Map;
import java.util.SortedMap;
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
        final SortedMap<Object, Object> sortedProperties = new TreeMap<>(System.getProperties());
        for (final Map.Entry<Object, Object> entry : sortedProperties.entrySet()) {
            ps.printf(" - %s: %s%n", entry.getKey(), entry.getValue());
        }
        ps.close();
    }
}
