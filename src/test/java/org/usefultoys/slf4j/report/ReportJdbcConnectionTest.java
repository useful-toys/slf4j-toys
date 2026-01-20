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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.impl.MockLoggerEvent.Level;
import org.usefultoys.slf4jtestmock.Slf4jMock;
import org.usefultoys.slf4jtestmock.WithMockLogger;
import org.usefultoys.test.ResetReporterConfig;
import org.usefultoys.test.ValidateCharset;
import org.usefultoys.test.WithLocale;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.usefultoys.slf4jtestmock.AssertLogger.assertEvent;
import static org.usefultoys.slf4jtestmock.AssertLogger.assertEventNot;
import static org.usefultoys.slf4jtestmock.AssertLogger.assertHasEvent;
import static org.usefultoys.slf4jtestmock.AssertLogger.assertNoEvent;

/**
 * Unit tests for {@link ReportJdbcConnection}.
 * <p>
 * Tests verify that ReportJdbcConnection correctly reports JDBC connection information
 * including connection properties, metadata, client info, and handles various edge cases.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>Connection State:</b> Verifies reporting of closed vs open connections, ensuring appropriate detail levels</li>
 *   <li><b>Basic Properties:</b> Tests logging of catalog, schema, URL, username, and connection state properties</li>
 *   <li><b>Connection Configuration:</b> Validates reporting of auto-commit, read-only mode, holdability, transaction isolation, and network timeout</li>
 *   <li><b>Client Information:</b> Ensures proper handling and display of client info properties and type maps</li>
 *   <li><b>Database Metadata:</b> Tests reporting of database product info, driver details, JDBC version, and connection limits</li>
 *   <li><b>Null/Empty Values:</b> Verifies behavior with null catalogs/schemas and empty client info</li>
 *   <li><b>Edge Cases:</b> Covers unknown holdability values, various transaction isolation levels, and error conditions</li>
 *   <li><b>Property Filtering:</b> Ensures sensitive information is properly handled according to configuration</li>
 * </ul>
 */
@SuppressWarnings("NonConstantLogger")
@DisplayName("ReportJdbcConnection")
@ValidateCharset
@ResetReporterConfig
@WithLocale("en")
@WithMockLogger
class ReportJdbcConnectionTest {

    @Slf4jMock
    private Logger logger;
    private Connection mockConnection;
    private DatabaseMetaData mockMetaData;

    @BeforeEach
    void setUp() throws SQLException {
        mockConnection = mock(Connection.class);
        mockMetaData = mock(DatabaseMetaData.class);

        // Default mocks for common calls to avoid NullPointerException in basic tests
        when(mockConnection.isClosed()).thenReturn(false);
        when(mockConnection.getCatalog()).thenReturn("test_catalog");
        when(mockConnection.getSchema()).thenReturn("test_schema");
        when(mockConnection.getMetaData()).thenReturn(mockMetaData);
        when(mockConnection.isReadOnly()).thenReturn(false);
        when(mockConnection.getAutoCommit()).thenReturn(true);
        when(mockConnection.getHoldability()).thenReturn(ResultSet.HOLD_CURSORS_OVER_COMMIT);
        when(mockConnection.getNetworkTimeout()).thenReturn(1000);
        when(mockConnection.getTransactionIsolation()).thenReturn(Connection.TRANSACTION_READ_COMMITTED);
        when(mockConnection.getClientInfo()).thenReturn(new Properties());
        when(mockConnection.getTypeMap()).thenReturn(new HashMap<>());

        when(mockMetaData.getURL()).thenReturn("jdbc:test://localhost/testdb");
        when(mockMetaData.getUserName()).thenReturn("testuser");
        when(mockMetaData.getDatabaseProductName()).thenReturn("TestDB");
        when(mockMetaData.getDatabaseProductVersion()).thenReturn("1.0");
        when(mockMetaData.getDriverName()).thenReturn("TestDriver");
        when(mockMetaData.getDriverVersion()).thenReturn("1.0");
        when(mockMetaData.getJDBCMajorVersion()).thenReturn(4);
        when(mockMetaData.getJDBCMinorVersion()).thenReturn(2);
        when(mockMetaData.getMaxConnections()).thenReturn(100);
        when(mockMetaData.getSQLStateType()).thenReturn(DatabaseMetaData.sqlStateSQL99);
    }

