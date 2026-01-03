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
import org.usefultoys.test.WithLocale;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Unit tests for {@link NullPrintStream}.
 * <p>
 * Tests validate that NullPrintStream silently ignores all output operations
 * without throwing exceptions, providing a no-op PrintStream implementation.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>Write Methods:</b> Tests write operations with byte arrays, single bytes, and offset/length variants</li>
 *   <li><b>Print Methods:</b> Verifies print operations for all primitive types and objects</li>
 *   <li><b>Println Methods:</b> Ensures println operations for all types execute without exceptions</li>
 *   <li><b>Append Methods:</b> Validates append operations with various parameters</li>
 *   <li><b>Format Methods:</b> Tests format and printf methods with different arguments</li>
 *   <li><b>Error Checking:</b> Confirms no errors are reported after operations</li>
 * </ul>
 */
@ValidateCharset
@WithLocale("en")
class NullPrintStreamTest {

    @Test
    @DisplayName("should execute all write methods without throwing exceptions")
    void shouldExecuteAllWriteMethodsWithoutThrowingExceptions() {
        // Given: a new NullPrintStream instance
        final NullPrintStream nullPrintStream = new NullPrintStream();
        // When: write methods are called with various arguments
        assertDoesNotThrow(() -> nullPrintStream.write(new byte[]{1, 2, 3}));
        assertDoesNotThrow(() -> nullPrintStream.write(1));
        assertDoesNotThrow(() -> nullPrintStream.write(new byte[]{1, 2, 3}, 0, 2));
        // Then: should not report errors and should not throw
        assertFalse(nullPrintStream.checkError(), "should not report errors after write operations");
        assertDoesNotThrow(nullPrintStream::flush);
        assertDoesNotThrow(nullPrintStream::close);
    }

    @Test
    @DisplayName("should execute all print methods without throwing exceptions")
    void shouldExecuteAllPrintMethodsWithoutThrowingExceptions() {
        // Given: a new NullPrintStream instance
        final NullPrintStream nullPrintStream = new NullPrintStream();
        // When: print methods are called with various data types
        assertDoesNotThrow(() -> nullPrintStream.print("test"));
        assertDoesNotThrow(() -> nullPrintStream.print(' '));
        assertDoesNotThrow(() -> nullPrintStream.print(true));
        assertDoesNotThrow(() -> nullPrintStream.print(123));
        assertDoesNotThrow(() -> nullPrintStream.print(123L));
        assertDoesNotThrow(() -> nullPrintStream.print(123.0));
        assertDoesNotThrow(() -> nullPrintStream.print(123.0f));
        assertDoesNotThrow(() -> nullPrintStream.print(true));
        assertDoesNotThrow(() -> nullPrintStream.print(new Object()));
        assertDoesNotThrow(() -> nullPrintStream.print(new char[]{'a', 'b'}));
        // Then: should not report errors
        assertFalse(nullPrintStream.checkError(), "should not report errors after print operations");
        assertDoesNotThrow(nullPrintStream::flush);
        assertDoesNotThrow(nullPrintStream::close);
    }

    @Test
    @DisplayName("should execute all println methods without throwing exceptions")
    void shouldExecuteAllPrintlnMethodsWithoutThrowingExceptions() {
        // Given: a new NullPrintStream instance
        final NullPrintStream nullPrintStream = new NullPrintStream();
        // When: println methods are called with various data types
        assertDoesNotThrow(() -> nullPrintStream.println());
        assertDoesNotThrow(() -> nullPrintStream.println("test"));
        assertDoesNotThrow(() -> nullPrintStream.println(' '));
        assertDoesNotThrow(() -> nullPrintStream.println(123));
        assertDoesNotThrow(() -> nullPrintStream.println(123L));
        assertDoesNotThrow(() -> nullPrintStream.println(123.0));
        assertDoesNotThrow(() -> nullPrintStream.println(123.0f));
        assertDoesNotThrow(() -> nullPrintStream.println(true));
        assertDoesNotThrow(() -> nullPrintStream.println(new Object()));
        assertDoesNotThrow(() -> nullPrintStream.println(new char[]{'a', 'b'}));
        // Then: should not report errors
        assertFalse(nullPrintStream.checkError(), "should not report errors after println operations");
        assertDoesNotThrow(nullPrintStream::flush);
        assertDoesNotThrow(nullPrintStream::close);
    }

    @Test
    @DisplayName("should execute all append methods without throwing exceptions")
    void shouldExecuteAllAppendMethodsWithoutThrowingExceptions() {
        // Given: a new NullPrintStream instance
        final NullPrintStream nullPrintStream = new NullPrintStream();
        // When: append methods are called with various arguments
        assertDoesNotThrow(() -> nullPrintStream.append("test"));
        assertDoesNotThrow(() -> nullPrintStream.append('c'));
        assertDoesNotThrow(() -> nullPrintStream.append("test", 0, 2));
        // Then: should not report errors
        assertFalse(nullPrintStream.checkError(), "should not report errors after append operations");
        assertDoesNotThrow(nullPrintStream::flush);
        assertDoesNotThrow(nullPrintStream::close);
    }

    @Test
    @DisplayName("should execute all format methods without throwing exceptions")
    void shouldExecuteAllFormatMethodsWithoutThrowingExceptions() {
        // Given: a new NullPrintStream instance
        final NullPrintStream nullPrintStream = new NullPrintStream();
        // When: format methods are called with format strings
        assertDoesNotThrow(() -> nullPrintStream.format("test %s", "value"));
        assertDoesNotThrow(() -> nullPrintStream.format(Locale.US, "test %s", "value"));
        // Then: should not report errors
        assertFalse(nullPrintStream.checkError(), "should not report errors after format operations");
        assertDoesNotThrow(nullPrintStream::flush);
        assertDoesNotThrow(nullPrintStream::close);
    }

    @Test
    @DisplayName("should execute all printf methods without throwing exceptions")
    void shouldExecuteAllPrintfMethodsWithoutThrowingExceptions() {
        // Given: a new NullPrintStream instance
        final NullPrintStream nullPrintStream = new NullPrintStream();
        // When: printf methods are called with format strings
        assertDoesNotThrow(() -> nullPrintStream.printf("test %s", "value"));
        assertDoesNotThrow(() -> nullPrintStream.printf(Locale.US, "test %s", "value"));
        // Then: should not report errors
        assertFalse(nullPrintStream.checkError(), "should not report errors after printf operations");
        assertDoesNotThrow(nullPrintStream::flush);
        assertDoesNotThrow(nullPrintStream::close);
    }
}
