# Logging Test Case - Guia de Testes com MockLogger

## Visão Geral

Este documento descreve como escrever testes para código que utiliza SLF4J, usando o framework `slf4j-test-mock` para capturar e inspecionar eventos de log gerados durante a execução dos testes.

## Dependências

```xml
<dependency>
    <groupId>org.usefultoys</groupId>
    <artifactId>slf4j-test-mock</artifactId>
    <version>${version}</version>
    <scope>test</scope>
</dependency>
```

## Estrutura Básica de um Teste

### 1. Configuração da Classe de Teste

```java
@ExtendWith({MockLoggerExtension.class})
class MyServiceTest {
    
    @Slf4jMock("test.myservice")  // Nome único para esta classe de teste
    private Logger logger;
    
    @Test
    void shouldLogExpectedMessage() {
        // Arrange
        MyService service = new MyService(logger);
        
        // Act
        service.doSomething();
        
        // Assert
        AssertLogger.assertEvent(logger, 0, Level.INFO, "Expected message");
    }
}
```

**Elementos essenciais:**

1. **`@ExtendWith(MockLoggerExtension.class)`**: Habilita o gerenciamento automático do MockLogger
2. **`@Slf4jMock("nome.único")`**: Declara e configura o logger mock
3. **Tipo `Logger`**: Sempre usar a interface `org.slf4j.Logger` (nunca `MockLogger`)
4. **Sem inicialização**: O logger é injetado automaticamente pela extensão

## Declaração do Logger Mock

### Forma Recomendada

```java
@Slf4jMock("test.report.calendar")
private Logger logger;
```

**Convenções de nomenclatura:**
- Prefixo `test.` para evitar conflitos com loggers de produção
- Nome único por classe de teste
- Padrão descritivo: `test.<módulo>.<classe>`

### Variações de Configuração

```java
// Logger com nome baseado em uma classe
@Slf4jMock(type = ReportCalendar.class)
private Logger logger;

// Logger com apenas alguns níveis habilitados
@Slf4jMock(value = "test.custom", debugEnabled = false, traceEnabled = false)
private Logger logger;

// Logger completamente desabilitado
@Slf4jMock(value = "test.disabled", enabled = false)
private Logger logger;
```

**Atributos disponíveis:**
- `value`: Nome explícito do logger (recomendado)
- `type`: Classe cujo nome será usado como nome do logger
- `enabled`: Habilita/desabilita o logger (padrão: `true`)
- `traceEnabled`, `debugEnabled`, `infoEnabled`, `warnEnabled`, `errorEnabled`: Controla níveis individuais

## Ciclo de Vida do Logger

O `MockLoggerExtension` gerencia automaticamente o ciclo de vida do logger:

1. **Uma vez por classe** (`postProcessTestInstance`):
   - Cria o MockLogger via `LoggerFactory.getLogger()`
   - Injeta no campo anotado com `@Slf4jMock`

2. **Antes de cada teste** (`beforeEach`):
   - **Limpa todos os eventos capturados** (`clearEvents()`)
   - Reaplica a configuração da anotação

3. **Durante o teste**:
   - MockLogger captura todos os eventos de log
   - Eventos ficam armazenados em uma lista interna

4. **Após o teste**:
   - Eventos permanecem disponíveis para assertions
   - Próximo teste começa com logger limpo

**Importante:** Você nunca precisa limpar manualmente os eventos. A extensão garante isolamento entre testes.

## Assertions - Inspeção de Eventos

A classe `AssertLogger` fornece métodos estáticos para inspecionar eventos de log sem precisar fazer cast para `MockLogger`.

### Assertions por Índice Específico

#### `assertEvent` - Verificação Positiva

Verifica que o evento em um **índice específico** corresponde aos critérios.

```java
// Verifica que o primeiro evento (índice 0) é INFO e contém "Calendar"
AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO, "Calendar");
```

**Variantes:**

```java
// Apenas mensagem (qualquer nível)
AssertLogger.assertEvent(logger, 0, "part1", "part2");

// Nível + mensagem
AssertLogger.assertEvent(logger, 0, Level.INFO, "part1", "part2");

// Nível + marker + mensagem
AssertLogger.assertEvent(logger, 0, Level.WARN, marker, "part1", "part2");

// Apenas marker
AssertLogger.assertEvent(logger, 0, marker);
```

