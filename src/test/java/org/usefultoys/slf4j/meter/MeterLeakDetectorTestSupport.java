/*
 * Copyright 2026 Daniel Felix Ferber
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.usefultoys.slf4j.meter;

/**
 * Public bridge that lets test infrastructure outside the {@code org.usefultoys.slf4j.meter} package
 * reset the {@link MeterLeakDetector}'s process-wide state between tests.
 * <p>
 * The detector itself is intentionally package-private; this class exists only in test sources and
 * exposes a single, clearly-named cleanup operation. It must never be called from production code.
 *
 * @author Daniel Felix Ferber
 */
public final class MeterLeakDetectorTestSupport {

    private MeterLeakDetectorTestSupport() {
        // Utility holder, not instantiable.
    }

    /**
     * Discards every registered or pending leak-detection reference without reporting anything.
     * Intended for test isolation only — calling it from production code would hide real leaks.
     */
    public static void clear() {
        MeterLeakDetector.clearForTests();
    }
}
