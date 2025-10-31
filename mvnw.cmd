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
@REM Maven Start Up Batch script
@REM
@REM Required ENV vars:
@REM JAVA_HOME - location of JDK home dir
@REM
@REM Optional ENV vars
@REM M2_HOME - location of maven2's installed home dir
@REM MAVEN_BATCH_ECHO - set to 'on' to enable the echoing of the batch commands
@REM MAVEN_BATCH_PAUSE - set to 'on' to wait for a keystroke before ending
@REM MAVEN_OPTS - parameters passed to the Java VM when running Maven
@REM     e.g. to debug Maven itself, use
@REM set MAVEN_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000
@REM MAVEN_SKIP_RC - flag to disable loading of mavenrc files
@REM ----------------------------------------------------------------------------

@ECHO OFF
SETLOCAL

IF NOT "%MAVEN_BATCH_ECHO%"==""  ECHO %MAVEN_BATCH_ECHO%

@REM set %HOME% to equivalent of $HOME
IF "%HOME%"=="" (SET "HOME=%HOMEDRIVE%%HOMEPATH%")

@REM Execute a user defined script before this one
IF NOT "%MAVEN_SKIP_RC%"=="" GOTO skipRcPre
  @REM check for pre script, once with legacy .bat ending and once with .cmd ending
  IF EXIST "%USERPROFILE%\mavenrc_pre.bat" CALL "%USERPROFILE%\mavenrc_pre.bat"
  IF EXIST "%USERPROFILE%\mavenrc_pre.cmd" CALL "%USERPROFILE%\mavenrc_pre.cmd"
  IF EXIST "%HOME%\mavenrc_pre.bat" CALL "%HOME%\mavenrc_pre.bat"
  IF EXIST "%HOME%\mavenrc_pre.cmd" CALL "%HOME%\mavenrc_pre.cmd"
  IF EXIST "%MAVEN_BATCH_PREF%\mavenrc_pre.bat" CALL "%MAVEN_BATCH_PREF%\mavenrc_pre.bat"
  IF EXIST "%MAVEN_BATCH_PREF%\mavenrc_pre.cmd" CALL "%MAVEN_BATCH_PREF%\mavenrc_pre.cmd"
:skipRcPre

SET MAVEN_CMD_LINE_ARGS=%*

@REM Find the project base dir, i.e. the directory that contains the folder ".mvn".
@REM Fallback to current working directory if not found.

SET MAVEN_PROJECTBASEDIR=%MAVEN_BASEDIR%
IF NOT "%MAVEN_PROJECTBASEDIR%"=="" GOTO endDetectBaseDir

SET EXEC_DIR=%CD%
SET WDIR=%EXEC_DIR%
:findBaseDir
IF EXIST "%WDIR%"\.mvn GOTO baseDirFound
cd ..
IF "%WDIR%"=="%CD%" GOTO baseDirNotFound
SET WDIR=%CD%
GOTO findBaseDir

:baseDirFound
SET MAVEN_PROJECTBASEDIR=%WDIR%
cd "%EXEC_DIR%"
GOTO endDetectBaseDir

:baseDirNotFound
SET MAVEN_PROJECTBASEDIR=%EXEC_DIR%
cd "%EXEC_DIR%"

:endDetectBaseDir

IF NOT EXIST "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar" (
  CALL :findMavenWrapperJar
)

SET WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
SET WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

set WRAPPER_JAVA_EXE="%JAVA_HOME%\bin\java.exe"
%WRAPPER_JAVA_EXE% %MAVEN_OPTS% %MAVEN_DEBUG_OPTS% -classpath %WRAPPER_JAR% "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" %WRAPPER_LAUNCHER% %MAVEN_CMD_LINE_ARGS%
IF ERRORLEVEL 1 goto error
goto end

:findMavenWrapperJar
SET DOWNLOAD_URL=

SET WRAPPER_JAR_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"
IF NOT "%MVNW_REPOURL%"=="" SET WRAPPER_JAR_URL="%MVNW_REPOURL%/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"

FOR /F "tokens=*" %%i IN ('"%JAVA_HOME%\bin\java.exe" -cp "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar" org.apache.maven.wrapper.Downloader %WRAPPER_JAR_URL% .mvn/wrapper/maven-wrapper.jar') DO SET DOWNLOAD_URL=%%i

IF EXIST "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar" GOTO okDownload
ECHO Error: Could not find Maven Wrapper jar in '%MAVEN_PROJECTBASEDIR%\.mvn\wrapper'.
EXIT /B 1

:okDownload
EXIT /B 0

:error
set ERROR_CODE=%ERRORLEVEL%

IF NOT "%MAVEN_SKIP_RC%"=="" GOTO skipRcPost
  @REM check for post script, once with legacy .bat ending and once with .cmd ending
  IF EXIST "%USERPROFILE%\mavenrc_post.bat" CALL "%USERPROFILE%\mavenrc_post.bat"
  IF EXIST "%USERPROFILE%\mavenrc_post.cmd" CALL "%USERPROFILE%\mavenrc_post.cmd"
  IF EXIST "%HOME%\mavenrc_post.bat" CALL "%HOME%\mavenrc_post.bat"
  IF EXIST "%HOME%\mavenrc_post.cmd" CALL "%HOME%\mavenrc_post.cmd"
  IF EXIST "%MAVEN_BATCH_PREF%\mavenrc_post.bat" CALL "%MAVEN_BATCH_PREF%\mavenrc_post.bat"
  IF EXIST "%MAVEN_BATCH_PREF%\mavenrc_post.cmd" CALL "%MAVEN_BATCH_PREF%\mavenrc_post.cmd"
:skipRcPost

@REM pause the script if MAVEN_BATCH_PAUSE is set to 'on'
IF "%MAVEN_BATCH_PAUSE%"=="on" PAUSE

EXIT /B %ERROR_CODE%

:end
IF NOT "%MAVEN_SKIP_RC%"=="" GOTO skipRcPost2
  @REM check for post script, once with legacy .bat ending and once with .cmd ending
  IF EXIST "%USERPROFILE%\mavenrc_post.bat" CALL "%USERPROFILE%\mavenrc_post.bat"
  IF EXIST "%USERPROFILE%\mavenrc_post.cmd" CALL "%USERPROFILE%\mavenrc_post.cmd"
  IF EXIST "%HOME%\mavenrc_post.bat" CALL "%HOME%\mavenrc_post.bat"
  IF EXIST "%HOME%\mavenrc_post.cmd" CALL "%HOME%\mavenrc_post.cmd"
  IF EXIST "%MAVEN_BATCH_PREF%\mavenrc_post.bat" CALL "%MAVEN_BATCH_PREF%\mavenrc_post.bat"
  IF EXIST "%MAVEN_BATCH_PREF%\mavenrc_post.cmd" CALL "%MAVEN_BATCH_PREF%\mavenrc_post.cmd"
:skipRcPost2

ENDLOCAL
EXIT /B 0