**Como funciona:**
- O método obtém o evento no índice especificado (0-based)
- Verifica se o nível corresponde (se fornecido)
- Verifica se o marker corresponde (se fornecido)
- Verifica se a mensagem contém **todas** as partes fornecidas (em qualquer ordem)
- Lança `AssertionError` se qualquer critério falhar

**Exemplo real:**

```java
@Test
void shouldLogDefaultCalendarInformation() {
    // Arrange
    final Date fixedCurrentDate = new Date(1678886400000L);
    final TimeZone defaultTimeZone = TimeZone.getDefault();
    ReportCalendar report = new ReportCalendar(logger);
    
    // Act
    report.run();
    
    // Assert - Verifica evento 0
    DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
    df.setTimeZone(defaultTimeZone);
    String expectedDateString = df.format(fixedCurrentDate);
    
    AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
        "Calendar",
        " - current date/time: " + expectedDateString,
        " - default timezone: " + defaultTimeZone.getDisplayName(),
        " - available IDs:",
        "America/Sao_Paulo; ",
        "UTC; ");
}
```

#### `assertEventNot` - Verificação Negativa

Verifica que o evento em um **índice específico** **NÃO corresponde** à combinação de critérios.

```java
// Verifica que evento 0 NÃO tem a combinação (INFO + "America/Sao_Paulo; ")
AssertLogger.assertEventNot(logger, 0, MockLoggerEvent.Level.INFO, "America/Sao_Paulo; ");
```

**Como funciona:**
- Obtém o evento no índice especificado
- Verifica se o evento **NÃO possui** simultaneamente todos os critérios
- O teste **PASSA** se:
  - O nível for diferente, OU
  - O marker for diferente, OU
  - A mensagem não contiver alguma das partes
- O teste **FALHA** se o evento possui exatamente a combinação de critérios

**Importante:** É uma negação da **combinação completa**, não de cada critério individualmente.

**Exemplo real:**

```java
@Test
void shouldLogCustomCalendarInformation() {
    // Arrange
    final TimeZone customTimeZone = TimeZone.getTimeZone("Europe/Berlin");
    final String[] customAvailableIDs = {"Europe/Berlin", "America/New_York", "Asia/Tokyo"};
    ReportCalendar report = new ReportCalendar(logger, customTimeZone, customAvailableIDs);
    
    // Act
    report.run();
    
    // Assert - Verifica que contém os IDs esperados
    AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
        "Europe/Berlin; ",
        "America/New_York; ",
        "Asia/Tokyo; ");
    
    // Assert - Verifica que NÃO contém ID não esperado
    AssertLogger.assertEventNot(logger, 0, MockLoggerEvent.Level.INFO, "America/Sao_Paulo; ");
}
```

### Assertions Existenciais

#### `assertHasEvent` - Verifica Existência

Verifica que **existe pelo menos um evento** em toda a lista que corresponde aos critérios.

```java
// Verifica que existe pelo menos um evento INFO contendo "Success"
AssertLogger.assertHasEvent(logger, Level.INFO, "Success");
```

**Uso:** Quando não importa a posição do evento, apenas que ele foi logado em algum momento.

**Variantes:**

```java
// Apenas mensagem
AssertLogger.assertHasEvent(logger, "part1", "part2");

// Nível + mensagem
AssertLogger.assertHasEvent(logger, Level.ERROR, "part1", "part2");

// Marker + mensagem
AssertLogger.assertHasEvent(logger, marker, "part1", "part2");

// Nível + marker + mensagem
AssertLogger.assertHasEvent(logger, Level.WARN, marker, "part1", "part2");
```

#### `assertNoEvent` - Verifica Inexistência

Verifica que **nenhum evento** na lista corresponde aos critérios.

```java
// Verifica que nenhum evento ERROR contém "Failed"
AssertLogger.assertNoEvent(logger, Level.ERROR, "Failed");
```

