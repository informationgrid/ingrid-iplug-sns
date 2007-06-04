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

# functions
stopIplug()
{
  echo "Try stopping ingrid component ($INGRID_HOME)..."
  PID=`ps ax | grep " $INGRID_HOME" | grep -v grep | awk '{print $1}'`
  if [ "$PID" == "" ]; then
    echo "process is not running. Exit."
    exit 1
  fi
  kill -9 "$PID"
  echo "process (pid=$PID) has been terminated."
}

stopNoExitIplug()
{
  echo "Try stopping ingrid component ($INGRID_HOME)..."
  PID=`ps ax | grep " $INGRID_HOME" | grep -v grep | awk '{print $1}'`
  if [ "$PID" == "" ]; then
    echo "process is not running."
  else
    kill -9 "$PID"
    echo "process (pid=$PID) has been terminated."
  fi
}


startIplug()
{
  echo "Try starting ingrid component ($INGRID_HOME)..."
  PID=`ps ax | grep " $INGRID_HOME" | grep -v grep | awk '{print $1}'`
  if [ "$PID" != "" ]; then
    echo "Process is still running (pid=$PID). Exit."
    exit 1
  fi
  
  echo 'syncronize libs from repository...'
  rsync -av --update --existing $INGRID_HOME/../repository/ $INGRID_HOME/lib/
  echo 'finished syncronize.'
  
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
  JAVA_HEAP_MAX=-Xmx256m
  
  # check envvars which might override default args
  if [ "$INGRID_HEAPSIZE" != "" ]; then
    JAVA_HEAP_MAX="-Xmx""$INGRID_HEAPSIZE""m"
    echo "run with heapsize $JAVA_HEAP_MAX"
  fi

  # CLASSPATH initially contains $INGRID_CONF_DIR, or defaults to $INGRID_HOME/conf
  CLASSPATH=${INGRID_CONF_DIR:=$INGRID_HOME/conf}
  CLASSPATH=${CLASSPATH}:$JAVA_HOME/lib/tools.jar
  
  # so that filenames w/ spaces are handled correctly in loops below
  IFS=
  # add libs to CLASSPATH
  for f in $INGRID_HOME/lib/*.jar; do
    CLASSPATH=${CLASSPATH}:$f;
  done
  # restore ordinary behaviour
  unset IFS
  
  # cygwin path translation
  if expr `uname` : 'CYGWIN*' > /dev/null; then
    CLASSPATH=`cygpath -p -w "$CLASSPATH"`
  fi

  CLASS=de.ingrid.iplug.PlugServer
  
  DM=`date +"%Y%m%d_%H%M%S"`
  if [ -f "$INGRID_HOME/console.log" ]
  then
    mv "$INGRID_HOME/console.log" "$INGRID_HOME/console.log.${DM}"
  fi  

  # run it
  exec nohup "$JAVA" $JAVA_HEAP_MAX $INGRID_OPTS -classpath "$CLASSPATH" $CLASS --descriptor conf/communication.properties > console.log &
  
  echo "ingrid component ($INGRID_HOME) started."
}


case "$1" in
  start)
    startIplug
    ;;
  stop)
    stopIplug
    ;;
  restart)
    stopNoExitIplug
    echo "sleep 3 sec ..."
    sleep 3
    startIplug
    ;;
  status)
    PID=`ps ax | grep " $INGRID_HOME" | grep -v grep | awk '{print $1}'`
    if [ "$PID" == "" ]; then
      echo "ingrid component ($INGRID_HOME) is not running."
    else
      echo "ingrid component ($INGRID_HOME) is running (pid=$PID)."
    fi
    ;;
  *)
    echo "Usage: $0 {start|stop|restart|status}"
    exit 1
    ;;
esac