    @Test
    @DisplayName("should report closed connection")
    void shouldReportClosedConnection() throws SQLException {
        // Given: closed connection
        when(mockConnection.isClosed()).thenReturn(true);

        // When: report is executed
        final ReportJdbcConnection report = new ReportJdbcConnection(logger, mockConnection);
        report.run();

        // Then: should log closed status without further details
        assertEvent(logger, 0, Level.INFO, "JDBC connection", " - Closed! ");
        verify(mockConnection).isClosed();
        verify(mockConnection, never()).getCatalog();
    }

    @Test
    @DisplayName("should report open connection basic properties")
    void shouldReportOpenConnectionBasicProperties() throws SQLException {
        // Given: open connection with basic properties configured
        // When: report is executed
        final ReportJdbcConnection report = new ReportJdbcConnection(logger, mockConnection);
        report.run();

        // Then: should log all connection details
        assertEvent(logger, 0, Level.INFO,
            "JDBC connection",
            " - catalog: test_catalog",
            " - schema: test_schema",
            "    URL: jdbc:test://localhost/testdb",
            "    user name: testuser",
            " - properties: auto-commit; holdability=hold-cursors-over-commit; timeout=1000ms; transaction=read-committed;",
            " - client info: n/a",
            " - database: TestDB (1.0)",
            " - driver: TestDriver (1.0); jdbc-version=4.2; max-connections=100; sql-state-type=SQL99;");
    }

    @Test
    @DisplayName("should report connection with null catalog and schema")
    void shouldReportConnectionWithNullCatalogAndSchema() throws SQLException {
        // Given: connection with null catalog and schema
        when(mockConnection.getCatalog()).thenReturn(null);
        when(mockConnection.getSchema()).thenReturn(null);

        // When: report is executed
        final ReportJdbcConnection report = new ReportJdbcConnection(logger, mockConnection);
        report.run();

        // Then: should not include catalog and schema in output
        assertEvent(logger, 0, Level.INFO, "JDBC connection", " - properties:");
        assertEventNot(logger, 0, Level.INFO, " - catalog:");
        assertEventNot(logger, 0, Level.INFO, " - schema:");
    }

    @Test
    @DisplayName("should report connection with read-only and no auto-commit")
    void shouldReportConnectionWithReadOnlyAndNoAutoCommit() throws SQLException {
        // Given: connection with read-only enabled and auto-commit disabled
        when(mockConnection.isReadOnly()).thenReturn(true);
        when(mockConnection.getAutoCommit()).thenReturn(false);

        // When: report is executed
        final ReportJdbcConnection report = new ReportJdbcConnection(logger, mockConnection);
        report.run();

        // Then: should log read-only but not auto-commit
        assertEvent(logger, 0, Level.INFO, "read-only; ");
        assertEventNot(logger, 0, Level.INFO, "auto-commit; ");
    }

    @Test
    @DisplayName("should report connection with holdability close cursors")
    void shouldReportConnectionWithHoldabilityCloseCursors() throws SQLException {
        // Given: connection with holdability set to close cursors at commit
        when(mockConnection.getHoldability()).thenReturn(ResultSet.CLOSE_CURSORS_AT_COMMIT);

        // When: report is executed
        final ReportJdbcConnection report = new ReportJdbcConnection(logger, mockConnection);
        report.run();

        // Then: should log holdability type
        assertHasEvent(logger, "holdability=close-cursors-at-commit; ");
    }

    @Test
    @DisplayName("should report connection with unknown holdability")
    void shouldReportConnectionWithUnknownHoldability() throws SQLException {
        // Given: connection with unknown holdability value
        when(mockConnection.getHoldability()).thenReturn(999);

        // When: report is executed
        final ReportJdbcConnection report = new ReportJdbcConnection(logger, mockConnection);
        report.run();

        // Then: should log unknown holdability
        assertHasEvent(logger, "holdability=unknown; ");
    }