**Uso:** Para garantir que algo não foi logado em nenhum momento durante o teste.

### Assertions para Throwables

#### Verificação Positiva de Exceções

```java
// Verifica que evento 0 tem uma IOException
AssertLogger.assertEventWithThrowable(logger, 0, IOException.class);

// Verifica que evento 0 tem throwable com mensagem contendo "Connection"
AssertLogger.assertEventWithThrowable(logger, 0, "Connection", "failed");

// Verifica que evento 0 tem IOException com mensagem específica
AssertLogger.assertEventWithThrowable(logger, 0, IOException.class, "Connection", "failed");

// Verifica que evento 0 tem algum throwable
AssertLogger.assertEventHasThrowable(logger, 0);

// Verifica que existe pelo menos um evento com IOException
AssertLogger.assertHasEventWithThrowable(logger, IOException.class);
```

#### Verificação Negativa de Exceções

```java
// Verifica que evento 0 NÃO tem throwable
AssertLogger.assertEventNotWithThrowable(logger, 0);

// Verifica que nenhum evento tem throwable
AssertLogger.assertNoEventWithThrowable(logger);

// Verifica que nenhum evento tem IOException
AssertLogger.assertNoEventWithThrowable(logger, IOException.class);
```

## Padrões de Teste

### Padrão Given-When-Then (Arrange-Act-Assert)

```java
@Test
void shouldLogWarningWhenResourceNotFound() {
    // Arrange (Given)
    ResourceService service = new ResourceService(logger);
    String nonExistentResource = "unknown.txt";
    
    // Act (When)
    service.loadResource(nonExistentResource);
    
    // Assert (Then)
    AssertLogger.assertEvent(logger, 0, Level.WARN, 
        "Resource not found", nonExistentResource);
}
```

### Verificação de Múltiplos Eventos

```java
@Test
void shouldLogProcessingSteps() {
    // Arrange
    DataProcessor processor = new DataProcessor(logger);
    
    // Act
    processor.process();
    
    // Assert - Verifica cada evento em ordem
    AssertLogger.assertEvent(logger, 0, Level.INFO, "Starting processing");
    AssertLogger.assertEvent(logger, 1, Level.DEBUG, "Reading data");
    AssertLogger.assertEvent(logger, 2, Level.DEBUG, "Validating data");
    AssertLogger.assertEvent(logger, 3, Level.INFO, "Processing complete");
}
```

### Verificação de Conteúdo Dinâmico

```java
@Test
void shouldLogTimestampedMessage() {
    // Arrange
    Date now = new Date();
    DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
    String expectedTimestamp = df.format(now);
    
    Service service = new Service(logger);
    
    // Act
    service.logWithTimestamp(now);
    
    // Assert
    AssertLogger.assertEvent(logger, 0, Level.INFO, 
        "Event at", expectedTimestamp);
}
```

### Verificação Positiva e Negativa Combinadas

```java
@Test
void shouldLogOnlyExpectedItems() {
    // Arrange
    String[] items = {"item1", "item2"};
    ItemProcessor processor = new ItemProcessor(logger);
    
    // Act
    processor.process(items);
    
    // Assert - Verifica que contém os esperados
    AssertLogger.assertHasEvent(logger, "item1");
    AssertLogger.assertHasEvent(logger, "item2");
    
    // Assert - Verifica que NÃO contém outros
    AssertLogger.assertNoEvent(logger, "item3");
    AssertLogger.assertNoEvent(logger, "item4");
}
```

### Teste de Exceções

```java
@Test
void shouldLogExceptionDetails() {
    // Arrange
    Service service = new Service(logger);
    
    // Act
    try {
        service.operationThatFails();
    } catch (IOException e) {
        // Expected
    }
    
    // Assert
    AssertLogger.assertEventWithThrowable(logger, 0, IOException.class);
    AssertLogger.assertEvent(logger, 0, Level.ERROR, "Operation failed");
}
```

## Tabela de Referência Rápida

