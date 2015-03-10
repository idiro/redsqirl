#!/bin/bash
set -e
DIRECTORY=$1
cd $DIRECTORY
mvn clean compile
