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
package org.usefultoys.slf4j.logback;

public final class AnsiColors {
    private AnsiColors() {
        // utility class
    }

    public final static String BOLD = "1;";

    public final static String BLACK = "30";
    public final static String RED = "31";
    public final static String GREEN = "32";
    public final static String YELLOW = "33";
    public final static String BLUE = "34";
    public final static String MAGENTA = "35";
    public final static String CYAN = "36";
    public final static String WHITE = "37";
    public final static String DEFAULT = "39";

    public final static String BRIGHT_BLACK = "90";
    public final static String BRIGHT_RED = "91";
    public final static String BRIGHT_GREEN = "92";
    public final static String BRIGHT_YELLOW = "93";
    public final static String BRIGHT_BLUE = "94";
    public final static String BRIGHT_MAGENTA = "95";
    public final static String BRIGHT_CYAN = "96";
    public final static String BRIGHT_WHITE = "97";
}