| Método | Escopo | Critérios | Uso |
|--------|--------|-----------|-----|
| `assertEvent` | Índice específico | AND (todos) | Verifica que evento POSSUI características |
| `assertEventNot` | Índice específico | NOT AND (combinação) | Verifica que evento NÃO POSSUI combinação |
| `assertHasEvent` | Qualquer evento | AND (todos) | Verifica que EXISTE evento com características |
| `assertNoEvent` | Nenhum evento | NOT EXISTS | Verifica que NÃO EXISTE evento com características |
| `assertEventWithThrowable` | Índice específico | Throwable | Verifica throwable no evento |
| `assertHasEventWithThrowable` | Qualquer evento | Throwable | Verifica que existe evento com throwable |
| `assertNoEventWithThrowable` | Nenhum evento | NOT Throwable | Verifica que nenhum evento tem throwable |

## Comparação de Assertions

### Exemplo com 3 Eventos Capturados

Suponha que durante um teste, o logger capturou:

```
Evento 0: INFO: "Processing started for user: john"
Evento 1: DEBUG: "Loading configuration from database"
Evento 2: INFO: "Processing completed successfully"
```

**Assertions que PASSAM:**

```java
// ✅ Evento 0 é INFO e contém "Processing"
assertEvent(logger, 0, Level.INFO, "Processing");

// ✅ Evento 0 NÃO tem a combinação (DEBUG + "Processing")
assertEventNot(logger, 0, Level.DEBUG, "Processing");

// ✅ Existe pelo menos um evento DEBUG (evento 1)
assertHasEvent(logger, Level.DEBUG);

// ✅ Não existe nenhum evento ERROR
assertNoEvent(logger, Level.ERROR);

// ✅ Existe evento contendo "successfully"
assertHasEvent(logger, "successfully");
```

**Assertions que FALHAM:**

```java
// ❌ Evento 0 não contém "completed"
assertEvent(logger, 0, Level.INFO, "completed");

// ❌ Evento 0 TEM a combinação (INFO + "Processing")
assertEventNot(logger, 0, Level.INFO, "Processing");

// ❌ Não existe evento WARN
assertHasEvent(logger, Level.WARN);

// ❌ Existe evento INFO (evento 0 e 2)
assertNoEvent(logger, Level.INFO);

// ❌ Não existe evento contendo "database"
assertNoEvent(logger, "database");
```

## Importante: Quando Usar AssertLogger vs JUnit Assertions

### ✅ USE AssertLogger para:

**Todas as verificações relacionadas a eventos de log:**

```java
// ✅ CORRETO - Verificar conteúdo de eventos de log
AssertLogger.assertEvent(logger, 0, Level.INFO, "Expected message");
AssertLogger.assertHasEvent(logger, "Some text");
AssertLogger.assertNoEvent(logger, Level.ERROR);

// ✅ CORRETO - Verificar presença de eventos específicos
AssertLogger.assertHasEvent(logger, "Java Virtual Machine");
AssertLogger.assertNoEvent(logger, "Unexpected message");
```

### ✅ USE JUnit Assertions (`org.junit.jupiter.api.Assertions`) para:

**Verificações que NÃO são sobre eventos de log:**

```java
// ✅ CORRETO - Verificar contagem de execuções (não é evento de log)
AtomicInteger executionCount = new AtomicInteger(0);
assertEquals(5, executionCount.get(), "Should execute 5 times");

// ✅ CORRETO - Verificar estado de objetos de negócio
assertTrue(ConfigParser.isInitializationOK(), "Config should be OK");

// ✅ CORRETO - Verificar valores retornados
assertEquals("expected", result, "Result should match");
```

### ❌ NÃO USE JUnit Assertions para:

**Qualquer verificação sobre eventos de log:**

```java
// ❌ ERRADO - Não acessar MockLogger diretamente
assertTrue(((MockLogger) logger).getEventCount() > 0);
assertEquals(1, ((MockLogger) logger).getEventCount());

// ❌ ERRADO - Não verificar conteúdo de mensagens assim
String logOutput = ((MockLogger) logger).getEvent(0).getFormattedMessage();
assertTrue(logOutput.contains("some text"));

// ✅ CORRETO - Use AssertLogger em vez disso
AssertLogger.assertHasEvent(logger, "some text");
```

