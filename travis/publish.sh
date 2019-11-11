#!/usr/bin/env bash
#
# Copyright 2017, 2018 Christoph Böhme
#
# Licensed under the Apache License, Version 2.0 the "License";
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

#
# Configures a GPG signing key in Gradle and invokes `./gradlew publish` to sign
# and publish all artifacts.
#

function main {
    require_no_pull_request
    require_secure_vars
    require_vars
    fetch_keyring
    configure_gradle
    ./gradlew publish getStagingProfile closeAndReleaseRepository
    clean_gradle_configuration
    remove_keyring
}

function require_no_pull_request {
    if [ -v TRAVIS_PULL_REQUEST -a "$TRAVIS_PULL_REQUEST" != "false" ]; then
        echo "Building pull request. Will not publish."
        exit 0
    fi
}

function require_secure_vars {
    if [ ! -v TRAVIS_SECURE_ENV_VARS -o "$TRAVIS_SECURE_ENV_VARS" = "false" ]; then
        echo "Skipping signing key setup"
        echo "No secure environment variables available"
        exit 0
    fi
}

function require_vars {
    local vars="KEYSERVER_URL KEYSERVER_USER KEYSERVER_PASSWORD KEYRING_FILE KEY_ID KEY_PASSWORD"
    local vars_missing=0
    for var in $vars; do
        if [ ! -v $var ]; then
            echo "Environment variable $var is missing"
            vars_missing=1
        fi
    done
    if [ $vars_missing -eq 1 ]; then
        exit 1
    fi
}

function fetch_keyring {
    KEYRING_PATH=$( pwd )/$KEYRING_FILE
    echo -n "Fetching keyring: "
    wget --user="$KEYSERVER_USER" \
         --password="$KEYSERVER_PASSWORD" \
         --no-check-certificate \
         --output-document="$KEYRING_PATH" \
         "$KEYSERVER_URL" >/dev/null 2>&1
    local status=$?
    if [ $status -ne 0 ]; then
        echo "FAILED with exit code $status"
        exit 1
    fi
    echo "OK"
}

function remove_keyring {
    echo -n "Removing keyring: "
    rm -f "$KEYRING_PATH"
    if [ $? -ne 0 ]; then
        echo "FAILED"
        exit 1
    fi
    echo "OK"
}

function configure_gradle {
    GRADLE_PROPERTIES=~/.gradle/gradle.properties
    echo -n "Configuring Gradle: "
    echo "signing.secretKeyRingFile=$KEYRING_PATH" >> $GRADLE_PROPERTIES
    echo "signing.keyId=$KEY_ID" >> $GRADLE_PROPERTIES
    echo "signing.password=$KEY_PASSWORD" >> $GRADLE_PROPERTIES
    echo "OK"
}

function clean_gradle_configuration {
    local temp_file=./gradle.properties.temp
    echo -n "Tidying up Gradle configuration: "
    grep -v "^signing\.\(secretKeyRingFile\|keyId\|password\)=" $GRADLE_PROPERTIES > $temp_file
    if [ $? -eq 2 ]; then
        echo "FAILED"
        exit 1
    fi
    mv $temp_file $GRADLE_PROPERTIES
    if [ $? -ne 0 ]; then
        echo "FAILED"
        exit 1
    fi
    echo "OK"
}

main
