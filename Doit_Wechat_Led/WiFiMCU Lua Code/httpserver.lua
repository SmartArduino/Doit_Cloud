
local function sendParaData(conn)
	local ip = wifi.sta.getip(); if ip==nil then ip="---.---.---.---" end
	local para1="'stassid':'"..cfgSTA.ssid.."',".."'stapsw':'"..cfgSTA.psw.."',".."'baud':'"..cfgSTA.baud.."',"
	local para2="'stamac':'"..wifi.sta.getmac().."',".."'staip':'"..ip.."'"
	local para3="settingsCallback({"..para1..para2.."});"
	local para =[[HTTP/1.1 200 OK
Content-Type:application/javascript;charset=UTF-8
Connection: close

]]
	conn:send(para..para3);
	para=nil;para1=nil;para2=nil;para3=nil;ip=nil;
	collectgarbage()
end

local function parsePara(request)
local k,_ = string.find(request, "baud=");
	local vars = string.sub(request,k);
	local k1=0;
	local k2=0;
	if #vars > 0 then
		k1=string.find(vars,'='); k2=string.find(vars,'&')
		baud= string.sub(vars,k1+1,k2-1)
		vars=string.sub(vars,k2+1);
		k1=string.find(vars,'='); k2=string.find(vars,'&')
		ssid= string.sub(vars,k1+1,k2-1)
		vars=string.sub(vars,k2+1);
		k1=string.find(vars,'='); k2=string.find(vars,'&')
		psw= string.sub(vars,k1+1)
		--print("Success! SSID: "..ssid.." Password: "..psw)
		saveCfgSTA(ssid,psw,baud)
	end
	k=nil;vars=nil;k1=nil;k2=nil;collectgarbage()
end

--print("Http server heap size:"..node.heap().."  port:80")

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
		--print("TCPSrv:"..request)
		if request==nil then return;end
		if string.find(request, "GET / HTTP/1.1") then
			conn:send([[
HTTP/1.1 200 OK
Content-Type:text/html
Connection: close

]])
			fOffset = 0
			--print("fOffset: "..fOffset)
		elseif string.find(request, "GET /para.js?") then
			sendParaData(conn)
		elseif string.find(request, "POST /para.cgi HTTP/1.1") then
			parsePara(request)
			conn:send([[
HTTP/1.1 200 OK
Content-Type: text/html
Content-Length: 34
Connection: close

Save success, Module will restart!
]])
			fOffset = 65534
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
				if fOffset == 65533 then
					conn:send([[<script type='text/javascript' src='/para.js?]]..tmr.now()..[['></script></body></html>']])
					fOffset = 65535
				elseif fOffset == 65534 then
					node.restart();
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
							fOffset = 65533
						end
					end
				end
		collectgarbage()
		end)  
	  collectgarbage()
    --print("TCPSrv:Client connected")
end)