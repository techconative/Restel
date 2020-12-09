# Restel
## About:

This Java project is a data driven test automation framework, which is capable of reading the Rest Service APIs from the excel sheet and runs it though TestNG test-library and generate allure reports.

## Pre-requisite:

- Java-11
- Gradle

## Configuring excel

Restel Excel configuration is  https://gitlab.pramati.com/restel/restel/-/wikis/Restel-Excel-Sheet[here] .

## Setup :

Run command : `$ sh scripts/build.sh`

This script will install allure-commandline tool locally for viewing reports.

## Run:

### To Run from an IDE :

In the src/main/resources/application.properties, change the property value of 'app.excelFile' to the excel file you wants to run or define an environmental variable *RESTEL_APP_FILE* to an excel file path.
Once the excel file path is configured, run the main class `RestelApplication` to run the tests from excel.

### To Run from scripts :

*Inputs* : Restel excel file

*Command to run* :

`$ sh scripts/run.sh  <excel file path>.`

This script will install allure-commandline to view the reports in browser and give your restel excel file as input

eg:  `$ sh scripts/run.sh  Sample_Suite_definition.xlsx`

## Swagger to Excel Conversion :

The Restel application also support conversion of swagger APIs to restel's excel sheet with Test Definitions, with which we can define test suites,Test suite executions and pass this excel to Restel application for testing the APIs.
(Note: The converted excel will only generate TestDefinitions with schema structure of request and response body.)

### To Run from scripts :

*Inputs* :

1. swagger json file (supports both OAS 2 and OAS 3 ).

2. File path to where the generated excel should be stored (Optional).

*Command to run* :

`$ sh scripts/swagger2excel.sh <swagger file> <excel storage path>`

eg:  `$ sh scripts/swagger2excel.sh  petstore.json restel_petstore.xlsx`

## Tools Used:

- Gradle
- TestNG library
- Allure