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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import org.slf4j.Logger;

/**
 * Outputstream que escreve seu conteúdo para um logger. O conteúdo é escrito no
 * logger ao chamar os métodos {@link #close()} ou {@link #flush()}. Para obter
 * instâncias deste stream, utilize um dos métodos  {@link #getDebugOutputStream(Logger)}, {@link #getErrorOutputStream(Logger)}, {@link #getInfoOutputStream(Logger)},
 * {@link #getTraceOutputStream(Logger)} ou {@link #getWarnOutputStream(Logger)}
 * pois não existe construtor público.
 * <p>
 * Deve ser utilizado apenas para escrever conteúdo com tamanho moderado, pois
 * todo conteúdo permanece temporariamente em buffer.
 * <p>
 * Para escrever mensagens formatadas, pode-se encapsular este
 * {@link OutputStream} como {@link PrintStream} ou {@link PrintWriter}.
 * <p>
 * <b>Detalhe de implementação:</b> Não é possível criar instâncias desta
 * classe, pois
 *
 * @author Daniel Felix Ferber
 */
public abstract class LoggerOutputStream extends OutputStream {

    /**
     * Logger para onde será escrito o conteúdo.
     */
    private final Logger logger;
    /**
     * Buffer que acumula temporariamente o conteúdo.
     */
    private final ByteArrayOutputStream os = new ByteArrayOutputStream(0x3FFF);

    /**
     * Construtor padrão. As instâncias de fato devem implementar
     * {@link #writeToLogger()} de acordo com a prioridade do logger.
     * Infelizmente, o {@link Logger} não permite representar a prioridade de um
     * logger, de forma que é necessário criar uma instância específica para a
     * prioridade desejada.
     *
     * @param logger Logger que receberá o conteúdo.
     */
    protected LoggerOutputStream(final Logger logger) {
        super();
        this.logger = logger;
    }

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
     * Escreve o conteúdo para o logger de acordo com a prioridade desejada.
     */
    protected abstract void writeToLogger();

    /**
     * @return O conteúdo acumulado pelo OutputStream.
     */
    protected String extractString() {
        return os.toString();
    }

    @Override
    public String toString() {
        return os.toString();
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object obj) {
        throw new UnsupportedOperationException();
    }
}
