#include <ESP8266WiFi.h>
static WiFiClient espClient;
#include "Mqtt.h"
#include "DHTesp.h"
#include "Time.h"
#include "pitches.h"
#include <MQUnifiedsensor.h>
#include <PubSubClient.h>
#include <SHA256.h>
#define PRODUCT_KEY       "a1PqXtaIQTd"
#define DEVICE_NAME       "Esp8266"
#define DEVICE_SECRET     "lJdqGnlNXxnngeTmUrZYhsQ0FK67wJxW"
#define REGION_ID         "cn-shanghai"
#define WIFI_SSID         "NU11"
#define WIFI_PASSWD       "qwertyuiop"
Mqtt iot;
unsigned long lastMsMain = 0;//发送延迟
int ledPin = 16; //led pin d0脚
int dht11Pin = 0; //dht11 pin d3脚
int lPin = 2, fanPin = 15 ; //载板灯 d4，风扇 d8
int soundPin = 13; //蜂鸣器 pin d7脚
#define somgPin A0//mq-2 
MQUnifiedsensor MQ2(somgPin, 2);
char currentHumidity[50];//湿度
char currentTemperature[50];//温度
char smog[50]; //烟雾值
char lightLuminance[50] = "0"; //灯亮度
char windSpeed[50] = "0"; //风速
char sound[50] = "0"; //蜂鸣器状态
char fan[50] = "0"; //风扇状态
char led[50] = "0"; //灯状态
char dateTime[50] = "0"; //定时器状态
char runtime[50] = "0";
int runTime = 0; //定时的时间
int currenttime = 0; //当前时间
char threshold[50] = "10000"; //烟雾阈值
int thresholds = 10000; //烟雾阈值
char H2[50]; //氢气浓度
char LPG[50];//液化石油气浓度
char CO[50];//一氧化碳浓度
char Alcohol[50]; //酒精浓度
char Propane[50];//丙烷浓度
char curtain[50] = "0"; //窗帘状态
DHTesp dht;//dht11
//蜂鸣器
int melody[] = {
  NOTE_C4, NOTE_G3, NOTE_G3, NOTE_GS3, NOTE_G3, 0, NOTE_B3, NOTE_C4
};
int noteDurations[] = {
  4, 8, 8, 4, 4, 4, 4, 4
};

#define CHECK_INTERVAL 10000
#define MESSAGE_BUFFER_SIZE 10
static const char *deviceName = NULL;
static const char *productKey = NULL;
static const char *deviceSecret = NULL;
static const char *region = NULL;
struct DeviceProperty
{
  String key;
  String value;
};
DeviceProperty PropertyMessageBuffer[MESSAGE_BUFFER_SIZE];
#define MQTT_PORT 1883
#define SHA256HMAC_SIZE 32
#define DATA_CALLBACK_SIZE 20
#define ALINK_BODY_FORMAT "{\"id\":\"123\",\"version\":\"1.0\",\"method\":\"thing.event.property.post\",\"params\":%s}"//发布主题
static unsigned long lastMs = 0;
static PubSubClient *client = NULL;
char Mqtt::clientId[256] = "";
char Mqtt::mqttUsername[100] = "";
char Mqtt::mqttPwd[256] = "";
char Mqtt::domain[150] = "";
char Mqtt::TopicPost[150] = "";
char Mqtt::TopicSet[150] = "";
static String hmac256(const String &signcontent, const String &ds)
{
  byte hashCode[SHA256HMAC_SIZE];
  SHA256 sha256;
  const char *key = ds.c_str();
  size_t keySize = ds.length();
  sha256.resetHMAC(key, keySize);
  sha256.update((const byte *)signcontent.c_str(), signcontent.length());
  sha256.finalizeHMAC(key, keySize, hashCode, sizeof(hashCode));
  String sign = "";
  for (byte i = 0; i < SHA256HMAC_SIZE; ++i)
  {
    sign += "0123456789ABCDEF"[hashCode[i] >> 4];
    sign += "0123456789ABCDEF"[hashCode[i] & 0xf];
  }
  return sign;
}