### 📋 Regra Geral

**Se a verificação envolve o `logger` injetado com `@Slf4jMock`, use `AssertLogger`.**  
**Para outras verificações de negócio, use JUnit Assertions normalmente.**

## Boas Práticas

### ✅ DO (Fazer)

1. **Sempre usar tipo `Logger`**
   ```java
   @Slf4jMock("test.myservice")
   private Logger logger;
   ```

2. **Usar nome único por classe de teste**
   ```java
   // ReportCalendarTest.java
   @Slf4jMock("test.report.calendar")
   
   // ReportMemoryTest.java
   @Slf4jMock("test.report.memory")
   ```

3. **Confiar no reset automático**
   - Não precisa limpar eventos manualmente
   - Cada teste começa com logger limpo

4. **Usar AssertLogger para TODAS as verificações de eventos de log**
   ```java
   // ✅ CORRETO
   AssertLogger.assertEvent(logger, 0, Level.INFO, "Message");
   AssertLogger.assertHasEvent(logger, "Physical system");
   
   // ❌ ERRADO
   assertTrue(((MockLogger) logger).getEventCount() > 0);
   ```

5. **Combinar assertions positivas e negativas**
   ```java
   AssertLogger.assertEvent(logger, 0, Level.INFO, "Expected");
   AssertLogger.assertEventNot(logger, 0, Level.INFO, "Unexpected");
   ```

6. **Verificar partes relevantes da mensagem**
   ```java
   // Ao invés de verificar a mensagem inteira
   AssertLogger.assertEvent(logger, 0, Level.INFO, 
       "key-part-1", "key-part-2", "key-part-3");
   ```

7. **Usar JUnit Assertions para lógica de negócio**
   ```java
   // ✅ CORRETO - Não é sobre eventos de log
   assertEquals(18, executionCount.get(), "All reports should execute");
   assertTrue(ConfigParser.isInitializationOK(), "Config should be valid");
   ```

### ❌ DON'T (Não fazer)

1. **Não usar tipo `MockLogger`**
   ```java
   // ❌ ERRADO
   @Slf4jMock("test.logger")
   private MockLogger logger;
   ```

2. **Não fazer cast para MockLogger para verificar eventos**
   ```java
   // ❌ ERRADO - Não acessar métodos do MockLogger para assertions
   MockLogger mock = (MockLogger) logger;
   assertEquals(1, mock.getEventCount());
   assertTrue(mock.getEvent(0).getFormattedMessage().contains("text"));
   
   // ✅ CORRETO - Use AssertLogger
   AssertLogger.assertHasEvent(logger, "text");
   ```

3. **Não usar JUnit Assertions para verificar eventos de log**
   ```java
   // ❌ ERRADO - Não usar assertTrue/assertEquals para logs
   assertTrue(((MockLogger) logger).getEventCount() > 0);
   assertEquals(0, ((MockLogger) logger).getEventCount());
   
   String logOutput = ((MockLogger) logger).getLoggerEvents().stream()
       .map(MockLoggerEvent::getFormattedMessage)
       .collect(Collectors.joining("\n"));
   assertTrue(logOutput.contains("Expected text"));
   
   // ✅ CORRETO - Use AssertLogger
   AssertLogger.assertHasEvent(logger, "Expected text");
   AssertLogger.assertNoEvent(logger, "Unexpected");
   ```

4. **Não criar métodos helper para acessar MockLogger**
   ```java
   // ❌ ERRADO - Não criar getters para MockLogger
   private MockLogger getMockLogger() {
       return (MockLogger) logger;
   }
   
   private String getLogOutput() {
       return mockLogger.getLoggerEvents().stream()
           .map(MockLoggerEvent::getFormattedMessage)
           .collect(Collectors.joining("\n"));
   }
   
   // ✅ CORRETO - Use AssertLogger diretamente
   AssertLogger.assertHasEvent(logger, "message");
   ```

