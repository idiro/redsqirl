#!/bin/bash

#Script that deploy the war file into tomcat
#Set up dynamic properties
SCRIPT_LOCATION=${BASH_SOURCE[0]}
SCRIPT_PATH="$(cd $(dirname "${SCRIPT_LOCATION}"); pwd -P)/$(basename "${SCRIPT_LOCATION}")"
SCRIPT_PATH="${SCRIPT_PATH%/*}"

CONF_FILE=${SCRIPT_PATH}/../conf/.internal.conf

source ${CONF_FILE} 2> /dev/null
if [ -z "${TOMCAT_PATH}" ]; then
    echo "Please specify the tomcat path where the war should be copied:"
    read TOMCAT_PATH_CUR
else
    TOMCAT_PATH_CUR=$TOMCAT_PATH
fi

if [ ! -d "${TOMCAT_PATH_CUR}" ]; then
   echo The file ${TOMCAT_PATH_CUR} does not exist.
   echo You may want to reset old internal settings that no longer valids using the reset_internals script.
   exit;
fi

echo "Are you sure that ${TOMCAT_PATH_CUR} is a correct destination? [y/N]"
read CONF
if [[ "${CONF}" != 'y' && "${CONF}" != 'Y' ]]; then
    echo Exit from the script
	exit;
fi

#Read the dynamic conf file and update tomcat_path
#The hive port needs to stick at the old value if any
rm ${CONF_FILE} 2> /dev/null
echo TOMCAT_PATH=\"${TOMCAT_PATH_CUR}\" > ${CONF_FILE}
echo HIVE_PORT_CUR=\"${HIVE_PORT_CUR}\" >> ${CONF_FILE}

for i in ${SCRIPT_PATH}/../lib/*.jar ; do
    CLASSPATH=$CLASSPATH:$i
done

MAIN_CLASS=com.redsqirl.workflow.utils.WorkflowPrefManager
export SCRIPT_PATH PID

JAVA_PATH=java
#echo $JAVA_PATH -server -classpath $CLASSPATH $MAIN_CLASS pathSystemPref="$(dirname  ${SCRIPT_PATH})/conf" pathSysHome="$(dirname  ${SCRIPT_PATH})"
$JAVA_PATH -server -classpath $CLASSPATH $MAIN_CLASS pathSystemPref="$(dirname  ${SCRIPT_PATH})/conf" pathSysHome="$(dirname  ${SCRIPT_PATH})"

#Copy war file
cp ${SCRIPT_PATH}/../war/* ${TOMCAT_PATH_CUR}

property_line="path_sys_home=`dirname ${SCRIPT_PATH}`"
tomcat_conf_dir=`dirname ${TOMCAT_PATH_CUR}`/conf
if [ -w $tomcat_conf ] ; then 
    echo $property_line > ${tomcat_conf_dir}/idiro.properties
else
    echo "You don't have permission to write in the directory ${tomcat_conf_dir}."
    echo "RedSqirl needs a idiro.properties file in the former directory containing \"${property_line}\".";
fi
