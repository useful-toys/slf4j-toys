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

import java.io.IOException;
import java.io.PrintStream;
import java.util.Locale;

/**
 * An {@link PrintStream} that discards everything.
 * Used instead of {@link LoggerOutputStream} as an optimization when logging level would prevent output anyway.

 * @author Daniel Felix Ferber
 *
 */
class NullPrintStream extends PrintStream {

    NullPrintStream() {
        // prevent instances outside this library
        super(new NullOutputStream());
    }

    @Override
    public void write(final byte[] b) throws IOException {
        // ignore
    }

    @Override
    public void write(final int b) {
        // ignore
    }

    @Override
    public void write(final byte[] buf, final int off, final int len) {
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
    public void print(final char[] s) {
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
    public PrintStream printf(final String format, final Object... args) {
        // ignore
        return this;
    }

    @Override
    public PrintStream printf(final Locale l, final String format, final Object... args) {
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
    public void println(final char[] x) {
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
    public PrintStream format(final String format, final Object... args) {
        // ignore
        return this;
    }

    @Override
    public PrintStream format(final Locale l, final String format, final Object... args) {
        // ignore
        return this;
    }
}
