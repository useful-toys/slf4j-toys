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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.MockLogger;
import org.slf4j.impl.MockLoggerEvent;
import org.usefultoys.slf4j.utils.ConfigParser;
import org.usefultoys.test.CharsetConsistency;
import org.usefultoys.test.ResetReporterConfig;
import org.usefultoys.test.WithLocale;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith({CharsetConsistency.class, ResetReporterConfig.class})
@WithLocale("en")
class ReportContainerInfoTest {

    private static final String TEST_LOGGER_NAME = "test.logger";
    private MockLogger mockLogger;

    @BeforeEach
    void setUp() {
        Logger testLogger = LoggerFactory.getLogger(TEST_LOGGER_NAME);
        mockLogger = (MockLogger) testLogger;
        mockLogger.clearEvents();
        mockLogger.setInfoEnabled(true); // Ensure INFO level is enabled
        mockLogger.setWarnEnabled(true); // Ensure WARN level is enabled for error reporting
    }

    @AfterEach
    void tearDown() {
    }

    private String getLogOutput() {
        return mockLogger.getLoggerEvents().stream()
                .map(MockLoggerEvent::getFormattedMessage)
                .collect(Collectors.joining("\n"));
    }

    // Helper method to create a ReportContainerInfo instance with mocked environment and file system
    private ReportContainerInfo createReportContainerInfo(Map<String, String> envMap, Map<String, String> fileContentMap) {
        return new ReportContainerInfo(mockLogger) {
            @Override
            protected Map<String, String> getEnvironmentVariables() {
                return envMap;
            }

            @Override
            protected String readFileContent(String path) throws IOException {
                // Simulate IOException if a specific path is marked for it
                if ("throw_io_exception".equals(fileContentMap.get(path))) {
                    throw new IOException("Simulated IO Error for " + path);
                }
                return fileContentMap.get(path);
            }
        };
    }

    @Test
    void testContainerInfoIsReportedForDocker() {
        Map<String, String> env = new HashMap<>();
        env.put("HOSTNAME", "my-docker-container");
        env.put("DOCKER_CONTAINER_ID", "a1b2c3d4e5f6");

        Map<String, String> fileContent = new HashMap<>();
        fileContent.put("/proc/self/cgroup", "1:name=systemd:/docker/a1b2c3d4e5f67890abcdef1234567890");
        fileContent.put("/sys/fs/cgroup/memory/memory.limit_in_bytes", "1073741824"); // 1GB
        fileContent.put("/sys/fs/cgroup/cpu/cpu.cfs_quota_us", "100000"); // 1 CPU
        fileContent.put("/sys/fs/cgroup/cpu/cpu.cfs_period_us", "100000");

        ReportContainerInfo reporter = createReportContainerInfo(env, fileContent);
        reporter.run();

        String logOutput = getLogOutput();
        assertTrue(logOutput.contains("Container Info:"));
        assertTrue(logOutput.contains(" - Hostname: my-docker-container"));
        assertTrue(logOutput.contains(" - Container ID (cgroup): a1b2c3d4e5f67890abcdef1234567890"));
        assertTrue(logOutput.contains(" - Memory Limit: 1073.7MB"));
        assertTrue(logOutput.contains(" - CPU Limit: 1.00 cores"));
        assertTrue(logOutput.contains(" - Docker Container ID (env): a1b2c3d4e5f6"));
        assertTrue(ConfigParser.isInitializationOK());
        assertEquals(0, mockLogger.getLoggerEvents().stream().filter(e -> e.getLevel() == MockLoggerEvent.Level.WARN).count(), "No WARN logs expected");
    }

