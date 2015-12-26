--[[
0， 上电：WiFiMCU开发板上电，前5秒等待配置阶段LED灯0.1s亮，0.1s灭; 5秒后1秒亮1秒灭尝试连接到sta
1,	微信配置：
	前5s内，LED灯快闪（0.1s亮，0.1s灭），此时长按Boot键进入Airkiss，led灯变为两次快闪一次灭
	在这种情况下可使用微信“四博智联”公众号，wifi配置功能可实现wifi配置上网
	如果超过3分钟没有配置成功，模块重启
2,	Easylink配置：
	前5s内，LED灯快闪（0.1s亮，0.1s灭），此时短按Boot键超过3秒进入Easylink，led灯变为0.3秒亮0.3秒灭
	如果超过3分钟没有配置成功，模块重启
3，	Webserver配置：WiFiMCU启动5s后正常发出ap信号，启动httpserver
	在httpserver启动后，WiFiMCU发出wifi，ssid为：“Doit_ESP_xxxxxx”，
	其中xxxxxx为芯片mac后六位。使用笔记本或者手机连接该ssid，密码为空，然后使用浏览器设置，
	地址为11.11.11.1
4, 	WiFiMCU开发板上LED灯常亮表示开发板已经连接上无线路由器
5,  D1为低电平时进入编译模式，否则直接运行]]
--tmr0 检查boot按键，sta连接情况
--tmr1 smartconfig

function resetCfgSTA()
	file.remove("configSTA.lua")
	file.open("configSTA.lua","w+")
	file.writeline("cfgSTA={")
	file.writeline("ssid='Doit',")
	file.writeline("psw='',")
	file.writeline("baud='9600',")
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
	print('startAPSTA:heapsize:'..mcu.mem())
	local _,_,_,_,_,mac,_=wifi.ap.getipadv()
	local cfg={ssid = cfg.apssid..string.upper(string.sub(mac,7,12)),pwd =""}
	wifi.startap(cfg)
	cfg={ssid = cfgSTA.ssid,pwd = cfgSTA.psw}
	wifi.startsta(cfg)
	cfg=nil;mac=nil;
	collectgarbage()
	print('startAPSTA:heapsize:'..mcu.mem())
end

function startSmartConfig(mode)
	print('start smartconfig,mode(0:easylink,1:airkiss,2:softap):'..mode)
	tmr.stopall()
	wifi.stop()
	wifi.stopsmartconfig()
	wifi.smartconfig(mode,3*60,function(ssid, psw)--timeout:60 seconds
		if(ssid ~=nil) then
			print("Success! SSID: "..ssid.." Password: "..psw)
			cfgSTA.ssid = ssid;cfgSTA.psw = psw
			saveCfgSTA(ssid,psw)
			print("Module will restart")
			mcu.reboot()
		else
			print("Timeout!")
		end
	end)
	smCnt=0;ledCnt=0;ledCnt2=0;
	tmr.start(1,300,function()
		smCnt = smCnt + 1
		--check timeout
		if(smCnt>=700) then
			print('smartconfig Timeout! restart!')
			mcu.reboot()
		end
		if mode==0 then --easylink mode
			 toggleRGBLED()
		else --air kiss mode
			ledCnt = ledCnt + 1
			if (ledCnt%3 == 0) then ledCnt2 = ledCnt2 + 1;end
			if (ledCnt2%2==0) then
				gpio.toggle(17)
			else
				gpio.write(17,gpio.HIGH)
			end
		end
		
	end)
end

function checkKey()
	if keyCnt >= 30 then --long pressed
		print("checkKey long pressed keyCnt: "..keyCnt)
		keyCnt = 0
		startSmartConfig(1)
	elseif keyCnt>=3 and gpio.read(0) ==1 then --short pressed
		print("checkKey short pressed keyCnt: "..keyCnt)
		keyCnt = 0
		startSmartConfig(0)
	end

	if gpio.read(0) ==0 then
		keyCnt = keyCnt + 1
	else
		keyCnt = 0
	end
	collectgarbage()
end

function gpioInit()
	gpio.mode(17,gpio.OUTPUT) --led
	gpio.write(17,gpio.LOW)
	gpio.mode(0,gpio.INPUT,gpio.PULLUP)--boot
	keyCnt = 0
end

--print('Module Start')
if file.open("configSTA.lua") then
	file.close("configSTA.lua")
else
	resetCfgSTA()
end
dofile('configSTA.lua')
dofile('config.lua')
gpioInit()
--startAPSTA()

tryCnt=0
sckTcpSrv=nil
--[[if file.open("httpserver.lc") then
	file.close("httpserver.lc")
	dofile('httpserver.lc')
end]]

tmr.start(0,100,function()
	tryCnt = tryCnt+1
	if tryCnt <=5*10 then 
		checkKey()
	elseif tryCnt%10 ~=0 then
		return
	end

	gpio.toggle(17)

	if tryCnt==5*10 then 
		startAPSTA()
		dofile('parseData.lc')
		dofile('udpserver.lc')
		dofile('httpserver.lc')
	end
	if tryCnt >=6*10  then		
		if wifi.sta.getip() ~= '0.0.0.0' then
			tmr.stop(0);
			tryCnt=nil;keyCnt=nil;flagRGBLED=nil
			--if(sckTcpSrv ~=nil) then sckTcpSrv:close();sckTcpSrv = nil;collectgarbage() end
			print("Connected:"..wifi.sta.getip())
			gpio.write(17,gpio.LOW)
			collectgarbage()
			print("mem:"..mcu.mem())
			print("dofile doNetTask")
			dofile("doNetTask.lc")			
		end
	end
   end)

