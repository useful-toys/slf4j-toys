/*
 * Copyright 2026 Daniel Felix Ferber
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.usefultoys.test.ResetSessionConfig;
import org.usefultoys.test.ValidateCharset;
import org.usefultoys.test.WithLocale;

import static org.junit.jupiter.api.Assertions.*;
import static org.usefultoys.slf4j.meter.MeterData.NULL_VALUE;

/**
 * Unit tests for {@link MeterContext}.
 * <p>
 * Tests validate that MeterContext correctly manages the context map,
 * supporting various data types, null values, and conditional additions.
 * <p>
 * <b>Coverage:</b>
 * <ul>
 *   <li><b>Context Addition:</b> Verifies that ctx() methods correctly add entries for all supported types (int, long, boolean, float, double, Object, String, formatted String).</li>
 *   <li><b>Null Handling:</b> Ensures that null keys and values are handled gracefully, using NULL_VALUE where appropriate.</li>
 *   <li><b>Conditional Addition:</b> Validates that ctx(condition, ...) only adds entries when the condition is met.</li>
 *   <li><b>Context Removal:</b> Tests that unctx() correctly removes entries from the context map.</li>
 *   <li><b>Formatting:</b> Verifies that formatted strings are correctly generated and added to the context.</li>
 *   <li><b>Edge Cases:</b> Covers illegal format strings and null format strings.</li>
 * </ul>
 *
 * @author Co-authored-by: GitHub Copilot using Gemini 3 Flash (Preview)
 */
@ValidateCharset
@WithLocale("en")
@ResetSessionConfig
class MeterContextTest {

    // Implementação de teste da interface MeterContext
    static class TestMeterContext extends MeterData implements MeterContext {
    }

    private TestMeterContext meterContext;

    @BeforeEach
    void setUp() {
        meterContext = new TestMeterContext();
    }

    @Test
    @DisplayName("ctx() with key-only should add key with null value")
    void shouldAddKeyWithNullValueWhenCtxWithKeyOnly() {
        // When: ctx is called with only the key
        meterContext.ctx("key1");

        // Then: the context map should contain the key with a null value
        assertFalse(meterContext.getContext().isEmpty(), "should not be empty after adding key");
        assertTrue(meterContext.getContext().containsKey("key1"), "should contain the added key");
        assertNull(meterContext.getContext().get("key1"), "should have null value for the key");
    }

    @Test
    @DisplayName("ctx() with null key should add entry with NULL_VALUE key")
    void shouldAddNullValueKeyWhenCtxWithNullKey() {
        // When: ctx is called with the null key
        meterContext.ctx(null);

        // Then: the context map should contain NULL_VALUE as key with a null value
        assertFalse(meterContext.getContext().isEmpty(), "should not be empty after adding null key");
        assertTrue(meterContext.getContext().containsKey(NULL_VALUE), "should contain NULL_VALUE as key");
        assertNull(meterContext.getContext().get(NULL_VALUE), "should have null value for NULL_VALUE key");
    }

    @Test
    @DisplayName("ctx(name, int) should add key with integer value as string")
    void shouldAddKeyWithIntValueAsStringWhenCtxWithKeyAndInt() {
        // When: ctx is called with key and int
        meterContext.ctx("key2", 42);

        // Then: the context map should contain the key with the integer value as string
        assertFalse(meterContext.getContext().isEmpty(), "should not be empty after adding key");
        assertTrue(meterContext.getContext().containsKey("key2"), "should contain the added key");
        assertEquals("42", meterContext.getContext().get("key2"), "should have '42' as value");
    }

    @Test
    @DisplayName("ctx(name, long) should add key with long value as string")
    void shouldAddKeyWithLongValueAsStringWhenCtxWithKeyAndLong() {
        // When: ctx is called with key and long
        meterContext.ctx("key3", 123456789L);

        // Then: the context map should contain the key with the long value as string
        assertFalse(meterContext.getContext().isEmpty(), "should not be empty after adding key");
        assertTrue(meterContext.getContext().containsKey("key3"), "should contain the added key");
        assertEquals("123456789", meterContext.getContext().get("key3"), "should have '123456789' as value");
    }

