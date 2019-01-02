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
package com.github.sbaudoin.sonar.plugins.shellcheck.languages;

import junit.framework.TestCase;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;

public class ShellQualityProfileTest extends TestCase {
    public void testDefine() {
        BuiltInQualityProfilesDefinition.Context context = new BuiltInQualityProfilesDefinition.Context();
        ShellQualityProfile qp = new ShellQualityProfile();
        qp.define(context);
        BuiltInQualityProfilesDefinition.BuiltInQualityProfile profile = context.profile("shell", "ShellCheck");
        assertNotNull(profile);
        assertTrue(profile.isDefault());
        assertEquals(1, profile.rules().size());
        assertEquals("rule1", profile.rules().get(0).ruleKey());
    }

    public void testProfileNotSetAsDefault() {
        BuiltInQualityProfilesDefinition.Context context = new BuiltInQualityProfilesDefinition.Context();

        // Register a default profile first
        DummyProfile qp1 = new DummyProfile();
        qp1.define(context);

        ShellQualityProfile qp2 = new ShellQualityProfile();
        qp2.define(context);

        BuiltInQualityProfilesDefinition.BuiltInQualityProfile profile = context.profile("shell", "ShellCheck");
        assertNotNull(profile);
        assertFalse(profile.isDefault());
    }


    private class DummyProfile implements BuiltInQualityProfilesDefinition {
        @Override
        public void define(Context context) {
            NewBuiltInQualityProfile profile = context.createBuiltInQualityProfile("Sonar Way", ShellLanguage.KEY);
            profile.setDefault(true);
            profile.done();
        }
    }
}
