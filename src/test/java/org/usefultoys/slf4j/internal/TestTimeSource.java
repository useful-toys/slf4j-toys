/*
 * Copyright 2025 Daniel Felix Ferber
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

package org.usefultoys.slf4j.internal;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * Test implementation of TimeSource for deterministic testing.
 */
@NoArgsConstructor
@AllArgsConstructor
public class TestTimeSource implements TimeSource {
    private long currentNanoTime = 0;

    public static final long DAY1 = 1_735_689_600_000_000_000L;

    public void setNanoTime(final long nanoTime) {
        currentNanoTime = nanoTime;
    }

    public void advanceMiliseconds(final long delta) {
        currentNanoTime += delta * 1000 * 1000; // Convert milliseconds to nanoseconds
    }

    @Override
    public long nanoTime() {
        return currentNanoTime;
    }
}
