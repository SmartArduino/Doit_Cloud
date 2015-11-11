
if sckUdpSrv ~= nil then
	sckUdpSrv:close();sckUdpSrv = nil
end

sckUdpSrv=net.createServer(net.UDP) 
collectgarbage()
print("UDP server heap size:"..node.heap().."  port:8089")
sckUdpSrv:on("receive",function(c,l)
	 parseData(net.UDP,c,l)
	 l=nil;collectgarbage()
	end)
sckUdpSrv:listen(8089)
collectgarbage()
