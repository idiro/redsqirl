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


function help() {
    echo -e "install.sh [-p port] [-d distribution] [-m master node] [-r realm] [-c certificate path -p certificate passord] [-s] [-q] [-h]"
    echo -e " "
    echo -e "Install Red Sqirl: in normal mode, the user will be prompted with options"
    echo -e " "
    echo -e "-p Red Sqirl 4 digit http port (default: 8442, )"
    echo -e "-d Distribution used"
    echo -e "\thdp"
    echo -e "\tcloudera"
    echo -e "\tmapr"
    echo -e "\tcustome (default)"
    echo -e "-m master node on which the hadoop services are running (default: `hostname`). This will merely prepare your settings, you will have the options to change them later on in the process."
    echo -e "-r Kerberos Realm, used only in Hadoop secure mode. (default: no kerberos)"
    echo -e "-c SSL certificate, if given the tomcat web server will automatically be setup with https"
    echo -e "-k SSL certificate password"
    echo -e "-s start or restart the web server after installation"
    echo -e "-q quiet mode"
    echo -e "-h Display this help"
    echo -e " "
}

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
    if [[ -z `grep "${prop}" $SETTINGS_FILE` ]]; then 
        echo $1 >> $SETTINGS_FILE
    else
        sed -i "/${prop}.*/c $1" $SETTINGS_FILE
    fi
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

source ${CONF_FILE} 2> /dev/null

QUIET="n"
TOMCAT_PORT=8842
PORT_ARG_EXIST="n"
DISTRIBUTION="custome"
DISTRIBUTION_ARG_EXIST="n"
MASTER_HOSTNAME=`hostname`
MASTER_ARG_EXIST="n"
REALM=""
SECURE="n"
START_R=""
CERTIFICATE_ARG_EXIST="n"
CERTIFICATE_PATH=""
CERTIFICATE_PASSWORD=""
while getopts "sqhp:d:m:r:c:k:" opt; do
    case $opt in
    s)
        START_R="y"
        ;;
	q)
        QUIET="y"
	    ;;
	p)
	    TOMCAT_PORT=$OPTARG
        PORT_ARG_EXIST="y"
	    ;;
	d)
	    DISTRIBUTION=$OPTARG
        DISTRIBUTION_ARG_EXIST="y"
        if [[ "$DISTRIBUTION" != "custome" &&  "$DISTRIBUTION" != "hdp" && "$DISTRIBUTION" != "cloudera" && "$DISTRIBUTION" != "mapr" ]]; then
            >&2 echo "[ERROR] Distributon supported are: custome, hdp, cloudera or map"
            help
            exit
        fi
	    ;;
	m)
	    MASTER_HOSTNAME=$OPTARG
        MASTER_ARG_EXIST="y"
	    ;;
	r)
        SECURE="y"
	    REALM=$OPTARG
	    ;;
    c)
        CERTIFICATE_ARG_EXIST="y"
	    CERTIFICATE_PATH=$OPTARG
        ;;
    k)
        CERTIFICATE_ARG_EXIST="y"
        CERTIFICATE_PASSWORD=$OPTARG
        ;;
	h)
	    help
	    exit 0;
	    ;;
    *)
        >&2 echo "[ERROR] Argument unrecognized"
        help
        exit;
        ;;
    esac
done

if [[ $QUIET == "y" && $CERTIFICATE_ARG_EXIST == "y" && ( (! -f "$CERTIFICATE_PATH") || -z "$CERTIFICATE_PASSWORD" ) ]]; then
    echo $CERTIFICATE_PATH
    echo $CERTIFICATE_PASSWORD
    >&2 echo "[ERROR] Certificate set up requires a certificate and a password"
    help
    exit;
fi

#Check permissions subdirectories to access lib folder
if [[ $QUIET == "n" ]]; then
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
fi

