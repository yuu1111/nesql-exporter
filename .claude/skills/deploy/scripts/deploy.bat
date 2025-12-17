@echo off
setlocal

set MODS_DIR=C:\Users\yuu21\AppData\Roaming\PrismLauncher\instances\GT_New_Horizons_2.8.3_Java_17-25\.minecraft\mods
set JAR_NAME=NESQL-Exporter-0.5.2.jar
set DEPS_JAR_NAME=NESQL-Exporter-0.5.2-deps.jar

echo Building...
call gradlew.bat build --no-daemon
if %errorlevel% neq 0 (
    echo Build failed!
    exit /b 1
)

echo Deploying to %MODS_DIR%...
copy /Y "build\libs\%JAR_NAME%" "%MODS_DIR%\"
if %errorlevel% neq 0 (
    echo Deploy failed!
    exit /b 1
)

copy /Y "build\libs\%DEPS_JAR_NAME%" "%MODS_DIR%\"
if %errorlevel% neq 0 (
    echo Deploy deps jar failed!
    exit /b 1
)

echo Done!
