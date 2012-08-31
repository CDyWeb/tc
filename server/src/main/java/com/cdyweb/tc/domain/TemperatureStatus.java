package com.cdyweb.tc.domain;

import com.cdyweb.tc.jdbc.ORM;
import java.util.Date;

public class TemperatureStatus {
  
  public double pv;
  public double sp;
  public Date sampleDate;
  public Boolean heating;
  public Boolean cooling;

  public TemperatureStatus() {
    this.sampleDate=new Date();
  }
  
  public TemperatureStatus(double pv,double sp,Date sampleDate,Boolean heating,Boolean cooling) {
    this.pv=pv;
    this.sp=sp;
    this.sampleDate=sampleDate;
    this.heating=heating;
    this.cooling=cooling;
  }

  public int getAge() {
    if (sampleDate==null) return 0;
    long diff = System.currentTimeMillis() - sampleDate.getTime();
    return (int) (diff / 1000);
  }
  
  public void save(ORM db) {
    db.insert(this);
  }
  
}
