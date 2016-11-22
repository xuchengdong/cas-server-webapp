@rem cas deploy script
@echo off
set CATALINA_BASE=E:\Server\tomcat-https\apache-tomcat-8.5.5
echo CATALINA_BASE=%CATALINA_BASE%
echo ========开始编译打包===========
call mvn clean package
echo ========打包完成==============
echo ========杀掉进程==============
for /f "tokens=5" %%a in ('netstat -aon ^| find "8080" ^| find "LISTENING"') do taskkill /f /pid %%a
pushd %CATALINA_BASE%\webapps
echo ========清除旧文件==============
if exist cas.war (del cas*) else (echo cas.war not exists)
if exist cas (rd /s /q cas) else (echo cas not exists)
Popd
echo ========复制新文件==============
copy .\target\cas.war %CATALINA_BASE%\webapps
pushd %CATALINA_BASE%
echo ========启动tomcat==============
call bin/startup.bat
Popd
