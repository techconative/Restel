# About

The below steps would help you get a feel of Restel by running a demo server and test it with a pre-written excel file.

## Pre-requisite

- Docker
- [JsonBox](https://github.com/vasanthv/jsonbox)

> Jsonbox, if started with docker compose, will run mongo. If facing issues, run mongo manually and start Jsonbox with `npm start`

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
