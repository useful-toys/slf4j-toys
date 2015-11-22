**What is SLF4J-TOYS?**

SLF4J-TOYS is collection of useful practices that complement SLF4J.

**What is the purpose of SLF4J-TOYS?**

SLF4J-TOYS promotes clean and organized log files, like:
```
[main] INFO dao - OK [Insert]: dao/saveUser; 0,1s; 1,5MB; login=alice; 
[main] INFO dao - OK [Update]: dao/saveUser; 0,1s; 1,5MB; login=bob; 
[main] INFO dao - REJECT [Concurrent]: dao/saveUser; 0,1s; 1,5MB; login=bob; 
[main] INFO dao - FAIL [OutOfQuota]: dao/saveUser; 0,1s; 1,5MB
```

And suggests a coding convention for consistent logging, like:
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

**Similar projects**

 * [Perf4J](http://perf4j.codehaus.org/) Appearantly discotinued project.
 * [Spped4J](http://perf4j.codehaus.org/) A continuation and enhancement of Perf4J.

