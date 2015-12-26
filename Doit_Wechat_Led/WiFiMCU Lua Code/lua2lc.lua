
local luaFile = {"micokit_rgb.lua","doNetTask.lua","httpserver.lua","udpserver.lua","parseData.lua","start.lua",}
for i, f in ipairs(luaFile) do
	if file.open(f) then
	  print('Start compile:'..f)
      file.close()
      print("	Compile File:"..f)
      file.compile(f)
	  print("	Remove File:"..f)
      file.remove(f)
	end
	collectgarbage()
 end
luaFile = nil
collectgarbage()
print("Compile Finished")