#!/bin/bash

#JAVA12_HOME
#export JAVA_HOME=$JAVA12_HOME
#export PATH=$JAVA12_HOME/bin:$PATH
java -version

./gradlew -v

./gradlew clean licenseMain licenseTest assemble test jacocoTestReport "$@"
