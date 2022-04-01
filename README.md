# Restel

## About:

Restel is a data driven test automation framework, which is capable of reading the Rest Service APIs from the excel sheet and runs it though TestNG test-library and generate allure reports.

## Features:

- Execute test cases defined in excel.
- Have the template excel created from OpenAPI docs.
- Present the test results as Allure reports.

## Pre-requisite:

## For Mac/Linux:

- Java-11
- Make file support

## For systems where the given Makefile doesn't work:

- Java-11
- [Allure](https://docs.qameta.io/allure/#_installing_a_commandline)

## Configuring excel

Sample configuration sheet is available [here](quickstart/jsonbox_test.xlsx).

## Setup :

Run the command, 

```
make setup
```

This script will install allure-commandline tool locally for viewing reports.

## Run:

### To Run from an IDE :

In the `src/main/resources/application.properties`, change the property value of `app.excelFile` to the excel file you wants to run or define an environmental variable *RESTEL_APP_FILE* to an excel file path.
Once the Excel file path is configured, run the main class `RestelApplication` to run the tests from excel.

### To Run from scripts :

*Inputs* : Restel excel file

*Command to run* :

`sh scripts/run.sh  <excel file path>.`

This script will install allure-commandline to view the reports in browser and give your restel excel file as input

eg:  `sh scripts/run.sh  Sample_Suite_definition.xlsx`

## Demo Run:

To run the demo file, please have a look into the documentation in [quickstart](./quickstart).

## Docs:

Docs on understanding the features, usage and concepts can be found in the [wiki](https://github.com/techconative/Restel/wiki).

## OpenAPI to Excel Conversion :

The Restel application also support conversion of Open API spec to restel's excel sheet with Test Definitions,
with which we can define test suites,Test suite executions and pass this excel to Restel application for testing the APIs.

> Note: The converted Excel will only generate TestDefinitions with schema structure of request and response body.

### To Run from scripts :

*Inputs* :

1. OpenAPI json file (supports both OAS 2 and OAS 3 ).

2. File path to where the generated excel should be stored (Optional).

*Command to run* :

`sh scripts/oas2excel.sh <Open API definition file> <excel storage path>`

eg:  `sh scripts/oas2excel.sh  petstore.json restel_petstore.xlsx`
