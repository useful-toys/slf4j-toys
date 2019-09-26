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
import org.junit.Test;
import org.slf4j.impl.TestLogger;
import org.usefultoys.slf4j.LoggerFactory;
import org.usefultoys.slf4j.Session;

/**
 *
 * @author Daniel Felix Ferber
 */
public class MeterIdentifierAttributesTest {

    @Test
    public void testIdentifierAttributes() {
        final String category1 = "TestCategory1";
        final TestLogger logger1 = (TestLogger) LoggerFactory.getLogger(category1);
        logger1.setEnabled(false);
        logger1.clearEvents();

        final Meter m1 = new Meter(logger1);
        Assert.assertTrue(Session.uuid.endsWith(m1.getSessionUuid()));
        Assert.assertEquals(logger1, m1.getLogger());
        Assert.assertEquals(category1, m1.getEventCategory());
        Assert.assertNull(m1.getEventName());
        Assert.assertEquals(1L, m1.getEventPosition());

        final Meter m2 = new Meter(logger1);
        Assert.assertTrue(Session.uuid.endsWith(m2.getSessionUuid()));
        Assert.assertEquals(logger1, m2.getLogger());
        Assert.assertEquals(category1, m2.getEventCategory());
        Assert.assertNull(m2.getEventName());
        Assert.assertEquals(2L, m2.getEventPosition());

        final String category2 = "TestCategory2";
        final TestLogger logger2 = (TestLogger) LoggerFactory.getLogger(category2);
        logger2.setEnabled(false);
        logger2.clearEvents();

        final Meter m3 = new Meter(logger2);
        Assert.assertTrue(Session.uuid.endsWith(m3.getSessionUuid()));
        Assert.assertEquals(logger2, m3.getLogger());
        Assert.assertEquals(category2, m3.getEventCategory());
        Assert.assertNull(m3.getEventName());
        Assert.assertEquals(1L, m3.getEventPosition());

        final Meter m4 = new Meter(logger2);
        Assert.assertTrue(Session.uuid.endsWith(m4.getSessionUuid()));
        Assert.assertEquals(logger2, m4.getLogger());
        Assert.assertEquals(category2, m4.getEventCategory());
        Assert.assertNull(m3.getEventName());
        Assert.assertEquals(2L, m4.getEventPosition());
    }
    
    @Test
    public void testIdentifierAttributesWithName() {
        final String category = "TestCategory3";
        final TestLogger logger = (TestLogger) LoggerFactory.getLogger(category);
        logger.setEnabled(false);
        logger.clearEvents();

        final Meter m1 = new Meter(logger);
        Assert.assertTrue(Session.uuid.endsWith(m1.getSessionUuid()));
        Assert.assertEquals(logger, m1.getLogger());
        Assert.assertEquals(category, m1.getEventCategory());
        Assert.assertNull(m1.getEventName());
        Assert.assertEquals(1L, m1.getEventPosition());

        final Meter m2 = new Meter(logger, "op");
        Assert.assertTrue(Session.uuid.endsWith(m2.getSessionUuid()));
        Assert.assertEquals(logger, m2.getLogger());
        Assert.assertEquals(category, m2.getEventCategory());
        Assert.assertEquals("op", m2.getEventName());
        Assert.assertEquals(1L, m2.getEventPosition());

        final Meter m3 = new Meter(logger, "op");
        Assert.assertTrue(Session.uuid.endsWith(m3.getSessionUuid()));
        Assert.assertEquals(logger, m3.getLogger());
        Assert.assertEquals(category, m3.getEventCategory());
        Assert.assertEquals("op", m3.getEventName());
        Assert.assertEquals(2L, m3.getEventPosition());

        final Meter m4 = new Meter(logger, "po");
        Assert.assertTrue(Session.uuid.endsWith(m4.getSessionUuid()));
        Assert.assertEquals(logger, m4.getLogger());
        Assert.assertEquals(category, m4.getEventCategory());
        Assert.assertEquals("po", m4.getEventName());
        Assert.assertEquals(1L, m4.getEventPosition());

        final Meter m5 = new Meter(logger, "po");
        Assert.assertTrue(Session.uuid.endsWith(m5.getSessionUuid()));
        Assert.assertEquals(logger, m5.getLogger());
        Assert.assertEquals(category, m5.getEventCategory());
        Assert.assertEquals("po", m5.getEventName());
        Assert.assertEquals(2L, m5.getEventPosition());        
    }
    
