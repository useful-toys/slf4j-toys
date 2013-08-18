/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.danielferber.slf4jtoys.slf4j.profiler.meter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author Daniel Felix Ferber
 */
public class MeterLogMessageHelper {

    private static final String UUID = "uuid";
    private static final String COUNTER = "c";
    private static final String THROWABLE = "tr";
    private static final String TIME = "t";
    private static final String CONTEXT = "ctx";
    private static final String THREAD = "th";
    private static final String DEPTH = "d";
    
        public static void writeToString(Parser p, MeterEvent e, StringBuilder buffer) {
        buffer.append(p.DATA_OPEN);

        /* name */
        buffer.append(e.name);

        /* message */
        if (e.message != null) {
            buffer.append(p.PROPERTY_DIV);
            p.writeQuotedString(buffer, e.message);
            buffer.append(p.PROPERTY_SPACE);
        }

        /* counter */
        if (e.counter > 0) {
            buffer.append(p.PROPERTY_SEPARATOR);
            buffer.append(p.PROPERTY_SPACE);
            buffer.append(MeterEvent.COUNTER);
            buffer.append(p.PROPERTY_EQUALS);
            buffer.append(e.counter);
        }

        /* uuid */
        if (e.uuid != null) {
            buffer.append(p.PROPERTY_SEPARATOR);
            buffer.append(p.PROPERTY_SPACE);
            buffer.append(MeterEvent.UUID);
            buffer.append(p.PROPERTY_EQUALS);
            buffer.append(e.uuid);
        }

        if (e.depthContext != 0 || e.depthCount != 0) {
            buffer.append(p.PROPERTY_SEPARATOR);
            buffer.append(p.PROPERTY_SPACE);
            buffer.append(MeterEvent.DEPTH);
            buffer.append(p.PROPERTY_EQUALS);
            buffer.append(e.depthContext);
            buffer.append(p.PROPERTY_DIV);
            buffer.append(e.depthCount);
        }

        if (e.threadStartId != 0) {
            buffer.append(p.PROPERTY_SEPARATOR);
            buffer.append(p.PROPERTY_SPACE);
            buffer.append(MeterEvent.THREAD);
            buffer.append(p.PROPERTY_EQUALS);
            buffer.append(e.threadStartId);
            buffer.append(p.PROPERTY_DIV);
            p.writeQuotedString(buffer, e.threadStartName);
            if (e.threadStopId != 0) {
                buffer.append(p.PROPERTY_DIV);
                buffer.append(e.threadStopId);
                buffer.append(p.PROPERTY_DIV);
                p.writeQuotedString(buffer, e.threadStopName);
            }
        }

        /* exception */
        if (e.exceptionClass != null) {
            buffer.append(p.PROPERTY_SEPARATOR);
            buffer.append(p.PROPERTY_SPACE);
            buffer.append(MeterEvent.THROWABLE);
            buffer.append(p.PROPERTY_EQUALS);
            buffer.append(e.exceptionClass);
            if (e.exceptionMessage != null) {
                buffer.append(p.PROPERTY_DIV);
                p.writeQuotedString(buffer, e.exceptionMessage);
            }
        }

        /* createTime, startTime, stopTime */
        if (e.createTime > 0) {
            buffer.append(p.PROPERTY_SEPARATOR);
            buffer.append(p.PROPERTY_SPACE);
            buffer.append(MeterEvent.TIME);
            buffer.append(p.PROPERTY_EQUALS);
            buffer.append(e.createTime);
            if (e.startTime != 0) {
                buffer.append(p.PROPERTY_DIV);
                buffer.append(e.startTime);
                if (e.stopTime != 0) {
                    buffer.append(p.PROPERTY_DIV);
                    buffer.append(e.stopTime);
                }
            }
        }

        /* context */
        Map<String, String> globalContext = MeterFactory.getContext();
        if ((e.context != null && !e.context.isEmpty()) || (globalContext != null && !globalContext.isEmpty())) {
            buffer.append(p.PROPERTY_SEPARATOR);
            buffer.append(p.PROPERTY_SPACE);
            buffer.append(MeterEvent.CONTEXT);
            buffer.append(p.PROPERTY_EQUALS);
            buffer.append(p.MAP_OPEN);
            boolean primeiro = true;
            if (e.context != null && !e.context.isEmpty()) {
                Iterator<Map.Entry<String, String>> i = e.context.entrySet().iterator();
                while (i.hasNext()) {
                    Map.Entry<String, String> entry = i.next();
                    if (primeiro) {
                        primeiro = false;
                    } else {
                        buffer.append(p.MAP_SEPARATOR);
                        buffer.append(p.MAP_SPACE);
                    }
                    buffer.append(entry.getKey());
                    if (entry.getValue() != null) {
                        buffer.append(p.MAP_EQUAL);
                        p.writeQuotedString(buffer, entry.getValue());
                    }
                }
            }
            if (globalContext != null && !globalContext.isEmpty()) {
                Iterator<Map.Entry<String, String>> i = globalContext.entrySet().iterator();
                while (i.hasNext()) {
                    Map.Entry<String, String> entry = i.next();
                    // não imprime valor pois contexto local tem preferência sobre o global.
                    if (primeiro) {
                        primeiro = false;
                    } else {
                        buffer.append(p.MAP_SEPARATOR);
                        buffer.append(p.MAP_SPACE);
                    }
                    if (e.context.containsKey(entry.getKey())) {
                        continue;
                    }
                    buffer.append(entry.getKey());
                    if (entry.getValue() != null) {
                        buffer.append(p.MAP_EQUAL);
                        buffer.append(p.STRING_DELIM);
                        buffer.append(entry.getValue().toString());
                        buffer.append(p.STRING_DELIM);
                    }
                }
            }
            buffer.append(p.MAP_CLOSE);
        }
        buffer.append(p.DATA_CLOSE);
    }

