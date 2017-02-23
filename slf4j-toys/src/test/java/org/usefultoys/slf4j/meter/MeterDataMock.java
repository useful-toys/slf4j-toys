/**
 * Copyright 2017 Daniel Felix Ferber
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.usefultoys.slf4j.meter;

/**
 *
 * @author Daniel Felix Ferber
 */
public class MeterDataMock extends MeterData {

    private static final long serialVersionUID = 1L;

    public MeterDataMock() {
    }

    public long getHeap_commited() {
        return heap_commited;
    }

    public void setHeap_commited(final long heap_commited) {
        this.heap_commited = heap_commited;
    }

    public long getHeap_max() {
        return heap_max;
    }

    public void setHeap_max(final long heap_max) {
        this.heap_max = heap_max;
    }

    public long getHeap_used() {
        return heap_used;
    }

    public void setHeap_used(final long heap_used) {
        this.heap_used = heap_used;
    }

    public long getNonHeap_commited() {
        return nonHeap_commited;
    }

    public void setNonHeap_commited(final long nonHeap_commited) {
        this.nonHeap_commited = nonHeap_commited;
    }

    public long getNonHeap_max() {
        return nonHeap_max;
    }

    public void setNonHeap_max(final long nonHeap_max) {
        this.nonHeap_max = nonHeap_max;
    }

    public long getNonHeap_used() {
        return nonHeap_used;
    }

    public void setNonHeap_used(final long nonHeap_used) {
        this.nonHeap_used = nonHeap_used;
    }

    public long getObjectPendingFinalizationCount() {
        return objectPendingFinalizationCount;
    }

    public void setObjectPendingFinalizationCount(final long objectPendingFinalizationCount) {
        this.objectPendingFinalizationCount = objectPendingFinalizationCount;
    }

    public long getClassLoading_loaded() {
        return classLoading_loaded;
    }

    public void setClassLoading_loaded(final long classLoading_loaded) {
        this.classLoading_loaded = classLoading_loaded;
    }

    public long getClassLoading_total() {
        return classLoading_total;
    }

    public void setClassLoading_total(final long classLoading_total) {
        this.classLoading_total = classLoading_total;
    }

    public long getClassLoading_unloaded() {
        return classLoading_unloaded;
    }

    public void setClassLoading_unloaded(final long classLoading_unloaded) {
        this.classLoading_unloaded = classLoading_unloaded;
    }

    public long getCompilationTime() {
        return compilationTime;
    }

    public void setCompilationTime(final long compilationTime) {
        this.compilationTime = compilationTime;
    }

    public long getGarbageCollector_count() {
        return garbageCollector_count;
    }

    public void setGarbageCollector_count(final long garbageCollector_count) {
        this.garbageCollector_count = garbageCollector_count;
    }

    public long getGarbageCollector_time() {
        return garbageCollector_time;
    }

    public void setGarbageCollector_time(final long garbageCollector_time) {
        this.garbageCollector_time = garbageCollector_time;
    }

    public long getRuntime_usedMemory() {
        return runtime_usedMemory;
    }

    public void setRuntime_usedMemory(final long runtime_usedMemory) {
        this.runtime_usedMemory = runtime_usedMemory;
    }

    public long getRuntime_maxMemory() {
        return runtime_maxMemory;
    }

    public void setRuntime_maxMemory(final long runtime_maxMemory) {
        this.runtime_maxMemory = runtime_maxMemory;
    }

    public long getRuntime_totalMemory() {
        return runtime_totalMemory;
    }

    public void setRuntime_totalMemory(final long runtime_totalMemory) {
        this.runtime_totalMemory = runtime_totalMemory;
    }

    public double getSystemLoad() {
        return systemLoad;
    }

    public void setSystemLoad(final double systemLoad) {
        this.systemLoad = systemLoad;
    }

    @Override
    public String getSessionUuid() {
        return sessionUuid;
    }

    public void setSessionUuid(final String sessionUuid) {
        this.sessionUuid = sessionUuid;
    }

    @Override
    public String getEventCategory() {
        return eventCategory;
    }

    public void setEventCategory(final String eventCategory) {
        this.eventCategory = eventCategory;
    }

    @Override
    public long getEventPosition() {
        return eventPosition;
    }

    public void setEventPosition(final long eventPosition) {
        this.eventPosition = eventPosition;
    }

    @Override
    public long getTime() {
        return time;
    }

    public void setTime(final long time) {
        this.time = time;
    }

}
