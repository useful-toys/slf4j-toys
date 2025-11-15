/*
 * Copyright 2025 Daniel Felix Ferber
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.usefultoys.slf4j.report;

import lombok.Cleanup;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.usefultoys.slf4j.LoggerFactory;
import org.usefultoys.slf4j.utils.UnitFormatter;

import java.io.*;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A report module that provides information about the containerized environment
 * (e.g., Docker, Kubernetes) if the application is running inside one.
 * It includes details such as container ID, resource limits, and orchestrator-specific metadata.
 * This report is crucial for validating deployment configurations in cloud-native environments.
 *
 * @author Daniel Felix Ferber
 * @see Reporter
 * @see ReporterConfig#reportContainerInfo
 */
@SuppressWarnings("NonConstantLogger")
@RequiredArgsConstructor
public class ReportContainerInfo implements Runnable {

    private final @NonNull Logger logger;

    // Regex to extract container ID from /proc/self/cgroup
    private static final Pattern CGROUP_CONTAINER_ID_PATTERN = Pattern.compile("^[0-9]+:[^:]+:/docker/([0-9a-fA-F]+)$");
    private static final String UNKNOWN = "unknown";

    /**
     * Executes the report, writing container information to the configured logger.
     * The output is formatted as human-readable INFO messages.
     */
    @Override
    public void run() {
        @Cleanup
        final PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
        ps.println("Container Info:");

        // 1. Basic Hostname (often container name)
        ps.printf(" - Hostname: %s%n", getEnvironmentVariables().getOrDefault("HOSTNAME", UNKNOWN));

        // 2. Container ID from /proc/self/cgroup (Linux-specific)
        String containerId = getContainerIdFromCgroup();
        ps.printf(" - Container ID (cgroup): %s%n", containerId);

        // 3. Kubernetes-specific environment variables
        ps.printf(" - Kubernetes Pod Name: %s%n", getEnvironmentVariables().getOrDefault("KUBERNETES_POD_NAME", UNKNOWN));
        ps.printf(" - Kubernetes Namespace: %s%n", getEnvironmentVariables().getOrDefault("KUBERNETES_NAMESPACE", UNKNOWN));
        ps.printf(" - Kubernetes Node Name: %s%n", getEnvironmentVariables().getOrDefault("KUBERNETES_NODE_NAME", UNKNOWN));

        // 4. Resource Limits from cgroups (Linux-specific)
        reportMemoryLimits(ps);
        reportCpuLimits(ps);

        // 5. Other common container environment variables
        ps.printf(" - Docker Container ID (env): %s%n", getEnvironmentVariables().getOrDefault("DOCKER_CONTAINER_ID", UNKNOWN));
        ps.printf(" - Container Name (env): %s%n", getEnvironmentVariables().getOrDefault("CONTAINER_NAME", UNKNOWN));

        ps.println(); // Ensure a newline at the end of the report
    }

    /**
     * Retrieves the environment variables. This method can be overridden in tests to provide
     * a custom map of environment variables without modifying the actual system environment.
     *
     * @return A map of environment variable names to their values.
     */
    protected Map<String, String> getEnvironmentVariables() {
        return System.getenv();
    }

    /**
     * Reads the content of a file. This method can be overridden in tests to simulate
     * file system content without actual file I/O.
     *
     * @param path The path to the file.
     * @return The content of the first line of the file, or null if the file does not exist or cannot be read.
     * @throws IOException If an I/O error occurs.
     */
    protected String readFileContent(String path) throws IOException {
        File file = new File(path);
        if (!file.exists() || !file.canRead()) {
            return null;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return reader.readLine();
        }
    }

    private String getContainerIdFromCgroup() {
        try {
            String cgroupLine = readFileContent("/proc/self/cgroup");
            if (cgroupLine == null) {
                return "Not available (not in Linux container or no read access)";
            }
            Matcher matcher = CGROUP_CONTAINER_ID_PATTERN.matcher(cgroupLine);
            if (matcher.matches()) {
                return matcher.group(1); // Docker container ID
            }
        } catch (IOException e) {
            logger.warn("Failed to read /proc/self/cgroup: {}", e.getMessage());
            return "Error reading cgroup file";
        }
        return "Not found in cgroup (not a Docker container?)";
    }

    private void reportMemoryLimits(PrintStream ps) {
        try {
            String limitStr = readFileContent("/sys/fs/cgroup/memory/memory.limit_in_bytes");
            if (limitStr != null) {
                long limit = Long.parseLong(limitStr.trim());
                if (limit < Long.MAX_VALUE / 2) { // Avoid reporting "no limit" as a huge number
                    ps.printf(" - Memory Limit: %s%n", UnitFormatter.bytes(limit));
                } else {
                    ps.println(" - Memory Limit: No limit set");
                }
            } else {
                ps.println(" - Memory Limit: Not available (not in Linux container or no read access)");
            }
        } catch (IOException | NumberFormatException e) {
            logger.warn("Failed to read memory limit from cgroup: {}", e.getMessage());
            ps.println(" - Memory Limit: Error reading");
        }
    }

    private void reportCpuLimits(PrintStream ps) {
        try {
            String cpuQuotaStr = readFileContent("/sys/fs/cgroup/cpu/cpu.cfs_quota_us");
            String cpuPeriodStr = readFileContent("/sys/fs/cgroup/cpu/cpu.cfs_period_us");

            if (cpuQuotaStr != null && cpuPeriodStr != null) {
                long quota = Long.parseLong(cpuQuotaStr.trim());
                long period = Long.parseLong(cpuPeriodStr.trim());

                if (quota > 0 && period > 0) {
                    double cpuShares = (double) quota / period;
                    ps.printf(" - CPU Limit: %.2f cores%n", cpuShares);
                } else {
                    ps.println(" - CPU Limit: No limit set");
                }
            } else {
                ps.println(" - CPU Limit: Not available (not in Linux container or no read access)");
            }
        } catch (IOException | NumberFormatException e) {
            logger.warn("Failed to read CPU limit from cgroup: {}", e.getMessage());
            ps.println(" - CPU Limit: Error reading");
        }
    }
}
