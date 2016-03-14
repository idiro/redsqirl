#!/bin/bash

CONTENT_TEMPLATE1=`cat template1.html`
CONTENT_TEMPLATE2=`cat template2.html`
DIR=target/site

for i in `ls ${DIR}| grep "\.html"`; do
    sed -i -e "s#<link rel=\"stylesheet\" href=\"\./css/apache\-maven\-fluido\-1\.3\.1\.min\.css\" />#${CONTENT_TEMPLATE1}#g" -e "s#<body class=\"topBarDisabled\">#${CONTENT_TEMPLATE2}#g" ${DIR}/$i
    sed -i "/apache\-maven\-fluido\-1\.3\.1\.min\.js/d" ${DIR}/$i
done
