#!/bin/sh

if [ -n "$JAVA_HOME" -a -x "$JAVA_HOME/bin/java" ]; then
  JAVA=$JAVA_HOME/bin/java
elif [ -n `which java` ]; then
  JAVA=`which java`
else
  JAVA=""
fi

if [ -z "$JAVA" ]; then
  echo "Java not found!"
else
  SCRIPT_LOCATION=$0
  if [ -x "$READLINK" ]; then
    while [ -L "$SCRIPT_LOCATION" ]; do
      SCRIPT_LOCATION=`"$READLINK" -e "$SCRIPT_LOCATION"`
    done
  fi
  ANJEZ_HOME=`dirname $SCRIPT_LOCATION | xargs realpath`
  $JAVA -jar $ANJEZ_HOME/Anjez.jar
fi
