
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
		elseif string.find(data,'cmd=get') ~=nil then
			local temp = (1-gpio.read(1))..'|'..(1-gpio.read(2))..'|'..(1-gpio.read(4))..'|'..(1-gpio.read(5))
			conn:send("cmd=get&stat="..temp)
			temp = nil
		elseif string.find(data,'cmd=control&v=') ~=nil then
			local st;
			_,st = string.find(data,'cmd=control&v=')
			local var = string.sub(data,st+1)
			print('var:'..var)
			if #var>0 then
				if var=='1|1' then
					gpio.write(1,gpio.LOW)
				elseif var=='1|0' then
					gpio.write(1,gpio.HIGH)
				elseif var=='2|1' then
					gpio.write(2,gpio.LOW)
				elseif var=='2|0' then
					gpio.write(2,gpio.HIGH)
				elseif var=='3|1' then
					gpio.write(4,gpio.LOW)
				elseif var=='3|0' then
					gpio.write(4,gpio.HIGH)
				elseif var=='4|1' then
					gpio.write(5,gpio.LOW)
				elseif var=='4|0' then
					gpio.write(5,gpio.HIGH)
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
--返回插座当前状态
	elseif string.find(data,'cmd=pipe&message=get_stat') ~=nil then
		local temp = (1-gpio.read(1))..'|'..(1-gpio.read(2))..'|'..(1-gpio.read(4))..'|'..(1-gpio.read(5))
		conn:send('cmd=pope&device_id='..cfg.id..'&device_key='..cfg.key..'&message='..temp..'\r\n')
		temp=nil
--处理远程控制命令
	elseif string.find(data,'cmd=publish&message=p_port') ~=nil then
		local st;
		_,st=string.find(data,'p_port')
		local var = string.sub(data,st+1,#data-2)
		print('var:'..var)
		if #var>0 then
			if var=='1|1' then
				gpio.write(1,gpio.LOW)
			elseif var=='1|0' then
				gpio.write(1,gpio.HIGH)
			elseif var=='2|1' then
				gpio.write(2,gpio.LOW)
			elseif var=='2|0' then
				gpio.write(2,gpio.HIGH)
			elseif var=='3|1' then
				gpio.write(4,gpio.LOW)
			elseif var=='3|0' then
				gpio.write(4,gpio.HIGH)
			elseif var=='4|1' then
				gpio.write(5,gpio.LOW)
			elseif var=='4|0' then
				gpio.write(5,gpio.HIGH)
			end
		end
		st=nil;var=nil
	end	
end

--初始化端口
gpio.mode(1,gpio.OUTPUT);gpio.write(1,gpio.HIGH);
gpio.mode(2,gpio.OUTPUT);gpio.write(2,gpio.HIGH);
gpio.mode(4,gpio.OUTPUT);gpio.write(4,gpio.HIGH);
gpio.mode(5,gpio.OUTPUT);gpio.write(5,gpio.HIGH);