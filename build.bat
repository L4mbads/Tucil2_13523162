@echo off
setlocal

set SRC_DIR=src\com\fachriza\imagequadtree
set BIN_DIR=bin
set JAR_NAME=ImageCompressor.jar
set MAIN_CLASS=com.fachriza.imagequadtree.ImageCompressor
set MANIFEST=manifest/MANIFEST.MF

if not exist %BIN_DIR% mkdir %BIN_DIR%

dir /s /b %SRC_DIR%\*.java > sources.txt
javac -d %BIN_DIR% -cp "lib/AnimatedGIFWriter.jar" -sourcepath %SRC_DIR% @sources.txt

if %ERRORLEVEL% neq 0 (
    echo Compilation failed!
    exit /b %ERRORLEVEL%
)

jar cfm %BIN_DIR%\%JAR_NAME% %MANIFEST% -C %BIN_DIR% .
if %ERRORLEVEL% neq 0 (
    echo Failed to create JAR!
    exit /b %ERRORLEVEL%
)

del sources.txt
echo Compilation and packaging complete. Run with:
echo java -jar %BIN_DIR%\%JAR_NAME%

endlocal