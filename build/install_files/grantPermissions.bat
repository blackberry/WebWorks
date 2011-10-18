@ECHO OFF

cacls conf/mdsweb/mds.war /e /G Everyone:F

cacls conf/mdsweb/mdssecurid.war /e /G Everyone:F

:END
