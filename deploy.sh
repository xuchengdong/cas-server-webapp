#!/bin/sh
export CATALINA_BASE=/home/xucd/Workspace/webserver/tomcat-sso/tomcat-cas
echo $CATALINA_BASE
echo ========开始编译打包===========
mvn clean package
echo ========打包完成==============
echo ========杀掉进程==============
ps -ef |grep tomcat-cas |awk '{print $2}'|xargs kill -9
#kill -9 `ps -ef | grep tomcat-cas | tr -s ' ' | awk -F' ' '{print $2}'`
echo ========清除旧文件==============
rm -rf $CATALINA_BASE/webapps/cas*
echo ========复制新文件==============
cp ./target/cas.war $CATALINA_BASE/webapps
cd $CATALINA_BASE
echo ========启动tomcat==============
./bin/startup.sh | tail -f -n -250 ./logs/catalina.out

