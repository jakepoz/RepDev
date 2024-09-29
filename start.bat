@echo off

set JPATH=
"%JPATH%java.exe" -version

echo.

"%JPATH%java.exe" -cp swt-win/swt.jar;swtcompare.jar;./ -Djava.library.path=swt-win -Dfile.encoding=Cp1252 com.repdev.RepDevMain

if NOT %ERRORLEVEL% == 0 pause