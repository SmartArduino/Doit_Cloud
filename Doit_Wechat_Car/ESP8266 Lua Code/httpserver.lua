
local function sendParaData(conn)
	local ip = wifi.sta.getip(); if ip==nil then ip="---.---.---.---" end
	local para=ip.."^$&"..wifi.sta.getmac().."^$&"..cfg.device_id.."^$&"..cfg.device_key.."^$&"..cfgSTA.ssid.."^$&"..cfgSTA.psw.."^$&"
	conn:send(para);
	para1=nil;ip=nil;
	collectgarbage()
end

fz=0
fOffset = 0
for k, v in pairs(file.list()) do
		if k == "index.html" then
			fz = v
			break
		end
end
k=nil;v=nil;collectgarbage()

sckTcpSrv=net.createServer(net.TCP,60);
clt=nil
sckTcpSrv:listen(80,function(conn) 
    conn:on("receive",function(conn,request)
		print("TCPSrv:"..request)
		if request==nil then return;end
		if string.find(request, "GET / HTTP/1.1") then
			conn:send([[
HTTP/1.1 200 OK
Content-Type:text/html
Connection: close

]])
			fOffset = 0
			--print("fOffset: "..fOffset)
		elseif string.find(request, "GET /para_list.do") then
			sendParaData(conn)
		elseif string.find(request, "GET /ap_list.do") then
			wifi.sta.getap( function(t) 
					--print("\r\n\r\nscan Callback\r\n")
					str=""
					if t then 
						for k,v in pairs(t) do 
							str = str..k.."^$&"
						end
					--print(str)
					conn:send(str)
					fOffset = 65535	
					str=nil
					else
					--print("\r\n\r\nscan failed!!!!!!!")
					end
				end)
		elseif string.find(request, "POST /submit") or string.find(request, "ssid=") then
			local k1,_ = string.find(request, "ssid=");
			local k2,_ = string.find(request, "device_id");
			if k1==nil or k2==nil then return;end
			local vars1 = string.sub(request,k1,k2-1);
			local vars2 = string.sub(request,k2);
			print("Save Para1:"..vars1)
			print("Save Para2:"..vars2)
			if #vars1 > 0 and #vars2>0 then
				print('Will save data!!!')
				file.remove("configSTA.lua")
				file.open("configSTA.lua","w+")
				file.writeline("cfgSTA={");
				file.write(vars1);
				file.writeline("}");
				file.close();
				--file.remove("config.lua")
				file.open("config.lua","w+")
				file.writeline("cfg={")
				file.write(vars2)
				file.writeline("server_addr='"..cfg.server_addr.."',")
				file.writeline("server_port="..cfg.server_port..",")
				file.writeline("apssid='Doit_ESP_'..node.chipid(),")
				file.writeline("}");
				file.close();		
				print('Save ok!!!')
				--conn:send([[Save success, Module will restart!]])
				node.restart()
			else
				--conn:send([[Save Failed! Module will restart!]])
			end --#vars > 0			
			fOffset = 65534
			vars1=nil;k1=nil;vars2=nil;k2=nil;
--page not found!
		else
			conn:send([[
HTTP/1.1 404 Not Found
Content-Type: text/html
Content-Length: 15
Connection: close

Page not found!
]])
		end
		collectgarbage()
		end)--end c:on receive

		conn:on("sent", function(conn)
				--print("fOffset: "..fOffset)
				if fOffset == 65534 then
					conn:close()
					node.restart()
					--tmr.alarm(4,2000,0,function() node.restart();end)
				elseif fOffset == 65535 then
					conn:close()
					conn = nil
					collectgarbage()
				else
					if file.open("index.html", "r") then
						file.seek("set", fOffset)
						local chunk = file.read(1024)
						file.close()
						if chunk then
							conn:send(chunk)
						end
						chunk = nil;
						fOffset = fOffset + 1024
						if fOffset > fz then
							fOffset = 65535
						end
					end
				end
		collectgarbage()
		end)  
	  collectgarbage()
    --print("TCPSrv:Client connected")
end)