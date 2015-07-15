#!/bin/bash

#Script that deploy the war file into tomcat
#Set up dynamic properties
SCRIPT_LOCATION=${BASH_SOURCE[0]}
SCRIPT_PATH="$(cd $(dirname "${SCRIPT_LOCATION}"); pwd -P)/$(basename "${SCRIPT_LOCATION}")"
SCRIPT_PATH="${SCRIPT_PATH%/*}"
CONF_FILE=$(dirname ${SCRIPT_PATH})/conf/.internal.conf

source ${CONF_FILE} 2> /dev/null
TOMCAT_PATH_CUR=$TOMCAT_PATH

if [ ! -d "${TOMCAT_PATH_CUR}" ]; then
   echo The file ${TOMCAT_PATH_CUR} does not exist.
   echo You may want to reset old internal settings that no longer valids using the reset_internals script.
   exit;
fi

if [[ -z $SHUTDOWN ]]; then
    SHUTDOWN=$(dirname TOMCAT_PATH_CUR})/bin/shutdown.sh
fi

if [[ ! -f $SHUTDOWN ]]; then
    echo $SHUTDOWN is not a file. Please specify the web application start script
    read SHUTDOWN
    if [[ ! -f $SHUTDOWN ]]; then
	echo $SHUTDOWN is not a file, will exit.
	exit
    else
	echo SHUTDOWN=${SHUTDOWN} >> ${CONF_FILE}
    fi
fi

if [[ -f /tmp/redsqirl.pid ]]; then
    echo "Shut down Red Sqirl web server..."
    ${SHUTDOWN}
    kill -9 `cat /tmp/redsqirl.pid`
    rm /tmp/redsqirl.pid
fi

