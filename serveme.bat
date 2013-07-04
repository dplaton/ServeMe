@echo off

REM *************************************************************************
REM *				ServeMe - the simple webserver							*
REM *************************************************************************

if "" == "%JAVA_HOME%" goto noJavaHome
if not exist %JAVA_HOME%/bin/java.exe goto noJavaExe

%JAVA_HOME%\bin\java.exe -cp .\lib\log4j-1.2.17.jar;.\bin\ServeMe.jar;.\bin ro.dp.serveme.ServeMe %1 %2
goto end
:noJavaHome
echo "The JAVA_HOME environment variable is not set. Please set it so it points to a valid JRE / JDK (i.e. 1.5 or greater)
exit /b

:noJavaExe
echo "The Java executable (java.exe) cannot be located. JAVA_HOME (%JAVA_HOME%) is set but probably points to a bad location. "

:end
