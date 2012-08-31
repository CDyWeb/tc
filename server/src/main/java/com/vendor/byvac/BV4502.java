package com.vendor.byvac;

import com.cdyweb.tc.comm.I2CDevice;
import com.cdyweb.tc.comm.I2CInterface;
import com.cdyweb.tc.domain.Relay;
import java.io.IOException;

public class BV4502 implements Relay, I2CInterface  {
  
  private short address = 0x62;
  
  private static short SWITCH_A = 0x01; // Relay A on/off
  private static short SWITCH_B = 0x02; // Relay B on/off
  private static short TIMED_A_ON = 0x03; // Relay A timed on
  private static short TIMED_A_OFF = 0x04; // Relay A timed off
  private static short TIMED_A_DISPLAY = 0x05; // Relay A timer display
  private static short TIMED_B_ON = 0x06; // Relay B timed on
  private static short TIMED_B_OFF = 0x07; // Relay B timed off
  private static short TIMED_B_DISPLAY = 0x08; // Relay B counter display
  
  private static I2CInterface device = null;
  private static BV4502 channel_A = null;
  private static BV4502 channel_B = null;
  
  private char channel = 'A';
  private Boolean status = null;

  private BV4502(char channel) {
    //private constructor
    this.channel=channel;
  }

  public static BV4502 getInstance(I2CInterface device, char channel) {
    BV4502.device = device;
    switch (channel) {
      case 'A' :
        if (channel_A==null) channel_A=new BV4502('A');
        return channel_A;
      case 'B' :
        if (channel_B==null) channel_B=new BV4502('B');
        return channel_B;
    }
    throw new IllegalArgumentException();
  }

  public void init(Boolean firstStatus) throws IOException {
    setStatus(firstStatus);
  }

  public Boolean getStatus() throws IOException {
    return status;
  }

  public void setStatus(Boolean newStatus) throws IOException {
    if (status==newStatus) return;
    System.out.println("Switching "+channel+" to: "+(newStatus?"ON":"OFF"));
    switch (channel) {
      case 'A' : switch_a(newStatus); break;
      case 'B' : switch_b(newStatus); break;
    }
    status=newStatus;
  }

  public byte[] i2c_read(short address, int length) throws IOException {
    return device.i2c_read(address,length);
  }

  public void i2c_write(short address, byte[] data) throws IOException {
    device.i2c_write(address, data);
  }

  public void switch_a(boolean state) throws IOException {
    byte[] data = new byte[2];
    data[0] = (byte) SWITCH_A;
    data[1] = state? (byte)0x01 : (byte)0x00;
    i2c_write(address,data);
  }

  public void switch_b(boolean state) throws IOException {
    byte[] data = new byte[2];
    data[0] = (byte) SWITCH_B;
    data[1] = state? (byte)0x01 : (byte)0x00;
    i2c_write(address,data);
  }

  public void timed_a_on() throws IOException {
    throw new IOException("Not implemented");
  }

  public void timed_a_off() throws IOException {
    throw new IOException("Not implemented");
  }

  public void timed_a_display() throws IOException {
    throw new IOException("Not implemented");
  }

  public void timed_b_on() throws IOException {
    throw new IOException("Not implemented");
  }

  public void timed_b_off() throws IOException {
    throw new IOException("Not implemented");
  }

  public void timed_b_display() throws IOException {
    throw new IOException("Not implemented");
  }

}