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
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.usefultoys.jul.mock.AssertHandler.*;

@DisplayName("AssertHandler Tests")
class AssertHandlerTest {

    private Logger logger;
    private MockHandler handler;

    @BeforeEach
    @DisplayName("should setup logger and handler before each test")
    void setUp() {
        logger = Logger.getLogger("test.logger");
        logger.setUseParentHandlers(false);
        handler = new MockHandler();
        logger.addHandler(handler);
        logger.setLevel(Level.ALL);
        handler.clearRecords();
    }

    @Test
    @DisplayName("should assert record with message")
    void shouldAssertRecordWithMessage() {
        logger.info("Test message");

        assertRecord(handler, 0, "Test");
        assertRecord(handler, 0, "message");
        assertRecord(handler, 0, "Test message");
    }

    @Test
    @DisplayName("should assert record with level and message")
    void shouldAssertRecordWithLevelAndMessage() {
        logger.info("Info message");
        logger.warning("Warning message");

        assertRecord(handler, 0, Level.INFO, "Info");
        assertRecord(handler, 1, Level.WARNING, "Warning");
    }

    @Test
    @DisplayName("should assert record with level and multiple message parts")
    void shouldAssertRecordWithLevelAndMultipleMessageParts() {
        logger.info("This is a test message");

        assertRecord(handler, 0, Level.INFO, "This", "test", "message");
    }

    @Test
    @DisplayName("should assert has record with message")
    void shouldAssertHasRecordWithMessage() {
        logger.info("First message");
        logger.warning("Second message");
        logger.severe("Third message");

        assertHasRecord(handler, "First");
        assertHasRecord(handler, "Second");
        assertHasRecord(handler, "Third");
    }

    @Test
    @DisplayName("should assert has record with level and message")
    void shouldAssertHasRecordWithLevelAndMessage() {
        logger.info("Info message");
        logger.warning("Warning message");

        assertHasRecord(handler, Level.INFO, "Info");
        assertHasRecord(handler, Level.WARNING, "Warning");
    }

    @Test
    @DisplayName("should assert has record with level and multiple message parts")
    void shouldAssertHasRecordWithLevelAndMultipleMessageParts() {
        logger.info("This is a complex test message");

        assertHasRecord(handler, Level.INFO, "complex", "test", "message");
    }

    @Test
    @DisplayName("should assert record with throwable")
    void shouldAssertRecordWithThrowable() {
        logger.log(Level.SEVERE, "Error occurred", new IOException("Connection failed"));

        assertRecordWithThrowable(handler, 0, IOException.class);
        assertRecordWithThrowable(handler, 0, IOException.class, "Connection");
    }

    @Test
    @DisplayName("should assert record has throwable")
    void shouldAssertRecordHasThrowable() {
        logger.log(Level.SEVERE, "Error occurred", new RuntimeException("Test exception"));

        assertRecordHasThrowable(handler, 0);
    }

    @Test
    @DisplayName("should assert has record with throwable")
    void shouldAssertHasRecordWithThrowable() {
        logger.info("Normal message");
        logger.log(Level.SEVERE, "Error", new SQLException("Database error"));

        assertHasRecordWithThrowable(handler, SQLException.class);
        assertHasRecordWithThrowable(handler, SQLException.class, "Database");
        assertHasRecordWithThrowable(handler);
    }

    @Test
    @DisplayName("should assert record count")
    void shouldAssertRecordCount() {
        logger.info("Message 1");
        logger.info("Message 2");
        logger.info("Message 3");

        assertRecordCount(handler, 3);
    }

    @Test
    @DisplayName("should assert no records")
    void shouldAssertNoRecords() {
        assertNoRecords(handler);
    }

    @Test
    @DisplayName("should assert record count by level")
    void shouldAssertRecordCountByLevel() {
        logger.info("Info 1");
        logger.info("Info 2");
        logger.warning("Warning 1");
        logger.severe("Severe 1");

        assertRecordCountByLevel(handler, Level.INFO, 2);
        assertRecordCountByLevel(handler, Level.WARNING, 1);
        assertRecordCountByLevel(handler, Level.SEVERE, 1);
    }

    @Test
    @DisplayName("should assert record count by message")
    void shouldAssertRecordCountByMessage() {
        logger.info("Error in module A");
        logger.warning("Error in module B");
        logger.severe("Success in module C");

        assertRecordCountByMessage(handler, "Error", 2);
        assertRecordCountByMessage(handler, "module", 3);
        assertRecordCountByMessage(handler, "Success", 1);
    }

    @Test
    @DisplayName("should assert record sequence by levels")
    void shouldAssertRecordSequenceByLevels() {
        logger.info("Info");
        logger.warning("Warning");
        logger.severe("Severe");

        assertRecordSequence(handler, Level.INFO, Level.WARNING, Level.SEVERE);
    }

    @Test
    @DisplayName("should assert record sequence by message parts")
    void shouldAssertRecordSequenceByMessageParts() {
        logger.info("Starting process");
        logger.info("Processing data");
        logger.info("Completing process");

        assertRecordSequence(handler, "Starting", "Processing", "Completing");
    }
}
