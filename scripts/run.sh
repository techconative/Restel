#!/usr/bin/env bash

echo "-------------- Running the Restel Application -------------------"
# Remove allure results directory .
rm -rf allure-results
rm -rf build/reports/allure-results
rm -rf build/reports/allure-report

RESTEL_APP_FILE=$1
export RESTEL_APP_FILE

#java -jar build/libs/restel-0.1-all.jar

./gradlew run

report_dir=build/reports/allure-report
result_dir=build/reports/allure-results

echo "-------------- Generate allure reports -----------------------"
allure generate $result_dir  -o $report_dir --clean

echo "-------------- View the reports in browser ----------------------"
allure open $report_dir