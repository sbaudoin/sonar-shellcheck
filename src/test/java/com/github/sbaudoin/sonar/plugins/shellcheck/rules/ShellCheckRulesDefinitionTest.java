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
package com.github.sbaudoin.sonar.plugins.shellcheck.rules;

import com.github.sbaudoin.sonar.plugins.shellcheck.checks.CheckRepository;
import com.github.sbaudoin.sonar.plugins.shellcheck.languages.ShellLanguage;
import junit.framework.TestCase;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinition.Rule;

public class ShellCheckRulesDefinitionTest extends TestCase {
    public void testDefine() {
        ShellCheckRulesDefinition rulesDefinition = new ShellCheckRulesDefinition();
        RulesDefinition.Context context = new RulesDefinition.Context();
        rulesDefinition.define(context);
        RulesDefinition.Repository repository = context.repository(CheckRepository.REPOSITORY_KEY);

        assertEquals(CheckRepository.REPOSITORY_NAME, repository.name());
        assertEquals(ShellLanguage.KEY, repository.language());
        assertEquals(CheckRepository.getRuleKeys().size(), repository.rules().size());

        Rule aRule = repository.rule("SC1000");
        assertNull(aRule);
        aRule = repository.rule("rule1");
        assertNotNull(aRule);
        assertEquals("Any rule", aRule.name());

        for (Rule rule : repository.rules()) {
            for (RulesDefinition.Param param : rule.params()) {
                assertFalse("Description for " + param.key() + " should not be empty", "".equals(param.description()));
            }
        }
    }
}
