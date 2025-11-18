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
 import org.slf4j.LoggerFactory;
 import org.slf4j.impl.MockLogger;

 import java.io.ByteArrayOutputStream;
 import java.security.Permission;

 import static org.junit.jupiter.api.Assertions.assertTrue;

 class ReportSystemEnvironmentSecurityExceptionTest {

    @BeforeAll
    static void validateConsistentCharset() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

     private SecurityManager originalSecurityManager;
     private MockLogger mockLogger;
     private ByteArrayOutputStream logOutput;

     @BeforeEach
     void setUp() {
         // Salvar o SecurityManager original
         originalSecurityManager = System.getSecurityManager();

         // Configurar o logger mock
         mockLogger = (MockLogger) LoggerFactory.getLogger(ReportSystemProperties.class);
         mockLogger.setEnabled(true);
         mockLogger.clearEvents();

         // Capturar saída do logger para verificação
         logOutput = new ByteArrayOutputStream();
         // Configurar o sistema para usar nossa implementação de PrintStream
         // Esta parte pode precisar de ajustes dependendo de como o LoggerFactory está implementado
     }

     @AfterEach
     void tearDown() {
         // Restaurar o SecurityManager original
         System.setSecurityManager(originalSecurityManager);
         mockLogger.clearEvents();
     }

     @Test
     void shouldHandleSecurityExceptionWhenAccessingSystemProperties() {
         // Configurar um SecurityManager que proíbe acesso a System.getProperties()
         System.setSecurityManager(new SecurityManager() {
             @Override
             public void checkPermission(final Permission perm) {
                 if (perm instanceof RuntimePermission &&
                     perm.getName().equals("getenv.*")) {
                     throw new SecurityException("Access to environment properties denied for testing");
                 }
             }
         });

         // Criar e executar o ReportSystemProperties
         final ReportSystemEnvironment reporter = new ReportSystemEnvironment(mockLogger);
         reporter.run();

         // Verificar se a mensagem de negação de acesso foi registrada
         assertTrue(mockLogger.getEventCount() > 0);
         final String logs = mockLogger.getEvent(0).getFormattedMessage();
         assertTrue(logs.contains("System Environment: access denied"));
     }
 }