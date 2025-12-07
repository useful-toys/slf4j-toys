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
import org.slf4j.impl.MockLoggerEvent.Level;
import org.usefultoys.slf4jtestmock.MockLoggerExtension;
import org.usefultoys.slf4jtestmock.Slf4jMock;
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

import static org.mockito.Mockito.*;
import static org.usefultoys.slf4jtestmock.AssertLogger.*;

@ExtendWith({CharsetConsistency.class, ResetReporterConfig.class, MockLoggerExtension.class})
@WithLocale("en")
class ReportJdbcConnectionTest {

    @Slf4jMock("test.report.jdbc")
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
    void testReportClosedConnection() throws SQLException {
        when(mockConnection.isClosed()).thenReturn(true);

        final ReportJdbcConnection report = new ReportJdbcConnection(logger, mockConnection);
        report.run();

        assertEvent(logger, 0, Level.INFO, "JDBC connection", " - Closed! ");
        verify(mockConnection).isClosed(); // Ensure isClosed was called
        verify(mockConnection, never()).getCatalog(); // Ensure no further calls
    }

    @Test
    void testReportOpenConnectionBasicProperties() throws SQLException {
        final ReportJdbcConnection report = new ReportJdbcConnection(logger, mockConnection);
        report.run();

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
    void testReportConnectionWithNullCatalogAndSchema() throws SQLException {
        when(mockConnection.getCatalog()).thenReturn(null);
        when(mockConnection.getSchema()).thenReturn(null);

        final ReportJdbcConnection report = new ReportJdbcConnection(logger, mockConnection);
        report.run();

        assertEvent(logger, 0, Level.INFO, "JDBC connection", " - properties:");
        // Ensure catalog and schema are not printed
        assertEventNot(logger, 0, Level.INFO, " - catalog:");
        assertEventNot(logger, 0, Level.INFO, " - schema:");
    }

    @Test
    void testReportConnectionWithReadOnlyAndNoAutoCommit() throws SQLException {
        when(mockConnection.isReadOnly()).thenReturn(true);
        when(mockConnection.getAutoCommit()).thenReturn(false);

        final ReportJdbcConnection report = new ReportJdbcConnection(logger, mockConnection);
        report.run();

        assertEvent(logger, 0, Level.INFO, "read-only; ");
        assertEventNot(logger, 0, Level.INFO, "auto-commit; ");
    }

    @Test
    void testReportConnectionWithHoldabilityCloseCursors() throws SQLException {
        when(mockConnection.getHoldability()).thenReturn(ResultSet.CLOSE_CURSORS_AT_COMMIT);

        final ReportJdbcConnection report = new ReportJdbcConnection(logger, mockConnection);
        report.run();

        assertHasEvent(logger, "holdability=close-cursors-at-commit; ");
    }

    @Test
    void testReportConnectionWithHoldabilityUnknown() throws SQLException {
        when(mockConnection.getHoldability()).thenReturn(999); // Unknown value

        final ReportJdbcConnection report = new ReportJdbcConnection(logger, mockConnection);
        report.run();

        assertHasEvent(logger, "holdability=unknown; ");
    }

    @Test
    void testReportConnectionWithTransactionIsolationSerializable() throws SQLException {
        when(mockConnection.getTransactionIsolation()).thenReturn(Connection.TRANSACTION_SERIALIZABLE);

        final ReportJdbcConnection report = new ReportJdbcConnection(logger, mockConnection);
        report.run();

        assertHasEvent(logger, "transaction=serializable; ");
    }

    @Test
    void testReportConnectionWithTransactionIsolationNone() throws SQLException {
        when(mockConnection.getTransactionIsolation()).thenReturn(Connection.TRANSACTION_NONE);

        final ReportJdbcConnection report = new ReportJdbcConnection(logger, mockConnection);
        report.run();

        assertHasEvent(logger, "transaction=none; ");
    }

    @Test
    void testReportConnectionWithTransactionIsolationUnknown() throws SQLException {
        when(mockConnection.getTransactionIsolation()).thenReturn(999); // Unknown value

        final ReportJdbcConnection report = new ReportJdbcConnection(logger, mockConnection);
        report.run();

        assertHasEvent(logger, "transaction=unknown; ");
    }

    @Test
    void testReportConnectionWithClientInfo() throws SQLException {
        Properties clientInfo = new Properties();
        clientInfo.setProperty("ApplicationName", "MyApp");
        clientInfo.setProperty("ClientUser", "testuser");
        clientInfo.setProperty("Password", "secret"); // Should be masked
        when(mockConnection.getClientInfo()).thenReturn(clientInfo);

        final ReportJdbcConnection report = new ReportJdbcConnection(logger, mockConnection);
        report.run();

        assertHasEvent(logger, " - client info: ");
        assertHasEvent(logger, "ApplicationName=MyApp; ");
        assertHasEvent(logger, "ClientUser=testuser; ");
        assertHasEvent(logger, "Password=?; ");
    }

    @Test
    void testReportConnectionWithNullClientInfo() throws SQLException {
        when(mockConnection.getClientInfo()).thenReturn(null);

        final ReportJdbcConnection report = new ReportJdbcConnection(logger, mockConnection);
        report.run();

        assertHasEvent(logger, " - client info: n/a");
    }

    @Test
    void testReportConnectionWithMetaDataNull() throws SQLException {
        when(mockConnection.getMetaData()).thenReturn(null);

        final ReportJdbcConnection report = new ReportJdbcConnection(logger, mockConnection);
        report.run();

        // Ensure metadata related fields are not printed
        assertNoEvent(logger, "URL:");
        assertNoEvent(logger, "user name:");
        assertNoEvent(logger, " - database:");
        assertNoEvent(logger, " - driver:");
    }

    @Test
    void testReportConnectionWithSQLStateTypeXOpen() throws SQLException {
        when(mockMetaData.getSQLStateType()).thenReturn(DatabaseMetaData.sqlStateXOpen);

        final ReportJdbcConnection report = new ReportJdbcConnection(logger, mockConnection);
        report.run();

        assertHasEvent(logger, "sql-state-type=X-Open; ");
    }

    @Test
    void testReportConnectionWithSQLStateTypeUnknown() throws SQLException {
        when(mockMetaData.getSQLStateType()).thenReturn(999); // Unknown value

        final ReportJdbcConnection report = new ReportJdbcConnection(logger, mockConnection);
        report.run();

        assertHasEvent(logger, "sql-state-type=unknown; ");
    }

    @Test
    void testReportConnectionWithTypeMapEnabledAndPopulated() throws SQLException {
        Map<String, Class<?>> typeMap = new HashMap<>();
        typeMap.put("my_udt", String.class);
        typeMap.put("another_udt", Integer.class);
        when(mockConnection.getTypeMap()).thenReturn(typeMap);

        final ReportJdbcConnection report = new ReportJdbcConnection(logger, mockConnection)
                .printTypeMap(true);
        report.run();

        assertHasEvent(logger, " - type map: my_udt->String; another_udt->Integer;");
    }

    @Test
    void testReportConnectionWithTypeMapEnabledAndEmpty() throws SQLException {
        when(mockConnection.getTypeMap()).thenReturn(Collections.emptyMap());

        final ReportJdbcConnection report = new ReportJdbcConnection(logger, mockConnection)
                .printTypeMap(true);
        report.run();

        assertHasEvent(logger, " - type map: n/a");
    }

    @Test
    void testReportConnectionWithTypeMapEnabledAndNull() throws SQLException {
        when(mockConnection.getTypeMap()).thenReturn(null);

        final ReportJdbcConnection report = new ReportJdbcConnection(logger, mockConnection)
                .printTypeMap(true);
        report.run();

        assertHasEvent(logger, " - type map: n/a");
    }

    @Test
    void testReportConnectionWithTypeMapDisabled() throws SQLException {
        Map<String, Class<?>> typeMap = new HashMap<>();
        typeMap.put("my_udt", String.class);
        when(mockConnection.getTypeMap()).thenReturn(typeMap);

        final ReportJdbcConnection report = new ReportJdbcConnection(logger, mockConnection)
                .printTypeMap(false); // Explicitly disabled
        report.run();

        assertNoEvent(logger, " - type map:"); // Ensure type map is not printed
    }

    @Test
    void testReportConnectionSQLExceptionHandling() throws SQLException {
        when(mockConnection.getCatalog()).thenThrow(new SQLException("Mock SQL Exception"));

        final ReportJdbcConnection report = new ReportJdbcConnection(logger, mockConnection);
        report.run();

        assertHasEvent(logger, "Cannot read connection property: Mock SQL Exception");
    }

    @Test
    void testReportConnectionSchemaNoSuchMethodError() throws SQLException {
        // Simulate older JDK where getSchema() might not exist
        when(mockConnection.getSchema()).thenThrow(new NoSuchMethodError("Mock NoSuchMethodError"));

        final ReportJdbcConnection report = new ReportJdbcConnection(logger, mockConnection);
        report.run();

        assertNoEvent(logger, " - schema:"); // Schema should not be printed
        assertNoEvent(logger, "Cannot read connection property:"); // Should not catch NoSuchMethodError as SQLException
    }
}
