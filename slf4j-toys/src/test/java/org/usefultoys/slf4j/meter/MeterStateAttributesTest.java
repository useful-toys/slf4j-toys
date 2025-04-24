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
package org.usefultoys.slf4j.meter;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.impl.MockLogger;
import org.usefultoys.slf4j.LoggerFactory;
import org.usefultoys.slf4j.SessionConfig;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Daniel Felix Ferber
 */
public class MeterStateAttributesTest {

    MockLogger logger = (MockLogger) LoggerFactory.getLogger("Test");

    public MeterStateAttributesTest() {
        logger.setEnabled(false);
    }

    @BeforeAll
    public static void validate() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    @BeforeEach
    public void clearEvents() {
        logger.clearEvents();
    }

    @Test
    public void testTimeAttributesOk() {
        final Meter m = new Meter(logger);

        assertFalse(m.isStarted());
        assertFalse(m.isStopped());
        assertFalse(m.isOK());
        assertFalse(m.isReject());
        assertFalse(m.isFail());
        assertNull(m.getOkPath());
        assertNull(m.getRejectPath());
        assertNull(m.getFailMessage());
        assertNull(m.getFailPath());

        m.start();

        assertTrue(m.isStarted());
        assertFalse(m.isStopped());
       assertFalse(m.isOK());
        assertFalse(m.isReject());
        assertFalse(m.isFail());
        assertNull(m.getOkPath());
        assertNull(m.getRejectPath());
        assertNull(m.getFailMessage());
        assertNull(m.getFailPath());

        m.ok();

        assertTrue(m.isStarted());
        assertTrue(m.isStopped());
        assertTrue(m.isOK());
        assertTrue(m.isStarted());
        assertFalse(m.isReject());
        assertFalse(m.isFail());
        assertNull(m.getOkPath());
        assertNull(m.getRejectPath());
        assertNull(m.getFailMessage());
        assertNull(m.getFailPath());
    }
    
    @Test
    public void testTimeAttributesOkWithFlow() {
        final Meter m = new Meter(logger);

        assertFalse(m.isStarted());
        assertFalse(m.isStopped());
       assertFalse(m.isOK());
        assertFalse(m.isReject());
        assertFalse(m.isFail());
        assertNull(m.getOkPath());
        assertNull(m.getRejectPath());
        assertNull(m.getFailMessage());
        assertNull(m.getFailPath());

        m.start();

        assertTrue(m.isStarted());
        assertFalse(m.isStopped());
       assertFalse(m.isOK());
        assertFalse(m.isReject());
        assertFalse(m.isFail());
        assertNull(m.getOkPath());
        assertNull(m.getRejectPath());
        assertNull(m.getFailMessage());
        assertNull(m.getFailPath());

        m.ok("Flow");

        assertTrue(m.isStarted());
        assertTrue(m.isStopped());
        assertTrue(m.isOK());
        assertFalse(m.isReject());
        assertFalse(m.isFail());
        assertEquals("Flow", m.getOkPath());
        assertNull(m.getRejectPath());
        assertNull(m.getFailMessage());
        assertNull(m.getFailPath());
    }
  
    @Test
    public void testTimeAttributesFlow() {
        final Meter m = new Meter(logger);

        assertFalse(m.isStarted());
        assertFalse(m.isStopped());
       assertFalse(m.isOK());
        assertFalse(m.isReject());
        assertFalse(m.isFail());
        assertNull(m.getOkPath());
        assertNull(m.getRejectPath());
        assertNull(m.getFailMessage());
        assertNull(m.getFailPath());

        m.start();

        assertTrue(m.isStarted());
        assertFalse(m.isStopped());
        assertFalse(m.isOK());
        assertFalse(m.isReject());
        assertFalse(m.isFail());
        assertNull(m.getOkPath());
        assertNull(m.getRejectPath());
        assertNull(m.getFailMessage());
        assertNull(m.getFailPath());

        m.path("Flow");

        assertTrue(m.isStarted());
        assertFalse(m.isStopped());
        assertFalse(m.isOK());
        assertFalse(m.isReject());
        assertFalse(m.isFail());
        assertEquals("Flow", m.getOkPath());
        assertNull(m.getRejectPath());
        assertNull(m.getFailMessage());
        assertNull(m.getFailPath());

        m.ok();

        assertTrue(m.isStarted());
        assertTrue(m.isStopped());
        assertTrue(m.isOK());
        assertFalse(m.isReject());
        assertFalse(m.isFail());
        assertEquals("Flow", m.getOkPath());
        assertNull(m.getRejectPath());
        assertNull(m.getFailMessage());
        assertNull(m.getFailPath());
    }
      
