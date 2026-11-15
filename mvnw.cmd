@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    https://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper startup batch script, version 3.3.2
@REM
@REM Required ENV vars:
@REM   JAVA_HOME - location of a JDK home dir
@REM
@REM Optional ENV vars
@REM   MAVEN_BATCH_ECHO - set to 'on' to enable the echoing of the batch commands
@REM   MAVEN_BATCH_PAUSE - set to 'on' to wait for a keystroke before ending
@REM   MAVEN_OPTS - parameters passed to the Java VM when running Maven
@REM     e.g. set MAVEN_OPTS=-Xms256m -Xmx512m
@REM ----------------------------------------------------------------------------

@IF "%MAVEN_BATCH_ECHO%"=="on" echo %MAVEN_BATCH_ECHO%

@REM Set JAVA_HOME if not already set, by trying to detect from registry
@IF NOT "%JAVA_HOME%"=="" GOTO OkJHome
@IF "%JDK_HOME%"=="" GOTO TryJRE
SET JAVA_HOME=%JDK_HOME%
GOTO OkJHome
:TryJRE
@IF "%JRE_HOME%"=="" GOTO NoJHome
SET JAVA_HOME=%JRE_HOME%
GOTO OkJHome
:NoJHome
ECHO.
ECHO ERROR: JAVA_HOME not found in your environment. >&2
ECHO Please set the JAVA_HOME environment variable to the location of your JDK. >&2
ECHO.
EXIT /B 1

:OkJHome
SET JAVA_EXE=%JAVA_HOME%\bin\java.exe
IF EXIST "%JAVA_EXE%" GOTO init
ECHO.
ECHO ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME% >&2
ECHO.
EXIT /B 1

:init
SET MAVEN_PROJECTBASEDIR=%~dp0
IF "%MAVEN_PROJECTBASEDIR:~-1%"=="\" SET MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%

SET WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain
SET WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
SET WRAPPER_PROPERTIES="%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties"

SET DOWNLOAD_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar

@IF EXIST %WRAPPER_JAR% GOTO ExecuteWrapper
ECHO Downloading Apache Maven Wrapper from %DOWNLOAD_URL%...
"%JAVA_EXE%" -classpath "%JAVA_HOME%\lib\tools.jar" org.apache.maven.wrapper.MavenWrapperDownloader "%DOWNLOAD_URL%" "%WRAPPER_JAR%"
@IF NOT EXIST %WRAPPER_JAR% (
    ECHO Fallback: downloading with PowerShell...
    powershell -Command "Invoke-WebRequest -Uri '%DOWNLOAD_URL%' -OutFile %WRAPPER_JAR%"
)

:ExecuteWrapper
@IF NOT EXIST %WRAPPER_JAR% (
    ECHO.
    ECHO ERROR: Cannot find maven-wrapper.jar. >&2
    ECHO Ensure internet access is available for first-time setup. >&2
    EXIT /B 1
)

"%JAVA_EXE%" %MAVEN_OPTS% ^
  -classpath %WRAPPER_JAR% ^
  "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" ^
  %WRAPPER_LAUNCHER% %*

IF ERRORLEVEL 1 EXIT /B 1

@IF "%MAVEN_BATCH_PAUSE%"=="on" PAUSE
