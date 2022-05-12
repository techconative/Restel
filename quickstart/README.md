# Quickstart

The below steps would help you get a feel of Restel by running a demo server and test it with a pre-written excel file.

You can quick start by either:
 1. [Cloning the git repository](#cloning-the-git-repository)
 2. [Downloading the required files from repo (without entire setup)](#downloading-the-required-files-from-repo-without-entire-setup)
 
You can edit [the sample sheet](https://github.com/techconative/Restel/blob/main/quickstart/jsonbox_test.xlsx) to play around with the tests being executed.

## Cloning the git repository

### Pre-requisite
- JDK 11
- Docker
- [JsonBox](https://github.com/vasanthv/jsonbox)

### Steps to follow

You can edit [the sample sheet](jsonbox_test.xlsx) to play around with the tests being executed.

#### *nix users

1. Go to the root folder of the project and run `make setup` to install the dependencies for reporting in your machine.
2. Start [Jsonbox](https://github.com/vasanthv/jsonbox) by running `docker-compose up`.
3. To execute the test, go to the home folder and run, `make demo-run`.

#### Windows users 
1. Manually install JDK11 and [allure](https://docs.qameta.io/allure/#_installing_a_commandline)
    * If you have [scoop](https://scoop.sh/) package manager, run `scoop install allure`.
2. Start [Jsonbox](https://github.com/vasanthv/jsonbox) by running `docker-compose up`
3. To execute the test, go to the home directory and run,
```
.\scripts\run.bat quickstart\jsonbox_test.xlsx
```
This invokes the **run.bat** script under *scripts* directory.

---
### Note
> If `docker-compose` is not working for JsonBox, please refer to the [instruction](https://github.com/vasanthv/jsonbox#how-to-run-locally) in jsonbox to have it up & running.

## Downloading the required files from repo (without entire setup)

### Pre-requisite
- JRE 11
- [Restel Jar](https://github.com/techconative/Restel/releases/latest)
- [Quickstart spreadsheet](https://github.com/techconative/Restel/blob/main/quickstart/jsonbox_test.xlsx)
- Docker + [JsonBox](https://github.com/vasanthv/jsonbox)
- [Allure CLI](https://docs.qameta.io/allure/#_installing_a_commandline)

### Steps to follow

1. Download the Restel JAR and quickstart Excel file into a directory.
2. Make sure Jsonbox is running.
3. Execute the JAR, passing the test sheet as argument.
4. Generate Allure report with the test result
5. View the allure report in browser
- *nix:
> ```sh
> java -jar restel-0.2-all.jar jsonbox_test.xlsx && \
> allure generate build/reports/allure-results -o build/reports/allure-report --clean && \
> allure open -p 37004 build/reports/allure-report
> ```

- Windows Powershell:
> ```powershell
> java -jar .\restel-0.2-all.jar .\jsonbox_test.xlsx && `
> allure generate .\build\reports\allure-results\ -o .\build\reports\allure-report\ --clean && `
> allure open -p 37004 .\build\reports\allure-report
> ```

### Note
> Use the appropriate file path separator based on your OS. *nix uses forward slash `/` while Windows uses backward slash `\`.