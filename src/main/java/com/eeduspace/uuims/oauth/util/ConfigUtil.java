package com.eeduspace.uuims.oauth.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigUtil {
  public static String getProperty(String propertyName) {
    Properties p = new Properties();
    InputStream in = ConfigUtil.class.getResourceAsStream("/conf/config.properties");
    try {
      p.load(in);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return p.getProperty(propertyName);
  }

}
