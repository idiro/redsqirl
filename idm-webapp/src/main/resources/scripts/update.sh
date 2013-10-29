#!/bin/bash

#Script that deploy the war file into tomcat
#Set up dynamic properties
SCRIPT_LOCATION=${BASH_SOURCE[0]}
SCRIPT_PATH="$(cd $(dirname "${SCRIPT_LOCATION}"); pwd -P)/$(basename "${SCRIPT_LOCATION}")"
SCRIPT_PATH="${SCRIPT_PATH%/*}"

CONF_FILE=${SCRIPT_PATH}/../conf/.internal.conf

echo "Please specify the tomcat path where the war should be copied:"
read TOMCAT_PATH_CUR

if [ ! -d "${TOMCAT_PATH_CUR}" ]; then
   echo The file ${TOMCAT_PATH_CUR} does not exist.
   exit;
fi

if [[ ! "${TOMCAT_PATH_CUR}" =~ "(tomcat)" ]]; then
    echo "Are you sure that ${TOMCAT_PATH_CUR} is a correct destination? [y/N]"
    read CONF
    if [[ "${CONF}" != 'y' && "${CONF}" != 'Y' ]]; then
	echo Exit from the script
	exit;
    fi
fi

#Read the dynamic conf file and update tomcat_path
#The hive port needs to stick at the old value if any
source ${CONF_FILE} 2> /dev/null
rm ${CONF_FILE} 2> /dev/null
echo TOMCAT_PATH=\"${TOMCAT_PATH_CUR}\" > ${CONF_FILE}
echo HIVE_PORT_CUR=\"${HIVE_PORT_CUR}\" >> ${CONF_FILE}

for i in ${SCRIPT_PATH}/../lib/*.jar ; do
    CLASSPATH=$CLASSPATH:$i
done

MAIN_CLASS=idiro.workflow.server.WorkflowPrefManager
export SCRIPT_PATH PID

JAVA_PATH=java
#echo $JAVA_PATH -server -classpath $CLASSPATH $MAIN_CLASS pathSystemPref="$(dirname  ${SCRIPT_PATH})/conf" pathSysHome="$(dirname  ${SCRIPT_PATH})"
$JAVA_PATH -server -classpath $CLASSPATH $MAIN_CLASS pathSystemPref="$(dirname  ${SCRIPT_PATH})/conf" pathSysHome="$(dirname  ${SCRIPT_PATH})"

#Copy war file
cp ${SCRIPT_PATH}/../war/* ${TOMCAT_PATH_CUR}
