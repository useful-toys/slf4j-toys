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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.usefultoys.slf4j.utils.ConfigParser;
import org.usefultoys.test.ResetReporterConfig;
import org.usefultoys.test.ValidateCharset;
import org.usefultoys.test.WithLocale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link ReporterConfig}.
 * <p>
 * Tests validate that ReporterConfig correctly parses system properties,
 * initializes default values, handles invalid configurations, and properly resets to defaults.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>Default Initialization:</b> Verifies all properties initialize with correct default values</li>
 *   <li><b>Reset Functionality:</b> Ensures reset() restores all properties to defaults after modification</li>
 *   <li><b>Boolean Properties Parsing:</b> Validates correct parsing of 18 boolean properties (reportVM, reportFileSystem, reportMemory, etc.)</li>
 *   <li><b>String Properties Parsing:</b> Tests parsing of name and forbiddenPropertyNamesRegex string properties</li>
 *   <li><b>Invalid Format Handling:</b> Confirms fallback to defaults when invalid values are provided</li>
 *   <li><b>Error Reporting:</b> Verifies ConfigParser error messages for all invalid property values</li>
 * </ul>
 */
@DisplayName("ReporterConfig")
@ValidateCharset
@ResetReporterConfig
@WithLocale("en")
class ReporterConfigTest {

    @Test
    @DisplayName("should initialize with default values")
    void shouldInitializeWithDefaultValues() {
        // Given: ReporterConfig with no system properties set
        // When: ReporterConfig.init() is called
        ReporterConfig.init();
        // Then: all properties should have their default values
        assertTrue(ReporterConfig.reportVM, "Default value for reportVM should be true");
        assertFalse(ReporterConfig.reportFileSystem, "Default value for reportFileSystem should be false");
        assertTrue(ReporterConfig.reportMemory, "Default value for reportMemory should be true");
        assertFalse(ReporterConfig.reportUser, "Default value for reportUser should be false");
        assertFalse(ReporterConfig.reportProperties, "Default value for reportProperties should be false");
        assertFalse(ReporterConfig.reportEnvironment, "Default value for reportEnvironment should be false");
        assertTrue(ReporterConfig.reportPhysicalSystem, "Default value for reportPhysicalSystem should be true");
        assertTrue(ReporterConfig.reportOperatingSystem, "Default value for reportOperatingSystem should be true");
        assertFalse(ReporterConfig.reportCalendar, "Default value for reportCalendar should be false");
        assertFalse(ReporterConfig.reportLocale, "Default value for reportLocale should be false");
        assertFalse(ReporterConfig.reportCharset, "Default value for reportCharset should be false");
        assertFalse(ReporterConfig.reportNetworkInterface, "Default value for reportNetworkInterface should be false");
        assertFalse(ReporterConfig.reportSSLContext, "Default value for reportSSLContext should be false");
        assertFalse(ReporterConfig.reportDefaultTrustKeyStore, "Default value for reportDefaultTrustKeyStore should be false");
        assertFalse(ReporterConfig.reportJvmArguments, "Default value for reportJvmArguments should be false");
        assertFalse(ReporterConfig.reportClasspath, "Default value for reportClasspath should be false");
        assertFalse(ReporterConfig.reportGarbageCollector, "Default value for reportGarbageCollector should be false");
        assertFalse(ReporterConfig.reportSecurityProviders, "Default value for reportSecurityProviders should be false");
        assertFalse(ReporterConfig.reportContainerInfo, "Default value for reportContainerInfo should be false");
        assertEquals("report", ReporterConfig.name, "Default value for name should be 'report'");
        assertEquals("(?i).*password.*|.*secret.*|.*key.*|.*token.*", ReporterConfig.forbiddenPropertyNamesRegex, "Default value for forbiddenPropertyNamesRegex should be the security regex");
        assertTrue(ConfigParser.isInitializationOK(), "No errors should be reported for default values");
    }

