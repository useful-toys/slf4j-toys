package org.usefultoys.slf4j;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class NullOutputStreamTest {

    @Test
    void testWriteSingleByte() {
        NullOutputStream nullOutputStream = new NullOutputStream();
        assertDoesNotThrow(() -> nullOutputStream.write(0));
    }

    @Test
    void testWriteByteArray() {
        NullOutputStream nullOutputStream = new NullOutputStream();
        assertDoesNotThrow(() -> nullOutputStream.write(new byte[]{1, 2, 3}));
    }

    @Test
    void testWriteByteArrayWithOffsetAndLength() {
        NullOutputStream nullOutputStream = new NullOutputStream();
        assertDoesNotThrow(() -> nullOutputStream.write(new byte[]{1, 2, 3, 4}, 1, 2));
    }
}