    @Test
    @DisplayName("ctx(name, boolean) should add key with boolean value as string")
    void shouldAddKeyWithBooleanValueAsStringWhenCtxWithKeyAndBoolean() {
        // When: ctx is called with key and boolean
        meterContext.ctx("key4", true);

        // Then: the context map should contain the key with the boolean value as string
        assertFalse(meterContext.getContext().isEmpty(), "should not be empty after adding key");
        assertTrue(meterContext.getContext().containsKey("key4"), "should contain the added key");
        assertEquals("true", meterContext.getContext().get("key4"), "should have 'true' as value");
    }

    @Test
    @DisplayName("ctx(name, float) should add key with float value as string")
    void shouldAddKeyWithFloatValueAsStringWhenCtxWithKeyAndFloat() {
        // When: ctx is called with key and float
        meterContext.ctx("key5", 3.14f);

        // Then: the context map should contain the key with the float value as string
        assertFalse(meterContext.getContext().isEmpty(), "should not be empty after adding key");
        assertTrue(meterContext.getContext().containsKey("key5"), "should contain the added key");
        assertEquals("3.14", meterContext.getContext().get("key5"), "should have '3.14' as value");
    }

    @Test
    @DisplayName("ctx(name, double) should add key with double value as string")
    void shouldAddKeyWithDoubleValueAsStringWhenCtxWithKeyAndDouble() {
        // When: ctx is called with key and double
        meterContext.ctx("key6", 2.71828);

        // Then: the context map should contain the key with the double value as string
        assertFalse(meterContext.getContext().isEmpty(), "should not be empty after adding key");
        assertTrue(meterContext.getContext().containsKey("key6"), "should contain the added key");
        assertEquals("2.71828", meterContext.getContext().get("key6"), "should have '2.71828' as value");
    }

    @Test
    @DisplayName("ctx(name, Object) should add key with object's toString() value")
    void shouldAddKeyWithObjectToStringValueWhenCtxWithKeyAndObject() {
        // Given: an object with custom toString
        final Object value = new Object() {
            @Override
            public String toString() {
                return "customObject";
            }
        };

        // When: ctx is called with key and object
        meterContext.ctx("key7", value);

        // Then: the context map should contain the key with the object's toString value
        assertFalse(meterContext.getContext().isEmpty(), "should not be empty after adding key");
        assertTrue(meterContext.getContext().containsKey("key7"), "should contain the added key");
        assertEquals("customObject", meterContext.getContext().get("key7"), "should have 'customObject' as value");
    }

    @Test
    @DisplayName("ctx(name, Object) with null object should add key with NULL_VALUE")
    void shouldAddKeyWithNullValueWhenCtxWithKeyAndNullObject() {
        // When: ctx is called with key and null object
        meterContext.ctx("key7_null", null);

        // Then: the context map should contain the key with NULL_VALUE
        assertFalse(meterContext.getContext().isEmpty(), "should not be empty after adding key");
        assertTrue(meterContext.getContext().containsKey("key7_null"), "should contain the added key");
        assertEquals(NULL_VALUE, meterContext.getContext().get("key7_null"), "should have NULL_VALUE as value");
    }

    @Test
    @DisplayName("ctx(name, String) should add key with string value")
    void shouldAddKeyWithStringValueWhenCtxWithKeyAndString() {
        // When: ctx is called with key and string
        meterContext.ctx("key8", "value8");

        // Then: the context map should contain the key with the string value
        assertFalse(meterContext.getContext().isEmpty(), "should not be empty after adding key");
        assertTrue(meterContext.getContext().containsKey("key8"), "should contain the added key");
        assertEquals("value8", meterContext.getContext().get("key8"), "should have 'value8' as value");
    }

