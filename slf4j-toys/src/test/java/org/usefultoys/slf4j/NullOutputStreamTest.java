package org.usefultoys.slf4j;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NullOutputStreamTest {

    @BeforeAll
    static void validate() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    @Test
    void testWriteSingleByte() {
        final NullOutputStream nullOutputStream = new NullOutputStream();
        assertDoesNotThrow(() -> nullOutputStream.write(0));
    }

    @Test
    void testWriteByteArray() {
        final NullOutputStream nullOutputStream = new NullOutputStream();
        assertDoesNotThrow(() -> nullOutputStream.write(new byte[]{1, 2, 3}));
    }

    @Test
    void testWriteByteArrayWithOffsetAndLength() {
        final NullOutputStream nullOutputStream = new NullOutputStream();
        assertDoesNotThrow(() -> nullOutputStream.write(new byte[]{1, 2, 3, 4}, 1, 2));
    }
}
