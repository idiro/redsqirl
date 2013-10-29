#!/bin/bash

#Setup a user.
#This script has to be executed by the user before login to IDM
SCRIPT_LOCATION=${BASH_SOURCE[0]}
SCRIPT_PATH="$(cd $(dirname "${SCRIPT_LOCATION}"); pwd -P)/$(basename "${SCRIPT_LOCATION}")"
SCRIPT_PATH="${SCRIPT_PATH%/*}"

CONF_FILE=${SCRIPT_PATH}/../conf/.internal.conf
CONF_SYS_FILE=${SCRIPT_PATH}/../conf/idm_sys.properties


#Check directories
MAIN_DIRECTORY=${HOME}/.idiroDM
HELP=${MAIN_DIRECTORY}/help
ICON_MENU=${MAIN_DIRECTORY}/icon_menu
IMAGES=${MAIN_DIRECTORY}/images
LIB=${MAIN_DIRECTORY}/lib
PACKAGES=${MAIN_DIRECTORY}/packages
TMP=${MAIN_DIRECTORY}/tmp
IDM_USER=${MAIN_DIRECTORY}/idm_user.properties

if [ -a ${MAIN_DIRECTORY} && ! -d ${MAIN_DIRECTORY} ]; then
    echo "${MAIN_DIRECTORY} has to be a directory"
    exit;
fi

if [ ! -d ${MAIN_DIRECTORY} ]; then
    mkdir ${MAIN_DIRECTORY}
fi

if [ ! -d ${HELP} ]; then
    mkdir ${HELP}
fi

if [ ! -d ${ICON_MENU} ]; then
    mkdir ${ICON_MENU}
fi

if [ ! -d ${IMAGES} ]; then
    mkdir ${IMAGES}
fi

if [ ! -d ${LIB} ]; then
    mkdir ${LIB}
fi

if [ ! -d ${PACKAGES} ]; then
    mkdir ${PACKAGES}
fi

if [ ! -d ${TMP} ]; then
    mkdir ${TMP}
fi

if [ ! -e ${IDM_USER} ]; then
    source ${CONF_FILE} 2> /dev/null
    source ${CONF_SYS_FILE}
    if [ -z "${HIVE_PORT_CUR}" ]; then
	HIVE_PORT_CUR=${start_hive_range}
    fi

    if [ -z "${HIVE_PORT_CUR}" -o "${HIVE_PORT_CUR}" == "${end_hive_range}" ]; then
	manual_assign="TRUE"
	echo "IDM run out of hive jdbc port, please specify a port number available manually: "
	read ${HIVE_PORT_CUR}
    fi

    if [[ -z "${HIVE_PORT_CUR}" -o "${HIVE_PORT_CUR}" =~ "?^[1-9][0-9]*$" ]]; then
	echo "Port given, ${HIVE_PORT_CUR}, not supported"
	exit;
    fi

    echo hive_jdbc_url=\"jdbc:hive://${namenode}:${HIVE_PORT_CUR}/default\" > ${IDM_USER}

    #Update internal conf
    if [ -n "${manual_assign}" ]; then
	let "${HIVE_PORT_CUR}++"
	rm ${CONF_FILE} 2> /dev/null
	echo TOMCAT_PATH=\"${TOMCAT_PATH_CUR}\" > ${CONF_FILE}
	echo HIVE_PORT_CUR=\"${HIVE_PORT_CUR}\" >> ${CONF_FILE}
    fi
fi
