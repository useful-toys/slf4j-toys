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

import java.io.PrintStream;
import java.util.Locale;

/**
 * A {@link PrintStream} implementation that silently discards all output.
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
class NullPrintStream extends PrintStream {

    NullPrintStream() {
        // prevent instances outside this library
        super(new NullOutputStream());
    }

    @Override
    public void write(@NotNull final byte[] b) {
        // ignore
    }

    @Override
    public void write(final int b) {
        // ignore
    }

    @Override
    public void write(@NotNull final byte[] buf, final int off, final int len) {
        // ignore
    }

    @Override
    public void print(final Object obj) {
        // ignore
    }

    @Override
    public void print(final String s) {
        // ignore
    }

    @Override
    public void print(final boolean b) {
        // ignore
    }

    @Override
    public void print(final char c) {
        // ignore
    }

    @Override
    public void print(@NotNull final char[] s) {
        // ignore
    }

    @Override
    public void print(final double d) {
        // ignore
    }

    @Override
    public void print(final float f) {
        // ignore
    }

    @Override
    public void print(final int i) {
        // ignore
    }

    @Override
    public void print(final long l) {
        // ignore
    }

    @Override
    public PrintStream printf(@NotNull final String format, final Object... args) {
        // ignore
        return this;
    }

    @Override
    public PrintStream printf(final Locale l, @NotNull final String format, final Object... args) {
        // ignore
        return this;
    }

    @Override
    public void println() {
        // ignore
    }

    @Override
    public void println(final Object x) {
        // ignore
    }

    @Override
    public void println(final String x) {
        // ignore
    }

    @Override
    public void println(final boolean x) {
        // ignore
    }

    @Override
    public void println(final char x) {
        // ignore
    }

    @Override
    public void println(@NotNull final char[] x) {
        // ignore
    }

    @Override
    public void println(final double x) {
        // ignore
    }

    @Override
    public void println(final float x) {
        // ignore
    }

    @Override
    public void println(final int x) {
        // ignore
    }

    @Override
    public void println(final long x) {
        // ignore
    }

    @Override
    public PrintStream append(final CharSequence csq) {
        // ignore
        return this;
    }

    @Override
    public PrintStream append(final char c) {
        // ignore
        return this;
    }

    @Override
    public PrintStream append(final CharSequence csq, final int start, final int end) {
        // ignore
        return this;
    }

    @Override
    public PrintStream format(@NotNull final String format, final Object... args) {
        // ignore
        return this;
    }

    @Override
    public PrintStream format(final Locale l, @NotNull final String format, final Object... args) {
        // ignore
        return this;
    }
}
