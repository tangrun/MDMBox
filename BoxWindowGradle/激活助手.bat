@echo off
set JLINK_VM_OPTIONS=
set DIR=%~dp0\bin
cd %DIR%
start %DIR%\javaw -m com.tangrun.mdm.boxwindow/com.tangrun.mdm.boxwindow.Launcher
exit
