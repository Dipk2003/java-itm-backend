@echo off
cd /d "D:\itech-backend\itech-backend"
set JAVA_HOME=C:\Program Files\Java\jdk-11.0.19
set PATH=%JAVA_HOME%\bin;%PATH%
.\mvnw.cmd spring-boot:run
pause