    @Test
    @DisplayName("should report connection with transaction isolation serializable")
    void shouldReportConnectionWithTransactionIsolationSerializable() throws SQLException {
        // Given: connection with serializable transaction isolation
        when(mockConnection.getTransactionIsolation()).thenReturn(Connection.TRANSACTION_SERIALIZABLE);

        // When: report is executed
        final ReportJdbcConnection report = new ReportJdbcConnection(logger, mockConnection);
        report.run();

        // Then: should log serializable transaction level
        assertHasEvent(logger, "transaction=serializable; ");
    }

    @Test
    @DisplayName("should report connection with transaction isolation read uncommitted")
    void shouldReportConnectionWithTransactionIsolationReadUncommitted() throws SQLException {
        // Given: connection with read uncommitted transaction isolation
        when(mockConnection.getTransactionIsolation()).thenReturn(Connection.TRANSACTION_READ_UNCOMMITTED);

        // When: report is executed
        final ReportJdbcConnection report = new ReportJdbcConnection(logger, mockConnection);
        report.run();

        // Then: should log read-uncommitted transaction level
        assertHasEvent(logger, "transaction=read-uncommitted; ");
    }

    @Test
    @DisplayName("should report connection with transaction isolation repeatable read")
    void shouldReportConnectionWithTransactionIsolationRepeatableRead() throws SQLException {
        // Given: connection with repeatable read transaction isolation
        when(mockConnection.getTransactionIsolation()).thenReturn(Connection.TRANSACTION_REPEATABLE_READ);

        // When: report is executed
        final ReportJdbcConnection report = new ReportJdbcConnection(logger, mockConnection);
        report.run();

        // Then: should log repeatable-read transaction level
        assertHasEvent(logger, "transaction=repeatable-read; ");
    }

    @Test
    @DisplayName("should report connection with transaction isolation none")
    void shouldReportConnectionWithTransactionIsolationNone() throws SQLException {
        // Given: connection with no transaction isolation
        when(mockConnection.getTransactionIsolation()).thenReturn(Connection.TRANSACTION_NONE);

        // When: report is executed
        final ReportJdbcConnection report = new ReportJdbcConnection(logger, mockConnection);
        report.run();

        // Then: should log none transaction level
        assertHasEvent(logger, "transaction=none; ");
    }

    @Test
    @DisplayName("should report connection with unknown transaction isolation")
    void shouldReportConnectionWithUnknownTransactionIsolation() throws SQLException {
        // Given: connection with unknown transaction isolation value
        when(mockConnection.getTransactionIsolation()).thenReturn(999);

        // When: report is executed
        final ReportJdbcConnection report = new ReportJdbcConnection(logger, mockConnection);
        report.run();

        // Then: should log unknown transaction level
        assertHasEvent(logger, "transaction=unknown; ");
    }

    @Test
    @DisplayName("should report connection with client info")
    void shouldReportConnectionWithClientInfo() throws SQLException {
        // Given: connection with client info including sensitive property
        final Properties clientInfo = new Properties();
        clientInfo.setProperty("ApplicationName", "MyApp");
        clientInfo.setProperty("ClientUser", "testuser");
        clientInfo.setProperty("Password", "secret"); // Should be masked
        when(mockConnection.getClientInfo()).thenReturn(clientInfo);

        // When: report is executed
        final ReportJdbcConnection report = new ReportJdbcConnection(logger, mockConnection);
        report.run();

        // Then: should log client info with password masked
        assertHasEvent(logger, " - client info: ");
        assertHasEvent(logger, "ApplicationName=MyApp; ");
        assertHasEvent(logger, "ClientUser=testuser; ");
        assertHasEvent(logger, "Password=?; ");
    }

    @Test
    @DisplayName("should report connection with null client info")
    void shouldReportConnectionWithNullClientInfo() throws SQLException {
        // Given: connection with null client info
        when(mockConnection.getClientInfo()).thenReturn(null);

        // When: report is executed
        final ReportJdbcConnection report = new ReportJdbcConnection(logger, mockConnection);
        report.run();

        // Then: should log n/a for client info
        assertHasEvent(logger, " - client info: n/a");
    }

