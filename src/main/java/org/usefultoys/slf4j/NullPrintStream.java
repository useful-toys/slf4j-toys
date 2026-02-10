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
    public void write(final byte[] b) {
        /* Discard output to avoid unnecessary I/O and formatting overhead */
    }

    @Override
    public void write(final int b) {
        /* Discard output to avoid unnecessary I/O and formatting overhead */
    }

    @Override
    public void write(final byte[] buf, final int off, final int len) {
        /* Discard output to avoid unnecessary I/O and formatting overhead */
    }

    @Override
    public void print(final Object obj) {
        /* Discard output to avoid unnecessary I/O and formatting overhead */
    }

    @Override
    public void print(final String s) {
        /* Discard output to avoid unnecessary I/O and formatting overhead */
    }

    @Override
    public void print(final boolean b) {
        /* Discard output to avoid unnecessary I/O and formatting overhead */
    }

    @Override
    public void print(final char c) {
        /* Discard output to avoid unnecessary I/O and formatting overhead */
    }

    @Override
    public void print(final char[] s) {
        /* Discard output to avoid unnecessary I/O and formatting overhead */
    }

    @Override
    public void print(final double d) {
        /* Discard output to avoid unnecessary I/O and formatting overhead */
    }

    @Override
    public void print(final float f) {
        /* Discard output to avoid unnecessary I/O and formatting overhead */
    }

    @Override
    public void print(final int i) {
        /* Discard output to avoid unnecessary I/O and formatting overhead */
    }

    @Override
    public void print(final long l) {
        /* Discard output to avoid unnecessary I/O and formatting overhead */
    }

    @Override
    public PrintStream printf(final String format, final Object... args) {
        /* Discard output to avoid unnecessary I/O and formatting overhead */
        return this;
    }

    @Override
    public PrintStream printf(final Locale l, final String format, final Object... args) {
        /* Discard output to avoid unnecessary I/O and formatting overhead */
        return this;
    }

    @Override
    public void println() {
        /* Discard output to avoid unnecessary I/O and formatting overhead */
    }

    @Override
    public void println(final Object x) {
        /* Discard output to avoid unnecessary I/O and formatting overhead */
    }

    @Override
    public void println(final String x) {
        /* Discard output to avoid unnecessary I/O and formatting overhead */
    }

    @Override
    public void println(final boolean x) {
        /* Discard output to avoid unnecessary I/O and formatting overhead */
    }

    @Override
    public void println(final char x) {
        /* Discard output to avoid unnecessary I/O and formatting overhead */
    }

    @Override
    public void println(final char[] x) {
        /* Discard output to avoid unnecessary I/O and formatting overhead */
    }

    @Override
    public void println(final double x) {
        /* Discard output to avoid unnecessary I/O and formatting overhead */
    }

    @Override
    public void println(final float x) {
        /* Discard output to avoid unnecessary I/O and formatting overhead */
    }

    @Override
    public void println(final int x) {
        /* Discard output to avoid unnecessary I/O and formatting overhead */
    }

    @Override
    public void println(final long x) {
        /* Discard output to avoid unnecessary I/O and formatting overhead */
    }

    @Override
    public PrintStream append(final CharSequence csq) {
        /* Discard output to avoid unnecessary I/O and formatting overhead */
        return this;
    }

    @Override
    public PrintStream append(final char c) {
        /* Discard output to avoid unnecessary I/O and formatting overhead */
        return this;
    }

    @Override
    public PrintStream append(final CharSequence csq, final int start, final int end) {
        /* Discard output to avoid unnecessary I/O and formatting overhead */
        return this;
    }

    @Override
    public PrintStream format(final String format, final Object... args) {
        /* Discard output to avoid unnecessary I/O and formatting overhead */
        return this;
    }

    @Override
    public PrintStream format(final Locale l, final String format, final Object... args) {
        /* Discard output to avoid unnecessary I/O and formatting overhead */
        return this;
    }
}
