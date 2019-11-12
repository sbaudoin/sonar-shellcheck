<!---
 Licensed to the Apache Software Foundation (ASF) under one or more
 contributor license agreements.  See the NOTICE file distributed with
 this work for additional information regarding copyright ownership.
 The ASF licenses this file to You under the Apache License, Version 2.0
 (the "License"); you may not use this file except in compliance with
 the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
-->
ShellCheck SonarQube Plugin
===========================

[![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/apache/maven.svg?label=License)](http://www.apache.org/licenses/LICENSE-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.sbaudoin/sonar-shellcheck-plugin.svg?label=Maven%20Central)](https://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.github.sbaudoin%22%20AND%20a%3A%22sonar-shellcheck-plugin%22)
[![Build Status](https://travis-ci.org/sbaudoin/sonar-shellcheck.svg?branch=master)](https://travis-ci.org/sbaudoin/sonar-shellcheck)
[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=com.github.sbaudoin:sonar-shellcheck-plugin&metric=alert_status)](https://sonarcloud.io/dashboard?id=com.github.sbaudoin:sonar-shellcheck-plugin)
[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=com.github.sbaudoin:sonar-shellcheck-plugin&metric=coverage)](https://sonarcloud.io/dashboard?id=com.github.sbaudoin:sonar-shellcheck-plugin)

SonarQube plugin to analyze Shell scripts with [ShellCheck](https://github.com/koalaman/shellcheck).

## Requirements
* SonarQube 6.6 or 7.0+
  
  **WARNING! This plugin is currently not compatible with SQ 8!**

* On the machine that will audit the code:
    * [ShellCheck](https://github.com/koalaman/shellcheck) 0.4.0 minimum must be installed
    * [Sonar scanner](https://github.com/SonarSource/sonar-scanner-cli) configured to point to your Sonar server

Tested on Linux.

## Installation
1. Download the [ShellCheck plugin](https://github.com/sbaudoin/sonar-shellcheck/releases)
2. Copy the plugin JAR file into the `extensions/plugins` directory of SonarQube and restart SonarQube
3. Optional: create a new quality profile to enable some rules (by default, if you do not create a custom profile, all rules are enabled)
    1. Log in SonarQube
    2. Create a new quality profile for the Shell language and enable the ShellCheck rules (search with the tag "shell")
4. Install ShellCheck and the Sonar scanner on a Linux machine. If needed, you can set the path to the `shellcheck` executable
   in the general settings of SonarQube.

## Execution
1. Prior to executing a code audit, you must create a file `sonar-project.properties` that will contain some details about your project (this is a requirement from the Sonar scanner):

    ```INI
    # must be unique in a given SonarQube instance
    sonar.projectKey=com.mycompany:my-scripts
    # this is the name and version displayed in the SonarQube UI. Was mandatory prior to SonarQube 6.1.
    sonar.projectName=A Name
    sonar.projectVersion=1.0-SNAPSHOT
    
    # Path is relative to the sonar-project.properties file. Replace "\" by "/" on Windows.
    # This property is optional if sonar.modules is set.
    sonar.sources=.
    
    # Encoding of the source code. Default is default system encoding
    #sonar.sourceEncoding=UTF-8
    ```

    You just have to do that once. Ideally, add this file along with your scripts in your preferred SCM.
2. Run the Sonar scanner from the directory where you wrote the file `sonar-project.properties`:

        sonar-scanner

3. Go to SonarQube and check the result

Subsequent scans will just required the last step to be executed. It can easily be integrated into a continuous integration pipeline.

## Known issues
### Plugin not compatible with the Sonar i-Code CNES plugin
The version 1.0.0 of this plugin appeared to be incompatible with the other [Sonar i-Code CNES plugin](https://github.com/lequal/sonar-icode-cnes-plugin) that also scans Shell script.
If you already have that plugin installed you cannot install and use this ShellCheck plugin (you will have to decide which plugin to run).
The [issue #1](https://github.com/sbaudoin/sonar-shellcheck/issues/1) has been filed to trace this incompatibility problem.

This problem was fixed in version 2.0.0. If you want to run both the Sonar i-Code CNES plugin and the ShellCheck plugin, you must set the following environment variable before starting SonarQube:

```bash
export SHELLCHECK_LANGUAGE_ADD=false