    @Test
    @DisplayName("should reset to default values after modification")
    void shouldResetToDefaultValuesAfterModification() {
        // Given: ReporterConfig properties modified via system properties
        System.setProperty(ReporterConfig.PROP_VM, "false");
        System.setProperty(ReporterConfig.PROP_FILE_SYSTEM, "true");
        System.setProperty(ReporterConfig.PROP_JVM_ARGUMENTS, "true");
        System.setProperty(ReporterConfig.PROP_CLASSPATH, "true");
        System.setProperty(ReporterConfig.PROP_GARBAGE_COLLECTOR, "true");
        System.setProperty(ReporterConfig.PROP_SECURITY_PROVIDERS, "true");
        System.setProperty(ReporterConfig.PROP_CONTAINER_INFO, "true");
        System.setProperty(ReporterConfig.PROP_NAME, "custom");
        System.setProperty(ReporterConfig.PROP_FORBIDDEN_PROPERTY_NAMES_REGEX, ".*custom.*");
        ReporterConfig.init();
        assertFalse(ReporterConfig.reportVM);
        assertTrue(ReporterConfig.reportFileSystem);
        assertTrue(ReporterConfig.reportJvmArguments);
        assertTrue(ReporterConfig.reportClasspath);
        assertTrue(ReporterConfig.reportGarbageCollector);
        assertTrue(ReporterConfig.reportSecurityProviders);
        assertTrue(ReporterConfig.reportContainerInfo);
        assertEquals("custom", ReporterConfig.name);
        assertEquals(".*custom.*", ReporterConfig.forbiddenPropertyNamesRegex);

        // When: ReporterConfig.reset() is called
        ReporterConfig.reset();

        // Then: all properties should return to default values
        assertTrue(ReporterConfig.reportVM);
        assertFalse(ReporterConfig.reportFileSystem);
        assertFalse(ReporterConfig.reportJvmArguments);
        assertFalse(ReporterConfig.reportClasspath);
        assertFalse(ReporterConfig.reportGarbageCollector);
        assertFalse(ReporterConfig.reportSecurityProviders);
        assertFalse(ReporterConfig.reportContainerInfo);
        assertEquals("report", ReporterConfig.name);
        assertEquals("(?i).*password.*|.*secret.*|.*key.*|.*token.*", ReporterConfig.forbiddenPropertyNamesRegex);
        assertTrue(ConfigParser.isInitializationOK(), "No errors should be reported after reset");
    }

    @Test
    @DisplayName("should parse reportVM property correctly")
    void shouldParseReportVMPropertyCorrectly() {
        // Given: reportVM system property set to "false"
        System.setProperty(ReporterConfig.PROP_VM, "false");
        // When: ReporterConfig.init() is called
        ReporterConfig.init();
        // Then: reportVM should be false and no errors should be reported
        assertFalse(ReporterConfig.reportVM, "reportVM should be false when set via system property");
        assertTrue(ConfigParser.isInitializationOK(), "No errors should be reported for valid property value");
    }

    @Test
    @DisplayName("should handle invalid reportVM format with fallback to default")
    void shouldHandleInvalidReportVMFormatWithFallbackToDefault() {
        System.setProperty(ReporterConfig.PROP_VM, "invalid");
        ReporterConfig.init();
        assertTrue(ReporterConfig.reportVM, "reportVM should fall back to default (true) when value is invalid");
        assertFalse(ConfigParser.isInitializationOK(), "Configuration error should be reported for invalid value");
        assertEquals(1, ConfigParser.initializationErrors.size(), "Exactly one error should be reported");
        assertTrue(ConfigParser.initializationErrors.get(0).contains("Invalid boolean value for property '" + ReporterConfig.PROP_VM), "Error message should mention the invalid property");
    }

    @Test
    @DisplayName("should parse reportFileSystem property correctly")
    void shouldParseReportFileSystemPropertyCorrectly() {
        // Given: reportFileSystem system property set to "true"
        System.setProperty(ReporterConfig.PROP_FILE_SYSTEM, "true");
        // When: ReporterConfig.init() is called
        ReporterConfig.init();
        // Then: reportFileSystem should be true and no errors should be reported
        assertTrue(ReporterConfig.reportFileSystem, "reportFileSystem should be true when set via system property");
        assertTrue(ConfigParser.isInitializationOK(), "No errors should be reported for valid property value");
    }

    @Test
    @DisplayName("should handle invalid reportFileSystem format with fallback to default")
    void shouldHandleInvalidReportFileSystemFormatWithFallbackToDefault() {
        // Given: reportFileSystem system property set to invalid value "invalid"
        System.setProperty(ReporterConfig.PROP_FILE_SYSTEM, "invalid");
        // When: ReporterConfig.init() is called
        ReporterConfig.init();
        // Then: reportFileSystem should fall back to default (false), error should be reported
        assertFalse(ReporterConfig.reportFileSystem, "reportFileSystem should fall back to default (false) when value is invalid");
        assertFalse(ConfigParser.isInitializationOK(), "Configuration error should be reported for invalid value");
        assertEquals(1, ConfigParser.initializationErrors.size(), "Exactly one error should be reported");
        assertTrue(ConfigParser.initializationErrors.get(0).contains("Invalid boolean value for property '" + ReporterConfig.PROP_FILE_SYSTEM), "Error message should mention the invalid property");
    }

    @Test
    @DisplayName("should parse reportMemory property correctly")
    void shouldParseReportMemoryPropertyCorrectly() {
        // Given: reportMemory system property set to "false"
        System.setProperty(ReporterConfig.PROP_MEMORY, "false");
        // When: ReporterConfig.init() is called
        ReporterConfig.init();
        // Then: reportMemory should be false and no errors should be reported
        assertFalse(ReporterConfig.reportMemory, "reportMemory should be false when set via system property");
        assertTrue(ConfigParser.isInitializationOK(), "No errors should be reported for valid property value");
    }

