#!/bin/bash

#Script that deploy the war file into tomcat
#Set up dynamic properties
SCRIPT_LOCATION=${BASH_SOURCE[0]}
SCRIPT_PATH="$(cd $(dirname "${SCRIPT_LOCATION}"); pwd -P)/$(basename "${SCRIPT_LOCATION}")"
SCRIPT_PATH="${SCRIPT_PATH%/*}"

FILENAME=$1
if [ -z $FILENAME ]; then
	echo "This script takes for argument the file path of the package to install"
	exit
fi

if [ ! -f "$FILENAME" ]; then
	echo "This script takes for argument the file path of the package to install"
	echo "The file $FILENAME does not exist"
	exit
fi


for i in ${SCRIPT_PATH}/../lib/*.jar ; do
    CLASSPATH=$CLASSPATH:$i
done

MAIN_CLASS=com.redsqirl.workflow.utils.PackageManager 
export SCRIPT_PATH PID

JAVA_PATH=java
$JAVA_PATH -server -classpath $CLASSPATH $MAIN_CLASS add system ${SCRIPT_PATH}/.. $FILENAME
