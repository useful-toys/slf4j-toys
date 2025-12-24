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

 import org.junit.jupiter.api.BeforeAll;
 import org.junit.jupiter.api.BeforeEach;
 import org.junit.jupiter.api.Test;
 import org.slf4j.LoggerFactory;
 import org.slf4j.impl.MockLogger;
 import org.usefultoys.slf4j.SessionConfig;

 import java.nio.charset.Charset;

 import static org.junit.jupiter.api.Assertions.assertEquals;
 import static org.junit.jupiter.api.Assertions.assertTrue;
 import static org.mockito.Mockito.doThrow;
 import static org.mockito.Mockito.spy;

/**
 * Tests SecurityException handling when accessing system properties.
 * <p>
 * Uses Mockito spy to simulate SecurityException, compatible with Java 21+ where SecurityManager was removed.
 */
class ReportSystemPropertiesSecurityExceptionTest {

    @BeforeAll
    static void validateConsistentCharset() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    private MockLogger mockLogger;

    @BeforeEach
    void setUp() {
        mockLogger = (MockLogger) LoggerFactory.getLogger(ReportSystemProperties.class);
        mockLogger.setEnabled(true);
        mockLogger.clearEvents();
    }

    @Test
    void shouldHandleSecurityExceptionWhenAccessingSystemProperties() {
        final ReportSystemProperties reporter = spy(new ReportSystemProperties(mockLogger));
        doThrow(new SecurityException("Access to system properties denied for testing"))
                .when(reporter).getSystemProperties();

        reporter.run();

        assertTrue(mockLogger.getEventCount() > 0, "should have logged at least one event");
        final String logs = mockLogger.getEvent(0).getFormattedMessage();
        assertTrue(logs.contains("System Properties: access denied"),
                   "should contain access denied message, but got: " + logs);
    }
}