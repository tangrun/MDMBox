; Script generated by the HM NIS Edit Script Wizard.

; HM NIS Edit Wizard helper defines
!define PRODUCT_NAME "激活助手"
;!define PRODUCT_VERSION "1.0.40823"
!define PRODUCT_PUBLISHER "cdblue"
!define PRODUCT_DIR_REGKEY "Software\Microsoft\Windows\CurrentVersion\App Paths\AndroidActivateAssistant.exe"
!define PRODUCT_UNINST_KEY "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PRODUCT_NAME}"
!define PRODUCT_UNINST_ROOT_KEY "HKLM"

; MUI 1.67 compatible ------
!include "MUI.nsh"

; MUI Settings
!define MUI_ABORTWARNING
!define MUI_ICON "E:\ForkProject\MDMBox\BoxWindowGradle\src\main\resources\application.ico"
!define MUI_UNICON "E:\ForkProject\MDMBox\BoxWindowGradle\src\main\resources\application.ico"

; Language Selection Dialog Settings
!define MUI_LANGDLL_REGISTRY_ROOT "${PRODUCT_UNINST_ROOT_KEY}"
!define MUI_LANGDLL_REGISTRY_KEY "${PRODUCT_UNINST_KEY}"
!define MUI_LANGDLL_REGISTRY_VALUENAME "NSIS:Language"

; Welcome page
!insertmacro MUI_PAGE_WELCOME
; Directory page
!insertmacro MUI_PAGE_DIRECTORY
; Instfiles page
!insertmacro MUI_PAGE_INSTFILES
; Finish page
!define MUI_FINISHPAGE_RUN "$INSTDIR\AndroidActivateAssistant.exe"
!insertmacro MUI_PAGE_FINISH

; Uninstaller pages
!insertmacro MUI_UNPAGE_INSTFILES

; Language files
!insertmacro MUI_LANGUAGE "English"
!insertmacro MUI_LANGUAGE "SimpChinese"

; MUI end ------

Name "${PRODUCT_NAME} ${PRODUCT_VERSION}"
OutFile "${PRODUCT_NAME}_${PRODUCT_VERSION}-Setup.exe"
InstallDir "$PROGRAMFILES\AndroidActivateAssistant"
InstallDirRegKey HKLM "${PRODUCT_DIR_REGKEY}" ""
ShowInstDetails show
ShowUnInstDetails show

Function .onInit
  !insertmacro MUI_LANGDLL_DISPLAY
FunctionEnd

