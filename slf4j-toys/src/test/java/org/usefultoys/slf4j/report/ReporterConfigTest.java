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
import org.usefultoys.slf4j.SessionConfig;
import org.usefultoys.slf4j.utils.ConfigParser;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.*;

class ReporterConfigTest {

    @BeforeAll
    static void validate() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    @BeforeEach
    void setUp() {
        // Limpa as propriedades do sistema para garantir um estado limpo
        ReporterConfig.reset();
        // Limpa os erros de inicialização do ConfigParser
        ConfigParser.clearInitializationErrors();
        // Reinicializa o ReporterConfig
        ReporterConfig.init();
    }

    @AfterEach
    void tearDown() {
        ReporterConfig.reset();
        ConfigParser.clearInitializationErrors();
    }

    @Test
    void testDefaultValues() {
        ReporterConfig.init();
        assertTrue(ReporterConfig.reportVM, "Default value for reportVM should be true");
        assertFalse(ReporterConfig.reportFileSystem, "Default value for reportFileSystem should be false");
        assertTrue(ReporterConfig.reportMemory, "Default value for reportMemory should be true");
        assertTrue(ReporterConfig.reportUser, "Default value for reportUser should be true");
        assertTrue(ReporterConfig.reportProperties, "Default value for reportProperties should be true");
        assertFalse(ReporterConfig.reportEnvironment, "Default value for reportEnvironment should be false");
        assertTrue(ReporterConfig.reportPhysicalSystem, "Default value for reportPhysicalSystem should be true");
        assertTrue(ReporterConfig.reportOperatingSystem, "Default value for reportOperatingSystem should be true");
        assertTrue(ReporterConfig.reportCalendar, "Default value for reportCalendar should be true");
        assertTrue(ReporterConfig.reportLocale, "Default value for reportLocale should be true");
        assertTrue(ReporterConfig.reportCharset, "Default value for reportCharset should be true");
        assertFalse(ReporterConfig.reportNetworkInterface, "Default value for reportNetworkInterface should be false");
        assertFalse(ReporterConfig.reportSSLContext, "Default value for reportSSLContext should be false");
        assertFalse(ReporterConfig.reportDefaultTrustKeyStore, "Default value for reportDefaultTrustKeyStore should be false");
        assertEquals("report", ReporterConfig.name, "Default value for name should be 'report'");
        assertEquals("(?i).*password.*|.*secret.*|.*key.*|.*token.*", ReporterConfig.forbiddenPropertyNamesRegex, "Default value for forbiddenPropertyNamesRegex should be the security regex");
        assertTrue(ConfigParser.isInitializationOK(), "No errors should be reported for default values");
    }

    @Test
    void testResetValues() {
        // Set some properties to non-default values
        System.setProperty(ReporterConfig.PROP_VM, "false");
        System.setProperty(ReporterConfig.PROP_FILE_SYSTEM, "true");
        System.setProperty(ReporterConfig.PROP_NAME, "custom");
        System.setProperty(ReporterConfig.PROP_FORBIDDEN_PROPERTY_NAMES_REGEX, ".*custom.*");
        ReporterConfig.init();
        assertFalse(ReporterConfig.reportVM);
        assertTrue(ReporterConfig.reportFileSystem);
        assertEquals("custom", ReporterConfig.name);
        assertEquals(".*custom.*", ReporterConfig.forbiddenPropertyNamesRegex);

        // Reset and check if they return to default
        ReporterConfig.reset();
        assertTrue(ReporterConfig.reportVM);
        assertFalse(ReporterConfig.reportFileSystem);
        assertEquals("report", ReporterConfig.name);
        assertEquals("(?i).*password.*|.*secret.*|.*key.*|.*token.*", ReporterConfig.forbiddenPropertyNamesRegex);
        assertTrue(ConfigParser.isInitializationOK(), "No errors should be reported after reset");
    }

