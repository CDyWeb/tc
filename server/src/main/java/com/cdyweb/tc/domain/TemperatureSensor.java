/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cdyweb.tc.domain;

import java.io.IOException;

/**
 *
 * @author Erwin
 */
public interface TemperatureSensor {
  
  public void init() throws IOException;
  public double read() throws IOException;

}
