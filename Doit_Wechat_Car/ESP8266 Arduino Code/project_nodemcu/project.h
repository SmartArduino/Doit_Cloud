#ifndef _MAIN_H__
#define _MAIN_H__


extern "C" {
#include "user_interface.h"
#include "smartconfig.h"
}
struct config_type
{
  char stassid[32];
  char stapsw[64];
  char id[64];
  char key[64];
  uint8_t magic;
};

void startTCPClient();
void doTCPClientTick();
void startUDPServer(int);
void doUdpServerTick();
void sendTCP(char *);
void sendUDP(char *);
void initParseData();
void parseTCPPackage(char*);
void parseUDPPackage(char*);
void parseUartPackage(char*);

void initHttpServer();
void doHttpServerTick();
void delayRestart(float);

//常量
#define VER             "Doit_ESPduino_V1.0"
#define DEFAULT_APSSID  "Doit_ESP_Car"
#define DEFAULT_STASSID ""
#define DEFAULT_STAPSW  ""
#define PINLED          16
#define PINKEY          0
#define HOST_NAME       "DoitWiFi_Device"

//产品相关
#define DEFAULT_ID    "DOIT-SN-851A-73814"
#define DEFAULT_KEY    "e14b22b5b29bceba4e8389f2a1182763"

const char apssid[]=DEFAULT_APSSID;
const char serverAddr[] = "s.doit.am";
const uint16_t serverPort = 8810;
const uint16_t udpPort = 8089;

unsigned long lastWiFiCheckTick = 0;
bool ledState = 0;

config_type config;

#endif
