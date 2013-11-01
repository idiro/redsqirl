#!/bin/bash

#Script that deploy the war file into tomcat
#Set up dynamic properties
SCRIPT_LOCATION=${BASH_SOURCE[0]}
SCRIPT_PATH="$(cd $(dirname "${SCRIPT_LOCATION}"); pwd -P)/$(basename "${SCRIPT_LOCATION}")"
SCRIPT_PATH="${SCRIPT_PATH%/*}"

PACKAGE_NAME=$1
if [ -z $PACKAGE_NAME ]; then
	echo "This script takes for argument the package name to uninstall"
	exit
fi

for i in ${SCRIPT_PATH}/../lib/*.jar ; do
    CLASSPATH=$CLASSPATH:$i
done

MAIN_CLASS=idiro.workflow.utils.PackageManager 
export SCRIPT_PATH PID

JAVA_PATH=java
$JAVA_PATH -server -classpath $CLASSPATH $MAIN_CLASS remove user $PACKAGE_NAME
