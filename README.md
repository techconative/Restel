# Restel

## About

Restel is a data-driven, no(low) code test automation framework, which is capable of reading the Rest Service APIs from the spreadsheet and runs it though TestNG test-library and generate allure reports.

## Rationale

When it comes to API testing, the typical phase goes like this,

1. Understand the application and its context
2. Come up with test case scenarios(tyically in spreadsheet)
3. Convert the test scenarios to code
4. Repeat

What if we could tweak step#2 and remove the step#3 altogether? That's where Restel could help.

The goal of Restel is to simplify API testing without having to write code and have the APIs automated.

## Features

- Execute test cases defined in Excel.
- Have the basic test Excel created from OpenAPI docs.
- Middleware support to do Oauth(Client-credential and password) and Basic auth login during test case execution.
- Present the test results as Allure reports.

## Pre-requisite

### For Mac/Linux

- Java-11
- Make file support

### For systems where the given Makefile doesn't work

Please install the following dependencies manually

- Java-11
- [Allure](https://docs.qameta.io/allure/#_installing_a_commandline)

Windows users can install allure using [scoop](https://scoop.sh/):
```
scoop install allure
```

## Configuring excel

Sample configuration sheet is available [here](quickstart/jsonbox_test.xlsx).

## Setup

```
make setup
```

If the above command doesn't works, you will have to install the aforementioned pre-requisites manually.


## Demo

Please make use of our [quickstart guide](./quickstart) to play around and get a taste of Restel.

## Run

### To Run from an IDE

In the `src/main/resources/application.properties`, change the property value of `app.excelFile` to the excel file you wants to run or define an environmental variable *RESTEL_APP_FILE* to an excel file path.
Once the Excel file path is configured, run the main class `RestelApplication` to run the tests from excel.

### To Run from scripts

*Inputs* : Restel excel file

*Command to run* :

*nix:

`sh scripts/run.sh  <excel file path>`

Windows:

`.\scripts\run.bat <excel file path>`

This script when given your restel excel file as input will run the tests and use `allure` CLI tool to generate and serve the reports via http. e.g.,:

*nix: `sh scripts/run.sh  quickstart/jsonbox_test.xlsx`

Windows: `.\scripts\run.bat quickstart\jsonbox_test.xlsx`


## Docs

Documentation to help understand the features, usage and concepts can be found at [Retel Docs site.](https://restel.techconative.info/docs/intro)

## OpenAPI to Excel Conversion

The Restel application also support conversion of Open API spec to restel's excel sheet with Test API Definitions,
with which we can define test suites,Test suite executions and pass this excel to Restel application for testing the APIs.

> Note: The converted Excel will only generate TestApiDefinitions with schema structure of request and response body.

### To Run from scripts

*Inputs* :

1. OpenAPI json file (supports both OAS 2 and OAS 3 ).

2. File path to where the generated excel should be stored (Optional).

*Command to run* :

`sh scripts/oas2excel.sh <Open API definition file> <excel storage path>`

eg:  `sh scripts/oas2excel.sh  petstore.json restel_petstore.xlsx`