    @Test
    @DisplayName("should handle invalid reportMemory format with fallback to default")
    void shouldHandleInvalidReportMemoryFormatWithFallbackToDefault() {
        // Given: reportMemory system property set to invalid value "invalid"
        System.setProperty(ReporterConfig.PROP_MEMORY, "invalid");
        // When: ReporterConfig.init() is called
        ReporterConfig.init();
        // Then: reportMemory should fall back to default (true), error should be reported
        assertTrue(ReporterConfig.reportMemory, "reportMemory should fall back to default (true) when value is invalid");
        assertFalse(ConfigParser.isInitializationOK(), "Configuration error should be reported for invalid value");
        assertEquals(1, ConfigParser.initializationErrors.size(), "Exactly one error should be reported");
        assertTrue(ConfigParser.initializationErrors.get(0).contains("Invalid boolean value for property '" + ReporterConfig.PROP_MEMORY), "Error message should mention the invalid property");
    }

    @Test
    @DisplayName("should parse reportUser property correctly")
    void shouldParseReportUserPropertyCorrectly() {
        // Given: reportUser system property set to "false"
        System.setProperty(ReporterConfig.PROP_USER, "false");
        // When: ReporterConfig.init() is called
        ReporterConfig.init();
        // Then: reportUser should be false and no errors should be reported
        assertFalse(ReporterConfig.reportUser, "reportUser should be false when set via system property");
        assertTrue(ConfigParser.isInitializationOK(), "No errors should be reported for valid property value");
    }

    @Test
    @DisplayName("should handle invalid reportUser format with fallback to default")
    void shouldHandleInvalidReportUserFormatWithFallbackToDefault() {
        // Given: reportUser system property set to invalid value "invalid"
        System.setProperty(ReporterConfig.PROP_USER, "invalid");
        // When: ReporterConfig.init() is called
        ReporterConfig.init();
        // Then: reportUser should fall back to default (false), error should be reported
        assertFalse(ReporterConfig.reportUser, "reportUser should fall back to default (false) when value is invalid");
        assertFalse(ConfigParser.isInitializationOK(), "Configuration error should be reported for invalid value");
        assertEquals(1, ConfigParser.initializationErrors.size(), "Exactly one error should be reported");
        assertTrue(ConfigParser.initializationErrors.get(0).contains("Invalid boolean value for property '" + ReporterConfig.PROP_USER), "Error message should mention the invalid property");
    }

    @Test
    @DisplayName("should parse reportProperties property correctly")
    void shouldParseReportPropertiesPropertyCorrectly() {
        // Given: reportProperties system property set to "false"
        System.setProperty(ReporterConfig.PROP_PROPERTIES, "false");
        // When: ReporterConfig.init() is called
        ReporterConfig.init();
        // Then: reportProperties should be false and no errors should be reported
        assertFalse(ReporterConfig.reportProperties, "reportProperties should be false when set via system property");
        assertTrue(ConfigParser.isInitializationOK(), "No errors should be reported for valid property value");
    }

    @Test
    @DisplayName("should handle invalid reportProperties format with fallback to default")
    void shouldHandleInvalidReportPropertiesFormatWithFallbackToDefault() {
        // Given: reportProperties system property set to invalid value "invalid"
        System.setProperty(ReporterConfig.PROP_PROPERTIES, "invalid");
        // When: ReporterConfig.init() is called
        ReporterConfig.init();
        // Then: reportProperties should fall back to default (false), error should be reported
        assertFalse(ReporterConfig.reportProperties, "reportProperties should fall back to default (false) when value is invalid");
        assertFalse(ConfigParser.isInitializationOK(), "Configuration error should be reported for invalid value");
        assertEquals(1, ConfigParser.initializationErrors.size(), "Exactly one error should be reported");
        assertTrue(ConfigParser.initializationErrors.get(0).contains("Invalid boolean value for property '" + ReporterConfig.PROP_PROPERTIES), "Error message should mention the invalid property");
    }

    @Test
    @DisplayName("should parse reportEnvironment property correctly")
    void shouldParseReportEnvironmentPropertyCorrectly() {
        // Given: reportEnvironment system property set to "true"
        System.setProperty(ReporterConfig.PROP_ENVIRONMENT, "true");
        // When: ReporterConfig.init() is called
        ReporterConfig.init();
        // Then: reportEnvironment should be true and no errors should be reported
        assertTrue(ReporterConfig.reportEnvironment, "reportEnvironment should be true when set via system property");
        assertTrue(ConfigParser.isInitializationOK(), "No errors should be reported for valid property value");
    }

