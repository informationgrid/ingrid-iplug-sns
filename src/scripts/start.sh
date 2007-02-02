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

THIS="$0"

# some directories
THIS_DIR=`dirname "$THIS"`
INGRID_HOME=`cd "$THIS_DIR" ; pwd`

# first sync libs
echo '... syncronize libs from repository'
rsync -av --update --existing $INGRID_HOME/../repository/ $INGRID_HOME/lib/

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
JAVA_HEAP_MAX=-Xmx1000m 

# check envvars which might override default args
if [ "$INGRID_HEAPSIZE" != "" ]; then
  #echo "run with heapsize $INGRID_HEAPSIZE"
  JAVA_HEAP_MAX="-Xmx""$INGRID_HEAPSIZE""m"
  #echo $JAVA_HEAP_MAX
fi

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

# restore ordinary behaviour
unset IFS


CLASS=de.ingrid.iplug.PlugServer


# cygwin path translation
if expr `uname` : 'CYGWIN*' > /dev/null; then
  CLASSPATH=`cygpath -p -w "$CLASSPATH"`
fi

# run it
#exec "$JAVA" $JAVA_HEAP_MAX $INGRID_OPTS -classpath "$CLASSPATH" $CLASS "$@"
#exec "$JAVA" $JAVA_HEAP_MAX $INGRID_OPTS -classpath "$CLASSPATH" $CLASS 8475 8476 localhost 11112
exec "$JAVA" $JAVA_HEAP_MAX $INGRID_OPTS -classpath "$CLASSPATH" $CLASS --descriptor conf/jxta.properties