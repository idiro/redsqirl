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


function check_permission_ancestor(){                                           
    _file=$1                                                                     
    _regex="drwxr.xr.x"                                                          
    #No enter if it is root directory                                            
    if [[ -n "$_file" && ${#_file} != 1 ]]; then                                 
	_parentdir=`dirname $_file`                                              
	_command=""                                                              
	_basenamef=`basename $_file`                                             
	_perm=`ls -al $_parentdir | grep " ${_basenamef}$"`     
	#echo $_file":" $_perm                                                   
	#if the user is owned by current user                                    
	#Change permission if needed                                         
	if ! [[ $_perm =~ $_regex ]]; then                                   
	    echo "File $_file: $_perm"
	fi                                                                   
	#Check parent directory                                              
	check_permission_ancestor $_parentdir                               
    fi                                                                           
} 

function change_rs_prop {
    prop=`cut -d "=" -f 1 <<< "$1"`
    sed -i "/${prop}.*/c $1" $SETTINGS_FILE
}

#Script that deploy the war file into tomcat
#Set up dynamic properties
SCRIPT_LOCATION=${BASH_SOURCE[0]}
SCRIPT_PATH="$(cd $(dirname "${SCRIPT_LOCATION}"); pwd -P)/$(basename "${SCRIPT_LOCATION}")"
SCRIPT_PATH="${SCRIPT_PATH%/*}"
DEFAULT_TOMCAT=$(dirname ${SCRIPT_PATH})/apache-tomcat-7.0.42/webapps
CONF_FILE=$(dirname ${SCRIPT_PATH})/conf/.internal.conf
SETTINGS_FILE=$(dirname ${SCRIPT_PATH})/conf/redsqirl_sys.properties
DONOTCONFIRM="FALSE"

TOMCAT_PORT=8842

source ${CONF_FILE} 2> /dev/null

#Check permissions subdirectories to access lib folder
file_perm=`check_permission_ancestor $(dirname ${SCRIPT_PATH})/lib `
if [ -n "$file_perm" ]; then
    echo "Every OS user should be able to read Red Sqirl home folder."
    echo "Suspcious permissions have been detected in the parent directory."
    echo -e "$file_perm"
    CONT=""
    echo "Do you want to continue anyway? [y/N]"
    read CONT
    if [[ "${CONT}" != 'y' && "${CONT}" != 'Y' ]]; then
	echo Exit from the script
	exit;
    fi
fi

#Input the Tomcat port, find the tomcat path
if [ -z "${TOMCAT_PATH}" ]; then
    if [[ -d ${DEFAULT_TOMCAT} ]]; then
	TOMCAT_PATH_CUR=${DEFAULT_TOMCAT}
	DONOTCONFIRM="TRUE"
	echo "Please specify the tomcat port (default 8842):"
        read TOMCAT_PORT
	if [[ -z "${TOMCAT_PORT}" ]]; then
	    TOMCAT_PORT=8842
	elif [[  ! "${TOMCAT_PORT}" =~ ^[0-9]{4}$ ]]; then
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

###############################################################################
#Auto settings
shopt -s nocasematch

#Cluster type
CLUSTER_TYPE=""
echo "In the next steps, Red Sqirl will create default settings as much relevant to you as possible."
while [ true ]; do
    echo "What is your cluster? [custome], hdp, cloudera or mapr. \"custome\" will jump the auto settings."
    read CLUSTER_TYPE
    if [[ -z "$CLUSTER_TYPE" ]]; then
	CLUSTER_TYPE="custome"
	break;
    elif [[ "$CLUSTER_TYPE" == "custome" ||  "$CLUSTER_TYPE" == "hdp" || "$CLUSTER_TYPE" == "cloudera" || "$CLUSTER_TYPE" == "mapr" ]]; then
	break;
    else
	echo "Type unrecognized"
    fi
done

#Master
if [[ "$CLUSTER_TYPE" == "hdp" || "$CLUSTER_TYPE" == "cloudera" || "$CLUSTER_TYPE" == "mapr" ]]; then
    while [ true ]; do
	echo "What is your master node hostname, on which most of your services are running?"
	read MASTER_HOSTNAME
	if [[ -z "$MASTER_HOSTNAME" ]]; then
	    MASTER_HOSTNAME=`hostname`
	fi
	echo "Is your master node hostname called ${MASTER_HOSTNAME}? [Y/n]"
	read START_R
	if [[ -z "${START_R}" || "${START_R}" == 'Y' || "${START_R}" == 'y' ]]; then
	    break;
	fi
    done
fi

#Write settings
if [[ "$CLUSTER_TYPE" == "hdp" ]]; then
    change_rs_prop "core.namenode=hdfs\://${MASTER_HOSTNAME}\:8020"
    change_rs_prop "core.jobtracker=${MASTER_HOSTNAME}\:8050"
    change_rs_prop "core.hcatalog.hive_url=jdbc\:hive2\://${MASTER_HOSTNAME}\:10000"
    change_rs_prop "core.oozie.oozie_url=http\://${MASTER_HOSTNAME}\:11000/oozie"
    change_rs_prop "core.hadoop_home=/usr/hdp/current/hadoop-client"

elif [[ "$CLUSTER_TYPE" == "cloudera" ]]; then
    change_rs_prop "core.namenode=hdfs\://${MASTER_HOSTNAME}\:8022"
    change_rs_prop "core.jobtracker=${MASTER_HOSTNAME}\:8032"
    change_rs_prop "core.hcatalog.hive_url=jdbc\:hive2\://${MASTER_HOSTNAME}\:9083"
    change_rs_prop "core.oozie.oozie_url=http\://${MASTER_HOSTNAME}\:11000/oozie"
    change_rs_prop "core.hadoop_home=/usr/lib/hadoop"

elif [[ "$CLUSTER_TYPE" == "mapr" ]]; then
    change_rs_prop "core.namenode=maprfs\:///mapr/mycluster.mapr.com"
    change_rs_prop "core.jobtracker=${MASTER_HOSTNAME}\:8032"
    change_rs_prop "core.hcatalog.hive_url=jdbc\:hive2\://${MASTER_HOSTNAME}\:10000"
    change_rs_prop "core.oozie.oozie_url=http\://${MASTER_HOSTNAME}\:11000/oozie"
    change_rs_prop "core.hadoop_home=/opt/mapr"
fi
shopt -u nocasematch

###############################################################################
#Read the dynamic conf file and update tomcat_path
#The hive port needs to stick at the old value if any
rm ${CONF_FILE} 2> /dev/null
echo TOMCAT_PATH=\"${TOMCAT_PATH_CUR}\" > ${CONF_FILE}

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
chmod 500 ${TOMCAT_PATH_CUR}/../bin
chmod 700 ${TOMCAT_PATH_CUR}/../bin/*.sh

#Change tomcat port
sed -i "s#<Connector port=\"....\" protocol=\"HTTP#<Connector port=\"$TOMCAT_PORT\" protocol=\"HTTP#g" ${TOMCAT_PATH_CUR}/../conf/server.xml

property_line="path_sys_home=`dirname ${SCRIPT_PATH}`"
tomcat_conf_dir=`dirname ${TOMCAT_PATH_CUR}`/conf
if [ -w $tomcat_conf ] ; then 
    echo $property_line > ${tomcat_conf_dir}/idiro.properties
else
    echo "You don\'t have permission to write in the directory ${tomcat_conf_dir}."
    echo "RedSqirl needs a idiro.properties file in the former directory containing \"${property_line}\".";
fi

echo "Installation successful..."


#Start tomcat
echo "Do you want to start the web server? [Y/n]"
read START_R
if [[ ! "${START_R}" == 'n' && ! "${START_R}" == 'N' ]]; then
    ${SCRIPT_PATH}/startup.sh
fi

