#!/bin/sh

DIR=`dirname "$0"`
cd "$DIR"

exec java -classpath gradle/wrapper/gradle-wrapper.jar org.gradle.wrapper.GradleWrapperMain "$@"
