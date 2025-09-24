@echo off
REM Simple Database Deployment Script for Windows
REM Deploys the End-to-End Testing Platform database

setlocal enabledelayedexpansion

REM Set colors
for /f %%a in ('echo prompt $E ^| cmd') do set "ESC=%%a"
set "GREEN=%ESC%[32m"
set "YELLOW=%ESC%[33m"
set "RED=%ESC%[31m"
set "NC=%ESC%[0m"

REM Default configuration
set "DB_NAME=testplatform"
set "DB_USER=postgres"
set "DB_PASSWORD=root"
set "DB_HOST=localhost"
set "DB_PORT=5432"

echo.
echo ========================================
echo   End-to-End Testing Platform Database
echo   Simple Deployment Script
echo ========================================
echo.

REM Check if PostgreSQL is installed
echo [INFO] Checking PostgreSQL installation...
psql --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] PostgreSQL is not installed or not in PATH
    echo Please install PostgreSQL and add it to your PATH
    pause
    exit /b 1
)
echo [SUCCESS] PostgreSQL is installed

REM Set password environment variable
set "PGPASSWORD=%DB_PASSWORD%"

REM Check if database exists
echo [INFO] Checking if database exists...
psql -h %DB_HOST% -p %DB_PORT% -U %DB_USER% -d %DB_NAME% -c "SELECT 1;" >nul 2>&1
if %errorlevel% equ 0 (
    echo [INFO] Database %DB_NAME% already exists
    set "DB_EXISTS=1"
) else (
    echo [INFO] Database %DB_NAME% does not exist, will create it
    set "DB_EXISTS=0"
)

REM Create database if it doesn't exist
if %DB_EXISTS% equ 0 (
    echo [INFO] Creating database %DB_NAME%...
    psql -h %DB_HOST% -p %DB_PORT% -U %DB_USER% -c "CREATE DATABASE %DB_NAME%;"
    if %errorlevel% neq 0 (
        echo [ERROR] Failed to create database
        pause
        exit /b 1
    )
    echo [SUCCESS] Database created successfully
)

REM Execute initialization script
echo [INFO] Executing database initialization script...
psql -h %DB_HOST% -p %DB_PORT% -U %DB_USER% -d %DB_NAME% -f init-simple.sql
if %errorlevel% neq 0 (
    echo [ERROR] Database initialization failed
    pause
    exit /b 1
)
echo [SUCCESS] Database initialized successfully

REM Ask if user wants to import sample data
echo.
set /p "import_sample=Do you want to import sample data? (y/n): "
if /i "!import_sample!"=="y" (
    echo [INFO] Importing sample data...
    psql -h %DB_HOST% -p %DB_PORT% -U %DB_USER% -d %DB_NAME% -f sample-data-simple.sql
    if %errorlevel% neq 0 (
        echo [WARNING] Sample data import had some issues, but continuing...
    ) else (
        echo [SUCCESS] Sample data imported successfully
    )
)

REM Verify database
echo.
echo [INFO] Verifying database structure...
psql -h %DB_HOST% -p %DB_PORT% -U %DB_USER% -d %DB_NAME% -c "SELECT 'Tables' as type, COUNT(*) as count FROM information_schema.tables WHERE table_schema = 'public' UNION ALL SELECT 'Indexes', COUNT(*) FROM pg_indexes WHERE schemaname = 'public' UNION ALL SELECT 'Functions', COUNT(*) FROM information_schema.routines WHERE routine_schema = 'public';"

echo.
echo ========================================
echo   Database Deployment Completed!
echo ========================================
echo.
echo Database Information:
echo   Name: %DB_NAME%
echo   Host: %DB_HOST%:%DB_PORT%
echo   User: %DB_USER%
echo.
echo Next Steps:
echo   1. Update your Spring Boot application.yml
echo   2. Start your backend application
echo   3. Test the database connection
echo.
pause
