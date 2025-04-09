package org.usefultoys.slf4j.report;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.usefultoys.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.Map;
import java.util.TreeMap;

/**
 * Reports environment variables available to the current process.
 */
@RequiredArgsConstructor
public class ReportSystemEnvironment implements Runnable {

    private final Logger logger;

    @Override
    public void run() {
        final PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
        ps.println("System Environment:");
        final TreeMap<String, String> sortedProperties = new TreeMap<>(System.getenv());
        for (final Map.Entry<String, String> entry : sortedProperties.entrySet()) {
            ps.println(" - " + entry.getKey() + ": " + entry.getValue());
        }
        ps.close();
    }
}
