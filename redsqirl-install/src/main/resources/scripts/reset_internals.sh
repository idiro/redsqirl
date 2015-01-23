#!/bin/bash

#reset dynamic properties
SCRIPT_LOCATION=${BASH_SOURCE[0]}
SCRIPT_PATH="$(cd $(dirname "${SCRIPT_LOCATION}"); pwd -P)/$(basename "${SCRIPT_LOCATION}")"
SCRIPT_PATH="${SCRIPT_PATH%/*}"

CONF_FILE=${SCRIPT_PATH}/../conf/.internal.conf

rm ${CONF_FILE}
