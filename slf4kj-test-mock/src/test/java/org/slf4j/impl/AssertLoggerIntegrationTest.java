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
package org.slf4j.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.slf4j.impl.MockLoggerEvent.Level;

/**
 * Integration tests demonstrating the use of {@link AssertLogger} with the SLF4J Logger interface.
 * These tests show how to use AssertLogger in real-world scenarios where you obtain loggers
 * through the standard SLF4J LoggerFactory.
 */
@DisplayName("AssertLogger Integration")
class AssertLoggerIntegrationTest {

    @Test
    @DisplayName("should work with LoggerFactory.getLogger")
    void shouldWorkWithLoggerFactoryGetLogger() {
        // Given: A logger obtained through LoggerFactory (standard SLF4J approach)
        final Logger logger = LoggerFactory.getLogger("integration.test");

        // When: We log some messages
        logger.info("Starting application");
        logger.warn("Configuration file not found, using defaults");
        logger.error("Failed to connect to database");

        // Then: We can use AssertLogger to verify the logged events
        AssertLogger.assertEvent(logger, 0, Level.INFO, "Starting application");
        AssertLogger.assertEvent(logger, 1, Level.WARN, "Configuration file");
        AssertLogger.assertEvent(logger, 2, Level.ERROR, "Failed to connect");
    }

    @Test
    @DisplayName("should work with markers and formatted messages")
    void shouldWorkWithMarkersAndFormattedMessages() {
        // Given: A logger with markers
        final Logger logger = LoggerFactory.getLogger("security.audit");
        final Marker securityMarker = MarkerFactory.getMarker("SECURITY");
        final Marker auditMarker = MarkerFactory.getMarker("AUDIT");

        // When: We log security events with formatted messages
        logger.info(securityMarker, "User {} logged in from IP {}", "john.doe", "192.168.1.100");
        logger.warn(auditMarker, "Failed login attempt for user {} from IP {}", "admin", "10.0.0.1");

        // Then: We can verify the events including markers and message content
        AssertLogger.assertEvent(logger, 0, Level.INFO, securityMarker, "User", "logged in", "192.168.1.100");
        AssertLogger.assertEvent(logger, 1, Level.WARN, auditMarker, "Failed login", "admin", "10.0.0.1");
    }

    @Test
    @DisplayName("should work in realistic business logic test")
    void shouldWorkInRealisticBusinessLogicTest() {
        // Given: A service that uses logging
        final UserService userService = new UserService();

        // When: We perform operations that generate logs
        userService.createUser("alice", "alice@example.com");
        userService.createUser("", "invalid@example.com"); // Invalid username
        userService.deleteUser("bob");

        // Then: We can verify all the logged events
        final Logger logger = userService.getLogger();
        AssertLogger.assertEvent(logger, 0, Level.INFO, "Creating user");
        AssertLogger.assertEvent(logger, 1, Level.ERROR, "Invalid username");
        AssertLogger.assertEvent(logger, 2, Level.DEBUG, "Deleting user");
    }

    /**
     * Example service class that demonstrates logging in business logic
     */
    private static class UserService {
        private final Logger logger = LoggerFactory.getLogger(UserService.class);

        public void createUser(final String username, final String email) {
            if (username == null || username.trim().isEmpty()) {
                logger.error("Invalid username provided: {}", username);
                return;
            }
            logger.info("Creating user {} with email {}", username, email);
        }

        public void deleteUser(final String username) {
            logger.debug("Deleting user: {}", username);
        }

        public Logger getLogger() {
            return logger;
        }
    }
}