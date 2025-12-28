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
import org.usefultoys.test.ValidateCharset;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Unit tests for {@link NullOutputStream}.
 * <p>
 * Tests validate that NullOutputStream silently ignores all write operations
 * without throwing exceptions, providing a no-op OutputStream implementation.
 */
@ValidateCharset
class NullOutputStreamTest {

    @Test
    @DisplayName("should write single byte without throwing exceptions")
    void shouldWriteSingleByteWithoutThrowingExceptions() {
        // Given: a new NullOutputStream instance
        final NullOutputStream nullOutputStream = new NullOutputStream();
        // When: write(int) is called
        assertDoesNotThrow(() -> nullOutputStream.write(0));
        // Then: should not throw exceptions and operations should succeed
        assertDoesNotThrow(nullOutputStream::flush);
        assertDoesNotThrow(nullOutputStream::close);
    }

    @Test
    @DisplayName("should write byte array without throwing exceptions")
    void shouldWriteByteArrayWithoutThrowingExceptions() {
        // Given: a new NullOutputStream instance
        final NullOutputStream nullOutputStream = new NullOutputStream();
        // When: write(byte[]) is called
        assertDoesNotThrow(() -> nullOutputStream.write(new byte[]{1, 2, 3}));
        // Then: should not throw exceptions and operations should succeed
        assertDoesNotThrow(nullOutputStream::flush);
        assertDoesNotThrow(nullOutputStream::close);
    }

    @Test
    @DisplayName("should write byte array with offset and length without throwing exceptions")
    void shouldWriteByteArrayWithOffsetAndLengthWithoutThrowingExceptions() {
        // Given: a new NullOutputStream instance
        final NullOutputStream nullOutputStream = new NullOutputStream();
        // When: write(byte[], int, int) is called
        assertDoesNotThrow(() -> nullOutputStream.write(new byte[]{1, 2, 3, 4}, 1, 2));
        // Then: should not throw exceptions and operations should succeed
        assertDoesNotThrow(nullOutputStream::flush);
        assertDoesNotThrow(nullOutputStream::close);
    }
}
