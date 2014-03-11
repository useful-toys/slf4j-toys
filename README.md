# slf4j-toys #

SLF4J-TOYS is set o useful things that work with SLF4J:
 * Consistent and predictable logger naming convention;
 * Recurrent start/stop logging patterns;
 * Perfomance measure of relevat operations;
 * Application resource usage;
 * Data and state logged using properties;
 * Large data logged using formatted print streams;
 * Parseable message for later analisys.

## Consistent and predictable logger naming convention

Nearly every application reuses the fully qualified class name as logger name. Though simple, there are several disadvantages. It imposes one logger per class. Logger names reflect strict software structure instead of self documenting logical organization. It implies long string constants conventions between implementation and logger configuration. Any refactor breaks existing logger configuration. 

The logger factory substitute eases the consistent and predictable logger naming convention. The original getLogger(...) methods were preserved for compatibility.

The getLogger(Class<?> clazz, String name) helps to fine tune loggers for specific operations or features of interest provided by your class. For example, you could define loggers to separately track open() and close() methods.

Instead of naming your logger with your class name, I suggest creating a logger for each feature group, then define a logger for each functionalty of each feature calling getLogger(Logger logger, String name).

For shorter, more intuitive and predictable logger names, I suggest calling getLogger(Logger logger, String name) for a feature or operation of interest based on another existing logger. This protects your logging configuration against refactoting as the logger name is preserved. Also, I recommeny using a short named logger as prefix for specific loggers.

## Recurrent logging patterns ##
The application should informe when a relevant operation starts, succeeds or fails.
```java
Meter m = MeterFactory.getMeter(WatcherDemo.class).start();
try {
  ...
  m.ok();
} catch (InterruptedException ex) {
  m.fail(ex);
}
```
## Similar projects ##

 * [Perf4J](http://perf4j.codehaus.org/) Appearantly discotinued project.
 * [Spped4J](http://perf4j.codehaus.org/) A continuation and enhancement of Perf4J.
