#!/bin/bash
source /etc/profile
unset CDPATH
export basedir=$(cd `dirname $0`/..; pwd)
mkdir -p $basedir/logs
mkdir -p $basedir/run

# 启动脚本
java \
-Xdebug \
-Xrunjdwp:server=y,transport=dt_socket,address=*:40569,suspend=n \
-jar SqlParserX-web-0.0.1-SNAPSHOT.jar
