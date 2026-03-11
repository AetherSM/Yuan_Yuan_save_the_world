#!/bin/sh
# ----------------------------------------------------------------------------
# Maven Wrapper bootstrap script
# ----------------------------------------------------------------------------

set -euf

MAVEN_PROJECTBASEDIR="${MAVEN_PROJECTBASEDIR:-$(cd "$(dirname "$0")" && pwd)}"
WRAPPER_DIR="$MAVEN_PROJECTBASEDIR/.mvn/wrapper"
WRAPPER_JAR="$WRAPPER_DIR/maven-wrapper.jar"
WRAPPER_LAUNCHER="org.apache.maven.wrapper.MavenWrapperMain"

if [ ! -r "$WRAPPER_JAR" ]; then
  echo "Missing Maven wrapper jar: $WRAPPER_JAR" >&2
  echo "Please restore .mvn/wrapper/maven-wrapper.jar (or re-generate Maven Wrapper)." >&2
  exit 1
fi

JAVA_EXEC="${JAVA_HOME:-}/bin/java"
if [ "${JAVA_HOME:-}" = "" ] || [ ! -x "$JAVA_EXEC" ]; then
  JAVA_EXEC="$(command -v java || true)"
fi

if [ "$JAVA_EXEC" = "" ]; then
  echo "java not found. Please install JDK 17+ and/or set JAVA_HOME." >&2
  exit 1
fi

exec "$JAVA_EXEC" \
  ${MAVEN_OPTS:-} \
  -classpath "$WRAPPER_JAR" \
  -Dmaven.multiModuleProjectDirectory="$MAVEN_PROJECTBASEDIR" \
  $WRAPPER_LAUNCHER "$@"
