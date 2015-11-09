--[[1,Doit微信开发板上电，打开softap同时尝试连接sta。1s亮，1s灭，如1分钟内没有连接上，进入httpserver配置，0.07秒亮，0.07秒灭
2,未连接到sta时，短按key，进入smartconfig，0.3秒亮0.3秒灭，若3分钟内没有成功，进入softap的httpserver模式
3,未连接到sta时，长按key超过3秒，进入airkiss，闪两次，灭1次，若3分钟内没有成功，进入softap的httpserver模式
4,Doit微信开发板上LED灯常亮表示开发板已经连接上无线路由器。]]

--tmr0 用于tcp client
--tmr4 用于串口
--tmr1 检查sta是否连上
--tmr2 key / flash led
--tmr3 检查smartconfig进度
--tmr5 car control
--tmr6 用于重启

function resetCfgSTA()
	file.remove("configSTA.lua")
	file.open("configSTA.lua","w+")
	file.writeline("cfgSTA={")
	file.writeline("ssid='Doit',")
	file.writeline("psw='12345678',")
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
	collectgarbage()
end

--wifi.ESPTOUCH wifi.AIR_KISS  softap
function startSmartConfig(mode)
	print('start smartconfig,mode(0:touch,1:airkiss,2:softap):'..mode)
	wifi.sta.disconnect()
	wifi.stopsmart()
	tmr.stop(0);tmr.stop(1);tmr.stop(2)
	conn = nil;collectgarbage()	
	if mode==2 then 
		cfgTemp={}
		cfgTemp.ssid=cfg.apssid
		wifi.ap.config(cfgTemp)
		cfgTemp=nil
		wifi.setmode(wifi.SOFTAP)
		if file.open("httpserver.lc") then
			file.close("httpserver.lc")
			dofile('httpserver.lc')
		end
		tmr.alarm(2,70,1,function()
			if gpio.read(0)==1 then gpio.write(0,gpio.LOW) else gpio.write(0,gpio.HIGH) end
		end)
	else --mode=0 or 1
		wifi.setmode(wifi.STATION)
		smCnt=0
		tmr.alarm(3,1000,1,function()
			smCnt = smCnt+1
			if(smCnt>=3*60) then
				--print('smartconfig Failed, restart!')
				--node.restart()
				print('smartconfig Failed,start soft ap')
				startSmartConfig(2)
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
			print("Module will restart after 10s...")
			tmr.alarm(1,10000,0,function() node.restart() end)
		end)
		ledCnt=0;ledCnt2=0;
		tmr.alarm(2,300,1,function()
			if mode==0 then --esp touch mode
				if gpio.read(0)==1 then gpio.write(0,gpio.LOW) else gpio.write(0,gpio.HIGH) end
			else --air kiss mode
				ledCnt = ledCnt + 1; if (ledCnt%3 == 0) then ledCnt2 = ledCnt2 + 1;end
				if (ledCnt2%2==0) then
					if gpio.read(0)==1 then gpio.write(0,gpio.LOW) else gpio.write(0,gpio.HIGH) end
				else
					gpio.write(0,gpio.HIGH)
				end
			end
			
		end)
	end --end mode=0 or 1
end

function checkKey()
	if gpio.read(3)==0 then
		keyCnt = keyCnt + 1
		if keyCnt >= 50 then --long pressed
			print("checkKey long pressed keyCnt: "..keyCnt)
			startSmartConfig(1)
		end
	else
		if keyCnt>=5 then --short pressed
			print("checkKey short pressed keyCnt: "..keyCnt)
			startSmartConfig(0)
		end
		keyCnt = 0
	end
end

function gpioInit()
	gpio.mode(0,gpio.OUTPUT) --led
	gpio.write(0,gpio.LOW)
	gpio.mode(3,gpio.INPUT)--flash
	
	keyCnt = 0
	tmr.alarm(2,100,1,checkKey)
end

print('Module Start')
if file.open("config.lua") then
	file.close("config.lua")
else
	print('config.lua not exist!')
	return
end
if file.open("configSTA.lua") then
	file.close("configSTA.lua")
else
	resetCfgSTA()
end
dofile('config.lua')
dofile('configSTA.lua')
dofile('parseData.lc')
gpioInit()
startAPSTA()
if file.open("udpserver.lc") then
	file.close("udpserver.lc")
	dofile('udpserver.lc')
end
tryCnt=0
tmr.alarm(1,1000,1,function()
	if wifi.sta.getip() == nil and tryCnt<60 then
		if gpio.read(0)==1 then gpio.write(0,gpio.LOW) else gpio.write(0,gpio.HIGH) end
		print("Connecting:"..cfgSTA.ssid..'  '..tryCnt.."/60s")
		tryCnt = tryCnt+1
	else
		tmr.stop(1);
		if tryCnt>=60 then 
			tryCnt=nil;collectgarbage()
			startSmartConfig(2)
		else
			tryCnt=nil;collectgarbage()
			print("Connected:"..wifi.sta.getip())
			gpio.write(0,gpio.LOW)
			dofile("doNetTask.lc")
		end
	end
   end)

