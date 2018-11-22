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
