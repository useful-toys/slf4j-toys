/*
 */
package br.com.danielferber.slf4jtoys.slf4j.profiler.internal;

/**
 *
 * @author x7ws - Daniel Felix Ferber
 */
public class Syntax {

    /* Symbols used in parseable messages. */
    public static final char STRING_QUOTE = '\\';
    public static final char STRING_DELIM = '"';
    public static final char MAP_CLOSE = ']';
    public static final char MAP_OPEN = '[';
    public static final char MAP_SPACE = ' ';
    public static final char MAP_SEPARATOR = ',';
    public static final char MAP_EQUAL = ':';
    public static final char PROPERTY_DIV = '|';
    public static final char PROPERTY_EQUALS = '=';
//    public static final char PROPERTY_SPACE = ' ';
    public static final char PROPERTY_SEPARATOR = ';';
    public static final char DATA_OPEN = '(';
    public static final char DATA_CLOSE = ')';
    /* Some symbols, as string, for convenience. */
    public static final String STRING_DELIM_QUOTED_STR;
    public static final String STRING_DELIM_STR;

    static {
        StringBuilder sb = new StringBuilder();
        sb.append(STRING_DELIM);
        STRING_DELIM_STR = sb.toString();
        sb = new StringBuilder();
        sb.append(STRING_QUOTE);
        sb.append(STRING_DELIM);
        STRING_DELIM_QUOTED_STR = sb.toString();
    }
}