    public static void readFromString(Parser p, MeterEvent e, String encodedData) throws IOException {
        /* Reseta todos os atributos. */
        e.name = null;
        e.message = null;
        e.counter = 0;
        e.createTime = e.startTime = e.stopTime = 0;
        e.exceptionClass = null;
        e.exceptionMessage = null;
        e.context = null;

        p.reset(encodedData);

        /* O nome é obrigatório. */
        e.name = p.readIdentifierString();
        if (p.readOptionalOperator(p.PROPERTY_DIV)) {
            /* A descrição é opcional. */
            e.message = p.readQuotedString();
        }


        if (!p.readOptionalOperator(p.PROPERTY_SEPARATOR)) {
            return;
        }

        String propertyName = p.readIdentifierString();
        while (propertyName != null) {
            p.readOperator(p.PROPERTY_EQUALS);
            if (MeterEvent.COUNTER.equals(propertyName)) {
                e.counter = p.readLong();
            } else if (MeterEvent.UUID.equals(propertyName)) {
                e.uuid = p.readUuid();
            } else if (MeterEvent.THREAD.equals(propertyName)) {
                e.threadStartId = p.readLong();
                p.readOperator(p.PROPERTY_DIV);
                e.threadStartName = p.readQuotedString();
                if (p.readOptionalOperator(p.PROPERTY_DIV)) {
                    e.threadStopId = p.readLong();
                    p.readOperator(p.PROPERTY_DIV);
                    e.threadStopName = p.readQuotedString();
                }
            } else if (MeterEvent.DEPTH.equals(propertyName)) {
                e.depthContext = p.readLong();
                p.readOperator(p.PROPERTY_DIV);
                e.depthCount = p.readLong();
            } else if (MeterEvent.THROWABLE.equals(propertyName)) {
                e.exceptionClass = p.readIdentifierString();
                if (p.readOptionalOperator(p.PROPERTY_DIV)) {
                    e.exceptionMessage = p.readQuotedString();
                }
            } else if (MeterEvent.TIME.equals(propertyName)) {
                e.createTime = p.readLong();
                if (p.readOptionalOperator(p.PROPERTY_DIV)) {
                    e.startTime = p.readLong();
                    if (p.readOptionalOperator(p.PROPERTY_DIV)) {
                        e.stopTime = p.readLong();
                    }
                }
            } else if (MeterEvent.CONTEXT.equals(propertyName)) {
                e.context = new HashMap<String, String>();
                p.readOperator('[');
                if (!p.readOptionalOperator(']')) {
                    do {
                        String key = p.readIdentifierString();
                        String value = null;
                        if (p.readOptionalOperator(p.MAP_EQUAL)) {
                            value = p.readQuotedString();
                        }
                        e.context.put(key, value);
                    } while (p.readOptionalOperator(p.MAP_SEPARATOR));
                }
                p.readOperator(p.MAP_CLOSE);
            } else {
                // property desconhecida, ignora
            }
            if (p.readOptionalOperator(p.PROPERTY_SEPARATOR)) {
                propertyName = p.readIdentifierString();
            } else {
                break;
            }
        }
    }

}
