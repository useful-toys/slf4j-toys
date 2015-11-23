/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
