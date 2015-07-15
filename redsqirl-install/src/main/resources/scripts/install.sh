#!/bin/bash

#Script that deploy the war file into tomcat
#Set up dynamic properties
SCRIPT_LOCATION=${BASH_SOURCE[0]}
SCRIPT_PATH="$(cd $(dirname "${SCRIPT_LOCATION}"); pwd -P)/$(basename "${SCRIPT_LOCATION}")"
SCRIPT_PATH="${SCRIPT_PATH%/*}"
DEFAULT_TOMCAT=$(dirname ${SCRIPT_PATH})/apache-tomcat-7.0.42/webapps
CONF_FILE=$(dirname ${SCRIPT_PATH})/conf/.internal.conf
DONOTCONFIRM="FALSE"

TOMCAT_PORT=8080

source ${CONF_FILE} 2> /dev/null
if [ -z "${TOMCAT_PATH}" ]; then
    if [[ -d ${DEFAULT_TOMCAT} ]]; then
	TOMCAT_PATH_CUR=${DEFAULT_TOMCAT}
	DONOTCONFIRM="TRUE"
	echo "Please specify the tomcat port (default 8080):"
        read TOMCAT_PORT
	if [[ -z "${TOMCAT_PORT}" ]]; then
	    TOMCAT_PORT=8080
	elif [[  ! "${TOMCAT_PORT}" =~ [0-9]4 ]]; then
        	echo "port number is invalid "
                exit;
        fi
    else
	echo "Please specify the tomcat path where the war should be copied:"
	read TOMCAT_PATH_CUR
    fi
else
    TOMCAT_PATH_CUR=$TOMCAT_PATH
fi

if [ ! -d "${TOMCAT_PATH_CUR}" ]; then
   echo The file ${TOMCAT_PATH_CUR} does not exist.
   echo You may want to reset old internal settings that no longer valids using the reset_internals script.
   exit;
fi

if [[ ${DONOTCONFIRM} == "FALSE" ]]; then
    echo "Are you sure that ${TOMCAT_PATH_CUR} is a correct destination? [y/N]"
    read CONF
    if [[ "${CONF}" != 'y' && "${CONF}" != 'Y' ]]; then
	echo Exit from the script
	exit;
    fi
fi

#Read the dynamic conf file and update tomcat_path
#The hive port needs to stick at the old value if any
rm ${CONF_FILE} 2> /dev/null
echo TOMCAT_PATH=\"${TOMCAT_PATH_CUR}\" > ${CONF_FILE}
echo HIVE_PORT_CUR=\"${HIVE_PORT_CUR}\" >> ${CONF_FILE}

PREV_DIR_RS=${TOMCAT_PATH_CUR}/redsqirl
PREV_DIR_PCK=${TOMCAT_PATH_CUR}/packages
if [[ -d "${PREV_DIR_RS}" ]]; then
    echo "Do you want to delete the previous ${PREV_DIR_RS} directory? [y/N]"
    read DEL_DIR_RS
    if [[ "${DEL_DIR_RS}" == 'y' || "${DEL_DIR_RS}" == 'Y' ]]; then
	rm -r ${PREV_DIR_RS}
    fi
fi
if [[ -d "${PREV_DIR_PCK}" ]]; then
    echo "Do you want to delete the previous ${PREV_DIR_PCK} directory? [y/N]"
    read DEL_DIR_PCK
    if [[ "${DEL_DIR_PCK}" == 'y' || "${DEL_DIR_PCK}" == 'Y' ]]; then
	rm -r ${PREV_DIR_PCK}
    fi
fi

#Copy war file
cp ${SCRIPT_PATH}/../war/* ${TOMCAT_PATH_CUR}
chmod -R 500 ${TOMCAT_PATH_CUR}/../bin
sed -i "s#<Connector port=\"8080\"#<Connector port=\"$TOMCAT_PORT\"#g" ${TOMCAT_PATH_CUR}/../conf/server.xml

property_line="path_sys_home=`dirname ${SCRIPT_PATH}`"
tomcat_conf_dir=`dirname ${TOMCAT_PATH_CUR}`/conf
if [ -w $tomcat_conf ] ; then 
    echo $property_line > ${tomcat_conf_dir}/idiro.properties
else
    echo "You don't have permission to write in the directory ${tomcat_conf_dir}."
    echo "RedSqirl needs a idiro.properties file in the former directory containing \"${property_line}\".";
fi

echo "Installation successful..."

echo "Do you want to start the web server? [Y/n]"
read START_R
if [[ ! "${START_R}" == 'n' && ! "${START_R}" == 'N' ]]; then
    ${SCRIPT_PATH}/startup.sh
fi