    @Test
    @DisplayName("should report connection with null metadata")
    void shouldReportConnectionWithNullMetadata() throws SQLException {
        // Given: connection with null metadata
        when(mockConnection.getMetaData()).thenReturn(null);

        // When: report is executed
        final ReportJdbcConnection report = new ReportJdbcConnection(logger, mockConnection);
        report.run();

        // Then: should not log metadata-related fields
        assertNoEvent(logger, "URL:");
        assertNoEvent(logger, "user name:");
        assertNoEvent(logger, " - database:");
        assertNoEvent(logger, " - driver:");
    }

    @Test
    @DisplayName("should report connection with SQL state type X-Open")
    void shouldReportConnectionWithSQLStateTypeXOpen() throws SQLException {
        // Given: metadata with X-Open SQL state type
        when(mockMetaData.getSQLStateType()).thenReturn(DatabaseMetaData.sqlStateXOpen);

        // When: report is executed
        final ReportJdbcConnection report = new ReportJdbcConnection(logger, mockConnection);
        report.run();

        // Then: should log X-Open SQL state type
        assertHasEvent(logger, "sql-state-type=X-Open; ");
    }

    @Test
    @DisplayName("should report connection with unknown SQL state type")
    void shouldReportConnectionWithUnknownSQLStateType() throws SQLException {
        // Given: metadata with unknown SQL state type value
        when(mockMetaData.getSQLStateType()).thenReturn(999);

        // When: report is executed
        final ReportJdbcConnection report = new ReportJdbcConnection(logger, mockConnection);
        report.run();

        // Then: should log unknown SQL state type
        assertHasEvent(logger, "sql-state-type=unknown; ");
    }

    @Test
    @DisplayName("should report connection with type map enabled and populated")
    void shouldReportConnectionWithTypeMapEnabledAndPopulated() throws SQLException {
        // Given: connection with populated type map and print enabled
        final Map<String, Class<?>> typeMap = new HashMap<>();
        typeMap.put("my_udt", String.class);
        typeMap.put("another_udt", Integer.class);
        when(mockConnection.getTypeMap()).thenReturn(typeMap);

        // When: report is executed with type map printing enabled
        final ReportJdbcConnection report = new ReportJdbcConnection(logger, mockConnection)
                .printTypeMap(true);
        report.run();

        // Then: should log type map entries
        assertHasEvent(logger, " - type map: my_udt->String; another_udt->Integer;");
    }

    @Test
    @DisplayName("should report connection with type map enabled and empty")
    void shouldReportConnectionWithTypeMapEnabledAndEmpty() throws SQLException {
        // Given: connection with empty type map and print enabled
        when(mockConnection.getTypeMap()).thenReturn(Collections.emptyMap());

        // When: report is executed with type map printing enabled
        final ReportJdbcConnection report = new ReportJdbcConnection(logger, mockConnection)
                .printTypeMap(true);
        report.run();

        // Then: should log n/a for empty type map
        assertHasEvent(logger, " - type map: n/a");
    }

    @Test
    @DisplayName("should report connection with type map enabled and null")
    void shouldReportConnectionWithTypeMapEnabledAndNull() throws SQLException {
        // Given: connection with null type map and print enabled
        when(mockConnection.getTypeMap()).thenReturn(null);

        // When: report is executed with type map printing enabled
        final ReportJdbcConnection report = new ReportJdbcConnection(logger, mockConnection)
                .printTypeMap(true);
        report.run();

        // Then: should log n/a for null type map
        assertHasEvent(logger, " - type map: n/a");
    }

