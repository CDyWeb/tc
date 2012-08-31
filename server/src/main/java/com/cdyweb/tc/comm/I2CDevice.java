package com.cdyweb.tc.comm;

import java.io.IOException;

public abstract class I2CDevice implements I2CInterface {
  
  private I2CInterface i2cInterface;

  public I2CInterface getI2cInterface() {
    return i2cInterface;
  }

  public void setI2cInterface(I2CInterface i2cInterface) {
    this.i2cInterface = i2cInterface;
  }
  
  public byte[] i2c_read(short address, int length) throws IOException {
    if (i2cInterface==null) throw new IOException("No device given");
    address |= 0x0001; //bit 0 must be 1
    return i2cInterface.i2c_read(address, length);
  };

  public void i2c_write(short address, byte[] data) throws IOException {
    if (i2cInterface==null) throw new IOException("No device given");
    address &= 0x00FE; //bit 0 must be 0
    i2cInterface.i2c_write(address, data);
  };

}