    @Test
    @DisplayName("ctx(name, String) with null string should add key with NULL_VALUE")
    void shouldAddKeyWithNullValueWhenCtxWithKeyAndNullString() {
        // When: ctx is called with key and null string
        meterContext.ctx("key8_null", null);

        // Then: the context map should contain the key with NULL_VALUE
        assertFalse(meterContext.getContext().isEmpty(), "should not be empty after adding key");
        assertTrue(meterContext.getContext().containsKey("key8_null"), "should contain the added key");
        assertEquals(NULL_VALUE, meterContext.getContext().get("key8_null"), "should have NULL_VALUE as value");
    }

    @Test
    @DisplayName("ctx(name, format, args) should add key with formatted string value")
    void shouldAddKeyWithFormattedStringValueWhenCtxWithKeyAndFormat() {
        // When: ctx is called with key, format, and arg
        meterContext.ctx("key9", "formatted %d", 100);

        // Then: the context map should contain the key with the formatted string value
        assertFalse(meterContext.getContext().isEmpty(), "should not be empty after adding key");
        assertTrue(meterContext.getContext().containsKey("key9"), "should contain the added key");
        assertEquals("formatted 100", meterContext.getContext().get("key9"), "should have 'formatted 100' as value");
    }

    @Test
    @DisplayName("ctx(name, Integer) should add key with Integer object value as string")
    void shouldAddKeyWithIntValueAsStringWhenCtxWithKeyAndIntegerObject() {
        // When: ctx is called with key and Integer object
        meterContext.ctx("key19", Integer.valueOf(123));

        // Then: the context map should contain the key with the integer value as string
        assertFalse(meterContext.getContext().isEmpty(), "should not be empty after adding key");
        assertTrue(meterContext.getContext().containsKey("key19"), "should contain the added key");
        assertEquals("123", meterContext.getContext().get("key19"), "should have '123' as value");
    }

    @Test
    @DisplayName("ctx(name, Integer) with null Integer should add key with NULL_VALUE")
    void shouldAddKeyWithNullValueWhenCtxWithKeyAndNullIntegerObject() {
        // When: ctx is called with key and null Integer
        meterContext.ctx("key19_null", (Integer) null);

        // Then: the context map should contain the key with NULL_VALUE
        assertFalse(meterContext.getContext().isEmpty(), "should not be empty after adding key");
        assertTrue(meterContext.getContext().containsKey("key19_null"), "should contain the added key");
        assertEquals(NULL_VALUE, meterContext.getContext().get("key19_null"), "should have NULL_VALUE as value");
    }

    @Test
    @DisplayName("ctx(name, Long) should add key with Long object value as string")
    void shouldAddKeyWithLongValueAsStringWhenCtxWithKeyAndLongObject() {
        // When: ctx is called with key and Long object
        meterContext.ctx("key20", Long.valueOf(456L));

        // Then: the context map should contain the key with the long value as string
        assertFalse(meterContext.getContext().isEmpty(), "should not be empty after adding key");
        assertTrue(meterContext.getContext().containsKey("key20"), "should contain the added key");
        assertEquals("456", meterContext.getContext().get("key20"), "should have '456' as value");
    }

    @Test
    @DisplayName("ctx(name, Long) with null Long should add key with NULL_VALUE")
    void shouldAddKeyWithNullValueWhenCtxWithKeyAndNullLongObject() {
        // When: ctx is called with key and null Long
        meterContext.ctx("key20_null", (Long) null);

        // Then: the context map should contain the key with NULL_VALUE
        assertFalse(meterContext.getContext().isEmpty(), "should not be empty after adding key");
        assertTrue(meterContext.getContext().containsKey("key20_null"), "should contain the added key");
        assertEquals(NULL_VALUE, meterContext.getContext().get("key20_null"), "should have NULL_VALUE as value");
    }

    @Test
    @DisplayName("ctx(condition, trueName) should add trueName if condition is true")
    void shouldAddTrueNameWhenCtxWithTrueCondition() {
        // When: ctx is called with true condition and key
        meterContext.ctx(true, "valueTrue");

        // Then: the context map should contain the key with null value
        assertFalse(meterContext.getContext().isEmpty(), "should not be empty after adding key");
        assertTrue(meterContext.getContext().containsKey("valueTrue"), "should contain the added key");
        assertNull(meterContext.getContext().get("valueTrue"), "should have null as value");
    }

