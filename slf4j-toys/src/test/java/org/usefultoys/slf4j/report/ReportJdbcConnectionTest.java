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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.MockLogger;
import org.usefultoys.slf4j.SessionConfig;
import org.usefultoys.slf4j.SystemConfig;

import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReportJdbcConnectionTest {
    private MockLogger logger;
    private Connection connection;
    private ReportJdbcConnection reporter;

    @BeforeAll
    static void validate() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    @BeforeEach
    void resetWatcherConfigBeforeEach() {
        // Reinitialize each configuration to ensure a clean configuration before each test
        ReporterConfig.reset();
        SessionConfig.reset();
        SystemConfig.reset();
    }

    @AfterAll
    static void resetWatcherConfigAfterAll() {
        // Reinitialize each configuration to ensure a clean configuration before each test
        ReporterConfig.reset();
        SessionConfig.reset();
        SystemConfig.reset();
    }

    @BeforeEach
    void setUp() {
        logger = (MockLogger) LoggerFactory.getLogger("test.jdbc");
        logger.clearEvents();

        connection = mock(Connection.class);
        reporter = new ReportJdbcConnection(logger, connection);
    }

    @Test
    void shouldLogClosedJdbcConnectionInfo() throws SQLException {

        // Arrange
        when(connection.isClosed()).thenReturn(true);

        // Act
        reporter.run();

        // Assert
        final MockLogger mockLogger = logger;
        final String fullLog = mockLogger.getEvent(0).getFormattedMessage();  // we assume all output is printed in one event

        assertTrue(fullLog.contains("Closed!"));
    }

    @Test
    void shouldLogJdbcConnectionInfo() throws SQLException {

        // Arrange
        final DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        final Properties clientInfo = new Properties();
        clientInfo.setProperty("appName", "testApp");

        when(connection.isClosed()).thenReturn(false);
        when(connection.getCatalog()).thenReturn("catalog1");
        when(connection.getSchema()).thenReturn("public");
        when(connection.getMetaData()).thenReturn(metaData);
        when(connection.isReadOnly()).thenReturn(false);
        when(connection.getAutoCommit()).thenReturn(true);
        when(connection.getHoldability()).thenReturn(ResultSet.HOLD_CURSORS_OVER_COMMIT);
        when(connection.getTransactionIsolation()).thenReturn(Connection.TRANSACTION_SERIALIZABLE);
        when(connection.getClientInfo()).thenReturn(clientInfo);
        when(connection.getTypeMap()).thenReturn(null);

        when(metaData.getURL()).thenReturn("jdbc:test://localhost/db");
        when(metaData.getUserName()).thenReturn("db_user");
        when(metaData.getDatabaseProductName()).thenReturn("TestDB");
        when(metaData.getDatabaseProductVersion()).thenReturn("1.0");
        when(metaData.getDriverName()).thenReturn("TestDriver");
        when(metaData.getDriverVersion()).thenReturn("1.2.3");
        when(metaData.getJDBCMajorVersion()).thenReturn(4);
        when(metaData.getJDBCMinorVersion()).thenReturn(2);
        when(metaData.getMaxConnections()).thenReturn(100);
        when(metaData.getSQLStateType()).thenReturn(DatabaseMetaData.sqlStateSQL99);

        // Act
        reporter.run();

        // Assert
        final MockLogger mockLogger = logger;
        final String fullLog = mockLogger.getEvent(0).getFormattedMessage();  // we assume all output is printed in one event

        assertTrue(fullLog.contains("JDBC connection"));
        assertTrue(fullLog.contains("catalog: catalog1"));
        assertTrue(fullLog.contains("schema: public"));
        assertTrue(fullLog.contains("URL: jdbc:test://localhost/db"));
        assertTrue(fullLog.contains("user name: db_user"));
        assertTrue(fullLog.contains("properties: auto-commit; holdability=hold-cursors-over-commit; timeout=0ms; transaction=serializable;"));
        assertTrue(fullLog.contains("database: TestDB"));
        assertTrue(fullLog.contains("driver: TestDriver (1.2.3); jdbc-version=4.2; max-connections=100; sql-state-type=SQL99;"));
        assertTrue(fullLog.contains("transaction=serializable"));
        assertTrue(fullLog.contains("client info: appName=testApp"));
    }

    @Test
    void shouldLogJdbcConnectionInfoOtherAttributes() throws SQLException {
        // Arrange
        when(connection.isClosed()).thenReturn(false);
        when(connection.getCatalog()).thenReturn(null);
        when(connection.getSchema()).thenReturn(null);
        when(connection.getMetaData()).thenReturn(null);
        when(connection.isReadOnly()).thenReturn(true);
        when(connection.getAutoCommit()).thenReturn(false);
        when(connection.getHoldability()).thenReturn(ResultSet.CLOSE_CURSORS_AT_COMMIT);
        when(connection.getTransactionIsolation()).thenReturn(Connection.TRANSACTION_REPEATABLE_READ);
        when(connection.getClientInfo()).thenReturn(null);
        when(connection.getTypeMap()).thenReturn(null);

        // Act
        reporter.run();

        // Assert
        final MockLogger mockLogger = logger;
        final String fullLog = mockLogger.getEvent(0).getFormattedMessage();  // we assume all output is printed in one event

        assertTrue(fullLog.contains("JDBC connection"));
        assertTrue(fullLog.contains("properties: read-only; holdability=close-cursors-at-commit; timeout=0ms; transaction=repeatable-read;"));
        assertTrue(fullLog.contains("client info: n/a"));
    }

    @Test
    void shouldLogTypeMapWhenEnabled() throws SQLException {
        // Arrange
        reporter.printTypeMap(true);


        when(connection.isClosed()).thenReturn(false);
        when(connection.getCatalog()).thenReturn(null);
        when(connection.getMetaData()).thenReturn(null);
        when(connection.getSchema()).thenReturn(null);
        when(connection.isReadOnly()).thenReturn(false);
        when(connection.getAutoCommit()).thenReturn(true);
        when(connection.getHoldability()).thenReturn(ResultSet.CLOSE_CURSORS_AT_COMMIT);
        when(connection.getTransactionIsolation()).thenReturn(Connection.TRANSACTION_NONE);
        when(connection.getClientInfo()).thenReturn(null);

        final Map<String, Class<?>> typeMap = new LinkedHashMap<>();
        typeMap.put("uuid", java.util.UUID.class);
        typeMap.put("jsonb", java.lang.String.class);
        when(connection.getTypeMap()).thenReturn(typeMap);

        // Act
        reporter.run();

        // Assert
        final MockLogger mockLogger = logger;
        final String fullLog = mockLogger.getEvent(0).getFormattedMessage();

        assertTrue(fullLog.contains("type map: uuid->UUID; jsonb->String; "));
    }

    @Test
    void shouldLogEmptyTypeMap() throws SQLException {
        // Arrange
        reporter.printTypeMap(true);


        when(connection.isClosed()).thenReturn(false);
        when(connection.getCatalog()).thenReturn(null);
        when(connection.getMetaData()).thenReturn(null);
        when(connection.getSchema()).thenReturn(null);
        when(connection.isReadOnly()).thenReturn(false);
        when(connection.getAutoCommit()).thenReturn(true);
        when(connection.getHoldability()).thenReturn(ResultSet.CLOSE_CURSORS_AT_COMMIT);
        when(connection.getTransactionIsolation()).thenReturn(Connection.TRANSACTION_READ_COMMITTED);
        when(connection.getClientInfo()).thenReturn(null);

        final Map<String, Class<?>> typeMap = new LinkedHashMap<>();
        when(connection.getTypeMap()).thenReturn(typeMap);

        // Act
        reporter.run();

        // Assert
        final MockLogger mockLogger = logger;
        final String fullLog = mockLogger.getEvent(0).getFormattedMessage();

        assertTrue(fullLog.contains("type map: n/a"));
    }

}