5. **Não inicializar logger programaticamente**
   ```java
   // ❌ ERRADO - A extensão faz isso
   @Slf4jMock("test.logger")
   private Logger logger = LoggerFactory.getLogger("test.logger");
   
   // ❌ ERRADO - Não fazer no @BeforeEach
   @BeforeEach
   void setUp() {
       Logger testLogger = LoggerFactory.getLogger("test.logger");
       mockLogger = (MockLogger) testLogger;
       mockLogger.clearEvents();
   }
   ```

6. **Não limpar eventos manualmente**
   ```java
   // ❌ ERRADO - MockLoggerExtension já faz isso
   @BeforeEach
   void setUp() {
       ((MockLogger) logger).clearEvents();
   }
   ```

7. **Não usar nomes genéricos ou constantes desnecessárias**
   ```java
   // ❌ ERRADO - Nome genérico (pode colidir)
   @Slf4jMock("test")
   private Logger logger;
   
   // ❌ ERRADO - Constante desnecessária
   private static final String TEST_LOGGER_NAME = "test.reporter";
   
   @BeforeEach
   void setUp() {
       Logger testLogger = LoggerFactory.getLogger(TEST_LOGGER_NAME);
   }
   
   // ✅ CORRETO - Nome específico diretamente na anotação
   @Slf4jMock("test.reporter")
   private Logger logger;
   ```

## Integração com Outras Extensões

O `MockLoggerExtension` pode ser combinado com outras extensões JUnit:

```java
@ExtendWith({
    CharsetConsistency.class,
    ResetReporterConfig.class,
    MockLoggerExtension.class
})
@WithLocale("en")
class ReportCalendarTest {
    
    @Slf4jMock("test.report.calendar")
    private Logger logger;
    
    // Testes...
}
```

**Ordem de execução:**
1. Extensões são executadas na ordem declarada
2. `MockLoggerExtension` deve vir por último se houver dependências de configuração

## Casos Especiais

### Testando com Múltiplos Loggers

Quando você precisa testar que diferentes loggers recebem eventos diferentes:

```java
@ExtendWith(MockLoggerExtension.class)
class MultiLoggerTest {
    
    @Slf4jMock("test.logger.main")
    private Logger mainLogger;
    
    @Test
    void shouldLogToCustomLogger() {
        // Arrange - Criar logger customizado em runtime
        String customLoggerName = "my.custom.logger";
        Logger customLogger = LoggerFactory.getLogger(customLoggerName);
        
        Service service = new Service(customLogger);
        
        // Act
        service.doSomething();
        
        // Assert - Verifica que customLogger recebeu o evento
        AssertLogger.assertHasEvent(customLogger, "Expected message");
        
        // Assert - Verifica que mainLogger NÃO recebeu o evento
        AssertLogger.assertNoEvent(mainLogger, "Expected message");
    }
}
```

**⚠️ Importante:** Se criar um logger adicional com `LoggerFactory.getLogger()`, ele **também** é um MockLogger e pode ser usado com `AssertLogger`.

### Testando com Níveis de Log Desabilitados

Quando você precisa verificar comportamento quando um nível está desabilitado:

```java
@Test
void shouldNotLogWhenInfoDisabled() {
    // Arrange - Desabilitar INFO temporariamente (cast necessário aqui)
    ((MockLogger) logger).setInfoEnabled(false);
    
    Service service = new Service(logger);
    
    // Act
    service.doSomethingThatLogsInfo();
    
    // Assert - Nenhum evento INFO deve ter sido capturado
    AssertLogger.assertNoEvent(logger, Level.INFO, "Any message");
}
```

**Nota:** Este é um dos **poucos casos** onde fazer cast para `MockLogger` é aceitável, pois estamos **configurando** o comportamento do mock, não **verificando** eventos.

### Testando com Loggers Customizados em Runtime

Quando o sistema sob teste cria seu próprio logger baseado em configuração:

```java
@Test
void shouldUseConfiguredLoggerName() {
    // Arrange - Configurar o sistema para usar um logger específico
    String customLoggerName = "my.custom.logger";
    System.setProperty(ReporterConfig.PROP_NAME, customLoggerName);
    ReporterConfig.init();
    
    // Obter referência ao logger que será usado
    Logger customLogger = LoggerFactory.getLogger(customLoggerName);
    
    // Act
    Reporter reporter = new Reporter(); // Usa logger configurado
    reporter.logDefaultReports();
    
    // Assert - Verificar no logger correto
    AssertLogger.assertHasEvent(customLogger, "Expected report content");
    
    // Cleanup
    System.clearProperty(ReporterConfig.PROP_NAME);
}
```

