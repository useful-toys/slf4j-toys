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
package org.usefultoys.slf4j.examples;

import org.usefultoys.slf4j.utils.UnitFormatter;

/**
 *
 * @author Daniel
 */
public class TimeUnitExample {
     public static void main(final String[] args) {
         System.out.println(UnitFormatter.nanoseconds(0L));
         System.out.println(UnitFormatter.nanoseconds(126L));
         System.out.println(UnitFormatter.nanoseconds(1464L));
         System.out.println(UnitFormatter.nanoseconds(1356525L));
         System.out.println(UnitFormatter.nanoseconds(1624534526L));
         System.out.println(UnitFormatter.nanoseconds(25364636544L));
         System.out.println(UnitFormatter.nanoseconds(657350564575L));
         System.out.println(UnitFormatter.nanoseconds(8645768936573L));
     }   
}