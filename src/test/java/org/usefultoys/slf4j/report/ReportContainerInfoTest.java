/*
 * Copyright 2026 Daniel Felix Ferber
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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.impl.MockLoggerEvent;
import org.usefultoys.slf4j.utils.ConfigParser;
import org.usefultoys.slf4jtestmock.AssertLogger;
import org.usefultoys.slf4jtestmock.Slf4jMock;
import org.usefultoys.slf4jtestmock.WithMockLogger;
import org.usefultoys.test.ResetReporterConfig;
import org.usefultoys.test.ValidateCharset;
import org.usefultoys.test.WithLocale;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link ReportContainerInfo}.
 * <p>
 * Tests verify that ReportContainerInfo correctly detects and logs container environment information
 * including Docker, Kubernetes, cgroup limits, and various error scenarios.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>Docker Container Detection:</b> Verifies detection and logging of Docker container information including hostname, container ID from cgroup, memory and CPU limits, and environment variables</li>
 *   <li><b>Kubernetes Pod Detection:</b> Tests identification and reporting of Kubernetes pod information including pod name, namespace, node name, and unavailable cgroup limits</li>
 *   <li><b>Non-Container Environment:</b> Ensures proper handling when not running in a container, logging unavailable container information</li>
 *   <li><b>Cgroup File Read Errors:</b> Validates error handling when cgroup files cannot be read, logging appropriate warnings and error messages</li>
 *   <li><b>Memory Limit Parsing:</b> Tests parsing of memory limits from cgroup files, including error cases with invalid values</li>
 *   <li><b>CPU Limit Parsing:</b> Verifies CPU limit calculation from cgroup quota and period values, handling parsing errors</li>
 *   <li><b>Container ID Pattern Matching:</b> Ensures proper extraction of container IDs from cgroup paths, handling non-matching patterns</li>
 *   <li><b>Unlimited Resource Limits:</b> Tests reporting when memory or CPU limits are not set (unlimited)</li>
 *   <li><b>Edge Cases:</b> Covers zero quota/period values and various error conditions in container detection</li>
 * </ul>
 */
@DisplayName("ReportContainerInfo")
@ValidateCharset
@ResetReporterConfig
@WithLocale("en")
@WithMockLogger
class ReportContainerInfoTest {

    @Slf4jMock
    private Logger logger;

    /**
     * Helper method to create a ReportContainerInfo instance with mocked environment and file system.
     */
    private ReportContainerInfo createReportContainerInfo(final Map<String, String> envMap, final Map<String, String> fileContentMap) {
        return new ReportContainerInfo(logger) {
            @Override
            protected Map<String, String> getEnvironmentVariables() {
                return envMap;
            }

            @Override
            protected String readFileContent(final String path) throws IOException {
                if ("throw_io_exception".equals(fileContentMap.get(path))) {
                    throw new IOException("Simulated IO Error for " + path);
                }
                return fileContentMap.get(path);
            }
        };
    }

