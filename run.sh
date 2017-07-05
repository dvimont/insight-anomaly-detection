#!/bin/bash

# NOTE that Java 1.8 and Maven are required to be installed in the environment in which this is to be run

### build the application (compile, run tests, install jar, etc.)
mvn clean install

### execute the application
mvn exec:java  -Dexec.mainClass=org.commonvox.insight.anomaly_detector.App \
  -Dexec.args="./log_input/batch_log.json ./log_input/stream_log.json ./log_output/flagged_purchases.json"
