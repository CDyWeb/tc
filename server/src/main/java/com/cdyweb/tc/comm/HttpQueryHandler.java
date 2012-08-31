package com.cdyweb.tc.comm;

import java.util.List;
import org.apache.http.NameValuePair;

public interface HttpQueryHandler {
  
  public String query_get(String uri, List<? extends NameValuePair> parameters);
  public String query_post(String uri, List<? extends NameValuePair> parameters, List<? extends NameValuePair> data);

}
