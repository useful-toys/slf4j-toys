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
        Assert.assertEquals(category1, m1.getCategory());
        Assert.assertNull(m1.getOperation());
        Assert.assertEquals(1L, m1.getPosition());

        final Meter m2 = new Meter(logger1);
        Assert.assertTrue(Session.uuid.endsWith(m2.getSessionUuid()));
        Assert.assertEquals(category1, m2.getCategory());
        Assert.assertNull(m2.getOperation());
        Assert.assertEquals(2L, m2.getPosition());

        final String category2 = "TestCategory2";
        final TestLogger logger2 = (TestLogger) LoggerFactory.getLogger(category2);
        logger2.setEnabled(false);
        logger2.clearEvents();

        final Meter m3 = new Meter(logger2);
        Assert.assertTrue(Session.uuid.endsWith(m3.getSessionUuid()));
        Assert.assertEquals(category2, m3.getCategory());
        Assert.assertNull(m3.getOperation());
        Assert.assertEquals(1L, m3.getPosition());

        final Meter m4 = new Meter(logger2);
        Assert.assertTrue(Session.uuid.endsWith(m4.getSessionUuid()));
        Assert.assertEquals(category2, m4.getCategory());
        Assert.assertNull(m3.getOperation());
        Assert.assertEquals(2L, m4.getPosition());
    }
    
    @Test
    public void testIdentifierAttributesWithName() {
        final String category = "TestCategory3";
        final TestLogger logger = (TestLogger) LoggerFactory.getLogger(category);
        logger.setEnabled(false);
        logger.clearEvents();

        final Meter m1 = new Meter(logger);
        Assert.assertTrue(Session.uuid.endsWith(m1.getSessionUuid()));
        Assert.assertEquals(category, m1.getCategory());
        Assert.assertNull(m1.getOperation());
        Assert.assertEquals(1L, m1.getPosition());

        final Meter m2 = new Meter(logger, "op");
        Assert.assertTrue(Session.uuid.endsWith(m2.getSessionUuid()));
        Assert.assertEquals(category, m2.getCategory());
        Assert.assertEquals("op", m2.getOperation());
        Assert.assertEquals(1L, m2.getPosition());

        final Meter m3 = new Meter(logger, "op");
        Assert.assertTrue(Session.uuid.endsWith(m3.getSessionUuid()));
        Assert.assertEquals(category, m3.getCategory());
        Assert.assertEquals("op", m3.getOperation());
        Assert.assertEquals(2L, m3.getPosition());

        final Meter m4 = new Meter(logger, "po");
        Assert.assertTrue(Session.uuid.endsWith(m4.getSessionUuid()));
        Assert.assertEquals(category, m4.getCategory());
        Assert.assertEquals("po", m4.getOperation());
        Assert.assertEquals(1L, m4.getPosition());

        final Meter m5 = new Meter(logger, "po");
        Assert.assertTrue(Session.uuid.endsWith(m5.getSessionUuid()));
        Assert.assertEquals(category, m5.getCategory());
        Assert.assertEquals("po", m5.getOperation());
        Assert.assertEquals(2L, m5.getPosition());
    }
    
    @Test
    public void testIdentifierAttributesWithSubmeter() {
        final String category = "TestCategory4";
        final TestLogger logger = (TestLogger) LoggerFactory.getLogger(category);
        logger.setEnabled(false);
        logger.clearEvents();

        final Meter m1 = new Meter(logger);
        Assert.assertTrue(Session.uuid.endsWith(m1.getSessionUuid()));
        Assert.assertEquals(category, m1.getCategory());
        Assert.assertNull(m1.getOperation());
        Assert.assertEquals(1L, m1.getPosition());

        final Meter m2 = m1.sub("rs");
        Assert.assertTrue(Session.uuid.endsWith(m2.getSessionUuid()));
        Assert.assertEquals(category, m2.getCategory());
        Assert.assertEquals("rs", m2.getOperation());
        Assert.assertEquals(1L, m2.getPosition());

        final Meter m3 = m1.sub("rs");
        Assert.assertTrue(Session.uuid.endsWith(m3.getSessionUuid()));
        Assert.assertEquals(category, m3.getCategory());
        Assert.assertEquals("rs", m3.getOperation());
        Assert.assertEquals(2L, m3.getPosition());
        
        final Meter m4 = m1.sub("sr");
        Assert.assertTrue(Session.uuid.endsWith(m4.getSessionUuid()));
        Assert.assertEquals(category, m4.getCategory());
        Assert.assertEquals("sr", m4.getOperation());
        Assert.assertEquals(1L, m4.getPosition());

        final Meter m5 = m1.sub("sr");
        Assert.assertTrue(Session.uuid.endsWith(m5.getSessionUuid()));
        Assert.assertEquals(category, m5.getCategory());
        Assert.assertEquals("sr", m5.getOperation());
        Assert.assertEquals(2L, m5.getPosition());
    }
    
    @Test
    public void testIdentifierAttributesWithSubSubmeter() {
        final String category = "TestCategory5";
        final TestLogger logger = (TestLogger) LoggerFactory.getLogger(category);
        logger.setEnabled(false);
        logger.clearEvents();

        final Meter m1 = new Meter(logger);
        Assert.assertTrue(Session.uuid.endsWith(m1.getSessionUuid()));
        Assert.assertEquals(category, m1.getCategory());
        Assert.assertNull(m1.getOperation());
        Assert.assertEquals(1L, m1.getPosition());

        final Meter m2 = m1.sub("rs");
        Assert.assertTrue(Session.uuid.endsWith(m2.getSessionUuid()));
        Assert.assertEquals(category, m2.getCategory());
        Assert.assertEquals("rs", m2.getOperation());
        Assert.assertEquals(1L, m2.getPosition());

        final Meter m3 = m2.sub("sr");
        Assert.assertTrue(Session.uuid.endsWith(m3.getSessionUuid()));
        Assert.assertEquals(category, m3.getCategory());
        Assert.assertEquals("rs/sr", m3.getOperation());
        Assert.assertEquals(1L, m3.getPosition());

        final Meter m4 = m2.sub("sr");
        Assert.assertTrue(Session.uuid.endsWith(m4.getSessionUuid()));
        Assert.assertEquals(category, m4.getCategory());
        Assert.assertEquals("rs/sr", m4.getOperation());
        Assert.assertEquals(2L, m4.getPosition());

        final Meter m5 = m4.sub("sr");
        Assert.assertTrue(Session.uuid.endsWith(m5.getSessionUuid()));
        Assert.assertEquals(category, m5.getCategory());
        Assert.assertEquals("rs/sr/sr", m5.getOperation());
        Assert.assertEquals(1L, m5.getPosition());
    }
    
    @Test
    public void testIdentifierAttributesWithNameAndSubmeter() {
        final String category = "TestCategory6";
        final TestLogger logger = (TestLogger) LoggerFactory.getLogger(category);
        logger.setEnabled(false);
        logger.clearEvents();

        final Meter m1 = new Meter(logger, "n");
        Assert.assertTrue(Session.uuid.endsWith(m1.getSessionUuid()));
        Assert.assertEquals(category, m1.getCategory());
        Assert.assertEquals("n", m1.getOperation());
        Assert.assertEquals(1L, m1.getPosition());

        final Meter m2 = m1.sub("rs");
        Assert.assertTrue(Session.uuid.endsWith(m2.getSessionUuid()));
        Assert.assertEquals(category, m2.getCategory());
        Assert.assertEquals("n/rs", m2.getOperation());
        Assert.assertEquals(1L, m2.getPosition());

        final Meter m3 = m1.sub("rs");
        Assert.assertTrue(Session.uuid.endsWith(m3.getSessionUuid()));
        Assert.assertEquals(category, m3.getCategory());
        Assert.assertEquals("n/rs", m3.getOperation());
        Assert.assertEquals(2L, m3.getPosition());
        
        final Meter m4 = m1.sub("sr");
        Assert.assertTrue(Session.uuid.endsWith(m4.getSessionUuid()));
        Assert.assertEquals(category, m4.getCategory());
        Assert.assertEquals("n/sr", m4.getOperation());
        Assert.assertEquals(1L, m4.getPosition());

        final Meter m5 = m1.sub("sr");
        Assert.assertTrue(Session.uuid.endsWith(m5.getSessionUuid()));
        Assert.assertEquals(category, m5.getCategory());
        Assert.assertEquals("n/sr", m5.getOperation());
        Assert.assertEquals(2L, m5.getPosition());
    }

 }