Section "MainSection" SEC01
  SetOutPath "$INSTDIR"
  SetOverwrite try
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\AndroidActivateAssistant.exe"
  CreateDirectory "$SMPROGRAMS\激活助手"
  CreateShortCut "$SMPROGRAMS\激活助手\激活助手.lnk" "$INSTDIR\AndroidActivateAssistant.exe"
  CreateShortCut "$DESKTOP\激活助手.lnk" "$INSTDIR\AndroidActivateAssistant.exe"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\AndroidActivateAssistant.ico"
  SetOutPath "$INSTDIR\app"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\app\.jpackage.xml"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\app\adb.exe"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\app\AdbWinApi.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\app\AdbWinUsbApi.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\app\AndroidActivateAssistant.cfg"
  SetOutPath "$INSTDIR\app\doc\css"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\app\doc\css\main.css"
  SetOutPath "$INSTDIR\app\doc\img"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\app\doc\img\allow_open_debug.png"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\app\doc\img\tip_device_registration.png"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\app\doc\img\withdrw_usb_allow.png"
  SetOutPath "$INSTDIR\app\doc"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\app\doc\index.html"
  SetOutPath "$INSTDIR\runtime\bin"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\api-ms-win-core-console-l1-1-0.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\api-ms-win-core-console-l1-2-0.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\api-ms-win-core-datetime-l1-1-0.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\api-ms-win-core-debug-l1-1-0.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\api-ms-win-core-errorhandling-l1-1-0.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\api-ms-win-core-file-l1-1-0.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\api-ms-win-core-file-l1-2-0.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\api-ms-win-core-file-l2-1-0.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\api-ms-win-core-handle-l1-1-0.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\api-ms-win-core-heap-l1-1-0.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\api-ms-win-core-interlocked-l1-1-0.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\api-ms-win-core-libraryloader-l1-1-0.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\api-ms-win-core-localization-l1-2-0.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\api-ms-win-core-memory-l1-1-0.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\api-ms-win-core-namedpipe-l1-1-0.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\api-ms-win-core-processenvironment-l1-1-0.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\api-ms-win-core-processthreads-l1-1-0.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\api-ms-win-core-processthreads-l1-1-1.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\api-ms-win-core-profile-l1-1-0.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\api-ms-win-core-rtlsupport-l1-1-0.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\api-ms-win-core-string-l1-1-0.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\api-ms-win-core-synch-l1-1-0.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\api-ms-win-core-synch-l1-2-0.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\api-ms-win-core-sysinfo-l1-1-0.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\api-ms-win-core-timezone-l1-1-0.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\api-ms-win-core-util-l1-1-0.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\api-ms-win-crt-conio-l1-1-0.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\api-ms-win-crt-convert-l1-1-0.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\api-ms-win-crt-environment-l1-1-0.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\api-ms-win-crt-filesystem-l1-1-0.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\api-ms-win-crt-heap-l1-1-0.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\api-ms-win-crt-locale-l1-1-0.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\api-ms-win-crt-math-l1-1-0.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\api-ms-win-crt-multibyte-l1-1-0.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\api-ms-win-crt-private-l1-1-0.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\api-ms-win-crt-process-l1-1-0.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\api-ms-win-crt-runtime-l1-1-0.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\api-ms-win-crt-stdio-l1-1-0.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\api-ms-win-crt-string-l1-1-0.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\api-ms-win-crt-time-l1-1-0.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\api-ms-win-crt-utility-l1-1-0.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\awt.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\fontmanager.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\freetype.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\java.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\javajpeg.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\jawt.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\jimage.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\jli.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\jsound.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\lcms.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\management.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\mlib_image.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\msvcp140.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\net.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\nio.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\prefs.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\rmi.dll"
  SetOutPath "$INSTDIR\runtime\bin\server"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\server\jvm.dll"
  SetOutPath "$INSTDIR\runtime\bin"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\splashscreen.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\ucrtbase.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\vcruntime140.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\vcruntime140_1.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\verify.dll"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\bin\zip.dll"
  SetOutPath "$INSTDIR\runtime\conf"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\conf\logging.properties"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\conf\net.properties"
  SetOutPath "$INSTDIR\runtime\conf\security"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\conf\security\java.policy"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\conf\security\java.security"
  SetOutPath "$INSTDIR\runtime\conf\security\policy\limited"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\conf\security\policy\limited\default_local.policy"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\conf\security\policy\limited\default_US_export.policy"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\conf\security\policy\limited\exempt_local.policy"
  SetOutPath "$INSTDIR\runtime\conf\security\policy"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\conf\security\policy\README.txt"
  SetOutPath "$INSTDIR\runtime\conf\security\policy\unlimited"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\conf\security\policy\unlimited\default_local.policy"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\conf\security\policy\unlimited\default_US_export.policy"
  SetOutPath "$INSTDIR\runtime\conf"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\conf\sound.properties"
  SetOutPath "$INSTDIR\runtime\legal\java.base"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.base\ADDITIONAL_LICENSE_INFO"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.base\aes.md"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.base\asm.md"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.base\ASSEMBLY_EXCEPTION"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.base\c-libutl.md"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.base\cldr.md"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.base\icu.md"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.base\LICENSE"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.base\public_suffix.md"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.base\unicode.md"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.base\wepoll.md"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.base\zlib.md"
  SetOutPath "$INSTDIR\runtime\legal\java.compiler"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.compiler\ADDITIONAL_LICENSE_INFO"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.compiler\ASSEMBLY_EXCEPTION"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.compiler\LICENSE"
  SetOutPath "$INSTDIR\runtime\legal\java.datatransfer"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.datatransfer\ADDITIONAL_LICENSE_INFO"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.datatransfer\ASSEMBLY_EXCEPTION"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.datatransfer\LICENSE"
  SetOutPath "$INSTDIR\runtime\legal\java.desktop"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.desktop\ADDITIONAL_LICENSE_INFO"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.desktop\ASSEMBLY_EXCEPTION"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.desktop\colorimaging.md"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.desktop\freetype.md"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.desktop\giflib.md"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.desktop\harfbuzz.md"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.desktop\jpeg.md"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.desktop\lcms.md"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.desktop\libpng.md"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.desktop\LICENSE"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.desktop\mesa3d.md"
  SetOutPath "$INSTDIR\runtime\legal\java.logging"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.logging\ADDITIONAL_LICENSE_INFO"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.logging\ASSEMBLY_EXCEPTION"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.logging\LICENSE"
  SetOutPath "$INSTDIR\runtime\legal\java.management"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.management\ADDITIONAL_LICENSE_INFO"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.management\ASSEMBLY_EXCEPTION"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.management\LICENSE"
  SetOutPath "$INSTDIR\runtime\legal\java.naming"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.naming\ADDITIONAL_LICENSE_INFO"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.naming\ASSEMBLY_EXCEPTION"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.naming\LICENSE"
  SetOutPath "$INSTDIR\runtime\legal\java.prefs"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.prefs\ADDITIONAL_LICENSE_INFO"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.prefs\ASSEMBLY_EXCEPTION"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.prefs\LICENSE"
  SetOutPath "$INSTDIR\runtime\legal\java.rmi"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.rmi\ADDITIONAL_LICENSE_INFO"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.rmi\ASSEMBLY_EXCEPTION"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.rmi\LICENSE"
  SetOutPath "$INSTDIR\runtime\legal\java.scripting"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.scripting\ADDITIONAL_LICENSE_INFO"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.scripting\ASSEMBLY_EXCEPTION"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.scripting\LICENSE"
  SetOutPath "$INSTDIR\runtime\legal\java.security.sasl"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.security.sasl\ADDITIONAL_LICENSE_INFO"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.security.sasl\ASSEMBLY_EXCEPTION"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.security.sasl\LICENSE"
  SetOutPath "$INSTDIR\runtime\legal\java.sql"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.sql\ADDITIONAL_LICENSE_INFO"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.sql\ASSEMBLY_EXCEPTION"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.sql\LICENSE"
  SetOutPath "$INSTDIR\runtime\legal\java.transaction.xa"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.transaction.xa\ADDITIONAL_LICENSE_INFO"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.transaction.xa\ASSEMBLY_EXCEPTION"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.transaction.xa\LICENSE"
  SetOutPath "$INSTDIR\runtime\legal\java.xml"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.xml\ADDITIONAL_LICENSE_INFO"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.xml\ASSEMBLY_EXCEPTION"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.xml\bcel.md"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.xml\dom.md"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.xml\jcup.md"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.xml\LICENSE"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.xml\xalan.md"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\java.xml\xerces.md"
  SetOutPath "$INSTDIR\runtime\legal\jdk.unsupported"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\jdk.unsupported\ADDITIONAL_LICENSE_INFO"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\jdk.unsupported\ASSEMBLY_EXCEPTION"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\legal\jdk.unsupported\LICENSE"
  SetOutPath "$INSTDIR\runtime\lib"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\lib\classlist"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\lib\fontconfig.bfc"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\lib\fontconfig.properties.src"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\lib\jawt.lib"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\lib\jrt-fs.jar"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\lib\jvm.cfg"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\lib\jvm.lib"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\lib\modules"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\lib\psfont.properties.ja"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\lib\psfontj2d.properties"
  SetOutPath "$INSTDIR\runtime\lib\security"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\lib\security\blocked.certs"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\lib\security\cacerts"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\lib\security\default.policy"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\lib\security\public_suffix_list.dat"
  SetOutPath "$INSTDIR\runtime\lib"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\lib\tzdb.dat"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\lib\tzmappings"
  SetOutPath "$INSTDIR\runtime"
  File "E:\ForkProject\MDMBox\BoxWindowGradle\target\jPackageOut\AndroidActivateAssistant\runtime\release"
