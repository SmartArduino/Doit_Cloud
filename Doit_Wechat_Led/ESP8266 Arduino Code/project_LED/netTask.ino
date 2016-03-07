#include "project.h"

#define MAX_PACKETSIZE 512
WiFiClient client;
WiFiUDP udp;
extern config_type config;
extern const char serverAddr[];
extern const uint16_t serverPort;
char buffTCP[MAX_PACKETSIZE];
unsigned int buffTCPIndex=0;
unsigned long preTCPTick=0;
unsigned long preHeartTick=0;
unsigned long preTCPStartTick=0;
bool preTCPConnected=false;
char buffUDP[MAX_PACKETSIZE];
unsigned long preUDPTick=0;

void startTCPClient()
{
   Serial.println("\r\nstartTCPClient");
  if(client.connect(serverAddr, serverPort)){
      Serial.print("Connected to server:");
      Serial.println(serverAddr);
	    char buff[128];
      sprintf(buff,"cmd=subscribe&device_id=%s&device_key=%s",config.id,config.key);
      Serial.print("[Send]");
      Serial.println(buff);
      client.println(buff);
      preTCPConnected = true;
      preHeartTick = millis();
      client.setNoDelay(true);
    }
  else{
      Serial.print("Failed connected to server:");
      Serial.println(serverAddr);
      client.stop();
      preTCPConnected = false;
  }
  preTCPStartTick = millis();
}
void sendTCP(char *p)
{
  if (!client.connected()) 
  {
    Serial.println("Client is not readly");
    return;
  }
  client.println(p);
  Serial.print("[TCP Client Send]:");
  Serial.println(p);
}
void doTCPClientTick()
{//检查数据，发送心跳
 //检查是否断开，断开后重连
   if(WiFi.status() != WL_CONNECTED) return;
   
   if (!client.connected()) {//断开重连
    if(preTCPConnected==true){
      preTCPConnected = false;
      preTCPStartTick = millis();
      Serial.println();
      Serial.println("TCP Client disconnected.");
      client.stop();
    }
    else if(millis() - preTCPStartTick > 10*1000)
      startTCPClient();
   }
   else
   {
      if (client.available()) {//收数据
      buffTCP[buffTCPIndex++] = client.read();
      preTCPTick = millis();
  	  if(buffTCPIndex>=MAX_PACKETSIZE - 1){
  		  buffTCPIndex = MAX_PACKETSIZE-2;
  		  preTCPTick = preTCPTick - 200;
  	    }
       preHeartTick = millis();
      }
      if(millis() - preHeartTick>=60*1000){//保持心跳
        char buff[128];
        preHeartTick = millis();
        sprintf(buff,"cmd=keep&device_id=%s&device_key=%s",config.id,config.key);
        Serial.print("Keep alive:");
        Serial.println(buff);
        client.println(buff);
      }
   }
   if(buffTCPIndex>0 && (millis() - preTCPTick>=100))
   {//data ready
    buffTCP[buffTCPIndex]=0x00;
    client.flush();
		parseTCPPackage(buffTCP);
    buffTCPIndex = 0;
   }   
}
void startUDPServer(int port)
{
  Serial.print("\r\nStartUDPServer at port:");
  Serial.println(port);
  udp.begin(port);
}
void doUdpServerTick()
{
  if(udp.parsePacket())
  {
    memset(buffUDP,0x00,sizeof(buffUDP));
    udp.read(buffUDP, MAX_PACKETSIZE-1);
    udp.flush();
    parseUDPPackage(buffUDP);
  }
}
void sendUDP(char *p)
{
  udp.beginPacket(udp.remoteIP(), udp.remotePort());
  udp.write(p);udp.endPacket();
}

