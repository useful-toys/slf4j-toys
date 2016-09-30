/*
 * Copyright 2015 Daniel Felix Ferber.
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
import java.io.OutputStream;

/**
 * An {@link OutputStream} that discards everything.
 *
 * @author Daniel Felix Ferber
 *
 */
class NullOutputStream extends OutputStream {

    NullOutputStream() {
        // prevent instances outside this library
    }

    @Override
    public void write(final int b) throws IOException {
        // ignore
    }

    @Override
    public void write(final byte[] b) throws IOException {
        // ignore
    }

    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        // ignore
    }
}
