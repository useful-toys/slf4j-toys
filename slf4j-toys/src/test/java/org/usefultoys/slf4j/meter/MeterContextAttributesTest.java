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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.impl.MockLogger;
import org.usefultoys.slf4j.LoggerFactory;
import org.usefultoys.slf4j.SessionConfig;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.*;
import static org.slf4j.impl.MockLoggerEvent.Level.ERROR;
import static org.usefultoys.slf4j.meter.Markers.ILLEGAL;

/**
 * @author Daniel Felix Ferber
 */
class MeterContextAttributesTest {

    MockLogger logger = (MockLogger) LoggerFactory.getLogger("Test");

    @BeforeAll
    static void validate() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    @BeforeEach
    void setupLogger() {
        logger.clearEvents();
        logger.setEnabled(true);
    }

    @AfterEach
    void clearLogger() {
        logger.clearEvents();
        logger.setEnabled(true);
    }

    @Test
    void testMessageAttributes() {
        final String description1 = "Test Message";
        final Meter m1 = new Meter(logger).m(description1);
        assertEquals(description1, m1.getDescription());

        final String description2 = "Test  %d Message";
        final Meter m2 = new Meter(logger).m(description2, 10);
        assertEquals(String.format(description2, 10), m2.getDescription());
    }

    @Test
    void testCtxWithoutValue() {
        final Meter meter = new Meter(logger);
        assertNull(meter.getContext());
        meter.ctx("key1");
        assertNotNull(meter.getContext());
        assertTrue(meter.getContext().containsKey("key1"));
        assertNull(meter.getContext().get("key1"));
        assertThrows(UnsupportedOperationException.class, () -> meter.getContext().put("a", "b"));
        meter.unctx("key1");
        assertFalse(meter.getContext().containsKey("key8"));
        meter.ctx("key10");
        assertTrue(meter.getContext().containsKey("key10"));
        assertNull(meter.getContext().get("key10"));
    }

    @Test
    void testCtxWithStringAndInt() {
        final Meter meter = new Meter(logger);
        assertNull(meter.getContext());
        meter.ctx("key2", 42);
        assertNotNull(meter.getContext());
        assertEquals("42", meter.getContext().get("key2"));
        meter.unctx("key2");
        assertFalse(meter.getContext().containsKey("key2"));
        meter.ctx("key11", 100);
        assertEquals("100", meter.getContext().get("key11"));
    }

    @Test
    void testCtxWithStringAndLong() {
        final Meter meter = new Meter(logger);
        assertNull(meter.getContext());
        meter.ctx("key3", 123456789L);
        assertNotNull(meter.getContext());
        assertEquals("123456789", meter.getContext().get("key3"));
        meter.unctx("key3");
        assertFalse(meter.getContext().containsKey("key3"));
        meter.ctx("key12", 987654321L);
        assertEquals("987654321", meter.getContext().get("key12"));
    }

    @Test
    void testCtxWithStringAndBoolean() {
        final Meter meter = new Meter(logger);
        assertNull(meter.getContext());
        meter.ctx("key4", true);
        assertNotNull(meter.getContext());
        assertEquals("true", meter.getContext().get("key4"));
        meter.unctx("key4");
        assertFalse(meter.getContext().containsKey("key4"));
        meter.ctx("key13", false);
        assertEquals("false", meter.getContext().get("key13"));
    }

    @Test
    void testCtxWithStringAndFloat() {
        final Meter meter = new Meter(logger);
        assertNull(meter.getContext());
        meter.ctx("key5", 3.14f);
        assertNotNull(meter.getContext());
        assertEquals("3.14", meter.getContext().get("key5"));
        meter.unctx("key5");
        assertFalse(meter.getContext().containsKey("key5"));
        meter.ctx("key14", 1.23f);
        assertEquals("1.23", meter.getContext().get("key14"));
    }

    @Test
    void testCtxWithStringAndDouble() {
        final Meter meter = new Meter(logger);
        assertNull(meter.getContext());
        meter.ctx("key6", 2.71828);
        assertNotNull(meter.getContext());
        assertEquals("2.71828", meter.getContext().get("key6"));
        meter.unctx("key6");
        assertFalse(meter.getContext().containsKey("key6"));
        meter.ctx("key15", 1.41421);
        assertEquals("1.41421", meter.getContext().get("key15"));
    }

