#!/bin/bash

SCRIPT_LOCATION=${BASH_SOURCE[0]}
SCRIPT_PATH="$(cd $(dirname "${SCRIPT_LOCATION}"); pwd -P)/$(basename "${SCRIPT_LOCATION}")"
SCRIPT_PATH="${SCRIPT_PATH%/*}"

USER=$1

if [ -z $USER ]; then
   echo Please specify a user.
   exit;
fi 

echo "Are you sure to remove the home directory for $USER [y/N]"
read CONFIRM 
if [ "$CONFIRM" != "y" ]; then
   echo Abort.
   exit;
fi

rm -r ${SCRIPT_PATH}/../users/${USER}
