--[[
0， 上电：Doit微信开发板上电，前5秒等待配置阶段LED灯0.1s亮，0.1s灭; 5秒后1秒亮1秒灭尝试连接到sta
1,	微信配置：
	前5s内，LED灯快闪（0.1s亮，0.1s灭），此时长按Flash键进入Airkiss，led灯变为两次快闪一次灭
	在这种情况下可使用微信“四博智联”公众号，wifi配置功能可实现wifi配置上网
	如果超过3分钟没有配置成功，模块重启
2,	ESP-Touch配置：
	前5s内，LED灯快闪（0.1s亮，0.1s灭），此时短按Flash键超过3秒进入Easylink，led灯变为0.3秒亮0.3秒灭
	如果超过3分钟没有配置成功，模块重启
3，	Webserver配置：Doit微信开发板上电5s后发出ap信号，启动httpserver
	在httpserver启动后，开发板发出wifi，ssid为：“Doit_ESP_xxxxxx”，其中xxxxxx为芯片chipid
	使用笔记本或者手机连接该ssid，密码为空，然后使用浏览器设置，地址为192.168.4.1
4, 	Doit微信开发板上LED灯常亮表示开发板已经连接上无线路由器
]]

--tmr0 用于tcp client
--tmr4 用于串口
--tmr1 检查sta是否连上，检查key
--tmr3 检查smartconfig进度
--tmr5 car control
--tmr6 用于重启

function resetCfgSTA()
	file.remove("configSTA.lua")
	file.open("configSTA.lua","w+")
	file.writeline("cfgSTA={")
	file.writeline("ssid='Doit',")
	file.writeline("psw='',")
	file.writeline("}")
	file.close()
end
function saveCfgSTA(ssid,psw)
	file.remove("configSTA.lua")
	file.open("configSTA.lua","w+")
	file.writeline("cfgSTA={")
	file.writeline("ssid='"..ssid.."',")
	file.writeline("psw='"..psw.."',")
	file.writeline("}")
	file.close()
end

function startAPSTA()
	wifi.sta.autoconnect(0)--diable auto connect
	wifi.setmode(wifi.STATIONAP)
	cfgTemp={}
	cfgTemp.ssid=cfg.apssid
	wifi.ap.config(cfgTemp)
	cfgTemp.ip="192.168.4.1"
	cfgTemp.netmask="255.255.255.0"
	cfgTemp.gateway="192.168.4.1"
	wifi.ap.setip(cfgTemp)
	--wifi.setmode(wifi.STATION)
	cfgTemp=nil
	wifi.sta.config(cfgSTA.ssid,cfgSTA.psw)
	wifi.sta.connect()
	dofile('httpserver.lc')
	dofile('parseData.lc')
	dofile('udpserver.lc')
	collectgarbage()
end

--wifi.ESPTOUCH wifi.AIR_KISS  softap
function startSmartConfig(mode)
	print('start smartconfig,mode(0:touch,1:airkiss,2:softap):'..mode)
	wifi.sta.disconnect()
	wifi.stopsmart()
	tmr.stop(0);tmr.stop(1);tmr.stop(2);tmr.stop(3)
	conn = nil;collectgarbage()	
	--mode=0 or 1
	wifi.setmode(wifi.STATION)
	smCnt=0
	tmr.alarm(3,1000,1,function()
		smCnt = smCnt+1
		if(smCnt>=3*60) then
			print('smartconfig Failed, restart!')
			node.restart()
		else 
			print('smartconfig is going...'..smCnt..'/180s')
		end
	end)
	wifi.startsmart(mode, function(ssid, psw)
		print("Success! SSID: "..ssid.." Password: "..psw)
		cfgSTA.ssid = ssid;cfgSTA.psw = psw
		saveCfgSTA(ssid,psw)
		tmr.stop(3);smCnt=nil
		collectgarbage()
		print("Module now restart!")
		node.restart()
	end)
		ledCnt=0;ledCnt2=0;
		tmr.alarm(2,300,1,function()
			if mode==0 then --esp touch mode
				toggleLed()
			else --air kiss mode
				ledCnt = ledCnt + 1; if (ledCnt%3 == 0) then ledCnt2 = ledCnt2 + 1;end
				if (ledCnt2%2==0) then
					toggleLed()
				else
					gpio.write(8,gpio.HIGH);gpio.write(7,gpio.HIGH);gpio.write(6,gpio.HIGH)
				end
			end
			
		end)
end

function checkKey()
	if keyCnt >= 30 then --long pressed
		if(sckTcpSrv ~=nil) then sckTcpSrv:close();sckTcpSrv = nil;collectgarbage() end
		print("checkKey long pressed keyCnt: "..keyCnt)
		keyCnt = 0
		startSmartConfig(1)
	elseif keyCnt>=3 and gpio.read(3) ==1 then --short pressed
		if(sckTcpSrv ~=nil) then sckTcpSrv:close();sckTcpSrv = nil;collectgarbage() end
		print("checkKey short pressed keyCnt: "..keyCnt)
		keyCnt = 0
		startSmartConfig(0)
	end

	if gpio.read(3) ==0 then
		keyCnt = keyCnt + 1
	else
		keyCnt = 0
	end
	collectgarbage()
end

function toggleLed()
if gpio.read(8)==1 then gpio.write(8,gpio.LOW) else gpio.write(8,gpio.HIGH) end
if gpio.read(7)==1 then gpio.write(7,gpio.LOW) else gpio.write(7,gpio.HIGH) end
if gpio.read(6)==1 then gpio.write(6,gpio.LOW) else gpio.write(6,gpio.HIGH) end
end
function gpioInit()
	gpio.mode(8,gpio.OUTPUT) --led
	gpio.write(8,gpio.HIGH)
	gpio.mode(7,gpio.OUTPUT) --led
	gpio.write(7,gpio.HIGH)
	gpio.mode(6,gpio.OUTPUT) --led
	gpio.write(6,gpio.HIGH)	
	gpio.mode(3,gpio.INPUT)--flash	
	keyCnt = 0
end

print('Module Start')
if file.open("configSTA.lua") then
	file.close("configSTA.lua")
else
	resetCfgSTA()
end
dofile('config.lua')
dofile('configSTA.lua')
gpioInit()

tryCnt=0
tmr.alarm(1,100,1,function()
	tryCnt = tryCnt+1
	if tryCnt <=5*10 then 
		checkKey()
	elseif tryCnt%10 ~=0 then
		return
	end
	toggleLed()
	if tryCnt==5*10 then 
		startAPSTA()
	end
	if tryCnt >=6*10  then
		print("Try connect to STA:"..cfgSTA.ssid.." PSW:"..cfgSTA.psw)
		if wifi.sta.getip() ~= nil then
			tmr.stop(1);
			tryCnt=nil;keyCnt=nil
			--if(sckTcpSrv ~=nil) then sckTcpSrv:close();sckTcpSrv = nil;collectgarbage() end
			print("Connected:"..wifi.sta.getip())
			gpio.write(0,gpio.LOW)
			collectgarbage()
			dofile("doNetTask.lc")
		end
	end
  end)

