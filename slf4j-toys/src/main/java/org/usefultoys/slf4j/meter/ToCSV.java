/*
 * Copyright 2024 Daniel Felix Ferber
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
package org.usefultoys.slf4j.meter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public final class ToCSV {

    private ToCSV() {
    }

    public static void main(final String argv[]) {
        if (argv.length < 1) {
            System.out.println("ToCSV file.log [prefix] ");
            return;
        }
        final String fileName = argv[0];
        //final String prefix = argv.length >= 2 ? argv[2] : null;

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(fileName));
            String line;
            final MeterData data = new MeterData();
            System.out.println("uuid,category,position,waiting,execution,success,slow");
            while ((line = reader.readLine()) != null) {
                final boolean plausible = data.read(line);
                if (plausible) {
                    final StringBuilder sb = new StringBuilder();
                    sb.append(data.getSessionUuid());
                    sb.append(',');
                    sb.append(data.getCategory());
                    sb.append(',');
                    sb.append(data.getPosition());
                    sb.append(',');
                    sb.append(data.getWaitingTime()/1000000.0);
                    sb.append(',');
                    sb.append(data.getExecutionTime()/1000000.0);
                    sb.append(',');
                    sb.append(data.isOK());
                    sb.append(',');
                    sb.append(data.isSlow());
                    sb.append(',');
                    System.out.println(sb);
                }
            }
        } catch (final Exception e) {
            System.err.println(e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException ex) {
                    System.err.println(ex.getMessage());
                }
            }
        }

    }
}
