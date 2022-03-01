#!/usr/bin/env bash
echo "------------- Setting up the Restel Application -----------------"
sudo apt-add-repository ppa:qameta/allure
sudo apt-get update -y
sudo apt-get install -y allure

# Configure restel application.
./gradlew clean build

echo "------------- Setting up successful -----------------"