    @Test
    void testContainerInfoIsReportedForKubernetes() {
        Map<String, String> env = new HashMap<>();
        env.put("HOSTNAME", "my-pod-123");
        env.put("KUBERNETES_POD_NAME", "my-app-pod-xyz");
        env.put("KUBERNETES_NAMESPACE", "production");
        env.put("KUBERNETES_NODE_NAME", "worker-node-1");

        Map<String, String> fileContent = new HashMap<>();
        // Simulate cgroup files not existing for simplicity in Kubernetes context
        fileContent.put("/proc/self/cgroup", null);
        fileContent.put("/sys/fs/cgroup/memory/memory.limit_in_bytes", null);
        fileContent.put("/sys/fs/cgroup/cpu/cpu.cfs_quota_us", null);
        fileContent.put("/sys/fs/cgroup/cpu/cpu.cfs_period_us", null);

        ReportContainerInfo reporter = createReportContainerInfo(env, fileContent);
        reporter.run();

        String logOutput = getLogOutput();
        assertTrue(logOutput.contains("Container Info:"));
        assertTrue(logOutput.contains(" - Hostname: my-pod-123"));
        assertTrue(logOutput.contains(" - Kubernetes Pod Name: my-app-pod-xyz"));
        assertTrue(logOutput.contains(" - Kubernetes Namespace: production"));
        assertTrue(logOutput.contains(" - Kubernetes Node Name: worker-node-1"));
        assertTrue(logOutput.contains(" - Container ID (cgroup): Not available"));
        assertTrue(logOutput.contains(" - Memory Limit: Not available (not in Linux container or no read access)"));
        assertTrue(logOutput.contains(" - CPU Limit: Not available (not in Linux container or no read access)"));
        assertTrue(ConfigParser.isInitializationOK());
        assertEquals(0, mockLogger.getLoggerEvents().stream().filter(e -> e.getLevel() == MockLoggerEvent.Level.WARN).count(), "No WARN logs expected");
    }

    @Test
    void testContainerInfoNotAvailableWhenNotInContainer() {
        Map<String, String> env = Collections.emptyMap();
        Map<String, String> fileContent = new HashMap<>();
        fileContent.put("/proc/self/cgroup", null);
        fileContent.put("/sys/fs/cgroup/memory/memory.limit_in_bytes", null);
        fileContent.put("/sys/fs/cgroup/cpu/cpu.cfs_quota_us", null);
        fileContent.put("/sys/fs/cgroup/cpu/cpu.cfs_period_us", null);

        ReportContainerInfo reporter = createReportContainerInfo(env, fileContent);
        reporter.run();

        String logOutput = getLogOutput();
        assertTrue(logOutput.contains("Container Info:"));
        assertTrue(logOutput.contains(" - Hostname: unknown"));
        assertTrue(logOutput.contains(" - Container ID (cgroup): Not available"));
        assertTrue(logOutput.contains(" - Memory Limit: Not available (not in Linux container or no read access)"));
        assertTrue(logOutput.contains(" - CPU Limit: Not available (not in Linux container or no read access)"));
        assertTrue(ConfigParser.isInitializationOK());
        assertEquals(0, mockLogger.getLoggerEvents().stream().filter(e -> e.getLevel() == MockLoggerEvent.Level.WARN).count(), "No WARN logs expected");
    }