SectionEnd

Section -AdditionalIcons
  SetOutPath $INSTDIR
  CreateShortCut "$SMPROGRAMS\激活助手\Uninstall.lnk" "$INSTDIR\uninst.exe"
SectionEnd

Section -Post
  WriteUninstaller "$INSTDIR\uninst.exe"
  WriteRegStr HKLM "${PRODUCT_DIR_REGKEY}" "" "$INSTDIR\AndroidActivateAssistant.exe"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "DisplayName" "$(^Name)"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "UninstallString" "$INSTDIR\uninst.exe"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "DisplayIcon" "$INSTDIR\AndroidActivateAssistant.exe"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "DisplayVersion" "${PRODUCT_VERSION}"
  WriteRegStr ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}" "Publisher" "${PRODUCT_PUBLISHER}"
SectionEnd


Function un.onUninstSuccess
  HideWindow
  MessageBox MB_ICONINFORMATION|MB_OK "$(^Name) 已成功地从你的计算机移除。"
FunctionEnd

Function un.onInit
!insertmacro MUI_UNGETLANGUAGE
  MessageBox MB_ICONQUESTION|MB_YESNO|MB_DEFBUTTON2 "你确实要完全移除 $(^Name) ，其及所有的组件？" IDYES +2
  Abort
FunctionEnd

