#!/usr/bin/env bash

echo "-------------- Running the Restel Application -------------------"
# Remove allure results directory .
rm -rf allure-results
rm -rf build/reports/allure-results
rm -rf build/reports/allure-report

RESTEL_APP_FILE=$1
BASE_URL=$2

export RESTEL_APP_FILE
export BASE_URL

if test -z "$BASE_URL"; then
  echo "baseUrl is empty, will read from excel"
else
  echo "-- Provided baseUrl= $BASE_URL  ----"
  ENABLE_URL=true
  export BASE_URL
  export ENABLE_URL
fi

java -jar restel-0.1-all.jar

## ./gradlew run

report_dir=build/reports/allure-report
result_dir=build/reports/allure-results

echo "-------------- Generate allure reports -----------------------"
allure generate $result_dir -o $report_dir --clean

echo "-------------- View the reports in browser ----------------------"
allure open -p 5050 $report_dir