    @Test
    public void testIdentifierAttributesWithSubmeter() {
        final String category = "TestCategory4";
        final TestLogger logger = (TestLogger) LoggerFactory.getLogger(category);
        logger.setEnabled(false);
        logger.clearEvents();

        final Meter m1 = new Meter(logger);
        Assert.assertTrue(Session.uuid.endsWith(m1.getSessionUuid()));
        Assert.assertEquals(logger, m1.getLogger());
        Assert.assertEquals(category, m1.getEventCategory());
        Assert.assertNull(m1.getEventName());
        Assert.assertEquals(1L, m1.getEventPosition());

        final Meter m2 = m1.sub("rs");
        Assert.assertTrue(Session.uuid.endsWith(m2.getSessionUuid()));
        Assert.assertEquals(logger, m2.getLogger());
        Assert.assertEquals(category, m2.getEventCategory());
        Assert.assertEquals("rs", m2.getEventName());
        Assert.assertEquals(1L, m2.getEventPosition());

        final Meter m3 = m1.sub("rs");
        Assert.assertTrue(Session.uuid.endsWith(m3.getSessionUuid()));
        Assert.assertEquals(logger, m3.getLogger());
        Assert.assertEquals(category, m3.getEventCategory());
        Assert.assertEquals("rs", m3.getEventName());
        Assert.assertEquals(2L, m3.getEventPosition());
        
        final Meter m4 = m1.sub("sr");
        Assert.assertTrue(Session.uuid.endsWith(m4.getSessionUuid()));
        Assert.assertEquals(logger, m4.getLogger());
        Assert.assertEquals(category, m4.getEventCategory());
        Assert.assertEquals("sr", m4.getEventName());
        Assert.assertEquals(1L, m4.getEventPosition());

        final Meter m5 = m1.sub("sr");
        Assert.assertTrue(Session.uuid.endsWith(m5.getSessionUuid()));
        Assert.assertEquals(logger, m5.getLogger());
        Assert.assertEquals(category, m5.getEventCategory());
        Assert.assertEquals("sr", m5.getEventName());
        Assert.assertEquals(2L, m5.getEventPosition());
    }
    
    @Test
    public void testIdentifierAttributesWithSubSubmeter() {
        final String category = "TestCategory5";
        final TestLogger logger = (TestLogger) LoggerFactory.getLogger(category);
        logger.setEnabled(false);
        logger.clearEvents();

        final Meter m1 = new Meter(logger);
        Assert.assertTrue(Session.uuid.endsWith(m1.getSessionUuid()));
        Assert.assertEquals(logger, m1.getLogger());
        Assert.assertEquals(category, m1.getEventCategory());
        Assert.assertNull(m1.getEventName());
        Assert.assertEquals(1L, m1.getEventPosition());

        final Meter m2 = m1.sub("rs");
        Assert.assertTrue(Session.uuid.endsWith(m2.getSessionUuid()));
        Assert.assertEquals(logger, m2.getLogger());
        Assert.assertEquals(category, m2.getEventCategory());
        Assert.assertEquals("rs", m2.getEventName());
        Assert.assertEquals(1L, m2.getEventPosition());

        final Meter m3 = m2.sub("sr");
        Assert.assertTrue(Session.uuid.endsWith(m3.getSessionUuid()));
        Assert.assertEquals(logger, m3.getLogger());
        Assert.assertEquals(category, m3.getEventCategory());
        Assert.assertEquals("rs/sr", m3.getEventName());
        Assert.assertEquals(1L, m3.getEventPosition());

        final Meter m4 = m2.sub("sr");
        Assert.assertTrue(Session.uuid.endsWith(m4.getSessionUuid()));
        Assert.assertEquals(logger, m4.getLogger());
        Assert.assertEquals(category, m4.getEventCategory());
        Assert.assertEquals("rs/sr", m4.getEventName());
        Assert.assertEquals(2L, m4.getEventPosition());

        final Meter m5 = m4.sub("sr");
        Assert.assertTrue(Session.uuid.endsWith(m5.getSessionUuid()));
        Assert.assertEquals(logger, m5.getLogger());
        Assert.assertEquals(category, m5.getEventCategory());
        Assert.assertEquals("rs/sr/sr", m5.getEventName());
        Assert.assertEquals(1L, m5.getEventPosition());
    }
    
    @Test
    public void testIdentifierAttributesWithNameAndSubmeter() {
        final String category = "TestCategory6";
        final TestLogger logger = (TestLogger) LoggerFactory.getLogger(category);
        logger.setEnabled(false);
        logger.clearEvents();

        final Meter m1 = new Meter(logger, "n");
        Assert.assertTrue(Session.uuid.endsWith(m1.getSessionUuid()));
        Assert.assertEquals(logger, m1.getLogger());
        Assert.assertEquals(category, m1.getEventCategory());
        Assert.assertEquals("n", m1.getEventName());
        Assert.assertEquals(1L, m1.getEventPosition());

        final Meter m2 = m1.sub("rs");
        Assert.assertTrue(Session.uuid.endsWith(m2.getSessionUuid()));
        Assert.assertEquals(logger, m2.getLogger());
        Assert.assertEquals(category, m2.getEventCategory());
        Assert.assertEquals("n/rs", m2.getEventName());
        Assert.assertEquals(1L, m2.getEventPosition());

        final Meter m3 = m1.sub("rs");
        Assert.assertTrue(Session.uuid.endsWith(m3.getSessionUuid()));
        Assert.assertEquals(logger, m3.getLogger());
        Assert.assertEquals(category, m3.getEventCategory());
        Assert.assertEquals("n/rs", m3.getEventName());
        Assert.assertEquals(2L, m3.getEventPosition());
        
        final Meter m4 = m1.sub("sr");
        Assert.assertTrue(Session.uuid.endsWith(m4.getSessionUuid()));
        Assert.assertEquals(logger, m4.getLogger());
        Assert.assertEquals(category, m4.getEventCategory());
        Assert.assertEquals("n/sr", m4.getEventName());
        Assert.assertEquals(1L, m4.getEventPosition());

        final Meter m5 = m1.sub("sr");
        Assert.assertTrue(Session.uuid.endsWith(m5.getSessionUuid()));
        Assert.assertEquals(logger, m5.getLogger());
        Assert.assertEquals(category, m5.getEventCategory());
        Assert.assertEquals("n/sr", m5.getEventName());
        Assert.assertEquals(2L, m5.getEventPosition());
    }

 }
