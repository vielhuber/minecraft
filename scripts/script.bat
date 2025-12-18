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
REM Copy server-icon.png
copy /Y "%userprofile%\Minecraft\test\scripts\server-icon.png" "%userprofile%\Minecraft\paper\server-icon.png"
REM Merge server.properties
powershell -Command "$target='%userprofile%\Minecraft\paper\server.properties'; $source='%userprofile%\Minecraft\test\scripts\server.properties'; $props=@{}; if(Test-Path $target){Get-Content $target | ForEach-Object{if($_ -match '^([^#=]+)=(.*)$'){$props[$matches[1].Trim()]=$matches[2]}}}; Get-Content $source | ForEach-Object{if($_ -match '^([^#=]+)=(.*)$'){$props[$matches[1].Trim()]=$matches[2]}}; $props['resource-pack-sha1']='%HASH%'; $props['resource-pack']='%DATA_URL%/%HASH%.zip'; $props.GetEnumerator() | ForEach-Object{\"$($_.Key)=$($_.Value)\"} | Set-Content $target"
copy /Y %userprofile%\Minecraft\test\build\libs\*.jar %userprofile%\Minecraft\paper\plugins\
cd %userprofile%/Minecraft/paper
start "Minecraft Server" /D "%userprofile%\Minecraft\paper" java -Xms4G -Xmx4G -jar paper.jar --nogui
goto :cleanup

:prod
echo === PROD ===
REM Copy server-icon.png
pscp -P %SERVER_PORT% -pw "%SERVER_PASS%" "%userprofile%\Minecraft\test\scripts\server-icon.png" "%SERVER_USER%@%SERVER_HOST%:/server-icon.png"
REM Merge server.properties
pscp -P %SERVER_PORT% -pw "%SERVER_PASS%" "%SERVER_USER%@%SERVER_HOST%:/server.properties" "..\server.properties"
powershell -Command "$target='..\server.properties'; $source='%userprofile%\Minecraft\test\scripts\server.properties'; $props=@{}; if(Test-Path $target){Get-Content $target | ForEach-Object{if($_ -match '^([^#=]+)=(.*)$'){$props[$matches[1].Trim()]=$matches[2]}}}; Get-Content $source | ForEach-Object{if($_ -match '^([^#=]+)=(.*)$'){$props[$matches[1].Trim()]=$matches[2]}}; $props['resource-pack-sha1']='%HASH%'; $props['resource-pack']='%DATA_URL%/%HASH%.zip'; $props.GetEnumerator() | ForEach-Object{\"$($_.Key)=$($_.Value)\"} | Set-Content $target"
pscp -P %SERVER_PORT% -pw "%SERVER_PASS%" "..\server.properties" "%SERVER_USER%@%SERVER_HOST%:/server.properties"
del ..\server.properties
pscp -P %SERVER_PORT% -pw "%SERVER_PASS%" "%userprofile%\Minecraft\test\build\libs\*.jar" "%SERVER_USER%@%SERVER_HOST%:/plugins/"
"%userprofile%\Minecraft\mcrcon\mcrcon.exe" -H %RCON_HOST% -P %RCON_PORT% -p "%RCON_PASS%" -c "restart"
goto :cleanup

:cleanup
cd "%ORIGINAL_DIR%"

endlocal