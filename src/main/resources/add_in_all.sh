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


SCRIPT_LOCATION=${BASH_SOURCE[0]}
SCRIPT_PATH="$(cd $(dirname "${SCRIPT_LOCATION}"); pwd -P)/$(basename "${SCRIPT_LOCATION}")"
SCRIPT_PATH="${SCRIPT_PATH%/*}"

TEMPLATE_FOLDER=`dirname ${SCRIPT_LOCATION}`/templates/
CONTENT_TEMPLATE1=`cat ${TEMPLATE_FOLDER}template1.html`
CONTENT_TEMPLATE2=`cat ${TEMPLATE_FOLDER}template2.html`
CONTENT_TEMPLATE3=`cat ${TEMPLATE_FOLDER}template3.html`
CONTENT_TEMPLATE4=`cat ${TEMPLATE_FOLDER}template4.html`
CONTENT_TEMPLATE5=`cat ${TEMPLATE_FOLDER}template5.html`
CONTENT_TEMPLATE6=`cat ${TEMPLATE_FOLDER}template6.html`
DIR=target/site
DIR2=target/site/help

for i in `ls ${DIR}| grep "\.html"`; do
    sed -i -e "s#<link rel=\"stylesheet\" href=\"\./css/apache\-maven\-fluido\-1\.3\.1\.min\.css\" />#${CONTENT_TEMPLATE1}#g" -e "s#<body class=\"topBarDisabled\">#${CONTENT_TEMPLATE2}#g" -e "s#<footer>#${CONTENT_TEMPLATE3}#g" -e "/apache\-maven\-fluido\-1\.3\.1\.min\.js/d" -e "s#<p id=\"poweredBy\"#<p id=\"poweredBy\" style=\"display:none;\"#g" ${DIR}/$i
done

for i in `ls ${DIR2}| grep "\.html"`; do
sed -i -e "/redsqirl\.css/d" -e "s#<head>#${CONTENT_TEMPLATE4}#g" -e "s#<body>#${CONTENT_TEMPLATE5}#g" -e "s#</body>#${CONTENT_TEMPLATE6}#g"  ${DIR2}/$i
done
