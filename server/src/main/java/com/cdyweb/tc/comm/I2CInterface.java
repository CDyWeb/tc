package com.cdyweb.tc.comm;

import java.io.IOException;

public interface I2CInterface {

  public byte[] i2c_read(short address, int length) throws IOException;

  public void i2c_write(short address, byte[] data) throws IOException;
  
}
