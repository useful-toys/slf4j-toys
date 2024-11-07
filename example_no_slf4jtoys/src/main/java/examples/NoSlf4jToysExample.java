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
package examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.util.Locale;

/**
 * Test SLF4J Meter using JUL as underlying framework.
 * This examample demonstrates recommended logging setting for JUL.
 *
 * @author Daniel Felix Ferber
 */
public class NoSlf4jToysExample {

    public static final Logger logger = LoggerFactory.getLogger(NoSlf4jToysExample.class);

    static {
        Locale.setDefault(Locale.ENGLISH);
    }

    public static void main(final String argv[])  {
        logger.error("error message");
        logger.warn("warn message");
        logger.info("info message");
        logger.debug("debug message");
        logger.trace("trace message");
        registrarOpcaoDoUsuario("ferber", 2);
        runOperation2("ferber", "Daniel", "dff4321@gmail.com");
        runOperation3("ferber", "documentos/receita.doc");
    }

    private static void registrarOpcaoDoUsuario(String usuario, int opcao) {
        try  {
            logger.info("Registrar opção do usuário");
            Thread.sleep(1000);

            logger.info("Consulta concluída");
            Thread.sleep(1000);
            logger.debug("Usuário {} pediu opção {}", usuario, opcao);
            logger.info("Transação concluída");
            Thread.sleep(1000);
            logger.debug("A solicitação foi persistida com ID {}", "ABC123");
            logger.info("Terminou de registrar opção do usuário");
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    private static void runOperation2(String usuario, String nome, String email) {
        try {
            logger.info("Operação 2 iniciada.");
            logger.info("Iniciando parte 1");
            Thread.sleep(1000);
            logger.debug("Usando usuario {}, que se chama {}", usuario, nome);
            logger.info("Iniciando parte 2");
            Thread.sleep(1000);
            logger.debug("Usando email {}", email);
            logger.info("Tentativa de enviar email");
            Thread.sleep(1000);
            logger.info("Operação 2 não foi possivel.", new IllegalAccessException());
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    private static void runOperation3(String usuario, String arquivo) {
        try {
            logger.info("Operação 3. O usuário é {} e o arquivo é {}", usuario, arquivo);
            Thread.sleep(1000);
            logger.info("1/3");
            Thread.sleep(1000);
            logger.info("2/3");
            Thread.sleep(1000);
            logger.error("Erro na operação 3", new FileNotFoundException());
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
}
