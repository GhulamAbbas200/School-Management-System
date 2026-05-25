@echo off
echo ===================================================
echo School Management System - Packaging Script
echo ===================================================

:: Step 1: Create dist folder
if not exist dist mkdir dist

:: Step 2: Compile all java files
echo Compiling Java source files...
javac -cp ".;mssql-jdbc-12.6.1.jre11.jar" *.java
if %errorlevel% neq 0 (
    echo [ERROR] Compilation failed. Please check your JDK installation.
    pause
    exit /b %errorlevel%
)

:: Step 3: Create Manifest file
echo Creating Manifest file...
(
echo Manifest-Version: 1.0
echo Main-Class: SchoolManagementSystem
echo Class-Path: mssql-jdbc-12.6.1.jre11.jar
echo.
) > manifest.txt

:: Step 4: Package into JAR
echo Packaging classes into JAR...
jar cfm dist\SchoolManagementSystem.jar manifest.txt *.class
if %errorlevel% neq 0 (
    echo [ERROR] JAR packaging failed.
    pause
    exit /b %errorlevel%
)

:: Step 5: Copy dependencies and config files
echo Copying dependencies and config files...
copy mssql-jdbc-12.6.1.jre11.jar dist\ > nul
if exist db.properties (
    copy db.properties dist\ > nul
) else (
    echo #Database Configuration for School Management System > dist\db.properties
    echo db.host=localhost >> dist\db.properties
    echo db.port=1433 >> dist\db.properties
    echo db.user=sa >> dist\db.properties
    echo db.password=MyPassword123 >> dist\db.properties
    echo db.integratedSecurity=false >> dist\db.properties
)

:: Step 6: Create run.bat inside dist folder
echo Creating run.bat inside dist...
(
echo @echo off
echo title School Management System
echo java -jar SchoolManagementSystem.jar
echo pause
) > dist\run.bat

:: Clean up local temporary manifest.txt
del manifest.txt

echo ===================================================
echo Packaging Completed Successfully!
echo The deployable application is located in the 'dist' folder.
echo You can copy the entire 'dist' folder to any machine to deploy it.
echo ===================================================
pause
