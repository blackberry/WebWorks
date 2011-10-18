@echo off

set PRODUCT_VERSION=20110823
set BUNDLE_NUMBER=%PRODUCT_VERSION%

call mvn clean install

pause