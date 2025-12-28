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

package org.usefultoys.slf4j;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.usefultoys.test.ResetSessionConfig;
import org.usefultoys.test.ValidateCharset;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Session}.
 * <p>
 * Tests validate that Session correctly generates and manages UUIDs,
 * with proper immutability and formatting validation.
 */
@ValidateCharset
@ResetSessionConfig
class SessionTest {

    @Test
    @DisplayName("should generate non-null UUID")
    void shouldGenerateNonNullUuid() {
        // Given: Session class loaded
        // When: UUID is accessed
        // Then: should return non-null value
        assertNotNull(Session.uuid, "Session UUID should not be null");
    }

    @Test
    @DisplayName("should keep UUID immutable across accesses")
    void shouldKeepUuidImmutableAcrossAccesses() {
        // Given: Session with generated UUID
        final String firstUuid = Session.uuid;
        // When: UUID is accessed again
        final String secondUuid = Session.uuid;
        // Then: should return the same UUID
        assertEquals(firstUuid, secondUuid, "Session UUID should remain constant");
    }

    @Test
    @DisplayName("should format UUID as 32-character hexadecimal string")
    void shouldFormatUuidAs32CharacterHexadecimalString() {
        // Given: Session with generated UUID
        // When: UUID format is validated
        // Then: should match hexadecimal pattern
        assertTrue(Session.uuid.matches("^[a-f0-9]{32}$"), "Session UUID should be a 32-character hexadecimal string");
    }

    @Test
    @DisplayName("should return short UUID with default size")
    void shouldReturnShortUuidWithDefaultSize() {
        // Given: Session with default uuidSize
        // When: shortSessionUuid() is called
        final String shortUuid = Session.shortSessionUuid();
        // Then: should return non-null value and match size
        assertNotNull(shortUuid, "shortSessionUuid() should not return null");
        assertTrue(Session.uuid.endsWith(shortUuid), "UUID should end with short UUID");
        assertEquals(SessionConfig.uuidSize, shortUuid.length(), "shortSessionUuid() should return a string of length " + SessionConfig.uuidSize);
    }

    @Test
    @DisplayName("should return short UUID with custom size")
    void shouldReturnShortUuidWithCustomSize() {
        // Given: SessionConfig with custom uuidSize
        SessionConfig.uuidSize = 10;
        // When: shortSessionUuid() is called
        final String shortUuid = Session.shortSessionUuid();
        // Then: should return non-null value with custom size
        assertNotNull(shortUuid, "shortSessionUuid() should not return null");
        assertTrue(Session.uuid.endsWith(shortUuid), "UUID should end with short UUID");
        assertEquals(10, shortUuid.length(), "shortSessionUuid() should return a string of length 10");
    }
}
