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
package org.usefultoys.slf4j.profiler.meter;

import org.junit.Test;
import org.usefultoys.slf4j.logger.LoggerFactory;
import org.usefultoys.slf4j.profiler.meter.Meter;
import org.usefultoys.slf4j.profiler.meter.MeterFactory;

/**
 *
 * @author Daniel Felix Ferber
 */
public class MeterUseCaseTestMisuseTest {

    @Test
    public void testMeterConfirmedButNotStarted() {
        final Meter m = MeterFactory.getMeter("teste");
        m.ok();
    }

    @Test
    public void testMeterAlreadyRefusedOrConfirmed1() {
        final Meter m = MeterFactory.getMeter("teste").start();
        m.ok();
        m.ok();
    }

    @Test
    public void testMeterAlreadyRefusedOrConfirmed2() {
        final Meter m = MeterFactory.getMeter("teste").start();
        m.ok();
        m.fail();
    }

    @Test
    public void testMeterRefusedButNotStarted() {
        final Meter m = MeterFactory.getMeter("teste");
        m.fail();
    }

    @Test
    public void testMeterAlreadyRefusedOrConfirmed3() {
        final Meter m = MeterFactory.getMeter("teste").start();
        m.fail();
        m.fail();
    }

    @Test
    public void testMeterAlreadyRefusedOrConfirmed4() {
        final Meter m = MeterFactory.getMeter("teste").start();
        m.fail();
        m.ok();
    }

    @Test
    public void testMeterAlreadyStarted() {
        final Meter m = MeterFactory.getMeter("teste").start().start();
        m.ok();
    }

    @Test
    public void testMeterNotRefusedNorConfirmed() throws InterruptedException {
        subMeterX();
        // Wait and force garbage colletor to finalize meter
        System.gc();
        Thread.sleep(1000);
        System.gc();
        Thread.sleep(1000);
        System.gc();
    }

    private void subMeterX() {
        final Meter m = MeterFactory.getMeter("teste").start();
    }

    @Test
    public void testMeterInternalException() {
        final Meter m = new Meter(LoggerFactory.getLogger("teste")) {
            /**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
            protected void collectSystemStatus() {
                throw new RuntimeException();
            }
        };
        m.start();
        m.ok();
    }

    @Test
    public void testIllegalCallSub() {
        final Meter m = MeterFactory.getMeter("teste").start();
        final Meter m2 = m.sub(null).start();
        m2.ok();
        m.ok();
    }

    @Test
    public void testIllegalCallM() {
        final Meter m = MeterFactory.getMeter("teste").m(null).start().ok();
    }

    @Test
    public void testIllegalCallM2() {
        final Meter m = MeterFactory.getMeter("teste").m(null, "abc").start().ok();
    }

    @Test
    public void testIllegalCallM3() {
        final Meter m = MeterFactory.getMeter("teste").m(null, "%d", 0.0f).start().ok();
    }

    @Test
    public void testIllegalCallLimitMilliseconds() {
        final Meter m = MeterFactory.getMeter("teste").limitMilliseconds(-10).start().ok();
    }

    @Test
    public void testIllegalCallIterations() {
        final Meter m = MeterFactory.getMeter("teste").iterations(-10).start().ok();
    }

    @Test
    public void testIllegalCallCtx0() {
        final Meter m = MeterFactory.getMeter("teste").ctx(null).start().ok();
    }

    @Test
    public void testIllegalCallCtx1() {
        final Meter m = MeterFactory.getMeter("teste").ctx(null, 0).start().ok();
    }

    @Test
    public void testIllegalCallCtx2() {
        final Meter m = MeterFactory.getMeter("teste").ctx(null, 0L).start().ok();
    }

    @Test
    public void testIllegalCallCtx3() {
        final Meter m = MeterFactory.getMeter("teste").ctx(null, "s").start().ok();
    }

    @Test
    public void testIllegalCallCtx4() {
        final Meter m = MeterFactory.getMeter("teste").ctx(null, true).start().ok();
    }

    @Test
    public void testIllegalCallCtx5() {
        final Meter m = MeterFactory.getMeter("teste").ctx(null, 0.0f).start().ok();
    }

    @Test
    public void testIllegalCallCtx6() {
        final Meter m = MeterFactory.getMeter("teste").ctx(null, 0.0).start().ok();
    }

    @Test
    public void testIllegalCallCtx7() {
        final Meter m = MeterFactory.getMeter("teste").ctx(null, Integer.valueOf(0)).start().ok();
    }

    @Test
    public void testIllegalCallCtx8() {
        final Meter m = MeterFactory.getMeter("teste").ctx(null, Long.valueOf(0)).start().ok();
    }

    @Test
    public void testIllegalCallCtx9() {
        final Meter m = MeterFactory.getMeter("teste").ctx(null, Boolean.FALSE).start().ok();
    }

    @Test
    public void testIllegalCallCtx10() {
        final Meter m = MeterFactory.getMeter("teste").ctx(null, Float.valueOf(0)).start().ok();
    }

    @Test
    public void testIllegalCallCtx11() {
        final Meter m = MeterFactory.getMeter("teste").ctx(null, Double.valueOf(0)).start().ok();
    }

    @Test
    public void testIllegalCallCtx12() {
        final Meter m = MeterFactory.getMeter("teste").ctx(null, "a", "b").start().ok();
    }

    @Test
    public void testIllegalCallCtx13() {
        final Meter m = MeterFactory.getMeter("teste").ctx("a", null, "b").start().ok();
    }

    @Test
    public void testIllegalCallCtx14() {
        final Meter m = MeterFactory.getMeter("teste").ctx("a", "%d", 0.0f).start().ok();
    }

    @Test
    public void testIllegalCallIncBy() {
        final Meter m = MeterFactory.getMeter("teste").incBy(-10).start().ok();
    }

    @Test
    public void testIllegalCallIncTo0() {
        final Meter m = MeterFactory.getMeter("teste").incTo(-10).start().ok();
    }

    @Test
    public void testIllegalCallIncTo1() {
        final Meter m = MeterFactory.getMeter("teste").incTo(10).incTo(5).start().ok();
    }
}
