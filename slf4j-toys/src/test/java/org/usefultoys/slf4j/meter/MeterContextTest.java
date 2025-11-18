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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.usefultoys.slf4j.SessionConfig;

import java.nio.charset.Charset;

import static org.junit.jupiter.api.Assertions.*;
import static org.usefultoys.slf4j.meter.MeterData.NULL_VALUE;

class MeterContextTest {
    @BeforeAll
    static void validateConsistentCharset() {
        assertEquals(Charset.defaultCharset().name(), SessionConfig.charset, "Test requires SessionConfig.charset = default charset");
    }

    // Implementação de teste da interface MeterContext
    static class TestMeterContext extends MeterData implements MeterContext {
    }

    private TestMeterContext meterContext;

    @BeforeEach
    void setUp() {
        meterContext = new TestMeterContext();
        // Removido: assertTrue(meterContext.getContext().isEmpty(), "Context map should be empty initially");
    }

    @Test
    @DisplayName("ctx() with key-only should add key with null value")
    void testCtxWithoutValue() {
        final String key = "key1";
        meterContext.ctx(key);
        assertFalse(meterContext.getContext().isEmpty(), "Context map should not be empty after adding key '" + key + "'");
        assertTrue(meterContext.getContext().containsKey(key), "Context map should contain '" + key + "'");
        assertNull(meterContext.getContext().get(key), "Value for '" + key + "' should be null");
    }

    @Test
    @DisplayName("ctx() with null key should add entry with NULL_VALUE key")
    void testCtxWithoutValueNull() {
        final String key = null; // This will be converted to NULL_VALUE internally
        meterContext.ctx(key);
        assertFalse(meterContext.getContext().isEmpty(), "Context map should not be empty after adding key '" + NULL_VALUE + "'");
        assertTrue(meterContext.getContext().containsKey(NULL_VALUE), "Context map should contain '" + NULL_VALUE + "'");
        assertNull(meterContext.getContext().get(NULL_VALUE), "Value for '" + NULL_VALUE + "' should be null");
    }

    @Test
    @DisplayName("ctx(name, int) should add key with integer value as string")
    void testCtxWithStringAndInt() {
        final String key = "key2";
        meterContext.ctx(key, 42);
        assertFalse(meterContext.getContext().isEmpty(), "Context map should not be empty after adding key '" + key + "'");
        assertTrue(meterContext.getContext().containsKey(key), "Context map should contain '" + key + "'");
        assertEquals("42", meterContext.getContext().get(key), "Value for '" + key + "' should be '42'");
    }

    @Test
    @DisplayName("ctx(name, long) should add key with long value as string")
    void testCtxWithStringAndLong() {
        final String key = "key3";
        meterContext.ctx(key, 123456789L);
        assertFalse(meterContext.getContext().isEmpty(), "Context map should not be empty after adding key '" + key + "'");
        assertTrue(meterContext.getContext().containsKey(key), "Context map should contain '" + key + "'");
        assertEquals("123456789", meterContext.getContext().get(key), "Value for '" + key + "' should be '123456789'");
    }

    @Test
    @DisplayName("ctx(name, boolean) should add key with boolean value as string")
    void testCtxWithStringAndBoolean() {
        final String key = "key4";
        meterContext.ctx(key, true);
        assertFalse(meterContext.getContext().isEmpty(), "Context map should not be empty after adding key '" + key + "'");
        assertTrue(meterContext.getContext().containsKey(key), "Context map should contain '" + key + "'");
        assertEquals("true", meterContext.getContext().get(key), "Value for '" + key + "' should be 'true'");
    }

    @Test
    @DisplayName("ctx(name, float) should add key with float value as string")
    void testCtxWithStringAndFloat() {
        final String key = "key5";
        meterContext.ctx(key, 3.14f);
        assertFalse(meterContext.getContext().isEmpty(), "Context map should not be empty after adding key '" + key + "'");
        assertTrue(meterContext.getContext().containsKey(key), "Context map should contain '" + key + "'");
        assertEquals("3.14", meterContext.getContext().get(key), "Value for '" + key + "' should be '3.14'");
    }