## Troubleshooting

### Logger não é injetado

**Problema:** Campo `logger` é null durante o teste.

**Causa:** Faltou `@ExtendWith(MockLoggerExtension.class)` na classe de teste.

**Solução:**
```java
@ExtendWith(MockLoggerExtension.class)  // Adicionar
class MyTest {
    @Slf4jMock("test.myservice")
    private Logger logger;
}
```

### Eventos de teste anterior aparecem

**Problema:** Eventos de um teste aparecem no próximo teste.

**Causa:** `MockLoggerExtension` não está registrado ou há erro na extensão.

**Solução:** Verificar que a extensão está declarada corretamente.

### AssertionError: "Expected MockLogger but got..."

**Problema:** LoggerFactory não retorna MockLogger.

**Causa:** Binding SLF4J incorreto no classpath (ex: logback-classic ao invés de slf4j-test-mock).

**Solução:**
1. Verificar dependências no pom.xml
2. Garantir que `slf4j-test-mock` está no scope test
3. Excluir outros bindings SLF4J do classpath de teste

### Assertion falha mas mensagem parece correta

**Problema:** `assertEvent` falha mas a mensagem parece conter as partes esperadas.

**Causa:** Whitespace, case sensitivity ou formatação diferente.

**Solução:**
1. Verificar espaços em branco nas strings
2. Verificar maiúsculas/minúsculas
3. Usar partes menores e mais específicas da mensagem
4. Imprimir a mensagem real para debug:
   ```java
   // Para debug temporário (remover depois)
   MockLogger mock = (MockLogger) logger;
   System.out.println(mock.getEvents().get(0).getMessage());
   ```

## Exemplo Completo

### Exemplo 1: Teste Simples com Dados Controlados

Baseado em `ReportCalendarTest.java`:

```java
package org.usefultoys.slf4j.report;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.impl.MockLoggerEvent;
import org.usefultoys.slf4jtestmock.AssertLogger;
import org.usefultoys.slf4jtestmock.MockLoggerExtension;
import org.usefultoys.slf4jtestmock.Slf4jMock;

import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

@ExtendWith(MockLoggerExtension.class)
class ReportCalendarTest {

    @Slf4jMock("test.report.calendar")
    private Logger logger;

    @Test
    void shouldLogCustomCalendarInformation() {
        // Arrange - Criar dados controlados para teste determinístico
        final Date customDate = new Date(1678886400000L); // March 15, 2023
        final TimeZone customTimeZone = TimeZone.getTimeZone("Europe/Berlin");
        final String[] customAvailableIDs = {"Europe/Berlin", "America/New_York", "Asia/Tokyo"};

        // Criar provider com dados mockados
        ReportCalendar.CalendarInfoProvider provider = new ReportCalendar.CalendarInfoProvider() {
            @Override
            public Date getCurrentDate() { return customDate; }
            @Override
            public TimeZone getDefaultTimeZone() { return customTimeZone; }
            @Override
            public String[] getAvailableTimeZoneIDs() { return customAvailableIDs; }
        };

        ReportCalendar report = new ReportCalendar(logger) {
            @Override
            protected ReportCalendar.CalendarInfoProvider getCalendarInfoProvider() {
                return provider;
            }
        };

        // Act
        report.run();

        // Assert - Verificações positivas (deve conter)
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
        df.setTimeZone(customTimeZone);
        String expectedDateString = df.format(customDate);
        
        AssertLogger.assertEvent(logger, 0, MockLoggerEvent.Level.INFO,
            "Calendar",
            " - current date/time: " + expectedDateString,
            " - default timezone: " + customTimeZone.getDisplayName(),
            "Europe/Berlin; ",
            "America/New_York; ",
            "Asia/Tokyo; ");
        
        // Assert - Verificação negativa (NÃO deve conter)
        AssertLogger.assertEventNot(logger, 0, MockLoggerEvent.Level.INFO, "America/Sao_Paulo; ");
    }
}
```

