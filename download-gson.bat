@echo off
echo Downloading GSON library for JSON support...

if not exist "lib" mkdir lib

echo Downloading gson-2.10.1.jar...
curl -L -o lib/gson-2.10.1.jar https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar

if exist "lib/gson-2.10.1.jar" (
    echo ✅ GSON library downloaded successfully!
    echo.
    echo To compile with GSON support:
    echo javac -cp ".;lib/gson-2.10.1.jar" *.java
    echo.
    echo To run with GSON support:
    echo java -cp ".;lib/gson-2.10.1.jar" TetrisGame
) else (
    echo ❌ Failed to download GSON library
    echo Please download manually from: https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar
    echo Place it in lib/gson-2.10.1.jar
)

pause