Section Uninstall
  Delete "$INSTDIR\uninst.exe"
  Delete "$INSTDIR\runtime\release"
  Delete "$INSTDIR\runtime\lib\tzmappings"
  Delete "$INSTDIR\runtime\lib\tzdb.dat"
  Delete "$INSTDIR\runtime\lib\security\public_suffix_list.dat"
  Delete "$INSTDIR\runtime\lib\security\default.policy"
  Delete "$INSTDIR\runtime\lib\security\cacerts"
  Delete "$INSTDIR\runtime\lib\security\blocked.certs"
  Delete "$INSTDIR\runtime\lib\psfontj2d.properties"
  Delete "$INSTDIR\runtime\lib\psfont.properties.ja"
  Delete "$INSTDIR\runtime\lib\modules"
  Delete "$INSTDIR\runtime\lib\jvm.lib"
  Delete "$INSTDIR\runtime\lib\jvm.cfg"
  Delete "$INSTDIR\runtime\lib\jrt-fs.jar"
  Delete "$INSTDIR\runtime\lib\jawt.lib"
  Delete "$INSTDIR\runtime\lib\fontconfig.properties.src"
  Delete "$INSTDIR\runtime\lib\fontconfig.bfc"
  Delete "$INSTDIR\runtime\lib\classlist"
  Delete "$INSTDIR\runtime\legal\jdk.unsupported\LICENSE"
  Delete "$INSTDIR\runtime\legal\jdk.unsupported\ASSEMBLY_EXCEPTION"
  Delete "$INSTDIR\runtime\legal\jdk.unsupported\ADDITIONAL_LICENSE_INFO"
  Delete "$INSTDIR\runtime\legal\java.xml\xerces.md"
  Delete "$INSTDIR\runtime\legal\java.xml\xalan.md"
  Delete "$INSTDIR\runtime\legal\java.xml\LICENSE"
  Delete "$INSTDIR\runtime\legal\java.xml\jcup.md"
  Delete "$INSTDIR\runtime\legal\java.xml\dom.md"
  Delete "$INSTDIR\runtime\legal\java.xml\bcel.md"
  Delete "$INSTDIR\runtime\legal\java.xml\ASSEMBLY_EXCEPTION"
  Delete "$INSTDIR\runtime\legal\java.xml\ADDITIONAL_LICENSE_INFO"
  Delete "$INSTDIR\runtime\legal\java.transaction.xa\LICENSE"
  Delete "$INSTDIR\runtime\legal\java.transaction.xa\ASSEMBLY_EXCEPTION"
  Delete "$INSTDIR\runtime\legal\java.transaction.xa\ADDITIONAL_LICENSE_INFO"
  Delete "$INSTDIR\runtime\legal\java.sql\LICENSE"
  Delete "$INSTDIR\runtime\legal\java.sql\ASSEMBLY_EXCEPTION"
  Delete "$INSTDIR\runtime\legal\java.sql\ADDITIONAL_LICENSE_INFO"
  Delete "$INSTDIR\runtime\legal\java.security.sasl\LICENSE"
  Delete "$INSTDIR\runtime\legal\java.security.sasl\ASSEMBLY_EXCEPTION"
  Delete "$INSTDIR\runtime\legal\java.security.sasl\ADDITIONAL_LICENSE_INFO"
  Delete "$INSTDIR\runtime\legal\java.scripting\LICENSE"
  Delete "$INSTDIR\runtime\legal\java.scripting\ASSEMBLY_EXCEPTION"
  Delete "$INSTDIR\runtime\legal\java.scripting\ADDITIONAL_LICENSE_INFO"
  Delete "$INSTDIR\runtime\legal\java.rmi\LICENSE"
  Delete "$INSTDIR\runtime\legal\java.rmi\ASSEMBLY_EXCEPTION"
  Delete "$INSTDIR\runtime\legal\java.rmi\ADDITIONAL_LICENSE_INFO"
  Delete "$INSTDIR\runtime\legal\java.prefs\LICENSE"
  Delete "$INSTDIR\runtime\legal\java.prefs\ASSEMBLY_EXCEPTION"
  Delete "$INSTDIR\runtime\legal\java.prefs\ADDITIONAL_LICENSE_INFO"
  Delete "$INSTDIR\runtime\legal\java.naming\LICENSE"
  Delete "$INSTDIR\runtime\legal\java.naming\ASSEMBLY_EXCEPTION"
  Delete "$INSTDIR\runtime\legal\java.naming\ADDITIONAL_LICENSE_INFO"
  Delete "$INSTDIR\runtime\legal\java.management\LICENSE"
  Delete "$INSTDIR\runtime\legal\java.management\ASSEMBLY_EXCEPTION"
  Delete "$INSTDIR\runtime\legal\java.management\ADDITIONAL_LICENSE_INFO"
  Delete "$INSTDIR\runtime\legal\java.logging\LICENSE"
  Delete "$INSTDIR\runtime\legal\java.logging\ASSEMBLY_EXCEPTION"
  Delete "$INSTDIR\runtime\legal\java.logging\ADDITIONAL_LICENSE_INFO"
  Delete "$INSTDIR\runtime\legal\java.desktop\mesa3d.md"
  Delete "$INSTDIR\runtime\legal\java.desktop\LICENSE"
  Delete "$INSTDIR\runtime\legal\java.desktop\libpng.md"
  Delete "$INSTDIR\runtime\legal\java.desktop\lcms.md"
  Delete "$INSTDIR\runtime\legal\java.desktop\jpeg.md"
  Delete "$INSTDIR\runtime\legal\java.desktop\harfbuzz.md"
  Delete "$INSTDIR\runtime\legal\java.desktop\giflib.md"
  Delete "$INSTDIR\runtime\legal\java.desktop\freetype.md"
  Delete "$INSTDIR\runtime\legal\java.desktop\colorimaging.md"
  Delete "$INSTDIR\runtime\legal\java.desktop\ASSEMBLY_EXCEPTION"
  Delete "$INSTDIR\runtime\legal\java.desktop\ADDITIONAL_LICENSE_INFO"
  Delete "$INSTDIR\runtime\legal\java.datatransfer\LICENSE"
  Delete "$INSTDIR\runtime\legal\java.datatransfer\ASSEMBLY_EXCEPTION"
  Delete "$INSTDIR\runtime\legal\java.datatransfer\ADDITIONAL_LICENSE_INFO"
  Delete "$INSTDIR\runtime\legal\java.compiler\LICENSE"
  Delete "$INSTDIR\runtime\legal\java.compiler\ASSEMBLY_EXCEPTION"
  Delete "$INSTDIR\runtime\legal\java.compiler\ADDITIONAL_LICENSE_INFO"
  Delete "$INSTDIR\runtime\legal\java.base\zlib.md"
  Delete "$INSTDIR\runtime\legal\java.base\wepoll.md"
  Delete "$INSTDIR\runtime\legal\java.base\unicode.md"
  Delete "$INSTDIR\runtime\legal\java.base\public_suffix.md"
  Delete "$INSTDIR\runtime\legal\java.base\LICENSE"
  Delete "$INSTDIR\runtime\legal\java.base\icu.md"
  Delete "$INSTDIR\runtime\legal\java.base\cldr.md"
  Delete "$INSTDIR\runtime\legal\java.base\c-libutl.md"
  Delete "$INSTDIR\runtime\legal\java.base\ASSEMBLY_EXCEPTION"
  Delete "$INSTDIR\runtime\legal\java.base\asm.md"
  Delete "$INSTDIR\runtime\legal\java.base\aes.md"
  Delete "$INSTDIR\runtime\legal\java.base\ADDITIONAL_LICENSE_INFO"
  Delete "$INSTDIR\runtime\conf\sound.properties"
  Delete "$INSTDIR\runtime\conf\security\policy\unlimited\default_US_export.policy"
  Delete "$INSTDIR\runtime\conf\security\policy\unlimited\default_local.policy"
  Delete "$INSTDIR\runtime\conf\security\policy\README.txt"
  Delete "$INSTDIR\runtime\conf\security\policy\limited\exempt_local.policy"
  Delete "$INSTDIR\runtime\conf\security\policy\limited\default_US_export.policy"
  Delete "$INSTDIR\runtime\conf\security\policy\limited\default_local.policy"
  Delete "$INSTDIR\runtime\conf\security\java.security"
  Delete "$INSTDIR\runtime\conf\security\java.policy"
  Delete "$INSTDIR\runtime\conf\net.properties"
  Delete "$INSTDIR\runtime\conf\logging.properties"
  Delete "$INSTDIR\runtime\bin\zip.dll"
  Delete "$INSTDIR\runtime\bin\verify.dll"
  Delete "$INSTDIR\runtime\bin\vcruntime140_1.dll"
  Delete "$INSTDIR\runtime\bin\vcruntime140.dll"
  Delete "$INSTDIR\runtime\bin\ucrtbase.dll"
  Delete "$INSTDIR\runtime\bin\splashscreen.dll"
  Delete "$INSTDIR\runtime\bin\server\jvm.dll"
  Delete "$INSTDIR\runtime\bin\rmi.dll"
  Delete "$INSTDIR\runtime\bin\prefs.dll"
  Delete "$INSTDIR\runtime\bin\nio.dll"
  Delete "$INSTDIR\runtime\bin\net.dll"
  Delete "$INSTDIR\runtime\bin\msvcp140.dll"
  Delete "$INSTDIR\runtime\bin\mlib_image.dll"
  Delete "$INSTDIR\runtime\bin\management.dll"
  Delete "$INSTDIR\runtime\bin\lcms.dll"
  Delete "$INSTDIR\runtime\bin\jsound.dll"
  Delete "$INSTDIR\runtime\bin\jli.dll"
  Delete "$INSTDIR\runtime\bin\jimage.dll"
  Delete "$INSTDIR\runtime\bin\jawt.dll"
  Delete "$INSTDIR\runtime\bin\javajpeg.dll"
  Delete "$INSTDIR\runtime\bin\java.dll"
  Delete "$INSTDIR\runtime\bin\freetype.dll"
  Delete "$INSTDIR\runtime\bin\fontmanager.dll"
  Delete "$INSTDIR\runtime\bin\awt.dll"
  Delete "$INSTDIR\runtime\bin\api-ms-win-crt-utility-l1-1-0.dll"
  Delete "$INSTDIR\runtime\bin\api-ms-win-crt-time-l1-1-0.dll"
  Delete "$INSTDIR\runtime\bin\api-ms-win-crt-string-l1-1-0.dll"
  Delete "$INSTDIR\runtime\bin\api-ms-win-crt-stdio-l1-1-0.dll"
  Delete "$INSTDIR\runtime\bin\api-ms-win-crt-runtime-l1-1-0.dll"
  Delete "$INSTDIR\runtime\bin\api-ms-win-crt-process-l1-1-0.dll"
  Delete "$INSTDIR\runtime\bin\api-ms-win-crt-private-l1-1-0.dll"
  Delete "$INSTDIR\runtime\bin\api-ms-win-crt-multibyte-l1-1-0.dll"
  Delete "$INSTDIR\runtime\bin\api-ms-win-crt-math-l1-1-0.dll"
  Delete "$INSTDIR\runtime\bin\api-ms-win-crt-locale-l1-1-0.dll"
  Delete "$INSTDIR\runtime\bin\api-ms-win-crt-heap-l1-1-0.dll"
  Delete "$INSTDIR\runtime\bin\api-ms-win-crt-filesystem-l1-1-0.dll"
  Delete "$INSTDIR\runtime\bin\api-ms-win-crt-environment-l1-1-0.dll"
  Delete "$INSTDIR\runtime\bin\api-ms-win-crt-convert-l1-1-0.dll"
  Delete "$INSTDIR\runtime\bin\api-ms-win-crt-conio-l1-1-0.dll"
  Delete "$INSTDIR\runtime\bin\api-ms-win-core-util-l1-1-0.dll"
  Delete "$INSTDIR\runtime\bin\api-ms-win-core-timezone-l1-1-0.dll"
  Delete "$INSTDIR\runtime\bin\api-ms-win-core-sysinfo-l1-1-0.dll"
  Delete "$INSTDIR\runtime\bin\api-ms-win-core-synch-l1-2-0.dll"
  Delete "$INSTDIR\runtime\bin\api-ms-win-core-synch-l1-1-0.dll"
  Delete "$INSTDIR\runtime\bin\api-ms-win-core-string-l1-1-0.dll"
  Delete "$INSTDIR\runtime\bin\api-ms-win-core-rtlsupport-l1-1-0.dll"
  Delete "$INSTDIR\runtime\bin\api-ms-win-core-profile-l1-1-0.dll"
  Delete "$INSTDIR\runtime\bin\api-ms-win-core-processthreads-l1-1-1.dll"
  Delete "$INSTDIR\runtime\bin\api-ms-win-core-processthreads-l1-1-0.dll"
  Delete "$INSTDIR\runtime\bin\api-ms-win-core-processenvironment-l1-1-0.dll"
  Delete "$INSTDIR\runtime\bin\api-ms-win-core-namedpipe-l1-1-0.dll"
  Delete "$INSTDIR\runtime\bin\api-ms-win-core-memory-l1-1-0.dll"
  Delete "$INSTDIR\runtime\bin\api-ms-win-core-localization-l1-2-0.dll"
  Delete "$INSTDIR\runtime\bin\api-ms-win-core-libraryloader-l1-1-0.dll"
  Delete "$INSTDIR\runtime\bin\api-ms-win-core-interlocked-l1-1-0.dll"
  Delete "$INSTDIR\runtime\bin\api-ms-win-core-heap-l1-1-0.dll"
  Delete "$INSTDIR\runtime\bin\api-ms-win-core-handle-l1-1-0.dll"
  Delete "$INSTDIR\runtime\bin\api-ms-win-core-file-l2-1-0.dll"
  Delete "$INSTDIR\runtime\bin\api-ms-win-core-file-l1-2-0.dll"
  Delete "$INSTDIR\runtime\bin\api-ms-win-core-file-l1-1-0.dll"
  Delete "$INSTDIR\runtime\bin\api-ms-win-core-errorhandling-l1-1-0.dll"
  Delete "$INSTDIR\runtime\bin\api-ms-win-core-debug-l1-1-0.dll"
  Delete "$INSTDIR\runtime\bin\api-ms-win-core-datetime-l1-1-0.dll"
  Delete "$INSTDIR\runtime\bin\api-ms-win-core-console-l1-2-0.dll"
  Delete "$INSTDIR\runtime\bin\api-ms-win-core-console-l1-1-0.dll"
  Delete "$INSTDIR\app\doc\index.html"
  Delete "$INSTDIR\app\doc\img\withdrw_usb_allow.png"
  Delete "$INSTDIR\app\doc\img\tip_device_registration.png"
  Delete "$INSTDIR\app\doc\img\allow_open_debug.png"
  Delete "$INSTDIR\app\doc\css\main.css"
  Delete "$INSTDIR\app\AndroidActivateAssistant.cfg"
  Delete "$INSTDIR\app\AdbWinUsbApi.dll"
  Delete "$INSTDIR\app\AdbWinApi.dll"
  Delete "$INSTDIR\app\adb.exe"
  Delete "$INSTDIR\app\.jpackage.xml"
  Delete "$INSTDIR\AndroidActivateAssistant.ico"
  Delete "$INSTDIR\AndroidActivateAssistant.exe"

  Delete "$SMPROGRAMS\激活助手\Uninstall.lnk"
  Delete "$DESKTOP\激活助手.lnk"
  Delete "$SMPROGRAMS\激活助手\激活助手.lnk"

  RMDir "$SMPROGRAMS\激活助手"
  RMDir "$INSTDIR\runtime\lib\security"
  RMDir "$INSTDIR\runtime\lib"
  RMDir "$INSTDIR\runtime\legal\jdk.unsupported"
  RMDir "$INSTDIR\runtime\legal\java.xml"
  RMDir "$INSTDIR\runtime\legal\java.transaction.xa"
  RMDir "$INSTDIR\runtime\legal\java.sql"
  RMDir "$INSTDIR\runtime\legal\java.security.sasl"
  RMDir "$INSTDIR\runtime\legal\java.scripting"
  RMDir "$INSTDIR\runtime\legal\java.rmi"
  RMDir "$INSTDIR\runtime\legal\java.prefs"
  RMDir "$INSTDIR\runtime\legal\java.naming"
  RMDir "$INSTDIR\runtime\legal\java.management"
  RMDir "$INSTDIR\runtime\legal\java.logging"
  RMDir "$INSTDIR\runtime\legal\java.desktop"
  RMDir "$INSTDIR\runtime\legal\java.datatransfer"
  RMDir "$INSTDIR\runtime\legal\java.compiler"
  RMDir "$INSTDIR\runtime\legal\java.base"
  RMDir "$INSTDIR\runtime\conf\security\policy\unlimited"
  RMDir "$INSTDIR\runtime\conf\security\policy\limited"
  RMDir "$INSTDIR\runtime\conf\security\policy"
  RMDir "$INSTDIR\runtime\conf\security"
  RMDir "$INSTDIR\runtime\conf"
  RMDir "$INSTDIR\runtime\bin\server"
  RMDir "$INSTDIR\runtime\bin"
  RMDir "$INSTDIR\runtime"
  RMDir "$INSTDIR\app"
  RMDir "$INSTDIR"

  DeleteRegKey ${PRODUCT_UNINST_ROOT_KEY} "${PRODUCT_UNINST_KEY}"
  DeleteRegKey HKLM "${PRODUCT_DIR_REGKEY}"
  SetAutoClose true
SectionEnd
