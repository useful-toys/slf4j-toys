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
package org.usefultoys.slf4j.meter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.impl.TestLogger;
import org.usefultoys.slf4j.LoggerFactory;

/**
 *
 * @author Daniel Felix Ferber
 */
public class MeterStateAttributesTest {

    TestLogger logger = (TestLogger) LoggerFactory.getLogger("Test");

    public MeterStateAttributesTest() {
        logger.setEnabled(false);
    }

    @Before
    public void clearEvents() {
        logger.clearEvents();
    }

    @Test
    public void testTimeAttributesOk() {
        final Meter m = new Meter(logger);

        Assert.assertFalse(m.isStarted());
        Assert.assertFalse(m.isStopped());
        Assert.assertFalse(m.isOK());
        Assert.assertFalse(m.isReject());
        Assert.assertFalse(m.isFail());
        Assert.assertNull(m.getOkPath());
        Assert.assertNull(m.getRejectPath());
        Assert.assertNull(m.getFailMessage());
        Assert.assertNull(m.getFailPath());

        m.start();

        Assert.assertTrue(m.isStarted());
        Assert.assertFalse(m.isStopped());
       Assert.assertFalse(m.isOK());
        Assert.assertFalse(m.isReject());
        Assert.assertFalse(m.isFail());
        Assert.assertNull(m.getOkPath());
        Assert.assertNull(m.getRejectPath());
        Assert.assertNull(m.getFailMessage());
        Assert.assertNull(m.getFailPath());

        m.ok();

        Assert.assertTrue(m.isStarted());
        Assert.assertTrue(m.isStopped());
        Assert.assertTrue(m.isOK());
        Assert.assertTrue(m.isStarted());
        Assert.assertFalse(m.isReject());
        Assert.assertFalse(m.isFail());
        Assert.assertNull(m.getOkPath());
        Assert.assertNull(m.getRejectPath());
        Assert.assertNull(m.getFailMessage());
        Assert.assertNull(m.getFailPath());
    }
    
    @Test
    public void testTimeAttributesOkWithFlow() {
        final Meter m = new Meter(logger);

        Assert.assertFalse(m.isStarted());
        Assert.assertFalse(m.isStopped());
       Assert.assertFalse(m.isOK());
        Assert.assertFalse(m.isReject());
        Assert.assertFalse(m.isFail());
        Assert.assertNull(m.getOkPath());
        Assert.assertNull(m.getRejectPath());
        Assert.assertNull(m.getFailMessage());
        Assert.assertNull(m.getFailPath());

        m.start();

        Assert.assertTrue(m.isStarted());
        Assert.assertFalse(m.isStopped());
       Assert.assertFalse(m.isOK());
        Assert.assertFalse(m.isReject());
        Assert.assertFalse(m.isFail());
        Assert.assertNull(m.getOkPath());
        Assert.assertNull(m.getRejectPath());
        Assert.assertNull(m.getFailMessage());
        Assert.assertNull(m.getFailPath());

        m.ok("Flow");

        Assert.assertTrue(m.isStarted());
        Assert.assertTrue(m.isStopped());
        Assert.assertTrue(m.isOK());
        Assert.assertFalse(m.isReject());
        Assert.assertFalse(m.isFail());
        Assert.assertEquals("Flow", m.getOkPath());
        Assert.assertNull(m.getRejectPath());
        Assert.assertNull(m.getFailMessage());
        Assert.assertNull(m.getFailPath());
    }
  
    @Test
    public void testTimeAttributesFlow() {
        final Meter m = new Meter(logger);

        Assert.assertFalse(m.isStarted());
        Assert.assertFalse(m.isStopped());
       Assert.assertFalse(m.isOK());
        Assert.assertFalse(m.isReject());
        Assert.assertFalse(m.isFail());
        Assert.assertNull(m.getOkPath());
        Assert.assertNull(m.getRejectPath());
        Assert.assertNull(m.getFailMessage());
        Assert.assertNull(m.getFailPath());

        m.start();

        Assert.assertTrue(m.isStarted());
        Assert.assertFalse(m.isStopped());
        Assert.assertFalse(m.isOK());
        Assert.assertFalse(m.isReject());
        Assert.assertFalse(m.isFail());
        Assert.assertNull(m.getOkPath());
        Assert.assertNull(m.getRejectPath());
        Assert.assertNull(m.getFailMessage());
        Assert.assertNull(m.getFailPath());

        m.path("Flow");

        Assert.assertTrue(m.isStarted());
        Assert.assertFalse(m.isStopped());
        Assert.assertFalse(m.isOK());
        Assert.assertFalse(m.isReject());
        Assert.assertFalse(m.isFail());
        Assert.assertEquals("Flow", m.getOkPath());
        Assert.assertNull(m.getRejectPath());
        Assert.assertNull(m.getFailMessage());
        Assert.assertNull(m.getFailPath());

        m.ok();

        Assert.assertTrue(m.isStarted());
        Assert.assertTrue(m.isStopped());
        Assert.assertTrue(m.isOK());
        Assert.assertFalse(m.isReject());
        Assert.assertFalse(m.isFail());
        Assert.assertEquals("Flow", m.getOkPath());
        Assert.assertNull(m.getRejectPath());
        Assert.assertNull(m.getFailMessage());
        Assert.assertNull(m.getFailPath());
    }
      
