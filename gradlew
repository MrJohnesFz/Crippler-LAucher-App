#!/usr/bin/env sh
###############################################################################
# Gradle start up script for UN*X
###############################################################################

set -e

# Resolve links: follow symlinks to find the real script path
PRG="$0"
while [ -h "$PRG" ] ; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done

PRGDIR=`dirname "$PRG"`
# Get canonicalized path for APP_HOME
APP_HOME=`cd "$PRGDIR" >/dev/null && pwd`

CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

# If no wrapper jar present, print a helpful error
if [ ! -f "$CLASSPATH" ]; then
  echo "ERROR: Could not find the gradle wrapper jar ($CLASSPATH)." >&2
  echo "You must also commit 'gradle/wrapper/gradle-wrapper.jar' and 'gradle/wrapper/gradle-wrapper.properties' in the repository." >&2
  exit 1
fi

# Find java
if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
  JAVA_CMD="$JAVA_HOME/bin/java"
else
  JAVA_CMD=`command -v java 2>/dev/null || true`
  if [ -z "$JAVA_CMD" ]; then
    echo "ERROR: Java not found. Please install Java or set JAVA_HOME." >&2
    exit 1
  fi
fi

# Execute the wrapper
exec "$JAVA_CMD" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
