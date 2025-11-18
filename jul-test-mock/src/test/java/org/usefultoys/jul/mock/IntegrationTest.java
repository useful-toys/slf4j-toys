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
package org.usefultoys.jul.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.usefultoys.jul.mock.AssertHandler.*;

/**
 * Integration tests demonstrating real-world usage scenarios of the jul-test-mock library.
 */
@DisplayName("Integration Tests - Real World Examples")
class IntegrationTest {

    private Logger logger;
    private MockHandler handler;

    @BeforeEach
    @DisplayName("should setup logger and handler before each test")
    void setUp() {
        logger = Logger.getLogger("integration.test");
        logger.setUseParentHandlers(false);
        handler = new MockHandler();
        logger.addHandler(handler);
        logger.setLevel(Level.ALL);
        handler.clearRecords();
    }

    @Test
    @DisplayName("should test user authentication workflow")
    void shouldTestUserAuthenticationWorkflow() {
        // Simulate authentication workflow
        logger.info("Authentication started for user: admin");
        logger.fine("Validating credentials");
        logger.fine("Checking user permissions");
        logger.info("Authentication successful for user: admin");

        // Verify logging sequence
        assertRecordCount(handler, 4);
        assertRecordSequence(handler, Level.INFO, Level.FINE, Level.FINE, Level.INFO);
        assertRecordSequence(handler, "started", "Validating", "Checking", "successful");
        
        // Verify specific messages
        assertHasRecord(handler, "admin");
        assertHasRecord(handler, Level.FINE, "credentials");
        assertHasRecord(handler, Level.FINE, "permissions");
    }

    @Test
    @DisplayName("should test file processing with errors")
    void shouldTestFileProcessingWithErrors() {
        // Simulate file processing with error
        logger.info("Starting file processing: data.csv");
        logger.fine("Reading file header");
        logger.log(Level.WARNING, "Invalid row at line {0}", 42);
        logger.log(Level.SEVERE, "Failed to process file", new IOException("File corrupted"));

        // Verify error handling
        assertRecordCount(handler, 4);
        assertRecordCountByLevel(handler, Level.INFO, 1);
        assertRecordCountByLevel(handler, Level.FINE, 1);
        assertRecordCountByLevel(handler, Level.WARNING, 1);
        assertRecordCountByLevel(handler, Level.SEVERE, 1);
        
        // Verify error message contains line number
        assertHasRecord(handler, Level.WARNING, "line 42");
        
        // Verify exception was logged
        assertHasRecordWithThrowable(handler, IOException.class, "corrupted");
    }

    @Test
    @DisplayName("should test batch processing performance logging")
    void shouldTestBatchProcessingPerformanceLogging() {
        // Simulate batch processing
        logger.info("Batch processing started");
        
        for (int i = 1; i <= 5; i++) {
            logger.log(Level.FINE, "Processing batch {0} of 5", i);
        }
        
        logger.info("Batch processing completed");

        // Verify batch processing logs
        assertRecordCount(handler, 7);
        assertRecordCountByLevel(handler, Level.INFO, 2);
        assertRecordCountByLevel(handler, Level.FINE, 5);
        
        // Verify start and end messages
        assertRecord(handler, 0, Level.INFO, "started");
        assertRecord(handler, 6, Level.INFO, "completed");
        
        // Verify all batches were logged
        assertRecordCountByMessage(handler, "Processing batch", 5);
    }

    @Test
    @DisplayName("should test configuration loading with warnings")
    void shouldTestConfigurationLoadingWithWarnings() {
        // Simulate configuration loading
        logger.info("Loading application configuration");
        logger.config("Configuration file: /etc/app/config.properties");
        logger.log(Level.WARNING, "Missing optional parameter: {0}, using default", "maxConnections");
        logger.log(Level.WARNING, "Missing optional parameter: {0}, using default", "timeout");
        logger.info("Configuration loaded successfully");

        // Verify configuration messages
        assertRecordCount(handler, 5);
        assertRecordCountByLevel(handler, Level.WARNING, 2);
        
        // Verify warnings mention default values
        assertRecordCountByMessage(handler, "using default", 2);
        
        // Verify both parameters are mentioned
        assertHasRecord(handler, "maxConnections");
        assertHasRecord(handler, "timeout");
    }

    @Test
    @DisplayName("should test multi-step transaction rollback")
    void shouldTestMultiStepTransactionRollback() {
        // Simulate transaction with rollback
        logger.info("Transaction started: TX-12345");
        logger.fine("Executing step 1: Update account balance");
        logger.fine("Executing step 2: Record transaction history");
        logger.warning("Constraint violation detected in step 2");
        logger.info("Rolling back transaction: TX-12345");
        logger.severe("Transaction failed: TX-12345");

        // Verify transaction lifecycle
        assertRecordSequence(handler,
            "started", "step 1", "step 2", "Constraint", "Rolling back", "failed");
        
        // Verify transaction ID appears in key messages
        assertRecordCountByMessage(handler, "TX-12345", 3);
        
        // Verify severity escalation
        assertRecordCountByLevel(handler, Level.INFO, 2);
        assertRecordCountByLevel(handler, Level.FINE, 2);
        assertRecordCountByLevel(handler, Level.WARNING, 1);
        assertRecordCountByLevel(handler, Level.SEVERE, 1);
    }

    @Test
    @DisplayName("should test service startup sequence")
    void shouldTestServiceStartupSequence() {
        // Simulate service startup
        logger.info("Starting application server");
        logger.config("Server port: 8080");
        logger.config("Worker threads: 10");
        logger.fine("Initializing database connection pool");
        logger.fine("Initializing cache manager");
        logger.info("Application server started successfully");

        // Verify startup sequence
        assertRecordCount(handler, 6);
        assertRecord(handler, 0, Level.INFO, "Starting");
        assertRecord(handler, 5, Level.INFO, "started successfully");
        
        // Verify configuration details
        assertHasRecord(handler, Level.CONFIG, "8080");
        assertHasRecord(handler, Level.CONFIG, "10");
        
        // Verify initialization steps
        assertHasRecord(handler, Level.FINE, "database");
        assertHasRecord(handler, Level.FINE, "cache");
    }
}