    @Test
    @DisplayName("should not report type map when disabled")
    void shouldNotReportTypeMapWhenDisabled() throws SQLException {
        // Given: connection with type map but print disabled
        final Map<String, Class<?>> typeMap = new HashMap<>();
        typeMap.put("my_udt", String.class);
        when(mockConnection.getTypeMap()).thenReturn(typeMap);

        // When: report is executed with type map printing disabled
        final ReportJdbcConnection report = new ReportJdbcConnection(logger, mockConnection)
                .printTypeMap(false);
        report.run();

        // Then: should not log type map
        assertNoEvent(logger, " - type map:");
    }

    @Test
    @DisplayName("should wrap type map entries with line break at 3-entry intervals")
    void shouldWrapTypeMapEntriesWithLineBreakAt3EntryIntervals() throws SQLException {
        // Given: connection with multiple type map entries (more than 3) to trigger line wrapping
        final Map<String, Class<?>> typeMap = new HashMap<>();
        typeMap.put("type_1", String.class);
        typeMap.put("type_2", Integer.class);
        typeMap.put("type_3", Long.class);
        typeMap.put("type_4", Double.class);
        when(mockConnection.getTypeMap()).thenReturn(typeMap);

        // When: report is executed with type map printing enabled
        final ReportJdbcConnection report = new ReportJdbcConnection(logger, mockConnection)
                .printTypeMap(true);
        report.run();

        // Then: should log all type map entries with line wrapping after 3rd entry (i++ % 3 == 0)
        // The 4th entry should appear on a new line with indentation
        assertHasEvent(logger, " - type map: ");
        assertHasEvent(logger, "->String; ");
        assertHasEvent(logger, "->Integer; ");
        assertHasEvent(logger, "->Long; ");
        assertHasEvent(logger, "->Double;");
    }

    @Test
    @DisplayName("should wrap client info entries with line break at 5-entry intervals")
    void shouldWrapClientInfoEntriesWithLineBreakAt5EntryIntervals() throws SQLException {
        // Given: connection with multiple client info entries (more than 5) to trigger line wrapping
        final Properties clientInfo = new Properties();
        clientInfo.setProperty("Property1", "value1");
        clientInfo.setProperty("Property2", "value2");
        clientInfo.setProperty("Property3", "value3");
        clientInfo.setProperty("Property4", "value4");
        clientInfo.setProperty("Property5", "value5");
        clientInfo.setProperty("Property6", "value6");
        when(mockConnection.getClientInfo()).thenReturn(clientInfo);

        // When: report is executed
        final ReportJdbcConnection report = new ReportJdbcConnection(logger, mockConnection);
        report.run();

        // Then: should log all client info entries with line wrapping after 5th entry (i++ % 5 == 0)
        // The 6th entry should appear on a new line with indentation
        assertHasEvent(logger, " - client info: ");
        assertHasEvent(logger, "Property1=value1; ");
        assertHasEvent(logger, "Property2=value2; ");
        assertHasEvent(logger, "Property3=value3; ");
        assertHasEvent(logger, "Property4=value4; ");
        assertHasEvent(logger, "Property5=value5; ");
        assertHasEvent(logger, "Property6=value6;");
    }

    @Test
    @DisplayName("should handle SQL exception")
    void shouldHandleSQLException() throws SQLException {
        // Given: connection that throws SQLException on getCatalog
        when(mockConnection.getCatalog()).thenThrow(new SQLException("Mock SQL Exception"));

        // When: report is executed
        final ReportJdbcConnection report = new ReportJdbcConnection(logger, mockConnection);
        report.run();

        // Then: should log error message
        assertHasEvent(logger, "Cannot read connection property: Mock SQL Exception");
    }

    @Test
    @DisplayName("should handle no such method error on schema")
    void shouldHandleNoSuchMethodErrorOnSchema() throws SQLException {
        // Given: connection that throws NoSuchMethodError on getSchema (simulating older JDK)
        when(mockConnection.getSchema()).thenThrow(new NoSuchMethodError("Mock NoSuchMethodError"));

        // When: report is executed
        final ReportJdbcConnection report = new ReportJdbcConnection(logger, mockConnection);
        report.run();

        // Then: should not log schema and should not catch as SQLException
        assertNoEvent(logger, " - schema:");
        assertNoEvent(logger, "Cannot read connection property:");
    }
}
