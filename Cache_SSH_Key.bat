@ECHO OFF

SET /P HOSTNAME=Enter the hostname: 
IF "%HOSTNAME%"=="" GOTO HOSTNAMEERROR
ECHO.
ECHO You are logging onto %HOSTNAME%


plink.exe -load aixterm %HOSTNAME%




GOTO END
:HOSTNAMEERROR
ECHO.
ECHO.
ECHO HOSTNAME REQUIRED
ECHO.
ECHO.


:END
echo.
echo.
pause