    @Test
    void testCtxWithStringAndObject() {
        final Meter meter = new Meter(logger);
        assertNull(meter.getContext());
        meter.ctx("key7", new Object() {
            @Override
            public String toString() {
                return "customObject";
            }
        });
        assertNotNull(meter.getContext());
        assertEquals("customObject", meter.getContext().get("key7"));
        meter.unctx("key7");
        assertFalse(meter.getContext().containsKey("key7"));
        meter.ctx("key16", new Object() {
            @Override
            public String toString() {
                return "anotherObject";
            }
        });
        assertEquals("anotherObject", meter.getContext().get("key16"));
        meter.ctx("key27", (Object) null);
        assertEquals("<null>", meter.getContext().get("key27"));
    }

    @Test
    void testCtxWithStringAndString() {
        final Meter meter = new Meter(logger);
        assertNull(meter.getContext());
        meter.ctx("key8", "value8");
        assertNotNull(meter.getContext());
        assertEquals("value8", meter.getContext().get("key8"));
        meter.unctx("key8");
        assertFalse(meter.getContext().containsKey("key8"));
        meter.ctx("key17", "value17");
        assertEquals("value17", meter.getContext().get("key17"));
        meter.ctx("key27", (String) null);
        assertEquals("<null>", meter.getContext().get("key27"));
    }

    @Test
    void testCtxWithStringAndFormattedString() {
        final Meter meter = new Meter(logger);
        assertNull(meter.getContext());
        meter.ctx("key9", "formatted %d", 100);
        assertNotNull(meter.getContext());
        assertEquals("formatted 100", meter.getContext().get("key9"));
        meter.unctx("key9");
        assertFalse(meter.getContext().containsKey("key9"));
        meter.ctx("key18", "another %s", "test");
        assertEquals("another test", meter.getContext().get("key18"));
    }

    @Test
    void testCtxWithStringAndIntegerObject() {
        final Meter meter = new Meter(logger);
        assertNull(meter.getContext());
        meter.ctx("key19", Integer.valueOf(123));
        assertNotNull(meter.getContext());
        assertEquals("123", meter.getContext().get("key19"));
        meter.unctx("key19");
        assertFalse(meter.getContext().containsKey("key19"));
        meter.ctx("key20", Integer.valueOf(456));
        assertEquals("456", meter.getContext().get("key20"));
        meter.ctx("key27", (Integer) null);
        assertEquals("<null>", meter.getContext().get("key27"));
    }

    @Test
    void testCtxWithStringAndLongObject() {
        final Meter meter = new Meter(logger);
        assertNull(meter.getContext());
        meter.ctx("key19", Long.valueOf(123));
        assertNotNull(meter.getContext());
        assertEquals("123", meter.getContext().get("key19"));
        meter.unctx("key19");
        assertFalse(meter.getContext().containsKey("key19"));
        meter.ctx("key20", Long.valueOf(456));
        assertEquals("456", meter.getContext().get("key20"));
        meter.ctx("key27", (Long) null);
        assertEquals("<null>", meter.getContext().get("key27"));
    }

    @Test
    void testCtxWithBooleanAndString1() {
        final Meter meter = new Meter(logger);
        assertNull(meter.getContext());
        meter.ctx(true, "valueTrue");
        assertNotNull(meter.getContext());
        assertTrue(meter.getContext().containsKey("valueTrue"));
        meter.unctx("valueTrue");
        assertFalse(meter.getContext().containsKey("valueTrue"));
        meter.ctx(false, "valueTrue");
        assertFalse(meter.getContext().containsKey("valueTrue"));
        meter.ctx(true, "otherTrue");
        assertTrue(meter.getContext().containsKey("otherTrue"));
    }

    @Test
    void testCtxWithBooleanAndString2() {
        final Meter meter = new Meter(logger);
        assertNull(meter.getContext());
        meter.ctx(false, "valueTrue");
        assertNull(meter.getContext());
        meter.ctx(true, "valueTrue");
        assertNotNull(meter.getContext());
        assertTrue(meter.getContext().containsKey("valueTrue"));
    }

