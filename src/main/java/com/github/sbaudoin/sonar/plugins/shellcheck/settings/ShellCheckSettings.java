/**
 * Copyright (c) 2018-2019, Sylvain Baudoin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.sbaudoin.sonar.plugins.shellcheck.settings;

import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;

import java.util.List;

import static java.util.Arrays.asList;

public class ShellCheckSettings {
    public static final String SHELLCHECK_PATH_KEY = "sonar.shellcheck.shellcheck.path";
    public static final String SHELLCHECK_PATH_DEFAULT_VALUE = "";
    public static final String FILE_SUFFIXES_KEY = "sonar.shell.file.suffixes";
    public static final String FILE_SUFFIXES_DEFAULT_VALUE = ".sh,.ksh,.bash";


    private ShellCheckSettings() {
    }


    public static List<PropertyDefinition> getProperties() {
        return asList(
                PropertyDefinition.builder(FILE_SUFFIXES_KEY)
                        .name("File Suffixes")
                        .description("Comma-separated list of suffixes for files to analyze.")
                        .defaultValue(FILE_SUFFIXES_DEFAULT_VALUE)
                        .multiValues(true)
                        .category("Shell")
                        .onQualifiers(Qualifiers.PROJECT)
                        .build(),
                PropertyDefinition.builder(SHELLCHECK_PATH_KEY)
                         .name("Path to shellcheck")
                        .description("Path to the shellcheck executable. Leave it empty if the command is in the system path.")
                        .defaultValue(SHELLCHECK_PATH_DEFAULT_VALUE)
                        .category("ShellCheck")
                        .onQualifiers(Qualifiers.PROJECT)
                        .build()
        );
    }
}
