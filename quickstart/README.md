# About

The below steps would help you get a feel of Restel by running a demo server and test it with a pre-written excel file.

## Pre-requisite

- [Restel Jar](https://github.com/techconative/Restel/releases/latest)
- [Quickstart spreadsheet](https://github.com/techconative/Restel/blob/main/quickstart/jsonbox_test.xlsx)
- [run.sh](https://github.com/techconative/Restel/blob/main/scripts/run.sh)/[run.bat](https://github.com/techconative/Restel/blob/main/scripts/run.bat) (as per your OS)
- Docker
- [JsonBox](https://github.com/vasanthv/jsonbox)

## Quickstart

You can edit [the sample sheet](jsonbox_test.xlsx) to play around with the tests being executed.

### *nix users

1. Go to the root folder of the project and run `make setup` to install the dependencies for reporting in your machine.
2. Start [Jsonbox](https://github.com/vasanthv/jsonbox) by running `docker-compose up`.
3. To execute the test, go to the home folder and run, `make demo-run`.

### Windows users 
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
