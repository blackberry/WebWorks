set BUNDLE_NUMBER=000

cd C:\p4\dev\webworks\api-builds\extensions
call ant -f buildMain.xml init.api.build build.apiExtensions package.apiExtensions

cd C:\p4\dev\webworks\api-builds\docs
call ant -f buildDocs.xml package

cd C:\p4\dev\webworks\build
call ant dependencies init buildTumblerJar buildLauncher buildFledgeDaemon buildDocs findbugs buildInstallerDependancies buildInstaller buildEclipsePlugin -Dtumbler.installer.dir=C:\p4\dev\webworks\build\installer

pause