
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
		elseif string.find(data,'cmd=gpio&id=') ~=nil then
			local st;
			_,st = string.find(data,'cmd=gpio&id=')
			local var = string.sub(data,st+1)
			print('var:'..var)
			if #var>0 then
				if string.sub(var,1,5) == '0&v=0' then
					gpio.write(0,gpio.LOW)
				elseif string.sub(var,1,5) == '0&v=1' then
					gpio.write(0,gpio.HIGH)
				elseif string.sub(var,1,5) == '1&v=0' then
					gpio.write(1,gpio.LOW)
				elseif string.sub(var,1,5) == '1&v=1' then
					gpio.write(1,gpio.HIGH)
				elseif string.sub(var,1,4) == '2&v=' then
					local d =  string.sub(var,5)
					pwm.setduty(2,d*1023/100)
					d=nil
				elseif string.sub(var,1,4) == '4&v=' then
					local d =  string.sub(var,5)
					pwm.setduty(4,d*1023/100)
					d=nil
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
			conn:send('cmd=m2m_chat&device_id='..cfg.id..'&device_key='..cfg.key..'&topic='..cfg.id..'_chat\r\n');
--处理远程控制命令
	elseif string.find(data,'&message=') ~=nil then
		local st;
		_,st=string.find(data,'&message=')
		local var = string.sub(data,st+1)
		--print('var:'..var)
		if #var>0 then
			if string.sub(var,1,5) == 'led|1' then
				gpio.mode(0,gpio.OUTPUT);gpio.write(0,gpio.LOW);
			elseif string.sub(var,1,5) == 'led|0' then
				gpio.mode(0,gpio.OUTPUT);gpio.write(0,gpio.HIGH);
			else
				uart.write(0,var)
			end
		end
		st=nil;var=nil
	end	
end

pwm.setup(2,1000,1023);pwm.start(2);
pwm.setduty(2,0);
pwm.setup(4,1000,1023);pwm.start(4);
pwm.setduty(4,0);
gpio.mode(0,gpio.OUTPUT);
gpio.mode(1,gpio.OUTPUT);
