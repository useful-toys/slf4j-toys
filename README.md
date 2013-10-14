# slf4j-toys #

SLF4J-TOYS is set o useful things that work with SLF4J:
 * Recurrent logging patterns.
 * Measure perfomance of relevat operations.
 * Watch application resources.
 * Log large data and state as formatted streams.

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
