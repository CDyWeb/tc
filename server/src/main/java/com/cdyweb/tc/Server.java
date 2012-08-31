package com.cdyweb.tc;

import com.cdyweb.tc.comm.HttpQueryHandler;
import com.cdyweb.tc.comm.HttpServer;
import com.cdyweb.tc.domain.Relay;
import com.cdyweb.tc.domain.TemperatureController;
import com.cdyweb.tc.domain.TemperatureSensor;
import com.cdyweb.tc.domain.TemperatureStatus;
import com.cdyweb.tc.jdbc.ORM;
import com.vendor.byvac.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Server implements Runnable, HttpQueryHandler {

  private static Server instance;
  private static boolean debugMode = false;
  protected transient static final Log logger = LogFactory.getLog(Server.class);
  private static Thread myThread;
  private static boolean alive = true;
  private static long interval = 30000; //30sec
  private static TrayIcon trayIcon;
  private static Image imageIdle, imageBusy, imageError;
  private ORM orm = null;
  private HttpServer httpServer=null;
  private TemperatureController controller = new TemperatureController();

  public static void main(String[] args) {

    if (SystemTray.isSupported()) {

      PopupMenu popup = new PopupMenu();

      MenuItem i1 = new MenuItem("Now!");
      i1.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          myThread.interrupt();
        }
      });
      popup.add(i1);
      MenuItem i2 = new MenuItem("Exit");
      i2.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          alive = false;
          myThread.interrupt();
        }
      });
      popup.add(i2);

      SystemTray tray = SystemTray.getSystemTray();
      imageIdle = Toolkit.getDefaultToolkit().getImage("./icon16.png");
      imageBusy = Toolkit.getDefaultToolkit().getImage("./busy.png");
      imageError = Toolkit.getDefaultToolkit().getImage("./error.png");
      trayIcon = new TrayIcon(imageIdle, "Temperature Control", popup);
      try {
        tray.add(trayIcon);
      } catch (AWTException e) {
        System.err.println("TrayIcon could not be added.");
      }
    }

    instance = new Server();
    (myThread = new Thread(instance)).start();
  }

  public static ApplicationContext getCtx() {
    try {
      return new ClassPathXmlApplicationContext("classpath*:/applicationContext-jdbc.xml");
    } catch (BeanDefinitionStoreException ex) {
      logger.fatal("cannot init beanfactory", ex);
      System.exit(-1);
    }
    return null;
  }

  public Server() {
    try {
      httpServer = (HttpServer) getCtx().getBean("httpServer"); 
      orm = (ORM) getCtx().getBean("orm");
      controller.setSensor( (TemperatureSensor) getCtx().getBean("sensor1"));
      controller.setCoolingRelay((Relay) getCtx().getBean("relay1"));
      controller.setHeatingRelay((Relay) getCtx().getBean("relay2"));
      controller.init();
      httpServer.start(this);
    } catch (Exception ex) {
      System.out.print(ex);
      System.exit(-1);
    }
  }
  
  public String query_get(String uri, java.util.List<? extends NameValuePair> parameters) {
    Formatter f=new Formatter();
    TemperatureStatus status = controller.getLastStatus();
    if (status==null) return "{err:true}";
    String output="";
    if (status.heating) output="\"heating\":\"1\"";
    if (status.cooling) output="\"cooling\":\"1\"";
    String pv_hist="";
    Long min=null;
    Long max=null;
    for (Object[] row : orm.hist("pv")) {
      if (pv_hist.length()>0) pv_hist+=",";
      pv_hist+="["+row[0]+","+row[1]+"]";
      if ((min==null) || ((Long)row[0]<min)) min=(Long)row[0];
      if ((max==null) || ((Long)row[0]>max)) max=(Long)row[0];
    }
    String sp_hist="";
    for (Object[] row : orm.hist("sp")) { //,new Date(min))) {
      if (sp_hist.length()>0) sp_hist+=",";
      sp_hist+="["+row[0]+","+row[1]+"]";
    }
    String response="{\"pv\":\""+f.format("%.1f", status.pv).toString()+"\",";
    response += "\"sp\":\""+status.sp+"\",";
    response += "\"output\":{"+output+"},";
    response += "\"age\":"+status.getAge()+",";
    response += "\"hist_min\":"+min+",";
    response += "\"hist_max\":"+max+",";
    response += "\"hist\":[["+pv_hist+"],["+sp_hist+"]]}";
    return response;
  }
  public String query_post(String uri, java.util.List<? extends NameValuePair> parameters, java.util.List<? extends NameValuePair> data) {
    Formatter f=new Formatter();
    for (NameValuePair entry:data) {
      if ("setpoint".equals(entry.getName())) {
        Double value=null;
        try {
          value=Double.parseDouble(entry.getValue());
        } catch (Exception ex) {
          logger.error("Cannot parese double: "+entry.getValue());
        }
        if (value!=null) {
          controller.setSetPoint(value);
          orm.setSetPoint(value);
        }
      }
    }
    String response="{\"setpoint\":\""+f.format("%.1f", controller.getSetPoint()).toString()+"\"}";
    return response;
  }

  @Override
  public void run() {
    SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM HH:mm");
    do {
      try {
        trayIcon.setToolTip("Busy...");
        trayIcon.setImage(imageBusy);
        
        TemperatureStatus t=controller.invoke();
        t.save(orm);

        trayIcon.setToolTip("Last sample: " + sdf.format(new Date()));
        trayIcon.setImage(imageIdle);
      } catch (Exception ex) {
        if (ex instanceof java.sql.SQLException) {
          logger.error(ex.getMessage(), ex);
          System.exit(-1);
        }
        if (!(ex instanceof InterruptedException)) {
          logger.error(ex.getMessage(), ex);
          //System.exit(-1);
          trayIcon.setImage(imageError);
          trayIcon.setToolTip(ex.getMessage());
        }
      }
      try {
        Thread.sleep(interval);
      } catch (Exception ex) {
        if (!(ex instanceof InterruptedException)) {
          logger.error(ex.getMessage(), ex);
          alive = false;
        }
      }
    } while (alive);
    System.exit(0);
  }
}