static void parmPass(JsonVariant parm)
{
  for (int i = 0; i < DATA_CALLBACK_SIZE; i++)
  {
    if (poniter_array[i].key)
    {
      bool hasKey = parm["params"].containsKey(poniter_array[i].key);
      if (hasKey)
      {
        poniter_array[i].fp(parm["params"]);
      }
    }
  }
}
//回调
static void callback(char *topic, byte *payload, unsigned int length)
{
  Serial.print("消息回调： [");
  Serial.print(topic);
  Serial.print("] ");
  payload[length] = '\0';
  char a[200];
  sprintf(a, "%s%d%s%s%s", " {\"method\":\"thing.service.property.set\",\"id\":\"" , random(5000), "\",\"params\":", (char *)payload, ",\"version\":\"1.0.0\"}"); //格式化json
  Serial.println(a);
  if (strstr(topic, Mqtt::TopicSet))
  {

    StaticJsonDocument<200> doc;
    DeserializationError error = deserializeJson(doc, a);

    if (!error)
    {
      parmPass(doc.as<JsonVariant>());
    }
  }
}
static bool mqttConnecting = false;
void Mqtt::mqttCheckConnect()
{
  if (client != NULL && !mqttConnecting)
  {
    if (!client->connected())
    {
      client->disconnect();
      Serial.println("连接MQTT服务器");
      mqttConnecting = true;
      if (client->connect(clientId, mqttUsername, mqttPwd))
      {
        boolean k = client->subscribe(TopicSet); //订阅
        if (k)
          Serial.print("订阅成功");
        else
          Serial.println("订阅失败");
        Serial.println("MQTT连接成功");
      }
      else
      {
        Serial.print("MQTT连接错误：");
        Serial.println(client->state());

      }
      mqttConnecting = false;
    }
    else
    {
      Serial.println("已连接");
    }
  }
}

