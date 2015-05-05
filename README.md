# slf4j-toys #

**SLF4J-TOYS is collection of useful things for SLF4J:**
 * Promote predictable logger naming convention;
 * Adopt logger coding conventions with minimal effort; 
 * Automate analysis on paseable log messages;
 * Measure resources with minimal effort;
 * Generate large messages and reports as printstreams;
 * Dump binary snapshot as outputstreams.

## Promote predictable logger naming convention

*Applications use the fully qualified class name as logger name. 
I prefer to call this LOGGER NAMING HELL.
One logger per class results in thousands of loggers names.
Those names are long and repetitive.
They reflect your implementation structure and do not follow a self-documenting logical organization.
They are challenging to predict by outsiders. 
Even worse, they impliy string constants conventions between your implementation and your logger configuration.
Any refactor breaks existing logger configuration.*

The **LoggerFactory substitute** predictable logger naming convention. The original `getLogger(...)` methods were preserved for compatibility.

I suggest creating a logger hierarchy that rely  on features groups, features and operations. 
For example, instead of *com.company.application.authentication.dao.hibernate.UserDAOImpl*, one could use *authentication.persistence.user*: 
shorter, more intuitive and does not leak implementation details. 
The suggested hierarchy is defined purely on . Names are short and self describing. 
One each level, define a logger based on the parent level logger by calling `getLogger(Logger parent, String name)`. 
This protects your logging configuration against refactoring as the logger name is preserved.

The `getLogger(Class<?> clazz, String name)` helps to fine tune loggers for specific operations or features of interest provided by your class. 
For example, you could define loggers to separately track `open()` and `close()` methods.

## Operation demarcation and performance measure ##

*Most logging messages intend telling that an operation of interest succeeds of failed. Sometimes, the beginning of the operation is logged too. Further, the performance concerned want log execution count and time. Some are interested on additional input/output data of the operation, of state that influences the operation. Others want to know how system resources were affected by an operation.*

The **Meter** is a special logger that address all these requirements. It clearly delimiters the beginning and ending of an operation, distinguishing success from failure. It counts executions and records its time. It tracks input/output data and relevant state. Further, it traces relevant system resources before and after the operation. The log messages are parsable for later comprehensive analysis. The level of the logger associated to the operation controls the amount of information.

## Similar projects ##

 * [Perf4J](http://perf4j.codehaus.org/) Appearantly discotinued project.
 * [Spped4J](http://perf4j.codehaus.org/) A continuation and enhancement of Perf4J.
