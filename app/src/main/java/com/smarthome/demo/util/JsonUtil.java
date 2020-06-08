package com.smarthome.demo.util;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtil {
  private String jsonString;
  private String name;

  public JsonUtil(String jsonString, String name) {
    this.jsonString = jsonString;
    this.name = name;
  }
  // 获取解析的值
  // 获取的json数据为：{"deviceType":"CustomCategory","iotId":"RzBeyfRCYPoOToJP4vFr000100","requestId":"123","productKey":"a1PqXtaIQTd","gmtCreate":1589036800668,"deviceName":"Esp8266","items":{"CurrentHumidity":{"time":1589036800676,"value":95}}}
  public String JsonSolve() throws JSONException {
    JSONObject json = new JSONObject(jsonString);
    JSONObject json2 = new JSONObject(json.getString("items"));
    JSONObject json3 = new JSONObject(json2.getString(name));
    return json3.getString("value");
  }
}
