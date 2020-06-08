#include <WiFiUdp.h>
unsigned int localPort = 2390; //本地端口监听UDP报文
IPAddress timeServerIP; // 服务器地址
const char* ntpServerName = "ntp1.aliyun.com";
const int NTP_PACKET_SIZE = 48; // NTP时间戳记在消息的前48个字节中
byte packetBuffer[ NTP_PACKET_SIZE]; //缓冲区
WiFiUDP udp;
//发送NTP请求
void sendNTPpacket(IPAddress& address) {
  memset(packetBuffer, 0, NTP_PACKET_SIZE);
  // 初始化NTP报文对应的格式
  packetBuffer[0] = 0b11100011;   //LI, Version, Mode
  packetBuffer[1] = 0;     // Stratum系统时钟的层数
  packetBuffer[2] = 6;     // Poll轮询时间间隔
  packetBuffer[3] = 0xEC;  // Precision本地时钟精度
  packetBuffer[12]  = 49;
  packetBuffer[13]  = 0x4E;
  packetBuffer[14]  = 49;
  packetBuffer[15]  = 52;
  udp.beginPacket(address, 123); //NTP请求到端口123
  udp.write(packetBuffer, NTP_PACKET_SIZE);
  udp.endPacket();
}
int NtpTime() {
  udp.begin(localPort);
  //从池中获取随机服务器
  WiFi.hostByName(ntpServerName, timeServerIP);
  sendNTPpacket(timeServerIP);
  delay(1000);
  int a = udp.parsePacket();
  if (!a) {
    Serial.println("时间获取失败");
    return -1;
  } else  {
    //读取时间信息
    udp.read(packetBuffer, NTP_PACKET_SIZE);
    //时间戳从接收到的数据包的字节40开始，为四个字节或者两个字节
    unsigned long highWord = word(packetBuffer[40], packetBuffer[41]);
    unsigned long lowWord = word(packetBuffer[42], packetBuffer[43]);
    unsigned long secsSince1900 = highWord << 16 | lowWord;
    const unsigned long seventyYears = 2208988800UL;
    unsigned long epoch = secsSince1900 - seventyYears;//获取的当前时间
    return epoch;
  }
}
