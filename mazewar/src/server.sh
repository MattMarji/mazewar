#!/bin/bash
JAVA_HOME=/usr/

#if [ "$#" -ne 1 ]; then
#    echo "Usage: ./run.sh  <port>"
#    exit 1
#fi
#
#${JAVA_HOME}/bin/java Server $1 

${JAVA_HOME}/bin/java Server 4000 
