FROM adoptopenjdk/openjdk11:jdk11u-ubuntu-nightly-slim

RUN apt update

RUN echo "--------------- Installing Allure --------------------------"
RUN curl -o allure-2.6.0.tgz -Ls https://dl.bintray.com/qameta/generic/io/qameta/allure/allure/2.6.0/allure-2.6.0.tgz
RUN tar -zxvf allure-2.6.0.tgz -C /opt/
RUN rm -rf allure-2.6.0.tgz
RUN ln -s /opt/allure-2.6.0/bin/allure /usr/bin/allure
RUN allure --version

RUN mkdir restel
WORKDIR restel

COPY build/libs/restel-0.1-all.jar  ./
COPY scripts ./
COPY Sample_Suite_definition.xlsx ./

CMD ["/bin/bash", "start.sh", "Sample_Suite_definition.xlsx"]
