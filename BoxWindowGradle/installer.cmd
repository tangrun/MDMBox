set BUILD_APP_VERSION=1.0.41010
rem ����ѡ�1�� ���� 2�� ���  0�� �������
set mode=0

if %mode%==0 or %mode%==1 (
    call mvnw.cmd clean compile package exec:exec@image -f pom.xml -X
    xcopy .\app\ .\target\jPackageOut\AndroidActivateAssistant\app\ /Y /E
)
if %mode%==0 or %mode%==2 (
    D:\NSIS\makensis.exe /DPRODUCT_VERSION=%BUILD_APP_VERSION% E:\ForkProject\MDMBox\BoxWindowGradle\installer.nsi
)






