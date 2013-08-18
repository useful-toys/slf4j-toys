/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.danielferber.slf4jtoys.slf4j.profiler.internal;

import java.io.Serializable;

/**
 *
 * @author Daniel
 */
public interface EventData extends Serializable {
    void reset();
}
