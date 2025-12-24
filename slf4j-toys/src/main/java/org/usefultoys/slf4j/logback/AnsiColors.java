/*
 * Copyright 2025 Daniel Felix Ferber
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
package org.usefultoys.slf4j.logback;

import lombok.experimental.UtilityClass;

/**
 * Defines ANSI escape codes for various text colors and styles, used for console output.
 * These constants are typically used in Logback converters to add color to log messages.
 * This class is package-private and not intended for use outside this library.
 */
@UtilityClass
class AnsiColors {
    /** ANSI escape code for bold text. */
    public static final String BOLD = "1;";

    /** ANSI escape code for black foreground color. */
    public static final String BLACK = "30";
    /** ANSI escape code for red foreground color. */
    public static final String RED = "31";
    /** ANSI escape code for green foreground color. */
    public static final String GREEN = "32";
    /** ANSI escape code for yellow foreground color. */
    public static final String YELLOW = "33";
    /** ANSI escape code for blue foreground color. */
    public static final String BLUE = "34";
    /** ANSI escape code for magenta foreground color. */
    public static final String MAGENTA = "35";
    /** ANSI escape code for cyan foreground color. */
    public static final String CYAN = "36";
    /** ANSI escape code for white foreground color. */
    public static final String WHITE = "37";
    /** ANSI escape code for default foreground color. */
    public static final String DEFAULT = "39";

    /** ANSI escape code for bright black (gray) foreground color. */
    public static final String BRIGHT_BLACK = "90";
    /** ANSI escape code for bright red foreground color. */
    public static final String BRIGHT_RED = "91";
    /** ANSI escape code for bright green foreground color. */
    public static final String BRIGHT_GREEN = "92";
    /** ANSI escape code for bright yellow foreground color. */
    public static final String BRIGHT_YELLOW = "93";
    /** ANSI escape code for bright blue foreground color. */
    public static final String BRIGHT_BLUE = "94";
    /** ANSI escape code for bright magenta foreground color. */
    public static final String BRIGHT_MAGENTA = "95";
    /** ANSI escape code for bright cyan foreground color. */
    public static final String BRIGHT_CYAN = "96";
    /** ANSI escape code for bright white foreground color. */
    public static final String BRIGHT_WHITE = "97";
}
