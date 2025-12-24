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
 import org.junit.jupiter.api.BeforeAll;
 import org.junit.jupiter.api.BeforeEach;
 import org.junit.jupiter.api.Test;
 import org.slf4j.LoggerFactory;
 import org.slf4j.impl.MockLogger;
 import org.usefultoys.slf4j.SessionConfig;

 import java.nio.charset.Charset;
 import java.security.Permission;
 import java.util.PropertyPermission;

 import static org.junit.jupiter.api.Assertions.assertEquals;
 import static org.junit.jupiter.api.Assertions.assertTrue;
 import static org.mockito.Mockito.doThrow;
 import static org.mockito.Mockito.spy;

/**
 * Tests SecurityManager behavior when accessing system properties.
 * <p>
 * For Java 8-20: Uses actual SecurityManager to test exception handling.
 * For Java 21+: Uses Mockito spy to simulate SecurityException (SecurityManager removed).
 */
class ReportSystemPropertiesSecurityExceptionTest {

    @BeforeAll
    static void validateConsistentCharset() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    private SecurityManager originalSecurityManager;
    private MockLogger mockLogger;
    private static final boolean isJava21Plus = getJavaVersion() >= 21;

    private static int getJavaVersion() {
        String version = System.getProperty("java.version");
        if (version.startsWith("1.")) {
            version = version.substring(2, 3);
        } else {
            int dot = version.indexOf(".");
            if (dot != -1) {
                version = version.substring(0, dot);
            }
        }
        return Integer.parseInt(version);
    }

    @BeforeEach
    void setUp() {
        if (!isJava21Plus) {
            // Salvar o SecurityManager original apenas em Java < 21
            originalSecurityManager = System.getSecurityManager();
        }
        
        // Configurar o logger mock
        mockLogger = (MockLogger) LoggerFactory.getLogger(ReportSystemProperties.class);
        mockLogger.setEnabled(true);
        mockLogger.clearEvents();
    }

    @AfterEach
    void tearDown() {
        if (!isJava21Plus && originalSecurityManager != null) {
            // Restaurar o SecurityManager original apenas em Java < 21
            System.setSecurityManager(originalSecurityManager);
        }
        mockLogger.clearEvents();
    }

    @Test
    void shouldHandleSecurityExceptionWhenAccessingSystemProperties() {
        if (isJava21Plus) {
            // Java 21+: SecurityManager removido, usar spy para simular exceção
            final ReportSystemProperties reporter = spy(new ReportSystemProperties(mockLogger));
            doThrow(new SecurityException("Access to system properties denied for testing"))
                    .when(reporter).getSystemProperties();

            reporter.run();
        } else {
            // Java 8-20: Usar SecurityManager real
            System.setSecurityManager(new SecurityManager() {
                @Override
                public void checkPermission(final Permission perm) {
                    if (perm instanceof PropertyPermission && 
                        perm.getName().equals("*") && 
                        perm.getActions().contains("read")) {
                        throw new SecurityException("Access to system properties denied for testing");
                    }
                }
            });

            final ReportSystemProperties reporter = new ReportSystemProperties(mockLogger);
            reporter.run();
        }

        // Verificar se a mensagem de negação de acesso foi registrada
        assertTrue(mockLogger.getEventCount() > 0, "should have logged at least one event");
        final String logs = mockLogger.getEvent(0).getFormattedMessage();
        assertTrue(logs.contains("System Properties: access denied"),
                   "should contain access denied message, but got: " + logs);
    }
}