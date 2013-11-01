/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.danielferber.slf4jtoys.slf4j.profiler.meter;

/**
 *
 * @author X7WS
 */
public class PublicMeterData extends MeterData {

    public PublicMeterData() {
        super();
    }

    public long getHeap_commited() {
        return heap_commited;
    }

    public void setHeap_commited(long heap_commited) {
        this.heap_commited = heap_commited;
    }

    public long getHeap_init() {
        return heap_init;
    }

    public void setHeap_init(long heap_init) {
        this.heap_init = heap_init;
    }

    public long getHeap_max() {
        return heap_max;
    }

    public void setHeap_max(long heap_max) {
        this.heap_max = heap_max;
    }

    public long getHeap_used() {
        return heap_used;
    }

    public void setHeap_used(long heap_used) {
        this.heap_used = heap_used;
    }

    public long getNonHeap_commited() {
        return nonHeap_commited;
    }

    public void setNonHeap_commited(long nonHeap_commited) {
        this.nonHeap_commited = nonHeap_commited;
    }

    public long getNonHeap_init() {
        return nonHeap_init;
    }

    public void setNonHeap_init(long nonHeap_init) {
        this.nonHeap_init = nonHeap_init;
    }

    public long getNonHeap_max() {
        return nonHeap_max;
    }

    public void setNonHeap_max(long nonHeap_max) {
        this.nonHeap_max = nonHeap_max;
    }

    public long getNonHeap_used() {
        return nonHeap_used;
    }

    public void setNonHeap_used(long nonHeap_used) {
        this.nonHeap_used = nonHeap_used;
    }

    public long getObjectPendingFinalizationCount() {
        return objectPendingFinalizationCount;
    }

    public void setObjectPendingFinalizationCount(long objectPendingFinalizationCount) {
        this.objectPendingFinalizationCount = objectPendingFinalizationCount;
    }

    public long getClassLoading_loaded() {
        return classLoading_loaded;
    }

    public void setClassLoading_loaded(long classLoading_loaded) {
        this.classLoading_loaded = classLoading_loaded;
    }

    public long getClassLoading_total() {
        return classLoading_total;
    }

    public void setClassLoading_total(long classLoading_total) {
        this.classLoading_total = classLoading_total;
    }

    public long getClassLoading_unloaded() {
        return classLoading_unloaded;
    }

    public void setClassLoading_unloaded(long classLoading_unloaded) {
        this.classLoading_unloaded = classLoading_unloaded;
    }

    public long getCompilationTime() {
        return compilationTime;
    }

    public void setCompilationTime(long compilationTime) {
        this.compilationTime = compilationTime;
    }

    public long getGarbageCollector_count() {
        return garbageCollector_count;
    }

    public void setGarbageCollector_count(long garbageCollector_count) {
        this.garbageCollector_count = garbageCollector_count;
    }

    public long getGarbageCollector_time() {
        return garbageCollector_time;
    }

    public void setGarbageCollector_time(long garbageCollector_time) {
        this.garbageCollector_time = garbageCollector_time;
    }

    public long getRuntime_usedMemory() {
        return runtime_usedMemory;
    }

    public void setRuntime_usedMemory(long runtime_usedMemory) {
        this.runtime_usedMemory = runtime_usedMemory;
    }

    public long getRuntime_maxMemory() {
        return runtime_maxMemory;
    }

    public void setRuntime_maxMemory(long runtime_maxMemory) {
        this.runtime_maxMemory = runtime_maxMemory;
    }

    public long getRuntime_totalMemory() {
        return runtime_totalMemory;
    }

    public void setRuntime_totalMemory(long runtime_totalMemory) {
        this.runtime_totalMemory = runtime_totalMemory;
    }

    public double getSystemLoad() {
        return systemLoad;
    }

    public void setSystemLoad(double systemLoad) {
        this.systemLoad = systemLoad;
    }

    public String getSessionUuid() {
        return sessionUuid;
    }

    public void setSessionUuid(String sessionUuid) {
        this.sessionUuid = sessionUuid;
    }

    public String getEventCategory() {
        return eventCategory;
    }

    public void setEventCategory(String eventCategory) {
        this.eventCategory = eventCategory;
    }

    public long getEventPosition() {
        return eventPosition;
    }

    public void setEventPosition(long eventPosition) {
        this.eventPosition = eventPosition;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
    
    
}
