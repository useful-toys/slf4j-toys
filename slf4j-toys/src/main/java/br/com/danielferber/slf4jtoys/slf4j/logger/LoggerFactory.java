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

import java.io.OutputStream;
import java.io.PrintStream;
import org.slf4j.Logger;

/**
 * Alternativa ao {@link org.slf4j.LoggerFactory}, com métodos mais específicos
 * e práticos.
 *
 * @author Daniel Felix Ferber
 */
public class LoggerFactory {

    /**
     * Obtém o logger com hierarquia associada ao nome. Equivalente a
     * {@link org.slf4j.LoggerFactory#getLogger(String)}.
     * <p>
     * Usado tipicamente para declarar loggers especiais da aplicação, cujo nome
     * não segue a convenção de nome igual ao nome 'fully qualified' da classe.
     *
     * @param name Nome do logger, que é uma hierarquia separada por pontos.
     * @returns logger Instância do logger.
     */
    public static Logger getLogger(String name) {
        return org.slf4j.LoggerFactory.getLogger(name);
    }

    /**
     * Obtém o logger com hierarquia associada com uma determinada classe
     * através do nome 'fully qualified' da classe. Equivalente a
     * {@link org.slf4j.LoggerFactory#getLogger(Class)}.
     * <p>
     * Usado tipicamente para declarar o logger das atividades executadas por
     * uma classe.
     *
     * @param name Classe.
     * @returns Instância do logger.
     */
    public static Logger getLogger(Class<?> clazz) {
        return org.slf4j.LoggerFactory.getLogger(clazz);
    }

    /**
     * Obtém o logger com hierarquia abaixo da hierarquia associada com uma
     * determinada classe.
     * <p>
     * Usado tipicamente para declarar loggers específicos por atividade de uma
     * classe. Desta forma é possível controlar o log individualmente por
     * atividade. Para cada atividade de interesse é declarado um logger
     * específico dentro da hierarquia do logger da classe.
     *
     * @param name Nome da hierarquia abaixo da classe.
     * @param clazz Classe.
     * @returns Instância do logger.
     */
    public static Logger getLogger(Class<?> clazz, String name) {
        return org.slf4j.LoggerFactory.getLogger(clazz.getName() + '.' + name);
    }

    /**
     * Obtém o logger com hierarquia abaixo da hierarquia associada com um
     * logger existente.
     *
     * @param name Nome da hierarquia abaixo da classe.
     * @param logger Logger existente.
     * @returns Instância do logger.
     */
    public static Logger getLogger(Logger logger, String name) {
        return org.slf4j.LoggerFactory.getLogger(logger.getName() + '.' + name);
    }

    /**
     * Obtém um {@link PrintStream} cujo conteúdo será redicionado para um
     * logger, com prioridade de rastreamento.
     */
    public static PrintStream getTracePrintStream(final Logger logger) {
        if (!logger.isTraceEnabled()) {
            return new NullPrintStream();
        }
        return new PrintStream(LoggerFactory.getTraceOutputStream(logger));
    }

    /**
     * Obtém um {@link PrintStream} cujo conteúdo será redicionado para um
     * logger, com prioridade de depuração.
     */
    public static PrintStream getDebugPrintStream(final Logger logger) {
        if (!logger.isDebugEnabled()) {
            return new NullPrintStream();
        }
        return new PrintStream(LoggerFactory.getDebugOutputStream(logger));
    }

    /**
     * Obtém um {@link PrintStream} cujo conteúdo será redicionado para um
     * logger, com prioridade de informação.
     */
    public static PrintStream getInfoPrintStream(final Logger logger) {
        if (!logger.isInfoEnabled()) {
            return new NullPrintStream();
        }
        return new PrintStream(LoggerFactory.getInfoOutputStream(logger));
    }

    /**
     * Obtém um {@link PrintStream} cujo conteúdo será redicionado para um
     * logger, com prioridade de advertência.
     */
    public static PrintStream getWarnPrintStream(final Logger logger) {
        if (!logger.isWarnEnabled()) {
            return new NullPrintStream();
        }
        return new PrintStream(LoggerFactory.getWarnOutputStream(logger));
    }

    /**
     * Obtém um {@link PrintStream} cujo conteúdo será redicionado para um
     * logger, com prioridade de erro.
     */
    public static PrintStream getErrorPrintStream(final Logger logger) {
        if (!logger.isErrorEnabled()) {
            return new NullPrintStream();
        }
        return new PrintStream(LoggerFactory.getErrorOutputStream(logger));
    }

    /**
     * Obtém um {@link PrintStream} cujo conteúdo será redicionado para um
     * logger, com prioridade de rastreamento.
     */
    public static OutputStream getTraceOutputStream(final Logger logger) {
        if (!logger.isTraceEnabled()) {
            return new NullOutputStream();
        }
        return new LoggerOutputStream(logger) {
            @Override
            protected void writeToLogger() {
                logger.trace(extractString());
            }
        };
    }

    /**
     * Obtém um {@link PrintStream} cujo conteúdo será redicionado para um
     * logger, com prioridade de depuração.
     */
    public static OutputStream getDebugOutputStream(final Logger logger) {
        if (!logger.isDebugEnabled()) {
            return new NullOutputStream();
        }
        return new LoggerOutputStream(logger) {
            @Override
            protected void writeToLogger() {
                logger.debug(extractString());
            }
        };
    }

    /**
     * Obtém um {@link PrintStream} cujo conteúdo será redicionado para um
     * logger, com prioridade de informação.
     */
    public static OutputStream getInfoOutputStream(final Logger logger) {
        if (!logger.isInfoEnabled()) {
            return new NullOutputStream();
        }
        return new LoggerOutputStream(logger) {
            @Override
            protected void writeToLogger() {
                logger.info(extractString());
            }
        };
    }

    /**
     * Obtém um {@link PrintStream} cujo conteúdo será redicionado para um
     * logger, com prioridade de advertência.
     */
    public static OutputStream getWarnOutputStream(final Logger logger) {
        if (!logger.isWarnEnabled()) {
            return new NullOutputStream();
        }
        return new LoggerOutputStream(logger) {
            @Override
            protected void writeToLogger() {
                logger.warn(extractString());
            }
        };
    }

    /**
     * Obtém um {@link PrintStream} cujo conteúdo será redicionado para um
     * logger, com prioridade de erro.
     */
    public static OutputStream getErrorOutputStream(final Logger logger) {
        if (!logger.isErrorEnabled()) {
            return new NullOutputStream();
        }
        return new LoggerOutputStream(logger) {
            @Override
            protected void writeToLogger() {
                logger.error(extractString());
            }
        };
    }
}
