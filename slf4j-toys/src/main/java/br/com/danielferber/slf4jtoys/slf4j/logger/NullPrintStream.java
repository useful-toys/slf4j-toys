/*
 * Copyright 2012 Daniel Felix Ferber
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
 * Outputstream que descarta todo conteúdo.
 * <p>
 * É utilizado para otimizar {@link LoggerOutputStream} quando a prioridade do
 * logger encapsulado não permite escrever o conteúdo no logger.
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
        // ignorar
    }

    @Override
    public void write(int b) {
        // ignorar
    }

    @Override
    public void write(byte[] buf, int off, int len) {
        // ignorar
    }

    @Override
    public void print(Object obj) {
        // ignorar
    }

    @Override
    public void print(String s) {
        // ignorar
    }

    @Override
    public void print(boolean b) {
        // ignorar
    }

    @Override
    public void print(char c) {
        // ignorar
    }

    @Override
    public void print(char[] s) {
        // ignorar
    }

    @Override
    public void print(double d) {
        // ignorar
    }

    @Override
    public void print(float f) {
        // ignorar
    }

    @Override
    public void print(int i) {
        // ignorar
    }

    @Override
    public void print(long l) {
        // ignorar
    }

    @Override
    public PrintStream printf(String format, Object... args) {
        // ignorar
        return this;
    }

    @Override
    public PrintStream printf(Locale l, String format, Object... args) {
        // ignorar
        return this;
    }

    @Override
    public void println() {
        // ignorar
    }

    @Override
    public void println(Object x) {
        // ignorar
    }

    @Override
    public void println(String x) {
        // ignorar
    }

    @Override
    public void println(boolean x) {
        // ignorar
    }

    @Override
    public void println(char x) {
        // ignorar
    }

    @Override
    public void println(char[] x) {
        // ignorar
    }

    @Override
    public void println(double x) {
        // ignorar
    }

    @Override
    public void println(float x) {
        // ignorar
    }

    @Override
    public void println(int x) {
        // ignorar
    }

    @Override
    public void println(long x) {
        // ignorar
    }

    @Override
    public PrintStream append(CharSequence csq) {
        // ignorar
        return this;
    }

    @Override
    public PrintStream append(char c) {
        // ignorar
        return this;
    }

    @Override
    public PrintStream append(CharSequence csq, int start, int end) {
        // ignorar
        return this;
    }

    @Override
    public PrintStream format(String format, Object... args) {
        // ignorar
        return this;
    }

    @Override
    public PrintStream format(Locale l, String format, Object... args) {
        // ignorar
        return this;
    }
}
