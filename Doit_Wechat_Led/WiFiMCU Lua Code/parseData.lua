
function parseData(scktype,skt,data)
	if scktype==net.UDP then 
--处理UDP数据	
		print('Udp parseData:'..data)
		if string.find(data,'cmd=ping') ~=nil then
			if wifi.sta.getip() ~=nil then 
				if wifi.sta.getip()=='0.0.0.0' then
				net.send(skt,'cmd=pong&sta_ip=&host_ip=11.11.11.1')
				else
				net.send(skt,'cmd=pong&sta_ip='..wifi.sta.getip()..'&host_ip=11.11.11.1')
				end
			else
				net.send(skt,'cmd=pong&sta_ip=&host_ip=11.11.11.1')
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
				net.send(skt,"CMD OK!")
				print("Module will restart after 2s...")
				mcu.restart()
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
				if(t[1]==nil or t[2]==nil or t[3]==nil ) then return end
				pwm.start(ledR,freq,(255-t[1])*100/255);
				pwm.start(ledG,freq,(255-t[2])*100/255);
				pwm.start(ledB,freq,(255-t[3])*100/255);
				print("Parse Message: "..var..' '..t[1]..' '..t[2]..' '..t[3])
				t=nil
			end
			st=nil;var=nil
			net.send(skt,'ok')
		end
		return
	else
		print('TCP parseData:'..data)
	end
--订阅设备对话的topic	
	if string.find(data,'cmd=subscribe&res=1') ~=nil then
			net.send(skt,'cmd=m2m_chat&device_id='..cfg.device_id..'&device_key='..cfg.device_key..'&topic='..cfg.device_id..'_chat\r\n');
--处理远程控制命令
	elseif string.find(data,'&message=') ~=nil then
		local st;
		_,st=string.find(data,'&message=')
		local var = string.sub(data,st+1)
		if string.find(var,'&message=') ~=nil then
			_,st=string.find(var,'&message=')
			var = string.sub(var,st+1)
		end		
		--print('var:'..var)
		if #var>0 then
			local t=split(var,'|')
			if(t[1]==nil or t[2]==nil or t[3]==nil ) then return end
			pwm.start(ledR,freq,(255-t[1])*100/255);
			pwm.start(ledG,freq,(255-t[2])*100/255);
			pwm.start(ledB,freq,(255-t[3])*100/255);
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
    for match in string.gmatch(str..delimiter,"(.-)"..delimiter) do
        table.insert(result, match)
    end
    return result
end

print('parseData.lua')
--PWM初始化
ledR=13
ledB=14
ledG=15
freq=10000
pwm.start(ledR,freq,100);
pwm.start(ledB,freq,100);
pwm.start(ledG,freq,100);
