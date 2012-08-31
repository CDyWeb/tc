package com.cdyweb.tc;

import com.cdyweb.tc.comm.RS232Device;

public class NullModemSim extends RS232Device implements Runnable {
  
  public static void main(String[] args) {
    (new Thread(new NullModemSim())).start();
  }
  
  public void run() {

    this.setPortName("COM11");

    byte[] response;
    
    float temp=20;

    try {
      if (!this.isOpen()) this.open();
      
      while(true) {
        byte[] buf = this.receive(0);
        if (buf.length>0) {
          
          switch (buf[0]) {
            case (byte)0x96 :
              System.out.println("SIM: Set Mode command received");
              response=new byte[1];
              response[0]=(byte)0x00;
              this.send(response);
              break;
              
            case (byte)0x53 :
              System.out.println("SIM: RW_SINGLE command received");
              if ((buf[1] & 0x01) == 0x01) {
                System.out.println("SIM: READ address "+buf[1]);
                response=new byte[1];
                this.send(response);
              } else {
                System.out.println("SIM: WRITE address "+buf[1]);
                response=new byte[1];
                response[0]=(byte)0x00;
                this.send(response);
              }
              break;

            case (byte)0x54 :
              System.out.println("SIM: RW_MULTIPLE command received");
              if ((buf[1] & 0x01) == 0x01) {
                System.out.println("SIM: READ address "+buf[1]+" - "+buf[2]+" bytes");
                if (buf[1]==(byte)0x97) {
                  System.out.println("SIM Temp: "+temp);
                  response=new byte[2];
                  response[0]=(byte)Math.floor(temp);
                  response[1]=(byte)Math.round((temp-Math.floor(temp))*256);
                  temp+=Math.random();
                  temp-=Math.random();
                } else {
                  response=new byte[1];
                  response[0]=(byte)0x00;
                }
                this.send(response);
              } else {
                System.out.println("SIM: WRITE address "+buf[1]);
                response=new byte[1];
                response[0]=(byte)0x00;
                this.send(response);
              }
              break;

            default:
              System.out.println("SIM: Unknown command received: "+buf[0]);
              response=new byte[1];
              response[0]=(byte)0xFF;
              this.send(response);
          }
          
        }
        Thread.currentThread().sleep(100);
      }
      
    } catch (Exception ex) {
      System.out.print(ex);
      System.exit(-1);
    }
  }
  
}
