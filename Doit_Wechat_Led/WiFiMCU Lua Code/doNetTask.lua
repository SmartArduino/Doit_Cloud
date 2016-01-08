
--uart  disable output
--node.output(function(str) end,0)
--[[sBuf=""
function s_output(d)
	sBuf = sBuf..d;
	tmr.stop(2)
	tmr.start(2,100,function()
		--print('Uart: '..sBuf)
		if flagConnected==true then
			local str = 'cmd=m2m_chat&device_id='..cfg.device_id..'&device_key='..cfg.key..'&topic='..cfg.device_key..'_chat&message='..sBuf..'\r\n'
			local str2="m2m_chat:("..(tmr.now()/1000).."ms) "
			net.send(skt,str)
			print(str2..str)
			str = nil;str2=nil;
		end
		sBuf = ""
	end);
	d=nil; 
	collectgarbage();
end
uart.on(1, 'data',s_output)]]

--tcp client
print("Start TCP Client");

flagConnected=false;
cnt = 0;
tmr.start(0, 1000, function()
	cnt=cnt+1
	if cnt%5==0 then
		if flagConnected==false then
		print("Try connect Server")
		skt = net.new(net.TCP,net.CLIENT) 
		net.on(skt,"connect",function(skt) 
			print("TCPClient:conneted to server");
			flagConnected = true;
			net.send(skt,'cmd=subscribe&device_id='..cfg.device_id..'&device_key='..cfg.device_key..'\r\n')
			end)
		net.on(skt,"disconnect",function(skt) 
			print("disconnect:skt:"..skt)
			flagConnected = false;
			net.close(skt);
			skt=nil;
			collectgarbage();
			end)
		net.on(skt,"receive",function(skt,d)
			--print("TCPClient:[Receive]"..d)
			parseData(net.TCP,skt,d)
			collectgarbage();
			end)
		net.on(skt,"dnsfound",function(skt,ip) print("dnsfound: skt:"..skt.." ip:"..ip) end)
		net.start(skt,cfg.server_port,cfg.server_addr) 
		print("Connect to:"..cfg.server_addr.." port:"..cfg.server_port)
		end 
	end --end if cnt%5==0 then
	
	if flagConnected == true then
		if cnt%300==0 then
			local str = 'cmd=keep&device_id='..cfg.device_id..'&device_key='..cfg.device_key..'\r\n'
			local str2="keep:("..(tmr.tick()/1000).."ms)"
			net.send(skt,str)
			print(str2..str)
			str = nil;str2=nil;
			collectgarbage();
		end
	end
end)
