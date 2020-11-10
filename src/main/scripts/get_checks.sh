#!/bin/bash

TARGET_DIR=org/sonar/l10n/shellcheck/rules/shellcheck
mkdir -p `dirname $0`/../resources/$TARGET_DIR
docker run --rm -v `pwd`/..:/mnt python:3.7-alpine3.12 sh /mnt/scripts/build_checks.sh /mnt/resources/$TARGET_DIR