    @Test
    void testCtxWithBooleanAndStringString1() {
        final Meter meter = new Meter(logger);
        assertNull(meter.getContext());
        meter.ctx(true, "valueTrue", "falseTrue");
        assertNotNull(meter.getContext());
        assertTrue(meter.getContext().containsKey("valueTrue"));
        meter.ctx(true, "otherTrue", "otherFalse");
        assertTrue(meter.getContext().containsKey("valueTrue"));
        meter.unctx("valueTrue");
        assertFalse(meter.getContext().containsKey("valueTrue"));
        meter.ctx(false, "valueTrue", "falseTrue");
        assertTrue(meter.getContext().containsKey("falseTrue"));
    }

    @Test
    void testCtxWithBooleanAndStringString2() {
        final Meter meter = new Meter(logger);
        assertNull(meter.getContext());
        meter.ctx(false, "valueTrue", "falseTrue");
        assertNotNull(meter.getContext());
        assertTrue(meter.getContext().containsKey("falseTrue"));
    }

    @Test
    void testCtxWithStringAndFloatObject() {
        final Meter meter = new Meter(logger);
        assertNull(meter.getContext());
        meter.ctx("key21", Float.valueOf(3.14f));
        assertNotNull(meter.getContext());
        assertEquals("3.14", meter.getContext().get("key21"));
        meter.unctx("key21");
        assertFalse(meter.getContext().containsKey("key21"));
        meter.ctx("key22", Float.valueOf(1.23f));
        assertEquals("1.23", meter.getContext().get("key22"));
        meter.ctx("key27", (Float) null);
        assertEquals("<null>", meter.getContext().get("key27"));
    }

    @Test
    void testCtxWithStringAndDoubleObject() {
        final Meter meter = new Meter(logger);
        assertNull(meter.getContext());
        meter.ctx("key23", Double.valueOf(2.71828));
        assertNotNull(meter.getContext());
        assertEquals("2.71828", meter.getContext().get("key23"));
        meter.unctx("key23");
        assertFalse(meter.getContext().containsKey("key23"));
        meter.ctx("key24", Double.valueOf(1.41421));
        assertEquals("1.41421", meter.getContext().get("key24"));
        meter.ctx("key28", (Double) null);
        assertEquals("<null>", meter.getContext().get("key28"));
    }

    @Test
    void testCtxWithStringAndBooleanObject() {
        final Meter meter = new Meter(logger);
        assertNull(meter.getContext());
        meter.ctx("key25", Boolean.TRUE);
        assertNotNull(meter.getContext());
        assertEquals("true", meter.getContext().get("key25"));
        meter.unctx("key25");
        assertFalse(meter.getContext().containsKey("key25"));
        meter.ctx("key26", Boolean.FALSE);
        assertEquals("false", meter.getContext().get("key26"));
        meter.ctx("key27", (Boolean) null);
        assertEquals("<null>", meter.getContext().get("key27"));
    }

    @Test
    void testUnctx() {
        final Meter meter = new Meter(logger);
        assertNull(meter.getContext());
        meter.unctx("key25");
        assertNull(meter.getContext());
    }

    @Test
    void testSubmeterInheritsCtx() {
        final Meter meter = new Meter(logger);
        final Meter meter1 = meter.sub("sub");
        assertNull(meter1.getContext());

        meter.ctx("a", "b");
        meter.ctx("b", 1);

        final Meter meter2 = meter.sub("sub");
        assertNotNull(meter2.getContext());
        assertEquals("b", meter2.getContext().get("a"));
        assertEquals("1", meter2.getContext().get("b"));

        meter.ctx("c", 0.0);
        assertFalse(meter2.getContext().containsKey("c"));
    }

    @Test
    void testMWithNullLogsError() {
        final Meter meter = new Meter(logger);

        // Test m(message) with null
        meter.m(null);
        logger.assertEvent(0, ERROR, ILLEGAL, "Illegal call to Meter.m(message): Null argument. id=Test#");
        logger.clearEvents();

        // Test ctx(message, args) with null message
        meter.m(null, 100);
        logger.assertEvent(0, ERROR, ILLEGAL, "Illegal call to Meter.m(message, args...): Null argument. id=Test#");
        logger.clearEvents();

        // Test ctx(message, args) with null message
        meter.m("%d", 100.0);
        logger.assertEvent(0, ERROR, ILLEGAL, "Illegal call to Meter.m(message, args...): Illegal string format. id=Test#");
        logger.clearEvents();
    }
}
