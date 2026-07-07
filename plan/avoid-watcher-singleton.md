# Plano: Avoid Watcher Singleton — Controllers de Push com Builder

**Branch:** `feature/avoid-watcher-singleton`
**Fase:** 1 (push) — pull (servlet) fica para fase posterior

> **Nota de implementação:** o plano original previa o uso de Lombok `@Builder`.
> Na versão 1.18.46 não há `@Builder.Ignore` e não é possível definir defaults em
> parâmetros de construtor de forma compatível com Java 8. A implementação final
> usa um `Builder` escrito manualmente, ainda fluente e com defaults de
> `WatcherConfig`.

## Contexto

`WatcherSingleton` (TDR-0012) é dívida técnica assumida: fornece um `Watcher` default globalmente acessível e métodos `startDefaultWatcherExecutor`/`startDefaultWatcherTimer`/`stop...` para agendamento push. Os defeitos documentados:

- `.glm-findings/02` — `dataEnabled` em runtime sem efeito (instância cached uma vez).
- `.glm-findings/03` — race: scheduler thread e servlet thread chamam `run()` no mesmo `Watcher`.
- `.glm-findings/05` — `Timer` non-daemon bloqueia shutdown da JVM.
- TDR-0005 — "Static Initialization Limitation": config mudada após a criação do singleton não surte efeito.
- TDR-0008 — distingue push (agendado) de pull (sob demanda, via servlet).

## Objetivos

- Substituir os mecanismos de **push** do `WatcherSingleton` por **dois controllers instanciáveis**, um por tecnologia (`ScheduledExecutorService`, `Timer`).
- Cada controller **detém sua própria instância de `Watcher`**, criada no construtor a partir de `name`.
- Defaults vindos de `WatcherConfig` (`name`, `delayMilliseconds`, `periodMilliseconds`), overridáveis.
- Facilidade de uso: `create()` estáticos como atalho ao builder Lombok `@Builder`.
- `WatcherSingleton` reduzido ao **mínimo** necessário ao servlet (pull), `@Deprecated`, transitório.

## Não objetivos (desta fase)

- Migração do `WatcherServlet`/`WatcherJavaxServlet` (pull) — fase posterior.
- Interface comum `WatcherController` — decidido: duas classes soltas, cada uma `implements AutoCloseable`.
- Aceitar `Watcher` pré-construído no builder — decidido: só `name` (controller cria e detém o watcher).

## Classes novas em `src/main/java/org/usefultoys/slf4j/watcher/`

### `WatcherExecutorController`

```java
@lombok.Builder
public final class WatcherExecutorController implements AutoCloseable {
    private final Watcher watcher;
    private final long delayMilliseconds;
    private final long periodMilliseconds;
    private ScheduledExecutorService executor;
    private ScheduledFuture<?> task;

    @Builder
    public WatcherExecutorController(
            @Builder.Default String name = WatcherConfig.name,
            @Builder.Default long delayMilliseconds = WatcherConfig.delayMilliseconds,
            @Builder.Default long periodMilliseconds = WatcherConfig.periodMilliseconds) {
        this.watcher = new Watcher(name);
        this.delayMilliseconds = delayMilliseconds;
        this.periodMilliseconds = periodMilliseconds;
    }

    public static WatcherExecutorController create() {
        return builder().build();
    }

    public static WatcherExecutorController create(String name) {
        return builder().name(name).build();
    }

    public static WatcherExecutorController create(String name, long delay, long period) {
        return builder().name(name).delay(delay).period(period).build();
    }

    public synchronized void start() {
        if (executor == null) {
            executor = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, name);
                t.setDaemon(true);
                return t;
            });
        }
        if (task == null) {
            task = executor.scheduleAtFixedRate(
                    watcher, delayMilliseconds, periodMilliseconds, TimeUnit.MILLISECONDS);
        }
    }

    public synchronized void stop() {
        if (task != null) {
            task.cancel(true);
            task = null;
        }
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }

    public synchronized boolean isRunning() {
        return task != null;
    }

    @Override
    public void close() {
        stop();
    }
}
```

### `WatcherTimerController`

Mesmo shape, com `Timer` daemon nomeado com `name` (fix `.glm-findings/05`):

