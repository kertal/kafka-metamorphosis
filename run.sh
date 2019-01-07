#!/usr/bin/env bash
#Build and run CLI application for testing

if [ "$1" == "topic" ]; then
    task=$1
    shift 1
fi

./gradlew installDist
./build/install/kafka-metamorphosis/bin/kafka-metamorphosis "$@"
