###
# **************************************************-
# Ingrid iPlug SNS
# ==================================================
# Copyright (C) 2014 - 2015 wemove digital solutions GmbH
# ==================================================
# Licensed under the EUPL, Version 1.1 or – as soon they will be
# approved by the European Commission - subsequent versions of the
# EUPL (the "Licence");
# 
# You may not use this work except in compliance with the Licence.
# You may obtain a copy of the Licence at:
# 
# http://ec.europa.eu/idabc/eupl5
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the Licence is distributed on an "AS IS" basis,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the Licence for the specific language governing permissions and
# limitations under the Licence.
# **************************************************#
###
#!/bin/sh
# 
##
# Environment Variables
#
#   INGRID_JAVA_HOME Overrides JAVA_HOME.
#
#   INGRID_HEAPSIZE  heap to use in mb, if not setted we use 1000.
#
#   INGRID_OPTS      addtional java runtime options
#

# resolve links - $0 may be a softlink
THIS="$0"
while [ -h "$THIS" ]; do
  ls=`ls -ld "$THIS"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '.*/.*' > /dev/null; then
    THIS="$link"
  else
    THIS=`dirname "$THIS"`/"$link"
  fi
done

# some directories
THIS_DIR=`dirname "$THIS"`
INGRID_HOME=`cd "$THIS_DIR" ; pwd`

# first sync libs
#echo '... syncronize libs from repository'
#rsync -av --update --existing $INGRID_HOME/../repository/ $INGRID_HOME/lib/


# some Java parameters
if [ "$INGRID_JAVA_HOME" != "" ]; then
  #echo "run java in $INGRID_JAVA_HOME"
  JAVA_HOME=$INGRID_JAVA_HOME
fi
  
if [ "$JAVA_HOME" = "" ]; then
  echo "Error: JAVA_HOME is not set."
  exit 1
fi

JAVA=$JAVA_HOME/bin/java

# CLASSPATH initially contains $INGRID_CONF_DIR, or defaults to $INGRID_HOME/conf
CLASSPATH=${INGRID_CONF_DIR:=$INGRID_HOME/conf}
CLASSPATH=${CLASSPATH}:$JAVA_HOME/lib/tools.jar
CLASSPATH=${CLASSPATH}:${INGRID_HOME}


# so that filenames w/ spaces are handled correctly in loops below
IFS=


# add libs to CLASSPATH
for f in $INGRID_HOME/lib/*.jar; do
  CLASSPATH=${CLASSPATH}:$f;
done
# add the step2 files to the classpath
CLASSPATH=${CLASSPATH}:$INGRID_HOME/webapp/step2/WEB-INF/classes

# restore ordinary behaviour
unset IFS


CLASS=de.ingrid.iplug.AdminServer


# cygwin path translation
if expr `uname` : 'CYGWIN*' > /dev/null; then
  CLASSPATH=`cygpath -p -w "$CLASSPATH"`
fi

INGRID_OPTS="$INGRID_OPTS -XX:+UseG1GC -XX:+UseStringDeduplication -XX:NewRatio=3"

# run it
exec "$JAVA" $INGRID_OPTS -classpath "$CLASSPATH" $CLASS 8082 webapp