#Input the Tomcat port, find the tomcat path
if [ -z "${TOMCAT_PATH}" ]; then
    if [[ -d ${DEFAULT_TOMCAT} ]]; then
        TOMCAT_PATH_CUR=${DEFAULT_TOMCAT}
        DONOTCONFIRM="TRUE"
        if [[ ${QUIET} == "n" && ${PORT_ARG_EXIST} == "n" ]]; then
            echo "Please specify the tomcat 4 digit port (default 8842):"
            read TOMCAT_PORT
        fi
        if [[ -z "${TOMCAT_PORT}" ]]; then
            TOMCAT_PORT=8842
        elif [[  ! "${TOMCAT_PORT}" =~ ^[0-9]{4}$ ]]; then
            echo "port number is invalid "
            exit;
        fi
    else
        >&2 echo "[WARN] Please specify the tomcat path where the war should be copied:"
        if [[ ${QUIET} == "y" ]]; then
            exit;
        fi
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

if [[ ${DONOTCONFIRM} == "FALSE" && ${QUIET} == "n" ]]; then
    echo "Are you sure that ${TOMCAT_PATH_CUR} is a correct destination? [y/N]"
    read CONF
    if [[ "${CONF}" != 'y' && "${CONF}" != 'Y' ]]; then
	echo Exit from the script
	exit;
    fi
fi


if [[ $QUIET == "n" ]]; then
    if [[ $CERTIFICATE_ARG_EXIST == "n" ]]; then
        echo "Do you want to enable Red Sqirl on https? [y/N]"
        read CERTIFICATE_ARG_EXIST
        if [[ "$CERTIFICATE_ARG_EXIST" == "y" || "$CERTIFICATE_ARG_EXIST" == "Y" ]]; then
            CERTIFICATE_ARG_EXIST="y"
        else
            CERTIFICATE_ARG_EXIST="n"
        fi
    fi
    if [[ $CERTIFICATE_ARG_EXIST == "y" ]]; then
        while [[ ! -f "$CERTIFICATE_PATH" ]]; do
            echo "What is the certificate path?"
            read CERTIFICATE_PATH
            if [[ ! -f "$CERTIFICATE_PATH" ]]; then
                >&2 echo "[ERROR] Certificate path invalid"
            else
                break;
            fi
        done
        while [[ -z "$CERTIFICATE_PASSWORD"  ]]; do
            echo "What is the certificate password?"
            read CERTIFICATE_PASSWORD
            if [[ -z "$CERTIFICATE_PASSWORD" ]]; then
                >&2 echo "[ERROR] Certificate requires a password"
            else
                break;
            fi
        done
    fi
fi
###############################################################################
#Auto settings
shopt -s nocasematch

#Cluster type
if [[ ${QUIET} == "n" && ${DISTRIBUTION_ARG_EXIST} == "n" ]]; then
    echo "In the next steps, Red Sqirl will create default settings as much relevant to you as possible."
    while [ true ]; do
        echo "What is your cluster? [custome], hdp, cloudera or mapr. \"custome\" will jump the auto settings."
        read DISTRIBUTION
        if [[ -z "$DISTRIBUTION" ]]; then
            DISTRIBUTION="custome"
            break;
        elif [[ "$DISTRIBUTION" == "custome" ||  "$DISTRIBUTION" == "hdp" || "$DISTRIBUTION" == "cloudera" || "$DISTRIBUTION" == "mapr" ]]; then
            break;
        else
            echo "Type unrecognized"
        fi
    done
fi

#Master
if [[  ${QUIET} == "n" && ("$DISTRIBUTION" == "hdp" || "$DISTRIBUTION" == "cloudera" || "$DISTRIBUTION" == "mapr") ]]; then
    echo -e "We will preset your settings with default values, you will have the options to change them later on, directly in Red Sqirl web application."
    if [[ ${MASTER_ARG_EXIST} == "n" ]]; then 
        while [ true ]; do
            echo "What is your master node hostname, on which most of your services are running?"
            read MASTER_HOSTNAME
            if [[ -z "$MASTER_HOSTNAME" ]]; then
                MASTER_HOSTNAME=`hostname`
            fi
            echo "Is your master node hostname called ${MASTER_HOSTNAME}? [Y/n]"
            read CONFIRM_HOSTNAME
            if [[ -z "${CONFIRM_HOSTNAME}" || "${CONFIRM_HOSTNAME}" == 'Y' || "${CONFIRM_HOSTNAME}" == 'y' ]]; then
                break;
            fi
        done
    fi
    #Secure
    if [[ -n "${REALM}" ]]; then
        SECURE="y"
    else
        echo "Does your cluster run in secure mode (Kerberos)? [y/N]"
        read SECURE
        if [[ "${SECURE}" == 'y' || "${SECURE}" == 'Y' ]]; then
            SECURE="y"
            while [ true ]; do
                echo "Don't forget to generate a passwordless principal for each user on this host!"
                echo "What is your kerberos realm?"
                read REALM
                echo "Is your realm called ${REALM}? [Y/n]"
                read REALM_OK
                if [[ -z "${REALM_OK}" || "${REALM_OK}" == 'Y' || "${REALM_OK}" == 'y' ]]; then
                    break;
                fi
            done
        else
            SECURE="n"
        fi
    fi
