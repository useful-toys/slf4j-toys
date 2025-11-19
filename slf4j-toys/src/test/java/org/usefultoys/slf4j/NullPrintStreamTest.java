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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;


class NullPrintStreamTest {

    @BeforeAll
    static void validateConsistentCharset() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    @BeforeAll
    public static void setupConsistentLocale() {
        Locale.setDefault(Locale.ENGLISH);
    }

    @Test
    void testWriteMethods() {
        final NullPrintStream nullPrintStream = new NullPrintStream();
        assertDoesNotThrow(() -> nullPrintStream.write(new byte[]{1, 2, 3}));
        assertDoesNotThrow(() -> nullPrintStream.write(1));
        assertDoesNotThrow(() -> nullPrintStream.write(new byte[]{1, 2, 3}, 0, 2));
    }

    @Test
    void testPrintMethods() {
        final NullPrintStream nullPrintStream = new NullPrintStream();
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
    }

    @Test
    void testPrintlnMethods() {
        final NullPrintStream nullPrintStream = new NullPrintStream();
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
    }

    @Test
    void testAppendMethods() {
        final NullPrintStream nullPrintStream = new NullPrintStream();
        assertDoesNotThrow(() -> nullPrintStream.append("test"));
        assertDoesNotThrow(() -> nullPrintStream.append('c'));
        assertDoesNotThrow(() -> nullPrintStream.append("test", 0, 2));
    }

    @Test
    void testFormatMethods() {
        final NullPrintStream nullPrintStream = new NullPrintStream();
        assertDoesNotThrow(() -> nullPrintStream.format("test %s", "value"));
        assertDoesNotThrow(() -> nullPrintStream.format(Locale.US, "test %s", "value"));
    }

    @Test
    void testPrintfMethods() {
        final NullPrintStream nullPrintStream = new NullPrintStream();
        assertDoesNotThrow(() -> nullPrintStream.printf("test %s", "value"));
        assertDoesNotThrow(() -> nullPrintStream.printf(Locale.US, "test %s", "value"));
    }
}
