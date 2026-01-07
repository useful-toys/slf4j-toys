# Diagrama de Classes - Meter

Este diagrama apresenta a estrutura da classe `Meter` e suas principais relações no projeto `slf4j-toys`.

```mermaid
classDiagram
    direction TB
    class Serializable {
        <<interface>>
    }

    class EventData {
        #String sessionUuid
        #long position
        #long lastCurrentTime
        #reset() void
        #writeJson5(StringBuilder sb) void
        +readJson5(String json5) void
    }

    class SystemData {
        #long heap_commited
        #long heap_max
        #long heap_used
        #long nonHeap_commited
        #long nonHeap_max
        #long nonHeap_used
        #long objectPendingFinalizationCount
        #long classLoading_loaded
        #long classLoading_total
        #long classLoading_unloaded
        #long compilationTime
        #long garbageCollector_count
        #long garbageCollector_time
        #long runtime_usedMemory
        #long runtime_maxMemory
        #long runtime_totalMemory
        #double systemLoad
        +reset() void
        #writeJson5(StringBuilder sb) void
        +readJson5(String json5) void
    }

    class MeterAnalysis {
        <<interface>>
        +getLastCurrentTime() long
        +getCategory() String
        +getOperation() String
        +getParent() String
        +getCreateTime() long
        +getStartTime() long
        +getStopTime() long
        +getTimeLimit() long
        +getCurrentIteration() long
        +getOkPath() String
        +getRejectPath() String
        +getFailPath() String
        +isStarted() boolean
        +isStopped() boolean
        +isOK() boolean
        +isReject() boolean
        +isFail() boolean
        +getPath() String
    }

    class Closeable {
        <<interface>>
        +close() void
    }

    class MeterContext~T~ {
        <<interface>>
        +putContext(String name, Object value) void
        +putContext(String name) void
        +removeContext(String name) void
        +clearContext() void
        +getFullID() String
        +ctx(String name) T
        +ctx(boolean condition, String trueName) T
        +ctx(boolean condition, String trueName, String falseName) T
        +ctx(String name, int value) T
        +ctx(String name, long value) T
        +ctx(String name, boolean value) T
        +ctx(String name, float value) T
        +ctx(String name, double value) T
        +ctx(String name, Integer value) T
        +ctx(String name, Long value) T
        +ctx(String name, Boolean value) T
        +ctx(String name, Float value) T
        +ctx(String name, Double value) T
        +ctx(String name, String value) T
        +ctx(String name, String format, Object... args) T
        +ctx(String name, Object value) T
        +unctx(String name) T
    }

    class MeterExecutor~T~ {
        <<interface>>
        +start() T
        +ok() T
        +fail(Object cause) T
        +reject(Object cause) T
        +putContext(String key, Object value) void
        +getMessageLogger() Logger
        +getDataLogger() Logger
        +run(Runnable runnable) void
        +runOrReject(Runnable runnable, Class... exceptionsToReject) void
        +call(Callable callable) T
        +callOrRejectChecked(Callable callable) T
        +callOrReject(Callable callable, Class... exceptionsToReject) T
        +safeCall(Callable callable) T
        +safeCall(Class exceptionClass, Callable callable) T
        +convertException(Class exceptionClass, Exception e) RuntimeException
    }

    class MeterData {
        #String category
        #String operation
        #String parent
        #String description
        #long createTime
        #long startTime
        #long stopTime
        #long timeLimit
        #long currentIteration
        #long expectedIterations
        #String okPath
        #String rejectPath
        #String failPath
        #String failMessage
        #Map~String, String~ context
    }

    class Meter {
        -Logger messageLogger
        -Logger dataLogger
        -long lastProgressTime
        -long lastProgressIteration
        -WeakReference~Meter~ previousInstance
        +static ConcurrentMap~String, AtomicLong~ EVENT_COUNTER
        -static ThreadLocal~WeakReference~Meter~~ localThreadInstance
        +Meter(Logger logger)
        +Meter(Logger logger, String operation)
        +Meter(Logger logger, String operation, String parent)
        +static Meter getCurrentInstance()
        +Meter sub(String suboperationName)
        +Meter m(String message)
        +Meter m(String format, Object... args)
        +Meter limitMilliseconds(long timeLimit)
        +Meter iterations(long expectedIterations)
        +Meter start()
        +Meter inc()
        +Meter incBy(long increment)
        +Meter incTo(long currentIteration)
        +Meter progress()
        +Meter path(Object pathId)
        +Meter ok()
        +Meter ok(Object pathId)
        +Meter success()
        +Meter success(Object pathId)
        +Meter reject(Object cause)
        +Meter fail(Object cause)
        +close() void
        #finalize() void
        ~checkCurrentInstance() boolean
    }

    Serializable <|.. EventData
    EventData <|-- SystemData
    SystemData <|-- MeterData
    MeterAnalysis <|.. MeterData
    MeterAnalysis <|-- MeterExecutor
    MeterData <|-- Meter
    MeterContext <|.. Meter
    MeterExecutor <|.. Meter
    Closeable <|.. Meter
    
    Meter --> Meter : previousInstance
    Meter --> Logger : messageLogger
    Meter --> Logger : dataLogger
```

## Descrição das Relações

*   **Meter**: A classe principal que gerencia o ciclo de vida de uma operação.
*   **MeterData**: Contém os dados coletados pela operação (timestamps, iterações, caminhos de sucesso/falha).
*   **SystemData**: Extensão de `EventData` que inclui métricas de status do sistema (JVM e SO).
*   **EventData**: Classe base para todos os eventos, fornecendo identificação (UUID da sessão) e timestamping.
*   **Serializable**: Interface que permite a serialização dos dados.
*   **MeterContext**: Interface para manipulação de contexto (pares chave-valor).

## Legenda de Visibilidade

Os símbolos à esquerda de atributos e métodos indicam seu nível de acesso:
*   `+` : **Public** (Acessível de qualquer lugar)
*   `-` : **Private** (Acessível apenas na própria classe)
*   `#` : **Protected** (Acessível na classe, subclasses e pacote)
*   `~` : **Package-Private** (Acessível apenas dentro do mesmo pacote)
*   **MeterExecutor**: Interface para execução de tarefas funcionais com controle de ciclo de vida.
*   **MeterAnalysis**: Interface base para análise de dados do Meter.
*   **Logger**: Utilizado para registrar mensagens legíveis por humanos e dados para processamento automático.
