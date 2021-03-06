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

set -e

function help {
    echo -e "publish.sh -d DIRECTORY_NAME [-h]"
}

SCRIPT_LOCATION=${BASH_SOURCE[0]}
SCRIPT_PATH="$(cd $(dirname "${SCRIPT_LOCATION}"); pwd -P)/$(basename "${SCRIPT_LOCATION}")"
SCRIPT_PATH="${SCRIPT_PATH%/*}"

while getopts "d:h" opt; do
    case $opt in
	d)
	    DIRECTORY_NAME=$OPTARG
	    ;;
	h)
	    help
	    exit 0;
	    ;;
    esac
done

if [ -z "$DIRECTORY_NAME" ]; then
    echo "No -d options"
    help
    exit 1;
fi
mkdir -p $DIRECTORY_NAME
pushd ${SCRIPT_PATH}/../../../
pwd

#Compile idiro hadoop
pushd ../idiro-hadoop
mvn clean install -Dhadoop.artifactId=hadoop-core -Dhadoop.version=1.0.3-mapr-4.1.0 -DskipTests
mvn clean install -Dhadoop.version=2.4.0 -DskipTests
mvn clean install -Dhadoop.version=2.6.0 -DskipTests
mvn clean install -Dhadoop.version=2.7.0 -DskipTests
mvn clean install -Dhadoop.version=2.7.1 -DskipTests
mvn clean install -Dhadoop.version=2.7.3 -DskipTests
#mvn clean install -Dhadoop.version=2.7.1.2.4.4.1-5 -DskipTests
popd

mvn clean install  -Dhadoop.version=1.0.3-mapr-4.1.0 -DskipTests
mv redsqirl-install/target/redsqirl-*-tomcat.tar.gz $DIRECTORY_NAME 

mvn clean install  -Dhadoop.version=2.4.0 -DskipTests
mv redsqirl-install/target/redsqirl-*-tomcat.tar.gz $DIRECTORY_NAME 

mvn clean install  -Dhadoop.version=2.6.0 -DskipTests
mv redsqirl-install/target/redsqirl-*-tomcat.tar.gz $DIRECTORY_NAME 

mvn clean install  -Dhadoop.version=2.7.0 -DskipTests
mv redsqirl-install/target/redsqirl-*-tomcat.tar.gz $DIRECTORY_NAME 

mvn clean install  -Dhadoop.version=2.7.1 -DskipTests
mv redsqirl-install/target/redsqirl-*-tomcat.tar.gz $DIRECTORY_NAME 

mvn clean install  -Dhadoop.version=2.7.3 -DskipTests
mv redsqirl-install/target/redsqirl-*-tomcat.tar.gz $DIRECTORY_NAME 

#mvn clean install  -Dhadoop.version=2.7.1.2.4.4.1-5 -DskipTests
#mv redsqirl-install/target/redsqirl-*-tomcat.tar.gz $DIRECTORY_NAME 

popd


