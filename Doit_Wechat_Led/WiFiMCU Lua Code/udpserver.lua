print('udpserver.lua')
if sckUdpSrv ~= nil then
	net.close(sckUdpSrv);sckUdpSrv = nil
end

collectgarbage()
print("UDP server heap size:"..mcu.mem().."  port:8089")
sckUdpSrv = net.new(net.UDP,net.SERVER) 
--net.on(sckUdpSrv,"sent",function(clt) print("sent:clt:"..clt) end)
--net.on(sckUdpSrv,"disconnect",function(clt) print("disconnect:clt:"..clt) end)
net.on(sckUdpSrv,"receive",function(clt,d)
	parseData(net.UDP,clt,d)
	d=nil;collectgarbage()
	net.close(clt)
end)

net.start(sckUdpSrv,8089) 