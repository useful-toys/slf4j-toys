package org.usefultoys.slf4j.report;

import org.slf4j.Logger;
import org.usefultoys.slf4j.LoggerFactory;
import org.usefultoys.slf4j.utils.UnitFormatter;

import java.io.File;
import java.io.PrintStream;

/**
 * Reports information about the file system roots, including total, free, and usable space.
 */
public class ReportFileSystem implements Runnable {

    private final Logger logger;

    public ReportFileSystem(final Logger logger) {
        this.logger = logger;
    }

    @Override
    public void run() {
        final PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
        final File[] roots = File.listRoots();
        boolean first = true;
        for (final File root : roots) {
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
