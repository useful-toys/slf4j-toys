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
package org.usefultoys.slf4j.internal;

import java.io.IOException;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author Daniel
 */
public class EventDataTest {

    public EventDataTest() {
    }

    @Test
    public void isCompletelyEqualTest() {
        final EventData a = createEventData();
        final EventData b = createEventData();

        assertTrue(a.isCompletelyEqualsTo(b));
        assertTrue(b.isCompletelyEqualsTo(a));

        populateEventData(a);

        populateEventData(b);

        assertTrue(a.isCompletelyEqualsTo(b));
        assertTrue(b.isCompletelyEqualsTo(a));

        b.eventPosition = 11;
        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));
        b.eventPosition = 1;

        b.sessionUuid = "uuiduuid";
        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));
        b.sessionUuid = "uuid";

        b.time = 22;
        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));
        b.time = 2;

        assertTrue(a.isCompletelyEqualsTo(b));
        assertTrue(b.isCompletelyEqualsTo(a));
    }

    @Test
    public void resetTest() {
        final EventData a = createEventData();
        final EventData b = createEventData();

        populateEventData(b);

        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));

        b.reset();

        assertTrue(a.isCompletelyEqualsTo(b));
        assertTrue(b.isCompletelyEqualsTo(a));
    }

    @Test
    public void writeReadTest1() {
        final EventData a = createEventData();

        final String s = a.write(new StringBuilder(), 'E').toString();
        System.out.println(s);

        final EventData b = createEventData();
        b.read(s, 'E');

        assertTrue(a.isCompletelyEqualsTo(b));
        assertTrue(b.isCompletelyEqualsTo(a));
    }

    @Test
    public void writeReadTest2() {
        final EventData a = createEventData();
        populateEventData(a);

        final String s = a.write(new StringBuilder(), 'E').toString();
        System.out.println(s);

        final EventData b = createEventData();
        assertTrue(b.read(s, 'E'));

        assertTrue(a.isCompletelyEqualsTo(b));
        assertTrue(b.isCompletelyEqualsTo(a));
    }

    public static void populateEventData(EventData a) {
        a.eventPosition = 1;
        a.sessionUuid = "uuid";
        a.time = 2;
    }

    private EventData createEventData() {
        return new EventData() {
            private static final long serialVersionUID = 1L;

            @Override
            public StringBuilder readableString(final StringBuilder builder) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            protected void writePropertiesImpl(final EventWriter w) {
                // empty
            }

            @Override
            protected boolean readPropertyImpl(final EventReader r, final String key) {
                return false;
            }

            @Override
            protected void resetImpl() {
                // empty
            }

            @Override
            protected boolean isCompletelyEqualsImpl(final EventData other) {
                return true;
            }
        };
    }
}
