/* 
 * Copyright 2015 Daniel Felix Ferber.
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
package br.com.danielferber.slf4jtoys.slf4j.demo;

import br.com.danielferber.slf4jtoys.slf4j.logger.LoggerFactory;
import br.com.danielferber.slf4jtoys.slf4j.profiler.meter.Meter;
import br.com.danielferber.slf4jtoys.slf4j.profiler.meter.MeterFactory;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

/**
 *
 * @author Daniel Felix Ferber
 */
public class BasicUsageTest {

    int a;

    @Before
    public void before() {
        a = 12;
    }

    @Test
    public void test1() {
        final Meter m = MeterFactory.getMeter("teste").ctx("in", a).start();
        try {
            a++;
        } catch (final RuntimeException e) {
            m.ctx("error", a).fail(e);
        }
        m.ctx("out", a).ok();
    }

    @Test
    public void test2() {
        final Meter m = MeterFactory.getMeter("teste").ctx("in", a).start();
        try {
            a /= 0;
        } catch (final RuntimeException e) {
            m.ctx("error", a).fail(e);
        }
        m.ctx("out", a).ok();
    }

    @Test
    public void test3() {
        final Meter m = MeterFactory.getMeter("teste").ctx("in", a).start();

        try {
            Meter m2 = m.sub("add").start();
            a += 5;
            m2.ok();

            Meter m3 = m2.sub("mul").start();
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
        } catch (final RuntimeException e) {
            m.ctx("error", a).fail(e);
        }
        m.ctx("out", a).ok();
    }

        @Test
    public void test4() {
        Logger logger = LoggerFactory.getLogger(BasicUsageTest.class);
        final Meter m = MeterFactory.getMeter(logger, "test4").ctx("in", a).start();

        try {
            Meter m2 = m.sub("add").start();
            a += 5;
            m2.ok();

            Meter m3 = m2.sub("mul").start();
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
        } catch (final RuntimeException e) {
            m.ctx("error", a).fail(e);
        }
        m.ctx("out", a).ok();
    }

}
