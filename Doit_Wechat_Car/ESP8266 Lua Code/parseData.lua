
function parseData(scktype,conn,data)
	if scktype==net.UDP then 
--处理UDP数据	
		print('Udp parseData:'..data)
		if string.find(data,'cmd=ping') ~=nil then
			if wifi.sta.getip() ~=nil then 
				conn:send('cmd=pong&sta_ip='..wifi.sta.getip()..'&host_ip=192.168.4.1')
			else
				conn:send('cmd=pong&sta_ip=&host_ip=192.168.4.1')
			end
			return
--处理ssid和psw
		elseif string.find(data,'cmd=config') ~=nil then
			local k,_ = string.find(data, "ssid=")
			if k==nil then return end
			local vars = string.sub(data,k)
			local k1=0
			local k2=0
			if #vars > 0 then
				k1=string.find(vars,'='); k2=string.find(vars,'&')
				ssid= string.sub(vars,k1+1,k2-1)
				vars=string.sub(vars,k2+1)
				k1=string.find(vars,'='); k2=string.find(vars,'&')
				psw= string.sub(vars,k1+1)
				print("Success! SSID: "..ssid.." Password: "..psw)
				saveCfgSTA(ssid,psw)
				conn:send("CMD OK!")
				print("Module will restart after 2s...")
				tmr.alarm(6,2000,0,function() node.restart() end)
			end
			k=nil;vars=nil;k1=nil;k2=nil;collectgarbage()
--处理UDP控制命令			
		elseif string.find(data,'cmd=control&d=') ~=nil then
			local st;
			_,st = string.find(data,'cmd=control&d=')
			local var = string.sub(data,st+1)
			print('var:'..var)
			if #var>0 then
				if string.sub(var,1,1) == '0' then
					pwm.setduty(1,0);pwm.setduty(2,0);stopFlag = true;JogTimeCnt=0;
				elseif string.sub(var,1,1) == '1' then
					gpio.write(3,gpio.HIGH);gpio.write(4,gpio.HIGH);JogTimeCnt=50;JogFlag=true
				elseif string.sub(var,1,1) == '2' then
					gpio.write(3,gpio.LOW);gpio.write(4,gpio.LOW);JogTimeCnt=50;JogFlag=true
				elseif string.sub(var,1,1) == '3' then
					gpio.write(3,gpio.LOW);gpio.write(4,gpio.HIGH);JogTimeCnt=2;JogFlag=true
				elseif string.sub(var,1,1) == '4' then
					gpio.write(3,gpio.HIGH);gpio.write(4,gpio.LOW);JogTimeCnt=2;JogFlag=true
				elseif string.sub(var,1,1) == '6' then
					spdTargetA = spdTargetA + 100; if spdTargetA > 1023 then spdTargetA = 1023; end
					spdTargetB = spdTargetA;
				elseif string.sub(var,1,1) == '7' then
					spdTargetA = spdTargetA - 100; if spdTargetA < 0 then spdTargetA = 0; end
					spdTargetB = spdTargetA;
				elseif string.sub(var,1,1) == '5' then
					conn:send('cmd=control&spd='..spdTargetA)
					return
				end
			end
			st=nil;var=nil
			conn:send('ok')
		end
		return
	else
		print('TCP parseData:'..data)
	end
--订阅设备对话的topic	
	if string.find(data,'cmd=subscribe&res=1') ~=nil then
		local str = 'cmd=m2m_chat&device_id='..cfg.device_id..'&device_key='..cfg.device_key..'&topic='..cfg.device_id..'_chat\r\n'
		conn:send(str)
--处理远程控制命令
	elseif string.find(data,'&message=') ~=nil then
		local st;
		_,st=string.find(data,'&message=')
		local var = string.sub(data,st+1)
		--print('var:'..var)
		if #var>0 then
			if string.sub(var,1,1) == '0' then
				pwm.setduty(1,0);pwm.setduty(2,0);stopFlag = true;JogTimeCnt=0;
			elseif string.sub(var,1,1) == '1' then
				gpio.write(3,gpio.HIGH);gpio.write(4,gpio.HIGH);JogTimeCnt=50;JogFlag=true
			elseif string.sub(var,1,1) == '2' then
				gpio.write(3,gpio.LOW);gpio.write(4,gpio.LOW);JogTimeCnt=50;JogFlag=true
			elseif string.sub(var,1,1) == '3' then
				gpio.write(3,gpio.LOW);gpio.write(4,gpio.HIGH);JogTimeCnt=2;JogFlag=true
			elseif string.sub(var,1,1) == '4' then
				gpio.write(3,gpio.HIGH);gpio.write(4,gpio.LOW);JogTimeCnt=2;JogFlag=true				
			end
		end
		st=nil;var=nil
	end	
end
--pwm端口
local function pwmInit()
	print('pwmInit')
	gpio.mode(1,gpio.OUTPUT);gpio.write(1,gpio.LOW);
	gpio.mode(2,gpio.OUTPUT);gpio.write(2,gpio.LOW);

	gpio.mode(3,gpio.OUTPUT);gpio.write(3,gpio.HIGH);
	gpio.mode(4,gpio.OUTPUT);gpio.write(4,gpio.HIGH);

	pwm.setup(1,1000,1023);--PWM 1KHz, Duty 1023
	pwm.start(1);pwm.setduty(1,0);
	pwm.setup(2,1000,1023);
	pwm.start(2);pwm.setduty(2,0);
end
--控制
function car_control()
	print('car_control')
	spdTargetA=1023;--target Speed
	spdTargetB=1023;--target Speed
	stopFlag=true;
	JogTimeCnt=0;
	JogFlag=false;
	tmr.alarm(5, 100, 1, function()
		if JogFlag==true then
			stopFlag = false
			if JogTimeCnt <=0 then
				JogFlag = false
				stopFlag = true
			end
			JogTimeCnt = JogTimeCnt -1
		end
		if stopFlag==false then
			pwm.setduty(1,spdTargetA)
			pwm.setduty(2,spdTargetB)
		else
			pwm.setduty(1,0)
			pwm.setduty(2,0)
		end
	end)
end

pwmInit()
car_control()