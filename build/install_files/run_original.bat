@ECHO OFF

set BBWPTEMP=%JAVA_HOME%\bin\java.exe

///  LINE REPLACED BY INSTALLSHIELD - SET BBWPTEMP2=JAVA_DIR_FOUND_BY_INSTALLER

IF NOT EXIST "%BBWPTEMP%" SET JAVA_HOME=%BBWPTEMP2%

set PATH=%PATH%;%JAVA_HOME%\bin

call run_original.bat

:END