    @Test
    @DisplayName("should handle invalid reportEnvironment format with fallback to default")
    void shouldHandleInvalidReportEnvironmentFormatWithFallbackToDefault() {
        // Given: reportEnvironment system property set to invalid value "invalid"
        System.setProperty(ReporterConfig.PROP_ENVIRONMENT, "invalid");
        // When: ReporterConfig.init() is called
        ReporterConfig.init();
        // Then: reportEnvironment should fall back to default (false), error should be reported
        assertFalse(ReporterConfig.reportEnvironment, "reportEnvironment should fall back to default (false) when value is invalid");
        assertFalse(ConfigParser.isInitializationOK(), "Configuration error should be reported for invalid value");
        assertEquals(1, ConfigParser.initializationErrors.size(), "Exactly one error should be reported");
        assertTrue(ConfigParser.initializationErrors.get(0).contains("Invalid boolean value for property '" + ReporterConfig.PROP_ENVIRONMENT), "Error message should mention the invalid property");
    }

    @Test
    @DisplayName("should parse reportPhysicalSystem property correctly")
    void shouldParseReportPhysicalSystemPropertyCorrectly() {
        // Given: reportPhysicalSystem system property set to "false"
        System.setProperty(ReporterConfig.PROP_PHYSICAL_SYSTEM, "false");
        // When: ReporterConfig.init() is called
        ReporterConfig.init();
        // Then: reportPhysicalSystem should be false and no errors should be reported
        assertFalse(ReporterConfig.reportPhysicalSystem, "reportPhysicalSystem should be false when set via system property");
        assertTrue(ConfigParser.isInitializationOK(), "No errors should be reported for valid property value");
    }

    @Test
    @DisplayName("should handle invalid reportPhysicalSystem format with fallback to default")
    void shouldHandleInvalidReportPhysicalSystemFormatWithFallbackToDefault() {
        // Given: reportPhysicalSystem system property set to invalid value "invalid"
        System.setProperty(ReporterConfig.PROP_PHYSICAL_SYSTEM, "invalid");
        // When: ReporterConfig.init() is called
        ReporterConfig.init();
        // Then: reportPhysicalSystem should fall back to default (true), error should be reported
        assertTrue(ReporterConfig.reportPhysicalSystem, "reportPhysicalSystem should fall back to default (true) when value is invalid");
        assertFalse(ConfigParser.isInitializationOK(), "Configuration error should be reported for invalid value");
        assertEquals(1, ConfigParser.initializationErrors.size(), "Exactly one error should be reported");
        assertTrue(ConfigParser.initializationErrors.get(0).contains("Invalid boolean value for property '" + ReporterConfig.PROP_PHYSICAL_SYSTEM), "Error message should mention the invalid property");
    }

    @Test
    @DisplayName("should parse reportOperatingSystem property correctly")
    void shouldParseReportOperatingSystemPropertyCorrectly() {
        // Given: reportOperatingSystem system property set to "false"
        System.setProperty(ReporterConfig.PROP_OPERATING_SYSTEM, "false");
        // When: ReporterConfig.init() is called
        ReporterConfig.init();
        // Then: reportOperatingSystem should be false and no errors should be reported
        assertFalse(ReporterConfig.reportOperatingSystem, "reportOperatingSystem should be false when set via system property");
        assertTrue(ConfigParser.isInitializationOK(), "No errors should be reported for valid property value");
    }

    @Test
    @DisplayName("should handle invalid reportOperatingSystem format with fallback to default")
    void shouldHandleInvalidReportOperatingSystemFormatWithFallbackToDefault() {
        // Given: reportOperatingSystem system property set to invalid value "invalid"
        System.setProperty(ReporterConfig.PROP_OPERATING_SYSTEM, "invalid");
        // When: ReporterConfig.init() is called
        ReporterConfig.init();
        // Then: reportOperatingSystem should fall back to default (true), error should be reported
        assertTrue(ReporterConfig.reportOperatingSystem, "reportOperatingSystem should fall back to default (true) when value is invalid");
        assertFalse(ConfigParser.isInitializationOK(), "Configuration error should be reported for invalid value");
        assertEquals(1, ConfigParser.initializationErrors.size(), "Exactly one error should be reported");
        assertTrue(ConfigParser.initializationErrors.get(0).contains("Invalid boolean value for property '" + ReporterConfig.PROP_OPERATING_SYSTEM), "Error message should mention the invalid property");
    }

    @Test
    @DisplayName("should parse reportCalendar property correctly")
    void shouldParseReportCalendarPropertyCorrectly() {
        // Given: reportCalendar system property set to "false"
        System.setProperty(ReporterConfig.PROP_CALENDAR, "false");
        // When: ReporterConfig.init() is called
        ReporterConfig.init();
        // Then: reportCalendar should be false and no errors should be reported
        assertFalse(ReporterConfig.reportCalendar, "reportCalendar should be false when set via system property");
        assertTrue(ConfigParser.isInitializationOK(), "No errors should be reported for valid property value");
    }

