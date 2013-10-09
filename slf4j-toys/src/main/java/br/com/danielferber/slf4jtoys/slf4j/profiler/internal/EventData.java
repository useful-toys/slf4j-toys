/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.danielferber.slf4jtoys.slf4j.profiler.internal;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

/**
 *
 * @author Daniel
 */
public abstract class EventData extends Patterns implements Serializable {

    protected abstract void reset();

     /**
     * Writes the event into the supplied StringBuilder. 
     *
     * @param sb The shared StringBuilder that receives the encoded message.
     */
    public final void write(StringBuilder sb) {
        EventWriter w = new EventWriter(sb);
        w.open(getHeaderChar());
        writeProperties(w);
        w.close();
    }

    /**
     * Implementation shall resort to the supplied MessageWRiter encode each
     * relevant property.
     *
     * @param w The shared MessageWriter
     */
    protected abstract void writeProperties(EventWriter w);

    protected abstract char getHeaderChar();

    /**
     * Read the event from the supplied string message. If the method fails to
     * recognize the message, then method returns false and might have load
     * inconsistent data into the supplied event.
     *
     * @param message The string that is supposed to contain a serialized event
     * @return true the string message contains a serialized event, false
     * otherwise
     * @throws IOException
     */
    public final boolean read(String message) throws IOException {
        String plausibleMessage = Patterns.extractPlausibleMessage(getHeaderChar(), message);
        if (plausibleMessage == null) {
            return false;
        }
        reset();
        EventReader r = new EventReader();
        r.reset(message);

        while (r.hasMore()) {
            String propertyName = r.readPropertyName();
            if (!readProperty(r, propertyName)) {
                throw new IOException("unknown property");
            }
        }
        return true;
    }

    /**
     *
     * Implementation shall resort to the supplied MessageReader to decode data
     * for one individual property.
     *
     * @param r The helper EventReader parsing the message
     * @param propertyName The property to be extracted from the string
     * @return true if the property was recognized, false otherwise
     * @throws IOException
     */
    protected abstract boolean readProperty(EventReader r, String propertyName) throws IOException;
}
