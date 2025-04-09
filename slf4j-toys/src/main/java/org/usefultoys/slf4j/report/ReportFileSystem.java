package org.usefultoys.slf4j.report;

import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.usefultoys.slf4j.LoggerFactory;
import org.usefultoys.slf4j.utils.UnitFormatter;

import java.io.File;
import java.io.PrintStream;

/**
 * Reports information about the file system roots, including total, free, and usable space.
 */
@SuppressWarnings("NonConstantLogger")
@RequiredArgsConstructor
public class ReportFileSystem implements Runnable {

    private final Logger logger;

    @Override
    public void run() {
        @Cleanup
        final PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
        final File[] roots = File.listRoots();
        boolean first = true;
        for (final File root : roots) {
            if (first) {
                first = false;
            } else {
                ps.println();
            }
            ps.printf("File system root: %s%n", root.getAbsolutePath());
            ps.printf(" - total space: %s%n", UnitFormatter.bytes(root.getTotalSpace()));
            ps.printf(" - currently free space: %s (%s usable)%n", UnitFormatter.bytes(root.getFreeSpace()), UnitFormatter.bytes(root.getUsableSpace()));
        }
        ps.close();
    }
}