    @Test
    @DisplayName("should handle invalid reportCalendar format with fallback to default")
    void shouldHandleInvalidReportCalendarFormatWithFallbackToDefault() {
        // Given: reportCalendar system property set to invalid value "invalid"
        System.setProperty(ReporterConfig.PROP_CALENDAR, "invalid");
        // When: ReporterConfig.init() is called
        ReporterConfig.init();
        // Then: reportCalendar should fall back to default (false), error should be reported
        assertFalse(ReporterConfig.reportCalendar, "reportCalendar should fall back to default (false) when value is invalid");
        assertFalse(ConfigParser.isInitializationOK(), "Configuration error should be reported for invalid value");
        assertEquals(1, ConfigParser.initializationErrors.size(), "Exactly one error should be reported");
        assertTrue(ConfigParser.initializationErrors.get(0).contains("Invalid boolean value for property '" + ReporterConfig.PROP_CALENDAR), "Error message should mention the invalid property");
    }

    @Test
    @DisplayName("should parse reportLocale property correctly")
    void shouldParseReportLocalePropertyCorrectly() {
        // Given: reportLocale system property set to "false"
        System.setProperty(ReporterConfig.PROP_LOCALE, "false");
        // When: ReporterConfig.init() is called
        ReporterConfig.init();
        // Then: reportLocale should be false and no errors should be reported
        assertFalse(ReporterConfig.reportLocale, "reportLocale should be false when set via system property");
        assertTrue(ConfigParser.isInitializationOK(), "No errors should be reported for valid property value");
    }

    @Test
    @DisplayName("should handle invalid reportLocale format with fallback to default")
    void shouldHandleInvalidReportLocaleFormatWithFallbackToDefault() {
        // Given: reportLocale system property set to invalid value "invalid"
        System.setProperty(ReporterConfig.PROP_LOCALE, "invalid");
        // When: ReporterConfig.init() is called
        ReporterConfig.init();
        // Then: reportLocale should fall back to default (false), error should be reported
        assertFalse(ReporterConfig.reportLocale, "reportLocale should fall back to default (false) when value is invalid");
        assertFalse(ConfigParser.isInitializationOK(), "Configuration error should be reported for invalid value");
        assertEquals(1, ConfigParser.initializationErrors.size(), "Exactly one error should be reported");
        assertTrue(ConfigParser.initializationErrors.get(0).contains("Invalid boolean value for property '" + ReporterConfig.PROP_LOCALE), "Error message should mention the invalid property");
    }

    @Test
    @DisplayName("should parse reportCharset property correctly")
    void shouldParseReportCharsetPropertyCorrectly() {
        // Given: reportCharset system property set to "false"
        System.setProperty(ReporterConfig.PROP_CHARSET, "false");
        // When: ReporterConfig.init() is called
        ReporterConfig.init();
        // Then: reportCharset should be false and no errors should be reported
        assertFalse(ReporterConfig.reportCharset, "reportCharset should be false when set via system property");
        assertTrue(ConfigParser.isInitializationOK(), "No errors should be reported for valid property value");
    }

    @Test
    @DisplayName("should handle invalid reportCharset format with fallback to default")
    void shouldHandleInvalidReportCharsetFormatWithFallbackToDefault() {
        // Given: reportCharset system property set to invalid value "invalid"
        System.setProperty(ReporterConfig.PROP_CHARSET, "invalid");
        // When: ReporterConfig.init() is called
        ReporterConfig.init();
        // Then: reportCharset should fall back to default (false), error should be reported
        assertFalse(ReporterConfig.reportCharset, "reportCharset should fall back to default (false) when value is invalid");
        assertFalse(ConfigParser.isInitializationOK(), "Configuration error should be reported for invalid value");
        assertEquals(1, ConfigParser.initializationErrors.size(), "Exactly one error should be reported");
        assertTrue(ConfigParser.initializationErrors.get(0).contains("Invalid boolean value for property '" + ReporterConfig.PROP_CHARSET), "Error message should mention the invalid property");
    }

    @Test
    @DisplayName("should parse reportNetworkInterface property correctly")
    void shouldParseReportNetworkInterfacePropertyCorrectly() {
        // Given: reportNetworkInterface system property set to "true"
        System.setProperty(ReporterConfig.PROP_NETWORK_INTERFACE, "true");
        // When: ReporterConfig.init() is called
        ReporterConfig.init();
        // Then: reportNetworkInterface should be true and no errors should be reported
        assertTrue(ReporterConfig.reportNetworkInterface, "reportNetworkInterface should be true when set via system property");
        assertTrue(ConfigParser.isInitializationOK(), "No errors should be reported for valid property value");
    }

