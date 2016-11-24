#!/bin/bash
# Copyright © 2016 Red Sqirl, Ltd. All rights reserved.
# Red Sqirl, Clarendon House, 34 Clarendon St., Dublin 2. Ireland
# 
# This file is part of Red Sqirl
# 
# User agrees that use of this software is governed by: 
# (1) the applicable user limitations and specified terms and conditions of 
#    the license agreement which has been entered into with Red Sqirl; and 
# (2) the proprietary and restricted rights notices included in this software.
# 
# WARNING: THE PROPRIETARY INFORMATION OF Red Sqirl IS PROTECTED BY IRISH AND 
# INTERNATIONAL LAW.  UNAUTHORISED REPRODUCTION, DISTRIBUTION OR ANY PORTION
# OF IT, MAY RESULT IN CIVIL AND/OR CRIMINAL PENALTIES.
# 
# If you have received this software in error please contact Red Sqirl at 
# support@redsqirl.com
#


#Script that deploy the war file into tomcat
#Set up dynamic properties
SCRIPT_LOCATION=${BASH_SOURCE[0]}
SCRIPT_PATH="$(cd $(dirname "${SCRIPT_LOCATION}"); pwd -P)/$(basename "${SCRIPT_LOCATION}")"
SCRIPT_PATH="${SCRIPT_PATH%/*}"
CONF_FILE=$(dirname ${SCRIPT_PATH})/conf/.internal.conf

echo $CONF_FILE
source ${CONF_FILE} 2> /dev/null
TOMCAT_PATH_CUR=$TOMCAT_PATH

if [ ! -d "${TOMCAT_PATH_CUR}" ]; then
   echo The file ${TOMCAT_PATH_CUR} does not exist.
   echo You may want to reset old internal settings that no longer valids using the reset_internals script.
   exit;
fi

if [[ -z $STARTUP ]]; then
    STARTUP="$(dirname $TOMCAT_PATH_CUR})"/bin/startup.sh
fi

if [[ -f /tmp/redsqirl.pid ]]; then
    ${SCRIPT_PATH}/shutdown.sh
fi

if [[ ! -f $STARTUP ]]; then
    echo $STARTUP is not a file. Please specify the web application start script
    read STARTUP
    if [[ ! -f $STARTUP ]]; then
	echo $STARTUP is not a file, will exit.
	exit
    else
	echo STARTUP=${STARTUP} >> ${CONF_FILE}
    fi
fi

echo "Start Red Sqirl web server..."
${STARTUP}
WEBAPP_PID=`ps aux | grep $(dirname $TOMCAT_PATH_CUR}) | grep $USER | head -1 | sed -e 's/\s\+/ /g' | cut -d' ' -f 2`
echo $WEBAPP_PID > /tmp/redsqirl.pid

sleep 1
cat $(dirname ${SCRIPT_PATH})/conf/logo.txt
