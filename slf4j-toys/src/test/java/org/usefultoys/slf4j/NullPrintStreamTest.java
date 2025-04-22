package org.usefultoys.slf4j;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NullPrintStreamTest {

    @BeforeAll
    static void validate() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    @Test
    void testWriteMethods() {
        NullPrintStream nullPrintStream = new NullPrintStream();
        assertDoesNotThrow(() -> nullPrintStream.write(new byte[]{1, 2, 3}));
        assertDoesNotThrow(() -> nullPrintStream.write(1));
        assertDoesNotThrow(() -> nullPrintStream.write(new byte[]{1, 2, 3}, 0, 2));
    }

    @Test
    void testPrintMethods() {
        NullPrintStream nullPrintStream = new NullPrintStream();
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
        NullPrintStream nullPrintStream = new NullPrintStream();
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
        NullPrintStream nullPrintStream = new NullPrintStream();
        assertDoesNotThrow(() -> nullPrintStream.append("test"));
        assertDoesNotThrow(() -> nullPrintStream.append('c'));
        assertDoesNotThrow(() -> nullPrintStream.append("test", 0, 2));
    }

    @Test
    void testFormatMethods() {
        NullPrintStream nullPrintStream = new NullPrintStream();
        assertDoesNotThrow(() -> nullPrintStream.format("test %s", "value"));
        assertDoesNotThrow(() -> nullPrintStream.format(Locale.US, "test %s", "value"));
    }

    @Test
    void testPrintfMethods() {
        NullPrintStream nullPrintStream = new NullPrintStream();
        assertDoesNotThrow(() -> nullPrintStream.printf("test %s", "value"));
        assertDoesNotThrow(() -> nullPrintStream.printf(Locale.US, "test %s", "value"));
    }
}