    @Test
    @DisplayName("ctx(condition, trueName) should not add trueName if condition is false")
    void shouldNotAddTrueNameWhenCtxWithFalseCondition() {
        // When: ctx is called with false condition and key
        meterContext.ctx(false, "valueFalse");

        // Then: the context map should remain empty
        assertTrue(meterContext.getContext().isEmpty(), "should remain empty when condition is false");
        assertFalse(meterContext.getContext().containsKey("valueFalse"), "should not contain the key");
    }

    @Test
    @DisplayName("ctx(condition, trueName, falseName) should add trueName if condition is true")
    void shouldAddTrueNameWhenCtxWithTrueConditionAndTwoNames() {
        // When: ctx is called with true condition and two keys
        meterContext.ctx(true, "valueTrue", "valueFalse");

        // Then: the context map should contain the true key with null value
        assertFalse(meterContext.getContext().isEmpty(), "should not be empty after adding key");
        assertTrue(meterContext.getContext().containsKey("valueTrue"), "should contain the true key");
        assertNull(meterContext.getContext().get("valueTrue"), "should have null as value");
        assertFalse(meterContext.getContext().containsKey("valueFalse"), "should not contain the false key");
    }

    @Test
    @DisplayName("ctx(condition, trueName, falseName) should add falseName if condition is false")
    void shouldAddFalseNameWhenCtxWithFalseConditionAndTwoNames() {
        // When: ctx is called with false condition and two keys
        meterContext.ctx(false, "valueTrue", "valueFalse");

        // Then: the context map should contain the false key with null value
        assertFalse(meterContext.getContext().isEmpty(), "should not be empty after adding key");
        assertTrue(meterContext.getContext().containsKey("valueFalse"), "should contain the false key");
        assertNull(meterContext.getContext().get("valueFalse"), "should have null as value");
        assertFalse(meterContext.getContext().containsKey("valueTrue"), "should not contain the true key");
    }

    @Test
    @DisplayName("ctx(name, Float) should add key with Float object value as string")
    void shouldAddKeyWithFloatValueAsStringWhenCtxWithKeyAndFloatObject() {
        // When: ctx is called with key and Float object
        meterContext.ctx("key21", Float.valueOf(3.14f));

        // Then: the context map should contain the key with the float value as string
        assertFalse(meterContext.getContext().isEmpty(), "should not be empty after adding key");
        assertTrue(meterContext.getContext().containsKey("key21"), "should contain the added key");
        assertEquals("3.14", meterContext.getContext().get("key21"), "should have '3.14' as value");
    }

    @Test
    @DisplayName("ctx(name, Float) with null Float should add key with NULL_VALUE")
    void shouldAddKeyWithNullValueWhenCtxWithKeyAndNullFloatObject() {
        // When: ctx is called with key and null Float
        meterContext.ctx("key21_null", (Float) null);

        // Then: the context map should contain the key with NULL_VALUE
        assertFalse(meterContext.getContext().isEmpty(), "should not be empty after adding key");
        assertTrue(meterContext.getContext().containsKey("key21_null"), "should contain the added key");
        assertEquals(NULL_VALUE, meterContext.getContext().get("key21_null"), "should have NULL_VALUE as value");
    }

    @Test
    @DisplayName("ctx(name, Double) should add key with Double object value as string")
    void shouldAddKeyWithDoubleValueAsStringWhenCtxWithKeyAndDoubleObject() {
        // When: ctx is called with key and Double object
        meterContext.ctx("key23", Double.valueOf(2.71828));

        // Then: the context map should contain the key with the double value as string
        assertFalse(meterContext.getContext().isEmpty(), "should not be empty after adding key");
        assertTrue(meterContext.getContext().containsKey("key23"), "should contain the added key");
        assertEquals("2.71828", meterContext.getContext().get("key23"), "should have '2.71828' as value");
    }

