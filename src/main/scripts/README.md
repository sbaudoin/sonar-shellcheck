get_checks
==========

Script that generates the rules' documentation and configuration from ShellCheck's Wiki and code.

Description
-----------

This script uses a Docker container to download and generates the HTML and JSON files required to
configure the ShellCheck rules in SonarQube:

* `get_checks.sh/cmd`: main script. It does not take parameters, just run it as is. It runs a Docker container
  to execute `build_checks.sh`. The `src/resources/org/sonar/l10n/shellcheck/rules/shellcheck` directory
  is mounted as a volume so that the files are directly generated in the project source tree.
* `build_checks.sh`: clones the source code and Wiki to extract the rule levels and calls `build_checks.py`
* `build_checks.py`: main program that generates the HTML and JSON files for the rules
* `build_checks.yml`: configuration file for `build_checks.py`

The script will output some information if some errors or inconsistencies are found. Eg.:
* "INFO: configured description ignored": a description for the above rule was eventually found so the
  description in `build_checks.yml` was ignored, so just remove the description from `build_checks.yml`.
* "Ignoring  SCxxxx.md ...": `build_checks.yml` tells that this rule must be ignored, so...
* "SCxxxx is missing desc files": this rule exists in the source code but has no description
  in the Wiki or `build_checks.yml`. In such a case you must inspect the source code to build
  a proper description in `build_checks.yml` or decide to ignore the error.
* "SCxxxx described but does not exist": the rule has a description (in the Wiki or in `build_checks.yml`)
  but was not found in the source code. In this case you must check the source code to determine
  what to do: the rule might not have been properly identified in the source code or just delete
  the rule files.

If you change `build_checks.yml`, it is strongly advised to execute again `get_checks.sh` (or `get_checks.cmd`
if you are under Windows). The script is very useful but some manual fixing will certainly be required, so wait
for the automated stuff to work as expected before doing the final, manual fixing.

Configuration
-------------

`build_checks.yml` defines the behaviour/configuration on a rule basis. Because there are tens of rules
in ShellCheck, not all rules are defined. Instead default settings (block `default`) are defined and
are overridable per rule (`rules` block) if the values cannot be extracted from the source code or
Wiki.

```yaml
default:
  type: CODE_SMELL
  constantCost: 2min
  tags_always:
    - shell
  tags:
    - convention
  severity:
    default: Major
    style: Info
    info: Info
    error: Blocker
    warning: Major

rules:
  SCxxxx:
    description: "Rule description"
    attribute: value
    ...
  SCxxxx:
    ...
  ...
```

* Default parameters:

  | Parameter      | Description                                                     |
  |----------------|-----------------------------------------------------------------|
  | `type`         | The SonarQube rule type. Known types:                           |
  |                | `BUG`, `CODE_SMELL`, `VULNERABILITY`.                           |
  | `constantCost` | the remediation cost in minutes. Examples: `2min`, `60mn`.      |
  | `tags_always`  | List of tags that will always be assigned to rules.             |
  | `tags`         | Default list of tags. Overridable: if defined for a rule, this  |
  |                | list is replaced contrary to the `tag_always` list that is      |
  |                | always assigned.                                                |
  | `severity`     | Mapping between the ShellCheck and SonarQube severity levels.   |
  |                | The keys are  the ShellCheck levels; the values are the         | 
  |                | SonarQube levels.                                               |

* Rule parameters: they are optional

  | Parameter      | Description                                                     |
  |----------------|-----------------------------------------------------------------|
  | `ignored`      | Boolean. Set to `True` to ignore this rule.                     |
  | `description`  | The (short) rule description if not found in the Wiki.          |
  | `type`         | The SonarQube rule type. Known types: see above.                |
  | `html`         | HTML body of the help page of the rule.                         |
  | `constantCost` | the remediation cost in minutes. Examples: `2min`, `60mn`.      |
  | `tags`         | the tags for the rule.                                          |
  | `severity`     | The ShellCheck severity level for the rule.                     |
