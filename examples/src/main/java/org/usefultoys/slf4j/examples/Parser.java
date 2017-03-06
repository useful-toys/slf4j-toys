/*
 * Copyright 2017 Daniel Felix Ferber
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
package org.usefultoys.slf4j.examples;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.usefultoys.slf4j.meter.MeterData;
import org.usefultoys.slf4j.watcher.WatcherData;

/**
 *
 * @author x7ws
 */
public class Parser {

    public static void main(final String argv[]) {
        if (argv.length < 1) {
            System.out.println("ToCSV file.log [prefix] ");
            return;
        }
        final String logFileName = argv[0];

        /* Value objects that temporarily  keep parsed attributes. */
        final MeterData meterData = new MeterData();
        final WatcherData watcherData = new WatcherData();
        String line;

        try (BufferedReader reader = new BufferedReader(new FileReader(logFileName))) {
            while ((line = reader.readLine()) != null) {
                if (meterData.read(line)) {
                    /* line contains a valid M{...} structure. Values were copied into metarData.
                       Do something with meterData. */
                } else if (meterData.read(line)) {
                    /* line contains a valid M{...} structure. Values were copied into metarData.
                       Do something with meterData. */
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
