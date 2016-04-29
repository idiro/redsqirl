#!/bin/bash

CONTENT_TEMPLATE1=`cat template1.html`
CONTENT_TEMPLATE2=`cat template2.html`
CONTENT_TEMPLATE3=`cat template3.html`
CONTENT_TEMPLATE4=`cat template4.html`
CONTENT_TEMPLATE5=`cat template5.html`
CONTENT_TEMPLATE6=`cat template6.html`
DIR=target/site
DIR2=target/site/help

for i in `ls ${DIR}| grep "\.html"`; do
    sed -i -e "s#<link rel=\"stylesheet\" href=\"\./css/apache\-maven\-fluido\-1\.3\.1\.min\.css\" />#${CONTENT_TEMPLATE1}#g" -e "s#<body class=\"topBarDisabled\">#${CONTENT_TEMPLATE2}#g" -e "s#<footer>#${CONTENT_TEMPLATE3}#g" -e "/apache\-maven\-fluido\-1\.3\.1\.min\.js/d" -e "s#<p id=\"poweredBy\"#<p id=\"poweredBy\" style=\"display:none;\"#g" ${DIR}/$i
done

for i in `ls ${DIR2}| grep "\.html"`; do
sed -i -e "/redsqirl\.css/d" -e "s#<head>#${CONTENT_TEMPLATE4}#g" -e "s#<body>#${CONTENT_TEMPLATE5}#g" -e "s#</body>#${CONTENT_TEMPLATE6}#g"  ${DIR2}/$i
done
