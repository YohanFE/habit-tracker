@echo off
echo Running JavaFX application...

REM Set paths (modify these if necessary)
set JAVAFX_PATH=C:\Program Files\Java\javafx-sdk-21\lib
set JAVA_PATH=C:\Program Files\Java\jdk-21\bin\java.exe

REM Verify JavaFX path exists
if not exist "%JAVAFX_PATH%" (
    echo ERROR: JavaFX SDK not found at %JAVAFX_PATH%
    echo Please install JavaFX SDK or update the path in this script.
    pause
    exit /b 1
)

REM Run the application
"%JAVA_PATH%" --module-path "%JAVAFX_PATH%" --add-modules javafx.controls,javafx.fxml -cp .;bin Main

echo.
if %ERRORLEVEL% NEQ 0 (
    echo Application exited with error code %ERRORLEVEL%
) else (
    echo Application completed successfully.
)

pause