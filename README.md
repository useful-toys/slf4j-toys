# SLF4J TOYS

*slf4j-toys* is a Java library that complements SLF4J with additional useful logging practices.

*slf4j-toys* enables simple but effective observability, without relying on complex or expensive tools.

## Goals

*   A **LoggerFactory** that provides additional useful factory SLF4J Logger methods.
*   A **Meter** that works like a log message builder that promotes consistent messages all across the application.
*   A **Watcher** that produces periodic memory and CPU status reports for simple monitoring.

## Installation

*slf4j-toys* is available from the [Maven Central repository](https://search.maven.org/artifact/org.usefultoys/slf4j-toys/1.9.0/jar).

**Gradle:**
```gradle
dependencies {
    implementation("org.usefultoys:slf4j-toys:1.9.0")
}
```

**Maven:**
```xml
<dependency>
    <groupId>org.usefultoys</groupId>
    <artifactId>slf4j-toys</artifactId>
    <version>1.9.0</version>
</dependency>
```

## Requirements

*   **Java 8** or newer.

## Usage Example

*slf4j-toys* promotes clean and organized log files, like this:
```
[main] 25/11/2015 14:35:14 INFO dao - OK [Insert]: dao/saveUser; 0,1s; 1,5MB; login=alice; 
[main] 25/11/2015 14:36:35 INFO dao - OK [Update]: dao/saveUser; 0,1s; 1,5MB; login=bob; 
[main] 25/11/2015 14:37:27 INFO dao - REJECT [Concurrent]: dao/saveUser; 0,1s; 1,5MB; login=bob; 
[main] 25/11/2015 14:38:52 INFO dao - FAIL [OutOfQuota]: dao/saveUser; 0,1s; 1,5MB
```

This can be achieved with the `Meter` API:
```java
final Meter m = MeterFactory.getMeter(LOGGER, "saveUser").start();
try {
    // Check if record exists in database   
    boolean exist = ...;
    if (exist) {
        // Insert record into database
        m.ok("Insert");
    } else {
        // Update record in database
        m.ok("Update");
    }
} catch (OutOfQuotaException e) {
    m.reject(e);
} catch (SQLException e) {
    m.fail(e);
    throw new IllegalStateException(e);
}
```

It can also produce reports about your host environment:
```
INFO report - Memory:
 - maximum allowed: 1,9GB
 - currently allocated: 129,0MB (1,8GB more available)
 - currently used: 4,1MB (124,9MB free)
```

## Design Considerations

* No dependencies other than SLF4J itself.
* Small footprint.
* Take advantage of the SLF4J API capabilities and Logback features. 

## Further Information

 * [Using the `LoggerFactory` for additional useful Logger use cases](docs/LoggerFactory-usage.md)
 * [Using the `Watcher` to monitor your application healt and resource usage](docs/Watcher-usage.md)
 * [Using the `Reporter` to generate reports about your host environment](docs/Reporter-usage.md)

## Similar Projects

*   [Perf4J](https://github.com/perf4j/perf4j): A performance logging library for Java.
*   [Speed4J](https://github.com/jalkanen/speed4j): A continuation and enhancement of Perf4J. See [comparison between slf4j-toys and Speed4J](docs/slf4j-toys-vs-speed4j.md).