    @Test
    @DisplayName("ctx(name, double) should add key with double value as string")
    void testCtxWithStringAndDouble() {
        final String key = "key6";
        meterContext.ctx(key, 2.71828);
        assertFalse(meterContext.getContext().isEmpty(), "Context map should not be empty after adding key '" + key + "'");
        assertTrue(meterContext.getContext().containsKey(key), "Context map should contain '" + key + "'");
        assertEquals("2.71828", meterContext.getContext().get(key), "Value for '" + key + "' should be '2.71828'");
    }

    @Test
    @DisplayName("ctx(name, Object) should add key with object's toString() value")
    void testCtxWithStringAndObject() {
        final String key7 = "key7";
        meterContext.ctx(key7, new Object() {
            @Override
            public String toString() {
                return "customObject";
            }
        });
        assertFalse(meterContext.getContext().isEmpty(), "Context map should not be empty after adding key '" + key7 + "'");
        assertTrue(meterContext.getContext().containsKey(key7), "Context map should contain '" + key7 + "'");
        assertEquals("customObject", meterContext.getContext().get(key7), "Value for '" + key7 + "' should be 'customObject'");

        final String key7_null = "key7_null";
        meterContext.ctx(key7_null, (Object) null);
        assertFalse(meterContext.getContext().isEmpty(), "Context map should not be empty after adding key '" + key7_null + "'");
        assertTrue(meterContext.getContext().containsKey(key7_null), "Context map should contain '" + key7_null + "'");
        assertEquals(NULL_VALUE, meterContext.getContext().get(key7_null), "Value for null object should be " + NULL_VALUE);
    }

    @Test
    @DisplayName("ctx(name, String) should add key with string value")
    void testCtxWithStringAndString() {
        final String key8 = "key8";
        meterContext.ctx(key8, "value8");
        assertFalse(meterContext.getContext().isEmpty(), "Context map should not be empty after adding key '" + key8 + "'");
        assertTrue(meterContext.getContext().containsKey(key8), "Context map should contain '" + key8 + "'");
        assertEquals("value8", meterContext.getContext().get(key8), "Value for '" + key8 + "' should be 'value8'");

        final String key8_null = "key8_null";
        meterContext.ctx(key8_null, (String) null);
        assertFalse(meterContext.getContext().isEmpty(), "Context map should not be empty after adding key '" + key8_null + "'");
        assertTrue(meterContext.getContext().containsKey(key8_null), "Context map should contain '" + key8_null + "'");
        assertEquals(NULL_VALUE, meterContext.getContext().get(key8_null), "Value for null string should be " + NULL_VALUE);
    }

    @Test
    @DisplayName("ctx(name, format, args) should add key with formatted string value")
    void testCtxWithStringAndFormattedString() {
        final String key = "key9";
        meterContext.ctx(key, "formatted %d", 100);
        assertFalse(meterContext.getContext().isEmpty(), "Context map should not be empty after adding key '" + key + "'");
        assertTrue(meterContext.getContext().containsKey(key), "Context map should contain '" + key + "'");
        assertEquals("formatted 100", meterContext.getContext().get(key), "Value for '" + key + "' should be 'formatted 100'");
    }

    @Test
    @DisplayName("ctx(name, Integer) should add key with Integer object value as string")
    void testCtxWithStringAndIntegerObject() {
        final String key19 = "key19";
        meterContext.ctx(key19, Integer.valueOf(123));
        assertFalse(meterContext.getContext().isEmpty(), "Context map should not be empty after adding key '" + key19 + "'");
        assertTrue(meterContext.getContext().containsKey(key19), "Context map should contain '" + key19 + "'");
        assertEquals("123", meterContext.getContext().get(key19), "Value for '" + key19 + "' should be '123'");

        final String key19_null = "key19_null";
        meterContext.ctx(key19_null, (Integer) null);
        assertFalse(meterContext.getContext().isEmpty(), "Context map should not be empty after adding key '" + key19_null + "'");
        assertTrue(meterContext.getContext().containsKey(key19_null), "Context map should contain '" + key19_null + "'");
        assertEquals(NULL_VALUE, meterContext.getContext().get(key19_null), "Value for null Integer should be " + NULL_VALUE);
    }

