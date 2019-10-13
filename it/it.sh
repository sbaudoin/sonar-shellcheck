#!/bin/bash

export SONARQUBE_VERSION="$1"
export SCANNER_VERSION="$2"
if [ -z "$SCANNER_VERSION" ]
then
    echo "Missing parameters: <SonarQube version> <scanner version>" >&2
    exit 1
fi

export SCRIPT_DIR=`dirname $0`

# Clean-up if needed
echo "Cleanup..."
docker-compose -f $SCRIPT_DIR/docker-compose.yml down

# Start containers
echo "Starting SonarQube..."
docker-compose -f $SCRIPT_DIR/docker-compose.yml up -d sonarqube
CONTAINER_NAME=$(docker ps --format "{{.Names}}" | grep 'it_sonarqube_1.*' | head -1)
# Wait for SonarQube to be up
grep -q "SonarQube is up" <(docker logs --follow --tail 0 $CONTAINER_NAME)
# Copy the plugin
echo "Installing the plugins..."
MAVEN_VERSION=$(grep '<version>' $SCRIPT_DIR/../pom.xml | head -1 | sed 's/<\/\?version>//g'| awk '{print $1}')
docker cp $SCRIPT_DIR/../target/sonar-shellcheck-plugin-$MAVEN_VERSION.jar $CONTAINER_NAME:/opt/sonarqube/extensions/plugins
# Restart SonarQube
docker-compose -f $SCRIPT_DIR/docker-compose.yml restart sonarqube
# Wait for SonarQube to be up
grep -q "SonarQube is up" <(docker logs --follow --tail 0 $CONTAINER_NAME)
# Check plug-in installation
if ! docker exec $CONTAINER_NAME curl -su admin:admin http://localhost:9000/api/plugins/installed | python -c '
import sys
import json

data = json.loads(sys.stdin.read())
if "plugins" in data:
    for plugin in data["plugins"]:
        if plugin["key"] == "shellcheck":
            sys.exit(0)
sys.exit(1)
'
then
    echo "Plugin not installed" >&2
    exit 1
fi

# Audit code
echo "Audit test Shell scripts..."
docker-compose -f $SCRIPT_DIR/docker-compose.yml up --build --exit-code-from auditor auditor
AUDIT_STATUS=$?

# Delete containers
echo "Cleanup..."
docker-compose -f $SCRIPT_DIR/docker-compose.yml down

exit $AUDIT_STATUS
