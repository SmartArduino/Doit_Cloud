
local luaFile = {"lua2lc.lua","doNetTask.lua","httpserver.lua","udpserver.lua","parseData.lua","start.lua",}
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
print("Compile Finished")