    @Test
    @DisplayName("ctx(name, Long) should add key with Long object value as string")
    void testCtxWithStringAndLongObject() {
        final String key20 = "key20";
        meterContext.ctx(key20, Long.valueOf(456L));
        assertFalse(meterContext.getContext().isEmpty(), "Context map should not be empty after adding key '" + key20 + "'");
        assertTrue(meterContext.getContext().containsKey(key20), "Context map should contain '" + key20 + "'");
        assertEquals("456", meterContext.getContext().get(key20), "Value for '" + key20 + "' should be '456'");

        final String key20_null = "key20_null";
        meterContext.ctx(key20_null, (Long) null);
        assertFalse(meterContext.getContext().isEmpty(), "Context map should not be empty after adding key '" + key20_null + "'");
        assertTrue(meterContext.getContext().containsKey(key20_null), "Context map should contain '" + key20_null + "'");
        assertEquals(NULL_VALUE, meterContext.getContext().get(key20_null), "Value for null Long should be " + NULL_VALUE);
    }

    @Test
    @DisplayName("ctx(condition, trueName) should add trueName if condition is true")
    void testCtxWithBooleanAndString_trueCondition() {
        final String key = "valueTrue";
        meterContext.ctx(true, key);
        assertFalse(meterContext.getContext().isEmpty(), "Context map should not be empty after adding key '" + key + "'");
        assertTrue(meterContext.getContext().containsKey(key), "Context map should contain '" + key + "'");
        assertNull(meterContext.getContext().get(key), "Value for '" + key + "' should be null");
    }

    @Test
    @DisplayName("ctx(condition, trueName) should not add trueName if condition is false")
    void testCtxWithBooleanAndString_falseCondition() {
        final String key = "valueFalse";
        meterContext.ctx(false, key);
        assertTrue(meterContext.getContext().isEmpty(), "Context map should remain empty when condition is false");
        assertFalse(meterContext.getContext().containsKey(key), "Context map should not contain '" + key + "'");
    }

    @Test
    @DisplayName("ctx(condition, trueName, falseName) should add trueName if condition is true")
    void testCtxWithBooleanAndStringString_trueCondition() {
        final String trueKey = "valueTrue";
        final String falseKey = "valueFalse";
        meterContext.ctx(true, trueKey, falseKey);
        assertFalse(meterContext.getContext().isEmpty(), "Context map should not be empty after adding key '" + trueKey + "'");
        assertTrue(meterContext.getContext().containsKey(trueKey), "Context map should contain '" + trueKey + "'");
        assertNull(meterContext.getContext().get(trueKey), "Value for '" + trueKey + "' should be null");
        assertFalse(meterContext.getContext().containsKey(falseKey), "Context map should not contain '" + falseKey + "'");
    }

    @Test
    @DisplayName("ctx(condition, trueName, falseName) should add falseName if condition is false")
    void testCtxWithBooleanAndStringString_falseCondition() {
        final String trueKey = "valueTrue";
        final String falseKey = "valueFalse";
        meterContext.ctx(false, trueKey, falseKey);
        assertFalse(meterContext.getContext().isEmpty(), "Context map should not be empty after adding key '" + falseKey + "'");
        assertTrue(meterContext.getContext().containsKey(falseKey), "Context map should contain '" + falseKey + "'");
        assertNull(meterContext.getContext().get(falseKey), "Value for '" + falseKey + "' should be null");
        assertFalse(meterContext.getContext().containsKey(trueKey), "Context map should not contain '" + trueKey + "'");
    }

    @Test
    @DisplayName("ctx(name, Float) should add key with Float object value as string")
    void testCtxWithStringAndFloatObject() {
        final String key21 = "key21";
        meterContext.ctx(key21, Float.valueOf(3.14f));
        assertFalse(meterContext.getContext().isEmpty(), "Context map should not be empty after adding key '" + key21 + "'");
        assertTrue(meterContext.getContext().containsKey(key21), "Context map should contain '" + key21 + "'");
        assertEquals("3.14", meterContext.getContext().get(key21), "Value for '" + key21 + "' should be '3.14'");

        final String key21_null = "key21_null";
        meterContext.ctx(key21_null, (Float) null);
        assertFalse(meterContext.getContext().isEmpty(), "Context map should not be empty after adding key '" + key21_null + "'");
        assertTrue(meterContext.getContext().containsKey(key21_null), "Context map should contain '" + key21_null + "'");
        assertEquals(NULL_VALUE, meterContext.getContext().get(key21_null), "Value for null Float should be " + NULL_VALUE);
    }