    @Test
    @DisplayName("should handle invalid reportNetworkInterface format with fallback to default")
    void shouldHandleInvalidReportNetworkInterfaceFormatWithFallbackToDefault() {
        // Given: reportNetworkInterface system property set to invalid value "invalid"
        System.setProperty(ReporterConfig.PROP_NETWORK_INTERFACE, "invalid");
        // When: ReporterConfig.init() is called
        ReporterConfig.init();
        // Then: reportNetworkInterface should fall back to default (false), error should be reported
        assertFalse(ReporterConfig.reportNetworkInterface, "reportNetworkInterface should fall back to default (false) when value is invalid");
        assertFalse(ConfigParser.isInitializationOK(), "Configuration error should be reported for invalid value");
        assertEquals(1, ConfigParser.initializationErrors.size(), "Exactly one error should be reported");
        assertTrue(ConfigParser.initializationErrors.get(0).contains("Invalid boolean value for property '" + ReporterConfig.PROP_NETWORK_INTERFACE), "Error message should mention the invalid property");
    }

    @Test
    @DisplayName("should parse reportSSLContext property correctly")
    void shouldParseReportSSLContextPropertyCorrectly() {
        // Given: reportSSLContext system property set to "true"
        System.setProperty(ReporterConfig.PROP_SSL_CONTEXT, "true");
        // When: ReporterConfig.init() is called
        ReporterConfig.init();
        // Then: reportSSLContext should be true and no errors should be reported
        assertTrue(ReporterConfig.reportSSLContext, "reportSSLContext should be true when set via system property");
        assertTrue(ConfigParser.isInitializationOK(), "No errors should be reported for valid property value");
    }

    @Test
    @DisplayName("should handle invalid reportSSLContext format with fallback to default")
    void shouldHandleInvalidReportSSLContextFormatWithFallbackToDefault() {
        // Given: reportSSLContext system property set to invalid value "invalid"
        System.setProperty(ReporterConfig.PROP_SSL_CONTEXT, "invalid");
        // When: ReporterConfig.init() is called
        ReporterConfig.init();
        // Then: reportSSLContext should fall back to default (false), error should be reported
        assertFalse(ReporterConfig.reportSSLContext, "reportSSLContext should fall back to default (false) when value is invalid");
        assertFalse(ConfigParser.isInitializationOK(), "Configuration error should be reported for invalid value");
        assertEquals(1, ConfigParser.initializationErrors.size(), "Exactly one error should be reported");
        assertTrue(ConfigParser.initializationErrors.get(0).contains("Invalid boolean value for property '" + ReporterConfig.PROP_SSL_CONTEXT), "Error message should mention the invalid property");
    }

    @Test
    @DisplayName("should parse reportDefaultTrustKeyStore property correctly")
    void shouldParseReportDefaultTrustKeyStorePropertyCorrectly() {
        // Given: reportDefaultTrustKeyStore system property set to "true"
        System.setProperty(ReporterConfig.PROP_DEFAULT_TRUST_KEYSTORE, "true");
        // When: ReporterConfig.init() is called
        ReporterConfig.init();
        // Then: reportDefaultTrustKeyStore should be true and no errors should be reported
        assertTrue(ReporterConfig.reportDefaultTrustKeyStore, "reportDefaultTrustKeyStore should be true when set via system property");
        assertTrue(ConfigParser.isInitializationOK(), "No errors should be reported for valid property value");
    }

    @Test
    @DisplayName("should handle invalid reportDefaultTrustKeyStore format with fallback to default")
    void shouldHandleInvalidReportDefaultTrustKeyStoreFormatWithFallbackToDefault() {
        // Given: reportDefaultTrustKeyStore system property set to invalid value "invalid"
        System.setProperty(ReporterConfig.PROP_DEFAULT_TRUST_KEYSTORE, "invalid");
        // When: ReporterConfig.init() is called
        ReporterConfig.init();
        // Then: reportDefaultTrustKeyStore should fall back to default (false), error should be reported
        assertFalse(ReporterConfig.reportDefaultTrustKeyStore, "reportDefaultTrustKeyStore should fall back to default (false) when value is invalid");
        assertFalse(ConfigParser.isInitializationOK(), "Configuration error should be reported for invalid value");
        assertEquals(1, ConfigParser.initializationErrors.size(), "Exactly one error should be reported");
        assertTrue(ConfigParser.initializationErrors.get(0).contains("Invalid boolean value for property '" + ReporterConfig.PROP_DEFAULT_TRUST_KEYSTORE), "Error message should mention the invalid property");
    }

