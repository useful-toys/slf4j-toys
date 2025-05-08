# SLF4J TOYS

*slf4j-toys* is a Java library that complements SLF4J with additional useful logging practices.

## Goals

 * A **LoggerFactory** that provides additional usefuly factory SLF4J Logger methods
 * A **Meter** that works like a log message builder that promotes consistent messages all across the application.
 * A **Watcher** that produces periodically memory and cpu status, for simple 

*slf4j-toys* enables simple but effective observability, without relying on complex or expensive tool.

## Download

Gradle:
```gradle
dependencies {
    implementation("org.usefultoys:slf4j-toys:1.9.0")
}
```

Maven:
```xml
<dependency>
    <groupId>org.usefultoys</groupId>
    <artifactId>slf4j-toys</artifactId>
    <version>1.9.0</version>
</dependency>
```

## Requirements

### Minimum Java version
- *slf4j-toys* 1.9.0 and newer: Java 8
- *slf4j-toys* 1.8.0 and older: Java 7

> [!IMPORTANT]\
> *slf4j-toys* will not work on Java 9 or above. This issue will be fixed.

## Design consideration
 * No dependencies other than SLF4J itself
 * Small footprint



SLF4J-TOYS promotes clean and organized log files, like:
```
[main] 25/11/2015 14:35:14 INFO dao - OK [Insert]: dao/saveUser; 0,1s; 1,5MB; login=alice; 
[main] 25/11/2015 14:36:35 INFO dao - OK [Update]: dao/saveUser; 0,1s; 1,5MB; login=bob; 
[main] 25/11/2015 14:37:27 INFO dao - REJECT [Concurrent]: dao/saveUser; 0,1s; 1,5MB; login=bob; 
[main] 25/11/2015 14:38:52 INFO dao - FAIL [OutOfQuota]: dao/saveUser; 0,1s; 1,5MB
```






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

And produces reports about your host environment, like:
```
INFO report - Memory:
 - maximum allowed: 1,9GB
 - currently allocated: 129,0MB (1,8GB more available)
 - currently used: 4,1MB (124,9MB free)
```

It further encourages simple logger names as:
```
persistence.user
persistence.product
```

**Installing and using**

SLF4J-TOYS is avaiable at [Maven Central repository](http://search.maven.org/#artifactdetails|org.usefultoys|slf4j-toys|1.5.0|jar):
```
<dependency>
    <groupId>org.usefultoys</groupId>
    <artifactId>slf4j-toys</artifactId>
    <version>1.5.0</version>
</dependency>
```

SLF4J-TOYS is also avaiable as [single JAR](https://github.com/useful-toys/slf4j-toys/releases/download/1.5.0/slf4j-toys-1.5.0.jar).



**Further information**

Take a look at our [WIKI](https://github.com/useful-toys/slf4j-toys/wiki) or the [API Javadoc](http://useful-toys.github.io/slf4j-toys/javadoc/).

**Similar projects**

 * [Perf4J](http://perf4j.codehaus.org/) Appearantly discotinued project.
 * [Spped4J](http://perf4j.codehaus.org/) A continuation and enhancement of Perf4J.

