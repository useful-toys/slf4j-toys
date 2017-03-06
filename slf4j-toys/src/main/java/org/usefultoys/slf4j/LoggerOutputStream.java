/*
 * Copyright 2017 Daniel Felix Ferber
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.usefultoys.slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * An {@link OutputStream} that redirects the written data to a logger whenever
 * {@link #close()} or {@link #flush()} is called.
 * <p>
 * Instance of this class are obtained by calling factory methods from
 * {@link LoggerFactory}.
 * <p>
 * Intended for moderated amount of data, that is buffered before being
 * redirected to the logger.
 *
 * @author Daniel Felix Ferber
 */
abstract class LoggerOutputStream extends OutputStream {

    LoggerOutputStream() {
        // prevent instances outside this library
    }

    /**
     * Buffer that buffers data until it is redirected to the logger.
     */
    private final ByteArrayOutputStream os = new ByteArrayOutputStream(0x3FFF);

    /**
     * Closes this output stream and writes any buffered output bytes as a message to the logger.
     */
    @Override
    public void close() throws IOException {
        os.close();
        writeToLogger();
        super.close();
    }

    /**
     * Flushes this output stream and forces any buffered output bytes to be written as a message to the logger.
     */
    @Override
    public void flush() throws IOException {
        os.flush();
    }

    @Override
    public void write(final int b) throws IOException {
        os.write(b);
    }

    @Override
    public void write(final byte[] b) throws IOException {
        os.write(b);
    }

    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        os.write(b, off, len);
    }

    /**
     * Writes any buffered output bytes as a message to the logger.
     */
    protected abstract void writeToLogger();

    /**
     * Converts any buffered output bytes to string.
     *
     * @return string representing any buffered output bytes
     */
    protected String extractString() {
        return os.toString();
    }

    @Override
    public String toString() {
        return os.toString();
    }
}
