/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cdyweb.tc;

import com.vendor.byvac.BV4235;

/**
 *
 * @author Erwin
 */
public class TestDecode {
  public static void main(String[] args) {
    byte[] buf=new byte[2];
    buf[0]=25;
    buf[1]=0x38;
    float f1 = BV4235.decode_temperature(buf);
    System.out.println(f1);
    
    buf[0]=25;
    buf[1]=(byte)0xF0;
    float f2 = BV4235.decode_temperature(buf);
    System.out.println(f2);

    buf[0]=-25;
    buf[1]=(byte)0xF0;
    float f3 = BV4235.decode_temperature(buf);
    System.out.println(f3);

    buf[0]=-25;
    buf[1]=0x38;
    float f4 = BV4235.decode_temperature(buf);
    System.out.println(f4);
  }
}
