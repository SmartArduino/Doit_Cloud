
print("WiFiMCU Started")
dofile('lua2lc.lua')
dofile('start.lc')
--[[tmr.alarm(0,3000,0,function() 
local luaFile = {"doNetTask.lua","httpserver.lua","udpserver.lua","parseData.lua","start.lua",}
for i, f in ipairs(luaFile) do
	print(node.heap())
	if file.open(f) then
      file.close()
      print("Compile File:"..f)
      node.compile(f)
	  print("Remove File:"..f)
      file.remove(f)
	end
	collectgarbage()
 end
luaFile = nil
collectgarbage()
tmr.alarm(0,500,0,function() 
	print("start file")
	if file.open("start.lc") then
		dofile("start.lc")
	else
		print("start.lc not exist")
	end
	end)
end)]]