    @Test
    @DisplayName("should report container info for Docker")
    void testContainerInfoIsReportedForDocker() {
        // Given: Docker environment with container ID and resource limits
        final Map<String, String> env = new HashMap<>();
        env.put("HOSTNAME", "my-docker-container");
        env.put("DOCKER_CONTAINER_ID", "a1b2c3d4e5f6");

        final Map<String, String> fileContent = new HashMap<>();
        fileContent.put("/proc/self/cgroup", "1:name=systemd:/docker/a1b2c3d4e5f67890abcdef1234567890");
        fileContent.put("/sys/fs/cgroup/memory/memory.limit_in_bytes", "1073741824"); // 1GB
        fileContent.put("/sys/fs/cgroup/cpu/cpu.cfs_quota_us", "100000"); // 1 CPU
        fileContent.put("/sys/fs/cgroup/cpu/cpu.cfs_period_us", "100000");

        final ReportContainerInfo reporter = createReportContainerInfo(env, fileContent);

        // When: report is executed
        reporter.run();

        // Then: should log Docker container info with all details
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
            "Container Info:",
            " - Hostname: my-docker-container",
            " - Container ID (cgroup): a1b2c3d4e5f67890abcdef1234567890",
            " - Memory Limit: 1073.7MB",
            " - CPU Limit: 1.00 cores",
            " - Docker Container ID (env): a1b2c3d4e5f6");
        assertTrue(ConfigParser.isInitializationOK());
        AssertLogger.assertEventCountByLevel(logger, MockLoggerEvent.Level.WARN, 0);
    }

    @Test
    @DisplayName("should report container info for Kubernetes")
    void testContainerInfoIsReportedForKubernetes() {
        // Given: Kubernetes environment with pod and namespace info
        final Map<String, String> env = new HashMap<>();
        env.put("HOSTNAME", "my-pod-123");
        env.put("KUBERNETES_POD_NAME", "my-app-pod-xyz");
        env.put("KUBERNETES_NAMESPACE", "production");
        env.put("KUBERNETES_NODE_NAME", "worker-node-1");

        final Map<String, String> fileContent = new HashMap<>();
        fileContent.put("/proc/self/cgroup", null);
        fileContent.put("/sys/fs/cgroup/memory/memory.limit_in_bytes", null);
        fileContent.put("/sys/fs/cgroup/cpu/cpu.cfs_quota_us", null);
        fileContent.put("/sys/fs/cgroup/cpu/cpu.cfs_period_us", null);

        final ReportContainerInfo reporter = createReportContainerInfo(env, fileContent);

        // When: report is executed
        reporter.run();

        // Then: should log Kubernetes pod info with unavailable cgroup limits
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
            "Container Info:",
            " - Hostname: my-pod-123",
            " - Kubernetes Pod Name: my-app-pod-xyz",
            " - Kubernetes Namespace: production",
            " - Kubernetes Node Name: worker-node-1",
            " - Container ID (cgroup): Not available",
            " - Memory Limit: Not available (not in Linux container or no read access)",
            " - CPU Limit: Not available (not in Linux container or no read access)");
        assertTrue(ConfigParser.isInitializationOK());
        AssertLogger.assertEventCountByLevel(logger, MockLoggerEvent.Level.WARN, 0);
    }

    @Test
    @DisplayName("should report when not running in container")
    void testContainerInfoNotAvailableWhenNotInContainer() {
        // Given: non-container environment with no cgroup files
        final Map<String, String> env = Collections.emptyMap();
        final Map<String, String> fileContent = new HashMap<>();
        fileContent.put("/proc/self/cgroup", null);
        fileContent.put("/sys/fs/cgroup/memory/memory.limit_in_bytes", null);
        fileContent.put("/sys/fs/cgroup/cpu/cpu.cfs_quota_us", null);
        fileContent.put("/sys/fs/cgroup/cpu/cpu.cfs_period_us", null);

        final ReportContainerInfo reporter = createReportContainerInfo(env, fileContent);

        // When: report is executed
        reporter.run();

        // Then: should log unavailable container info
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
            "Container Info:",
            " - Hostname: unknown",
            " - Container ID (cgroup): Not available",
            " - Memory Limit: Not available (not in Linux container or no read access)",
            " - CPU Limit: Not available (not in Linux container or no read access)");
        assertTrue(ConfigParser.isInitializationOK());
        AssertLogger.assertEventCountByLevel(logger, MockLoggerEvent.Level.WARN, 0);
    }

    @Test
    @DisplayName("should handle cgroup file read error")
    void testCgroupFileReadError() {
        // Given: environment with cgroup file read error
        final Map<String, String> env = new HashMap<>();
        env.put("HOSTNAME", "test-host");

        final Map<String, String> fileContent = new HashMap<>();
        fileContent.put("/proc/self/cgroup", "throw_io_exception");

        final ReportContainerInfo reporter = createReportContainerInfo(env, fileContent);

        // When: report is executed
        reporter.run();

        // Then: should log error reading cgroup and warning message
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.WARN,
                "Failed to read /proc/self/cgroup: Simulated IO Error for /proc/self/cgroup");
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.INFO,
                "Container Info:",
                " - Hostname: test-host",
                " - Container ID (cgroup): Error reading cgroup file");
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    @DisplayName("should handle memory limit parsing error")
    void testMemoryLimitParsingError() {
        // Given: environment with invalid memory limit value
        final Map<String, String> env = new HashMap<>();
        final Map<String, String> fileContent = new HashMap<>();
        fileContent.put("/sys/fs/cgroup/memory/memory.limit_in_bytes", "invalid_number");

        final ReportContainerInfo reporter = createReportContainerInfo(env, fileContent);

        // When: report is executed
        reporter.run();

        // Then: should log error reading memory limit and warning message
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.WARN,
                "Failed to read memory limit from cgroup: For input string: \"invalid_number\"");
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.INFO,
                "Container Info:",
                " - Memory Limit: Error reading");
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    @DisplayName("should handle CPU limit parsing error")
    void testCpuLimitParsingError() {
        // Given: environment with invalid CPU quota value
        final Map<String, String> env = new HashMap<>();
        final Map<String, String> fileContent = new HashMap<>();
        fileContent.put("/sys/fs/cgroup/cpu/cpu.cfs_quota_us", "invalid_quota");
        fileContent.put("/sys/fs/cgroup/cpu/cpu.cfs_period_us", "100000");

        final ReportContainerInfo reporter = createReportContainerInfo(env, fileContent);

        // When: report is executed
        reporter.run();

        // Then: should log error reading CPU limit and warning message
        AssertLogger.assertEvent(logger, 1, MockLoggerEvent.Level.INFO,
                "Container Info:",
                " - CPU Limit: Error reading");
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.WARN,
                "Failed to read CPU limit from cgroup: For input string: \"invalid_quota\"");

        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    @DisplayName("should handle cgroup container ID pattern no match")
    void testCgroupContainerIdPatternNoMatch() {
        // Given: environment with cgroup path that doesn't match Docker pattern
        final Map<String, String> env = new HashMap<>();
        final Map<String, String> fileContent = new HashMap<>();
        fileContent.put("/proc/self/cgroup", "1:name=systemd:/some/other/path");

        final ReportContainerInfo reporter = createReportContainerInfo(env, fileContent);

        // When: report is executed
        reporter.run();

        // Then: should log that container ID not found in cgroup
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
            "Container Info:",
            " - Container ID (cgroup): Not found in cgroup (not a Docker container?)");
        AssertLogger.assertEventCountByLevel(logger, MockLoggerEvent.Level.WARN, 0);
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    @DisplayName("should report when memory limit has no limit set")
    void testMemoryLimitNoLimitSet() {
        // Given: environment with no memory limit (MAX_VALUE)
        final Map<String, String> env = new HashMap<>();
        final Map<String, String> fileContent = new HashMap<>();
        fileContent.put("/sys/fs/cgroup/memory/memory.limit_in_bytes", String.valueOf(Long.MAX_VALUE));

        final ReportContainerInfo reporter = createReportContainerInfo(env, fileContent);

        // When: report is executed
        reporter.run();

        // Then: should log no memory limit set
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
            "Container Info:",
            " - Memory Limit: No limit set");
        AssertLogger.assertEventCountByLevel(logger, MockLoggerEvent.Level.WARN, 0);
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    @DisplayName("should report when CPU limit has no limit set")
    void testCpuLimitNoLimitSet() {
        // Given: environment with no CPU limit (quota = -1)
        final Map<String, String> env = new HashMap<>();
        final Map<String, String> fileContent = new HashMap<>();
        fileContent.put("/sys/fs/cgroup/cpu/cpu.cfs_quota_us", "-1");
        fileContent.put("/sys/fs/cgroup/cpu/cpu.cfs_period_us", "100000");

        final ReportContainerInfo reporter = createReportContainerInfo(env, fileContent);

        // When: report is executed
        reporter.run();

        // Then: should log no CPU limit set
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
            "Container Info:",
            " - CPU Limit: No limit set");
        AssertLogger.assertEventCountByLevel(logger, MockLoggerEvent.Level.WARN, 0);
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    @DisplayName("should report when CPU limit has zero quota")
    void testCpuLimitZeroQuota() {
        // Given: environment with zero CPU quota
        final Map<String, String> env = new HashMap<>();
        final Map<String, String> fileContent = new HashMap<>();
        fileContent.put("/sys/fs/cgroup/cpu/cpu.cfs_quota_us", "0");
        fileContent.put("/sys/fs/cgroup/cpu/cpu.cfs_period_us", "100000");

        final ReportContainerInfo reporter = createReportContainerInfo(env, fileContent);

        // When: report is executed
        reporter.run();

        // Then: should log no CPU limit set
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
            "Container Info:",
            " - CPU Limit: No limit set");
        AssertLogger.assertEventCountByLevel(logger, MockLoggerEvent.Level.WARN, 0);
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    @DisplayName("should report when CPU limit has zero period")
    void testCpuLimitZeroPeriod() {
        // Given: environment with zero CPU period
        final Map<String, String> env = new HashMap<>();
        final Map<String, String> fileContent = new HashMap<>();
        fileContent.put("/sys/fs/cgroup/cpu/cpu.cfs_quota_us", "100000");
        fileContent.put("/sys/fs/cgroup/cpu/cpu.cfs_period_us", "0");

        final ReportContainerInfo reporter = createReportContainerInfo(env, fileContent);

        // When: report is executed
        reporter.run();

        // Then: should log no CPU limit set and no warnings
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
            "Container Info:",
            " - CPU Limit: No limit set");
        AssertLogger.assertEventCountByLevel(logger, MockLoggerEvent.Level.WARN, 0);
        assertTrue(ConfigParser.isInitializationOK());
    }
}
