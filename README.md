# cas-server-webapp
## https 证书制作
>1.创建keystore
```
cd /home/xucd/Workspace/webserver/tomcat-sso/config
keytool -genkeypair -keyalg RSA -keysize 2048 -sigalg SHA1withRSA -validity 36500 -alias cas -keystore cas.keystore -dname "CN=passport.dongfeng.com,OU=dongfeng,O=dongfeng,L=shanghai,ST=shanghai,C=CN"
```
>2.导出数字证书
```
keytool -exportcert -alias cas -keystore cas.keystore -file cas.cer -rfc
```
>3.导入到客户端Java证书库
```
cd $JAVA_HOME/jre/lib/security
keytool -import -alias cas -keystore cas -file /home/xucd/Workspace/webserver/tomcat-sso/config/cas.cer -trustcacerts
```
