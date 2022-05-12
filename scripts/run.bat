@echo off
echo -------------- Running the Restel Application -------------------
REM Remove allure results directory
rmdir /S /Q allure-results 2> NUL
rmdir /S /Q build\reports\allure-results 2> NUL
rmdir /S /Q build\reports\allure-report 2> NUL

REM java -jar build/libs/restel-0.1-all.jar

CALL gradlew.bat run --args="%1"

set report_dir=build\reports\allure-report
set result_dir=build\reports\allure-results

echo -------------- Generate allure reports --------------------------
CALL allure generate %result_dir% -o %report_dir% --clean

echo -------------- View the reports in browser ----------------------
CALL allure open -p 37004 %report_dir%