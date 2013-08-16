/*
 */
package br.com.danielferber.slf4jtoys.slf4j.profiler.internal;

/**
 *
 * @author x7ws - Daniel Felix Ferber
 */
public class Syntax {

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
    /* Some symbols, as string, for convenience. */
    public String STRING_DELIM_QUOTED_STR;
    public String STRING_DELIM_STR;
    /* Time units used in parseable messages. */
    public static final double[] TIME_FACTORS = new double[]{1000.0, 1000.0, 1000.0, 60.0, 60.0};
    public static final String[] TIME_UNITS = new String[]{"ns", "us", "ms", "s", "m", "h"};

    public Syntax() {
        reset();
    }

    void reset() {
        StringBuilder sb = new StringBuilder();
        sb.append(STRING_DELIM);
        STRING_DELIM_STR = sb.toString();
        sb = new StringBuilder();
        sb.append(STRING_QUOTE);
        sb.append(STRING_DELIM);
        STRING_DELIM_QUOTED_STR = sb.toString();
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