    @Test
    public void testTimeAttributesFlow2() {
        final Meter m = new Meter(logger);

        assertFalse(m.isStarted());
        assertFalse(m.isStopped());
        assertFalse(m.isOK());
        assertFalse(m.isReject());
        assertFalse(m.isFail());
        assertNull(m.getOkPath());
        assertNull(m.getRejectPath());
        assertNull(m.getFailMessage());
        assertNull(m.getFailPath());

        m.start();

        assertTrue(m.isStarted());
        assertFalse(m.isStopped());
       assertFalse(m.isOK());
        assertFalse(m.isReject());
        assertFalse(m.isFail());
        assertNull(m.getOkPath());
        assertNull(m.getRejectPath());
        assertNull(m.getFailMessage());
        assertNull(m.getFailPath());

        m.path("Flow");

        assertTrue(m.isStarted());
        assertFalse(m.isStopped());
        assertFalse(m.isOK());
        assertFalse(m.isReject());
        assertFalse(m.isFail());
        assertEquals("Flow", m.getOkPath());
        assertNull(m.getRejectPath());
        assertNull(m.getFailMessage());
        assertNull(m.getFailPath());

        m.path("Path");

        assertTrue(m.isStarted());
        assertFalse(m.isStopped());
        assertFalse(m.isOK());
        assertFalse(m.isReject());
        assertFalse(m.isFail());
        assertEquals("Path", m.getOkPath());
        assertNull(m.getRejectPath());
        assertNull(m.getFailMessage());
        assertNull(m.getFailPath());

    	m.ok();

        assertTrue(m.isStarted());
        assertTrue(m.isStopped());
        assertTrue(m.isOK());
        assertFalse(m.isReject());
        assertFalse(m.isFail());
        assertEquals("Path", m.getOkPath());
        assertNull(m.getRejectPath());
        assertNull(m.getFailMessage());
        assertNull(m.getFailPath());
    }
      
    @Test
    public void testTimeAttributesReject() {
        final Meter m = new Meter(logger);

        assertFalse(m.isStarted());
        assertFalse(m.isStopped());
       assertFalse(m.isOK());
        assertFalse(m.isReject());
        assertFalse(m.isFail());
        assertNull(m.getOkPath());
        assertNull(m.getRejectPath());
        assertNull(m.getFailMessage());
        assertNull(m.getFailPath());

        m.start();

        assertTrue(m.isStarted());
        assertFalse(m.isStopped());
      assertFalse(m.isOK());
        assertFalse(m.isReject());
        assertFalse(m.isFail());
        assertNull(m.getOkPath());
        assertNull(m.getRejectPath());
        assertNull(m.getFailMessage());
        assertNull(m.getFailPath());

        m.reject("Reject");

        assertTrue(m.isStarted());
        assertTrue(m.isStopped());
        assertFalse(m.isOK());
        assertTrue(m.isReject());
        assertFalse(m.isFail());
        assertNull(m.getOkPath());
        assertEquals("Reject", m.getRejectPath());
        assertNull(m.getFailMessage());
        assertNull(m.getFailPath());
    }

    @Test
    public void testTimeAttributesFail() {
        final Meter m = new Meter(logger);

        assertFalse(m.isStarted());
        assertFalse(m.isStopped());
        assertFalse(m.isOK());
        assertFalse(m.isReject());
        assertFalse(m.isFail());
        assertNull(m.getOkPath());
        assertNull(m.getRejectPath());
        assertNull(m.getFailMessage());
        assertNull(m.getFailPath());

        m.start();

        assertTrue(m.isStarted());
        assertFalse(m.isStopped());
       assertFalse(m.isOK());
        assertFalse(m.isReject());
        assertFalse(m.isFail());
        assertNull(m.getOkPath());
        assertNull(m.getRejectPath());
        assertNull(m.getFailMessage());
        assertNull(m.getFailPath());

        m.fail(new IllegalStateException("ISE"));

        assertTrue(m.isStarted());
        assertTrue(m.isStopped());
        assertFalse(m.isOK());
        assertFalse(m.isReject());
        assertTrue(m.isFail());
        assertNull(m.getOkPath());
        assertNull(m.getRejectPath());
        assertEquals("ISE", m.getFailMessage());
        assertEquals("java.lang.IllegalStateException", m.getFailPath());
    }
 }
