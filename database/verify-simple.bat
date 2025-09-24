@echo off
REM Simple Database Verification Script for Windows
REM Verifies the End-to-End Testing Platform database

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
echo   Database Verification
echo ========================================
echo.

REM Set password environment variable
set "PGPASSWORD=%DB_PASSWORD%"

REM Test connection
echo [INFO] Testing database connection...
psql -h %DB_HOST% -p %DB_PORT% -U %DB_USER% -d %DB_NAME% -c "SELECT 1;" >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Cannot connect to database
    echo Please check your database configuration
    pause
    exit /b 1
)
echo [SUCCESS] Database connection successful

REM Check tables
echo.
echo [INFO] Checking database tables...
psql -h %DB_HOST% -p %DB_PORT% -U %DB_USER% -d %DB_NAME% -c "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' ORDER BY table_name;"

REM Check data counts
echo.
echo [INFO] Checking data counts...
psql -h %DB_HOST% -p %DB_PORT% -U %DB_USER% -d %DB_NAME% -c "SELECT 'Users' as table_name, COUNT(*) as count FROM users UNION ALL SELECT 'Test Environments', COUNT(*) FROM test_environments UNION ALL SELECT 'Global Variables', COUNT(*) FROM global_variables UNION ALL SELECT 'Test Suites', COUNT(*) FROM test_suites UNION ALL SELECT 'Test Cases', COUNT(*) FROM test_cases;"

REM Check indexes
echo.
echo [INFO] Checking indexes...
psql -h %DB_HOST% -p %DB_PORT% -U %DB_USER% -d %DB_NAME% -c "SELECT COUNT(*) as index_count FROM pg_indexes WHERE schemaname = 'public';"

REM Check functions
echo.
echo [INFO] Checking functions...
psql -h %DB_HOST% -p %DB_PORT% -U %DB_USER% -d %DB_NAME% -c "SELECT COUNT(*) as function_count FROM information_schema.routines WHERE routine_schema = 'public';"

echo.
echo ========================================
echo   Verification Complete!
echo ========================================
echo.
pause
