/**
 * Copyright (c) 2018-2020, Sylvain Baudoin
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
package com.github.sbaudoin.sonar.plugins.shellcheck.languages;

import com.github.sbaudoin.sonar.plugins.shellcheck.checks.CheckRepository;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;

import java.util.Map;

/**
 * Default, built-in quality profile for the projects having Shell scripts
 */
public class ShellQualityProfile implements BuiltInQualityProfilesDefinition {
    @Override
    public void define(Context context) {
        NewBuiltInQualityProfile profile = context.createBuiltInQualityProfile("ShellCheck", ShellLanguage.KEY);
        // Try to set this profile as default
        boolean hasDefault = false;
        Map<String, BuiltInQualityProfile> profiles = context.profilesByLanguageAndName().get(ShellLanguage.KEY);
        if (profiles != null) {
            for (BuiltInQualityProfile p : profiles.values()) {
                if (p.isDefault()) {
                    hasDefault = true;
                    break;
                }
            }
        }
        if (!hasDefault) {
            profile.setDefault(true);
        }

       // All rules
        CheckRepository.getRuleKeys().forEach(key -> profile.activateRule(CheckRepository.REPOSITORY_KEY, key));

        profile.done();
    }
}
