@rem cas deploy script
@echo off
set CATALINA_BASE=E:\Server\tomcat-https\apache-tomcat-8.5.5
echo CATALINA_BASE=%CATALINA_BASE%
echo ========��ʼ������===========
call mvn clean package
echo ========������==============
echo ========ɱ������==============
for /f "tokens=5" %%a in ('netstat -aon ^| find "8080" ^| find "LISTENING"') do taskkill /f /pid %%a
pushd %CATALINA_BASE%\webapps
echo ========������ļ�==============
if exist cas.war (del cas*) else (echo cas.war not exists)
if exist cas (rd /s /q cas) else (echo cas not exists)
Popd
echo ========�������ļ�==============
copy .\target\cas.war %CATALINA_BASE%\webapps
pushd %CATALINA_BASE%
echo ========����tomcat==============
call bin/startup.bat
Popd