    @Test
    @DisplayName("ctx(name, Double) with null Double should add key with NULL_VALUE")
    void shouldAddKeyWithNullValueWhenCtxWithKeyAndNullDoubleObject() {
        // When: ctx is called with key and null Double
        meterContext.ctx("key23_null", (Double) null);

        // Then: the context map should contain the key with NULL_VALUE
        assertFalse(meterContext.getContext().isEmpty(), "should not be empty after adding key");
        assertTrue(meterContext.getContext().containsKey("key23_null"), "should contain the added key");
        assertEquals(NULL_VALUE, meterContext.getContext().get("key23_null"), "should have NULL_VALUE as value");
    }

    @Test
    @DisplayName("ctx(name, Boolean) should add key with Boolean object value as string")
    void shouldAddKeyWithBooleanValueAsStringWhenCtxWithKeyAndBooleanObject() {
        // When: ctx is called with key and Boolean object
        meterContext.ctx("key25", Boolean.TRUE);

        // Then: the context map should contain the key with the boolean value as string
        assertFalse(meterContext.getContext().isEmpty(), "should not be empty after adding key");
        assertTrue(meterContext.getContext().containsKey("key25"), "should contain the added key");
        assertEquals("true", meterContext.getContext().get("key25"), "should have 'true' as value");
    }

    @Test
    @DisplayName("ctx(name, Boolean) with null Boolean should add key with NULL_VALUE")
    void shouldAddKeyWithNullValueWhenCtxWithKeyAndNullBooleanObject() {
        // When: ctx is called with key and null Boolean
        meterContext.ctx("key25_null", (Boolean) null);

        // Then: the context map should contain the key with NULL_VALUE
        assertFalse(meterContext.getContext().isEmpty(), "should not be empty after adding key");
        assertTrue(meterContext.getContext().containsKey("key25_null"), "should contain the added key");
        assertEquals(NULL_VALUE, meterContext.getContext().get("key25_null"), "should have NULL_VALUE as value");
    }

    @Test
    @DisplayName("unctx() should remove existing key from context")
    void shouldRemoveExistingKeyWhenUnctx() {
        // Given: a key added to context
        meterContext.ctx("keyToRemove");
        assertTrue(meterContext.getContext().containsKey("keyToRemove"), "should contain the key before removal");

        // When: unctx is called with the key
        meterContext.unctx("keyToRemove");

        // Then: the context map should not contain the key
        assertFalse(meterContext.getContext().containsKey("keyToRemove"), "should not contain the key after removal");
    }

    @Test
    @DisplayName("unctx() should do nothing if key does not exist")
    void shouldDoNothingWhenUnctxWithNonExistingKey() {
        // When: unctx is called with non-existing key
        meterContext.unctx("nonExistentKey");

        // Then: the context map should remain empty
        assertTrue(meterContext.getContext().isEmpty(), "should remain empty");
    }

    @Test
    @DisplayName("ctx() with null name should add entry with NULL_VALUE key")
    void shouldAddNullValueKeyWhenCtxWithNullName() {
        // When: ctx is called with null name
        meterContext.ctx(null);

        // Then: the context map should contain NULL_VALUE as key
        assertFalse(meterContext.getContext().isEmpty(), "should not be empty after adding null key");
        assertTrue(meterContext.getContext().containsKey(NULL_VALUE), "should contain NULL_VALUE as key");
        assertNull(meterContext.getContext().get(NULL_VALUE), "should have null as value");
    }

    @Test
    @DisplayName("unctx() with null name should do nothing")
    void shouldDoNothingWhenUnctxWithNullName() {
        // Given: a key added to context
        meterContext.ctx("key1", "value1");
        assertTrue(meterContext.getContext().containsKey("key1"), "should contain the key");

        // When: unctx is called with null name
        meterContext.unctx(null);

        // Then: the context map should remain unchanged
        assertFalse(meterContext.getContext().isEmpty(), "should not be empty");
        assertTrue(meterContext.getContext().containsKey("key1"), "should still contain the key");
    }