    @Test
    @DisplayName("should parse reportJvmArguments property correctly")
    void shouldParseReportJvmArgumentsPropertyCorrectly() {
        // Given: reportJvmArguments system property set to "true"
        System.setProperty(ReporterConfig.PROP_JVM_ARGUMENTS, "true");
        // When: ReporterConfig.init() is called
        ReporterConfig.init();
        // Then: reportJvmArguments should be true and no errors should be reported
        assertTrue(ReporterConfig.reportJvmArguments, "reportJvmArguments should be true when set via system property");
        assertTrue(ConfigParser.isInitializationOK(), "No errors should be reported for valid property value");
    }

    @Test
    @DisplayName("should handle invalid reportJvmArguments format with fallback to default")
    void shouldHandleInvalidReportJvmArgumentsFormatWithFallbackToDefault() {
        // Given: reportJvmArguments system property set to invalid value "invalid"
        System.setProperty(ReporterConfig.PROP_JVM_ARGUMENTS, "invalid");
        // When: ReporterConfig.init() is called
        ReporterConfig.init();
        // Then: reportJvmArguments should fall back to default (false), error should be reported
        assertFalse(ReporterConfig.reportJvmArguments, "reportJvmArguments should fall back to default (false) when value is invalid");
        assertFalse(ConfigParser.isInitializationOK(), "Configuration error should be reported for invalid value");
        assertEquals(1, ConfigParser.initializationErrors.size(), "Exactly one error should be reported");
        assertTrue(ConfigParser.initializationErrors.get(0).contains("Invalid boolean value for property '" + ReporterConfig.PROP_JVM_ARGUMENTS), "Error message should mention the invalid property");
    }

    @Test
    @DisplayName("should parse reportClasspath property correctly")
    void shouldParseReportClasspathPropertyCorrectly() {
        // Given: reportClasspath system property set to "true"
        System.setProperty(ReporterConfig.PROP_CLASSPATH, "true");
        // When: ReporterConfig.init() is called
        ReporterConfig.init();
        // Then: reportClasspath should be true and no errors should be reported
        assertTrue(ReporterConfig.reportClasspath, "reportClasspath should be true when set via system property");
        assertTrue(ConfigParser.isInitializationOK(), "No errors should be reported for valid property value");
    }

    @Test
    @DisplayName("should handle invalid reportClasspath format with fallback to default")
    void shouldHandleInvalidReportClasspathFormatWithFallbackToDefault() {
        // Given: reportClasspath system property set to invalid value "invalid"
        System.setProperty(ReporterConfig.PROP_CLASSPATH, "invalid");
        // When: ReporterConfig.init() is called
        ReporterConfig.init();
        // Then: reportClasspath should fall back to default (false), error should be reported
        assertFalse(ReporterConfig.reportClasspath, "reportClasspath should fall back to default (false) when value is invalid");
        assertFalse(ConfigParser.isInitializationOK(), "Configuration error should be reported for invalid value");
        assertEquals(1, ConfigParser.initializationErrors.size(), "Exactly one error should be reported");
        assertTrue(ConfigParser.initializationErrors.get(0).contains("Invalid boolean value for property '" + ReporterConfig.PROP_CLASSPATH), "Error message should mention the invalid property");
    }

    @Test
    @DisplayName("should parse reportGarbageCollector property correctly")
    void shouldParseReportGarbageCollectorPropertyCorrectly() {
        // Given: reportGarbageCollector system property set to "true"
        System.setProperty(ReporterConfig.PROP_GARBAGE_COLLECTOR, "true");
        // When: ReporterConfig.init() is called
        ReporterConfig.init();
        // Then: reportGarbageCollector should be true and no errors should be reported
        assertTrue(ReporterConfig.reportGarbageCollector, "reportGarbageCollector should be true when set via system property");
        assertTrue(ConfigParser.isInitializationOK(), "No errors should be reported for valid property value");
    }

    @Test
    @DisplayName("should handle invalid reportGarbageCollector format with fallback to default")
    void shouldHandleInvalidReportGarbageCollectorFormatWithFallbackToDefault() {
        // Given: reportGarbageCollector system property set to invalid value "invalid"
        System.setProperty(ReporterConfig.PROP_GARBAGE_COLLECTOR, "invalid");
        // When: ReporterConfig.init() is called
        ReporterConfig.init();
        // Then: reportGarbageCollector should fall back to default (false), error should be reported
        assertFalse(ReporterConfig.reportGarbageCollector, "reportGarbageCollector should fall back to default (false) when value is invalid");
        assertFalse(ConfigParser.isInitializationOK(), "Configuration error should be reported for invalid value");
        assertEquals(1, ConfigParser.initializationErrors.size(), "Exactly one error should be reported");
        assertTrue(ConfigParser.initializationErrors.get(0).contains("Invalid boolean value for property '" + ReporterConfig.PROP_GARBAGE_COLLECTOR), "Error message should mention the invalid property");
    }

