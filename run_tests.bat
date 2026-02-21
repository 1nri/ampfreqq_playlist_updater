@echo off
REM Test Runner Script for AmpFreQQ Playlist Editor (Windows)
REM Usage: run_tests.bat [all|unit|integration|suite]

setlocal enabledelayedexpansion

REM Get script directory
set SCRIPT_DIR=%~dp0
cd /d "%SCRIPT_DIR%"

REM JUnit paths
set JUNIT_JAR=junit-4.13.2.jar
set HAMCREST_JAR=hamcrest-core-1.3.jar

REM Check if JUnit exists
if not exist "%JUNIT_JAR%" (
    echo Downloading JUnit...
    powershell -Command "(New-Object Net.WebClient).DownloadFile('https://repo1.maven.org/maven2/junit/junit/4.13.2/junit-4.13.2.jar', '%JUNIT_JAR%')"
)

if not exist "%HAMCREST_JAR%" (
    echo Downloading Hamcrest...
    powershell -Command "(New-Object Net.WebClient).DownloadFile('https://repo1.maven.org/maven2/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar', '%HAMCREST_JAR%')"
)

REM Verify JARs exist
if not exist "%JUNIT_JAR%" (
    echo Error: Could not find junit-4.13.2.jar
    echo Please download manually from https://github.com/junit-team/junit4/releases
    exit /b 1
)

if not exist "%HAMCREST_JAR%" (
    echo Error: Could not find hamcrest-core-1.3.jar
    echo Please download manually from https://mvnrepository.com/artifact/org.hamcrest/hamcrest-core/1.3
    exit /b 1
)

REM Set classpath
set CLASSPATH=.;%JUNIT_JAR%;%HAMCREST_JAR%

echo.
echo ===================================================
echo AmpFreQQ Playlist Editor - Test Suite (Windows)
echo ===================================================
echo.

REM Compile tests
echo Compiling tests...
javac -cp "%CLASSPATH%" PlaylistUtility.java PlaylistUtilityTest.java PlaylistEditorIntegrationTest.java PlaylistEditorTestSuite.java
if errorlevel 1 (
    echo Failed to compile tests
    exit /b 1
)
echo Compilation successful
echo.

REM Determine test type
set TEST_TYPE=%1
if "%TEST_TYPE%"=="" set TEST_TYPE=suite

REM Run tests
if "%TEST_TYPE%"=="unit" (
    echo Running Unit Tests...
    java -cp "%CLASSPATH%" org.junit.runner.JUnitCore PlaylistUtilityTest
) else if "%TEST_TYPE%"=="integration" (
    echo Running Integration Tests...
    java -cp "%CLASSPATH%" org.junit.runner.JUnitCore PlaylistEditorIntegrationTest
) else if "%TEST_TYPE%"=="suite" (
    echo Running All Tests...
    java -cp "%CLASSPATH%" org.junit.runner.JUnitCore PlaylistEditorTestSuite
) else if "%TEST_TYPE%"=="all" (
    echo Running All Tests...
    java -cp "%CLASSPATH%" org.junit.runner.JUnitCore PlaylistEditorTestSuite
) else (
    echo Usage: run_tests.bat [all^|unit^|integration^|suite]
    exit /b 1
)

echo.
echo ===================================================
echo Test run completed
echo ===================================================

endlocal
