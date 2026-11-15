#!/bin/sh
# ----------------------------------------------------------------------------
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#    https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
# ----------------------------------------------------------------------------

# Apache Maven Wrapper startup shell script, version 3.3.2

MAVEN_PROJECTBASEDIR="${MAVEN_BASEDIR:-$(dirname "$(readlink -f "$0")" 2>/dev/null || pwd -P)}"
WRAPPER_JAR="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"
WRAPPER_PROPERTIES="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties"
WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain
DOWNLOAD_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar

# Find Java
if [ -n "$JAVA_HOME" ]; then
    JAVA_EXE="$JAVA_HOME/bin/java"
else
    JAVA_EXE=$(which java 2>/dev/null)
    if [ -z "$JAVA_EXE" ]; then
        echo "ERROR: JAVA_HOME is not set and java could not be found in PATH." >&2
        exit 1
    fi
fi

# Download wrapper JAR if missing
if [ ! -f "$WRAPPER_JAR" ]; then
    echo "Downloading Maven Wrapper JAR..."
    if command -v curl >/dev/null 2>&1; then
        curl -o "$WRAPPER_JAR" "$DOWNLOAD_URL"
    elif command -v wget >/dev/null 2>&1; then
        wget -O "$WRAPPER_JAR" "$DOWNLOAD_URL"
    else
        echo "ERROR: curl or wget required to download the Maven Wrapper JAR." >&2
        exit 1
    fi
fi

exec "$JAVA_EXE" \
  $MAVEN_OPTS \
  -classpath "$WRAPPER_JAR" \
  "-Dmaven.multiModuleProjectDirectory=$MAVEN_PROJECTBASEDIR" \
  $WRAPPER_LAUNCHER "$@"
