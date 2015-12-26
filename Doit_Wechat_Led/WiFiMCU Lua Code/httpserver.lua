local function sendWebPageData(clt)
	net.send(clt,([[
HTTP/1.1 200 OK
Content-Type:text/html
Connection: close

]]))
	file.open("index.html") 
	local d=''
	repeat 
		d=file.read()
		if d==nil then break;end
		net.send(clt,d)
	until d==nil
	file.close()
	net.close(clt);clt=nil
	d=nil
	collectgarbage()
end
local function sendParaData(clt)
	local ip = wifi.sta.getip(); if ip=='0.0.0.0' then ip="---.---.---.---" end
	local _,_,_,_,_,mac,_=wifi.ap.getipadv()
	local para=ip.."^$&"..mac.."^$&"..cfg.device_id.."^$&"..cfg.device_key.."^$&"..cfgSTA.ssid.."^$&"..cfgSTA.psw.."^$&"
	net.send(clt,para);
	para1=nil;ip=nil;mac=nil
	net.close(clt)
	collectgarbage()
end

--定义accept回调函数
local function accept_cb(clt,ip,port)
	print("accept ip:"..ip.." port:"..port.." clt:"..clt) 
end
local function disconnect_cb(clt)
	print("disconnect_cb clt:"..clt) 
end
--定义sent回调函数
function sent_cb(clt)
	print("sent,clt:"..clt)
end

--定义receive回调函数
function receive_cb(clt,request)
	print("receive data,clt:"..clt.." request:")
	print(request)
	if string.find(request, "GET / HTTP/1.1") then
			sendWebPageData(clt)
	elseif string.find(request, "GET /para_list.do") then
			sendParaData(clt)
	elseif string.find(request, "GET /ap_list.do") then
		wifi.scan(function(t) 
				str=""
				print("wifi scan ok")
				if t~=nil then 
					for k,v in pairs(t) do 
						str = str..k.."^$&"
					end
				--print(str)
				net.send(clt,str)
				print("wifi send ok")
				str=nil
				net.close(clt)
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
				file.remove("configSTA.lua")
				file.open("configSTA.lua","w+")
				file.writeline("cfgSTA={");
				file.write(vars1);
				file.writeline("}");
				file.close();
				file.remove("config.lua")
				file.open("config.lua","w+")
				file.writeline("cfg={")
				file.write(vars2)
				file.writeline("server_addr='"..cfg.server_addr.."',")
				file.writeline("server_port="..cfg.server_port..",")
				file.writeline("apssid='Doit_ESP_'")
				file.writeline("}");
				file.close();
				net.send(clt,[[Save success, Module will restart!]])
			else
				net.send(clt,[[Save Failed! Module will restart!]])
			end --#vars > 0			
			vars1=nil;k1=nil;vars2=nil;k2=nil;
			net.close(clt)
			print("Module will restart")
			mcu.reboot()
--page not found!
		else
			net.close(clt)
		end
		collectgarbage()
end

--注意网页中包含反斜杠n的时候，需要使用双斜杠

--建立http server
webskt = net.new(net.TCP,net.SERVER) 
net.on(webskt,"accept",accept_cb)
net.on(webskt,"disconnect",disconnect_cb)
net.on(webskt,"receive",receive_cb)
net.on(webskt,"sent",sent_cb)
net.start(webskt,8989) 
print('Start httpserver at port:8989')