    @Test
    @DisplayName("ctx(condition, trueName) with null trueName should add entry with NULL_VALUE key if condition is true")
    void shouldAddNullValueKeyWhenCtxWithTrueConditionAndNullTrueName() {
        // When: ctx is called with true condition and null trueName
        meterContext.ctx(true, null);

        // Then: the context map should contain NULL_VALUE as key
        assertFalse(meterContext.getContext().isEmpty(), "should not be empty");
        assertTrue(meterContext.getContext().containsKey(NULL_VALUE), "should contain NULL_VALUE as key");
        assertNull(meterContext.getContext().get(NULL_VALUE), "should have null as value");
    }

    @Test
    @DisplayName("ctx(condition, trueName, falseName) with null trueName should add entry with NULL_VALUE key if condition is true")
    void shouldAddNullValueKeyWhenCtxWithTrueConditionAndNullTrueNameAndFalseName() {
        // When: ctx is called with true condition, null trueName, and falseName
        meterContext.ctx(true, null, "falseName");

        // Then: the context map should contain NULL_VALUE as key
        assertFalse(meterContext.getContext().isEmpty(), "should not be empty");
        assertTrue(meterContext.getContext().containsKey(NULL_VALUE), "should contain NULL_VALUE as key");
        assertNull(meterContext.getContext().get(NULL_VALUE), "should have null as value");
        assertFalse(meterContext.getContext().containsKey("falseName"), "should not contain falseName");
    }

    @Test
    @DisplayName("ctx(condition, trueName, falseName) with null falseName should add entry with NULL_VALUE key if condition is false")
    void shouldAddNullValueKeyWhenCtxWithFalseConditionAndTrueNameAndNullFalseName() {
        // When: ctx is called with false condition, trueName, and null falseName
        meterContext.ctx(false, "trueName", null);

        // Then: the context map should contain NULL_VALUE as key
        assertFalse(meterContext.getContext().isEmpty(), "should not be empty");
        assertTrue(meterContext.getContext().containsKey(NULL_VALUE), "should contain NULL_VALUE as key");
        assertNull(meterContext.getContext().get(NULL_VALUE), "should have null as value");
        assertFalse(meterContext.getContext().containsKey("trueName"), "should not contain trueName");
    }

    @Test
    @DisplayName("ctx(name, format, args) with null name should add entry with NULL_VALUE key and formatted value")
    void shouldAddNullValueKeyWithFormattedValueWhenCtxWithNullNameAndFormat() {
        // When: ctx is called with null name, format, and arg
        meterContext.ctx(null, "formatted %d", 100);

        // Then: the context map should contain NULL_VALUE as key with formatted value
        assertFalse(meterContext.getContext().isEmpty(), "should not be empty");
        assertTrue(meterContext.getContext().containsKey(NULL_VALUE), "should contain NULL_VALUE as key");
        assertEquals("formatted 100", meterContext.getContext().get(NULL_VALUE), "should have formatted value");
    }

    @Test
    @DisplayName("ctx(name, format, args) with null format should add entry with '<null format>' value")
    void shouldAddNullFormatValueWhenCtxWithKeyAndNullFormat() {
        // When: ctx is called with key and null format
        meterContext.ctx("key", null, 100);

        // Then: the context map should contain the key with '<null format>' value
        assertFalse(meterContext.getContext().isEmpty(), "should not be empty");
        assertTrue(meterContext.getContext().containsKey("key"), "should contain the key");
        assertEquals("<null format>", meterContext.getContext().get("key"), "should have '<null format>' as value");
    }

    @Test
    @DisplayName("ctx(name, format, args) with illegal format should add entry with exception message")
    void shouldAddExceptionMessageWhenCtxWithIllegalFormat() {
        // When: ctx is called with illegal format
        meterContext.ctx("key", "%d", "not an int");

        // Then: the context map should contain the key with non-null value (exception message)
        assertFalse(meterContext.getContext().isEmpty(), "should not be empty");
        assertTrue(meterContext.getContext().containsKey("key"), "should contain the key");
        assertNotNull(meterContext.getContext().get("key"), "should have non-null value");
        // The exact message can vary by JVM/Locale, so we check for presence and type
        assertTrue(meterContext.getContext().get("key").contains("java.lang.String"), "Value should contain format exception message");
    }
}