fi


#Write settings
if [[ "$DISTRIBUTION" == "hdp" ]]; then
    change_rs_prop "core.namenode=hdfs\://${MASTER_HOSTNAME}\:8020"
    change_rs_prop "core.jobtracker=${MASTER_HOSTNAME}\:8050"
    change_rs_prop "core.oozie.oozie_url=http\://${MASTER_HOSTNAME}\:11000/oozie"
    change_rs_prop "core.hadoop_home=/usr/hdp/current/hadoop-client"
    change_rs_prop "core.hcatalog.metastore_uri=thrift\://${MASTER_HOSTNAME}\:9083"
    if [[ "${SECURE}" == 'y' ]]; then
        change_rs_prop "core.hcatalog.hive_url=jdbc\:hive2\://${MASTER_HOSTNAME}\:10000/default;principal=hive/${MASTER_HOSTNAME}@${REALM}"
        change_rs_prop "core.security.enable=TRUE"
        change_rs_prop "core.security.realm=${REALM}"
    else
        change_rs_prop "core.hcatalog.hive_url=jdbc\:hive2\://${MASTER_HOSTNAME}\:10000/default"
    fi
elif [[ "$DISTRIBUTION" == "cloudera" ]]; then
    change_rs_prop "core.namenode=hdfs\://${MASTER_HOSTNAME}\:8022"
    change_rs_prop "core.jobtracker=${MASTER_HOSTNAME}\:8032"
    change_rs_prop "core.hcatalog.metastore_uri=thrift\://${MASTER_HOSTNAME}\:9083"
    change_rs_prop "core.oozie.oozie_url=http\://${MASTER_HOSTNAME}\:11000/oozie"
    change_rs_prop "core.hadoop_home=/usr/lib/hadoop"
    if [[ "${SECURE}" == 'y' ]]; then
        change_rs_prop "core.hcatalog.hive_url=jdbc\:hive2\://${MASTER_HOSTNAME}\:9083/default;principal=hive/${MASTER_HOSTNAME}@${REALM}"
        change_rs_prop "core.security.enable=TRUE"
        change_rs_prop "core.security.realm=${REALM}"
    else
        change_rs_prop "core.hcatalog.hive_url=jdbc\:hive2\://${MASTER_HOSTNAME}\:9083/default"
    fi

elif [[ "$DISTRIBUTION" == "mapr" ]]; then
    change_rs_prop "core.namenode=maprfs\:///mapr/mycluster.mapr.com"
    change_rs_prop "core.jobtracker=${MASTER_HOSTNAME}\:8032"
    change_rs_prop "core.hcatalog.hive_url=jdbc\:hive2\://${MASTER_HOSTNAME}\:10000/default"
    change_rs_prop "core.hcatalog.metastore_uri=thrift\://${MASTER_HOSTNAME}\:9083"
    change_rs_prop "core.oozie.oozie_url=http\://${MASTER_HOSTNAME}\:11000/oozie"
    change_rs_prop "core.hadoop_home=/opt/mapr"
    if [[ "${SECURE}" == 'y' ]]; then
        change_rs_prop "core.hcatalog.hive_url=jdbc\:hive2\://${MASTER_HOSTNAME}\:10000/default;principal=hive/${MASTER_HOSTNAME}@${REALM}"
        change_rs_prop "core.security.enable=TRUE"
        change_rs_prop "core.security.realm=${REALM}"
    else
        change_rs_prop "core.hcatalog.hive_url=jdbc\:hive2\://${MASTER_HOSTNAME}\:10000/default"
    fi
