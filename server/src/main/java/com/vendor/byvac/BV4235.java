package com.vendor.byvac;

import com.cdyweb.tc.comm.I2CDevice;
import com.cdyweb.tc.domain.TemperatureSensor;
import java.io.IOException;

public class BV4235 extends I2CDevice implements TemperatureSensor {

  private short address = 0x96; //I2C address 0x96 8bit; 0x4b 7 bit address
  
  private byte REG_TEMP = 0x00;
  private byte REG_CONF = 0x01;
  private byte REG_HYST = 0x02;
  private byte REG_LIMIT = 0x03;
  
  public static double decode_temperature(byte[] resp) {
    int i1=resp[0] << 4;
    int i2=0x000F & (resp[1] >> 4);
    double f=i1+i2;
    return f/16;
  }
  
  public double read() throws IOException {
    byte[] data=new byte[1];
    data[0] = REG_TEMP;
    i2c_write(address,data);
    byte[] resp = i2c_read(address, 2);
    //return (((int)result[0]) << 8) | (int)result[1] ;
    return decode_temperature(resp);
  }

  public void init() throws IOException {
    set_12bit_resolution();
  }
  
  public void set_limit() throws IOException {
    throw new IOException("not implemented");
  }
  public void get_limit() throws IOException {
    throw new IOException("not implemented");
  }
  public void set_hysteresis() throws IOException {
    throw new IOException("not implemented");
  }
  public void get_hysteresis() throws IOException {
    throw new IOException("not implemented");
  }
  public void set_config(byte config) throws IOException {
    byte[] data=new byte[2];
    data[0] = REG_CONF;
    data[1] = config;
    i2c_write(address, data);
  }
  public void set_12bit_resolution() throws IOException {
    this.set_config((byte)0x60); //bit 5&6
  }
  public byte get_config() throws IOException {
    byte[] data=new byte[1];
    data[0] = REG_CONF;
    i2c_write(address,data);
    byte[] result = i2c_read(address, 1);
    return result[0];
  }

}