### Exemplo 2: Teste com Múltiplas Verificações

Baseado em `ReporterTest.java`:

```java
package org.usefultoys.slf4j.report;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.usefultoys.slf4jtestmock.AssertLogger;
import org.usefultoys.slf4jtestmock.MockLoggerExtension;
import org.usefultoys.slf4jtestmock.Slf4jMock;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockLoggerExtension.class)
class ReporterTest {

    @Slf4jMock("test.reporter")
    private Logger logger;
    
    private Reporter reporter;

    @BeforeEach
    void setUp() {
        reporter = new Reporter(logger);
    }

    @Test
    void shouldExecuteOnlyEnabledReports() {
        // Arrange
        System.setProperty(ReporterConfig.PROP_VM, "true");
        System.setProperty(ReporterConfig.PROP_PROPERTIES, "true");
        ReporterConfig.init();

        // Contador de execuções (não é evento de log - pode usar JUnit Assertions)
        AtomicInteger executionCount = new AtomicInteger(0);
        Executor countingExecutor = command -> {
            command.run();
            executionCount.incrementAndGet();
        };

        // Act
        reporter.logDefaultReports(countingExecutor);

        // Assert - Verificar contagem de execuções (JUnit Assertions)
        assertEquals(6, executionCount.get(), "Only 6 reports should be executed");
        
        // Assert - Verificar eventos de log (AssertLogger)
        AssertLogger.assertHasEvent(logger, "Java Virtual Machine");
        AssertLogger.assertHasEvent(logger, "System Properties");
        
        // Assert - Verificar estado de configuração (JUnit Assertions)
        assertTrue(ConfigParser.isInitializationOK(), "Config should be valid");
    }

    @Test
    void shouldNotLogWhenReportsDisabled() {
        // Arrange - Desabilitar todos os reports
        System.setProperty(ReporterConfig.PROP_VM, "false");
        System.setProperty(ReporterConfig.PROP_PROPERTIES, "false");
        ReporterConfig.init();

        AtomicInteger executionCount = new AtomicInteger(0);
        Executor countingExecutor = command -> {
            command.run();
            executionCount.incrementAndGet();
        };

        // Act
        reporter.logDefaultReports(countingExecutor);

        // Assert - Nenhuma execução (JUnit Assertions)
        assertEquals(0, executionCount.get(), "No reports should be executed");
        
        // Assert - Nenhum evento logado (AssertLogger)
        AssertLogger.assertNoEvent(logger, "Java Virtual Machine");
        AssertLogger.assertNoEvent(logger, "Physical system");
    }
}
```

### Exemplo 3: Teste com Exceções

```java
@ExtendWith(MockLoggerExtension.class)
class UserServiceTest {
    
    @Slf4jMock("test.service.user")
    private Logger logger;
    
    @Test
    void shouldLogExceptionWhenDatabaseFails() {
        // Arrange
        UserService service = new UserServiceWithFailingDatabase(logger);
        User user = new User("john.doe", "john@example.com");
        
        // Act & Assert - Verificar que exceção é lançada (JUnit Assertions)
        assertThrows(DatabaseException.class, () -> service.createUser(user));
        
        // Assert - Verificar que exceção foi logada (AssertLogger)
        AssertLogger.assertEventWithThrowable(logger, 0, DatabaseException.class);
        AssertLogger.assertEvent(logger, 0, Level.ERROR, 
            "Failed to create user", "john.doe");
    }
}
```

## Referências

- [AssertLogger API](../tmp/org/usefultoys/slf4jtestmock/AssertLogger.java)
- [MockLoggerExtension API](../tmp/org/usefultoys/slf4jtestmock/MockLoggerExtension.java)
- [Slf4jMock Annotation](../tmp/org/usefultoys/slf4jtestmock/Slf4jMock.java)
- [Exemplo Completo: ReportCalendarTest](../slf4j-toys/src/test/java/org/usefultoys/slf4j/report/ReportCalendarTest.java)

