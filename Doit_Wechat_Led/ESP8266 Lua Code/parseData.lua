
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
		elseif string.find(data,'cmd=light&v=') ~=nil then
			local st;
			_,st = string.find(data,'cmd=light&v=')
			local var = string.sub(data,st+1)
			print('var:'..var)
			if #var>0 then
				t=split(var,'|')
				pwm.setduty(ledR,(255-t[1])*1023/255);
				pwm.setduty(ledG,(255-t[2])*1023/255);
				pwm.setduty(ledB,(255-t[3])*1023/255);
				print("Parse Message: "..var..' '..t[1]..' '..t[2]..' '..t[3])
				t=nil
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
			local t=split(var,'|')
			pwm.setduty(ledR,(255-t[1])*1023/255);
			pwm.setduty(ledG,(255-t[2])*1023/255);
			pwm.setduty(ledB,(255-t[3])*1023/255);
			print("Parse Message: "..var..' '..t[1]..' '..t[2]..' '..t[3])
			t=nil
		end
		st=nil;var=nil
	end	
end

function split(str, delimiter)
	if str==nil or str=='' or delimiter==nil then
		return nil
	end
	
    local result = {}
    for match in (str..delimiter):gmatch("(.-)"..delimiter) do
        table.insert(result, match)
    end
    return result
end

--PWM初始化
ledR=4;	ledB=2;	ledG=1
pwm.setup(ledR,1000,1023);pwm.start(ledR);
pwm.setduty(ledR,1023);
pwm.setup(ledG,1000,1023);pwm.start(ledG);
pwm.setduty(ledG,1023);
pwm.setup(ledB,1000,1023);pwm.start(ledB);
pwm.setduty(ledB,1023);
