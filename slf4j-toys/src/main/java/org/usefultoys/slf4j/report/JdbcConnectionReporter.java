/**
 * Copyright 2017 Daniel Felix Ferber
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

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import org.usefultoys.slf4j.LoggerFactory;

/**
 *
 * @author Daniel
 */
public class JdbcConnectionReporter {

    /**
     * Logger that prints reports as information messages.
     */
    private final Logger logger;

    private boolean printTypeMap;

    public JdbcConnectionReporter(Logger logger) {
        this.logger = logger;
    }

    public JdbcConnectionReporter printTypeMap(boolean printTypeMap) {
        this.printTypeMap = printTypeMap;
        return this;
    }

    public void run(Connection connection) {
        PrintStream ps = LoggerFactory.getInfoPrintStream(logger);
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
            } catch (NoSuchMethodError ignore) {
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
            } catch (NoSuchMethodError ignore) {
                // only since 1.7
            }
            ps.print("transaction=");
            switch (connection.getTransactionIsolation()) {
                case Connection.TRANSACTION_READ_UNCOMMITTED:
                    ps.print("read-uncommited; ");
                    break;
                case Connection.TRANSACTION_READ_COMMITTED:
                    ps.print("read-commited; ");
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
            final Properties info = connection.getClientInfo();
            if (info == null || info.isEmpty()) {
                ps.println("n/a");
            } else {
                int i = 1;
                for (Map.Entry<Object, Object> entry : info.entrySet()) {
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
                ps.print(" - driver: " + metadata.getDriverName() + " (" + metadata.getDriverVersion() + ")");
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
                Map<String, Class<?>> map = connection.getTypeMap();
                ps.println(" - type map: ");
                if (map == null || map.isEmpty()) {
                    ps.println("n/a");
                } else {
                    int i = 1;
                    for (Map.Entry<String, Class<?>> entry : map.entrySet()) {
                        if (i++ % 5 == 0) {
                            ps.print("\n      ");
                        }
                        ps.println(entry.getKey() + "->" + entry.getClass() + "; ");
                    }
                    ps.println();
                }
            }
        } catch (SQLException e) {
            ps.println("   Cannot read property: " + e.getLocalizedMessage());
        } finally {
            ps.close();
        }
    }

}
