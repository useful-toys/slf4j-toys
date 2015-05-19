# slf4j-toys #

**SLF4J-TOYS is collection of useful things for SLF4J:**

 * Promote more predictable logger naming convention;
 * Adopt logging conventions with minimal effort;
 * Measure resource usage with minimal effort;
 * Report data readably using PrintStream;
 * Dump binary snapshots using OutputStreams.


## Promote more predictable logger naming convention

*Modern Java application suffer from what I prefer to call this LOGGER NAMING HELL.
The convention of logger per class results in an assortment of thousands long, repetitive and boring names.
They reflect your implementation structure and do not follow a self-documenting logical organization.
They are challenging to predict by outsiders. 
Even worse, they imply string constants convention between your implementation and your outside logger configuration. 
Any refactor breaks existing logger configuration.*

The **LoggerFactory substitute** promotes a more predictable logger naming convention. 

Instead of letting your package hierarchy should to drive your logger hierarchy, 
I suggest creating a logger hierarchy that relies on features groups, features and operations. 
For example, instead of _com.company.application.dao.authentication.hibernate.UserDAOImpl_, 
one could use _persistence.authentication.user_: 
shorter, more intuitive and does not leak implementation details. 
The suggested hierarchy is defined over a more predictable structure that probably will not change much from application to application. 
A refactoring will not harm much any existing logger configuration.
Names are shorter and self-describing. 

Define your logger as public constants. 
Create the hierarchy with `getLogger(Logger parent, String name)`.
This protects your logging configuration against refactoring as the logger structure is preserved.

For those who still prefer the traditional convention, the original `getLogger(...)` methods were preserved for compatibility.
The `getLogger(Class<?> clazz, String name)` helps to fine tune loggers for specific operations or features of interest provided by your class. 
For example, you could define loggers to separately track `open()` and `close()` methods.

## Operation demarcation and performance measure ##

*Most logging messages intend telling that an operation of interest succeeds of failed. Sometimes, the beginning of the operation is logged too. Further, the performance concerned want log execution count and time. Some are interested on additional input/output data of the operation, of state that influences the operation. Others want to know how system resources were affected by an operation.*

The **Meter** is a special logger that address all these requirements. It clearly delimiters the beginning and ending of an operation, distinguishing success from failure. It counts executions and records its time. It tracks input/output data and relevant state. Further, it traces relevant system resources before and after the operation. The log messages are parsable for later comprehensive analysis. The level of the logger associated to the operation controls the amount of information.

## Similar projects ##

 * [Perf4J](http://perf4j.codehaus.org/) Appearantly discotinued project.
 * [Spped4J](http://perf4j.codehaus.org/) A continuation and enhancement of Perf4J.
