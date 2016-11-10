#!/bin/sh
cd ~/Workspace/IdeaProjects/cas-server-webapp
mvn clean package
ps -ef |grep tomcat-cas |awk '{print $2}'|xargs kill -9
rm -rf ~/Workspace/webserver/tomcat-sso/tomcat-cas/webapps/cas*
cp ./target/cas.war ~/Workspace/webserver/tomcat-sso/tomcat-cas/webapps
cd ~/Workspace/webserver/tomcat-sso/tomcat-cas
./bin/startup.sh | tail -f ./logs/catalina.out

