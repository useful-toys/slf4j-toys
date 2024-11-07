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
import org.usefultoys.slf4j.LoggerFactory;
import org.usefultoys.slf4j.SessionConfig;
import org.usefultoys.slf4j.SystemConfig;
import org.usefultoys.slf4j.meter.Meter;
import org.usefultoys.slf4j.meter.MeterConfig;
import org.usefultoys.slf4j.meter.MeterFactory;
import org.usefultoys.slf4j.watcher.WatcherConfig;
import org.usefultoys.slf4j.watcher.WatcherSingleton;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;

/**
 * Test SLF4J Meter using JUL as underlying framework. This examample demonstrates recommended logging setting for JUL.
 *
 * @author Daniel Felix Ferber
 */
public class LogbackExample {

    public static final Logger logger = LoggerFactory.getLogger("example");

    static {
        Locale.setDefault(Locale.ENGLISH);
        SessionConfig.uuidSize = 0;
        WatcherConfig.delayMilliseconds = 1000;
        WatcherConfig.periodMilliseconds = 2000;
        SystemConfig.useClassLoadingManagedBean = true;
        SystemConfig.useCompilationManagedBean = true;
        SystemConfig.useGarbageCollectionManagedBean = true;
        SystemConfig.useMemoryManagedBean = true;
        SystemConfig.usePlatformManagedBean = true;
        MeterConfig.progressPeriodMilliseconds = 2000;
        MeterConfig.printCategory = false;
        MeterConfig.printStatus = false;
        MeterConfig.printPosition = false;
        MeterConfig.printMemory = false;
        MeterConfig.printLoad = false;
    }

    public static void main(final String argv[]) throws IOException, InterruptedException {
        WatcherSingleton.startDefaultWatcherExecutor();
        logger.error("error message");
        logger.warn("warn message");
        logger.info("info message");
        logger.debug("debug message");
        logger.trace("trace message");
        registrarOpcaoDoUsuario("ferber", 2, false);
        registrarOpcaoDoUsuario("ferber", 2, true);
        enviarEmail("ferber", "Daniel", "dff4321@gmail.com");
        gravarArquivo("ferber", "documentos/receita.doc");
        WatcherSingleton.stopDefaultWatcherExecutor();
    }

    private static void registrarOpcaoDoUsuario(String usuario, int opcao, boolean slow) throws InterruptedException {
        Meter m = MeterFactory.getMeter(logger, "registrarOpcaoDoUsuario").iterations(3).limitMilliseconds(4000)
                .ctx("usuario", usuario).ctx("opcao", opcao).start();
        Thread.sleep(800);
        Thread.sleep(800);
        Thread.sleep(800);
        if (slow) Thread.sleep(3000);
        m.ctx("id", "ABC123").ok("alterar-opcao");
    }

    private static void enviarEmail(String usuario, String nome, String email) throws InterruptedException {
        Meter m = MeterFactory.getMeter(logger, "enviarEmail")
                .ctx("usuario", usuario)
                .ctx("nome", nome)
                .ctx("email", email)
                .start();
        Thread.sleep(3000);
        m.reject("usuario-inexistente");
    }

    private static void gravarArquivo(String usuario, String arquivo) throws InterruptedException {
        Meter m = MeterFactory.getMeter(logger, "gravarArquivo")
                .ctx("usuario", usuario).ctx("arquivo", arquivo).start();
        Thread.sleep(3000);
        m.fail(new FileNotFoundException());
    }
}
