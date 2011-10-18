SET TUMBLER_SRC_DIR=%1%
SET TUMBLER_BIN_DIR=%2%

gcc -O2 -Wall -g %TUMBLER_SRC_DIR%\TumblerLauncher\bbwp.c -o %TUMBLER_BIN_DIR%\bbwp

