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
package br.com.danielferber.slf4jtoys.slf4j.profiler.meter;

import br.com.danielferber.slf4jtoys.slf4j.logger.LoggerFactory;
import java.util.Random;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;

/**
 *
 * @author Daniel Felix Ferber
 */
public class MeterUseCaseTest {

    final String meterName = "name";
    final Logger logger = LoggerFactory.getLogger(meterName);

    public MeterUseCaseTest() {
    }

    @Test
    public void testOk() {
        System.out.println("testOk:");
        final Meter m = new Meter(logger).m("Example of execution that succeeds.").start();
        try {
            /* Run stuff that may fail. */
            if (1 + 2 == 4) {
                throw new Exception("message");
            }
            m.ok();
        } catch (Exception e) {
            m.fail(e);
            // may rethrow
        }
        Assert.assertTrue(m.isSuccess());
        Assert.assertFalse(m.isSlow());
    }

    @Test
    public void testFail() {
        System.out.println("testFail:");
        final Meter m = new Meter(logger).m("Example of execution that fails.").start();
        try {
            /* Run stuff that may fail. */
            if (1 + 2 == 3) {
                throw new Exception("message");
            }
            m.ok();
        } catch (Exception e) {
            m.fail(e);
            // may rethrow
        }
        Assert.assertFalse(m.isSuccess());
        Assert.assertFalse(m.isSlow());
    }

    @Test
    public void testSlownessNo() {
        System.out.println("testSlownessNo:");
        final Meter m = new Meter(logger).m("Example of execution that succeeds within time limit.").limitMilliseconds(200).start();
        try {
            /* Run stuff that may delay. */
            Thread.sleep(1);
            m.ok();
        } catch (Exception e) {
            m.fail(e);
            // may rethrow
        }
        Assert.assertTrue(m.isSuccess());
        Assert.assertFalse(m.isSlow());
    }

    @Test
    public void testSlownessYes() {
        System.out.println("testSlownessYes:");
        final Meter m = new Meter(logger).m("Example of execution that succeeds but exceeds time limit.").limitMilliseconds(200).start();
        try {
            /* Run stuff that may delay. */
            Thread.sleep(220);
            m.ok();
        } catch (Exception e) {
            m.fail(e);
            // may rethrow
        }
        Assert.assertTrue(m.isSuccess());
        Assert.assertTrue(m.isSlow());
    }

    @Test
    public void testIteration() {
        System.out.println("testIteration:");
        final Meter m = new Meter(logger).m("Example of execution that succeeds and reports progress of completed iterations.").iterations(10).start();
        try {
            /* Run stuff that repeats. */
            for (int i = 0; i < 10; i++) {
                /* Do stuff. */
                m.inc().progress();
            }
            m.ok();
        } catch (Exception e) {
            m.fail(e);
            // may rethrow
        }
        Assert.assertTrue(m.isSuccess());
        Assert.assertFalse(m.isSlow());
    }

    @Test
    public void testIteration2() {
        System.out.println("testIteration2:");
        final Meter m = new Meter(logger).m("Example of execution that succeeds and reports progress of completed iterations.").iterations(30).start();
        Random r = new Random();
        try {
            /* Run stuff that repeats, step size may vary. */
            for (int i = 0; i < 30;) {
                /* Do stuff. */
                int increment = r.nextInt(3) + 1;
                i += increment;
                m.incBy(increment).progress();
            }
            m.ok();
        } catch (Exception e) {
            m.fail(e);
            // may rethrow
        }
        Assert.assertTrue(m.isSuccess());
        Assert.assertFalse(m.isSlow());
    }

    @Test
    public void testIteration3() {
        System.out.println("testIteration3:");
        final Meter m = new Meter(logger).m("Example of execution that succeeds and reports progress of completed iterations.").iterations(30).start();
        Random r = new Random();
        try {
            /* Run stuff that repeats, step size may vary. */
            for (int i = 0; i < 30;) {
                /* Do stuff. */
                int increment = r.nextInt(3) + 1;
                i += increment;
                m.incBy(i).progress();
            }
            m.ok();
        } catch (Exception e) {
            m.fail(e);
            // may rethrow
        }
        Assert.assertTrue(m.isSuccess());
        Assert.assertFalse(m.isSlow());
    }

    @Test
    public void testSlownessContext() {
        System.out.println("testSlownessContext:");

        final Meter m = new Meter(logger).m("Example of execution that succeeds and reports context.").limitMilliseconds(200);
        m.ctx("input", "for example, an value received as input").start();
        try {
            /* Run stuff. */
            m.ctx("detail", "for example, an value calculared during execution");
            m.ctx("extra", "for example, nother value calculared during execution");
            /* Run stuff. */
            m.ctx("extra", "for example, an value overriden during execution");
            /* Run stuff. */
            m.unctx("extra");
            /* Run stuff. */
            Thread.sleep(1);
            m.ctx("output", "for example, an value produced as output").ok();
        } catch (Exception e) {
            m.ctx("cause", "for example, an identifier for the failure cause").fail(e);
            // may rethrow
        }
        Assert.assertTrue(m.isSuccess());
        Assert.assertFalse(m.isSlow());
    }

}
