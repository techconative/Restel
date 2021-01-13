#!/usr/bin/env bash

echo "-------------- Running the Restel Application -------------------"
# Remove allure results directory .
rm -rf allure-results
rm -rf build/reports/allure-results
rm -rf build/reports/allure-report

while getopts ":b:h:f:" opt; do
  case $opt in
  b) BASE_URL=$OPTARG ;;
  h) AUTH_HEADER=$OPTARG ;;
  f) RESTEL_APP_FILE=$OPTARG ;;
  esac
done

if test -z "$BASE_URL"; then
  echo "baseUrl is empty, will read from excel"
else
  echo "-- Provided baseUrl= $BASE_URL  ----"
  ENABLE_URL=true
  export ENABLE_URL
  export BASE_URL
fi

if test -z "$AUTH_HEADER"; then
  echo ""
else
  echo "-- Provided Authorization header = $AUTH_HEADER  ----"
  export AUTH_HEADER
fi

if test -z "$RESTEL_APP_FILE"; then
  echo "Error input file with -f not provide "
  exit 1
else
  echo "-- Provided input file  = $RESTEL_APP_FILE  ----"
  export RESTEL_APP_FILE
fi

##java -jar build/libs/restel-0.1-all.jar

./gradlew run

report_dir=build/reports/allure-report
result_dir=build/reports/allure-results

echo "-------------- Generate allure reports -----------------------"
allure generate $result_dir -o $report_dir --clean

echo "-------------- View the reports in browser ----------------------"
allure open -p 5050 $report_dir
