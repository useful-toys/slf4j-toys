package org.usefultoys.jul.internal;

/**
 * Collection of properties that drive {@link SystemData} behavior.
 * 
 * @author Daniel Felix Ferber
 *
 */
public class SystemConfig {
	/**
	 * If Sun native OperatingSystemMXBean is available.
	 */
	static final boolean hasSunOperatingSystemMXBean;

	static {
		boolean tmpHasSunOperatingSystemMXBean = false;
		try {
			Class.forName("com.sun.management.OperatingSystemMXBean");
			tmpHasSunOperatingSystemMXBean = true;
		} catch (ClassNotFoundException ignored) {
			// ignora
		}
		hasSunOperatingSystemMXBean = tmpHasSunOperatingSystemMXBean;
	}
	
    /**
     * If memory usage status is retrieved from MemoryMXBean.
     * Not all JVM may support or allow MemoryMXBean usage.
     * Value is read from system property {@code jultoys.useMemoryManagedBean} at application startup, defaults to {@code false}.
     * You may assign a new value at runtime.
     */
    public static boolean useMemoryManagedBean = Config.getProperty("jultoys.useMemoryManagedBean", false);
    /**
     * If class loading status is retrieved from ClassLoadingMXBean.
     * Not all JVM may support or allow ClassLoadingMXBean usage.
     * Value is read from system property {@code jultoys.useClassLoadingManagedBean} at application startup, defaults to {@code false}.
     * You may assign a new value at runtime.
     */
    public static boolean useClassLoadingManagedBean = Config.getProperty("jultoys.useClassLoadingManagedBean", false);
    /**
     * If JIT compiler status is retrieved from CompilationMXBean.
     * Not all JVM may support or allow CompilationMXBean usage.
     * Value is read from system property {@code jultoys.useCompilationManagedBean} at application startup, defaults to {@code false}.
     * You may assign a new value at runtime.
     */
    public static boolean useCompilationManagedBean = Config.getProperty("jultoys.useCompilationManagedBean", false);
    /**
     * If garbage collector status is retrieved from GarbageCollectorMXBean.
     * Not all JVM may support or allow GarbageCollectorMXBean usage.
     * Value is read from system property {@code jultoys.useGarbageCollectionManagedBean} at application startup, defaults to {@code false}.
     * You may assign a new value at runtime.
     */
    public static boolean useGarbageCollectionManagedBean = Config.getProperty("jultoys.useGarbageCollectionManagedBean", false);
    /**
     * If operating system status is retrieved from OperatingSystemMXBean.
     * Not all JVM may support or allow OperatingSystemMXBean usage.
     * Value is read from system property {@code jultoys.usePlatformManagedBean} at application startup, defaults to {@code false}.
     * You may assign a new value at runtime.
     */
    public static boolean usePlatformManagedBean = Config.getProperty("jultoys.usePlatformManagedBean", false);

}
