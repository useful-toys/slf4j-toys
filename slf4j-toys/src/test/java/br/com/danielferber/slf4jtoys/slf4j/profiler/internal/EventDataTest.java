/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.danielferber.slf4jtoys.slf4j.profiler.internal;

import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Daniel
 */
public class EventDataTest {

    public EventDataTest() {
    }

    @Test
    public void isCompletelyEqualTest() {
        EventData a = createEventData();
        EventData b = createEventData();

        assertTrue(a.isCompletelyEqualsTo(b));
        assertTrue(b.isCompletelyEqualsTo(a));

        a.eventCategory = "a";
        a.eventPosition = 1;
        a.sessionUuid = "b";
        a.time = 2;

        b.eventCategory = "a";
        b.eventPosition = 1;
        b.sessionUuid = "b";
        b.time = 2;

        assertTrue(a.isCompletelyEqualsTo(b));
        assertTrue(b.isCompletelyEqualsTo(a));

        b.eventCategory = "aa";

        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));

        b.eventCategory = "a";
        b.eventPosition = 11;

        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));

        b.eventPosition = 1;
        b.sessionUuid = "bb";

        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));

        b.sessionUuid = "b";
        b.time = 22;

        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));
    }

    @Test
    public void resetTest() {
        EventData a = createEventData();
        EventData b = createEventData();

        b.eventCategory = "a";
        b.eventPosition = 1;
        b.sessionUuid = "b";
        b.time = 2;

        assertFalse(a.isCompletelyEqualsTo(b));
        assertFalse(b.isCompletelyEqualsTo(a));

        b.reset();

        assertTrue(a.isCompletelyEqualsTo(b));
        assertTrue(b.isCompletelyEqualsTo(a));
    }

    @Test
    public void isSameAsTest() {
        EventData a = createEventData();
        EventData b = createEventData();

        assertTrue(a.isSameAs(b));
        assertTrue(b.isSameAs(a));

        a.eventCategory = "a";
        a.eventPosition = 1;
        a.sessionUuid = "b";
        a.time = 2;

        b.eventCategory = "a";
        b.eventPosition = 1;
        b.sessionUuid = "b";
        b.time = 2;

        assertTrue(a.isSameAs(b));
        assertTrue(b.isSameAs(a));

        b.eventCategory = "aa";

        assertFalse(a.isSameAs(b));
        assertFalse(b.isSameAs(a));

        b.eventCategory = "a";
        b.eventPosition = 11;

        assertFalse(a.isSameAs(b));
        assertFalse(b.isSameAs(a));

        b.eventPosition = 1;
        b.sessionUuid = "bb";

        assertFalse(a.isSameAs(b));
        assertFalse(b.isSameAs(a));

        b.sessionUuid = "b";
        b.time = 22;

        assertTrue(a.isSameAs(b));
        assertTrue(b.isSameAs(a));
    }

    @Test
    public void equalsHashTest() {
        EventData a = createEventData();
        EventData b = createEventData();

        assertTrue(a.equals(b));
        assertTrue(b.equals(a));
        assertTrue(a.hashCode() == b.hashCode());

        a.eventCategory = "a";
        a.eventPosition = 1;
        a.sessionUuid = "b";
        a.time = 2;

        b.eventCategory = "a";
        b.eventPosition = 1;
        b.sessionUuid = "b";
        b.time = 2;

        assertTrue(a.equals(b));
        assertTrue(b.equals(a));
        assertTrue(a.hashCode() == b.hashCode());

        b.eventCategory = "aa";

        assertFalse(a.equals(b));
        assertFalse(b.equals(a));
        assertFalse(a.hashCode() == b.hashCode());

        b.eventCategory = "a";
        b.eventPosition = 11;

        assertFalse(a.equals(b));
        assertFalse(b.equals(a));
        assertFalse(a.hashCode() == b.hashCode());

        b.eventPosition = 1;
        b.sessionUuid = "bb";

        assertFalse(a.equals(b));
        assertFalse(b.equals(a));
        assertFalse(a.hashCode() == b.hashCode());

        b.sessionUuid = "b";
        b.time = 22;

        assertTrue(a.equals(b));
        assertTrue(b.equals(a));
        assertTrue(a.hashCode() == b.hashCode());
    }

    @Test
    public void writeReadTest1() {
        EventData a = createEventData();

        String s = a.write(new StringBuilder(), 'E').toString();
        System.out.println(s);

        EventData b = createEventData();
        b.read(s, 'E');

        assertTrue(a.isCompletelyEqualsTo(b));
        assertTrue(b.isCompletelyEqualsTo(a));
    }

    @Test
    public void writeReadTest2() {
        EventData a = createEventData();
        a.eventCategory = "a";
        a.eventPosition = 1;
        a.sessionUuid = "b";
        a.time = 2;

        String s = a.write(new StringBuilder(), 'E').toString();
        System.out.println(s);

        EventData b = createEventData();
        assertTrue(b.read(s, 'E'));

        assertTrue(a.isCompletelyEqualsTo(b));
        assertTrue(b.isCompletelyEqualsTo(a));
    }

    private EventData createEventData() {
        return new EventData() {

            @Override
            public StringBuilder readableString(StringBuilder builder) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            protected void writePropertiesImpl(EventWriter w) {
                // empty
            }

            @Override
            protected boolean readPropertyImpl(EventReader r, String key) throws IOException {
                return false;
            }

            @Override
            protected void resetImpl() {
                // empty
            }

            @Override
            protected boolean isCompletelyEqualsImpl(EventData other) {
                return true;
            }
        };
    }
}
