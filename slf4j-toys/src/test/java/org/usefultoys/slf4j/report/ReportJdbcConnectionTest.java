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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.MockLogger;
import org.usefultoys.test.CharsetConsistency;
import org.usefultoys.test.ResetReporterConfig;
import org.usefultoys.test.WithLocale;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith({CharsetConsistency.class, ResetReporterConfig.class})
@WithLocale("en")
class ReportJdbcConnectionTest {

    private MockLogger mockLogger;
    private Connection mockConnection;
    private DatabaseMetaData mockMetaData;

    @BeforeEach
    void setUp() throws SQLException {
        final Logger logger = LoggerFactory.getLogger("test.report.jdbc");
        mockLogger = (MockLogger) logger;
        mockLogger.clearEvents();

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
    void testReportClosedConnection() throws SQLException {
        when(mockConnection.isClosed()).thenReturn(true);

        final ReportJdbcConnection report = new ReportJdbcConnection(mockLogger, mockConnection);
        report.run();

        assertTrue(mockLogger.getEventCount() > 0);
        final String logs = mockLogger.getEvent(0).getFormattedMessage();
        assertTrue(logs.contains("JDBC connection"));
        assertTrue(logs.contains(" - Closed! "));
        verify(mockConnection).isClosed(); // Ensure isClosed was called
        verify(mockConnection, never()).getCatalog(); // Ensure no further calls
    }

    @Test
    void testReportOpenConnectionBasicProperties() throws SQLException {
        final ReportJdbcConnection report = new ReportJdbcConnection(mockLogger, mockConnection);
        report.run();

        assertTrue(mockLogger.getEventCount() > 0);
        final String logs = mockLogger.getEvent(0).getFormattedMessage();
        assertTrue(logs.contains("JDBC connection"));
        assertTrue(logs.contains(" - catalog: test_catalog"));
        assertTrue(logs.contains(" - schema: test_schema"));
        assertTrue(logs.contains("    URL: jdbc:test://localhost/testdb"));
        assertTrue(logs.contains("    user name: testuser"));
        assertTrue(logs.contains(" - properties: auto-commit; holdability=hold-cursors-over-commit; timeout=1000ms; transaction=read-committed;"));
        assertTrue(logs.contains(" - client info: n/a")); // Default empty properties
        assertTrue(logs.contains(" - database: TestDB (1.0)"));
        assertTrue(logs.contains(" - driver: TestDriver (1.0); jdbc-version=4.2; max-connections=100; sql-state-type=SQL99;"));
    }

    @Test
    void testReportConnectionWithNullCatalogAndSchema() throws SQLException {
        when(mockConnection.getCatalog()).thenReturn(null);
        when(mockConnection.getSchema()).thenReturn(null);

        final ReportJdbcConnection report = new ReportJdbcConnection(mockLogger, mockConnection);
        report.run();

        assertTrue(mockLogger.getEventCount() > 0);
        final String logs = mockLogger.getEvent(0).getFormattedMessage();
        assertTrue(logs.contains("JDBC connection"));
        assertTrue(logs.contains(" - properties:")); // Check for properties section
        // Ensure catalog and schema are not printed
        assertTrue(!logs.contains(" - catalog:"));
        assertTrue(!logs.contains(" - schema:"));
    }

    @Test
    void testReportConnectionWithReadOnlyAndNoAutoCommit() throws SQLException {
        when(mockConnection.isReadOnly()).thenReturn(true);
        when(mockConnection.getAutoCommit()).thenReturn(false);

        final ReportJdbcConnection report = new ReportJdbcConnection(mockLogger, mockConnection);
        report.run();

        assertTrue(mockLogger.getEventCount() > 0);
        final String logs = mockLogger.getEvent(0).getFormattedMessage();
        assertTrue(logs.contains("read-only; "));
        assertTrue(!logs.contains("auto-commit; "));
    }

    @Test
    void testReportConnectionWithHoldabilityCloseCursors() throws SQLException {
        when(mockConnection.getHoldability()).thenReturn(ResultSet.CLOSE_CURSORS_AT_COMMIT);

        final ReportJdbcConnection report = new ReportJdbcConnection(mockLogger, mockConnection);
        report.run();

        assertTrue(mockLogger.getEventCount() > 0);
        final String logs = mockLogger.getEvent(0).getFormattedMessage();
        assertTrue(logs.contains("holdability=close-cursors-at-commit; "));
    }

    @Test
    void testReportConnectionWithHoldabilityUnknown() throws SQLException {
        when(mockConnection.getHoldability()).thenReturn(999); // Unknown value

        final ReportJdbcConnection report = new ReportJdbcConnection(mockLogger, mockConnection);
        report.run();

        assertTrue(mockLogger.getEventCount() > 0);
        final String logs = mockLogger.getEvent(0).getFormattedMessage();
        assertTrue(logs.contains("holdability=unknown; "));
    }

    @Test
    void testReportConnectionWithTransactionIsolationSerializable() throws SQLException {
        when(mockConnection.getTransactionIsolation()).thenReturn(Connection.TRANSACTION_SERIALIZABLE);

        final ReportJdbcConnection report = new ReportJdbcConnection(mockLogger, mockConnection);
        report.run();

        assertTrue(mockLogger.getEventCount() > 0);
        final String logs = mockLogger.getEvent(0).getFormattedMessage();
        assertTrue(logs.contains("transaction=serializable; "));
    }

    @Test
    void testReportConnectionWithTransactionIsolationNone() throws SQLException {
        when(mockConnection.getTransactionIsolation()).thenReturn(Connection.TRANSACTION_NONE);

        final ReportJdbcConnection report = new ReportJdbcConnection(mockLogger, mockConnection);
        report.run();

        assertTrue(mockLogger.getEventCount() > 0);
        final String logs = mockLogger.getEvent(0).getFormattedMessage();
        assertTrue(logs.contains("transaction=none; "));
    }

    @Test
    void testReportConnectionWithTransactionIsolationUnknown() throws SQLException {
        when(mockConnection.getTransactionIsolation()).thenReturn(999); // Unknown value

        final ReportJdbcConnection report = new ReportJdbcConnection(mockLogger, mockConnection);
        report.run();

        assertTrue(mockLogger.getEventCount() > 0);
        final String logs = mockLogger.getEvent(0).getFormattedMessage();
        assertTrue(logs.contains("transaction=unknown; "));
    }

    @Test
    void testReportConnectionWithClientInfo() throws SQLException {
        Properties clientInfo = new Properties();
        clientInfo.setProperty("ApplicationName", "MyApp");
        clientInfo.setProperty("ClientUser", "testuser");
        clientInfo.setProperty("Password", "secret"); // Should be masked
        when(mockConnection.getClientInfo()).thenReturn(clientInfo);

        final ReportJdbcConnection report = new ReportJdbcConnection(mockLogger, mockConnection);
        report.run();

        assertTrue(mockLogger.getEventCount() > 0);
        final String logs = mockLogger.getEvent(0).getFormattedMessage();
        assertTrue(logs.contains(" - client info: "));
        assertTrue(logs.contains("ApplicationName=MyApp; "));
        assertTrue(logs.contains("ClientUser=testuser; "));
        assertTrue(logs.contains("Password=?; "));
    }

    @Test
    void testReportConnectionWithNullClientInfo() throws SQLException {
        when(mockConnection.getClientInfo()).thenReturn(null);

        final ReportJdbcConnection report = new ReportJdbcConnection(mockLogger, mockConnection);
        report.run();

        assertTrue(mockLogger.getEventCount() > 0);
        final String logs = mockLogger.getEvent(0).getFormattedMessage();
        assertTrue(logs.contains(" - client info: n/a"));
    }

    @Test
    void testReportConnectionWithMetaDataNull() throws SQLException {
        when(mockConnection.getMetaData()).thenReturn(null);

        final ReportJdbcConnection report = new ReportJdbcConnection(mockLogger, mockConnection);
        report.run();

        assertTrue(mockLogger.getEventCount() > 0);
        final String logs = mockLogger.getEvent(0).getFormattedMessage();
        // Ensure metadata related fields are not printed
        assertTrue(!logs.contains("URL:"));
        assertTrue(!logs.contains("user name:"));
        assertTrue(!logs.contains(" - database:"));
        assertTrue(!logs.contains(" - driver:"));
    }

    @Test
    void testReportConnectionWithSQLStateTypeXOpen() throws SQLException {
        when(mockMetaData.getSQLStateType()).thenReturn(DatabaseMetaData.sqlStateXOpen);

        final ReportJdbcConnection report = new ReportJdbcConnection(mockLogger, mockConnection);
        report.run();

        assertTrue(mockLogger.getEventCount() > 0);
        final String logs = mockLogger.getEvent(0).getFormattedMessage();
        assertTrue(logs.contains("sql-state-type=X-Open; "));
    }

    @Test
    void testReportConnectionWithSQLStateTypeUnknown() throws SQLException {
        when(mockMetaData.getSQLStateType()).thenReturn(999); // Unknown value

        final ReportJdbcConnection report = new ReportJdbcConnection(mockLogger, mockConnection);
        report.run();

        assertTrue(mockLogger.getEventCount() > 0);
        final String logs = mockLogger.getEvent(0).getFormattedMessage();
        assertTrue(logs.contains("sql-state-type=unknown; "));
    }

    @Test
    void testReportConnectionWithTypeMapEnabledAndPopulated() throws SQLException {
        Map<String, Class<?>> typeMap = new HashMap<>();
        typeMap.put("my_udt", String.class);
        typeMap.put("another_udt", Integer.class);
        when(mockConnection.getTypeMap()).thenReturn(typeMap);

        final ReportJdbcConnection report = new ReportJdbcConnection(mockLogger, mockConnection)
                .printTypeMap(true);
        report.run();

        assertTrue(mockLogger.getEventCount() > 0);
        final String logs = mockLogger.getEvent(0).getFormattedMessage();
        assertTrue(logs.contains(" - type map: my_udt->String; another_udt->Integer;"));
    }

    @Test
    void testReportConnectionWithTypeMapEnabledAndEmpty() throws SQLException {
        when(mockConnection.getTypeMap()).thenReturn(Collections.emptyMap());

        final ReportJdbcConnection report = new ReportJdbcConnection(mockLogger, mockConnection)
                .printTypeMap(true);
        report.run();

        assertTrue(mockLogger.getEventCount() > 0);
        final String logs = mockLogger.getEvent(0).getFormattedMessage();
        assertTrue(logs.contains(" - type map: n/a"));
    }

    @Test
    void testReportConnectionWithTypeMapEnabledAndNull() throws SQLException {
        when(mockConnection.getTypeMap()).thenReturn(null);

        final ReportJdbcConnection report = new ReportJdbcConnection(mockLogger, mockConnection)
                .printTypeMap(true);
        report.run();

        assertTrue(mockLogger.getEventCount() > 0);
        final String logs = mockLogger.getEvent(0).getFormattedMessage();
        assertTrue(logs.contains(" - type map: n/a"));
    }

    @Test
    void testReportConnectionWithTypeMapDisabled() throws SQLException {
        Map<String, Class<?>> typeMap = new HashMap<>();
        typeMap.put("my_udt", String.class);
        when(mockConnection.getTypeMap()).thenReturn(typeMap);

        final ReportJdbcConnection report = new ReportJdbcConnection(mockLogger, mockConnection)
                .printTypeMap(false); // Explicitly disabled
        report.run();

        assertTrue(mockLogger.getEventCount() > 0);
        final String logs = mockLogger.getEvent(0).getFormattedMessage();
        assertTrue(!logs.contains(" - type map:")); // Ensure type map is not printed
    }

    @Test
    void testReportConnectionSQLExceptionHandling() throws SQLException {
        when(mockConnection.getCatalog()).thenThrow(new SQLException("Mock SQL Exception"));

        final ReportJdbcConnection report = new ReportJdbcConnection(mockLogger, mockConnection);
        report.run();

        assertTrue(mockLogger.getEventCount() > 0);
        final String logs = mockLogger.getEvent(0).getFormattedMessage();
        assertTrue(logs.contains("Cannot read connection property: Mock SQL Exception"));
    }

    @Test
    void testReportConnectionSchemaNoSuchMethodError() throws SQLException {
        // Simulate older JDK where getSchema() might not exist
        when(mockConnection.getSchema()).thenThrow(new NoSuchMethodError("Mock NoSuchMethodError"));

        final ReportJdbcConnection report = new ReportJdbcConnection(mockLogger, mockConnection);
        report.run();

        assertTrue(mockLogger.getEventCount() > 0);
        final String logs = mockLogger.getEvent(0).getFormattedMessage();
        assertTrue(!logs.contains(" - schema:")); // Schema should not be printed
        assertTrue(!logs.contains("Cannot read connection property:")); // Should not catch NoSuchMethodError as SQLException
    }
}
