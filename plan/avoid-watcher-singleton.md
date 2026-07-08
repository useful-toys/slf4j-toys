# Plano: Avoid Watcher Singleton — Controllers de Push + Servlet Pull

**Branch:** `feature/avoid-watcher-singleton` (fase 1, push) e `feat/watcher-servlet-own-instance` (fase 2, pull)
**Status:** Concluído

> **Nota de implementação:** não usa Lombok `@Builder` (proibido pelo guia de
> estilo). Cada controller expõe apenas métodos factory estáticos `create(...)`
> que leem os defaults de `WatcherConfig` no momento da chamada.

## Contexto

`WatcherSingleton` (TDR-0012) era dívida técnica assumida: fornecia um `Watcher` default globalmente acessível e métodos `startDefaultWatcherExecutor`/`startDefaultWatcherTimer`/`stop...` para agendamento push. Os defeitos documentados:

- `.glm-findings/02` — `dataEnabled` em runtime sem efeito (instância cached uma vez).
- `.glm-findings/03` — race: scheduler thread e servlet thread chamavam `run()` no mesmo `Watcher`.
- `.glm-findings/05` — `Timer` non-daemon bloqueia shutdown da JVM.
- TDR-0005 — "Static Initialization Limitation": config mudada após a criação do singleton não surte efeito.
- TDR-0008 — distingue push (agendado) de pull (sob demanda, via servlet).

## Objetivos

### Fase 1 (push) — concluída em `feature/avoid-watcher-singleton`

- Substituir os mecanismos de **push** do `WatcherSingleton` por **dois controllers instanciáveis**, um por tecnologia (`ScheduledExecutorService`, `Timer`).
- Cada controller **detém sua própria instância de `Watcher`**, criada no construtor a partir de `name`.
- Defaults vindos de `WatcherConfig` (`name`, `delayMilliseconds`, `periodMilliseconds`), overridáveis.
- Facilidade de uso: métodos factory estáticos `create()`.
- `WatcherSingleton` reduzido ao **mínimo** necessário ao servlet (pull), `@Deprecated`, transitório.

### Fase 2 (pull) — concluída em `feat/watcher-servlet-own-instance`

- Remover `WatcherSingleton` completamente.
- `WatcherServlet` e `WatcherJavaxServlet` passam a criar sua própria instância de `Watcher` em `init(ServletConfig)`.
- Permitir override do nome do watcher via init-param `slf4jtoys.watcher.name` no `web.xml`; por padrão usa `WatcherConfig.name`.
- Guardar chamadas concorrentes a `runWatcher()` com um `ReentrantLock.tryLock()` privado por servlet: se outra coleta estiver em andamento, a requisição simultânea recebe HTTP 429 e desiste, em vez de enfileirar. Isso evita a race do caminho pull sem amarrar threads do container.

## Classes novas em `src/main/java/org/usefultoys/slf4j/watcher/`

### `WatcherExecutorController`

```java
public final class WatcherExecutorController implements AutoCloseable {
    private final String name;
    private final long delayMilliseconds;
    private final long periodMilliseconds;
    private final Watcher watcher;

    private ScheduledExecutorService executor;
    private ScheduledFuture<?> task;

    private WatcherExecutorController(
            final String name,
            final long delayMilliseconds,
            final long periodMilliseconds) {
        this.name = name;
        this.delayMilliseconds = delayMilliseconds;
        this.periodMilliseconds = periodMilliseconds;
        this.watcher = new Watcher(name);
    }

    public static WatcherExecutorController create() {
        return new WatcherExecutorController(
                WatcherConfig.name,
                WatcherConfig.delayMilliseconds,
                WatcherConfig.periodMilliseconds);
    }

    public static WatcherExecutorController create(final String name) {
        return new WatcherExecutorController(
                name,
                WatcherConfig.delayMilliseconds,
                WatcherConfig.periodMilliseconds);
    }

    public static WatcherExecutorController create(
            final String name,
            final long delayMilliseconds,
            final long periodMilliseconds) {
        return new WatcherExecutorController(name, delayMilliseconds, periodMilliseconds);
    }

    public synchronized void start() { /* daemon executor, thread name = name */ }
    public synchronized void stop() {}
    public synchronized boolean isRunning() { return task != null; }
    @Override public void close() { stop(); }
}
```

### `WatcherTimerController`

Mesmo shape, com `Timer` daemon nomeado com `name` (fix `.glm-findings/05`).

### Nome das threads

Timer e thread do executor recebem **exatamente `name`** (ex.: "watcher" no default, "myapp" se override). Alinha thread dump ↔ logger ↔ watcher. Diferente do original, que usava "Watcher" fixo (Timer) e "pool-N-thread-M" (executor).

## `WatcherSingleton` — remoção completa

- **Removidos:** `WatcherSingleton.java` e `WatcherSingletonTest.java`.
- **Breaking:** callers de push migram para `WatcherExecutorController.create().start()` / `WatcherTimerController.create().start()`. Callers de `getDefaultWatcher()` migram para `new Watcher(WatcherConfig.name)` ou usam `WatcherServlet`/`WatcherJavaxServlet`.

## Semântica "late creation"

Controllers: `create()` lê `WatcherConfig` no momento da chamada. A aplicação seta config antes, e nada é materializado no startup da biblioteca. O `Watcher` só nasce no `create()`, a partir do `name` capturado.

Servlets: o `Watcher` é criado em `init(ServletConfig)`, capturando `WatcherConfig.name` e as configurações de logger no momento da inicialização do servlet. O init-param `slf4jtoys.watcher.name` pode sobrescrever o nome.

## Testes (`src/test/java/org/usefultoys/slf4j/watcher/`)

- **Novos** `WatcherExecutorControllerTest`, `WatcherTimerControllerTest` (fase 1): idempotência start/stop, logging via Awaitility, instância própria de `Watcher` por controller, defaults do `WatcherConfig`, overrides via `create()`, `close()`/try-with-resources, thread daemon + nome = `name`.
- `WatcherServletTest`/`WatcherJavaxServletTest` atualizados (fase 2): chamam `init(ServletConfig)` antes de `doGet`; novos testes cobrem init-param, fallback para `WatcherConfig.name`, e valor em branco.
- `WatcherSingletonTest` removido junto com a classe.

## Documentação

- `README.md`: seção "Watcher" com controllers e exemplo de `web.xml` com init-param.
- TDR-0036: descreve a fase 1 (push).
- TDR-0037: descreve a fase 2 (pull / remoção do singleton).
- TDR-0012, TDR-0005, TDR-0008: atualizados para refletir a remoção completa do singleton.
- Wiki: atualizar `Watcher-use-case-servlet.md` e remover/apagar `Watcher-use-case-singleton.md`.

## Pré-execução (trunk-based)

Criar branch/worktree a partir de `main` atualizada. Não editar em `main`.

## Verificação (pós-implementação)

```
mvnw.cmd -B -Pslf4j-2.0 test
mvnw.cmd -B -Pslf4j-2.0,with-logback test
mvnw.cmd -B -Pslf4j-1.7-javax test
```

Perfil `jdk-8` auto (compatibilidade Java 8). CI: Qodana/CodeQL.