```java
@lombok.Builder
public final class WatcherTimerController implements AutoCloseable {
    private final Watcher watcher;
    private final long delayMilliseconds;
    private final long periodMilliseconds;
    private Timer timer;
    private TimerTask timerTask;

    @Builder
    public WatcherTimerController(
            @Builder.Default String name = WatcherConfig.name,
            @Builder.Default long delayMilliseconds = WatcherConfig.delayMilliseconds,
            @Builder.Default long periodMilliseconds = WatcherConfig.periodMilliseconds) {
        this.watcher = new Watcher(name);
        this.delayMilliseconds = delayMilliseconds;
        this.periodMilliseconds = periodMilliseconds;
    }

    public static WatcherTimerController create() {
        return builder().build();
    }

    public static WatcherTimerController create(String name) {
        return builder().name(name).build();
    }

    public static WatcherTimerController create(String name, long delay, long period) {
        return builder().name(name).delay(delay).period(period).build();
    }

    public synchronized void start() {
        if (timer == null) {
            timer = new Timer(name, true);
        }
        if (timerTask == null) {
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    watcher.run();
                }
            };
            timer.schedule(timerTask, delayMilliseconds, periodMilliseconds);
        }
    }

    public synchronized void stop() {
        if (timerTask != null) {
            timerTask = null;
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public synchronized boolean isRunning() {
        return timerTask != null;
    }

    @Override
    public void close() {
        stop();
    }
}
```

### Nome das threads

Timer e thread do executor recebem **exatamente `name`** (ex.: "watcher" no default, "myapp" se override). Alinha thread dump ↔ logger ↔ watcher. Diferente do original, que usava "Watcher" fixo (Timer) e "pool-N-thread-M" (executor).

## `WatcherSingleton` — máximo de remoção

```java
@UtilityClass
@Deprecated
public final class WatcherSingleton {
    private static Watcher DEFAULT_WATCHER_INSTANCE;

    @Deprecated
    public static synchronized Watcher getDefaultWatcher() {
        if (DEFAULT_WATCHER_INSTANCE == null) {
            DEFAULT_WATCHER_INSTANCE = new Watcher(WatcherConfig.name);
        }
        return DEFAULT_WATCHER_INSTANCE;
    }
}
```

- **Removidos:** `startDefaultWatcherExecutor`, `stopDefaultWatcherExecutor`, `startDefaultWatcherTimer`, `stopDefaultWatcherTimer` + campos `defaultWatcherExecutor`, `scheduledDefaultWatcher`, `defaultWatcherTimer`, `defaultWatcherTask`.
- **Mantido:** só `getDefaultWatcher()` (pull — `WatcherServlet.runWatcher()` e `WatcherJavaxServlet.runWatcher()` continuam chamando), `@Deprecated`, transitório até o servlet ser migrado.
- Javadoc reescrito: de "manages periodic execution" para "provides the default Watcher instance for pull-mode (servlet)".
- **Breaking para callers de push:** quem chamava `WatcherSingleton.startDefaultWatcherExecutor()` migra para `WatcherExecutorController.create().start()`; `startDefaultWatcherTimer()` → `WatcherTimerController.create().start()`.

## Semântica "late creation"

`@Builder.Default` lê `WatcherConfig` no momento da chamada a `builder()` (ou `create()`). A aplicação seta config antes, e nada é materializado no startup da biblioteca. O `Watcher` só nasce no `build()`/`create()`, a partir do `name` capturado.

## Testes (`src/test/java/org/usefultoys/slf4j/watcher/`)

- **Novos** `WatcherExecutorControllerTest`, `WatcherTimerControllerTest`: portar os casos de push do `WatcherSingletonTest` — idempotência start/stop, logging via Awaitility, instância própria de `Watcher` por controller, defaults do `WatcherConfig`, overrides via `create()` e builder, `close()`/try-with-resources, thread daemon + nome = `name`.
- `WatcherSingletonTest` **slimmed**: só `getDefaultWatcher()` (lazy cache, nome de `WatcherConfig`); casos de push removidos (migrados para os controllers).
- `WatcherServletTest`, `WatcherTest`, `WatcherConfigTest`: **intocados**.

## Documentação

- `README.md`: nova seção "Watcher" com `create()` + builder + try-with-resources.
- Novo TDR "Watcher Controller substitui push do WatcherSingleton" — revoga o lado push do TDR-0012.
- TDR-0005: marcar "Static Initialization Limitation" do push como resolvido; pull (servlet via `getDefaultWatcher()`) pendente.
- `.glm-findings/02`, `03`, `05`: marcar resolvidos no push.

## Pré-execução (trunk-based)

Criar branch/worktree `feature/avoid-watcher-singleton` a partir de `main` atualizada. Não editar em `main`.

## Verificação (pós-implementação)

```
mvnw.cmd -B -Pslf4j-2.0 test
mvnw.cmd -B -Pslf4j-2.0,with-logback test
mvnw.cmd -B -Pslf4j-1.7-javax test
```

Perfil `jdk-8` auto (compatibilidade Java 8). CI: Qodana/CodeQL.
