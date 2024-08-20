FROM mcr.microsoft.com/playwright:v1.46.0-jammy

RUN apt-get update && \
    apt-get upgrade -y && \
    apt-get install -y maven

RUN git clone https://github.com/timmitomas/PlaywrightDemo
WORKDIR demo