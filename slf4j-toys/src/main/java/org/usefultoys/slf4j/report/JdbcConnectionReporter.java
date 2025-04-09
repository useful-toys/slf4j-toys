/*
 * Copyright 2024 Daniel Felix Ferber
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.usefultoys.slf4j.report;

import org.slf4j.Logger;
import org.usefultoys.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

/**
 * Reports details about a JDBC {@link Connection}, such as catalog, schema, metadata, properties,
 * transaction settings, client info, and optionally the type map.
 * <p>
 * This class logs information using a SLF4J {@link Logger}, formatting it to resemble a structured diagnostic output.
 * It is useful for debugging or documenting the JDBC environment at runtime.
 * <p>
 * Typical usage:
 * <pre>{@code
 * JdbcConnectionReporter reporter = new JdbcConnectionReporter(logger)
 *     .printTypeMap(true);
 * reporter.run(connection);
 * }</pre>
 *
 * @author Daniel
 */
@SuppressWarnings("Since15")
public class JdbcConnectionReporter implements Runnable {

    /** Logger that prints the JDBC connection report. */
    private final Logger logger;
    private final Connection connection;

    /** Whether to print the JDBC type map. */
    private boolean printTypeMap;

    /**
     * Creates a new reporter that logs output using the specified SLF4J logger.
     *
     * @param logger the SLF4J logger to use for reporting
     */
    public JdbcConnectionReporter(final Logger logger, final Connection connection) {
        this.logger = logger;
        this.connection = connection;
    }

    /**
     * Defines whether the JDBC type map should be printed.
     *
     * @param printTypeMap {@code true} to print the type map; {@code false} to skip it
     * @return this instance, for method chaining
     */
    public JdbcConnectionReporter printTypeMap(final boolean printTypeMap) {
        this.printTypeMap = printTypeMap;
        return this;
    }

    /**
     * Executes the report for the specified JDBC {@link Connection}.
     *
     * <p>This method logs the output using {@link LoggerFactory#getInfoPrintStream(Logger)}.
     * If the connection is closed, it logs a message and exits early.</p>
     *
     * @param connection the JDBC connection to inspect
     */
    @Override
    public void run() {
        final PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
        ps.println("JDBC connection");
        try {
            if (connection.isClosed()) {
                ps.println(" - Closed! ");
                return;
            }
            if (connection.getCatalog() != null) {
                ps.println(" - catalog: " + connection.getCatalog());
            }
            try {
                if (connection.getSchema() != null) {
                    ps.println(" - schema: " + connection.getSchema());
                }
            } catch (final NoSuchMethodError ignore) {
                // only since 1.7
            }
            final DatabaseMetaData metadata = connection.getMetaData();
            if (metadata != null) {
                ps.println("    URL: " + metadata.getURL());
                ps.println("    user name: " + metadata.getUserName());
            }
            ps.print(" - properties: ");
            if (connection.isReadOnly()) {
                ps.print("read-only; ");
            }
            if (connection.getAutoCommit()) {
                ps.print("auto-commit; ");
            }
            ps.print("holdability=");
            switch (connection.getHoldability()) {
                case ResultSet.HOLD_CURSORS_OVER_COMMIT:
                    ps.print("hold-cursors-over-commit; ");
                    break;
                case ResultSet.CLOSE_CURSORS_AT_COMMIT:
                    ps.print("close-cursors-at-commit; ");
                    break;
                default:
                    ps.print("unknown; ");
            }
            try {
                ps.print("timeout=" + connection.getNetworkTimeout() + "ms; ");
            } catch (final NoSuchMethodError ignore) {
                // only since 1.7
            }
            ps.print("transaction=");
            switch (connection.getTransactionIsolation()) {
                case Connection.TRANSACTION_READ_UNCOMMITTED:
                    ps.print("read-uncommited; ");
                    break;
                case Connection.TRANSACTION_READ_COMMITTED:
                    ps.print("read-committed; ");
                    break;
                case Connection.TRANSACTION_REPEATABLE_READ:
                    ps.print("repeatable-read; ");
                    break;
                case Connection.TRANSACTION_SERIALIZABLE:
                    ps.print("serializable; ");
                    break;
                case Connection.TRANSACTION_NONE:
                    ps.print("none; ");
                    break;
                default:
                    ps.print("unknown; ");
            }
            ps.println();
            ps.print(" - client info: ");
            //noinspection CollectionDeclaredAsConcreteClass
            final Properties info = connection.getClientInfo();
            if (info == null || info.isEmpty()) {
                ps.println("n/a");
            } else {
                int i = 1;
                for (final Map.Entry<Object, Object> entry : info.entrySet()) {
                    if (i++ % 5 == 0) {
                        ps.print("\n      ");
                    }
                    final String name = (String) entry.getKey();
                    if (name.toLowerCase().contains("password")) {
                        ps.print(name + "=?; ");
                        continue;
                    }
                    ps.print(name + "=" + entry.getValue());
                }
                ps.println();
            }
            if (metadata != null) {
                ps.println(" - database: " + metadata.getDatabaseProductName() + " (" + metadata.getDatabaseProductVersion() + ")");
                ps.print(" - driver: " + metadata.getDriverName() + " (" + metadata.getDriverVersion() + "); ");
                ps.print("jdbc-version=" + metadata.getJDBCMajorVersion() + "." + metadata.getJDBCMinorVersion() + "; ");
                ps.print("max-connections=" + metadata.getMaxConnections() + "; ");
                ps.print("sql-state-type=");
                switch (metadata.getSQLStateType()) {
                    case DatabaseMetaData.sqlStateSQL99:
                        ps.print("SQL99; ");
                        break;
                    case DatabaseMetaData.sqlStateXOpen:
                        ps.print("X-Open; ");
                        break;
                    default:
                        ps.print("unknown; ");
                }
                ps.println();
            }
            if (printTypeMap) {
                final Map<String, Class<?>> map = connection.getTypeMap();
                ps.print(" - type map: ");
                if (map == null || map.isEmpty()) {
                    ps.println("n/a");
                } else {
                    int i = 1;
                    for (final Map.Entry<String, Class<?>> entry : map.entrySet()) {
                        if (i++ % 3 == 0) {
                            ps.print("\n      ");
                        }
                        ps.print(entry.getKey() + "->" + entry.getValue().getSimpleName() + "; ");
                    }
                    ps.println();
                }
            }
        } catch (final SQLException e) {
            ps.println("   Cannot read property: " + e.getLocalizedMessage());
        } finally {
            ps.close();
        }
    }
}
