
#include "project.h"

#define LEDR 5
#define LEDB 4
#define LEDG 2

/*
*初始化
*/
void initParseData()
{
  Serial.println("[initParseData]");
  pinMode(LEDR, OUTPUT);
  pinMode(LEDB, OUTPUT);
  pinMode(LEDG, OUTPUT);
  digitalWrite(LEDR,HIGH);
  digitalWrite(LEDB,HIGH);
  digitalWrite(LEDG,HIGH);
  //analogWriteRange(1000);//default range 1023
  //analogWriteFreq(100);//default frequency is 1khz
}

/*
*UDP接收包处理
*/
void parseUDPPackage(char *p)
{
  Serial.print("[UDP parseData:]");
  Serial.println(p);
  //ping命令
  if(strstr(p,"cmd=ping") !=NULL){
    IPAddress ip = WiFi.localIP();
    char t[128];
    sprintf(t,"cmd=pong&sta_ip=%d.%d.%d.%d&host_ip=192.168.4.1",ip[0],ip[1],ip[2],ip[3]);
    if(ip[0]==0&&ip[1]==0&&ip[2]==0&&ip[3]==0)
    strcpy(t,"cmd=pong&sta_ip=&host_ip=192.168.4.1");
    sendUDP(t);
  }
  //设置ssid和密码 cmd=config&ssid=Doit&ps=doit3305
  else if(strstr(p,"cmd=config") !=NULL){
    char *s = strstr(p,"ssid=");if(s==NULL) return;
    s = strstr(s,"=");if(s==NULL) return;
    char *e = strstr(s,"&");if(e==NULL) return;
    char ssid[64];memset(ssid,0x00,64);
    memcpy(ssid,s+1,e-s-1);
    s=strstr(e,"=");if(s==NULL) return;
    e=strstr(s,"&");//if(e==NULL) return;
    char psw[64];memset(psw,0x00,64);
    if(e!=NULL) memcpy(psw,s+1,e-s-1);
    else strcpy(psw,s+1);
    Serial.print("SSID: "); Serial.println(ssid);
    Serial.print("PSW: ");  Serial.println(psw);
    Serial.println("Module will restart after 2s...");
    strcpy(config.stassid,ssid);
    strcpy(config.stapsw,psw);
    sendUDP((char*)"CMD OK");
    saveConfig();
    delay(2000);
    ESP.restart();
  }
  //控制 cmd=light&v=126|252|0
  else if(strstr(p,"cmd=light&v=") !=NULL){
    char *s = strstr(p,"cmd=light&v="), *e = NULL;
    int v1=0,v2=0,v3=0;
     s = s+strlen("cmd=light&v=");if(*s==0x00) return;
    char temp[64];
    int i=0,j=0;
    char v=0;
    for(i=0;i<strlen(s);i++)
    {
      if(*(s+i)!='|' || *(s+i)!=0x00)
        {
          temp[j++]=*(s+i);
        }
      if(*(s+i)=='|'){
        temp[j]=0x00;
        if(v==0) v1 = atoi(temp);
        if(v==1) v2 = atoi(temp);
        if(v==2) v3 = atoi(temp);
        v++;j=0;
      }
    }
    if(*(s+strlen(s)-1) !='|')
    v3 = atoi(temp);
    Serial.printf("Value1:%d,%d,%d\r\n",v1,v2,v3);
    v1 = (int)((255 - v1)*1023/255.0);
    v2 = (int)((255 - v2)*1023/255.0);
    v3 = (int)((255 - v3)*1023/255.0);
    v1 = v1%1024;v2 = v2%1024;v3 = v3%1024;
    Serial.printf("Value2:%d,%d,%d\r\n",v1,v2,v3);
    analogWrite(LEDR, v1);//Call `analogWrite(pin, 0)` to disable PWM on the pin.
    analogWrite(LEDG, v2);//Call `analogWrite(pin, 0)` to disable PWM on the pin.
    analogWrite(LEDB, v3);//Call `analogWrite(pin, 0)` to disable PWM on the pin.
    delay(10);
  }
}

/*
*TCP接收包处理
*/
void parseTCPPackage(char *p)
{
 Serial.print("[TCP parseData]");
  Serial.println(p);
  if(strstr(p,"cmd=subscribe&res=1") !=NULL){
    char t[128];
    sprintf(t,"cmd=m2m_chat&device_id=&s&device_key=%s&topic=%s_chat",config.id,config.key,config.id);
    sendTCP(t);
  }
  //cmd=publish&topic=DOIT-SN-851A-73814&message=3|2|3|
  else if(strstr(p,"&message=") !=NULL){
    char *s = strstr(p,"&message="), *e = NULL;
    int v1=0,v2=0,v3=0;
     s = s+strlen("&message=");if(*s==0x00) return;
    char temp[64];
    int i=0,j=0;
    char v=0;
    for(i=0;i<strlen(s);i++)
    {
      if(*(s+i)!='|' || *(s+i)!=0x00)
        {
          temp[j++]=*(s+i);
        }
      if(*(s+i)=='|'){
        temp[j]=0x00;
        if(v==0) v1 = atoi(temp);
        if(v==1) v2 = atoi(temp);
        if(v==2) v3 = atoi(temp);
        v++;j=0;
      }
    }
    if(*(s+strlen(s)-1) !='|')
    v3 = atoi(temp);
    Serial.printf("Value1:%d,%d,%d\r\n",v1,v2,v3);
    v1 = (int)((255 - v1)*1023/255.0);
    v2 = (int)((255 - v2)*1023/255.0);
    v3 = (int)((255 - v3)*1023/255.0);
    v1 = v1%1024;v2 = v2%1024;v3 = v3%1024;
    Serial.printf("Value2:%d,%d,%d\r\n",v1,v2,v3);
    analogWrite(LEDR, v1);//Call `analogWrite(pin, 0)` to disable PWM on the pin.
    analogWrite(LEDG, v2);//Call `analogWrite(pin, 0)` to disable PWM on the pin.
    analogWrite(LEDB, v3);//Call `analogWrite(pin, 0)` to disable PWM on the pin.
    delay(10);
  }
}

/*
*串口接收包处理
*/
void parseUartPackage(char *p)
{
  Serial.println("[Uart parseData]");
  Serial.println(p);
  Serial.println("Invalid cmd");
}

