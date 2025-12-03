setlocal disabledelayedexpansion

set "ORIGINAL_DIR=%cd%"
set "MODE=%1"

cd "%userprofile%/Minecraft/test"
call gradlew.bat build

REM read .env file
for /f "usebackq tokens=1,2 delims==" %%i in ("scripts\.env") do (
    set "key=%%i"
    set "val=%%j"
    call :setvar
)
goto :continue
:setvar
setlocal enabledelayedexpansion
set "val=!val:'=!"
endlocal & set "%key%=%val%"
goto :eof
:continue

REM RESOURCE PACK
cd "%userprofile%/Minecraft/test/resourcepack/data"
if exist ..\data.zip del ..\data.zip
tar -a -cf ..\data.zip *
for /f "skip=1 tokens=* usebackq" %%a in (`certutil -hashfile ..\data.zip SHA1`) do (
    set "HASH=%%a"
    goto :hashDone
)
:hashDone
set "HASH=%HASH: =%"
echo SHA1: %HASH%
curl --insecure -T "..\data.zip" -u "%DATA_USER%:%DATA_PASS%" "ftp://%DATA_HOST%%DATA_PATH%/%HASH%.zip"
del ..\data.zip

if /i "%MODE%"=="PROD" goto :prod
if /i "%MODE%"=="LOCAL" goto :local
goto :local

:local
echo === LOCAL ===
powershell -Command "(Get-Content '%userprofile%\Minecraft\paper\server.properties') -replace '^resource-pack-sha1=.*$', 'resource-pack-sha1=%HASH%' | Set-Content '%userprofile%\Minecraft\paper\server.properties'"
powershell -Command "(Get-Content '%userprofile%\Minecraft\paper\server.properties') -replace '^resource-pack=.*$', 'resource-pack=%DATA_URL%/%HASH%.zip' | Set-Content '%userprofile%\Minecraft\paper\server.properties'"
copy /Y %userprofile%\Minecraft\test\build\libs\*.jar %userprofile%\Minecraft\paper\plugins\
cd %userprofile%/Minecraft/paper
start "Minecraft Server" /D "%userprofile%\Minecraft\paper" java -Xms4G -Xmx4G -jar paper.jar --nogui
goto :cleanup

:prod
echo === PROD ===
pscp -P %SERVER_PORT% -pw "%SERVER_PASS%" "%SERVER_USER%@%SERVER_HOST%:/server.properties" "..\server.properties"
powershell -Command "(Get-Content '..\server.properties') -replace '^resource-pack-sha1=.*$', 'resource-pack-sha1=%HASH%' | Set-Content '..\server.properties'"
powershell -Command "(Get-Content '..\server.properties') -replace '^resource-pack=.*$', 'resource-pack=%DATA_URL%/%HASH%.zip' | Set-Content '..\server.properties'"
pscp -P %SERVER_PORT% -pw "%SERVER_PASS%" "..\server.properties" "%SERVER_USER%@%SERVER_HOST%:/server.properties"
del ..\server.properties
pscp -P %SERVER_PORT% -pw "%SERVER_PASS%" "%userprofile%\Minecraft\test\build\libs\*.jar" "%SERVER_USER%@%SERVER_HOST%:/plugins/"
"%userprofile%\Minecraft\mcrcon\mcrcon.exe" -H %RCON_HOST% -P %RCON_PORT% -p "%RCON_PASS%" -c "restart"
goto :cleanup

:cleanup
cd "%ORIGINAL_DIR%"

endlocal