/*
 * Copyright 2024 Daniel Felix Ferber
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

import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;

/**
 * A {@link OutputStream} implementation that silently discards all output.
 * <p>
 * Used as a performance optimization when log output is unnecessary (e.g., when the logging level disables output).
 * This avoids the overhead of formatting and I/O operations.
 * <p>
 * Replaces {@link LoggerOutputStream} in scenarios where output would be suppressed anyway.
 * <p>
 * This class is package-private and not intended for use outside this library.
 *
 * @author Daniel Felix Ferber
 */
class NullOutputStream extends OutputStream {

    NullOutputStream() {
        // prevent instances outside this library
    }

    @Override
    public void write(final int b) {
        // ignore
    }

    @Override
    public void write(@NotNull final byte[] b) {
        // ignore
    }

    @Override
    public void write(@NotNull final byte[] b, final int off, final int len) {
        // ignore
    }
}
