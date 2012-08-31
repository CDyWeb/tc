package com.vendor.web4robot;

import com.cdyweb.tc.comm.I2CInterface;
import com.cdyweb.tc.comm.RS232Device;
import java.io.IOException;

//http://www.web4robot.com/files/USBtoI2Cboard.pdf

public class USBI2C extends RS232Device implements I2CInterface {
    
    private static int COM_TIMEOUT = 250;

    private static short SET_MODE = 0x96; // Module mode setup
    private static short GET_VERSION = 0x97; // Read software version
    private static short RW_SINGLE = 0x53; // Read/Write single byte for non-registered devices
    private static short RW_MULTIPLE = 0x54; // Read/Write multiple bytes for non-registered devices
    private static short RW_1B_ADDR = 0x55; // Read/Write single or multiple bytes for 1 byte addressed devices
    private static short RW_2B_ADDR = 0x56; // Read/Write single or multiple bytes for 2 byte addressed devices
    private static short RW_1B_EEPROM = 0x65; // Read/Write single or multiple bytes for 1 byte addressed EEPROM
    private static short RW_2B_EEPROM = 0x66; // Read/Write single or multiple bytes for 2 byte addressed EEPROM
    private static short SET_IO = 0x90; // Set data port pin
    private static short CLEAR_IO = 0x91; // Clear data port pin
    private static short GET_IO = 0x92; // Get data port pin value
    private static short GET_ANALOG = 0x93; // Get analog input value
    private static short SET_PWM_V = 0x94; // Set PWM value
    private static short SET_PWM_F = 0x95; // Set PWM frequency
    
    private static short SET_PWM_F_3KHZ = 1;
    private static short SET_PWM_F_12KHZ = 2;
    private static short SET_PWM_F_48KHZ = 3;
    
    private static short SET_MODE_I2C_100 = 1; // I2C 100KBits mode
    private static short SET_MODE_I2C_400 = 2; // I2C 400KBits mode
    private static short SET_MODE_I2C_SERIAL = 3; // Serial mode 9600 baud rate
    
    private short mode=0;
    
    private static USBI2C instance=null;
    
    private USBI2C () {
      // >>> USBI2C Constructor is Private
    }
    
    public static USBI2C getInstance() {
      if (instance==null) instance=new USBI2C();
      return instance;
    }
    
    
    private static byte[] _shortToByteArray(byte[] dest, short value, int pos) {
        dest[pos+0]=(byte)(value >>> 8);
        dest[pos+1]=(byte)value;
        return dest;
    }

    //Module mode setup
    public void set_mode(short new_mode) throws IOException {
      byte[] data=new byte[2];
      data[0]=(byte)SET_MODE;
      data[1]=(byte)new_mode;
      byte[] result = this.sendAndReceive(data, COM_TIMEOUT, 1);
      if (result.length==0) throw new IOException("no answer from device");
      if (result[0]!=0) throw new IOException("device error: "+result[0]);
      this.mode=new_mode;
    }
    
    //Read software version
    public byte get_version() throws IOException {
      byte[] data=new byte[1];
      data[0]=(byte)GET_VERSION;
      byte[] result = this.sendAndReceive(data, COM_TIMEOUT, 1);
      if (result.length==0) throw new IOException("no answer from device");
      return result[0];
    }
    
    //Read single byte for non-registered devices
    public byte[] read_single(short address) throws IOException {
      if ((this.mode==0) || (this.mode==SET_MODE_I2C_SERIAL)) this.set_mode(SET_MODE_I2C_100);
      byte[] data=new byte[2];
      data[0]=(byte)RW_SINGLE;
      data[1]=(byte)address;
      byte[] result = this.sendAndReceive(data, COM_TIMEOUT, 1);
      if (result.length!=1) throw new IOException("no answer from device");
      return result;
    }