    @Test
    @DisplayName("should parse reportSecurityProviders property correctly")
    void shouldParseReportSecurityProvidersPropertyCorrectly() {
        // Given: reportSecurityProviders system property set to "true"
        System.setProperty(ReporterConfig.PROP_SECURITY_PROVIDERS, "true");
        // When: ReporterConfig.init() is called
        ReporterConfig.init();
        // Then: reportSecurityProviders should be true and no errors should be reported
        assertTrue(ReporterConfig.reportSecurityProviders, "reportSecurityProviders should be true when set via system property");
        assertTrue(ConfigParser.isInitializationOK(), "No errors should be reported for valid property value");
    }

    @Test
    @DisplayName("should handle invalid reportSecurityProviders format with fallback to default")
    void shouldHandleInvalidReportSecurityProvidersFormatWithFallbackToDefault() {
        // Given: reportSecurityProviders system property set to invalid value "invalid"
        System.setProperty(ReporterConfig.PROP_SECURITY_PROVIDERS, "invalid");
        // When: ReporterConfig.init() is called
        ReporterConfig.init();
        // Then: reportSecurityProviders should fall back to default (false), error should be reported
        assertFalse(ReporterConfig.reportSecurityProviders, "reportSecurityProviders should fall back to default (false) when value is invalid");
        assertFalse(ConfigParser.isInitializationOK(), "Configuration error should be reported for invalid value");
        assertEquals(1, ConfigParser.initializationErrors.size(), "Exactly one error should be reported");
        assertTrue(ConfigParser.initializationErrors.get(0).contains("Invalid boolean value for property '" + ReporterConfig.PROP_SECURITY_PROVIDERS), "Error message should mention the invalid property");
    }

    @Test
    @DisplayName("should parse reportContainerInfo property correctly")
    void shouldParseReportContainerInfoPropertyCorrectly() {
        // Given: reportContainerInfo system property set to "true"
        System.setProperty(ReporterConfig.PROP_CONTAINER_INFO, "true");
        // When: ReporterConfig.init() is called
        ReporterConfig.init();
        // Then: reportContainerInfo should be true and no errors should be reported
        assertTrue(ReporterConfig.reportContainerInfo, "reportContainerInfo should be true when set via system property");
        assertTrue(ConfigParser.isInitializationOK(), "No errors should be reported for valid property value");
    }

    @Test
    @DisplayName("should handle invalid reportContainerInfo format with fallback to default")
    void shouldHandleInvalidReportContainerInfoFormatWithFallbackToDefault() {
        // Given: reportContainerInfo system property set to invalid value "invalid"
        System.setProperty(ReporterConfig.PROP_CONTAINER_INFO, "invalid");
        // When: ReporterConfig.init() is called
        ReporterConfig.init();
        // Then: reportContainerInfo should fall back to default (false), error should be reported
        assertFalse(ReporterConfig.reportContainerInfo, "reportContainerInfo should fall back to default (false) when value is invalid");
        assertFalse(ConfigParser.isInitializationOK(), "Configuration error should be reported for invalid value");
        assertEquals(1, ConfigParser.initializationErrors.size(), "Exactly one error should be reported");
        assertTrue(ConfigParser.initializationErrors.get(0).contains("Invalid boolean value for property '" + ReporterConfig.PROP_CONTAINER_INFO), "Error message should mention the invalid property");
    }

    @Test
    @DisplayName("should parse name property correctly")
    void shouldParseNamePropertyCorrectly() {
        // Given: name system property set to "customReport"
        System.setProperty(ReporterConfig.PROP_NAME, "customReport");
        // When: ReporterConfig.init() is called
        ReporterConfig.init();
        // Then: name should reflect the system property value and no errors should be reported
        assertEquals("customReport", ReporterConfig.name, "name should reflect the system property value");
        assertTrue(ConfigParser.isInitializationOK(), "No errors should be reported for valid property value");
    }

    @Test
    @DisplayName("should parse forbiddenPropertyNamesRegex property correctly")
    void shouldParseForbiddenPropertyNamesRegexPropertyCorrectly() {
        // Given: forbiddenPropertyNamesRegex system property set to ".*custom.*"
        System.setProperty(ReporterConfig.PROP_FORBIDDEN_PROPERTY_NAMES_REGEX, ".*custom.*");
        // When: ReporterConfig.init() is called
        ReporterConfig.init();
        // Then: forbiddenPropertyNamesRegex should reflect the system property value and no errors should be reported
        assertEquals(".*custom.*", ReporterConfig.forbiddenPropertyNamesRegex, "forbiddenPropertyNamesRegex should reflect the system property value");
        assertTrue(ConfigParser.isInitializationOK(), "No errors should be reported for valid property value");
    }
}