    @Test
    public void testTimeAttributesFlow2() {
        final Meter m = new Meter(logger);

        Assert.assertFalse(m.isStarted());
        Assert.assertFalse(m.isStopped());
        Assert.assertFalse(m.isOK());
        Assert.assertFalse(m.isReject());
        Assert.assertFalse(m.isFail());
        Assert.assertNull(m.getOkPath());
        Assert.assertNull(m.getRejectPath());
        Assert.assertNull(m.getFailMessage());
        Assert.assertNull(m.getFailPath());

        m.start();

        Assert.assertTrue(m.isStarted());
        Assert.assertFalse(m.isStopped());
       Assert.assertFalse(m.isOK());
        Assert.assertFalse(m.isReject());
        Assert.assertFalse(m.isFail());
        Assert.assertNull(m.getOkPath());
        Assert.assertNull(m.getRejectPath());
        Assert.assertNull(m.getFailMessage());
        Assert.assertNull(m.getFailPath());

        m.path("Flow");

        Assert.assertTrue(m.isStarted());
        Assert.assertFalse(m.isStopped());
        Assert.assertFalse(m.isOK());
        Assert.assertFalse(m.isReject());
        Assert.assertFalse(m.isFail());
        Assert.assertEquals("Flow", m.getOkPath());
        Assert.assertNull(m.getRejectPath());
        Assert.assertNull(m.getFailMessage());
        Assert.assertNull(m.getFailPath());

        m.path("Path");

        Assert.assertTrue(m.isStarted());
        Assert.assertFalse(m.isStopped());
        Assert.assertFalse(m.isOK());
        Assert.assertFalse(m.isReject());
        Assert.assertFalse(m.isFail());
        Assert.assertEquals("Path", m.getOkPath());
        Assert.assertNull(m.getRejectPath());
        Assert.assertNull(m.getFailMessage());
        Assert.assertNull(m.getFailPath());

    	m.ok();

        Assert.assertTrue(m.isStarted());
        Assert.assertTrue(m.isStopped());
        Assert.assertTrue(m.isOK());
        Assert.assertFalse(m.isReject());
        Assert.assertFalse(m.isFail());
        Assert.assertEquals("Path", m.getOkPath());
        Assert.assertNull(m.getRejectPath());
        Assert.assertNull(m.getFailMessage());
        Assert.assertNull(m.getFailPath());
    }
      
    @Test
    public void testTimeAttributesReject() {
        final Meter m = new Meter(logger);

        Assert.assertFalse(m.isStarted());
        Assert.assertFalse(m.isStopped());
       Assert.assertFalse(m.isOK());
        Assert.assertFalse(m.isReject());
        Assert.assertFalse(m.isFail());
        Assert.assertNull(m.getOkPath());
        Assert.assertNull(m.getRejectPath());
        Assert.assertNull(m.getFailMessage());
        Assert.assertNull(m.getFailPath());

        m.start();

        Assert.assertTrue(m.isStarted());
        Assert.assertFalse(m.isStopped());
      Assert.assertFalse(m.isOK());
        Assert.assertFalse(m.isReject());
        Assert.assertFalse(m.isFail());
        Assert.assertNull(m.getOkPath());
        Assert.assertNull(m.getRejectPath());
        Assert.assertNull(m.getFailMessage());
        Assert.assertNull(m.getFailPath());

        m.reject("Reject");

        Assert.assertTrue(m.isStarted());
        Assert.assertTrue(m.isStopped());
        Assert.assertFalse(m.isOK());
        Assert.assertTrue(m.isReject());
        Assert.assertFalse(m.isFail());
        Assert.assertNull(m.getOkPath());
        Assert.assertEquals("Reject", m.getRejectPath());
        Assert.assertNull(m.getFailMessage());
        Assert.assertNull(m.getFailPath());
    }

    @Test
    public void testTimeAttributesFail() {
        final Meter m = new Meter(logger);

        Assert.assertFalse(m.isStarted());
        Assert.assertFalse(m.isStopped());
        Assert.assertFalse(m.isOK());
        Assert.assertFalse(m.isReject());
        Assert.assertFalse(m.isFail());
        Assert.assertNull(m.getOkPath());
        Assert.assertNull(m.getRejectPath());
        Assert.assertNull(m.getFailMessage());
        Assert.assertNull(m.getFailPath());

        m.start();

        Assert.assertTrue(m.isStarted());
        Assert.assertFalse(m.isStopped());
       Assert.assertFalse(m.isOK());
        Assert.assertFalse(m.isReject());
        Assert.assertFalse(m.isFail());
        Assert.assertNull(m.getOkPath());
        Assert.assertNull(m.getRejectPath());
        Assert.assertNull(m.getFailMessage());
        Assert.assertNull(m.getFailPath());

        m.fail(new IllegalStateException("ISE"));

        Assert.assertTrue(m.isStarted());
        Assert.assertTrue(m.isStopped());
        Assert.assertFalse(m.isOK());
        Assert.assertFalse(m.isReject());
        Assert.assertTrue(m.isFail());
        Assert.assertNull(m.getOkPath());
        Assert.assertNull(m.getRejectPath());
        Assert.assertEquals("ISE", m.getFailMessage());
        Assert.assertEquals("java.lang.IllegalStateException", m.getFailPath());
    }
 }