    @Test
    void testReportVMProperty() {
        System.setProperty(ReporterConfig.PROP_VM, "false");
        ReporterConfig.init();
        assertFalse(ReporterConfig.reportVM, "reportVM should reflect the system property value");
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    void testReportVMInvalidFormat() {
        System.setProperty(ReporterConfig.PROP_VM, "invalid");
        ReporterConfig.init();
        assertTrue(ReporterConfig.reportVM, "reportVM should fall back to default for invalid format"); // Default is true
        assertFalse(ConfigParser.isInitializationOK());
        assertEquals(1, ConfigParser.initializationErrors.size());
        assertTrue(ConfigParser.initializationErrors.get(0).contains("Invalid boolean value for property '" + ReporterConfig.PROP_VM));
    }

    @Test
    void testReportFileSystemProperty() {
        System.setProperty(ReporterConfig.PROP_FILE_SYSTEM, "true");
        ReporterConfig.init();
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    void testReportFileSystemInvalidFormat() {
        System.setProperty(ReporterConfig.PROP_FILE_SYSTEM, "invalid");
        ReporterConfig.init();
        assertFalse(ReporterConfig.reportFileSystem, "reportFileSystem should fall back to default for invalid format"); // Default is false
        assertFalse(ConfigParser.isInitializationOK());
        assertEquals(1, ConfigParser.initializationErrors.size());
        assertTrue(ConfigParser.initializationErrors.get(0).contains("Invalid boolean value for property '" + ReporterConfig.PROP_FILE_SYSTEM));
    }

    @Test
    void testReportMemoryProperty() {
        System.setProperty(ReporterConfig.PROP_MEMORY, "false");
        ReporterConfig.init();
        assertFalse(ReporterConfig.reportMemory, "reportMemory should reflect the system property value");
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    void testReportMemoryInvalidFormat() {
        System.setProperty(ReporterConfig.PROP_MEMORY, "invalid");
        ReporterConfig.init();
        assertTrue(ReporterConfig.reportMemory, "reportMemory should fall back to default for invalid format"); // Default is true
        assertFalse(ConfigParser.isInitializationOK());
        assertEquals(1, ConfigParser.initializationErrors.size());
        assertTrue(ConfigParser.initializationErrors.get(0).contains("Invalid boolean value for property '" + ReporterConfig.PROP_MEMORY));
    }

    @Test
    void testReportUserProperty() {
        System.setProperty(ReporterConfig.PROP_USER, "false");
        ReporterConfig.init();
        assertFalse(ReporterConfig.reportUser, "reportUser should reflect the system property value");
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    void testReportUserInvalidFormat() {
        System.setProperty(ReporterConfig.PROP_USER, "invalid");
        ReporterConfig.init();
        assertTrue(ReporterConfig.reportUser, "reportUser should fall back to default for invalid format"); // Default is true
        assertFalse(ConfigParser.isInitializationOK());
        assertEquals(1, ConfigParser.initializationErrors.size());
        assertTrue(ConfigParser.initializationErrors.get(0).contains("Invalid boolean value for property '" + ReporterConfig.PROP_USER));
    }

    @Test
    void testReportPropertiesProperty() {
        System.setProperty(ReporterConfig.PROP_PROPERTIES, "false");
        ReporterConfig.init();
        assertFalse(ReporterConfig.reportProperties, "reportProperties should reflect the system property value");
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    void testReportPropertiesInvalidFormat() {
        System.setProperty(ReporterConfig.PROP_PROPERTIES, "invalid");
        ReporterConfig.init();
        assertTrue(ReporterConfig.reportProperties, "reportProperties should fall back to default for invalid format"); // Default is true
        assertFalse(ConfigParser.isInitializationOK());
        assertEquals(1, ConfigParser.initializationErrors.size());
        assertTrue(ConfigParser.initializationErrors.get(0).contains("Invalid boolean value for property '" + ReporterConfig.PROP_PROPERTIES));
    }

    @Test
    void testReportEnvironmentProperty() {
        System.setProperty(ReporterConfig.PROP_ENVIRONMENT, "true");
        ReporterConfig.init();
        assertTrue(ReporterConfig.reportEnvironment, "reportEnvironment should reflect the system property value");
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    void testReportEnvironmentInvalidFormat() {
        System.setProperty(ReporterConfig.PROP_ENVIRONMENT, "invalid");
        ReporterConfig.init();
        assertFalse(ReporterConfig.reportEnvironment, "reportEnvironment should fall back to default for invalid format"); // Default is false
        assertFalse(ConfigParser.isInitializationOK());
        assertEquals(1, ConfigParser.initializationErrors.size());
        assertTrue(ConfigParser.initializationErrors.get(0).contains("Invalid boolean value for property '" + ReporterConfig.PROP_ENVIRONMENT));
    }

    @Test
    void testReportPhysicalSystemProperty() {
        System.setProperty(ReporterConfig.PROP_PHYSICAL_SYSTEM, "false");
        ReporterConfig.init();
        assertFalse(ReporterConfig.reportPhysicalSystem, "reportPhysicalSystem should reflect the system property value");
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    void testReportPhysicalSystemInvalidFormat() {
        System.setProperty(ReporterConfig.PROP_PHYSICAL_SYSTEM, "invalid");
        ReporterConfig.init();
        assertTrue(ReporterConfig.reportPhysicalSystem, "reportPhysicalSystem should fall back to default for invalid format"); // Default is true
        assertFalse(ConfigParser.isInitializationOK());
        assertEquals(1, ConfigParser.initializationErrors.size());
        assertTrue(ConfigParser.initializationErrors.get(0).contains("Invalid boolean value for property '" + ReporterConfig.PROP_PHYSICAL_SYSTEM));
    }

    @Test
    void testReportOperatingSystemProperty() {
        System.setProperty(ReporterConfig.PROP_OPERATING_SYSTEM, "false");
        ReporterConfig.init();
        assertFalse(ReporterConfig.reportOperatingSystem, "reportOperatingSystem should reflect the system property value");
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    void testReportOperatingSystemInvalidFormat() {
        System.setProperty(ReporterConfig.PROP_OPERATING_SYSTEM, "invalid");
        ReporterConfig.init();
        assertTrue(ReporterConfig.reportOperatingSystem, "reportOperatingSystem should fall back to default for invalid format"); // Default is true
        assertFalse(ConfigParser.isInitializationOK());
        assertEquals(1, ConfigParser.initializationErrors.size());
        assertTrue(ConfigParser.initializationErrors.get(0).contains("Invalid boolean value for property '" + ReporterConfig.PROP_OPERATING_SYSTEM));
    }

    @Test
    void testReportCalendarProperty() {
        System.setProperty(ReporterConfig.PROP_CALENDAR, "false");
        ReporterConfig.init();
        assertFalse(ReporterConfig.reportCalendar, "reportCalendar should reflect the system property value");
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    void testReportCalendarInvalidFormat() {
        System.setProperty(ReporterConfig.PROP_CALENDAR, "invalid");
        ReporterConfig.init();
        assertTrue(ReporterConfig.reportCalendar, "reportCalendar should fall back to default for invalid format"); // Default is true
        assertFalse(ConfigParser.isInitializationOK());
        assertEquals(1, ConfigParser.initializationErrors.size());
        assertTrue(ConfigParser.initializationErrors.get(0).contains("Invalid boolean value for property '" + ReporterConfig.PROP_CALENDAR));
    }

    @Test
    void testReportLocaleProperty() {
        System.setProperty(ReporterConfig.PROP_LOCALE, "false");
        ReporterConfig.init();
        assertFalse(ReporterConfig.reportLocale, "reportLocale should reflect the system property value");
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    void testReportLocaleInvalidFormat() {
        System.setProperty(ReporterConfig.PROP_LOCALE, "invalid");
        ReporterConfig.init();
        assertTrue(ReporterConfig.reportLocale, "reportLocale should fall back to default for invalid format"); // Default is true
        assertFalse(ConfigParser.isInitializationOK());
        assertEquals(1, ConfigParser.initializationErrors.size());
        assertTrue(ConfigParser.initializationErrors.get(0).contains("Invalid boolean value for property '" + ReporterConfig.PROP_LOCALE));
    }

    @Test
    void testReportCharsetProperty() {
        System.setProperty(ReporterConfig.PROP_CHARSET, "false");
        ReporterConfig.init();
        assertFalse(ReporterConfig.reportCharset, "reportCharset should reflect the system property value");
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    void testReportCharsetInvalidFormat() {
        System.setProperty(ReporterConfig.PROP_CHARSET, "invalid");
        ReporterConfig.init();
        assertTrue(ReporterConfig.reportCharset, "reportCharset should fall back to default for invalid format"); // Default is true
        assertFalse(ConfigParser.isInitializationOK());
        assertEquals(1, ConfigParser.initializationErrors.size());
        assertTrue(ConfigParser.initializationErrors.get(0).contains("Invalid boolean value for property '" + ReporterConfig.PROP_CHARSET));
    }

    @Test
    void testReportNetworkInterfaceProperty() {
        System.setProperty(ReporterConfig.PROP_NETWORK_INTERFACE, "true");
        ReporterConfig.init();
        assertTrue(ReporterConfig.reportNetworkInterface, "reportNetworkInterface should reflect the system property value");
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    void testReportNetworkInterfaceInvalidFormat() {
        System.setProperty(ReporterConfig.PROP_NETWORK_INTERFACE, "invalid");
        ReporterConfig.init();
        assertFalse(ReporterConfig.reportNetworkInterface, "reportNetworkInterface should fall back to default for invalid format"); // Default is false
        assertFalse(ConfigParser.isInitializationOK());
        assertEquals(1, ConfigParser.initializationErrors.size());
        assertTrue(ConfigParser.initializationErrors.get(0).contains("Invalid boolean value for property '" + ReporterConfig.PROP_NETWORK_INTERFACE));
    }

    @Test
    void testReportSSLContextProperty() {
        System.setProperty(ReporterConfig.PROP_SSL_CONTEXT, "true");
        ReporterConfig.init();
        assertTrue(ReporterConfig.reportSSLContext, "reportSSLContext should reflect the system property value");
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    void testReportSSLContextInvalidFormat() {
        System.setProperty(ReporterConfig.PROP_SSL_CONTEXT, "invalid");
        ReporterConfig.init();
        assertFalse(ReporterConfig.reportSSLContext, "reportSSLContext should fall back to default for invalid format"); // Default is false
        assertFalse(ConfigParser.isInitializationOK());
        assertEquals(1, ConfigParser.initializationErrors.size());
        assertTrue(ConfigParser.initializationErrors.get(0).contains("Invalid boolean value for property '" + ReporterConfig.PROP_SSL_CONTEXT));
    }

    @Test
    void testReportDefaultTrustKeyStoreProperty() {
        System.setProperty(ReporterConfig.PROP_DEFAULT_TRUST_KEYSTORE, "true");
        ReporterConfig.init();
        assertTrue(ReporterConfig.reportDefaultTrustKeyStore, "reportDefaultTrustKeyStore should reflect the system property value");
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    void testReportDefaultTrustKeyStoreInvalidFormat() {
        System.setProperty(ReporterConfig.PROP_DEFAULT_TRUST_KEYSTORE, "invalid");
        ReporterConfig.init();
        assertFalse(ReporterConfig.reportDefaultTrustKeyStore, "reportDefaultTrustKeyStore should fall back to default for invalid format"); // Default is false
        assertFalse(ConfigParser.isInitializationOK());
        assertEquals(1, ConfigParser.initializationErrors.size());
        assertTrue(ConfigParser.initializationErrors.get(0).contains("Invalid boolean value for property '" + ReporterConfig.PROP_DEFAULT_TRUST_KEYSTORE));
    }

    @Test
    void testNameProperty() {
        System.setProperty(ReporterConfig.PROP_NAME, "customReport");
        ReporterConfig.init();
        assertEquals("customReport", ReporterConfig.name, "name should reflect the system property value");
        assertTrue(ConfigParser.isInitializationOK());
    }

    @Test
    void testForbiddenPropertyNamesRegexProperty() {
        System.setProperty(ReporterConfig.PROP_FORBIDDEN_PROPERTY_NAMES_REGEX, ".*custom.*");
        ReporterConfig.init();
        assertEquals(".*custom.*", ReporterConfig.forbiddenPropertyNamesRegex, "forbiddenPropertyNamesRegex should reflect the system property value");
        assertTrue(ConfigParser.isInitializationOK());
    }
}
