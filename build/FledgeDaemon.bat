@ECHO OFF

SET TUMBLER_SRC_DIR=%1%
SET TUMBLER_BIN_DIR=%2%

call %3%

rem compile FledgeDaemon.cpp
 
REM FledgeHook.exe
cl /O2 /Oi /GL /D "WIN32" /D "NDEBUG" /D "_WINDOWS" /FD /EHsc /Gy /W4 /TP "%TUMBLER_SRC_DIR%\FledgeDaemon\main.cpp" /link /OUT:"%TUMBLER_BIN_DIR%\FledgeHook.exe" /INCREMENTAL:NO /OPT:REF /OPT:ICF /LTCG /DYNAMICBASE /NXCOMPAT /MACHINE:X86 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib oleacc.lib psapi.lib 

REM FledgeHook.dll
cl /O2 /Oi /GL /D "WIN32" /D "NDEBUG" /D "_WINDOWS" /FD /EHsc /Gy /W4 /TP /LD "%TUMBLER_SRC_DIR%\FledgeDaemon\dllmain.cpp" /link /OUT:"%TUMBLER_BIN_DIR%\FledgeHook.dll" /INCREMENTAL:NO /OPT:REF /OPT:ICF /LTCG /DYNAMICBASE /NXCOMPAT /MACHINE:X86 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib oleacc.lib psapi.lib 
