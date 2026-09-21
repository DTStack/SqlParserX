#!/bin/bash
source /etc/profile
unset CDPATH
export basedir=$(cd `dirname $0`/..; pwd)
mkdir -p $basedir/logs
mkdir -p $basedir/run

# 启动脚本
java \
-Xdebug \
-Xrunjdwp:server=y,transport=dt_socket,address=0.0.0.0:40889,suspend=n \
-cp /opt/tools/sqlparserx/lib/*:/opt/tools/sqlparserx/dtstack/* \
com.dtstack.sqlparserx.SqlParserXWebApplication

java -cp /Users/edy/changlu_workspace/daishuyun/code/diy/testsqlparserx/lib/*:/Users/edy/changlu_workspace/daishuyun/code/diy/testsqlparserx/dtstack/* com.dtstack.sqlparserx.SqlParserXWebApplication
