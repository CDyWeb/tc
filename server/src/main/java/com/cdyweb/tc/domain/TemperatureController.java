/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cdyweb.tc.domain;

import java.io.IOException;

/**
 *
 * @author Erwin
 */
public class TemperatureController {
 
  private double setPoint=20;
  private double deltaCool=.5;
  private double deltaHeat=.5;

  private TemperatureSensor sensor=null;
  private Relay coolingRelay=null;
  private Relay heatingRelay=null;
  
  private TemperatureStatus lastStatus=null;

  public double getSetPoint() {
    return setPoint;
  }

  public void setSetPoint(double setPoint) {
    this.setPoint = setPoint;
  }

  public double getDeltaCool() {
    return deltaCool;
  }

  public void setDeltaCool(double deltaCool) {
    this.deltaCool = deltaCool;
  }

  public double getDeltaHeat() {
    return deltaHeat;
  }

  public void setDeltaHeat(double deltaHeat) {
    this.deltaHeat = deltaHeat;
  }

  public TemperatureSensor getSensor() {
    return sensor;
  }

  public void setSensor(TemperatureSensor sensor) {
    this.sensor = sensor;
  }

  public Relay getCoolingRelay() {
    return coolingRelay;
  }

  public void setCoolingRelay(Relay coolingRelay) {
    this.coolingRelay = coolingRelay;
  }

  public Relay getHeatingRelay() {
    return heatingRelay;
  }

  public void setHeatingRelay(Relay heatingRelay) {
    this.heatingRelay = heatingRelay;
  }

  public TemperatureStatus getLastStatus() {
    return lastStatus;
  }
  
  public void init() throws IOException {
    sensor.init();
    coolingRelay.init(Boolean.FALSE);
    heatingRelay.init(Boolean.FALSE);
  }

  public TemperatureStatus invoke() throws IOException {
    TemperatureStatus newStatus=new TemperatureStatus();
    newStatus.sp = this.setPoint;
    newStatus.pv = sensor.read();
    coolingRelay.setStatus(newStatus.cooling = (newStatus.pv > setPoint+deltaCool));
    heatingRelay.setStatus(newStatus.heating = (newStatus.pv < setPoint-deltaHeat));
    lastStatus=newStatus;
    return newStatus;
  }
  
}