    @Test
    @DisplayName("ctx(name, Double) should add key with Double object value as string")
    void testCtxWithStringAndDoubleObject() {
        final String key23 = "key23";
        meterContext.ctx(key23, Double.valueOf(2.71828));
        assertFalse(meterContext.getContext().isEmpty(), "Context map should not be empty after adding key '" + key23 + "'");
        assertTrue(meterContext.getContext().containsKey(key23), "Context map should contain '" + key23 + "'");
        assertEquals("2.71828", meterContext.getContext().get(key23), "Value for '" + key23 + "' should be '2.71828'");

        final String key23_null = "key23_null";
        meterContext.ctx(key23_null, (Double) null);
        assertFalse(meterContext.getContext().isEmpty(), "Context map should not be empty after adding key '" + key23_null + "'");
        assertTrue(meterContext.getContext().containsKey(key23_null), "Context map should contain '" + key23_null + "'");
        assertEquals(NULL_VALUE, meterContext.getContext().get(key23_null), "Value for null Double should be " + NULL_VALUE);
    }

    @Test
    @DisplayName("ctx(name, Boolean) should add key with Boolean object value as string")
    void testCtxWithStringAndBooleanObject() {
        final String key25 = "key25";
        meterContext.ctx(key25, Boolean.TRUE);
        assertFalse(meterContext.getContext().isEmpty(), "Context map should not be empty after adding key '" + key25 + "'");
        assertTrue(meterContext.getContext().containsKey(key25), "Context map should contain '" + key25 + "'");
        assertEquals("true", meterContext.getContext().get(key25), "Value for '" + key25 + "' should be 'true'");

        final String key25_null = "key25_null";
        meterContext.ctx(key25_null, (Boolean) null);
        assertFalse(meterContext.getContext().isEmpty(), "Context map should not be empty after adding key '" + key25_null + "'");
        assertTrue(meterContext.getContext().containsKey(key25_null), "Context map should contain '" + key25_null + "'");
        assertEquals(NULL_VALUE, meterContext.getContext().get(key25_null), "Value for null Boolean should be " + NULL_VALUE);
    }

    @Test
    @DisplayName("unctx() should remove existing key from context")
    void testUnctx_existingKey() {
        final String keyToRemove = "keyToRemove";
        meterContext.ctx(keyToRemove);
        assertFalse(meterContext.getContext().isEmpty(), "Context map should not be empty after adding key '" + keyToRemove + "'");
        assertTrue(meterContext.getContext().containsKey(keyToRemove), "Context map should contain '" + keyToRemove + "'");
        meterContext.unctx(keyToRemove);
        assertFalse(meterContext.getContext().containsKey(keyToRemove), "Context should not contain '" + keyToRemove + "' after removal");
    }

    @Test
    @DisplayName("unctx() should do nothing if key does not exist")
    void testUnctx_nonExistingKey() {
        meterContext.unctx("nonExistentKey");
        assertTrue(meterContext.getContext().isEmpty(), "Context map should remain empty after trying to remove non-existent key");
    }

    @Test
    @DisplayName("ctx() with null name should add entry with NULL_VALUE key")
    void testCtxWithNullName() {
        meterContext.ctx(null);
        assertFalse(meterContext.getContext().isEmpty(), "Context map should not be empty after adding null key");
        assertTrue(meterContext.getContext().containsKey(NULL_VALUE), "Context map should contain '" + NULL_VALUE + "'");
        assertNull(meterContext.getContext().get(NULL_VALUE), "Value for '" + NULL_VALUE + "' should be null");
    }

    @Test
    @DisplayName("unctx() with null name should do nothing")
    void testUnctxWithNullName() {
        final String key1 = "key1";
        meterContext.ctx(key1, "value1");
        assertFalse(meterContext.getContext().isEmpty(), "Context map should not be empty after adding key '" + key1 + "'");
        assertTrue(meterContext.getContext().containsKey(key1), "Context map should contain '" + key1 + "'");
        meterContext.unctx(null);
        assertFalse(meterContext.getContext().isEmpty(), "Context map should not change when removing null key");
        assertTrue(meterContext.getContext().containsKey(key1), "Existing key should still be present");
    }

