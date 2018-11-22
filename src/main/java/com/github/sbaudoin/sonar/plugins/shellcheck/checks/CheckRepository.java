/**
 * Copyright (c) 2018, Sylvain Baudoin
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
package com.github.sbaudoin.sonar.plugins.shellcheck.checks;

import com.github.sbaudoin.sonar.plugins.shellcheck.util.FileSystem;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CheckRepository {
    public static final String REPOSITORY_KEY = "shellcheck";
    public static final String REPOSITORY_NAME = "ShellCheck";
    public static final String RULES_DEFINITION_FOLDER = "org/sonar/l10n/shellcheck/rules/shellcheck";

    private static final Logger LOGGER = Loggers.get(CheckRepository.class);
    private static final List<String> RULE_KEYS = new ArrayList<>();

    static {
        URL definitionDir = CheckRepository.class.getClassLoader().getResource(RULES_DEFINITION_FOLDER);
        if (definitionDir == null) {
            LOGGER.info("No ShellCheck rules found");
        } else {
            LOGGER.debug("Creating FileSystem for " + definitionDir);
            try (FileSystem fs = new FileSystem(definitionDir.toURI())) {
                LOGGER.debug("Reading rule definition files...");
                fs.readDirectory(definitionDir.toURI()).filter(entry -> entry.toString().endsWith(".json")).forEach(entry -> {
                    String key = getRuleKey(entry);
                    LOGGER.debug("RuleKey of {} is {}", entry.toString(), key);
                    if (htmlDescFileExists(entry)) {
                        RULE_KEYS.add(key);
                    } else {
                        LOGGER.warn("Rule {} defined but not described (.html file missing)", key);
                    }
                });
            } catch (URISyntaxException e) {
                LOGGER.error("Cannot find ShellCheck rules", e);
            } catch (IOException e) {
                LOGGER.error("Unknown error", e);
            }
        }
    }


    /**
     * Hide constructor
     */
    private CheckRepository() {
    }


    /**
     * Returns the {@code RuleKey} of the rule identified by its Id as a string
     *
     * @param ruleKey a rule key passed as a string
     * @return a {@code RuleKey} if found or {@code null}
     */
    public static RuleKey getRuleKey(String ruleKey) {
        return RuleKey.of(CheckRepository.REPOSITORY_KEY, ruleKey);
    }

    /**
     * Returns the keys of all rules supported by this plugin
     *
     * @return the keys of all rules supported by this plugin
     */
    public static List<String> getRuleKeys() {
        return RULE_KEYS;
    }


    private static String getRuleKey(Path definitionFile) {
        return definitionFile.getFileName().toString().replace(".json", "");
    }

    private static boolean htmlDescFileExists(Path definitionFile) {
        return Files.exists(definitionFile.resolveSibling(getRuleKey(definitionFile) + ".html"));
    }
}
