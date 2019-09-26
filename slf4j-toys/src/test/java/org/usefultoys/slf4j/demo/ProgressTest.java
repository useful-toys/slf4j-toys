/*
 * Copyright 2019 Daniel Felix Ferber
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
package org.usefultoys.slf4j.demo;

import org.junit.Test;
import org.usefultoys.slf4j.meter.Meter;
import org.usefultoys.slf4j.meter.MeterFactory;

/**
 *
 * @author Daniel Felix Ferber
 */
public class ProgressTest {

    @Test
    public void testFastProgress() {
        final Meter m = MeterFactory.getMeter("teste").iterations(20).start();
        for (int i = 0; i < 20; i++) {
            m.inc().progress();
            try {
                Thread.sleep(500);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }
        m.ok();
    }

    @Test
    public void testFastProgressLargeMeterProgressPeriod() {
        System.setProperty("meter.progress.period", "5s");
        final Meter m = MeterFactory.getMeter("teste").iterations(20).start();
        for (int i = 0; i < 20; i++) {
            m.inc().progress();
            try {
                Thread.sleep(500);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }
        m.ok();
    }

    @Test
    public void testFastProgressSmallMeterProgressPeriod() {
        System.setProperty("meter.progress.period", "500ms");
        final Meter m = MeterFactory.getMeter("teste").iterations(20).start();
        for (int i = 0; i < 20; i++) {
            m.inc().progress();
            try {
                Thread.sleep(500);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }
        m.ok();
    }
}
