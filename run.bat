@echo off
setlocal EnableDelayedExpansion
cd /d "%~dp0"
if not exist build\classes mkdir build\classes
(for /r src %%F in (*.java) do (
    set "SOURCE=%%F"
    echo "!SOURCE:\=/!"
)) > build\sources.txt
javac --release 17 -encoding UTF-8 -d build\classes @build\sources.txt
if errorlevel 1 exit /b 2
java -cp build\classes ru.mirea.graph.Main %*
