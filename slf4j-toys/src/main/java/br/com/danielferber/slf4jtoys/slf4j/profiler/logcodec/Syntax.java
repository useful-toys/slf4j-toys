/*
 */
package br.com.danielferber.slf4jtoys.slf4j.profiler.logcodec;

/**
 *
 * @author x7ws - Daniel Felix Ferber
 */
class Syntax {

    /* Symbols used in parseable messages. */
    static final char STRING_QUOTE = '\\';
    static final char STRING_DELIM = '"';
    static final char MAP_CLOSE = ']';
    static final char MAP_OPEN = '[';
//    static final char MAP_SPACE = ' ';
    static final char MAP_SEPARATOR = ',';
    static final char MAP_EQUAL = ':';
    static final char PROPERTY_DIV = '|';
    static final char PROPERTY_EQUALS = '=';
//    static final char PROPERTY_SPACE = ' ';
    static final char PROPERTY_SEPARATOR = ';';
    static final char DATA_OPEN = '(';
    static final char DATA_CLOSE = ')';
    /* Some symbols, as string, for convenience. */
    static final String STRING_DELIM_QUOTED_STR;
    static final String STRING_DELIM_STR;

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