void Mqtt::begin(Client &espClient, const char *_productKey, const char *_deviceName, const char *_deviceSecret, const char *_region)
{

  client = new PubSubClient(espClient);
  productKey = _productKey;
  deviceName = _deviceName;
  deviceSecret = _deviceSecret;
  region = _region;
  long times = millis();
  String timestamp = String(times);

  sprintf(clientId, "%s|securemode=3,signmethod=hmacsha256,timestamp=%s|", deviceName, timestamp.c_str());

  String signcontent = "clientId";
  signcontent += deviceName;
  signcontent += "deviceName";
  signcontent += deviceName;
  signcontent += "productKey";
  signcontent += productKey;
  signcontent += "timestamp";
  signcontent += timestamp;
  String pwd = hmac256(signcontent, deviceSecret);
  strcpy(mqttPwd, pwd.c_str());
  sprintf(mqttUsername, "%s&%s", deviceName, productKey);
  sprintf(TopicPost, "/sys/%s/%s/thing/event/property/post", productKey, deviceName);
  sprintf(TopicSet, "/sys/%s/%s/thing/service/property/set", productKey, deviceName);
  sprintf(domain, "%s.iot-as-mqtt.%s.aliyuncs.com", productKey, region);
  client->setServer(domain, MQTT_PORT); //连接mqtt服务器
  client->setCallback(callback);//设置回调
  mqttCheckConnect();//检查连接
}
void Mqtt::loop()
{
  client->loop();
  if (millis() - lastMs >= CHECK_INTERVAL)
  {
    lastMs = millis();
    mqttCheckConnect();
  }
}
void Mqtt::send(const char *param)
{

  char jsonBuf[1024];
  sprintf(jsonBuf, ALINK_BODY_FORMAT, param);
  Serial.println(jsonBuf);
  boolean d = client->publish(TopicPost, jsonBuf);
  if (d)
    Serial.println("发布成功");
  else
    Serial.println("发布失败");
}
//绑定
int Mqtt::bindData(char *key, poniter_fun fp)
{
  int i;
  for (i = 0; i < DATA_CALLBACK_SIZE; i++)
  {
    if (!poniter_array[i].fp)
    {
      poniter_array[i].key = key;
      poniter_array[i].fp = fp;
      return 0;
    }
  }
  return -1;
}
/****************************************************启动配置**********************************************************/
void setup()
{
  Serial.begin(115200);
  pinMode(ledPin, OUTPUT);
  pinMode(lPin, OUTPUT);
  pinMode(fanPin, OUTPUT);
  pinMode(soundPin, OUTPUT);
  digitalWrite(lPin, LOW);//载板灯常亮，设备在线
  digitalWrite(fanPin, LOW);
  digitalWrite(soundPin, LOW);
  digitalWrite(ledPin, LOW);
  dht.setup(dht11Pin, DHTesp::DHT11);
  MQ2.inicializar();
  //窗帘
  pinMode(5, OUTPUT);
  pinMode(4, OUTPUT);
  pinMode(14, OUTPUT);
  pinMode(12, OUTPUT);
  clockwise(300);//初始为关窗状态
  //连接到wifi
  wifiInit(WIFI_SSID, WIFI_PASSWD);
  Mqtt::begin(espClient, PRODUCT_KEY, DEVICE_NAME, DEVICE_SECRET, REGION_ID);
  Mqtt::bindData("LED", LED);
  Mqtt::bindData("FAN", FAN);
  Mqtt::bindData("SOUND", SOUND);
  Mqtt::bindData("LightLuminance", LightLuminance);
  Mqtt::bindData("WindSpeed", WindSpeed);
  Mqtt::bindData("Threshold", Threshold);
  Mqtt::bindData("Curtain", Curtain);
  Mqtt::bindData("TIME", TIME);
  Mqtt::bindData("Runtime", Runtime);
}
void loop()
{

  Mqtt::loop();//mqtt建连
  delay(dht.getMinimumSamplingPeriod());
  //北京时间
  if(NtpTime()!=-1){
  if (((NtpTime()  % 86400L) / 3600) <= 15)
    currenttime = (((NtpTime()  % 86400L) / 3600) + 8) * 100 + (NtpTime()  % 3600) / 60;
  else if (((NtpTime()  % 86400L) / 3600) == 16)
    currenttime = (NtpTime()  % 3600) / 60;
  else
    currenttime = ((NtpTime()  % 86400L) / 3600 + 8 - 24) * 100 + (NtpTime()  % 3600) / 60;
  Serial.print("通过NTP协议获取的网络时间");
  Serial.println(currenttime);
  }
  sprintf(currentHumidity, "%g", dht.getHumidity());
  sprintf(currentTemperature, "%g",  dht.getTemperature());
  MQ2.update();
  sprintf(smog, "%d", map(analogRead(somgPin), 0, 1023, 100, 10000));
  sprintf(H2, "%d", (int)MQ2.readSensor("H2"));
  sprintf(LPG, "%d", (int)MQ2.readSensor("LPG"));
  sprintf(CO, "%d", (int)MQ2.readSensor("CO"));
  sprintf(Alcohol, "%d", (int)MQ2.readSensor("Alcohol"));
  sprintf(Propane, "%d", (int)MQ2.readSensor("Propane"));

  char *jsonString = (char *) malloc(strlen(currentHumidity) + strlen(led) + strlen(currentTemperature) + strlen(smog) + strlen(curtain)
                                     + strlen(lightLuminance) + strlen(windSpeed) + strlen(sound) + strlen(fan) + strlen(threshold)
                                     + strlen(H2) + strlen(LPG) + strlen(CO) + strlen(Alcohol) + strlen(Propane) + strlen(dateTime) + strlen(runtime) + 188);
  sprintf(jsonString, "%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s", "{\"LED\":", led, ",\"FAN\":", fan, ",\"SOUND\":", sound, ",\"SMOG\":", smog,
          ",\"Threshold\":", threshold, ",\"LightLuminance\":", lightLuminance, ",\"WindSpeed\":", windSpeed, ",\"CurrentTemperature\":", currentTemperature,
          ",\"CurrentHumidity\":", currentHumidity, ",\"H2\":", H2, ",\"LPG\":", LPG, ",\"CO\":", CO, ",\"Alcohol\":", Alcohol, ",\"Propane\":", Propane, ",\"Curtain\":", curtain,
          ",\"TIME\":", dateTime, ",\"Runtime\":", runtime, "}");

  if (millis() - lastMsMain >= 3000)
  {
    if (strcmp(sound, "1") == 0 && map(analogRead(somgPin), 0, 1023, 100, 10000) >= thresholds) //蜂鸣器打开且大于烟雾阈值
    {
      for (int thisNote = 0; thisNote < 8; thisNote++) {
        int noteDuration = 1000 / noteDurations[thisNote];
        tone(soundPin, melody[thisNote], noteDuration);
        delay(noteDuration + 30);
      }
    }
    if (strcmp(curtain, "0") == 0 && strcmp(dateTime, "1") == 0 && currenttime == runTime) //窗帘关闭且定时器打开，到达定时的时间开启窗帘
    {
      anticlockwise(300);
      strcpy(curtain, "1");
    }
    lastMsMain = millis();
    Mqtt::send(jsonString);//数据发送到阿里云物联网平台
  }
}
void wifiInit(const char *ssid, const char *passphrase)
{
  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, passphrase);
  WiFi.setAutoConnect (true);
  WiFi.setAutoReconnect (true);
  while (WiFi.status() != WL_CONNECTED)
  {
    delay(1000);
    Serial.println("未连接wifi");
  }
  Serial.println("连接到AP");
}

