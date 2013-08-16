/*
 * Copyright 2012 Daniel Felix Ferber
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package br.com.danielferber.slf4jtoys.slf4j.profiler.internal;

import java.io.EOFException;
import java.io.IOException;

public class Parser {

    /* Internal parser state. */
    private int start;
    private String charsString;
    private char[] chars;
    private int lenght;
    /* Symbols used in parseable messages. */
    public char STRING_QUOTE = '\\';
    public char STRING_DELIM = '"';
    public char MAP_CLOSE = ']';
    public char MAP_OPEN = '[';
    public char MAP_SPACE = ' ';
    public char MAP_SEPARATOR = ',';
    public char MAP_EQUAL = ':';
    public char PROPERTY_DIV = '|';
    public char PROPERTY_EQUALS = '=';
    public char PROPERTY_SPACE = ' ';
    public char PROPERTY_SEPARATOR = ';';
    public char DATA_OPEN = '(';
    public char DATA_CLOSE = ')';
    /* Time units used in parseable messages. */
    public static final double[] TIME_FACTORS = new double[]{1000.0, 1000.0, 1000.0, 60.0, 60.0};
    public static final String[] TIME_UNITS = new String[]{"ns", "us", "ms", "s", "m", "h"};
    /* Other? */
    private static String STRING_DELIM_QUOTED_STR;
    private static String STRING_DELIM_STR;

    public Parser() {
        super();
        reset();
    }

    public void writeQuotedString(StringBuilder sb, String string) {
        sb.append(STRING_DELIM);
        sb.append(string.replace(Parser.STRING_DELIM_STR, Parser.STRING_DELIM_QUOTED_STR));
        sb.append(STRING_DELIM);
    }

    public String readQuotedString() throws IOException {
        if (start >= lenght) {
            throw new EOFException();
        }
        char c = chars[start];
        if (c != STRING_DELIM) {
            throw new IOException("missing quotes");
        }
        start++;
        int end = start;
        StringBuilder sb = new StringBuilder();
        while (end < lenght) {
            c = chars[end];
            if (c == STRING_DELIM) {
                sb.append(charsString.substring(start, end));
                start = end + 1;
                break;
            } else if (c == STRING_QUOTE) {
                sb.append(charsString.substring(start, end));
                start = end + 1;
                if (start >= lenght) {
                    throw new EOFException();
                }
                c = chars[start];
                if (c == STRING_DELIM) {
                    sb.append(STRING_DELIM);
                } else {
                    sb.append(STRING_QUOTE);
                    sb.append(c);
                }
                start++;
                end = start + 1;
            } else {
                end++;
            }
        }
        if (end >= lenght) {
            throw new EOFException();
        }
        return sb.toString();
    }

    public String readUuid() throws EOFException {
        if (start >= lenght) {
            throw new EOFException();
        }

        int end = start;
        while (end < lenght) {
            char c = chars[end];
            if (!Character.isJavaIdentifierPart(c) && c != '.') {
                break;
            }
            end++;
        }

        String substring = charsString.substring(start, end);
        start = end;
        return substring;
    }

    public String readIdentifierString() throws IOException {
        if (start >= lenght) {
            throw new EOFException();
        }

        char c = chars[start];
        if (!Character.isJavaIdentifierStart(c)) {
            throw new IOException("invalid identifier");
        }
        int end = start + 1;
        while (end < lenght) {
            c = chars[end];
            if (!Character.isJavaIdentifierPart(c) && c != '.') {
                break;
            }
            end++;
        }

        String substring = charsString.substring(start, end);
        start = end;
        return substring;
    }

    public void readOperator(char operator) throws IOException {
        while (start < lenght && Character.isWhitespace(chars[start])) {
            start++;
        }
        if (start >= lenght) {
            throw new EOFException();
        }
        char c = chars[start++];
        if (c != operator) {
            throw new IOException("missing '" + operator + "'");
        }
        while (start < lenght && Character.isWhitespace(chars[start])) {
            start++;
        }
    }

    public boolean readOptionalOperator(char operator) {
        while (start < lenght && Character.isWhitespace(chars[start])) {
            start++;
        }
        if (start >= lenght) {
            return false;
        }
        char c = chars[start];
        if (c != operator) {
            return false;
        }
        start++;
        while (start < lenght && Character.isWhitespace(chars[start])) {
            start++;
        }
        return true;
    }

    public long readLong() throws IOException {
        if (start >= lenght) {
            throw new EOFException();
        }

        char c = chars[start];
        int end;
        if (c == '-') {
            // Se for sinal de menos, então testa um caractere a mais para obter o dígito.
            if ((start + 1) >= lenght) {
                throw new EOFException();
            }
            c = chars[start + 1];
            end = start + 2;
        } else {
            end = start + 1;
        }
        if (!Character.isDigit(c)) {
            throw new NumberFormatException();
        }

        while (end < lenght) {
            c = chars[end];
            if (!Character.isDigit(c)) {
                break;
            }
            end++;
        }

        String substring = charsString.substring(start, end);
        start = end;
        long valor;
        try {
            valor = Long.parseLong(substring);
        } catch (NumberFormatException e) {
            throw new IOException(e);
        }
        return valor;
    }

    public int readInt() throws IOException {
        return (int) readLong();
    }

    public double readDouble() throws IOException {
        if (start >= lenght) {
            throw new EOFException();
        }

        char c = chars[start];
        int end;
        if (c == '-') {
            // Se for sinal de menos, então testa um caractere a mais para obter o dígito.
            if ((start + 1) >= lenght) {
                throw new EOFException();
            }
            c = chars[start + 1];
            end = start + 2;
        } else {
            end = start + 1;
        }
        if (!Character.isDigit(c) && c != '.') {
            throw new NumberFormatException();
        }

        while (end < lenght) {
            c = chars[end];
            if (!Character.isDigit(c) && c != '.') {
                break;
            }
            end++;
        }

        String substring = charsString.substring(start, end);
        start = end;
        double valor;
        try {
            valor = Double.parseDouble(substring);
        } catch (NumberFormatException e) {
            throw new IOException(e);
        }
        return valor;
    }

    public void reset(String encodedData) {
        chars = encodedData.toCharArray();
        start = 0;
        lenght = chars.length;
        charsString = encodedData;
        reset();

    }

    protected void reset() {
        StringBuilder sb = new StringBuilder();
        sb.append(STRING_DELIM);
        Parser.STRING_DELIM_STR = sb.toString();
        sb = new StringBuilder();
        sb.append(STRING_QUOTE);
        sb.append(STRING_DELIM);
        Parser.STRING_DELIM_QUOTED_STR = sb.toString();
    }

    public static String bestUnit(double value, String[] timeUnits, double[] timeFactors) {
        int last = timeUnits.length - 1;
        int index = 0;
        double limit = timeFactors[index] * 1.1;
        double modifiedValue = value;
        while (index != last && modifiedValue > limit) {
            modifiedValue /= timeFactors[index];
            limit = timeFactors[index] * 1.1;
            index++;
        }
        return String.format("%.1f%s", modifiedValue, timeUnits[index]);
    }
}
