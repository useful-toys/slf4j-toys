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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.usefultoys.test.CharsetConsistency;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;


@ExtendWith(CharsetConsistency.class)
class NullOutputStreamTest {

    @Test
    void testWriteSingleByte() {
        final NullOutputStream nullOutputStream = new NullOutputStream();
        assertDoesNotThrow(() -> nullOutputStream.write(0));
        assertDoesNotThrow(nullOutputStream::flush);
        assertDoesNotThrow(nullOutputStream::close);
    }

    @Test
    void testWriteByteArray() {
        final NullOutputStream nullOutputStream = new NullOutputStream();
        assertDoesNotThrow(() -> nullOutputStream.write(new byte[]{1, 2, 3}));
        assertDoesNotThrow(nullOutputStream::flush);
        assertDoesNotThrow(nullOutputStream::close);
    }

    @Test
    void testWriteByteArrayWithOffsetAndLength() {
        final NullOutputStream nullOutputStream = new NullOutputStream();
        assertDoesNotThrow(() -> nullOutputStream.write(new byte[]{1, 2, 3, 4}, 1, 2));
        assertDoesNotThrow(nullOutputStream::flush);
        assertDoesNotThrow(nullOutputStream::close);
    }
}