//灯开关
void LED(JsonVariant L)
{
  if (L["LED"] == 0)
  {
    digitalWrite(ledPin, LOW);
    strcpy(led, "0");
    sprintf(lightLuminance, "%d", 0);
  }
  else
  {
    digitalWrite(ledPin, HIGH);
    strcpy(led, "1");
    analogWrite(ledPin, 20);
    sprintf(lightLuminance, "%d", 20);
  }
}

//风扇开关
void FAN(JsonVariant L)
{
  if (L["FAN"] == 0)
  {

    digitalWrite(fanPin, LOW);
    strcpy(fan, "0");
    sprintf(windSpeed, "%d", 0);
  }
  else
  {
    digitalWrite(fanPin, HIGH);
    strcpy(fan, "1");
    analogWrite(fanPin, 500);
    sprintf(windSpeed, "%d", 500);
  }
}
//窗帘开关
void Curtain(JsonVariant L)
{
  if (L["Curtain"] == 0)
  {
    strcpy(curtain, "0");
    clockwise(300);
  }
  else
  {
    strcpy(curtain, "1");
    anticlockwise(300);
  }
}
//定时器开关
void TIME(JsonVariant L)
{
  if (L["TIME"] == 0)
  {
    strcpy(dateTime, "0");

  }
  else
  {
    strcpy(dateTime, "1");

  }
}
//蜂鸣器开关
void SOUND(JsonVariant L)
{
  if (L["SOUND"] == 0)
  {

    strcpy(sound, "0");
    sprintf(threshold, "%d", 10000);
    digitalWrite(soundPin, LOW);
  }
  else
  {
    strcpy(sound, "1");
    sprintf(threshold, "%d", 10000);
    digitalWrite(soundPin, HIGH);
  }
}

//灯的亮度
void LightLuminance(JsonVariant L)
{
  int k = L["LightLuminance"];
  analogWrite(ledPin, k);
  sprintf(lightLuminance, "%d", k);
}
//定时的时间
void Runtime(JsonVariant L)
{
  int k = L["Runtime"];
  runTime = k;
  sprintf(runtime, "%d", k);
}
//风扇风速
void WindSpeed(JsonVariant L)
{
  int k = L["WindSpeed"];
  analogWrite(fanPin, k);
  sprintf(windSpeed, "%d", k);
}

//烟雾阈值
void Threshold(JsonVariant L)
{
  thresholds = L["Threshold"];
  sprintf(threshold, "%d", thresholds);
}
//窗帘打开
void clockwise(int num)
{
  for (int count = 0; count < num; count++)
  {

    digitalWrite(5, HIGH);
    delay(3);
    digitalWrite(5, LOW);
    digitalWrite(4, HIGH);
    delay(3);
    digitalWrite(4, LOW);
    digitalWrite(14, HIGH);
    delay(3);
    digitalWrite(14, LOW);
    digitalWrite(12, HIGH);
    delay(3);
    digitalWrite(12, LOW);
  }
}
//窗帘关闭
void anticlockwise(int num)
{
  for (int count = 0; count < num; count++)
  {
    digitalWrite(12, HIGH);
    delay(3);
    digitalWrite(12, LOW);
    digitalWrite(14, HIGH);
    delay(3);
    digitalWrite(14, LOW);
    digitalWrite(4, HIGH);
    delay(3);
    digitalWrite(4, LOW);
    digitalWrite(5, HIGH);
    delay(3);
    digitalWrite(5, LOW);
  }
}
