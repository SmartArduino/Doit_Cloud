
#include "project.h"

Ticker timer;

#define PinPWMA 3
#define PinPWMB 12
#define PinDirA 1
#define PinDirB 13
//nodemcu dev kit
/*#define PinPWMA 5
  #define PinPWMB 4
  #define PinDirA 0
  #define PinDirB 2*/

int spdTargetA = 1023;
int spdTargetB = 1023;
bool stopFlag = true;
bool JogFlag = false;
uint16_t JogTimeCnt = 0;

/*
  电机控制
*/
void tick_car_control()
{
  if (JogFlag == true) {
    stopFlag = false;
    if (JogTimeCnt <= 0) {
      JogFlag = false; stopFlag = true;
    }
    JogTimeCnt--;
  }
  if (stopFlag == false) {
    analogWrite(PinPWMA, spdTargetA);
    analogWrite(PinPWMB, spdTargetA);
  }
  else {
    JogTimeCnt = 0;
    analogWrite(PinPWMA, 0);
    analogWrite(PinPWMB, 0);
  }
}

/*
  初始化
*/
void initParseData()
{
  Serial.println("[initParseData]");
  pinMode(PinPWMA, OUTPUT); pinMode(PinPWMB, OUTPUT);
  pinMode(PinDirA, OUTPUT); pinMode(PinDirB, OUTPUT);
  digitalWrite(PinDirA, HIGH); digitalWrite(PinDirB, HIGH);
  digitalWrite(PinPWMA, LOW); digitalWrite(PinPWMB, LOW);
  timer.attach(0.1, tick_car_control);
}
/*
  UDP接收包处理
*/
void parseUDPPackage(char *p)
{
  Serial.print("[UDP parseData:]");
  Serial.println(p);
  //ping命令
  if (strstr(p, "cmd=ping") != NULL) {
    IPAddress ip = WiFi.localIP();
    char t[128];
    sprintf(t, "cmd=pong&sta_ip=%d.%d.%d.%d&host_ip=192.168.4.1", ip[0], ip[1], ip[2], ip[3]);
    if (ip[0] == 0 && ip[1] == 0 && ip[2] == 0 && ip[3] == 0)
      strcpy(t, "cmd=pong&sta_ip=&host_ip=192.168.4.1");
    sendUDP(t);
  }
  //设置ssid和密码 cmd=config&ssid=Doit&ps=doit3305
  else if (strstr(p, "cmd=config") != NULL) {
    char *s = strstr(p, "ssid="); if (s == NULL) return;
    s = strstr(s, "="); if (s == NULL) return;
    char *e = strstr(s, "&"); if (e == NULL) return;
    char ssid[64]; memset(ssid, 0x00, 64);
    memcpy(ssid, s + 1, e - s - 1);
    s = strstr(e, "="); if (s == NULL) return;
    e = strstr(s, "&"); //if(e==NULL) return;
    char psw[64]; memset(psw, 0x00, 64);
    if (e != NULL) memcpy(psw, s + 1, e - s - 1);
    else strcpy(psw, s + 1);
    Serial.print("SSID: "); Serial.println(ssid);
    Serial.print("PSW: ");  Serial.println(psw);
    Serial.println("Module will restart after 2s...");
    strcpy(config.stassid, ssid);
    strcpy(config.stapsw, psw);
    sendUDP((char*)"CMD OK");
    saveConfig();
    delay(2000);
    ESP.restart();
  }
  //控制
  else if (strstr(p, "cmd=control&d=") != NULL) {
    char *s = p + strlen("cmd=control&d="); if (*s == 0x00) return;
    switch (*s) {
      case '0': analogWrite(PinPWMA, 0); analogWrite(PinPWMB, 0); stopFlag = true; JogTimeCnt = 0; break;
      case '1': digitalWrite(PinDirA, HIGH); digitalWrite(PinDirB, HIGH); JogTimeCnt = 50; JogFlag = true; break;
      case '2': digitalWrite(PinDirA, LOW); digitalWrite(PinDirB, LOW); JogTimeCnt = 50; JogFlag = true; break;
      case '3': digitalWrite(PinDirA, LOW); digitalWrite(PinDirB, HIGH); JogTimeCnt = 50; JogFlag = true; break;
      case '4': digitalWrite(PinDirA, HIGH); digitalWrite(PinDirB, LOW); JogTimeCnt = 50; JogFlag = true; break;
      case '5': char t[32]; sprintf(t, "cmd=control&spd=%d", spdTargetA); sendUDP(t); break;
      case '6': spdTargetA += 100; if (spdTargetA > 1023) spdTargetA = 1023; spdTargetB = spdTargetA; break;
      case '7': spdTargetA -= 100; if (spdTargetA < 0) spdTargetA = 0; spdTargetB = spdTargetA; break;
      default: break;
    }
    if (*s != 5) sendUDP((char*)"CMD OK");
  }
}
/*
  TCP接收包处理
*/
void parseTCPPackage(char *p)
{
  Serial.print("[TCP parseData:]");
  Serial.println(p);
  if (strstr(p, "cmd=subscribe&res=1") != NULL) {
    char t[128];
    sprintf(t, "cmd=m2m_chat&device_id=&s&device_key=%s&topic=%s_chat", config.id, config.key, config.id);
    sendTCP(t);
  }
  //cmd=publish&topic=DOIT-SN-851A-73814&message=3
  else if (strstr(p, "&message=") != NULL) {
    char *s = strstr(p, "&message=");
    s = s + strlen("&message="); if (*s == 0x00) return;
    switch (*s) {
      case '0': analogWrite(PinPWMA, 0); analogWrite(PinPWMB, 0); stopFlag = true; JogTimeCnt = 0; break;
      case '1': digitalWrite(PinDirA, HIGH); digitalWrite(PinDirB, HIGH); JogTimeCnt = 50; JogFlag = true; break;
      case '2': digitalWrite(PinDirA, LOW); digitalWrite(PinDirB, LOW); JogTimeCnt = 50; JogFlag = true; break;
      case '3': digitalWrite(PinDirA, LOW); digitalWrite(PinDirB, HIGH); JogTimeCnt = 2; JogFlag = true; break;
      case '4': digitalWrite(PinDirA, HIGH); digitalWrite(PinDirB, LOW); JogTimeCnt = 2; JogFlag = true; break;
      case '5': char t[32]; sprintf(t, "cmd=control&spd=%d", spdTargetA); sendTCP(t); break;
      case '6': spdTargetA += 100; if (spdTargetA > 1023) spdTargetA = 1023; spdTargetB = spdTargetA; break;
      case '7': spdTargetA -= 100; if (spdTargetA < 0) spdTargetA = 0; spdTargetB = spdTargetA; break;
      default: break;
    }
  }
}
/*
  串口接收包处理
*/
void parseUartPackage(char *p)
{
  Serial.println("[Uart parseData]");
  Serial.println(p);
  Serial.println("Invalid cmd");
}