    @Test
    void testCgroupFileReadError() {
        Map<String, String> env = new HashMap<>();
        env.put("HOSTNAME", "test-host");

        Map<String, String> fileContent = new HashMap<>();
        fileContent.put("/proc/self/cgroup", "throw_io_exception"); // Special value to trigger IOException in mock

        ReportContainerInfo reporter = createReportContainerInfo(env, fileContent);
        reporter.run();

        String logOutput = getLogOutput();
        assertTrue(logOutput.contains(" - Hostname: test-host"));
        assertTrue(logOutput.contains(" - Container ID (cgroup): Error reading cgroup file"));
        assertEquals(1, mockLogger.getLoggerEvents().stream().filter(e -> e.getLevel() == MockLoggerEvent.Level.WARN).count(), "One WARN log expected");
        assertTrue(mockLogger.getLoggerEvents().stream().anyMatch(e -> e.getFormattedMessage().contains("Failed to read /proc/self/cgroup: Simulated IO Error for /proc/self/cgroup")));
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    void testMemoryLimitParsingError() {
        Map<String, String> env = new HashMap<>();
        Map<String, String> fileContent = new HashMap<>();
        fileContent.put("/sys/fs/cgroup/memory/memory.limit_in_bytes", "invalid_number");

        ReportContainerInfo reporter = createReportContainerInfo(env, fileContent);
        reporter.run();

        String logOutput = getLogOutput();
        assertTrue(logOutput.contains(" - Memory Limit: Error reading"));
        assertEquals(1, mockLogger.getLoggerEvents().stream().filter(e -> e.getLevel() == MockLoggerEvent.Level.WARN).count(), "One WARN log expected");
        assertTrue(mockLogger.getLoggerEvents().stream().anyMatch(e -> e.getFormattedMessage().contains("Failed to read memory limit from cgroup: For input string: \"invalid_number\"")));
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    void testCpuLimitParsingError() {
        Map<String, String> env = new HashMap<>();
        Map<String, String> fileContent = new HashMap<>();
        fileContent.put("/sys/fs/cgroup/cpu/cpu.cfs_quota_us", "invalid_quota");
        fileContent.put("/sys/fs/cgroup/cpu/cpu.cfs_period_us", "100000");

        ReportContainerInfo reporter = createReportContainerInfo(env, fileContent);
        reporter.run();

        String logOutput = getLogOutput();
        assertTrue(logOutput.contains(" - CPU Limit: Error reading"));
        assertEquals(1, mockLogger.getLoggerEvents().stream().filter(e -> e.getLevel() == MockLoggerEvent.Level.WARN).count(), "One WARN log expected");
        assertTrue(mockLogger.getLoggerEvents().stream().anyMatch(e -> e.getFormattedMessage().contains("Failed to read CPU limit from cgroup: For input string: \"invalid_quota\"")));
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    void testCgroupContainerIdPatternNoMatch() {
        Map<String, String> env = new HashMap<>();
        Map<String, String> fileContent = new HashMap<>();
        fileContent.put("/proc/self/cgroup", "1:name=systemd:/some/other/path"); // Does not match Docker pattern

        ReportContainerInfo reporter = createReportContainerInfo(env, fileContent);
        reporter.run();

        String logOutput = getLogOutput();
        assertTrue(logOutput.contains(" - Container ID (cgroup): Not found in cgroup (not a Docker container?)"));
        assertEquals(0, mockLogger.getLoggerEvents().stream().filter(e -> e.getLevel() == MockLoggerEvent.Level.WARN).count(), "No WARN logs expected");
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    void testMemoryLimitNoLimitSet() {
        Map<String, String> env = new HashMap<>();
        Map<String, String> fileContent = new HashMap<>();
        fileContent.put("/sys/fs/cgroup/memory/memory.limit_in_bytes", String.valueOf(Long.MAX_VALUE)); // Simulate no limit

        ReportContainerInfo reporter = createReportContainerInfo(env, fileContent);
        reporter.run();

        String logOutput = getLogOutput();
        assertTrue(logOutput.contains(" - Memory Limit: No limit set"));
        assertEquals(0, mockLogger.getLoggerEvents().stream().filter(e -> e.getLevel() == MockLoggerEvent.Level.WARN).count(), "No WARN logs expected");
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    void testCpuLimitNoLimitSet() {
        Map<String, String> env = new HashMap<>();
        Map<String, String> fileContent = new HashMap<>();
        fileContent.put("/sys/fs/cgroup/cpu/cpu.cfs_quota_us", "-1"); // -1 indicates no quota
        fileContent.put("/sys/fs/cgroup/cpu/cpu.cfs_period_us", "100000");

        ReportContainerInfo reporter = createReportContainerInfo(env, fileContent);
        reporter.run();

        String logOutput = getLogOutput();
        assertTrue(logOutput.contains(" - CPU Limit: No limit set"));
        assertEquals(0, mockLogger.getLoggerEvents().stream().filter(e -> e.getLevel() == MockLoggerEvent.Level.WARN).count(), "No WARN logs expected");
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    void testCpuLimitZeroQuota() {
        Map<String, String> env = new HashMap<>();
        Map<String, String> fileContent = new HashMap<>();
        fileContent.put("/sys/fs/cgroup/cpu/cpu.cfs_quota_us", "0"); // Zero quota
        fileContent.put("/sys/fs/cgroup/cpu/cpu.cfs_period_us", "100000");

        ReportContainerInfo reporter = createReportContainerInfo(env, fileContent);
        reporter.run();

        String logOutput = getLogOutput();
        assertTrue(logOutput.contains(" - CPU Limit: No limit set"));
        assertEquals(0, mockLogger.getLoggerEvents().stream().filter(e -> e.getLevel() == MockLoggerEvent.Level.WARN).count(), "No WARN logs expected");
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    void testCpuLimitZeroPeriod() {
        Map<String, String> env = new HashMap<>();
        Map<String, String> fileContent = new HashMap<>();
        fileContent.put("/sys/fs/cgroup/cpu/cpu.cfs_quota_us", "100000");
        fileContent.put("/sys/fs/cgroup/cpu/cpu.cfs_period_us", "0"); // Zero period

        ReportContainerInfo reporter = createReportContainerInfo(env, fileContent);
        reporter.run();

        String logOutput = getLogOutput();
        assertTrue(logOutput.contains(" - CPU Limit: No limit set"));
        assertEquals(0, mockLogger.getLoggerEvents().stream().filter(e -> e.getLevel() == MockLoggerEvent.Level.WARN).count(), "No WARN logs expected");
        assertTrue(ConfigParser.isInitializationOK());
    }
}
