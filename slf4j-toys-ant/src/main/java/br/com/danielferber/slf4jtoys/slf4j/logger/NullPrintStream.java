/*
 * Copyright 2013 Daniel Felix Ferber
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package br.com.danielferber.slf4jtoys.slf4j.logger;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Locale;

/**
 * OutputStream that discards everything.
 *
 * @author Daniel Felix Ferber
 *
 */
class NullPrintStream extends PrintStream {

    NullPrintStream() {
        super(new NullOutputStream());
    }

    @Override
    public void write(byte[] b) throws IOException {
        // ignore
    }

    @Override
    public void write(int b) {
        // ignore
    }

    @Override
    public void write(byte[] buf, int off, int len) {
        // ignore
    }

    @Override
    public void print(Object obj) {
        // ignore
    }

    @Override
    public void print(String s) {
        // ignore
    }

    @Override
    public void print(boolean b) {
        // ignore
    }

    @Override
    public void print(char c) {
        // ignore
    }

    @Override
    public void print(char[] s) {
        // ignore
    }

    @Override
    public void print(double d) {
        // ignore
    }

    @Override
    public void print(float f) {
        // ignore
    }

    @Override
    public void print(int i) {
        // ignore
    }

    @Override
    public void print(long l) {
        // ignore
    }

    @Override
    public PrintStream printf(String format, Object... args) {
        // ignore
        return this;
    }

    @Override
    public PrintStream printf(Locale l, String format, Object... args) {
        // ignore
        return this;
    }

    @Override
    public void println() {
        // ignore
    }

    @Override
    public void println(Object x) {
        // ignore
    }

    @Override
    public void println(String x) {
        // ignore
    }

    @Override
    public void println(boolean x) {
        // ignore
    }

    @Override
    public void println(char x) {
        // ignore
    }

    @Override
    public void println(char[] x) {
        // ignore
    }

    @Override
    public void println(double x) {
        // ignore
    }

    @Override
    public void println(float x) {
        // ignore
    }

    @Override
    public void println(int x) {
        // ignore
    }

    @Override
    public void println(long x) {
        // ignore
    }

    @Override
    public PrintStream append(CharSequence csq) {
        // ignore
        return this;
    }

    @Override
    public PrintStream append(char c) {
        // ignore
        return this;
    }

    @Override
    public PrintStream append(CharSequence csq, int start, int end) {
        // ignore
        return this;
    }

    @Override
    public PrintStream format(String format, Object... args) {
        // ignore
        return this;
    }

    @Override
    public PrintStream format(Locale l, String format, Object... args) {
        // ignore
        return this;
    }
}
