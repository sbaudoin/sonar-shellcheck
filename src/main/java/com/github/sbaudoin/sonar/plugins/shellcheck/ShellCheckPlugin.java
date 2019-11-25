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
package com.github.sbaudoin.sonar.plugins.shellcheck;

import com.github.sbaudoin.sonar.plugins.shellcheck.languages.ShellLanguage;
import com.github.sbaudoin.sonar.plugins.shellcheck.languages.ShellQualityProfile;
import com.github.sbaudoin.sonar.plugins.shellcheck.rules.ShellCheckRulesDefinition;
import com.github.sbaudoin.sonar.plugins.shellcheck.rules.ShellCheckSensor;
import com.github.sbaudoin.sonar.plugins.shellcheck.settings.ShellCheckSettings;
import org.sonar.api.Plugin;

import java.security.AccessControlException;
import java.util.Optional;

public class ShellCheckPlugin implements Plugin {
    public static final String ADD_SHELL_LANGUAGE_ENV_VAR = "SHELLCHECK_LANGUAGE_ADD";
    public static final String ADD_SHELL_LANGUAGE_CONF_PROP = "sonar.shell.addlanguage";


    @Override
    public void define(Context context) {
        Optional<Boolean> addLanguage = Optional.empty();
        try {
            if (System.getenv().get(ADD_SHELL_LANGUAGE_ENV_VAR) != null) {
                addLanguage = Optional.of(Boolean.parseBoolean(System.getenv().get(ADD_SHELL_LANGUAGE_ENV_VAR)));
            }
        } catch (AccessControlException exc) {
            // Do nothing on purpose: in SQ 8.0, we do not have access to system env variables
            // and we deal with that with a new SQ configuration property read below
        }
        // If no env variable found, read from SQ conf
        if (!addLanguage.isPresent()) {
            addLanguage = context.getBootConfiguration().getBoolean(ADD_SHELL_LANGUAGE_CONF_PROP);
        }
        // Install language unless instructed otherwise
        if (addLanguage.orElse(true)) {
            context.addExtension(ShellLanguage.class);
        }

        context.addExtension(ShellQualityProfile.class);

        // Add plugin settings (file extensions, etc.)
        context.addExtensions(ShellCheckSettings.getProperties());

        // Add rules
        context.addExtensions(ShellCheckRulesDefinition.class, ShellCheckSensor.class);
    }
}
