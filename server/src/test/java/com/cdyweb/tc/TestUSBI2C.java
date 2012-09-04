/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cdyweb.tc;

import com.vendor.byvac.BV4502;
import com.vendor.web4robot.USBI2C;

/**
 *
 * @author Erwin
 */
public class TestUSBI2C {
  public static void main(String[] args) {
    try {
      USBI2C device = USBI2C.getInstance();
      
      device.setPortName("COM8");
      device.setBaudRate(9600);
      
      //device.set_mode(USBI2C.SET_MODE_I2C_SERIAL);
      
      int version = device.get_version();
      System.out.println("USBI2C version: "+version);
      
      BV4502 relayA=BV4502.getInstance(device, 'A');
      BV4502 relayB=BV4502.getInstance(device, 'B');
      
      byte[] versionRelay = relayA.read_version();
      System.out.println("BV4502 version: "+versionRelay[0]+" "+versionRelay[1]);

      relayA.setStatus(true);
      
      Thread.currentThread().sleep(1000);
      
      relayA.setStatus(false);
      
      Thread.currentThread().sleep(1000);
      
      relayB.setStatus(true);
      
      Thread.currentThread().sleep(1000);
      
      relayB.setStatus(false);
      
      System.out.println("All done testing");
      System.exit(0);
      
    } catch (Exception ex) {
      System.out.println(ex);
      System.exit(-1);
    }
  }  
}
