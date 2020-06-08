//mqtt连接
#ifndef MQTT
#define MQTT
#include <Arduino.h>
#include <ArduinoJson.h>
#include "Client.h"
typedef void (*poniter_fun)(JsonVariant ele);
typedef struct poniter_desc
{
  char *key;
  poniter_fun fp;
} poniter_desc, *p_poniter_desc;
static poniter_desc poniter_array[20];
static p_poniter_desc p_poniter_array;
class Mqtt
{
  private:
    static char mqttPwd[256];
    static char clientId[256];
    static char mqttUsername[100];
    static char domain[150];
    static void mqttCheckConnect();
  public:
    static char TopicPost[150];
    static char TopicSet[150];
    static void loop();
    static void begin(Client &espClient,const char *_productKey,const char *_deviceName,const char *_deviceSecret,const char *_region);
    //发送json 数据
    static void send(const char *param);
    static int bindData(char *key, poniter_fun fp);

};
#endif
