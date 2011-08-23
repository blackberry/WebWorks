@echo off

if not defined TUMBLER_INSTALLER_DIR set TUMBLER_INSTALLER_DIR="..\installer"

echo "Checking existence of deliverables in %TUMBLER_INSTALLER_DIR%\INSTALL_RELEASE\Web Component Pack\DiskImages\DISK1":

echo "Checking bbwp.zip..."
if exist "%TUMBLER_INSTALLER_DIR%\INSTALL_RELEASE\Web Component Pack\DiskImages\DISK1\bbwp.zip" goto CheckMSI
echo "bbwp.zip not found!"
goto Fail

:CheckMSI
echo "Checking BlackBerry Widget SDK.msi..."
if exist "%TUMBLER_INSTALLER_DIR%\INSTALL_RELEASE\Web Component Pack\DiskImages\DISK1\BlackBerry Widget SDK.msi" goto CheckEXE
echo "BlackBerry Widget SDK.msi not found!"
goto Fail

:CheckEXE
echo "Checking setup.exe..."
if exist "%TUMBLER_INSTALLER_DIR%\INSTALL_RELEASE\Web Component Pack\DiskImages\DISK1\setup.exe" goto Success
echo "setup.exe not found!"
goto Fail

:Success
echo "All deliverables found."
goto End

:Fail
echo "BUILD FAILED"

:End