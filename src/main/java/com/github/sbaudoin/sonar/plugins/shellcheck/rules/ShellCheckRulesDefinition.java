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
package com.github.sbaudoin.sonar.plugins.shellcheck.rules;

import com.github.sbaudoin.sonar.plugins.shellcheck.checks.CheckRepository;
import com.github.sbaudoin.sonar.plugins.shellcheck.languages.ShellLanguage;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonarsource.analyzer.commons.RuleMetadataLoader;

import java.util.ArrayList;
import java.util.List;

public class ShellCheckRulesDefinition implements RulesDefinition {
    @Override
    public void define(RulesDefinition.Context context) {
        RulesDefinition.NewRepository repository = context.createRepository(CheckRepository.REPOSITORY_KEY, ShellLanguage.KEY).setName(CheckRepository.REPOSITORY_NAME);

        RuleMetadataLoader metadataLoader = new RuleMetadataLoader(CheckRepository.RULES_DEFINITION_FOLDER);

        List<String> keys = new ArrayList<>();
        CheckRepository.getRuleKeys().forEach(keys::add);
        metadataLoader.addRulesByRuleKey(repository, keys);

        repository.done();
    }
}
