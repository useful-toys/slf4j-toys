/*
 * Copyright 2024 Daniel Felix Ferber
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
package org.usefultoys.slf4j.examples;

import org.slf4j.Logger;
import org.usefultoys.slf4j.LoggerFactory;
import org.usefultoys.slf4j.meter.Meter;
import org.usefultoys.slf4j.meter.MeterFactory;

/**
 *
 * @author Daniel Felix Ferber
 */
public class BasicUsageTest {

    int a;

    public void before() {
        a = 12;
    }

    public void test1() {
        final Meter m = MeterFactory.getMeter("teste").ctx("in", a).start();
        try {
            a++;
            m.ctx("out", a).ok();
        } catch (final RuntimeException e) {
            m.ctx("error", a).fail(e);
        }
    }

    public void test2() {
        final Meter m = MeterFactory.getMeter("teste").ctx("in", a).start();
        try {
            a /= 0;
            m.ctx("out", a).ok();
        } catch (final RuntimeException e) {
            m.ctx("error", a).fail(e);
        }
    }

    public void test3() {
        final Meter m = MeterFactory.getMeter("teste").ctx("in", a).start();
        try {
            Meter m2 = m.sub("add").start();
            a += 5;
            m2.ok();

            final Meter m3 = m2.sub("mul").start();
            a *= 5;
            m3.ok();

            for (int i = 0; i < 3; i++) {
                m2 = m.sub("sub").start();
                a -= 5;
                m2.ok();
            }

            m2 = m.sub("div").start();
            a /= 5;
            m2.ok();

            m.ctx("out", a).ok();
        } catch (final RuntimeException e) {
            m.ctx("error", a).fail(e);
        }
    }

    public void test4() {
        final Logger logger = LoggerFactory.getLogger(BasicUsageTest.class);
        final Meter m = MeterFactory.getMeter(logger, "test4").ctx("in", a).start();

        try {
            Meter m2 = m.sub("add").start();
            a += 5;
            m2.ok();

            final Meter m3 = m2.sub("mul").start();
            a *= 5;
            m3.ok();

            for (int i = 0; i < 3; i++) {
                m2 = m.sub("sub").start();
                a -= 5;
                m2.ok();
            }

            m2 = m.sub("div").start();
            a /= 5;
            m2.ok();

            m.ctx("out", a).ok();
        } catch (final RuntimeException e) {
            m.ctx("error", a).fail(e);
        }
    }
}
