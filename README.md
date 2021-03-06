**What is SLF4J-TOYS?**

SLF4J-TOYS is collection of useful practices that complement SLF4J.

**What is the purpose of SLF4J-TOYS?**

SLF4J-TOYS promotes clean and organized log files, like:
```
[main] 25/11/2015 14:35:14 INFO dao - OK [Insert]: dao/saveUser; 0,1s; 1,5MB; login=alice; 
[main] 25/11/2015 14:36:35 INFO dao - OK [Update]: dao/saveUser; 0,1s; 1,5MB; login=bob; 
[main] 25/11/2015 14:37:27 INFO dao - REJECT [Concurrent]: dao/saveUser; 0,1s; 1,5MB; login=bob; 
[main] 25/11/2015 14:38:52 INFO dao - FAIL [OutOfQuota]: dao/saveUser; 0,1s; 1,5MB
```

Such messages are not only easily readable, but also easily parsable for later automatic investigation, like statistics and  data mining.

Most of SLF4J-TOYS is a kind of log message builder, that suggests a coding convention for consistent logging, like:
```
final Meter m = MeterFactory.getMeter(LOGGER, "saveUser").start();
try {
    // Check if record exists in database   
    boolean exist = ...;
    if (exist) {
        // Insert record into database
        m2.ok("Insert");
    } else {
        // Update record in database
        m2.ok("Update");
    }
} catch (OutOfQuotaException e) {
    m2.reject(e);
} catch (SQLException e) {
    m2.fail(e);
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

