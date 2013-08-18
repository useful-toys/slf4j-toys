/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.danielferber.slf4jtoys.slf4j.profiler.logcodec;

import br.com.danielferber.slf4jtoys.slf4j.profiler.internal.EventData;
import java.io.IOException;

/**
 *
 * @author Daniel
 */
public abstract class MessageCodec<T extends EventData> {

    private final char messagePrefix;

    protected MessageCodec(char messagePrefix) {
        this.messagePrefix = messagePrefix;
    }

    public final void writeLogMessage(StringBuilder sb, MessageWriter w, T e) {
        w.reset(sb);
        w.openData(messagePrefix);
        writeProperties(w, e);
        w.closeData();
    }

    protected abstract void writeProperties(MessageWriter w, T e);

    protected String extractPlausibleMessage(String s) {
        return MessageReader.extractPlausibleMessage(messagePrefix, s);
    }

    public boolean readLogMessage(String message, MessageReader p, T e) throws IOException {
        String plausibleMessage = extractPlausibleMessage(message);
        if (plausibleMessage == null) {
            return false;
        }
        e.reset();
        while (p.hasMore()) {
            String propertyName = p.readIdentifier();
            if (!readProperty(p, propertyName, e)) {
                throw new IOException("unknown property");
            }
        }
        return true;
    }

    protected abstract boolean readProperty(MessageReader p, String propertyName, T e) throws IOException;
}
