package com.github.sbaudoin.sonar.plugins.shellcheck.settings;

import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;

import java.util.List;

import static java.util.Arrays.asList;

public class ShellCheckSettings {
    public static final String SHELLCHECK_PATH_KEY = "sonar.shellcheck.shellcheck.path";
    public static final String SHELLCHECK_PATH_DEFAULT_VALUE = "";
    public static final String FILE_SUFFIXES_KEY = "sonar.shell.file.suffixes";
    public static final String FILE_SUFFIXES_DEFAULT_VALUE = ".sh,.ksh";


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