fi
shopt -u nocasematch

###############################################################################
#Read the dynamic conf file and update tomcat_path
#The hive port needs to stick at the old value if any
rm ${CONF_FILE} 2> /dev/null
echo TOMCAT_PATH=\"${TOMCAT_PATH_CUR}\" > ${CONF_FILE}

PREV_DIR_RS=${TOMCAT_PATH_CUR}/redsqirl
PREV_DIR_PCK=${TOMCAT_PATH_CUR}/packages
if [[ -d "${PREV_DIR_RS}" && ${QUIET} == "n" ]]; then
    echo "Do you want to delete the previous ${PREV_DIR_RS} directory? [y/N]"
    read DEL_DIR_RS
    if [[ "${DEL_DIR_RS}" == 'y' || "${DEL_DIR_RS}" == 'Y' ]]; then
        rm -r ${PREV_DIR_RS}
    fi
fi
if [[ -d "${PREV_DIR_PCK}" && ${QUIET} == "n" ]]; then
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
#protocol="org.apache.coyote.http11.Http11Protocol"

if [[ $CERTIFICATE_ARG_EXIST == "n" ]]; then
    sed -i "s#.*<Connector port=\"....\" protocol=\"HTTP/1.1\".*#    <Connector port=\"$TOMCAT_PORT\" protocol=\"HTTP/1.1\"#g" ${TOMCAT_PATH_CUR}/../conf/server.xml
    sed -i "s#.*<Connector port=\"....\" protocol=\"org.apache.coyote.http.*#    <Connector port=\"$TOMCAT_PORT\" protocol=\"HTTP/1.1\"#g" ${TOMCAT_PATH_CUR}/../conf/server.xml
else
    sed -i "s#.*<Connector port=\"....\" protocol=\"HTTP/1.1\".*#    <Connector port=\"$TOMCAT_PORT\" protocol=\"org.apache.coyote.http11.Http11NioProtocol\" maxThreads=\"200\" scheme=\"https\" secure=\"true\" SSLEnabled=\"true\" keystoreFile=\"${CERTIFICATE_PATH}\" keystorePass=\"${CERTIFICATE_PASSWORD}\" clientAuth=\"false\" sslProtocol=\"TLS\"#g" ${TOMCAT_PATH_CUR}/../conf/server.xml
    sed -i "#.*<Connector port=\"....\" protocol=\"org.apache.coyote.http.*#    <Connector port=\"$TOMCAT_PORT\" protocol=\"org.apache.coyote.http11.Http11NioProtocol\" maxThreads=\"200\" scheme=\"https\" secure=\"true\" SSLEnabled=\"true\" keystoreFile=\"${CERTIFICATE_PATH}\" keystorePass=\"${CERTIFICATE_PASSWORD}\" clientAuth=\"false\" sslProtocol=\"TLS\"#g" ${TOMCAT_PATH_CUR}/../conf/server.xml
fi

property_line="path_sys_home=`dirname ${SCRIPT_PATH}`"
tomcat_conf_dir=`dirname ${TOMCAT_PATH_CUR}`/conf
err_prop="n"
if [ -w $tomcat_conf ] ; then 
    echo $property_line > ${tomcat_conf_dir}/idiro.properties
    echo "Installation successful..."
else
    >&2 echo "[ERROR] You don\'t have permission to write in the directory ${tomcat_conf_dir}."
    echo "RedSqirl needs a idiro.properties file in the former directory containing \"${property_line}\".";
    err_prop="y"
fi



#Start tomcat
if [[ $err_prop == "n" ]]; then
    if [[ ${QUIET} == "n" && -z "${START_R}" ]]; then
        echo "Do you want to start the web server? [Y/n]"
        read START_R
        if [[ ! "${START_R}" == 'n' && ! "${START_R}" == 'N' ]]; then
            ${SCRIPT_PATH}/startup.sh
        fi
    elif [[ ${START_R} == "y" ]]; then
        ${SCRIPT_PATH}/startup.sh
    fi
elif [[ ${START_R} == "y" ]]; then
    >&2 echo "[ERROR] Enable to start tomcat due to missing property"
fi