    //Write single byte to non-registered devices
    public void write_single(short address, short msg) throws IOException {
      if ((this.mode==0) || (this.mode==SET_MODE_I2C_SERIAL)) this.set_mode(SET_MODE_I2C_100);
      byte[] data=new byte[3];
      data[0]=(byte)RW_SINGLE;
      data[1]=(byte)address;
      data[2]=(byte)msg;
      byte[] result = this.sendAndReceive(data, COM_TIMEOUT, 1);
      if (result.length==0) throw new IOException("no answer from device");
      if (result[0]!=0) throw new IOException("device error: "+result[0]);
    }
    
    //Read multiple bytes for non-registered devices
    public byte[] read_multiple(short address, int length) throws IOException {
      if ((this.mode==0) || (this.mode==SET_MODE_I2C_SERIAL)) this.set_mode(SET_MODE_I2C_100);
      byte[] data=new byte[3];
      data[0]=(byte)RW_MULTIPLE;
      data[1]=(byte)address;
      data[2]=(byte)length;
      byte[] result = this.sendAndReceive(data, COM_TIMEOUT, length);
      if (result.length!=length) throw new IOException("no answer from device");
      return result;
    }
    //Write multiple bytes to non-registered devices
    public void write_multiple(short address, byte[] msg) throws IOException {
      if ((this.mode==0) || (this.mode==SET_MODE_I2C_SERIAL)) this.set_mode(SET_MODE_I2C_100);
      byte[] data=new byte[msg.length+2];
      data[0]=(byte)RW_MULTIPLE;
      data[1]=(byte)address;
      System.arraycopy(msg, 0, data, 2, msg.length);
      byte[] result = this.sendAndReceive(data, COM_TIMEOUT, 1);
      if (result.length==0) throw new IOException("no answer from device");
      if (result[0]!=0) throw new IOException("device error: "+result[0]);
    }
    
    //Read/Write single or multiple bytes for 1 byte addressed devices
    public void rw_1b_addr() throws IOException {
      throw new IOException("Not implemented");
    }
    
    //Read/Write single or multiple bytes for 2 byte addressed devices
    public void rw_2b_addr() throws IOException {
      throw new IOException("Not implemented");
    }
    
    //Read/Write single or multiple bytes for 1 byte addressed EEPROM
    public void rw_1b_eeprom() throws IOException {
      throw new IOException("Not implemented");
    }
    
    //Read/Write single or multiple bytes for 2 byte addressed EEPROM
    public void rw_2b_eeprom() throws IOException {
      throw new IOException("Not implemented");
    }
    
    //Set data port pin
    public void set_io() throws IOException {
      throw new IOException("Not implemented");
    }
    
    //Clear data port pin
    public void clear_io() throws IOException {
      throw new IOException("Not implemented");
    }
    
    //Get data port pin value
    public void get_io() throws IOException {
      throw new IOException("Not implemented");
    }
    
    //Get analog input value
    public void get_analog() throws IOException {
      throw new IOException("Not implemented");
    }
    
    //Set PWM value
    public void set_pwm_v(short value) throws IOException {
      byte[] data=new byte[3];
      data[0]=(byte)SET_PWM_V;
      data=_shortToByteArray(data,value,1);
      byte[] result = this.sendAndReceive(data, COM_TIMEOUT, 1);
      if (result.length==0) throw new IOException("no answer from device");
      if (result[0]!=0) throw new IOException("device error: "+result[0]);
    }
    
    //Set PWM frequency
    public void set_pwm_f(short frequency) throws IOException {
      byte[] data=new byte[2];
      data[0]=(byte)SET_PWM_F;
      data[1]=(byte)frequency;
      byte[] result = this.sendAndReceive(data, COM_TIMEOUT, 1);
      if (result.length==0) throw new IOException("no answer from device");
      if (result[0]!=0) throw new IOException("device error: "+result[0]);
    }
    


    public byte[] i2c_read(short address, int length) throws IOException {
      if (length<=0) {
        throw new IOException("no data");
      }
      if (length==1) {
        return this.read_single(address);
      } else {
        return this.read_multiple(address, length);
      }
    }
    public void i2c_write(short address, byte[] data) throws IOException {
      if (data.length==0) {
        throw new IOException("no data");
      }
      if (data.length==1) {
        this.write_single(address, data[0]);
      } else {
        this.write_multiple(address, data);
      }
    }
    
}
