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
public interface Relay {
  
  public void init(Boolean firstStatus) throws IOException;
  public Boolean getStatus() throws IOException;
  public void setStatus(Boolean newStatus) throws IOException;

}
