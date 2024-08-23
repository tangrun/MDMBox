
set BUILD_APP_VERSION=1.0.40823

call mvnw.cmd clean compile package exec:exec@image -f pom.xml -X

D:\NSIS\makensis.exe /DPRODUCT_VERSION=%BUILD_APP_VERSION% E:\ForkProject\MDMBox\BoxWindowGradle\installer.nsi