    @Test
    @DisplayName("ctx(condition, trueName) with null trueName should add entry with NULL_VALUE key if condition is true")
    void testCtxWithBooleanAndNullTrueName_trueCondition() {
        meterContext.ctx(true, null);
        assertFalse(meterContext.getContext().isEmpty(), "Context map should not be empty when trueName is null and condition is true");
        assertTrue(meterContext.getContext().containsKey(NULL_VALUE), "Context map should contain '" + NULL_VALUE + "'");
        assertNull(meterContext.getContext().get(NULL_VALUE), "Value for '" + NULL_VALUE + "' should be null");
    }

    @Test
    @DisplayName("ctx(condition, trueName, falseName) with null trueName should add entry with NULL_VALUE key if condition is true")
    void testCtxWithBooleanAndNullTrueNameFalseName_trueCondition() {
        final String falseName = "falseName";
        meterContext.ctx(true, null, falseName);
        assertFalse(meterContext.getContext().isEmpty(), "Context map should not be empty when trueName is null and condition is true");
        assertTrue(meterContext.getContext().containsKey(NULL_VALUE), "Context map should contain '" + NULL_VALUE + "'");
        assertNull(meterContext.getContext().get(NULL_VALUE), "Value for '" + NULL_VALUE + "' should be null");
        assertFalse(meterContext.getContext().containsKey(falseName), "Context map should not contain '" + falseName + "'");
    }

    @Test
    @DisplayName("ctx(condition, trueName, falseName) with null falseName should add entry with NULL_VALUE key if condition is false")
    void testCtxWithBooleanAndTrueNameNullFalseName_falseCondition() {
        final String trueName = "trueName";
        meterContext.ctx(false, trueName, null);
        assertFalse(meterContext.getContext().isEmpty(), "Context map should not be empty when falseName is null and condition is false");
        assertTrue(meterContext.getContext().containsKey(NULL_VALUE), "Context map should contain '" + NULL_VALUE + "'");
        assertNull(meterContext.getContext().get(NULL_VALUE), "Value for '" + NULL_VALUE + "' should be null");
        assertFalse(meterContext.getContext().containsKey(trueName), "Context map should not contain '" + trueName + "'");
    }

    @Test
    @DisplayName("ctx(name, format, args) with null name should add entry with NULL_VALUE key and formatted value")
    void testCtxWithFormattedStringAndNullName() {
        final String format = "formatted %d";
        final int arg = 100;
        meterContext.ctx(null, format, arg);
        assertFalse(meterContext.getContext().isEmpty(), "Context map should not be empty after adding null key with formatted value");
        assertTrue(meterContext.getContext().containsKey(NULL_VALUE), "Context map should contain '" + NULL_VALUE + "'");
        assertEquals(String.format(format, arg), meterContext.getContext().get(NULL_VALUE), "Value for '" + NULL_VALUE + "' should be the formatted string");
    }

    @Test
    @DisplayName("ctx(name, format, args) with null format should add entry with '<null format>' value")
    void testCtxWithFormattedStringAndNullFormat() {
        final String key = "key";
        meterContext.ctx(key, null, 100);
        assertFalse(meterContext.getContext().isEmpty(), "Context map should not be empty after handling null format");
        assertTrue(meterContext.getContext().containsKey(key), "Context map should contain key '" + key + "'");
        assertEquals("<null format>", meterContext.getContext().get(key), "Value for key '" + key + "' should be '<null format>'");
    }

    @Test
    @DisplayName("ctx(name, format, args) with illegal format should add entry with exception message")
    void testCtxWithFormattedStringAndIllegalFormat() {
        final String key = "key";
        final String format = "%d";
        final Object arg = "not an int";
        meterContext.ctx(key, format, arg); // Illegal format
        assertFalse(meterContext.getContext().isEmpty(), "Context map should not be empty after illegal format");
        assertTrue(meterContext.getContext().containsKey(key), "Context map should contain key '" + key + "'");
        assertNotNull(meterContext.getContext().get(key), "Value for key '" + key + "' should not be null");
        // The exact message can vary by JVM/Locale, so we check for presence and type
        assertTrue(meterContext.getContext().get(key).contains("java.lang.String"), "Value should contain format exception message");
    }
}