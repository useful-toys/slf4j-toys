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

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * An {@link OutputStream} implementation that buffers data and redirects it to a logger when {@link #close()} is called.
 * <p>
 * Intended for handling moderate volumes of output data that should be logged as complete messages instead of
 * character-by-character.
 * <p>
 * Instances should be obtained via the factory methods in {@link LoggerFactory}.
 * <p>
 * This class is package-private and not intended to be instantiated directly outside this library.
 *
 * @author Daniel Felix Ferber
 */
abstract class LoggerOutputStream extends OutputStream {

    /**
     * Creates a new logger output stream.
     */
    LoggerOutputStream() {
        // prevent instances outside this library
    }

    /**
     * Internal buffer to accumulate written data until flushed or closed.
     */
    private final ByteArrayOutputStream os = new ByteArrayOutputStream(0x3FFF);

    /**
     * Closes this stream and writes any buffered data to the logger.
     * <p>
     * Also closes the internal buffer.
     */
    @Override
    public void close() throws IOException {
        os.close();
        writeToLogger();
        super.close();
    }

    @Override
    public void flush() throws IOException {
        os.flush();
    }

    @Override
    public void write(final int b) {
        os.write(b);
    }

    @Override
    public void write(@NotNull final byte[] b) throws IOException {
        os.write(b);
    }

    @Override
    public void write(@NotNull final byte[] b, final int off, final int len) {
        os.write(b, off, len);
    }

    /**
     * Transfers any buffered data to the logger.
     * <p>
     * Subclasses must implement how the buffered data is logged.
     */
    protected abstract void writeToLogger();

    /**
     * Returns the buffered content as a string.
     *
     * @return the buffered data, converted to a string
     */
    protected String extractString() {
        return os.toString();
    }

    @Override
    public String toString() {
        return os.toString();
    }
}
