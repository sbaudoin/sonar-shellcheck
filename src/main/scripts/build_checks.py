#!/usr/bin/env python

import mistune
import re
import json
import sys
import os
import yaml
import html as html_parser


############################
# FUNCTIONS
############################
def get_config(rule_name, name):
    if 'rules' in config and rule_name in config['rules'] and name in config['rules'][rule_name]:
        return config['rules'][rule_name][name]
    return config['default'].get(name, None)


def get_tags(rule_name):
    tags_always = get_config(rule_name, 'tags_always')
    if not tags_always:
        tags_always = []
    return tags_always + get_config(rule_name, 'tags')


def get_default_severity(severity):
    return config['default']['severity'][severity]


def get_severity(rule_name):
    if 'rules' in config and rule_name in config['rules'] and 'severity' in config['rules'][rule_name]:
        return get_default_severity(config['rules'][rule_name]['severity'])
    return get_default_severity('default')


############################
# MAIN ROUTINE
############################

# Check command line arguments
if len(sys.argv) != 3:
    print('Usage: build_checks.py <path to ShellCheck Wiki pages> <YAML file with rule severities>', file=sys.stderr)
    sys.exit(1)
sc_path = sys.argv[1]
sev_file = sys.argv[2]
if not os.path.isdir(sc_path):
    print('Error: `' + sc_path + '\' does not exist or is not a directory', file=sys.stderr)
    sys.exit(1)
if not os.path.isfile(sev_file):
    print('Error: `' + sev_file + '\' does not exist or is not a file', file=sys.stderr)
    sys.exit(1)

# Load YAML configuration
try:
    config = yaml.load(open(os.path.dirname(os.path.abspath(__file__)) + '/build_checks.yml').read(), Loader=yaml.FullLoader)
except yaml.YAMLError as exc:
    print("Error in configuration file:", exc, file=sys.stderr)
    sys.exit(1)
# Complete with severities
try:
    severities = yaml.load(open(sev_file).read(), Loader=yaml.FullLoader)
    if not 'rules' in config:
        config['rules'] = {}
    for rule in severities:
        if not rule in config['rules']:
            config['rules'][rule] = {}
        config['rules'][rule]['severity'] = severities[rule]
except yaml.YAMLError as exc:
    print("Error in severities file:", exc, file=sys.stderr)
    sys.exit(1)

markdown = mistune.Markdown()

# Process SCxxxx files found in Wiki
for filename in os.listdir(sc_path):
    if not (re.match('SC[0-9]{4}\.md', filename)):
        continue

    rule = filename.replace('.md', '')

    if get_config(rule, 'ignored'):
        print('  Ignoring ', filename, '...')
        continue

    print('  Processing ', filename, '...')

    if get_config(rule, 'description'):
        print('    INFO: configured description ignored')

    # Read MD file
    md_data = open(sc_path + '/' + filename, 'r', encoding='utf-8').read()
    # Check utf-8 code points > 3 bytes
    for c in md_data:
        if c > '\uffff':
            print('    WARN: this file may contain UTF-8 code points not supported by MySQL')
            break
    md_data = re.sub('^(### .*):$', '\\1', md_data, flags=re.MULTILINE)
    md_data = re.sub('^## (.*)$', '\\1', md_data, flags=re.MULTILINE)
    md_data = re.sub('###', '##', md_data, flags=re.MULTILINE)

    # Write HTML file
    html = markdown(md_data)
    file = open(rule + '.html', 'w')
    file.write(html)
    file.close()

    # Write JSON file
    title = re.sub('<[^>]*>', '', html.splitlines()[0])
    title = re.sub('"', '\'', title)

    json_data = {
        'title': title,
        'type': get_config(rule, 'type'),
        'status': 'ready',
        'remediation': {'func': 'Constant/Issue', 'constantCost': get_config(rule, 'constantCost')},
        'tags': get_tags(rule),
        'defaultSeverity': get_severity(rule)
    }
    file = open(rule + '.json', 'w')
    file.write(json.dumps(json_data, indent=2).replace('/', '\\/'))
    file.close()

# Process rules defined in build_checks.yml if needed
for rule in config['rules']:
    if get_config(rule, 'ignored'):
        continue
    if not os.path.isfile(rule + '.html') or not os.path.isfile(rule + '.json')\
            and 'description' in config['rules'][rule]:
        print('  Generating additional files for', rule, '...')

        # Write HTML file
        file = open(rule + '.html', 'w')
        if 'html' in config['rules'][rule]:
            file.write(config['rules'][rule]['html'])
        else:
            # We cannot continue if there is no description
            if 'description' not in config['rules'][rule]:
                file.close()
                continue
            else:
                file.write('<p>')
                file.write(html_parser.escape(config['rules'][rule]['description']))
                file.write('</p>')
        file.close()

        # Write JSON file
        json_data = {}
        json_data['title'] = config['rules'][rule]['description']
        json_data['type'] = get_config(rule, 'type')
        json_data['status'] = 'ready'
        json_data['remediation'] = { 'func': 'Constant/Issue', 'constantCost': get_config(rule, 'constantCost') }
        json_data['tags'] = get_tags(rule)
        json_data['defaultSeverity'] = get_severity(rule)
        file = open(rule + '.json', 'w')
        file.write(json.dumps(json_data, indent=2).replace('/', '\\/'))
        file.close()
