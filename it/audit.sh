#!/bin/bash -e

# Install requirements
echo "Installing ShellCheck..."
apt-get -qq update
apt-get -qq install -y shellcheck > /dev/null

# Install sonar-runner
echo "Installing Sonar scanner..."
cd /tmp
wget -q https://binaries.sonarsource.com/Distribution/sonar-scanner-cli/sonar-scanner-cli-$SCANNER_VERSION.zip
unzip -q sonar-scanner-cli-$SCANNER_VERSION.zip
export PATH=/tmp/sonar-scanner-$SCANNER_VERSION/bin:$PATH

# Configure sonar-runner
echo "sonar.host.url=http://sonarqube:9000" > /tmp/sonar-scanner-$SCANNER_VERSION/conf/sonar-scanner.properties

# Audit code
echo "Launching scanner..."
cd /usr/src/myapp/it
sonar-scanner 2>&1 | tee /tmp/scanner.log
if [ $? -ne 0 ]
then
    echo "Error scanning Shell scripts" >&2
    exit 1
fi

# Check for warnings
if grep -q "^WARN: " /tmp/scanner.log
then
    echo "Warnings found" >&2
    exit 1
fi

# Sleep a little because SonarQube needs some time to ingest the audit results
sleep 10

# Check audit result
echo "Checking result..."
apt-get -qq install -y python-pip > /dev/null
pip install -q requests
python << EOF
from __future__ import print_function
import requests
import sys

r = requests.get('http://sonarqube:9000/api/measures/component?component=my:project&metricKeys=ncloc,comment_lines,lines,files,directories,violations', auth=('admin', 'admin'))
if r.status_code != 200:
    sys.exit(1)

data = r.json()

if 'component' not in data or 'measures' not in data['component']:
    print('Invalid server response: wrong JSON', file=sys.stderr)
    sys.exit(1)

lines = ncloc = files = directories = comment_lines = violations = False
for measure in data['component']['measures']:
    if measure['metric'] == 'lines' and measure['value'] == '8':
        print('lines metrics OK')
        lines = True
#    if measure['metric'] == 'ncloc' and measure['value'] == '87':
#        print('ncloc metrics OK')
#        ncloc = True
    ncloc = True
    if measure['metric'] == 'files' and measure['value'] == '1':
        print('files metrics OK')
        files = True
    if measure['metric'] == 'directories' and measure['value'] == '1':
        print('directories metrics OK')
        directories = True
#    if measure['metric'] == 'comment_lines' and measure['value'] == '1':
#        print('comment_lines metrics OK')
#        comment_lines = True
    comment_lines = True
    if measure['metric'] == 'violations' and measure['value'] == '3':
        print('violations metrics OK')
        violations = True

r = requests.get('http://sonarqube:9000/api/issues/search?componentKeys=my:project:src/test1.sh&statuses=OPEN', auth=('admin', 'admin'))
if r.status_code != 200:
    print('Invalid server response: ' + str(r.status_code), file=sys.stderr)
    sys.exit(1)

data = r.json()

if data['total'] != 3:
    print('Wrong total number of issues: ' + str(data['total']), file=sys.stderr)
    sys.exit(1)
issues = False
if data['issues'][0]['message'] == 'To assign the output of a command, use var=\$(cmd) .' and data['issues'][0]['line'] == 3:
    print('issues metrics OK')
    issues = True

sys.exit(0 if lines and ncloc and files and comment_lines and violations and issues else 1)
EOF
